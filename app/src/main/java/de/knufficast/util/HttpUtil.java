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
