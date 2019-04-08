/*
 * (C) Copyright 2014-2018 Nuxeo (http://nuxeo.com/) and others.
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
 *     dmetzler
 */

package org.nuxeo.ecm.restapi.server.jaxrs;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.io.APIVersion;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;

/**
 * The root entry for the WebEngine module.
 *
 * @since 5.7.2
 */
@Path("/api/v{apiVersion}")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "APIRoot")
public class APIRoot extends ModuleRoot {

    @Path("/")
    public Object route(@PathParam("apiVersion") int apiVersion) {
        APIVersion.set(apiVersion);
        return newObject("apiObject");
    }

}
