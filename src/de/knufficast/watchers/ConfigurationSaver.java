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

import de.knufficast.App;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.Event;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.NewEpisodeEvent;
import de.knufficast.events.NewImageEvent;
import de.knufficast.events.QueueChangedEvent;

/**
 * A watcher that saves the {@link Configuration} upon certain events.
 * 
 * @author crazywater
 * 
 */
public class ConfigurationSaver {
  private EventBus eventBus;
  private boolean saving;
  private Thread saverThread = new Thread() {
    @Override
    public void run() {
      App.get().save();
    }
  };

  private Listener<Event> saver = new Listener<Event>() {
    @Override
    public void onEvent(Event event) {
      if (!saving) {
        saving = true;
        saverThread.start();
      }
    }
  };

  public ConfigurationSaver(EventBus eventBus) {
    this.eventBus = eventBus;
  }
  
  public void register() {
    eventBus.addListener(NewEpisodeEvent.class, saver);
    eventBus.addListener(EpisodeDownloadStateEvent.class, saver);
    eventBus.addListener(QueueChangedEvent.class, saver);
    eventBus.addListener(NewImageEvent.class, saver);
  }

  public void unregister() {
    eventBus.removeListener(NewEpisodeEvent.class, saver);
    eventBus.removeListener(EpisodeDownloadStateEvent.class, saver);
    eventBus.removeListener(QueueChangedEvent.class, saver);
    eventBus.removeListener(NewImageEvent.class, saver);
  }
}
