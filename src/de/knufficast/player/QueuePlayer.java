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
package de.knufficast.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.PlayerErrorEvent;
import de.knufficast.events.PlayerProgressEvent;
import de.knufficast.events.PlayerStateChangeEvent;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.logic.model.DBEpisode;
import de.knufficast.logic.model.DBEpisode.DownloadState;
import de.knufficast.logic.model.Queue;
import de.knufficast.player.PlayerService.PlayerBinder;
import de.knufficast.util.Callback;
import de.knufficast.util.Function;
import de.knufficast.util.PollingThread;

/**
 * A wrapper around {@link PlayerService} that manages always playing what is on
 * top of the queue and removing the top upon completion.
 * 
 * @author crazywater
 * 
 */
public class QueuePlayer {
  private static final long UI_UPDATE_INTERVAL = 1000; // ms
  private final Queue queue;
  private final EventBus eventBus;
  private final Context context;
  private PlayerService player;
  private boolean shouldPlay;
  private AudioManager audioManager;
  private final RemoteController remoteController = new RemoteController();

  private final ServiceConnection playerConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
      player = ((PlayerBinder) binder).getService();
      player.setOnCompletionCallback(onCompletionCallback);
      player.setOnPreparedCallback(onPreparedCallback);
      prepareAsync();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      player = null;
    }
  };
  private final Function<Void, Integer> getProgress = new Function<Void, Integer>() {
    @Override
    public Integer call(Void a) {
      return player.getCurrentPosition();
    }
  };
  private final Callback<Integer> progressListener = new Callback<Integer>() {
    @Override
    public void call(Integer progress) {
      eventBus.fireEvent(new PlayerProgressEvent(queue.peek(), progress, player
          .getDuration()));
    }
  };
  private final Callback<Void> onCompletionCallback = new Callback<Void>() {
    @Override
    public void call(Void unused) {
      eventBus.fireEvent(new PlayerStateChangeEvent(false));
      queue.pop();
    }
  };
  private final Callback<Void> onPreparedCallback = new Callback<Void>() {
    @Override
    public void call(Void unused) {
      int progress = player.getCurrentPosition();
      int total = player.getDuration();
      eventBus
          .fireEvent(new PlayerProgressEvent(queue.peek(), progress, total));
      if (shouldPlay) {
        play();
      }
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
      if (!queue.isEmpty() && event.getIdentifier() == queue.peek().getId()) {
        topOfQueueChanged();
      }
    }
  };
  private final Listener<PlayerErrorEvent> errorListener = new Listener<PlayerErrorEvent>() {
    @Override
    public void onEvent(PlayerErrorEvent event) {
      pause();
    }
  };
  private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        remoteController.release();
        pause();
      }
    }
  };

  private PollingThread<Integer> progressReporter;

  public QueuePlayer(Queue queue, Context context, EventBus eventBus) {
    this.queue = queue;
    this.eventBus = eventBus;
    this.context = context;

    eventBus.addListener(QueueChangedEvent.class, topChangedListener);
    eventBus.addListener(EpisodeDownloadStateEvent.class, topDownloadListener);
    eventBus.addListener(PlayerErrorEvent.class, errorListener);
  }

  private void topOfQueueChanged() {
    if (player == null) {
      return;
    }
    boolean tmp = shouldPlay;
    if (shouldPlay) {
      pause();
    }
    shouldPlay = tmp;
    prepareAsync();
  }

  /**
   * Prepare the queuePlayer to play whatever is on top of the queue. Fires a
   * {@link PlayerProgressEvent} as soon as the length of the current episode is
   * known. May fire a {@link PlayerErrorEvent}. If the player is already
   * playing, apart from the events, nothing happens.
   */
  public void prepareAsync() {
    if (player == null) {
      context.bindService(new Intent(context, PlayerService.class),
          playerConnection, Context.BIND_AUTO_CREATE);
      // prepareAsync is called again once the player is not null anymore
    } else {
      int progress = 0;
      int total = 0;
      if (!queue.isEmpty()) {
        DBEpisode next = queue.peek();
        if (next.getDownloadState() == DownloadState.FINISHED) {
          player.setEpisode(next);
        } else {
          player.setEpisode(null);
          shouldPlay = false;
        }
        if (player.isPrepared()) {
          progress = player.getCurrentPosition();
          total = player.getDuration();
          next.setDuration(total);
        }
      }
      eventBus
          .fireEvent(new PlayerProgressEvent(queue.peek(), progress, total));
    }
    if (audioManager == null) {
      audioManager = (AudioManager) context
          .getSystemService(Context.AUDIO_SERVICE);
    }
  }

  /**
   * Pauses if playing, starts playing if paused. This method should never be
   * called on an unprepared player.
   */
  public void togglePlaying() {
    if (!player.isPlaying()) {
      play();
    } else {
      pause();
    }
  }

  public void play() {
    if (player == null) {
      return;
    }
    shouldPlay = true;
    remoteController.register(context, audioManager);
    if (!queue.isEmpty() && player.hasEpisode()) {
      int audioFocus = audioManager.requestAudioFocus(
          onAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
          AudioManager.AUDIOFOCUS_GAIN);
      if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        if (progressListener != null) {
          startProgressReporter();
        }
        player.play();
        remoteController.updateMetadata(queue.peek(), player.getDuration());
        remoteController.updateState(true);
        eventBus.fireEvent(new PlayerStateChangeEvent(true));
      }
    }
  }

  public void pause() {
    if (player == null) {
      return;
    }
    shouldPlay = false;
    if (progressListener != null) {
      stopProgressReporter();
    }
    player.pause();
    remoteController.updateState(false);
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
    if (player != null) {
      player.seekTo(msec);
    }
  }

  /**
   * Stops the player service and relinquishes all its resources, if it isn't
   * playing and still needs them.
   */
  public void releaseIfNotPlaying() {
    if (player != null && !isPlaying()) {
      player.stopSelf();
    }
  }

  public boolean isPlaying() {
    if (player != null) {
      return player.isPlaying();
    }
    return false;
  }
}
