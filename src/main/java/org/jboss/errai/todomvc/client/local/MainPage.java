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

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.todomvc.client.shared.QueryNames;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.todomvc.client.shared.TodoItemEndpoint;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static org.jboss.errai.todomvc.client.shared.QueryNames.ALL;

/**
 * This is the companion Java class of the main page as specified by
 * {@link Templated}. It refers to the HTML inside MainPage.html.
 * <p>
 * The {@link Page} annotation declares this form as a bookmarkable page that
 * can be transitioned to by other pages of this application. Further the
 * specified role (DefaultPage.class) make this page appear by default when the
 * application is started.
 */
@Templated("#root")
@Page(role = DefaultPage.class, path = "index.html")
public class MainPage extends Composite {

    @Inject Logger logger;

    /**
     * Errai's JAX-RS module generates a stub class that makes AJAX calls back to
     * the server for each resource method on the {@link TodoItemEndpoint}
     * interface. The paths and HTTP methods for the AJAX calls are determined
     * automatically based on the JAX-RS annotations ({@code @Path}, {@code @GET},
     * {@code @POST}, and so on) on the resource.
     */
    @Inject Caller<TodoItemEndpoint> endpoint;

    @PageState String filter;
    @Inject DataSync dataSync;
    @Inject EntityManager em;
    @Inject TodoMessages messages;

    @Inject @DataField TextBox newTodo;
    @DataField Element toggleAll = Document.get().createCheckInputElement();
    @Inject @DataField TodoItemsWidget todoItems;
    @Inject @DataField InlineLabel todoCount;
    @Inject @DataField Button clearCompleted;

    // for the click handlers
    @Inject @DataField InlineHyperlink allTodos;
    @Inject @DataField InlineHyperlink activeTodos;
    @Inject @DataField InlineHyperlink completedTodos;

    @PageShown
    void sync() {
        dataSync.sync(response -> {
            logger.debug("Received sync response:" + response);
            refresh();
        });
    }

    void refresh() {
        List<TodoItem> items = loadTodos();
        if (items.isEmpty()) {
            // hide main section and footer
            toggleAll.getParentElement().getStyle().setDisplay(NONE);
            todoCount.getElement().getParentElement().getStyle().setDisplay(NONE);
        } else {
            toggleAll.getParentElement().getStyle().clearDisplay();
            todoCount.getElement().getParentElement().getStyle().clearDisplay();
        }
        switch (queryName()) {
            case ALL:
                allTodos.addStyleName("selected");
                activeTodos.removeStyleName("selected");
                completedTodos.removeStyleName("selected");
                break;
            case ACTIVE:
                allTodos.removeStyleName("selected");
                activeTodos.addStyleName("selected");
                completedTodos.removeStyleName("selected");
                break;
            case COMPLETED:
                allTodos.removeStyleName("selected");
                activeTodos.removeStyleName("selected");
                completedTodos.addStyleName("selected");
                break;
        }
        todoItems.setItems(items);
        todoCount.setText(messages.todoItems(items.size()));
        clearCompleted.setText(messages.clearCompleted(items.size()));
    }

    List<TodoItem> loadTodos() {
        TypedQuery<TodoItem> query = em.createNamedQuery(queryName().query(), TodoItem.class);
        return query.getResultList();
    }

    QueryNames queryName() {
        return Strings.isNullOrEmpty(filter) ? ALL : Enums.getIfPresent(QueryNames.class, filter.toUpperCase()).or(ALL);
    }

    @EventHandler("toggleAll")
    void onToggleAll(ClickEvent event) {
        List<TodoItem> items = loadTodos();
        for (TodoItem item : items) {
            item.setDone(!item.isDone());
            em.persist(item);
        }
        em.flush();
        dataSync.sync();
    }

    @EventHandler("newTodo")
    void onNewItem(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KEY_ENTER && !newTodo.getText().trim().equals("")) {
            TodoItem item = new TodoItem();
            item.setText(newTodo.getText());
            endpoint.call(new ResponseCallback() {
                @Override
                public void callback(final Response response) {
                    if (response.getStatusCode() == SC_CREATED) {
                        sync();
                    }
                }
            }).create(item);
            newTodo.setText("");
        }
    }
}
