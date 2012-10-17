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
import android.util.Log;
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
  private static final int MAX_RETRIES = 3;

  private final Map<String, String> urlToFile = new HashMap<String, String>();
  private final Map<String, BitmapDrawable> imageMap = new HashMap<String, BitmapDrawable>();

  private final Set<String> downloadingUrls = new HashSet<String>();

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
    return doGetResource(url, 0);
  }

  private BitmapDrawable doGetResource(final String url, final int retryDepth) {
    if (retryDepth > MAX_RETRIES) {
      downloadingUrls.remove(url);
      return getDefaultIcon();
    }
    if (url == null || "".equals(url)) {
      return getDefaultIcon();
    }
    if (urlToFile.containsKey(url)) {
      if (!imageMap.containsKey(url)) {
        if (!insertDrawable(url)) {
          doGetResource(url, retryDepth + 1);
        }
      }
      // try again
      if (imageMap.containsKey(url)) {
        return imageMap.get(url);
      }
    }
    if (netUtil.isOnWifi() && !downloadingUrls.contains(url)) {
      downloadingUrls.add(url);
      final String filename = "imageCache-file-" + url.hashCode();
      FileUtil util = fileUtil;
      DownloadTask task = new DownloadTask(util, null,
          new BooleanCallback<Void, String>() {
            @Override
            public void success(Void unused) {
              downloadingUrls.remove(url);
              notifyNewImage(url, filename);
              save();
            }

            @Override
            public void fail(String error) {
              if (error == DownloadTask.ERROR_DATA_RANGE) {
                // only retry if we got "Invalid data range" for now...
                doGetResource(url, retryDepth + 1);
              } else {
                Log.d("ImageCache", "Download error is " + error);
                downloadingUrls.remove(url);
              }
            }
          });
      task.executeOnExecutor(downloadExecutor, url, filename);
    }
    return getDefaultIcon();
  }

  /**
   * Insert a newly found image into the map and notify listeners.
   */
  private void notifyNewImage(String url, String filename) {
    urlToFile.put(url, filename);
    Log.d("ImageCache", "Firing event for " + url);
    eventBus.fireEvent(new NewImageEvent(url));
  }

  private boolean insertDrawable(String url) {
    try {
      Bitmap bitmap = BitmapFactory.decodeStream(fileUtil.read(urlToFile
          .get(url)));
      BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
          bitmap);
      imageMap.put(url, drawable);
      return true;
    } catch (FileNotFoundException e) {
      urlToFile.remove(url);
      return false;
    }
  }

  /**
   * Deferred initialization so we know that the call to getResources works
   */
  public void init() {
    Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.logo);
    defaultIcon = new BitmapDrawable(context.getResources(), icon);
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
  public void load() {
    try {
      FileInputStream fis = configFileUtil
          .read(IMAGECACHE_FILENAME);
      ObjectInputStream is = new ObjectInputStream(fis);
      setImages((Map<String, String>) is.readObject());
      is.close();
    } catch (IOException e) {
      // on failure: assume no cache
      setImages(new HashMap<String, String>());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void setImages(Map<String, String> newImages) {
    imageMap.clear();
    urlToFile.clear();
    for (Map.Entry<String, String> entry : newImages.entrySet()) {
      notifyNewImage(entry.getKey(), entry.getValue());
    }
  }

  public BitmapDrawable getDefaultIcon() {
    return defaultIcon;
  }
}
