package de.knufficast.player;

import java.io.IOException;

import android.content.Context;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.PlayerProgressEvent;
import de.knufficast.events.PlayerStateChangeEvent;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
import de.knufficast.logic.model.Queue;
import de.knufficast.util.Callback;
import de.knufficast.util.Function;
import de.knufficast.util.PollingThread;

/**
 * A wrapper around {@link Player} that manages always playing what is on top of
 * the queue and removing the top upon completion.
 * 
 * @author crazywater
 * 
 */
public class QueuePlayer {
  private static final long UI_UPDATE_INTERVAL = 1000; // ms
  private final Queue queue;
  private final EventBus eventBus;
  private final Player player;
  private PollingThread<Integer> progressReporter;

  private final Function<Void, Integer> getProgress = new Function<Void, Integer>() {
    @Override
    public Integer call(Void a) {
      return player.getCurrentPosition();
    }
  };
  private final Callback<Integer> progressListener = new Callback<Integer>() {
    @Override
    public void call(Integer progress) {
      eventBus
          .fireEvent(new PlayerProgressEvent(progress, player.getDuration()));
    }
  };
  private final Callback<Void> onCompletionCallback = new Callback<Void>() {
    @Override
    public void call(Void unused) {
      eventBus.fireEvent(new PlayerStateChangeEvent(false));
      queue.pop();
    }
  };
  private final Listener<QueueChangedEvent> topChangedListener = new Listener<QueueChangedEvent>() {
    @Override
    public void onEvent(QueueChangedEvent event) {
      if (event.topOfQueueChanged()) {
        topOfQueueChanged();
      }
    }
  };

  private final Listener<EpisodeDownloadStateEvent> topDownloadListener = new Listener<EpisodeDownloadStateEvent>() {
    @Override
    public void onEvent(EpisodeDownloadStateEvent event) {
      if (!queue.isEmpty()
          && event.getIdentifier().equals(queue.peek().getIdentifier())) {
        topOfQueueChanged();
      }
    }
  };

  private void topOfQueueChanged() {
    boolean playing = player.isPlaying();
    if (playing) {
      pause();
    }
    try {
      prepare();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (playing) {
      play();
    }
  }

  public QueuePlayer(Queue queue, Context context, EventBus eventBus) {
    this.queue = queue;
    this.eventBus = eventBus;
    player = new Player(context);
    player.setOnCompletionCallback(onCompletionCallback);

    eventBus.addListener(QueueChangedEvent.class, topChangedListener);
    eventBus.addListener(EpisodeDownloadStateEvent.class, topDownloadListener);
  }

  /**
   * Prepare the queuePlayer to play whatever is on top of the queue.
   */
  public void prepare() throws IOException {
    int progress = 0;
    int total = 0;
    if (!queue.isEmpty()) {
      Episode next = queue.peek();
      if (next.getDownloadState() == DownloadState.FINISHED) {
        player.setEpisode(next);
        progress = player.getCurrentPosition();
        total = player.getDuration();
      } else {
        player.setEpisode(null);
      }
    }
    eventBus.fireEvent(new PlayerProgressEvent(progress, total));
  }

  /**
   * Pauses if playing, starts playing if paused.
   */
  public void togglePlaying() {
    if (!player.isPlaying()) {
      play();
    } else {
      pause();
    }
  }

  private void play() {
    if (!queue.isEmpty() && player.hasEpisode()) {
      if (progressListener != null) {
        startProgressReporter();
      }
      player.play();
      eventBus.fireEvent(new PlayerStateChangeEvent(true));
    }
  }

  private void pause() {
    if (progressListener != null) {
      stopProgressReporter();
    }
    player.pause();
    eventBus.fireEvent(new PlayerStateChangeEvent(false));
  }

  private void startProgressReporter() {
    progressReporter = new PollingThread<Integer>(progressListener,
        getProgress, UI_UPDATE_INTERVAL);
    progressReporter.start();
  }

  private void stopProgressReporter() {
    if (progressReporter != null) {
      progressReporter.interrupt();
      progressReporter = null;
    }
  }

  public void seekTo(int msec) {
    player.seekTo(msec);
  }

  public int getCurrentDuration() {
    return player.getDuration();
  }

  public int getCurrentProgress() {
    return player.getCurrentPosition();
  }
  
  public boolean isPlaying() {
    return player.isPlaying();
  }
}
