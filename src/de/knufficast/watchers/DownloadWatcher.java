package de.knufficast.watchers;

import android.content.Context;
import de.knufficast.App;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
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
        Episode ep = config.getEpisode(event.getIdentifier());
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
