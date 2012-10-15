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
package de.knufficast.logic.db;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.knufficast.App;
import de.knufficast.logic.FlattrConfiguration;
import de.knufficast.logic.db.DBEpisode.DownloadState;
import de.knufficast.watchers.UpdaterService;

/**
 * All user-defined state of the application.
 * 
 * @author crazywater
 */
public class Configuration {
  private static final String LAST_UPDATE_KEY = "lastUpdated";
  private FlattrConfiguration flattrConfig;

  /**
   * Gets a list of all feeds.
   */
  public List<DBFeed> getAllFeeds() {
    List<Long> ids = App.get().getDB().getIds(SQLiteHelper.TABLE_FEEDS);
    List<DBFeed> result = new ArrayList<DBFeed>();
    for (Long id : ids) {
      result.add(new DBFeed(id));
    }
    return result;
  }

  public FlattrConfiguration getFlattrConfig() {
    if (flattrConfig == null) {
      flattrConfig = new FlattrConfiguration(getSharedPreferences());
    }
    return flattrConfig;
  }

  /**
   * When the feeds were last refreshed by an {@link UpdaterService}, in
   * milliseconds UNIX time.
   */
  public long getLastUpdate() {
    return getSharedPreferences().getLong(LAST_UPDATE_KEY, 0);
  }

  /**
   * Set the last feed refresh date.
   */
  public void setLastUpdate(long lastUpdate) {
    getSharedPreferences().edit().putLong(LAST_UPDATE_KEY, lastUpdate).commit();
  }

  /**
   * Add new feeds to the configuration.
   */
  public void addFeeds(Iterable<? extends DBFeed> moreFeeds) {
    if (!moreFeeds.iterator().hasNext()) {
      return;
    }
    for (DBFeed feed : moreFeeds) {
      // set episodes in new feeds as old per default
      for (DBEpisode ep : feed.getEpisodes()) {
        ep.setNew(false);
      }
    }
  }

  /**
   * Sanitizes the input after reloading the configuration, i.e. changes values
   * that indicate that the application was terminated abnormally. Should only
   * be called after restarting the application.
   */
  public void sanitize() {
    for (DBFeed feed : getAllFeeds()) {
      for (DBEpisode ep : feed.getEpisodes()) {
        if (ep.getDownloadState() == DownloadState.DOWNLOADING) {
          ep.setDownloadState(DownloadState.ERROR);
        }
      }
    }
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
