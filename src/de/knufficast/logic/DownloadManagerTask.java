package de.knufficast.logic;

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.widget.Toast;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.logic.model.Episode;
import de.knufficast.util.BooleanCallback;

public class DownloadManagerTask {

  private final BooleanCallback<Void, Void> finishedCallback;
  private final Episode episode;
  private final long downloadTaskId;

  private final DownloadManager downloadManager;

  public DownloadManagerTask(final Context context, final Episode episode,
      final BooleanCallback<Void, Void> finishedCallback) {

    this.finishedCallback = finishedCallback;
    this.episode = episode;

    downloadManager = (DownloadManager) context
        .getSystemService(Context.DOWNLOAD_SERVICE);

    context.registerReceiver(onComplete, new IntentFilter(
        DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    int networkTypesMask = DownloadManager.Request.NETWORK_WIFI;
    if (!App.get().getConfiguration().downloadNeedsWifi()) {
      networkTypesMask |= DownloadManager.Request.NETWORK_MOBILE;
    }

    context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS).mkdirs();

    downloadTaskId = downloadManager.enqueue(new DownloadManager.Request(Uri
        .parse(episode.getDataUrl()))
        .setAllowedNetworkTypes(networkTypesMask)
        .setAllowedOverRoaming(false)
        .setTitle(episode.getTitle())
        .setDescription(Html.fromHtml(episode.getDescription()).toString())
        .setDestinationInExternalFilesDir(context,
            Environment.DIRECTORY_PODCASTS, episode.getFileLocation()));

    Toast.makeText(context, R.string.download_started, Toast.LENGTH_LONG)
        .show();
  }

  BroadcastReceiver onComplete = new BroadcastReceiver() {
    @Override
    public void onReceive(Context ctxt, Intent intent) {
      episode.setDownloadedFileName(new File(downloadManager
          .getUriForDownloadedFile(downloadTaskId).toString()).getName());
      finishedCallback.success(null);
    }
  };
}