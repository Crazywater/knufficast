package de.knufficast.events;

import de.knufficast.logic.model.Episode;

/**
 * An event that is fired when the {@link Queue} has its head removed.
 * 
 * @author crazywater
 */
public class QueuePoppedEvent implements Event {
  private Episode episode;

  public QueuePoppedEvent(Episode episode) {
    this.episode = episode;
  }

  public Episode getEpisode() {
    return episode;
  }
}
