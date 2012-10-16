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
package de.knufficast;

import android.app.AlarmManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.knufficast.events.EventBus;
import de.knufficast.flattr.FlattrQueue;
import de.knufficast.logic.ImageCache;
import de.knufficast.logic.db.Configuration;
import de.knufficast.logic.db.DBEpisode;
import de.knufficast.logic.db.DBFeed;
import de.knufficast.logic.db.Database;
import de.knufficast.logic.db.Queue;
import de.knufficast.logic.db.SQLiteHelper;
import de.knufficast.player.QueuePlayer;
import de.knufficast.util.LockManager;
import de.knufficast.util.file.CacheFileUtil;
import de.knufficast.watchers.ConfigurationSaver;
import de.knufficast.watchers.DownloadRemover;
import de.knufficast.watchers.DownloadWatcher;
import de.knufficast.watchers.FlattrWatcher;
import de.knufficast.watchers.QueueDownloader;
import de.knufficast.watchers.UpdaterService;

/**
 * The main entry point and global state of the application.
 * 
 * @author crazywater
 */
public class App extends Application {
  private final Configuration configuration = new Configuration();
  private final Queue queue = new Queue();
  private final LockManager lockManager = new LockManager(this);
  private final EventBus eventBus = new EventBus();
  private final ImageCache imageCache = new ImageCache(this, eventBus,
      new CacheFileUtil(this));
  private final ConfigurationSaver configurationSaver = new ConfigurationSaver(
      eventBus);
  private final DownloadWatcher downloadWatcher = new DownloadWatcher(this,
      eventBus);
  private final DownloadRemover downloadRemover = new DownloadRemover(this,
      eventBus);
  private final FlattrWatcher flattrWatcher = new FlattrWatcher(this, eventBus);
  private final FlattrQueue flattrQueue = new FlattrQueue();
  private final Database database = new Database(this);

  private final String KEY_QUEUE_PREF = "queue";

  private QueuePlayer queuePlayer;
  private static App instance;

  /**
   * Main entry point of the application. Generates or restores global state.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    database.open();
    load();
    queuePlayer = new QueuePlayer(getQueue(), this, eventBus);
    initUpdater();
    imageCache.init();
    lockManager.init();
    configurationSaver.register();
    downloadWatcher.register();
    downloadRemover.register();
    flattrWatcher.register();
    configuration.sanitize();
  }

  /**
   * Returns the singleton App object.
   */
  public static App get() {
    return instance;
  }

  public Database getDB() {
    return database;
  }

  /**
   * Returns the user {@link Configuration} singleton for the application.
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Returns the play {@link Queue} singleton.
   */
  public Queue getQueue() {
    return queue;
  }

  /**
   * Returns the {@link QueuePlayer} singleton.
   */
  public QueuePlayer getPlayer() {
    return queuePlayer;
  }

  /**
   * Returns the global {@link EventBus}.
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  /**
   * Returns the global {@link ImageCache}.
   */
  public ImageCache getImageCache() {
    return imageCache;
  }

  /**
   * Returns the global {@link LockManager}.
   */
  public LockManager getLockManager() {
    return lockManager;
  }

  public FlattrQueue getFlattrQueue() {
    return flattrQueue;
  }

  /**
   * Registers the updater at the {@link AlarmManager} in order to be called
   * periodically.
   */
  private void initUpdater() {
    UpdaterService.init();
  }

  /**
   * Saves the entire application state to storage.
   */
  public synchronized void save() {
    saveQueue();
    imageCache.save();
  }

  /**
   * Loads the entire application state. Invalidates all references to the
   * configuration object and therefore should be called with care.
   */
  private synchronized void load() {
    loadQueue();
    imageCache.load();
  }

  public void deleteFeed(DBFeed feed) {
    QueueDownloader queueDownloader = QueueDownloader.get();
    for (DBEpisode ep : feed.getEpisodes()) {
      if (queue.contains(ep)) {
        queue.remove(ep);
      }
      queueDownloader.deleteDownload(ep);
    }
    getDB().delete(SQLiteHelper.TABLE_FEEDS, feed.getId());
  }

  private void saveQueue() {
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(this);
    prefs.edit().putString(KEY_QUEUE_PREF, queue.toString()).commit();
  }

  private void loadQueue() {
    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(this);
    String queueStr = prefs.getString(KEY_QUEUE_PREF, "");
    queue.fromString(queueStr);
  }
}
