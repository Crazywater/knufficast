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

import android.content.Context;
import de.knufficast.App;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.model.DBEpisode;
import de.knufficast.logic.model.DBEpisode.DownloadState;
import de.knufficast.util.NetUtil;

/**
 * A watcher that restarts downloads if necessary.
 * 
 * @author crazywater
 * 
 */
public class DownloadWatcher {
  private final EventBus eventBus;
  private final Context context;
  private final NetUtil netUtil;
  private final QueueDownloader queueDownloader;

  private Listener<QueueChangedEvent> checkDownloadsListener = new Listener<QueueChangedEvent>() {
    @Override
    public void onEvent(QueueChangedEvent event) {
      new QueueDownloader(context).restartDownloads();
    }
  };
  
  private Listener<EpisodeDownloadStateEvent> episodeDownloadErrorListener = new Listener<EpisodeDownloadStateEvent>() {
    @Override
    public void onEvent(EpisodeDownloadStateEvent event) {
      Configuration config = App.get().getConfiguration();
      if (config.autoRetry()) {
        DBEpisode ep = new DBEpisode(event.getIdentifier());
        if (ep.getDownloadState() == DownloadState.ERROR && netUtil.isOnline()) {
          queueDownloader.restartDownloads();
        }
      }
    }
  };

  public DownloadWatcher(Context context, EventBus eventBus) {
    this.context = context;
    this.eventBus = eventBus;
    netUtil = new NetUtil(context);
    queueDownloader = new QueueDownloader(context);
  }

  public void register() {
    Configuration config = App.get().getConfiguration();
    if (config.autoRetry() && netUtil.isOnline()) {
      queueDownloader.restartDownloads();
    }

    eventBus.addListener(EpisodeDownloadStateEvent.class,
        episodeDownloadErrorListener);
    eventBus.addListener(QueueChangedEvent.class, checkDownloadsListener);
  }

  public void unregister() {
    eventBus.removeListener(EpisodeDownloadStateEvent.class,
        episodeDownloadErrorListener);
    eventBus.removeListener(QueueChangedEvent.class, checkDownloadsListener);
  }
}
