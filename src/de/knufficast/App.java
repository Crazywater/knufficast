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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.AlarmManager;
import android.app.Application;
import de.knufficast.events.EventBus;
import de.knufficast.flattr.FlattrQueue;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.ImageCache;
import de.knufficast.logic.model.Queue;
import de.knufficast.player.QueuePlayer;
import de.knufficast.util.LockManager;
import de.knufficast.util.file.InternalFileUtil;
import de.knufficast.watchers.ConfigurationSaver;
import de.knufficast.watchers.DownloadRemover;
import de.knufficast.watchers.DownloadWatcher;
import de.knufficast.watchers.FlattrWatcher;
import de.knufficast.watchers.UpdaterService;

/**
 * The main entry point and global state of the application.
 * 
 * @author crazywater
 */
public class App extends Application {
  private static final String CONFIG_FILENAME = "knuffiCastConfiguration";
  private Configuration configuration;
  private final LockManager lockManager = new LockManager(this);
  private final EventBus eventBus = new EventBus();
  private final ImageCache imageCache = new ImageCache(this, eventBus);
  private final ConfigurationSaver configurationSaver = new ConfigurationSaver(
      eventBus);
  private final DownloadWatcher downloadWatcher = new DownloadWatcher(this,
      eventBus);
  private final DownloadRemover downloadRemover = new DownloadRemover(this,
      eventBus);
  private final FlattrWatcher flattrWatcher = new FlattrWatcher(this, eventBus);
  private final FlattrQueue flattrQueue = new FlattrQueue();
  private QueuePlayer queuePlayer;
  private static App instance;

  /**
   * Main entry point of the application. Generates or restores global state.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    load();
    queuePlayer = new QueuePlayer(getQueue(), this, eventBus);
    initUpdater();
    imageCache.init();
    lockManager.init();
    configurationSaver.register();
    downloadWatcher.register();
    downloadRemover.register();
    flattrWatcher.register();
  }

  /**
   * Returns the singleton App object.
   */
  public static App get() {
    return instance;
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
    return configuration.getQueue();
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
   * Loads the global "configuration" object from storage.
   */
  private void loadConfiguration() {
    try {
      FileInputStream fis = new InternalFileUtil(this).read(CONFIG_FILENAME);
      ObjectInputStream is = new ObjectInputStream(fis);
      configuration = ((Configuration) is.readObject());
      is.close();
      configuration.sanitize();
    } catch (IOException e) {
      // do nothing
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    if (configuration == null) {
      configuration = new Configuration();
    }
  }

  /**
   * Saves the global "configuration" object to storage.
   */
  private void saveConfiguration() {
    FileOutputStream fos;
    try {
      fos = new InternalFileUtil(this).write(CONFIG_FILENAME, false);
      ObjectOutputStream os = new ObjectOutputStream(fos);
      os.writeObject(configuration);
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    saveConfiguration();
    imageCache.save();
  }

  /**
   * Loads the entire application state. Invalidates all references to the
   * configuration object and therefore should be called with care.
   */
  private synchronized void load() {
    loadConfiguration();
    imageCache.load();
  }
}
