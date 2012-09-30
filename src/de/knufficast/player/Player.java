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

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.PlayState;
import de.knufficast.util.Callback;
import de.knufficast.util.file.ExternalFileUtil;

/**
 * An audio player for episodes, based on the android {@link MediaPlayer}.
 * 
 * @author crazywater
 * 
 */
public class Player {
  private final MediaPlayer mediaPlayer = new MediaPlayer();
  private final Context context;
  private Episode episode;

  public Player(Context context) {
    this.context = context;
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
  }

  /**
   * Prepares the player to play the episode.
   * 
   * @throws IOException
   *           if we can't load the episode
   */
  public void setEpisode(Episode episode) throws IOException {
    if (episode == this.episode) {
      return;
    }
    this.episode = episode;
    mediaPlayer.reset();
    if (episode != null) {
      FileInputStream inputStream = new ExternalFileUtil(context).read(episode
          .getFileLocation());
      mediaPlayer.setDataSource(inputStream.getFD());
      mediaPlayer.prepare();
      int seekLocation = episode.getSeekLocation();
      if (episode.getSeekLocation() > 0) {
        mediaPlayer.seekTo(seekLocation);
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
    mediaPlayer.start();
    episode.setPlayState(PlayState.STARTED_PLAYING);
  }

  public void pause() {
    int currentPosition = mediaPlayer.getCurrentPosition();
    if (currentPosition > episode.getSeekLocation()) {
      episode.setSeekLocation(currentPosition);
    }
    mediaPlayer.pause();
  }

  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  public void seekTo(int msec) {
    mediaPlayer.seekTo(msec);
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
  public void setOnCompletionCallback(final Callback<Void> callback) {
    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        // set the play state to "finished" as well before we call the callback
        if (episode != null) {
          episode.setPlayState(PlayState.FINISHED);
        }
        callback.call(null);
      }
    });
  }
}
