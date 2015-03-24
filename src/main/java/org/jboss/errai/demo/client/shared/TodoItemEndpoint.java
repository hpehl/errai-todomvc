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
package org.jboss.errai.demo.client.shared;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This JAX-RS resource interface is used on both the client and the server. On
 * the server, it is implemented to expose the described resource methods as
 * HTTP endpoints. On the client, the interface can be used to construct
 * type safe remote method calls without having to worry about implementing the
 * request or serialization logic.
 */
public interface TodoItemEndpoint {

    @POST
    @Consumes("application/json")
    Response create(TodoItem entity);

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/json")
    Response update(@PathParam("id") Long id, TodoItem entity);

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    Response delete(@PathParam("id") Long id);
}
