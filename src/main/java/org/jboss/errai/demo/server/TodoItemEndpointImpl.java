package org.jboss.errai.demo.server;

import org.jboss.errai.demo.client.shared.TodoItem;
import org.jboss.errai.demo.client.shared.TodoItemEndpoint;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * A stateless EJB implementing the REST endpoint to create, update and delete {@link TodoItem}s.
 */
@Stateless
@SuppressWarnings("unused")
public class TodoItemEndpointImpl implements TodoItemEndpoint {

    @Inject
    private Event<TodoItem> created;

    @Inject
    private TodoItemRepository repository;

    @Override
    public Response create(TodoItem entity) {
        repository.create(entity);
        created.fire(entity);
        return Response.created(
                UriBuilder.fromResource(TodoItemEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
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