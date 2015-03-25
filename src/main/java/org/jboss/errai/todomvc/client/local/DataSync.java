/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.todomvc.client.local;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.todomvc.client.shared.TodoItem;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.jboss.errai.todomvc.client.shared.QueryName.ALL;

/**
 * Helper class for full two-way data synchronization between the client and the
 * server.
 */
public class DataSync {

    /**
     * The Errai Data Sync helper class which allows us to initiate a data
     * synchronization with the server.
     */
    @Inject
    private ClientSyncManager syncManager;

    /**
     * Performs a full two-way data synchronization between the client and the
     * server: the server gets all new and updated objects from us,
     * and we get all new and updated objects from the server.
     *
     * @param callback the callback to invoked upon completion of the data sync request.
     */
    public void sync(RemoteCallback<List<SyncResponse<TodoItem>>> callback) {
        syncManager.coldSync(ALL.query(), TodoItem.class, Collections.<String, Object>emptyMap(), callback, null);
    }
}
