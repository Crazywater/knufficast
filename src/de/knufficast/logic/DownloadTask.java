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
import java.io.IOException;
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
import de.knufficast.util.file.FileUtil;

/**
 * An {@link AsyncTask} that downloads files to external storage and can report
 * its progress to callbacks. Takes two arguments in {@link #execute}: input URL
 * and output filename. The progress is reported in bytes downloaded and bytes
 * total.
 * 
 * @author crazywater
 * 
 */
public class DownloadTask extends AsyncTask<String, Long, String> {
  private final Callback<Pair<Long, Long>> progressCallback;
  private final BooleanCallback<Void, String> finishedCallback;
  private final FileUtil fileUtil;
  private String urlStr;
  private String filename;
  private long lastPublishTimestamp;

  public static final String SUCCESS = "Success";
  public static final String ERROR_DATA_RANGE = "Invalid data range";
  public static final String ERROR_RESPONSE_CODE = "Invalid response code";
  public static final String ERROR_CONTENT_LENGTH = "Invalid content length";
  public static final String ERROR_CONNECTION = "Connection error";
  public static final String ERROR_OTHER = "Unknown error";

  // How often progress is reported to the callback
  private static final long PUBLISH_INTERVAL = 250; // ms

  public DownloadTask(Context context,
      Callback<Pair<Long, Long>> progressCallback,
      BooleanCallback<Void, String> finishedCallback) {
    this.progressCallback = progressCallback;
    this.finishedCallback = finishedCallback;
    fileUtil = new ExternalFileUtil(context);
  }

  public DownloadTask(FileUtil fileUtil,
      Callback<Pair<Long, Long>> progressCallback,
      BooleanCallback<Void, String> finishedCallback) {
    this.progressCallback = progressCallback;
    this.finishedCallback = finishedCallback;
    this.fileUtil = fileUtil;
  }

  @Override
  protected String doInBackground(String... urlAndFilename) {
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
      File file = fileUtil.resolveFile(filename);
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
          throw new RuntimeException(ERROR_DATA_RANGE);
        }

        // check responsecode 2xx
        if (connection.getResponseCode() / 100 != 2) {
          throw new RuntimeException(ERROR_RESPONSE_CODE);
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
          throw new RuntimeException(ERROR_CONTENT_LENGTH);
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
      return SUCCESS;
    } catch (IOException e) {
      return ERROR_CONNECTION;
    } catch (RuntimeException e) {
      return e.getMessage();
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
  protected void onPostExecute(String result) {
    if (finishedCallback != null) {
      if (result == SUCCESS) {
        finishedCallback.success(null);
      } else {
        finishedCallback.fail(result);
      }
    }
  }
}
