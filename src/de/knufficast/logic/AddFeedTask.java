package de.knufficast.logic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.util.Log;
import de.knufficast.logic.model.Feed;

/**
 * A background task that downloads RSS feeds, parses them and adds them to the
 * {@link Configuration}.
 * 
 * @author crazywater
 */
public class AddFeedTask extends AsyncTask<String, Void, Void> {
  private Presenter presenter;
  private Configuration configuration;
  private String error;

  public AddFeedTask(Presenter presenter, Configuration configuration) {
    this.presenter = presenter;
    this.configuration = configuration;
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
        List<Feed> feeds = new FeedDownloader()
            .getFeeds((HttpURLConnection) new URL(urls[i])
            .openConnection());
        configuration.addFeeds(feeds);
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
