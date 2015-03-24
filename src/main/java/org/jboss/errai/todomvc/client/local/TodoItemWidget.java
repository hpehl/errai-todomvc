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

import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
@Templated("Index.html#item")
public class TodoItemWidget extends Composite implements HasModel<TodoItem> {

    @Inject Event<TodoItem> itemChangedEvent;
    @Inject @AutoBound DataBinder<TodoItem> itemBinder;

    @Inject @DataField FlowPanel view;
    @Inject @Bound @DataField InlineLabel text;
    @Inject @Bound @DataField CheckBox done;
    @Inject @DataField Button delete;
    @Inject @DataField TextBox edit;

    @PostConstruct
    void setup() {
        itemBinder.addPropertyChangeHandler(event -> itemChangedEvent.fire(itemBinder.getModel()));
    }

    @Override
    public TodoItem getModel() {
        return itemBinder.getModel();
    }

    @Override
    public void setModel(TodoItem item) {
        itemBinder.setModel(item, InitialState.FROM_MODEL);
    }

    @EventHandler("text")
    void onEdit(DoubleClickEvent event) {
        Window.alert("Edit not yet implemented!");
    }
}
