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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import de.knufficast.App;
import de.knufficast.logic.DownloadTask;
import de.knufficast.logic.db.Configuration;
import de.knufficast.logic.db.DBEpisode;
import de.knufficast.logic.db.DBEpisode.DownloadState;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.Callback;
import de.knufficast.util.NetUtil;
import de.knufficast.util.file.ExternalFileUtil;

/**
 * Handles downloads of the queue: Can restart downloads of not yet downloaded
 * queue items and delete them.
 * 
 * @author crazywater
 * 
 */
public class QueueDownloader {
  private Context context;
  private NetUtil netUtil;

  private static QueueDownloader instance;

  private Map<DBEpisode, DownloadTask> downloadTasks = new HashMap<DBEpisode, DownloadTask>();

  private static final int MAX_DOWNLOAD_THREADS = 2;
  private final BlockingQueue<Runnable> downloadTaskQueue = new LinkedBlockingQueue<Runnable>();
  private final ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(1,
      MAX_DOWNLOAD_THREADS, 1, TimeUnit.SECONDS, downloadTaskQueue);

  private QueueDownloader(Context context) {
    this.context = context;
    this.netUtil = new NetUtil(context);
  }

  public static QueueDownloader get() {
    if (instance == null) {
      instance = new QueueDownloader(App.get());
    }
    return instance;
  }

  public void restartDownloads() {
    Configuration config = App.get().getConfiguration();
    if (netUtil.isOnWifi() || !config.downloadNeedsWifi()) {
      for (final DBEpisode episode : App.get().getQueue().asList()) {
        if (episode.getDownloadState() != DownloadState.FINISHED
            && episode.getDownloadState() != DownloadState.DOWNLOADING) {
          final String url = episode.getDataUrl();
          episode.setDownloadState(DownloadState.DOWNLOADING);
          Callback<Pair<Long, Long>> progressCallback = new Callback<Pair<Long, Long>>() {
            @Override
            public void call(Pair<Long, Long> progress) {
              episode.setDownloadProgress(progress.first, progress.second);
            }
          };
          BooleanCallback<Void, Void> finishedCallback = new BooleanCallback<Void, Void>() {
            @Override
            public void success(Void unused) {
              episode.setDownloadState(DownloadState.FINISHED);
              downloadTasks.remove(episode);
            }

            @Override
            public void fail(Void unused) {
              episode.setDownloadState(DownloadState.ERROR);
              downloadTasks.remove(episode);
            }
          };
          DownloadTask task = new DownloadTask(context, progressCallback, finishedCallback);
          downloadTasks.put(episode, task);
          task.executeOnExecutor(downloadExecutor, url,
              episode.getFileLocation());
        }
      }
    }
  }

  public void pauseAllDownloads() {
    for (Map.Entry<DBEpisode, DownloadTask> entry : downloadTasks.entrySet()) {
      DBEpisode episode = entry.getKey();
      DownloadTask downloadTask = entry.getValue();
      episode.setDownloadState(DownloadState.PAUSED);
      downloadTask.cancel(true);
    }
    downloadTasks.clear();
  }

  public void deleteDownload(DBEpisode episode) {
    if (downloadTasks.containsKey(episode)) {
      downloadTasks.get(episode).cancel(true);
      downloadTasks.remove(episode);
    }
    File file = new ExternalFileUtil(context).resolveFile(episode
        .getFileLocation());
    if (file.exists()) {
      boolean deleted = file.delete();
      if (!deleted) {
        Log.e("QueueDownloader",
            "Could not delete " + episode.getFileLocation());
      }
    }
    episode.setDownloadProgress(0, episode.getTotalBytes());
    episode.setDownloadState(DownloadState.NONE);
  }
}
