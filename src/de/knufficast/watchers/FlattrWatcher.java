package de.knufficast.watchers;

import android.content.Context;
import android.content.Intent;
import de.knufficast.App;
import de.knufficast.events.EventBus;
import de.knufficast.events.FlattrQueueEvent;
import de.knufficast.events.Listener;
import de.knufficast.events.QueuePoppedEvent;
import de.knufficast.flattr.FlattrQueue;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.FlattrState;
import de.knufficast.logic.model.Episode.PlayState;

public class FlattrWatcher {
  private final Context context;
  private final EventBus eventBus;

  public FlattrWatcher(Context context, EventBus eventBus) {
    this.context = context;
    this.eventBus = eventBus;
  }

  private final Listener<QueuePoppedEvent> queuePoppedListener = new Listener<QueuePoppedEvent>() {
    @Override
    public void onEvent(QueuePoppedEvent event) {
      Episode ep = event.getEpisode();
      if (ep.getFlattrState() == FlattrState.NONE
          && ep.getPlayState() == PlayState.FINISHED
          && App.get().getConfiguration().autoFlattr()) {
        FlattrQueue flattrQueue = App.get().getFlattrQueue();
        ep.setFlattrState(FlattrState.ENQUEUED);
        flattrQueue.enqueue(ep);
      }
    }
  };

  private final Listener<FlattrQueueEvent> flattrListener = new Listener<FlattrQueueEvent>() {
    @Override
    public void onEvent(FlattrQueueEvent event) {
      new Thread() {
        @Override
        public void run() {
          context.startService(new Intent(context, FlattrQueueService.class));
        }
      }.start();
    }
  };

  public void register() {
    eventBus.addListener(QueuePoppedEvent.class, queuePoppedListener);
    eventBus.addListener(FlattrQueueEvent.class, flattrListener);
  }

  public void unregister() {
    eventBus.removeListener(QueuePoppedEvent.class, queuePoppedListener);
    eventBus.removeListener(FlattrQueueEvent.class, flattrListener);
  }
}
