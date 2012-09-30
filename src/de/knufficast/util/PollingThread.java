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
