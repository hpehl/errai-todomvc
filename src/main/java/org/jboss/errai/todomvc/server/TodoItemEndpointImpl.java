package org.jboss.errai.todomvc.server;

import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.todomvc.client.shared.TodoItemEndpoint;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * A stateless EJB implementing the REST endpoint to create, update and delete {@link TodoItem}s.
 */
@SuppressWarnings("unused")
@Stateless
public class TodoItemEndpointImpl implements TodoItemEndpoint {

    @Inject
    private Event<TodoItem> created;

    @Inject
    private TodoItemRepository repository;

    @Override
    public Response create(TodoItem entity) {
        repository.create(entity);
        created.fire(entity);
        URI uri = UriBuilder.fromResource(TodoItemEndpoint.class).path(String.valueOf(entity.getId())).build();
        return Response.created(uri).build();
    }

    @Override
    public Response update(Long id, TodoItem entity) {
        repository.update(id, entity);
        return Response.noContent().build();
    }

    @Override
    public Response delete(Long id) {
        repository.delete(id);
        return Response.noContent().build();
    }

}