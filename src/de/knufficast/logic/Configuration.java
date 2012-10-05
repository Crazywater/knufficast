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
package de.knufficast.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.knufficast.App;
import de.knufficast.events.NewEpisodeEvent;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
import de.knufficast.logic.model.EpisodeIdentifier;
import de.knufficast.logic.model.Feed;
import de.knufficast.logic.model.Queue;
import de.knufficast.watchers.QueueDownloader;
import de.knufficast.watchers.UpdaterService;

/**
 * All user-defined state of the application.
 * 
 * @author crazywater
 */
public class Configuration implements Serializable {
  private static final long serialVersionUID = 1L;

  private Map<String, Feed> feedsByUrl = new HashMap<String, Feed>();
  private Queue queue = new Queue();
  private FlattrConfiguration flattrConfig;
  private long lastUpdate;

  /**
   * The play queue representation.
   */
  public Queue getQueue() {
    return queue;
  }

  /**
   * Gets a list of all feeds. Writes to this list are not reflected by the
   * configuration.
   */
  public List<Feed> getAllFeeds() {
    return new ArrayList<Feed>(feedsByUrl.values());
  }

  /**
   * Gets a single feed.
   * 
   * @param url
   *          the URL of this feed, e.g. obtainable by an
   *          {@link EpisodeIdentifier}
   */
  public Feed getFeed(String url) {
    return feedsByUrl.get(url);
  }

  public FlattrConfiguration getFlattrConfig() {
    if (flattrConfig == null) {
      flattrConfig = new FlattrConfiguration();
    }
    return flattrConfig;
  }

  /**
   * When the feeds were last refreshed by an {@link UpdaterService}, in
   * milliseconds UNIX time.
   */
  public long getLastUpdate() {
    return lastUpdate;
  }

  /**
   * Set the last feed refresh date.
   */
  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  /**
   * Add new feeds to the configuration.
   */
  public void addFeeds(Iterable<? extends Feed> moreFeeds) {
    if (!moreFeeds.iterator().hasNext()) {
      return;
    }
    for (Feed feed : moreFeeds) {
      // set episodes in new feeds as old per default
      for (Episode ep : feed.getEpisodes()) {
        ep.setNoLongerNew();
      }
      feedsByUrl.put(feed.getFeedUrl(), feed);
    }
  }

  /**
   * Merge feeds with the existing feeds: If two feeds have the same URL, the
   * new feeds determine the metadata. Episodes are taken from the old feed,
   * except if they didn't exist then yet. May fire a {@link NewEpisodeEvent} as
   * a side effect.
   */
  public void mergeFeeds(Iterable<? extends Feed> newFeeds) {
    if (!newFeeds.iterator().hasNext()) {
      return;
    }
    for (Feed newFeed : newFeeds) {
      Feed oldFeed = getFeed(newFeed.getFeedUrl());
      Feed resultFeed = newFeed;
      boolean newEpisode = false;
      if (oldFeed != null) {
        Feed.Builder mergeBuilder = Feed.builder();
        mergeBuilder.importMetadata(newFeed);
        newEpisode = mergeBuilder.mergeEpisodes(oldFeed, newFeed);
        resultFeed = mergeBuilder.build();
      }
      feedsByUrl.put(resultFeed.getFeedUrl(), resultFeed);
      if (newEpisode) {
        App.get().getEventBus().fireEvent(new NewEpisodeEvent());
      }
    }
  }

  /**
   * Sanitizes the input after reloading the configuration, i.e. changes values
   * that indicate that the application was terminated abnormally. Should only
   * be called after restarting the application.
   */
  public void sanitize() {
    for (Feed feed : getAllFeeds()) {
      for (Episode ep : feed.getEpisodes()) {
        if (ep.getDownloadState() == DownloadState.DOWNLOADING) {
          ep.setDownloadState(DownloadState.ERROR);
        }
      }
    }
  }

  /**
   * Deletes an entire feed, including episodes, downloads, queue entries.
   */
  public void deleteFeed(String feedUrl) {
    // TODO: delete cached icons
    QueueDownloader queueDownloader = new QueueDownloader(App.get());
    Feed feed = feedsByUrl.get(feedUrl);
    for (Episode ep : feed.getEpisodes()) {
      if (queue.contains(ep)) {
        queue.remove(ep);
      }
      queueDownloader.deleteDownload(ep);
    }
    feedsByUrl.remove(feedUrl);
  }

  /**
   * Returns the episode for an {@link EpisodeIdentifier} or null, if no such
   * feed/episode.
   */
  public Episode getEpisode(EpisodeIdentifier identifier) {
    Feed feed = getFeed(identifier.getFeedId());
    if (feed == null) {
      return null;
    }
    return feed.getEpisode(identifier.getEpisodeId());
  }

  /**
   * Returns the {@link SharedPreferences} which are not stored in this object
   * but managed by Android.
   */
  public SharedPreferences getSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(App.get());
  }

  /**
   * Whether refreshes need WiFi connection.
   */
  public boolean refreshNeedsWifi() {
    return getSharedPreferences().getBoolean("pref_key_refresh_needs_wifi",
        false);
  }

  /**
   * Whether episode downloads need WiFi connection.
   */
  public boolean downloadNeedsWifi() {
    return getSharedPreferences().getBoolean("pref_key_download_needs_wifi",
        true);
  }

  /**
   * Whether new episodes should automatically be enqueued.
   */
  public boolean autoEnqueue() {
    return getSharedPreferences().getBoolean("pref_key_auto_enqueue", true);
  }

  /**
   * Whether dequeued episodes should automatically have their download deleted.
   */
  public boolean autoDelete() {
    return getSharedPreferences().getBoolean("pref_key_auto_delete", true);
  }

  /**
   * Whether downloads with errors should automatically retry.
   */
  public boolean autoRetry() {
    return getSharedPreferences().getBoolean("pref_key_auto_retry", true);
  }

  public boolean autoFlattr() {
    return getSharedPreferences().getBoolean("pref_key_auto_flattr", false);
  }

  /**
   * The feed update interval in milliseconds. Note that this is 1000 times what
   * the {@link SharedPreferences} save.
   */
  public int getUpdateInterval() {
    return 1000 * Integer.parseInt(getSharedPreferences().getString(
        "pref_key_update_freq", "3600"));
  }
}
