/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Salem Aouana
 */

package org.nuxeo.ecm.platform.oauth2.enums;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.nuxeo.ecm.platform.oauth2.Constants.TOKEN_SERVICE;

import java.util.Arrays;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.query.sql.model.Predicate;
import org.nuxeo.ecm.core.query.sql.model.Predicates;

/**
 * OAuth2 token type can be:
 * <ul>
 * <li>Provided by Nuxeo: {@link #AS_PROVIDER}</li>
 * <li>Consumed by Nuxeo: {@link #AS_CLIENT}</li>
 * </ul>
 *
 * @since 11.1
 */
public enum NuxeoOAuth2TokenType {

    AS_PROVIDER("asProvider") {
        @Override
        public Predicate getPredicate() {
            return Predicates.eq("serviceName", TOKEN_SERVICE);
        }
    },

    AS_CLIENT("asClient") {
        @Override
        public Predicate getPredicate() {
            return Predicates.noteq("serviceName", TOKEN_SERVICE);
        }
    };

    NuxeoOAuth2TokenType(String value) {
        this.value = value;
    }

    public abstract Predicate getPredicate();

    public static NuxeoOAuth2TokenType fromValueType(String value) {
        return Arrays.stream(values())
                     .filter(type -> type.getValue().equals(value))
                     .findAny()
                     .orElseThrow(() -> new NuxeoException(String.format("Undefined oAuth2 type for value '%s'", value),
                             SC_NOT_FOUND));

    }

    public String getValue() {
        return value;
    }

    protected final String value;
}
