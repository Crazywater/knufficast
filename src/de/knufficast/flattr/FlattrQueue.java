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
package de.knufficast.flattr;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.knufficast.App;
import de.knufficast.events.FlattrQueueEvent;
import de.knufficast.logic.model.DBEpisode;

/**
 * The queue of things left to flattr.
 * 
 * @author crazywater
 * 
 */
public class FlattrQueue {
  private final Queue<DBEpisode> episodes = new ConcurrentLinkedQueue<DBEpisode>();

  public void enqueue(DBEpisode episode) {
    if (!episodes.contains(episode)) {
      episodes.add(episode);
      App.get().getEventBus().fireEvent(new FlattrQueueEvent());
    }
  }

  public boolean isEmpty() {
    return episodes.isEmpty();
  }

  public DBEpisode pop() {
    return episodes.poll();
  }

}
