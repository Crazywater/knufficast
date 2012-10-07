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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;
import de.knufficast.logic.model.XMLToDBWriter;
import de.knufficast.logic.model.XMLFeed;

/**
 * A background task that downloads RSS feeds, parses them and adds them to the
 * {@link Configuration}.
 * 
 * @author crazywater
 */
public class AddFeedTask extends AsyncTask<String, Void, Void> {
  private Presenter presenter;
  private String error;

  public AddFeedTask(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void onPreExecute() {
    presenter.onStartAddingFeed();
  }

  @Override
  protected Void doInBackground(String... urls) {
    int count = urls.length;
    for (int i = 0; i < count; i++) {
      Log.d("AddFeedTask", "Fetching Feed url " + urls[i]);
      try {
        String url = urls[i];
        // add http in the front - otherwise we get invalid protocol
        if (!url.startsWith("http://") || url.startsWith("https://")) {
          url = "http://" + url;
        }
        List<XMLFeed> feeds = new FeedDownloader()
            .getFeeds((HttpURLConnection) new URL(urls[i])
            .openConnection());
        new XMLToDBWriter().addFeeds(feeds);
      } catch (IOException e) {
        error = e.getMessage();
      } catch (XmlPullParserException e) {
        error = e.getMessage();
      }
    }
    return null;
  }

  @Override
  protected void onPostExecute(Void unused) {
    if (error == null) {
      presenter.onFeedAdded();
    } else {
      presenter.onFeedAddError(error);
    }
  }

  public interface Presenter {
    void onStartAddingFeed();
    void onFeedAdded();
    void onFeedAddError(String error);
  }
}
