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

package org.nuxeo.ecm.platform.oauth2.tokens;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.query.sql.model.Predicates;
import org.nuxeo.ecm.core.query.sql.model.QueryBuilder;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.oauth2.enums.NuxeoOAuth2TokenType;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * The implementation that manages the tokens in Nuxeo.
 *
 * @since 11.1
 */
public class OAuth2TokenServiceImpl extends DefaultComponent implements OAuth2TokenService {

    @Override
    public List<NuxeoOAuth2Token> getTokens() {
        return findTokens(null, null);
    }

    @Override
    public List<NuxeoOAuth2Token> getTokens(String nxuser) {
        requireNonNull(nxuser, "nxuser cannot be null");

        return findTokens(nxuser, null);
    }

    @Override
    public List<NuxeoOAuth2Token> getTokens(NuxeoOAuth2TokenType type) {
        requireNonNull(type, "oAuth2TokenType cannot be null");

        return findTokens(null, type);
    }

    @Override
    public List<NuxeoOAuth2Token> getTokens(String nxuser, NuxeoOAuth2TokenType type) {
        requireNonNull(nxuser, "nxuser cannot be null");
        requireNonNull(type, "oAuth2TokenType cannot be null");

        return findTokens(nxuser, type);
    }

    protected List<NuxeoOAuth2Token> findTokens(String nxuser, NuxeoOAuth2TokenType type) {
        return Framework.doPrivileged(() -> {
            DirectoryService ds = Framework.getService(DirectoryService.class);
            try (Session session = ds.open(TOKEN_DIR)) {
                QueryBuilder queryBuilder = getQueryBuilder(nxuser, type);
                List<DocumentModel> documents = session.query(queryBuilder, false);
                return documents.stream().map(NuxeoOAuth2Token::new).collect(Collectors.toList());
            }
        });
    }

    protected QueryBuilder getQueryBuilder(String nxuser, NuxeoOAuth2TokenType type) {
        QueryBuilder queryBuilder = new QueryBuilder();

        if (nxuser != null) {
            queryBuilder.predicate(Predicates.eq(NuxeoOAuth2Token.KEY_NUXEO_LOGIN, nxuser));
        }
        if (type != null) {
            queryBuilder.predicate(type.getPredicate());
        }
        return queryBuilder;
    }

}
