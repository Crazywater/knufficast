package de.knufficast.events;

import de.knufficast.logic.model.EpisodeIdentifier;

/**
 * An event that signifies that the download progress of an URL has changed.
 * 
 * @author crazywater
 */
public class EpisodeDownloadProgressEvent implements Event {
  private EpisodeIdentifier id;

  public EpisodeDownloadProgressEvent(EpisodeIdentifier id) {
    this.id = id;
  }

  public EpisodeIdentifier getIdentifier() {
    return id;
  }
}

