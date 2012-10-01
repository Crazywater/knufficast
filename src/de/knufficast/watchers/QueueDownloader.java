/*******************************************************************************
 * Copyright 2012 Crazywater
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.knufficast.watchers;

import java.io.File;

import android.content.Context;
import android.util.Log;
import de.knufficast.App;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.DownloadManagerTask;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.NetUtil;
import de.knufficast.util.file.PodcastFileUtil;

/**
 * Handles downloads of the queue: Can restart downloads of not yet downloaded
 * queue items and delete them.
 * 
 * @author crazywater
 * 
 */
public class QueueDownloader {
  private final Context context;
  private final NetUtil netUtil;

  public QueueDownloader(Context context) {
    this.context = context;
    this.netUtil = new NetUtil(context);
  }

  public void restartDownloads() {
    Configuration config = App.get().getConfiguration();
    for (final Episode episode : config.getQueue().asList()) {
      if (episode.getDownloadState() != DownloadState.FINISHED
          && episode.getDownloadState() != DownloadState.DOWNLOADING) {
        episode.setDownloadState(DownloadState.DOWNLOADING);
        BooleanCallback<Void, Void> finishedCallback = new BooleanCallback<Void, Void>() {
          @Override
          public void success(Void unused) {
            episode.setDownloadState(DownloadState.FINISHED);
          }

          @Override
          public void fail(Void unused) {
            episode.setDownloadState(DownloadState.ERROR);
          }
        };
        new DownloadManagerTask(context, episode, finishedCallback);
      }
    }
  }

  public void deleteDownload(Episode episode) {
    File file = new PodcastFileUtil(context).resolveFile(episode
        .getDownloadFileName());
    if (file.exists()) {
      boolean deleted = file.delete();
      if (!deleted) {
        Log.e("QueueDownloader",
            "Could not delete " + episode.getDownloadFileName());
      }
    }
    episode.setDownloadProgress(0, 0);
    episode.setDownloadState(DownloadState.NONE);
  }
}
