package de.knufficast.logic.model;

import java.io.Serializable;

public class EpisodeIdentifier implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String feedId;
  private final String episodeId;

  public EpisodeIdentifier(String feedId, String episodeId) {
    this.feedId = feedId;
    this.episodeId = episodeId;
  }

  public String getFeedId() {
    return feedId;
  }

  public String getEpisodeId() {
    return episodeId;
  }

  @Override
  public int hashCode() {
    return getFeedId().hashCode() ^ getEpisodeId().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EpisodeIdentifier)) {
      return false;
    }
    EpisodeIdentifier ei = (EpisodeIdentifier) o;
    return getFeedId().equals(ei.getFeedId())
        && getEpisodeId().equals(ei.getEpisodeId());
  }

  @Override
  public String toString() {
    return getFeedId() + "::" + getEpisodeId();
  }
}
