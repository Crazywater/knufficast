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

import java.io.FileInputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import de.knufficast.App;
import de.knufficast.events.PlayerErrorEvent;
import de.knufficast.logic.model.DBEpisode;
import de.knufficast.logic.model.DBEpisode.PlayState;
import de.knufficast.util.Callback;
import de.knufficast.util.file.ExternalFileUtil;

/**
 * An audio player for episodes, based on the android {@link MediaPlayer}.
 * 
 * @author crazywater
 * 
 */
public class PlayerService extends Service {
  private MediaPlayer mediaPlayer;
  private final IBinder binder = new PlayerBinder();
  private DBEpisode episode;
  private boolean prepared;

  private final OnErrorListener onErrorListener = new OnErrorListener() {
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      prepared = false;
      App.get().getEventBus().fireEvent(new PlayerErrorEvent());
      return true;
    }
  };

  /**
   * Prepares the player to play the episode.
   */
  public void setEpisode(DBEpisode episode) {
    if (episode == this.episode) {
      return;
    }
    prepared = false;
    this.episode = episode;
    mediaPlayer.reset();
    if (episode != null) {
      try {
        FileInputStream inputStream;
        inputStream = new ExternalFileUtil(getApplicationContext())
            .read(episode.getFileLocation());
        mediaPlayer.setDataSource(inputStream.getFD());
        mediaPlayer.prepareAsync();
      } catch (IOException e) {
        App.get().getEventBus().fireEvent(new PlayerErrorEvent());
      }
    }
  }

  /**
   * Whether this player is prepared to play an episode.
   */
  public boolean hasEpisode() {
    return episode != null;
  }

  public void play() {
    if (prepared) {
      mediaPlayer.start();
      episode.setPlayState(PlayState.STARTED_PLAYING);
    }
  }

  public void pause() {
    if (prepared) {
      int currentPosition = mediaPlayer.getCurrentPosition();
      if (currentPosition > episode.getSeekLocation()) {
        episode.setSeekLocation(currentPosition);
      }
      mediaPlayer.pause();
    }
  }

  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  public boolean isPrepared() {
    return prepared;
  }

  public void seekTo(int msec) {
    if (prepared) {
      mediaPlayer.seekTo(msec);
      episode.setSeekLocation(msec);
    }
  }

  public int getDuration() {
    return mediaPlayer.getDuration();
  }

  /**
   * Returns the current seek position in milliseconds.
   */
  public int getCurrentPosition() {
    return mediaPlayer.getCurrentPosition();
  }

  /**
   * Sets a callback to be called upon completion of playing the episode.
   */
  void setOnCompletionCallback(final Callback<Void> callback) {
    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {

        // set the play state to "finished" as well before we call the callback
        if (episode != null) {
          episode.setPlayState(PlayState.FINISHED);
          episode.setSeekLocation(mediaPlayer.getCurrentPosition());
        }
        prepared = false;
        callback.call(null);
      }
    });
  }

  /**
   * Sets a callback to be called upon preparing for playing the episode.
   */
  void setOnPreparedCallback(final Callback<Void> callback) {
    mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        int seekLocation = episode.getSeekLocation();
        if (episode.getSeekLocation() > 0) {
          mediaPlayer.seekTo(seekLocation);
        }
        prepared = true;
        callback.call(null);
      }
    });
  }

  class PlayerBinder extends Binder {
    PlayerService getService() {
      return PlayerService.this;
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setWakeMode(getApplicationContext(),
        PowerManager.PARTIAL_WAKE_LOCK);
    // do NOT call the completion callback on errors
    mediaPlayer.setOnErrorListener(onErrorListener);
  }

  @Override
  public void onDestroy() {
    mediaPlayer.release();
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }
}
