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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import de.knufficast.R;
import de.knufficast.events.EventBus;
import de.knufficast.events.NewImageEvent;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.NetUtil;
import de.knufficast.util.file.FileUtil;
import de.knufficast.util.file.InternalFileUtil;

/**
 * Caches downloaded images locally.
 * 
 * @author crazywater
 */
public class ImageCache {
  private static final String IMAGECACHE_FILENAME = "imageCache-index";
  private static final int MAX_DOWNLOAD_THREADS = 5;
  private static final int IMAGE_SIZE = 200;

  private final Map<String, String> urlToFile = new HashMap<String, String>();
  private final Map<String, BitmapDrawable> imageMap = new HashMap<String, BitmapDrawable>();

  private final Set<String> processingUrls = new HashSet<String>();
  private final Set<String> blockedUrls = new HashSet<String>();

  private final BlockingQueue<Runnable> downloadTaskQueue = new LinkedBlockingQueue<Runnable>();
  private final ThreadPoolExecutor downloadExecutor = new ThreadPoolExecutor(1,
      MAX_DOWNLOAD_THREADS, 5, TimeUnit.SECONDS, downloadTaskQueue);

  private final Context context;
  private final EventBus eventBus;
  private final NetUtil netUtil;
  private final FileUtil fileUtil;
  private final FileUtil configFileUtil;
  private BitmapDrawable defaultIcon;

  public ImageCache(Context context, EventBus eventBus, FileUtil fileUtil) {
    this.context = context;
    this.eventBus = eventBus;
    this.netUtil = new NetUtil(context);
    this.fileUtil = fileUtil;
    this.configFileUtil = new InternalFileUtil(context);
  }

  /**
   * Get a {@link Drawable} from a URL. Might return the default icon first and
   * fire a {@link NewImageEvent} later.
   */
  public BitmapDrawable getResource(final String url) {
    if (url == null || "".equals(url) || blockedUrls.contains(url)
        || processingUrls.contains(url)) {
      return getDefaultIcon();
    }
    if (urlToFile.containsKey(url)) {
      if (imageMap.get(url) == null) {
        insertDrawable(url);
        return getDefaultIcon();
      } else {
        return imageMap.get(url);
      }
    }
    if (netUtil.isOnWifi()) {
      processingUrls.add(url);
      final String filename = "imageCache-file-" + url.hashCode();
      FileUtil util = fileUtil;
      DownloadTask task = new DownloadTask(util, null,
          new BooleanCallback<Void, String>() {
            @Override
            public void success(Void unused) {
              urlToFile.put(url, filename);
              save();
              insertDrawable(url);
            }

            @Override
            public void fail(String error) {
              if (error != DownloadTask.ERROR_CONNECTION
                  && error != DownloadTask.ERROR_DATA_RANGE) {
                blockedUrls.add(url);
              } else {
                eventBus.fireEvent(new NewImageEvent(url));
              }
              processingUrls.remove(url);
            }
          });
      task.executeOnExecutor(downloadExecutor, url, filename);
    }
    return getDefaultIcon();
  }

  private void insertDrawable(final String url) {
    processingUrls.add(url);
    new AsyncTask<Void, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Void... params) {
        try {
          // get the header information only
          FileInputStream fStream = fileUtil.read(urlToFile.get(url));
          BitmapFactory.Options o = new BitmapFactory.Options();
          o.inJustDecodeBounds = true;
          BitmapFactory.decodeStream(fStream, null, o);

          if (o.outWidth <= 0 || o.outHeight <= 0) {
            throw new FileNotFoundException();
          }

          // find the scale at which we still get minimum IMAGE_SIZE pixels in
          // one dimension (must be power of 2)
          int dim = o.outWidth > o.outHeight ? o.outWidth : o.outHeight;
          int scale = 1;
          while ((dim >>= 1) > IMAGE_SIZE) {
            scale++;
          }

          // decode and put into imagemap
          BitmapFactory.Options o2 = new BitmapFactory.Options();
          o2.inSampleSize = scale;

          Bitmap bitmap = BitmapFactory.decodeStream(
              fileUtil.read(urlToFile.get(url)), null, o2);
          BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
              bitmap);
          imageMap.put(url, drawable);
          return true;
        } catch (FileNotFoundException e) {
          urlToFile.remove(url);
          return false;
        } catch (OutOfMemoryError e) {
          urlToFile.remove(url);
          return false;
        }
      }

      @Override
      public void onPostExecute(Boolean success) {
        processingUrls.remove(url);
        if (success) {
          eventBus.fireEvent(new NewImageEvent(url));
        }
      }
    }.execute(null, null);
  }

  /**
   * Deferred initialization so we know that the call to getResources works
   */
  public void init() {
    Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.logo);
    defaultIcon = new BitmapDrawable(context.getResources(), icon);
    load();
  }

  public void save() {
    FileOutputStream fos;
    try {
      fos = configFileUtil.write(IMAGECACHE_FILENAME, false);
      ObjectOutputStream os = new ObjectOutputStream(fos);
      os.writeObject(urlToFile);
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private void load() {
    try {
      FileInputStream fis = configFileUtil
          .read(IMAGECACHE_FILENAME);
      ObjectInputStream is = new ObjectInputStream(fis);
      setImages((Map<String, String>) is.readObject());
      is.close();
    } catch (IOException e) {
      // on failure: assume no cache
      setImages(new HashMap<String, String>());
      File f = configFileUtil.resolveFile(IMAGECACHE_FILENAME);
      if (f.exists()) {
        f.delete();
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void setImages(Map<String, String> newImages) {
    urlToFile.clear();
    urlToFile.putAll(newImages);
  }

  public BitmapDrawable getDefaultIcon() {
    return defaultIcon;
  }
}
