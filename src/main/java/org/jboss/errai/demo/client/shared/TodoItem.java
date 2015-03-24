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

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;
import java.util.Date;

/**
 * This is a JPA entity representing a task. It is used on both the
 * server and the client. On the server, it is persisted into the relational
 * database that is configured as data source in META-INF/persistence.xml. On
 * the client, it is persisted into the browser's offline storage.
 *
 * @author Harald Pehl
 */
@Entity
@Bindable
@Portable
@NamedQueries({
        @NamedQuery(name = "allTodos", query = "SELECT t FROM TodoItem t ORDER BY t.createdAt"),
        @NamedQuery(name = "activeTodos", query = "SELECT t FROM TodoItem t WHERE t.done = false ORDER BY t.createdAt"),
        @NamedQuery(name = "completedTodos", query = "SELECT t FROM TodoItem t WHERE t.done = true ORDER BY t.createdAt")
})
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    private String text;
    private Date createdAt;
    private boolean done;
    private Date finishedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof TodoItem)) { return false; }

        TodoItem todoItem = (TodoItem) o;
        return !(id != null ? !id.equals(todoItem.id) : todoItem.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TodoItem{" + text + ", done=" + done + '}';
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
