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
package org.jboss.errai.todomvc.server;

import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.server.JavaReflectionAttributeAccessor;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * A stateless EJB implementing the DAO (Data Access Object) pattern for {@link TodoItem} objects.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TodoItemRepository {

    /**
     * A JPA EntityManager which is configured according to the
     * {@code default} persistence context defined in
     * {@code META-INF/persistence.xml}. Note that this field is not initialized
     * by the application: it is injected by the EJB container.
     */
    @PersistenceContext(unitName = "default")
    private EntityManager em;

    /**
     * Stores the given new item in the database, assigning it a unique ID.
     * When this method returns, the given entity object will have its ID property set.
     *
     * @throws EntityExistsException If the given item is already in the database.
     */
    public void create(TodoItem entity) {
        em.persist(entity);
    }

    /**
     * Updates the state of the given item in the database.
     *
     * @param id     The unique identifier for the given item.
     * @param entity The item update the database with.
     */
    public void update(Long id, TodoItem entity) {
        entity.setId(id);
        em.merge(entity);
    }

    /**
     * Removes the item with the given ID from the database.
     *
     * @param id The unique ID of the item to delete. Must not be null.
     *
     * @throws IllegalArgumentException if {@code id} is null, or if there is no item with that ID in the database.
     */
    public void delete(Long id) {
        TodoItem item = em.find(TodoItem.class, id);
        em.remove(item);
    }

    /**
     * Passes a data sync operation on the given data set to the server-side of
     * the Errai DataSync system.
     * <p>
     * This method is not invoked directly by the application code; it is called
     * via Errai RPC by Errai's ClientSyncManager.
     *
     * @param dataSet       The data set to synchronize.
     * @param remoteResults The remote results produced by ClientSyncManager, which the
     *                      server-side needs to perform to synchronize the server data with
     *                      the client data.
     *
     * @return A list of sync response operations that ClientSyncManager needs to
     * perform to synchronize the client data with the server data.
     */
    public <X> List<SyncResponse<X>> sync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults) {
        JpaAttributeAccessor attributeAccessor = new JavaReflectionAttributeAccessor();
        DataSyncService dss = new org.jboss.errai.jpa.sync.server.DataSyncServiceImpl(em, attributeAccessor);
        return dss.coldSync(dataSet, remoteResults);
    }
}
