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
import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.todomvc.client.shared.QueryName;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.todomvc.client.shared.TodoItemEndpoint;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static org.jboss.errai.todomvc.client.shared.QueryName.*;

/**
 * This is the companion Java class of the main page as specified by
 * {@link Templated}. It refers to the HTML inside MainPage.html#root.
 * <p>
 * The {@link Page} annotation declares this form as a bookmarkable page that
 * can be transitioned to by other pages of this application. Further the
 * specified role (DefaultPage.class) make this page appear by default when the
 * application is started.
 */
@Templated("#root")
@Page(role = DefaultPage.class, path = "index.html")
public class MainPage extends Composite {

    @PageState String filter;

    /**
     * Errai's JAX-RS module generates a stub class that makes AJAX calls back to
     * the server for each resource method on the {@link TodoItemEndpoint}
     * interface. The paths and HTTP methods for the AJAX calls are determined
     * automatically based on the JAX-RS annotations ({@code @Path}, {@code @GET},
     * {@code @POST}, and so on) on the resource.
     */
    @Inject Caller<TodoItemEndpoint> endpoint;

    @Inject DataSync dataSync;
    @Inject EntityManager em;
    @Inject TodoMessages messages;
    @Inject Navigation navigation;

    @Inject @DataField TextBox newTodo;
    @DataField Element toggleAll = Document.get().createCheckInputElement();
    @Inject @DataField TodoItemsWidget todoItems;
    @Inject @DataField InlineLabel todoCount;
    @Inject @DataField Button clearCompleted;

    @Inject @DataField Anchor allTodos;
    @Inject @DataField Anchor activeTodos;
    @Inject @DataField Anchor completedTodos;


    // ------------------------------------------------------ sync and refresh

    @PageShown
    void sync() {
        dataSync.sync(response -> refresh());
    }

    @SuppressWarnings("UnusedParameters")
    void changedItem(@Observes TodoItem changed) {
        sync(); // simply reload all items
    }

    void refresh() {
        QueryName queryName = queryName();
        switch (queryName) {
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

        if (loadTodos(ALL).isEmpty()) {
            // hide main section and footer
            toggleAll.getParentElement().getStyle().setDisplay(NONE);
            todoCount.getElement().getParentElement().getStyle().setDisplay(NONE);
        } else {
            toggleAll.getParentElement().getStyle().clearDisplay();
            todoCount.getElement().getParentElement().getStyle().clearDisplay();
        }
        todoItems.setItems(loadTodos(queryName));
        todoCount.setText(messages.todoItems(loadTodos(ACTIVE).size()));
    }


    // ------------------------------------------------------ entity manager and query

    List<TodoItem> loadTodos(QueryName queryName) {
        TypedQuery<TodoItem> query = em.createNamedQuery(queryName.query(), TodoItem.class);
        return query.getResultList();
    }

    QueryName queryName() {
        return Strings.isNullOrEmpty(filter) ? ALL : Enums.getIfPresent(QueryName.class, filter.toUpperCase()).or(ALL);
    }


    // ------------------------------------------------------ event handler

    @EventHandler("toggleAll")
    void onToggleAll(ClickEvent event) {
        boolean checked = Elements.isChecked(toggleAll);
        List<TodoItem> items = loadTodos(queryName());
        for (TodoItem item : items) {
            item.setDone(checked);
            em.persist(item);
        }
        em.flush();
        sync();
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

    @EventHandler("allTodos")
    void allTodos(ClickEvent event) {
        navigation.goTo(MainPage.class, ImmutableMultimap.of("filter", ALL.name().toLowerCase()));
    }

    @EventHandler("activeTodos")
    void activeTodos(ClickEvent event) {
        navigation.goTo(MainPage.class, ImmutableMultimap.of("filter", ACTIVE.name().toLowerCase()));
    }

    @EventHandler("completedTodos")
    void completedTodos(ClickEvent event) {
        navigation.goTo(MainPage.class, ImmutableMultimap.of("filter", COMPLETED.name().toLowerCase()));
    }

    @EventHandler("clearCompleted")
    void clearCompleted(ClickEvent event) {
        List<TodoItem> items = loadTodos(COMPLETED);
        for (TodoItem item : items) {
            em.remove(item);
        }
        em.flush();
        sync();
    }
}
