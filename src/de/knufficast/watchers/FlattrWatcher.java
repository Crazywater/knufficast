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
import android.content.Intent;
import de.knufficast.App;
import de.knufficast.events.EventBus;
import de.knufficast.events.FlattrQueueEvent;
import de.knufficast.events.Listener;
import de.knufficast.events.QueuePoppedEvent;
import de.knufficast.flattr.FlattrQueue;
import de.knufficast.logic.db.DBEpisode;
import de.knufficast.logic.db.DBEpisode.FlattrState;
import de.knufficast.logic.db.DBEpisode.PlayState;

/**
 * A watcher that starts the {@link FlattrQueueService} whenever something new
 * enters the FlattrQueue and puts new things in the FlattrQueue when they
 * should be flattred.
 * 
 * @author crazywater
 * 
 */
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
      DBEpisode ep = event.getEpisode();
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
