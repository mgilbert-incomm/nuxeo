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
 *     Thomas Roger
 */

package org.nuxeo.ecm.core.io;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * Object used to store the REST API version, if any, for a given request.
 *
 * @since 11.1
 */
public class APIVersion {

    public static final int V1 = 1;

    public static final int V2 = 2;

    public static final List<Integer> VALID_VERSIONS = Arrays.asList(V1, V2);

    private static final ThreadLocal<Integer> VERSION = new ThreadLocal<>();

    /**
     * Sets the REST API version.
     */
    public static void set(int version) {
        if (!VALID_VERSIONS.contains(version)) {
            throw new NuxeoException(
                    String.format("%s is not part of the valid versions: %s", version, APIVersion.VALID_VERSIONS),
                    SC_BAD_REQUEST);
        }
        VERSION.set(version);
    }

    /**
     * Returns the REST API version if known, {@code -1} otherwise.
     */
    public static int get() {
        return Objects.requireNonNullElse(VERSION.get(), -1);
    }

    private APIVersion() {
        // not allowed
    }
}
