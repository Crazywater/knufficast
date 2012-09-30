package de.knufficast.watchers;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import de.knufficast.App;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.DownloadTask;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
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

  public QueueDownloader(Context context) {
    this.context = context;
    this.netUtil = new NetUtil(context);
  }

  public void restartDownloads() {
    Configuration config = App.get().getConfiguration();
    if (netUtil.isOnWifi() || !config.downloadNeedsWifi()) {
      for (final Episode episode : config.getQueue().asList()) {
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
            }

            @Override
            public void fail(Void unused) {
              episode.setDownloadState(DownloadState.ERROR);
            }
          };
          new DownloadTask(context, progressCallback, finishedCallback)
              .execute(url, episode.getFileLocation());
        }
      }
    }
  }

  public void deleteDownload(Episode episode) {
    File file = new ExternalFileUtil(context).resolveFile(episode
        .getFileLocation());
    if (file.exists()) {
      boolean deleted = file.delete();
      if (!deleted) {
        Log.e("QueueDownloader",
            "Could not delete " + episode.getFileLocation());
      }
    }
    episode.setDownloadProgress(0, 0);
    episode.setDownloadState(DownloadState.NONE);
  }
}
