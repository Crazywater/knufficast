package de.knufficast.watchers;

import android.content.Context;
import de.knufficast.App;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.QueuePoppedEvent;

/**
 * A watcher that removes downloads when episodes are popped from the queue and
 * the user has set the option to auto-delete.
 * 
 * @author crazywater
 * 
 */
public class DownloadRemover {
  private final QueueDownloader queueDownloader;
  private final EventBus eventBus;

  private final Listener<QueuePoppedEvent> queuePoppedListener = new Listener<QueuePoppedEvent>() {
    @Override
    public void onEvent(QueuePoppedEvent event) {
      if (App.get().getConfiguration().autoDelete()) {
        queueDownloader.deleteDownload(event.getEpisode());
      }
    }
  };

  public DownloadRemover(Context context, EventBus eventBus) {
    this.eventBus = eventBus;
    queueDownloader = new QueueDownloader(context);
  }

  public void register() {
    eventBus.addListener(QueuePoppedEvent.class, queuePoppedListener);
  }

  public void unregister() {
    eventBus.removeListener(QueuePoppedEvent.class, queuePoppedListener);
  }
}
