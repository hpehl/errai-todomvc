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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.todomvc.client.shared.TodoItem;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_ENTER;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_ESCAPE;

@Templated("MainPage.html#todoItem")
public class TodoItemWidget extends Composite implements HasModel<TodoItem> {

    TodoItem model;

    @Inject EntityManager em;
    @Inject DataSync dataSync;

    @DataField Element done = Document.get().createCheckInputElement();
    @DataField Element text = Document.get().createLabelElement();
    @Inject @DataField Button delete;
    @Inject @DataField TextBox edit;
    
    @Override
    public TodoItem getModel() {
        return model;
    }

    @Override
    public void setModel(TodoItem model) {
        this.model = model;
        updateWidgets(model);
    }

    void updateWidgets(final TodoItem item) {
        setChecked(done, item.isDone());
        text.setInnerText(item.getText());
    }

    @EventHandler("done")
    void onDoneClicked(ClickEvent event) {
        model.setDone(isChecked(done));
        saveAndSync();
    }

    @EventHandler("text")
    void onStartEdit(DoubleClickEvent event) {
        editMode(true);
        edit.setText(model.getText());
    }

    @EventHandler("edit")
    void onFinishEdit(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KEY_ESCAPE) {
            editMode(false);
        }
        else if (event.getNativeKeyCode() == KEY_ENTER && !edit.getText().trim().equals("")) {
            editMode(false);
            model.setText(edit.getText());
            saveAndSync();
        }
    }

    void saveAndSync() {
        model = em.merge(model);
        dataSync.sync();
        updateWidgets(model);
    }

    native void editMode(boolean enable) /*-{
        var view = $doc.querySelector("div.view");
        var edit = $doc.querySelector("input.edit");
        if (enable) {
            view.style.display = "none";
            edit.style.display = "inherit";

        } else {
            view.style.display = "inherit";
            edit.style.display = "none";
        }
    }-*/;

    native boolean isChecked(JavaScriptObject checkbox) /*-{
        return checkbox.checked;
    }-*/;

    native void setChecked(JavaScriptObject checkbox, boolean checked) /*-{
        checkbox.checked = checked;
    }-*/;
}
