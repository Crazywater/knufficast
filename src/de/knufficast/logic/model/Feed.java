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
package de.knufficast.logic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a Feed. Contains {@link Episode}s.
 * 
 * @author crazywater
 * 
 */
public class Feed implements Serializable {
  private static final long serialVersionUID = 1L;

  private String title;
  private List<Episode> episodes;
  private String imgUrl;
  private String feedUrl;
  private String description;
  private String encoding;
  private String eTag;
  private long lastUpdated;

  public Feed(String title, String feedUrl, List<Episode> episodes,
      String imgUrl, String description, String encoding, String eTag,
      long lastUpdated) {
    this.title = title;
    this.feedUrl = feedUrl;
    this.episodes = episodes;
    this.imgUrl = imgUrl;
    this.description = description;
    this.encoding = encoding;
    this.lastUpdated = lastUpdated;
    this.eTag = eTag;
  }

  /**
   * Returns the human-readable title of this feed.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the URL of this feed. Currently used for identification purposes as
   * well.
   */
  public String getFeedUrl() {
    return feedUrl;
  }

  /**
   * Returns a human-readable description of this feed.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the list of episodes of this feed. Changes in this list are not
   * reflected in the feed itself.
   */
  public List<Episode> getEpisodes() {
    return new ArrayList<Episode>(episodes);
  }

  /**
   * Returns the URL of the icon of this feed.
   */
  public String getImgUrl() {
    return imgUrl;
  }
  
  /**
   * Returns the encoding of this feed (e.g. "UTF-8").
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Returns the "ETag" header of the last feed download, if any, or null. This
   * header is used for asking the server if the feed has to be refreshed
   * (re-downloaded).
   */
  public String getETag() {
    return eTag;
  }

  public boolean hasEpisode(Episode episode) {
    return episodes.contains(episode);
  }

  /**
   * When the feed has been last updated, in UNIX time. This time is NOT the
   * system time on the phone, but rather what the server supplied in its "Date"
   * header. Used to check if we need to refresh the feed.
   */
  public long getLastUpdated() {
    return lastUpdated;
  }

  public Episode getEpisode(String guid) {
    for (Episode episode : getEpisodes()) {
      if (episode.getGuid().equals(guid)) {
        return episode;
      }
    }
    return null;
  }

  /**
   * Creates a new mutable feed.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Mutable version of {@link Feed}.
   * 
   * @author crazywater
   * 
   */
  public static class Builder {
    private String title = "";
    private String imgUrl = "";
    private String feedUrl = "";
    private String description = "";
    private String encoding = "UTF-8";
    private String eTag;
    private long lastUpdated;
    private List<Episode> episodes = new ArrayList<Episode>();

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder imgUrl(String imgUrl) {
      this.imgUrl = imgUrl;
      return this;
    }

    public Builder feedUrl(String feedUrl) {
      this.feedUrl = feedUrl;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder addEpisode(Episode episode) {
      episodes.add(episode);
      return this;
    }

    public Builder encoding(String encoding) {
      this.encoding = encoding;
      return this;
    }

    public Builder eTag(String eTag) {
      this.eTag = eTag;
      return this;
    }

    public Builder lastUpdated(long lastUpdated) {
      this.lastUpdated = lastUpdated;
      return this;
    }

    public Builder importMetadata(Feed feed) {
      this.title = feed.title;
      this.feedUrl = feed.feedUrl;
      this.imgUrl = feed.imgUrl;
      this.description = feed.description;
      this.encoding = feed.encoding;
      this.eTag = feed.eTag;
      this.lastUpdated = feed.lastUpdated;
      return this;
    }

    /**
     * Merges the episodes of the new and the old feed into this feed builder.
     * {@link Episode} objects from the old feed are preferred if the episode
     * appears in both the old and the new feed. New episodes not present in the
     * old feed are added at the start, not at the end.
     * 
     * @return whether there were any episodes in the new feed that weren't in
     *         the old feed
     */
    public boolean mergeEpisodes(Feed oldFeed, Feed newFeed) {
      boolean newEpisodes = false;
      for (Episode episode : newFeed.getEpisodes()) {
        if (!oldFeed.hasEpisode(episode)) {
          addEpisode(episode);
          newEpisodes = true;
        }
      }
      for (Episode episode : oldFeed.getEpisodes()) {
        addEpisode(episode);
      }
      return newEpisodes;
    }

    public Feed build() {
      return new Feed(title, feedUrl, episodes, imgUrl, description, encoding,
          eTag, lastUpdated);
    }
  }
}
