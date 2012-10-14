package de.knufficast.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An utility class for HTTP interactions.
 * 
 * @author crazywater
 * 
 */
public class HttpUtil {
  private final HttpClient httpClient = new DefaultHttpClient();

  public JSONObject getJson(HttpUriRequest request) throws IOException,
      JSONException {
    HttpResponse response = httpClient.execute(request);
    String result = readAll(response.getEntity());
    return new JSONObject(result);
  }

  public JSONArray getJsonArray(HttpUriRequest request) throws IOException,
      JSONException {
    HttpResponse response = httpClient.execute(request);
    String result = readAll(response.getEntity());
    return new JSONArray(result);
  }

  /**
   * Reads an HttpEntity into an entire string.
   * 
   * @param entity
   * @return
   * @throws IOException
   */
  private String readAll(HttpEntity entity) throws IOException {
    if (entity == null) {
      return "";
    }
    InputStream inputStream = entity.getContent();
    if (inputStream == null) {
      return "";
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        inputStream));
    StringBuilder builder = new StringBuilder();
    String line = "";
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    return builder.toString();
  }
}
