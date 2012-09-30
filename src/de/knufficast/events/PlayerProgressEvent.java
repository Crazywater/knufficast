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
 * An event that is fired after the player has played a while, to notify
 * listeners about the play progress.
 * 
 * @author crazywater
 */
public class PlayerProgressEvent implements Event {
  private final int progress;
  private final int total;

  public PlayerProgressEvent(int progress, int total) {
    this.progress = progress;
    this.total = total;
  }

  /**
   * Returns the progress of the current track in milliseconds.
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Returns the total length of the current track in milliseconds.
   */
  public int getTotal() {
    return total;
  }
}
