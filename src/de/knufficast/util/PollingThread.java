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
package de.knufficast.util;

/**
 * A {@link Thread} that does polling on a fetch function and relays the results
 * to a callback.
 * 
 * @author crazywater
 * 
 * @param <T>
 *          the type of data to poll and relay
 */
public class PollingThread<T> extends Thread {
  private final Callback<T> sendProgress;
  private final Function<Void, T> fetchProgress;
  private final long waitTime;
  private boolean running;

  /**
   * Creates a new polling thread.
   * 
   * @param sendProgress
   *          the callback: Where to send the polling results
   * @param fetchProgress
   *          the function: How to fetch the data
   * @param waitTime
   *          the time between two pollings in milliseconds
   */
  public PollingThread(Callback<T> sendProgress,
      Function<Void, T> fetchProgress, long waitTime) {
    this.sendProgress = sendProgress;
    this.fetchProgress = fetchProgress;
    this.waitTime = waitTime;
  }
  
  @Override
  public void run() {
    running = true;
    while(running) {
      try {
        Thread.sleep(waitTime);
        sendProgress.call(fetchProgress.call(null));
      } catch (InterruptedException e) {
        running = false;
      }
    }
  }
}
