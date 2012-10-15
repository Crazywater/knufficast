package de.knufficast.search;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knufficast.util.BooleanCallback;
import de.knufficast.util.HttpUtil;

public class ITunesSearch implements PodcastSearch {
  private static final String SEARCH_URL = "http://itunes.apple.com/search?media=podcast&entity=podcast&term=%s";
  private static final String ERROR_CONNECTION = "No connection";
  private static final String ERROR_JSON = "JSON Error";
  private final HttpUtil httpUtil = new HttpUtil();

  public class ITunesResult implements PodcastSearch.Result {
    private String title = "";
    private String website = "";
    private String description = "";
    private String feedUrl = "";
    private String imgUrl = "";

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public String getFeedUrl() {
      return feedUrl;
    }

    public String getImgUrl() {
      return imgUrl;
    }

    public String getWebsite() {
      return website;
    }
  };

  public void search(final String query,
      final BooleanCallback<List<Result>, String> callback) {
    runOnThread(new Runnable() {
      @Override
      public void run() {
        String queryUrl = String.format(SEARCH_URL, URLEncoder.encode(query));
        HttpGet request = new HttpGet(queryUrl);
        try {
          List<Result> results = new ArrayList<Result>();
          JSONObject response = httpUtil.getJson(request);
          JSONArray resultsArray = response.getJSONArray("results");
          int max = resultsArray.length();
          for (int i = 0; i < max; i++) {
            JSONObject jsonResult = resultsArray.getJSONObject(i);
            ITunesResult result = new ITunesResult();
            result.website = jsonResult.optString("collectionViewUrl");
            result.description = jsonResult.optString("artistName");
            result.title = jsonResult.optString("collectionName");
            result.feedUrl = jsonResult.optString("feedUrl");
            result.imgUrl = jsonResult.optString("artworkUrl60");
            results.add(result);
          }
          callback.success(results);
        } catch (IOException e) {
          callback.fail(ERROR_CONNECTION);
        } catch (JSONException e) {
          callback.fail(ERROR_JSON);
        }
      }
    });
  }
  
  private void runOnThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
  }
}
