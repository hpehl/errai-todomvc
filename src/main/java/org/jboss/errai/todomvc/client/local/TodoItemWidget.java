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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.todomvc.client.shared.TodoItemEndpoint;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static com.google.gwt.dom.client.Style.Display.*;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;

/**
 * Widget for a single TodoItem
 */
@Templated("MainPage.html#todoItem")
public class TodoItemWidget extends Composite implements HasModel<TodoItem> {

    TodoItem model;

    @Inject EntityManager em;
    @Inject Event<TodoItem> changedItem;
    @Inject Caller<TodoItemEndpoint> endpoint;

    @DataField Element done = Document.get().createCheckInputElement();
    @DataField LabelElement text = Document.get().createLabelElement();
    @Inject @DataField TextBox edit;
    @Inject @DataField Button delete;


    // ------------------------------------------------------ model

    @Override
    public TodoItem getModel() {
        return model;
    }

    @Override
    public void setModel(TodoItem model) {
        this.model = model;
        Elements.setChecked(done, model.isDone());
        text.setInnerText(model.getText());
        if (model.isDone()) {
            addStyleName("completed");
        } else {
            removeStyleName("completed");
        }
    }


    // ------------------------------------------------------ event handler

    @EventHandler("done")
    void onDoneClicked(ClickEvent event) {
        model.setDone(Elements.isChecked(done));
        saveAndFire();
    }

    @EventHandler("text")
    void onStartEdit(DoubleClickEvent event) {
        editMode(true);
        edit.setText(model.getText());
        edit.setFocus(true);
    }

    @EventHandler("edit")
    void onFinishEdit(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KEY_ESCAPE) {
            editMode(false);
        }
        else if (event.getNativeKeyCode() == KEY_ENTER && !edit.getText().trim().equals("")) {
            editMode(false);
            text.setInnerText(edit.getText());
            model.setText(edit.getText());
            saveAndFire();
        }
    }

    @EventHandler("delete")
    void onDelete(ClickEvent event) {
        endpoint.call(new ResponseCallback() {
            @Override
            public void callback(final Response response) {
                if (response.getStatusCode() == Response.SC_NO_CONTENT) {
                    changedItem.fire(model);
                }
            }
        }).delete(model.getId());
    }

    void editMode(boolean enable) {
        if (enable) {
            done.getParentElement().getStyle().setDisplay(NONE);
            edit.getElement().getStyle().setDisplay(INITIAL);
            addStyleName("editing");
        } else {
            done.getParentElement().getStyle().setDisplay(BLOCK);
            edit.getElement().getStyle().setDisplay(NONE);
            removeStyleName("editing");
        }
    }


    // ------------------------------------------------------ save and fire event

    void saveAndFire() {
        model = em.merge(model);
        changedItem.fire(model);
    }
}
