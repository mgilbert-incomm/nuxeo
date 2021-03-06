/*
 * (C) Copyright 2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Kevin Leturc <kleturc@nuxeo.com>
 */
package org.nuxeo.ecm.core.io.impl.transformers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.schema.PropertyDeprecationHandler;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.schema.TypeConstants;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.runtime.api.Framework;

/**
 * This is a {@link DocumentTransformer} which removes property marked as removed in deprecation system.
 *
 * @since 9.2
 */
public class PropertyDeprecationRemover implements DocumentTransformer {

    protected final SchemaManager schemaManager;

    protected final PropertyDeprecationHandler removeHandler;

    public PropertyDeprecationRemover() {
        schemaManager = Framework.getService(SchemaManager.class);
        removeHandler = schemaManager.getRemovedProperties();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean transform(ExportedDocument xdoc) throws IOException {
        Element root = xdoc.getDocument().getRootElement();
        for (Element schema : root.elements("schema")) {
            String schemaName = schema.attributeValue("name");
            if (removeHandler.hasMarkedProperties(schemaName)) {
                // schema has removed properties - get them
                Set<String> props = removeHandler.getProperties(schemaName);
                for (String prop : props) {
                    handleProperty(schema, prop);
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected void handleProperty(Element schema, String propertyToRemove) {
        String schemaName = schema.attributeValue("name");

        // build namespace uri for input schema
        String namespaceURI = "http://www.nuxeo.org/ecm/schemas/" + schemaName + '/';
        String schemaPrefix = Optional.ofNullable(schema.getNamespaceForURI(namespaceURI))
                                      .map(Namespace::getPrefix)
                                      .filter(StringUtils::isNotBlank)
                                      .map(p -> p + ':')
                                      .orElse(StringUtils.EMPTY);

        String fallback = removeHandler.getFallback(schemaName, propertyToRemove);
        // check if the fallback is inside a blob
        Field fallbackField = schemaManager.getField(schemaPrefix + fallback);
        if (fallbackField != null && TypeConstants.isContentType(fallbackField.getDeclaringType())) {
            // as we export blob with the property "filename" instead of "name", which differ from schema definition,
            // we need to replace the "name" property name
            fallback = fallback.replace("/name", "/filename");
        }

        // handle list and other elements
        int starIndex = propertyToRemove.indexOf('*');
        if (starIndex < 0) {
            // removed property is not in a list
            Element elementToRemove = (Element) schema.selectSingleNode(schemaPrefix + propertyToRemove);
            if (elementToRemove != null) {
                moveAndDetachProperty(schema, schema, elementToRemove, fallback);
            }
        } else {
            // removed property is in a list
            List<Element> elementsToRemove = (List<Element>) (List<?>) schema.selectNodes(schemaPrefix + propertyToRemove);
            // compute number of times we need to get parent - here we only handle one list level (the last one)
            // we assume that fallback of a list of complex is inside the same complex property
            int count = StringUtils.countMatches(propertyToRemove.substring(starIndex), "/");
            // compute a new fallback
            String newFallback = fallback;
            if (newFallback != null) {
                // we want to skip "*/"
                newFallback = newFallback.substring(newFallback.lastIndexOf("*/") + 2);
            }
            for (Element elementToRemove : elementsToRemove) {
                Element parent = elementToRemove;
                for (int i = 0; i < count; i++) {
                    parent = parent.getParent();
                }
                moveAndDetachProperty(schema, parent, elementToRemove, newFallback);
            }
        }
    }

    protected void moveAndDetachProperty(Element schema, Element parent, Element elementToRemove, String fallback) {
        if (fallback != null) {
            // as we don't currently handle fallback to another schema, we can move content easily
            String[] fallbackSegments = fallback.split("/");
            for (String fallbackSegment : fallbackSegments) {
                QName qName;
                Element element;
                if (parent == schema) {
                    // first element has a namespace
                    qName = QName.get(fallbackSegment, schema.getNamespaceForPrefix(schema.attributeValue("name")));
                } else {
                    // children don't have namespace
                    qName = QName.get(fallbackSegment);
                }
                element = parent.element(qName);
                // create element if it doesn't exist
                if (element == null) {
                    element = DocumentHelper.createElement(qName);
                    parent.add(element);
                }
                parent = element;
            }
            // move content to last element if it doesn't has content - removed properties don't
            // override fallback
            if (!parent.hasContent()) {
                parent.setContent(elementToRemove.content());
            }
        }
        // finally detach removed property
        elementToRemove.detach();
    }

}
