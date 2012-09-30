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
