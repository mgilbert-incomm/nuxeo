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

import java.util.List;

import org.nuxeo.ecm.platform.oauth2.enums.NuxeoOAuth2TokenType;

/**
 * The OAuth2 token service that manages tokens. An OAuth2 token can be:
 * <ul>
 * <li>Provided by Nuxeo, it's the OAuth2 server provider.</li>
 * <li>Consumed by Nuxeo, it's a client of another server provider.</li>
 * </ul>
 *
 * @since 11.1
 */
public interface OAuth2TokenService {
    String TOKEN_DIR = "oauth2Tokens";

    /**
     * Get the OAuth2 tokens.
     *
     * @return the oAuth2 tokens
     */
    List<NuxeoOAuth2Token> getTokens();

    /**
     * Get the OAuth2 tokens.
     *
     * @param nxuser the nuxeo user
     * @return the oAuth2 tokens
     */
    List<NuxeoOAuth2Token> getTokens(String nxuser);

    /**
     * Get the OAuth2 tokens.
     *
     * @param type the token type {@link NuxeoOAuth2TokenType}
     * @return the oAuth2 tokens
     */
    List<NuxeoOAuth2Token> getTokens(NuxeoOAuth2TokenType type);

    /**
     * Get the OAuth2 tokens.
     *
     * @param nxuser the nuxeo user
     * @param type the token type {@link NuxeoOAuth2TokenType}
     * @return the oAuth2 tokens
     */
    List<NuxeoOAuth2Token> getTokens(String nxuser, NuxeoOAuth2TokenType type);
}
