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
package de.knufficast.watchers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import de.knufficast.App;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.FeedDownloader;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Feed;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.NetUtil;

/**
 * A service to refresh the feeds.
 * 
 * @author crazywater
 * 
 */
public class UpdaterService extends IntentService {
  private final NetUtil netUtil;
  private final BooleanCallback<Feed, Feed> callback;

  private static AtomicBoolean refreshing = new AtomicBoolean();

  public UpdaterService() {
    super("UpdaterService");
    netUtil = new NetUtil(this);
    callback = null;
  }

  public UpdaterService(BooleanCallback<Feed, Feed> callback) {
    super("UpdaterService");
    netUtil = new NetUtil(this);
    this.callback = callback;
  }

  /**
   * Refreshes all subscribed feeds. Locks the state such that only one
   * refresher can run at the same time.
   */
  public boolean refreshAll() {
    HttpURLConnection.setFollowRedirects(true);
    if (!refreshing.getAndSet(true)) {
      Configuration config = App.get().getConfiguration();
      List<Feed> allFeeds = config.getAllFeeds();
      boolean refreshSuccessful = true;
      // refresh feeds
      for (Feed feed : allFeeds) {
        Log.d("UpdaterService", "Refreshing Feed " + feed.getFeedUrl());
        try {
          refresh(config, feed);
          if (callback != null) {
            callback.success(feed);
          }
        } catch (Exception e) {
          refreshSuccessful = false;
          if (callback != null) {
            callback.fail(feed);
          }
        }
      }
      // auto-enqueue all new episodes
      if (App.get().getConfiguration().autoEnqueue()) {
        for (Feed feed : allFeeds) {
          for (Episode episode : feed.getEpisodes()) {
            if (episode.isNew() && episode.hasDownload()) {
              App.get().getQueue().add(episode);
              episode.setNoLongerNew();
            }
          }
        }
      }
      App.get().save();
      refreshing.set(false);
      return refreshSuccessful;
    }
    return false;
  }

  private void retryDownloads() {
    // restart downloads of new items
    new QueueDownloader(getApplicationContext()).restartDownloads();
  }

  private void refresh(Configuration config, Feed feed) throws IOException,
  XmlPullParserException {
    boolean needsUpdate = true;
    HttpURLConnection conn = (HttpURLConnection) new URL(feed.getFeedUrl())
    .openConnection();
    if (feed.getLastUpdated() > 0) {
      conn.setIfModifiedSince(feed.getLastUpdated());
    }
    if (feed.getETag() != null) {
      conn.addRequestProperty("If-None-Match", feed.getETag());
    }
    conn.connect();
    if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
      needsUpdate = false;
    }
    long lastModifiedTimestamp = conn.getLastModified();
    if (lastModifiedTimestamp > 0
        && lastModifiedTimestamp <= feed.getLastUpdated()) {
      needsUpdate = false;
    }
    if (needsUpdate) {
      List<Feed> feeds = new FeedDownloader().getFeeds(conn);
      config.mergeFeeds(feeds);
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    long nowTime = System.currentTimeMillis();
    Configuration config = App.get().getConfiguration();
    // only update feeds if last time is long enough ago
    // we need to check this because otherwise we'd refresh
    // upon every network change
    if (nowTime - config.getLastUpdate() >= config.getUpdateInterval()) {
      boolean refreshSuccessful = netUtil.isOnline();
      if (netUtil.isOnline()) {
        if (!App.get().getConfiguration().refreshNeedsWifi()
            || netUtil.isOnWifi()) {
          refreshSuccessful = refreshAll();
        } else {
          refreshSuccessful = false;
        }
      }
      if (refreshSuccessful) {
        config.setLastUpdate(nowTime);
        App.get().save();
      }
    }
    if (netUtil.isOnline() && config.autoRetry()) {
      retryDownloads();
    }
  }

  public static void init() {
    AlarmManager alarmMgr = (AlarmManager) App.get().getSystemService(
        Context.ALARM_SERVICE);
    Intent intent = new Intent(App.get(), UpdateAlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(App.get(), 0,
        intent, 0);

    // remove any pending events
    alarmMgr.cancel(pendingIntent);

    // start a repeating intent
    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime(), App.get().getConfiguration()
            .getUpdateInterval(), pendingIntent);
  }
}
