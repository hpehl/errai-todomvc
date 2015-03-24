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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.jboss.errai.todomvc.client.local.Index.Filter.ALL;

/**
 * This is the companion Java class of the main page as specified by
 * {@link Templated}. It refers to the HTML inside the body of Index.html.
 * <p>
 * The {@link Page} annotation declares this form as a bookmarkable page that
 * can be transitioned to by other pages of this application. Further the
 * specified role (DefaultPage.class) make this page appear by default when the
 * application is started.
 */
@Templated
@Page(role = DefaultPage.class)
public class Index extends Composite {

    @SuppressWarnings("unused")
    enum Filter {
        ALL("allTodos"),
        ACTIVE("activeTodos"),
        COMPLETED("completedTodos");

        final String query;

        Filter(String query) {
            this.query = query;
        }
    }


    @PageState String filter;
    @Inject DataSync sync;
    @Inject EntityManager em;
    @Inject Logger logger;

    @Inject @DataField TextBox newTodo;
    @Inject @DataField CheckBox toggleAll;
    @Inject @DataField ListWidget<TodoItem, TodoItemWidget> todoList;
    @Inject @DataField InlineLabel todoCount;
    @Inject @DataField Button clearCompleted;

    @PageShown
    private void sync() {
        sync.sync(response -> {
            logger.debug("Received sync response:" + response);
            loadTodos();
        });
    }

    private void loadTodos() {
        Filter f = Strings.isNullOrEmpty(filter) ? ALL : Enums.getIfPresent(Filter.class, filter).or(ALL);
        TypedQuery<TodoItem> query = em.createNamedQuery(f.query, TodoItem.class);
        todoList.setItems(query.getResultList());
    }
}
