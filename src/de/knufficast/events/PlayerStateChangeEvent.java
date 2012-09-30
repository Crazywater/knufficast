package de.knufficast.events;

import de.knufficast.player.QueuePlayer;

/**
 * An event that is fired as the {@link QueuePlayer} changes its state
 * (Played/Pausing).
 */
public class PlayerStateChangeEvent implements Event {
  private boolean playing;

  public PlayerStateChangeEvent(boolean playing) {
    this.playing = playing;
  }

  /**
   * The new state the {@link QueuePlayer} is in.
   */
  public boolean isPlaying() {
    return playing;
  }
}
