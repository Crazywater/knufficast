package de.knufficast.gpodder;

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

public class GPodderSearch {
  private static final String SEARCH_URL = "http://gpodder.net/search.json?q=%s";
  private static final String ERROR_CONNECTION = "No connection";
  private static final String ERROR_JSON = "JSON Error";
  private final HttpUtil httpUtil = new HttpUtil();

  public class Result {
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

    public String feedUrl() {
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
          JSONArray response = httpUtil.getJsonArray(request);
          int max = response.length();
          for (int i = 0; i < max; i++) {
            JSONObject jsonResult = response.getJSONObject(i);
            Result result = new Result();
            result.website = jsonResult.optString("website");
            result.description = jsonResult.optString("description");
            result.title = jsonResult.optString("title");
            result.feedUrl = jsonResult.optString("url");
            result.imgUrl = jsonResult.optString("scaled_logo_url");
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
