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
package de.knufficast.events;

import de.knufficast.logic.model.Episode;

/**
 * An event that is fired when an element is removed from the queue.
 * 
 * @author crazywater
 */
public class QueueRemovedEvent implements Event {
  private final Episode ep;
  
  public QueueRemovedEvent(Episode ep) {
    this.ep = ep;
  }

  public Episode getEpisode() {
    return ep;
  }
}
