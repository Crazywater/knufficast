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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representation of a Feed. Contains {@link Episode}s.
 * 
 * @author crazywater
 * 
 */
public class Feed implements Serializable {
  private static final long serialVersionUID = 1L;

  private String description;
  private String encoding;
  private List<Episode> episodes;
  private String eTag;
  private String feedUrl;
  private String imgUrl;
  private long lastUpdated;
  private String title;

  public void addEpisode(Episode episode) {
    episodes.add(episode);
  }

  /**
   * Returns a human-readable description of this feed.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the encoding of this feed (e.g. "UTF-8").
   */
  public String getEncoding() {
    return encoding;
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
   * Returns the list of episodes of this feed. Changes in this list are not
   * reflected in the feed itself.
   */
  public List<Episode> getEpisodes() {
    return new ArrayList<Episode>(episodes);
  }

  /**
   * Returns the "ETag" header of the last feed download, if any, or null. This
   * header is used for asking the server if the feed has to be refreshed
   * (re-downloaded).
   */
  public String getETag() {
    return eTag;
  }

  /**
   * Returns the URL of this feed. Currently used for identification purposes as
   * well.
   */
  public String getFeedUrl() {
    return feedUrl;
  }

  /**
   * Returns the URL of the icon of this feed.
   */
  public String getImgUrl() {
    return imgUrl;
  }

  /**
   * When the feed has been last updated, in UNIX time. This time is NOT the
   * system time on the phone, but rather what the server supplied in its "Date"
   * header. Used to check if we need to refresh the feed.
   */
  public long getLastUpdated() {
    return lastUpdated;
  }

  /**
   * Returns the human-readable title of this feed.
   */
  public String getTitle() {
    return title;
  }

    public boolean hasEpisode(Episode episode) {
      return episodes.contains(episode);
    }

  /**
  * Merges the episodes of the new and the old feed into this feed builder.
  * {@link Episode} objects from the old feed are preferred if the episode
  * appears in both the old and the new feed. New episodes not present in the
  * old feed are added at the start, not at the end.
  * 
  * @return whether there were any episodes in the new feed that weren't in the
  *         old feed
  */
 public boolean mergeEpisodes(Feed oldFeed) {
  boolean hasNewEpisodes = false;
  Set<Episode> newEpisodes = new HashSet<Episode>(getEpisodes());
  episodes.clear();
  for (Episode episode : newEpisodes) {
    if (!oldFeed.hasEpisode(episode)) {
      addEpisode(episode);
      hasNewEpisodes = true;
    }
  }
  for (Episode episode : oldFeed.getEpisodes()) {
    addEpisode(episode);
  }
  return hasNewEpisodes;
 }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setEpisodes(List<Episode> episodes) {
    this.episodes = episodes;
  }

  public void setETag(String eTag) {
    this.eTag = eTag;
  }

  public void setFeedUrl(String feedUrl) {
    this.feedUrl = feedUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public void setLastUpdated(long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
