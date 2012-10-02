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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import de.knufficast.R;
import de.knufficast.events.EventBus;
import de.knufficast.events.NewImageEvent;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.file.ExternalFileUtil;
import de.knufficast.util.file.InternalFileUtil;

/**
 * Caches downloaded images locally.
 * 
 * @author crazywater
 */
public class ImageCache {
  private static final String IMAGECACHE_FILENAME = "imageCache-index";

  private final Map<String, String> urlToFile = new HashMap<String, String>();
  private final Map<String, BitmapDrawable> imageMap = new HashMap<String, BitmapDrawable>();
  private final Set<String> unsuccessfulUrls = new HashSet<String>();

  private final Context context;
  private final EventBus eventBus;
  private BitmapDrawable defaultIcon;

  public ImageCache(Context context, EventBus eventBus) {
    this.context = context;
    this.eventBus = eventBus;
  }

  /**
   * Get a {@link Drawable} from a URL. Might return the default icon first and
   * fire an {@link NewImageEvent} later.
   */
  public BitmapDrawable getResource(final String url) {
    if (url == null || "".equals(url)) {
      return getDefaultIcon();
    }
    if (imageMap.containsKey(url)) {
      return imageMap.get(url);
    }
    if (!unsuccessfulUrls.contains(url)) {
      final String filename = "imageCache-file-" + url.hashCode();
      new DownloadTask(context, null, new BooleanCallback<Void, Void>() {
        @Override
        public void success(Void unused) {
          notifyNewImage(url, filename);
        }

        @Override
        public void fail(Void unused) {
          unsuccessfulUrls.add(url);
        }
      }).execute(url, filename);
    }
    return getDefaultIcon();
  }

  /**
   * Insert a newly found image into the map and notify listeners.
   */
  private void notifyNewImage(String url, String filename) {
    try {
      Bitmap bitmap = BitmapFactory.decodeStream(new ExternalFileUtil(context)
          .read(filename));
      // bitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true);
      BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
          bitmap);

      imageMap.put(url, drawable);
      urlToFile.put(url, filename);
      eventBus.fireEvent(new NewImageEvent(url));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
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
      fos = new InternalFileUtil(context).write(IMAGECACHE_FILENAME, false);
      ObjectOutputStream os = new ObjectOutputStream(fos);
      os.writeObject(urlToFile);
      os.writeObject(unsuccessfulUrls);
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public void load() {
    try {
      FileInputStream fis = new InternalFileUtil(context)
          .read(IMAGECACHE_FILENAME);
      ObjectInputStream is = new ObjectInputStream(fis);
      setImages((Map<String, String>) is.readObject());
      setUnsuccessfulUrls((Set<String>) is.readObject());
      is.close();
    } catch (IOException e) {
      // on failure: assume no cache
      setImages(new HashMap<String, String>());
      setUnsuccessfulUrls(new HashSet<String>());
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

  private void setUnsuccessfulUrls(Set<String> newUrls) {
    unsuccessfulUrls.clear();
    unsuccessfulUrls.addAll(newUrls);
  }

  public BitmapDrawable getDefaultIcon() {
    return defaultIcon;
  }
}
