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

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import de.knufficast.App;
import de.knufficast.logic.ImageCache;
import de.knufficast.logic.model.DBEpisode;
import de.knufficast.logic.model.DBFeed;

/**
 * Controls the RemoteControlClient for the media server (lock screen controls).
 * 
 * @author crazywater
 * 
 */
public class RemoteController {
  private RemoteControlClient remoteControlClient;

  /**
   * Register the remote control at the audio manager.
   */
  public void register(Context context, AudioManager audioManager) {
    if (remoteControlClient == null) {
      ComponentName myEventReceiver = new ComponentName(
          context.getPackageName(), MediaButtonReceiver.class.getName());
      audioManager.registerMediaButtonEventReceiver(myEventReceiver);
      // build the PendingIntent for the remote control client
      Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
      mediaButtonIntent.setComponent(myEventReceiver);
      // create and register the remote control client
      PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0,
          mediaButtonIntent, 0);
      remoteControlClient = new RemoteControlClient(mediaPendingIntent);
      remoteControlClient
          .setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
              | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
              | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
      audioManager.registerRemoteControlClient(remoteControlClient);
    }
  }

  /**
   * Update the state of the remote control.
   */
  public void updateState(boolean isPlaying) {
    if (remoteControlClient != null) {
      if (isPlaying) {
        remoteControlClient
            .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
      } else {
        remoteControlClient
            .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
      }
    }
  }

  /**
   * Updates the state of the remote control to "stopped".
   */
  public void stop() {
    if (remoteControlClient != null) {
      remoteControlClient
          .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
    }
  }

  /**
   * Set the metadata of this episode according to the episode.
   */
  public void updateMetadata(DBEpisode episode, long duration) {
    if (remoteControlClient != null) {
      MetadataEditor editor = remoteControlClient.editMetadata(true);
      DBFeed feed = episode.getFeed();

      ImageCache imageCache = App.get().getImageCache();
      String imgUrl = episode.getImgUrl();
      BitmapDrawable episodeIcon = imageCache.getResource(imgUrl);
      if (episodeIcon.equals(imageCache.getDefaultIcon())) {
        episodeIcon = imageCache.getResource(feed.getImgUrl());
      }
      editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK,
          episodeIcon.getBitmap());

      editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration);

      editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
          feed.getTitle());
      editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
          episode.getTitle());
      editor.apply();
    }
  }

  /**
   * Release the remote control.
   */
  public void release() {
    remoteControlClient = null;
  }
}
