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

/**
 * An event that is fired when the {@link Queue} has changed (new episode added,
 * episode removed).
 * 
 * @author crazywater
 */
public class QueueChangedEvent implements Event {
  private boolean topOfQueueChanged;

  public QueueChangedEvent(boolean topOfQueueChanged) {
    this.topOfQueueChanged = topOfQueueChanged;
  }

  public boolean topOfQueueChanged() {
    return topOfQueueChanged;
  }
}
