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
package de.knufficast.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import de.knufficast.App;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.Callback;
import de.knufficast.util.file.ExternalFileUtil;

/**
 * An {@link AsyncTask} that downloads files to external storage and can report
 * its progress to callbacks. Takes two arguments in {@link #execute}: input URL
 * and output filename. The progress is reported in bytes downloaded and bytes
 * total.
 * 
 * @author crazywater
 * 
 */
public class DownloadTask extends AsyncTask<String, Long, Boolean> {
  private final Context context;
  private final Callback<Pair<Long, Long>> progressCallback;
  private final BooleanCallback<Void, Void> finishedCallback;
  private String urlStr;
  private String filename;
  private long lastPublishTimestamp;

  // How often progress is reported to the callback
  private static final long PUBLISH_INTERVAL = 250; // ms

  public DownloadTask(Context context,
      Callback<Pair<Long, Long>> progressCallback,
      BooleanCallback<Void, Void> finishedCallback) {
    this.context = context;
    this.progressCallback = progressCallback;
    this.finishedCallback = finishedCallback;
  }

  @Override
  protected Boolean doInBackground(String... urlAndFilename) {
    App.get().getLockManager().lockWifi(urlAndFilename);
    try {
      if (urlAndFilename.length != 2) {
        throw new IllegalArgumentException("Wrong number of download arguments");
      }
      urlStr = urlAndFilename[0];
      filename = urlAndFilename[1];
      
      // open input
      URL url = new URL(urlStr);
      
      HttpURLConnection.setFollowRedirects(true);

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      File file = new ExternalFileUtil(context).resolveFile(filename);
      long initiallyDownloaded = file.length();

      boolean append = initiallyDownloaded > 0;

      // resume download if possible
      if (append) {
        connection.setRequestProperty("Range", "bytes=" + initiallyDownloaded
            + "-");
      }
      if (!isCancelled()) {
        connection.connect();
        if (connection.getResponseCode() / 100 == 3) {
          // redirect
          String location = connection.getHeaderField("Location");
          connection = (HttpURLConnection) new URL(location).openConnection();
          if (append) {
            connection.setRequestProperty("Range", "bytes="
                + initiallyDownloaded + "-");
          }
          connection.connect();
        }
        if (connection.getResponseCode() == 416) {
          file.delete();
          throw new RuntimeException("Invalid data range, need to redownload");
        }

        // check responsecode 2xx
        if (connection.getResponseCode() / 100 != 2) {
          throw new RuntimeException("Weird response code "
              + connection.getResponseCode());
        }
        if (!"bytes".equals(connection.getHeaderField("Accept-Ranges"))) {
          append = false;
        }
  
        // open output
        FileOutputStream output = new FileOutputStream(file, append);
  
        // open input
        InputStream input = new BufferedInputStream(connection.getInputStream());
  
        // check content length
        int contentLength = connection.getContentLength();
        if (!(contentLength > 0)) {
          throw new RuntimeException("Weird content length "
              + connection.getContentLength());
        }
  
        byte data[] = new byte[1024];
  
        int count = 0;
        long downloaded = initiallyDownloaded;
        while (!isCancelled() && (count = input.read(data)) != -1) {
          output.write(data, 0, count);
          downloaded += count;
          publishProgressRateLimited(downloaded, contentLength
              + initiallyDownloaded);
        }
  
        output.flush();
        output.close();
        input.close();
      }

      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      App.get().getLockManager().unlockWifi(urlAndFilename);
    }
  }

  /**
   * Rate-limits the progress such that it isn't reported more often than
   * {@link #PUBLISH_INTERVAL}.
   */
  private void publishProgressRateLimited(long downloaded, long length) {
    long now = System.currentTimeMillis();
    if (now - lastPublishTimestamp >= PUBLISH_INTERVAL) {
      lastPublishTimestamp = now;
      publishProgress(downloaded, length);
    }
  }

  @Override
  protected void onProgressUpdate(Long... progress) {
    if (progressCallback != null) {
      progressCallback.call(Pair.create(progress[0], progress[1]));
    }
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if (finishedCallback != null) {
      if (result) {
        finishedCallback.success(null);
      } else {
        finishedCallback.fail(null);
      }
    }
  }
}
