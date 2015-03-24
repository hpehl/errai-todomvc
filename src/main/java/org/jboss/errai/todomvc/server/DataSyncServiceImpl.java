package org.jboss.errai.todomvc.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * An Errai RPC service which is called by the client when it wishes to
 * synchronize the TodoItems between itself and the server.
 */
@Service
@ApplicationScoped
public class DataSyncServiceImpl implements DataSyncService {

    /**
     * An EJB responsible for getting the JPA EntityManager and for transaction demarcation.
     */
    @Inject
    private TodoItemRepository repository;

    @Override
    public <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults) {
        return repository.sync(dataSet, remoteResults);
    }
}