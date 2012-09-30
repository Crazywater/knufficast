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
