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
package de.knufficast.flattr;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Base64;
import de.knufficast.App;
import de.knufficast.logic.FlattrConfiguration;
import de.knufficast.logic.FlattrConfiguration.FlattrStatus;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.HttpUtil;

/**
 * A class that communicates with Flattr via REST.
 * 
 * @author crazywater
 * 
 */
public class FlattrApi {
  private static final IFlattrConstants CONSTANTS = null;
  private static final String LOGIN_URL = "https://flattr.com/oauth/token";
  private static final String LOOKUP_URL = "https://api.flattr.com/rest/v2/things/lookup/?url=%s";
  private static final String FLATTR_URL = "https://api.flattr.com/rest/v2/flattr";

  public static final String ERROR_AUTHORIZATION = "Not authorized";
  public static final String ERROR_JSON = "JSON Error";
  public static final String ERROR_NO_MEANS = "No means";
  public static final String ERROR_CONNECTION = "Connection Error";
  public static final String ERROR_NOT_FOUND = "Not found";

  private final HttpUtil httpUtil = new HttpUtil();

  private final FlattrConfiguration config = App.get().getConfiguration()
      .getFlattrConfig();

  private void login() {
    try {
      JSONObject json = new JSONObject();
      json.put("code", config.getAuthCode());
      json.put("grant_type", "authorization_code");
      json.put("redirect_uri", "knufficastoauth://");
      // the client ID and secret are used as username and password
      // encode username and password using base64 user:password (BASIC auth for
      // http)
      String userPassword = "Basic "
          + new String(Base64.encode((CONSTANTS.getClientId() + ":" + CONSTANTS
              .getClientSecret()).getBytes(), Base64.NO_WRAP));
      JSONObject response = postAuthorized(LOGIN_URL, json, userPassword);
      if (response.getString("access_token") != null) {
        config.setAccessToken(response.getString("access_token"));
        config.setFlattrStatus(FlattrStatus.AUTHENTICATED);
      } else {
        config.resetAuthentication();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      config.resetAuthentication();
    }
  }

  private JSONObject lookup(final String flattrUrl) throws JSONException,
      IOException {
    String authorization = getTokenAuthorization();
    if (authorization == null) {
      return null;
    }
    String encodedUrl = URLEncoder.encode(flattrUrl);
    String requestUrl = String.format(LOOKUP_URL, encodedUrl);
    return getAuthorized(requestUrl, authorization);
  }

  /**
   * Flattrs a thing.
   */
  public void flattr(final String flattrUrl,
      final BooleanCallback<String, String> callback) {
    runOnThread(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject request = new JSONObject();
          request.put("url", flattrUrl);
          String authorization = getTokenAuthorization();
          if (authorization == null) {
            callback.fail(ERROR_AUTHORIZATION);
          }
          JSONObject result = postAuthorized(FLATTR_URL, request, authorization);
          if (result.has("error")) {
            if (result.getString("error").equals("no_means")) {
              config.setFlattrStatus(FlattrStatus.NO_MEANS);
              callback.fail(ERROR_NO_MEANS);
            } else if (result.getString("error").equals("flattr_once")) {
              callback.success(flattrUrl);
            } else if (result.getString("error").equals("unauthorized")) {
              config.setFlattrStatus(FlattrStatus.NOT_AUTHENTICATED);
              callback.fail(ERROR_AUTHORIZATION);
            } else {
              callback.fail(ERROR_NOT_FOUND);
            }
          }
          if (!result.has("message")) {
            throw new JSONException("No message field");
          } else {
            String str = result.getString("message");
            if ("ok".equals(str)) {
              callback.success(flattrUrl);
            } else {
              throw new JSONException("Message field is not ok");
            }
          }
        } catch (JSONException e) {
          callback.fail(ERROR_JSON);
        } catch (IOException e) {
          callback.fail(ERROR_CONNECTION);
        }
      }
    });
  }

  /**
   * Inform this client of an OAuth response.
   * 
   * @param data
   *          the URI of the OAuth response
   */
  public void setOauthResponse(final String data) {
    runOnThread(new Runnable() {
      @Override
      public void run() {
        Uri uri = Uri.parse(data);
        FlattrConfiguration config = App.get().getConfiguration()
            .getFlattrConfig();
        config.setFlattrStatus(FlattrStatus.AUTHENTICATING);
        if (uri.getQueryParameter("code") != null) {
          config.setAuthCode(uri.getQueryParameter("code"));
          login();
        } else {
          config.setFlattrStatus(FlattrStatus.ERROR);
        }
      }
    });
  }

  /**
   * Asks the flattr servers whether a thing has been flattred.
   */
  public void isFlattred(final String flattrUrl,
      final BooleanCallback<Boolean, String> callback) {
    runOnThread(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject thing = lookup(flattrUrl);
          if (thing == null) {
            callback.fail(ERROR_CONNECTION);
            return;
          }
          if (thing.has("flattred")) {
            boolean result = thing.getBoolean("flattred");
            callback.success(result);
          } else if (thing.has("message")) {
            if ("flattrable".equals(thing.getString("message"))) {
              // thing is flattrable but hasn't been flattred yet
              callback.success(false);
            } else {
              callback.fail(ERROR_JSON);
            }
          } else {
            callback.fail(ERROR_JSON);
          }
        } catch (JSONException e) {
          callback.fail(ERROR_JSON);
        } catch (IOException e) {
          callback.fail(ERROR_CONNECTION);
        }
      }
    });
  }

  private void runOnThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.start();
  }

  private String getTokenAuthorization() {
    String accessToken = config.getAccessToken();
    if (accessToken == null) {
      return null;
    }
    return "Bearer " + accessToken;
  }

  private JSONObject getAuthorized(String httpsUrl, String authorization)
      throws JSONException, IOException {
    HttpGet request = new HttpGet(httpsUrl);
    request.addHeader("Authorization", authorization);
    return httpUtil.getJson(request);
  }

  private JSONObject postAuthorized(String httpsUrl, JSONObject contents,
      String authorization)
          throws JSONException, IOException {
    HttpPost request = new HttpPost(httpsUrl);
    request.addHeader("Authorization", authorization);
    request.addHeader("content-type", "application/json");
    if (contents != null) {
      StringEntity params = new StringEntity(contents.toString());
      request.setEntity(params);
    }
    return httpUtil.getJson(request);
  }
}
