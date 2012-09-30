package de.knufficast.events;

import de.knufficast.logic.model.EpisodeIdentifier;

/**
 * An event that signifies that the download state of an URL has changed.
 * 
 * @author crazywater
 */
public class EpisodeDownloadStateEvent implements Event {
  private EpisodeIdentifier id;

  public EpisodeDownloadStateEvent(EpisodeIdentifier id) {
    this.id = id;
  }

  public EpisodeIdentifier getIdentifier() {
    return id;
  }
}
