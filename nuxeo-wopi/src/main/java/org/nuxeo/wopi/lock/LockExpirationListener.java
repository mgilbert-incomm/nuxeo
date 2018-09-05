/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/) and others.
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
 *     Antoine Taillefer <ataillefer@nuxeo.com>
 */
package org.nuxeo.wopi.lock;

import static org.nuxeo.wopi.Constants.LOCK_DIRECTORY_DOC_ID;
import static org.nuxeo.wopi.Constants.LOCK_DIRECTORY_FILE_ID;
import static org.nuxeo.wopi.Constants.LOCK_DIRECTORY_NAME;
import static org.nuxeo.wopi.Constants.LOCK_DIRECTORY_SCHEMA_NAME;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CloseableCoreSession;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * Handles expired WOPI locks by unlocking the related document and removing the stored lock.
 *
 * @since 10.3
 */
public class LockExpirationListener implements PostCommitEventListener {

    private static final Log log = LogFactory.getLog(LockExpirationListener.class);

    @Override
    public void handleEvent(EventBundle eventBundle) {
        Framework.doPrivileged(() -> {
            try (Session directorySession = Framework.getService(DirectoryService.class).open(LOCK_DIRECTORY_NAME)) {
                LockHelper.getExpiredLocksByRepository().entrySet().forEach(entry -> {
                    String repository = entry.getKey();
                    List<DocumentModel> lockEntries = entry.getValue();
                    try (CloseableCoreSession session = CoreInstance.openCoreSession(repository)) {
                        lockEntries.forEach(lockEntry -> handleExpiredLock(session, directorySession, lockEntry));
                    }
                });
            }
        });

    }

    protected void handleExpiredLock(CoreSession session, Session directorySession, DocumentModel entry) {
        String docId = (String) entry.getProperty(LOCK_DIRECTORY_SCHEMA_NAME, LOCK_DIRECTORY_DOC_ID);
        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "WOPI lock expired for document %s/%s, unlocking document and removing lock from directory.",
                    session.getRepositoryName(), docId));
        }
        // unlock document
        session.removeLock(new IdRef(docId));
        // remove WOPI lock from directory
        directorySession.deleteEntry((String) entry.getProperty(LOCK_DIRECTORY_SCHEMA_NAME, LOCK_DIRECTORY_FILE_ID));
    }

}