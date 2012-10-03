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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.knufficast.App;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.events.QueuePoppedEvent;
import de.knufficast.events.QueueRemovedEvent;

/**
 * Represents the play queue.
 * 
 * @author crazywater
 */
public class Queue implements Serializable {
  private static final long serialVersionUID = 1L;
  private Set<Episode> queue = new LinkedHashSet<Episode>();

  /**
   * Adds a new episode to the queue. Fires a {@link QueueChangedEvent} as a
   * side-effect.
   */
  public synchronized void add(Episode ep) {
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
  public synchronized void move(Episode ep, int to) {
    Set<Episode> newQueue = new LinkedHashSet<Episode>();
    Iterator<Episode> it = queue.iterator();
    for (int i = 0; i < to;) {
      Episode next = it.next();
      if (next != ep) {
        newQueue.add(next);
        i++;
      }
    }
    newQueue.add(ep);
    while (it.hasNext()) {
      Episode next = it.next();
      if (next != ep) {
        newQueue.add(next);
      }
    }
    Episode oldTop = queue.iterator().next();
    queue = newQueue;
    Episode newTop = queue.iterator().next();
    App.get().getEventBus().fireEvent(new QueueChangedEvent(oldTop != newTop));
  }

  /**
   * Removes an episode from the queue. May fire a {@link QueueChangedEvent} as
   * a side-effect.
   */
  public synchronized void remove(Episode ep) {
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
  public synchronized Episode peek() {
    if (queue.isEmpty()) {
      return null;
    }
    return queue.iterator().next();
  }

  public boolean contains(Episode episode) {
    return queue.contains(episode);
  }

  /**
   * Returns the queue as a list, top of the queue at index 0. Changes to this
   * list are not reflected in the queue.
   */
  public synchronized List<Episode> asList() {
    return new ArrayList<Episode>(queue);
  }

  /**
   * Removes the first element in the queue and returns it, or null if it is
   * empty. May fire a {@link QueueChangedEvent} and {@link QueuePoppedEvent} as
   * a side-effect.
   */
  public synchronized Episode pop() {
    if (queue.isEmpty()) {
      return null;
    }
    Episode head = queue.iterator().next();
    queue.remove(head);
    App.get().getEventBus().fireEvent(new QueueChangedEvent(true));
    App.get().getEventBus().fireEvent(new QueueRemovedEvent(head));
    App.get().getEventBus().fireEvent(new QueuePoppedEvent(head));
    return head;
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }
}
