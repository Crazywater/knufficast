package de.knufficast.flattr;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.knufficast.App;
import de.knufficast.events.FlattrQueueEvent;
import de.knufficast.logic.model.Episode;

public class FlattrQueue {
  private final Queue<Episode> episodes = new ConcurrentLinkedQueue<Episode>();

  public void enqueue(Episode episode) {
    if (!episodes.contains(episode)) {
      episodes.add(episode);
      App.get().getEventBus().fireEvent(new FlattrQueueEvent());
    }
  }

  public boolean isEmpty() {
    return episodes.isEmpty();
  }

  public Episode pop() {
    return episodes.poll();
  }

}
