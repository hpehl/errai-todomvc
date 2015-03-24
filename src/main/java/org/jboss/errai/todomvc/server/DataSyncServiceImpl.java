package org.jboss.errai.todomvc.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.jpa.sync.client.shared.ConflictResponse;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.client.shared.UpdateResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.List;

/**
 * An Errai RPC service which is called by the client when it wishes to
 * synchronize the _todo items between itself and the server.
 */
@Service
@ApplicationScoped
public class DataSyncServiceImpl implements DataSyncService {

    /**
     * An EJB responsible for getting the JPA EntityManager and for transaction demarcation.
     */
    @Inject
    private TodoItemRepository repository;

    /**
     * A CDI event source that fires TodoItem instances to observers, both on the server and on clients.
     */
    @Inject
    private Event<TodoItem> updated;

    @Override
    public <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults) {

        // First, let the TodoItemRepository EJB do the sync (it gets the correct
        // EntityManager and also handles transactions)
        List<SyncResponse<X>> response = repository.sync(dataSet, remoteResults);

        // Now fire a CDI event for each UserComplaint which was updated as a result of this sync
        for (SyncResponse<X> syncRequestResponse : response) {
            if (syncRequestResponse instanceof UpdateResponse) {
                TodoItem newItem = (TodoItem) ((UpdateResponse<?>) syncRequestResponse).getEntity();
                updated.fire(newItem);
            } else if (syncRequestResponse instanceof ConflictResponse) {
                TodoItem newItem = (TodoItem) ((ConflictResponse<?>) syncRequestResponse).getActualNew();
                updated.fire(newItem);
            }
        }

        // Finally, return the list of sync responses to the client, whose sync
        // manager will update the client database
        return response;
    }
}