/*******************************************************************************
 * Copyright 2012 Crazywater
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.knufficast.logic.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.knufficast.App;
import de.knufficast.events.EventBus;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.events.QueuePoppedEvent;
import de.knufficast.events.QueueRemovedEvent;

/**
 * Represents the play queue.
 * 
 * @author crazywater
 */
public class Queue {
  private Set<DBEpisode> queue = new LinkedHashSet<DBEpisode>();

  /**
   * Adds a new episode to the queue. Fires a {@link QueueChangedEvent} as a
   * side-effect.
   */
  public synchronized void add(DBEpisode ep) {
    if (queue.contains(ep)) {
      return;
    }
    boolean topChanged = queue.isEmpty();
    queue.add(ep);
    App.get().getEventBus().fireEvent(new QueueChangedEvent(topChanged));
  }

  /**
   * Moves the episode to the specified index (0 being the top of the queue).
   * May fire a {@link QueueChangedEvent} as a side-effect.
   */
  public synchronized void move(DBEpisode ep, int to) {
    Set<DBEpisode> newQueue = new LinkedHashSet<DBEpisode>();
    Iterator<DBEpisode> it = queue.iterator();
    for (int i = 0; i < to;) {
      DBEpisode next = it.next();
      if (next != ep) {
        newQueue.add(next);
        i++;
      }
    }
    newQueue.add(ep);
    while (it.hasNext()) {
      DBEpisode next = it.next();
      if (next != ep) {
        newQueue.add(next);
      }
    }
    DBEpisode oldTop = queue.iterator().next();
    queue = newQueue;
    DBEpisode newTop = queue.iterator().next();
    App.get().getEventBus().fireEvent(new QueueChangedEvent(oldTop != newTop));
  }

  /**
   * Removes an episode from the queue. May fire a {@link QueueChangedEvent} as
   * a side-effect.
   */
  public synchronized void remove(DBEpisode ep) {
    boolean topChanged = ep == queue.iterator().next();
    if (!queue.contains(ep)) {
      return;
    }
    queue.remove(ep);
    App.get().getEventBus().fireEvent(new QueueChangedEvent(topChanged));
    App.get().getEventBus().fireEvent(new QueueRemovedEvent(ep));
  }

  /**
   * Returns the first element in the queue or null if the queue is empty.
   */
  public synchronized DBEpisode peek() {
    if (queue.isEmpty()) {
      return null;
    }
    return queue.iterator().next();
  }

  public boolean contains(DBEpisode episode) {
    return queue.contains(episode);
  }

  /**
   * Returns the queue as a list, top of the queue at index 0. Changes to this
   * list are not reflected in the queue.
   */
  public synchronized List<DBEpisode> asList() {
    return new ArrayList<DBEpisode>(queue);
  }

  /**
   * Removes the first element in the queue and returns it, or null if it is
   * empty. May fire a {@link QueueChangedEvent} and {@link QueuePoppedEvent} as
   * a side-effect.
   */
  public synchronized DBEpisode pop() {
    if (queue.isEmpty()) {
      return null;
    }
    DBEpisode head = queue.iterator().next();
    queue.remove(head);
    EventBus eventBus = App.get().getEventBus();
    eventBus.fireEvent(new QueueChangedEvent(true));
    eventBus.fireEvent(new QueueRemovedEvent(head));
    eventBus.fireEvent(new QueuePoppedEvent(head));
    return head;
  }

  /**
   * Moves the top element of the queue to the bottom.
   */
  public synchronized void rotateDownward() {
    if (queue.size() > 1) {
      Set<DBEpisode> newQueue = new LinkedHashSet<DBEpisode>();
      Iterator<DBEpisode> it = queue.iterator();
      DBEpisode bottom = it.next();
      while (it.hasNext()) {
        bottom = it.next();
      }
      newQueue.add(bottom);
      it = queue.iterator();
      while (it.hasNext()) {
        newQueue.add(it.next());
      }
      queue = newQueue;
      App.get().getEventBus().fireEvent(new QueueChangedEvent(true));
    }
  }

  /**
   * Moves the top element of the queue to the bottom.
   */
  public synchronized void rotateUpward() {
    if (queue.size() > 1) {
      Set<DBEpisode> newQueue = new LinkedHashSet<DBEpisode>();
      Iterator<DBEpisode> it = queue.iterator();
      DBEpisode head = it.next();
      while (it.hasNext()) {
        newQueue.add(it.next());
      }
      newQueue.add(head);
      queue = newQueue;
      App.get().getEventBus().fireEvent(new QueueChangedEvent(true));
    }
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (DBEpisode ep : queue) {
      sb.append(ep.getId());
      sb.append(",");
    }
    return sb.toString();
  }

  public void fromString(String string) {
    queue.clear();
    String[] ids = string.split(",");
    for (String idStr : ids) {
      if (!"".equals(idStr)) {
        long id = Long.valueOf(idStr).longValue();
        queue.add(new DBEpisode(id));
      }
    }
  }
}
