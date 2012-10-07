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

import android.content.SharedPreferences;
import de.knufficast.App;
import de.knufficast.events.FlattrStatusEvent;

public class FlattrConfiguration {
  private SharedPreferences prefs;
  private static final String FLATTR_STATUS_KEY = "flattrStatus";
  private static final String FLATTR_AUTH_CODE = "flattrAuthCode";
  private static final String FLATTR_ACCESS_TOKEN = "flattrAccessToken";

  public FlattrConfiguration(SharedPreferences prefs) {
    this.prefs = prefs;
  }

  public enum FlattrStatus {
    NOT_AUTHENTICATED, AUTHENTICATING, AUTHENTICATED, ERROR, NO_MEANS
  }

  public void setFlattrStatus(FlattrStatus status) {
    if (status != getStatus()) {
      prefs.edit().putString(FLATTR_STATUS_KEY, status.name());
      App.get().getEventBus().fireEvent(new FlattrStatusEvent(status));
    }
  }

  public FlattrStatus getStatus() {
    String str = prefs.getString(FLATTR_STATUS_KEY,
        FlattrStatus.NOT_AUTHENTICATED.name());
    return FlattrStatus.valueOf(str);
  }

  public void resetAuthentication() {
    setAccessToken(null);
    setAuthCode(null);
    setFlattrStatus(FlattrStatus.NOT_AUTHENTICATED);
  }

  public void setAuthCode(String authCode) {
    prefs.edit().putString(FLATTR_AUTH_CODE, authCode);
  }

  public void setAccessToken(String accessToken) {
    prefs.edit().putString(FLATTR_ACCESS_TOKEN, accessToken);
  }

  public String getAuthCode() {
    return prefs.getString(FLATTR_AUTH_CODE, null);
  }

  public String getAccessToken() {
    return prefs.getString(FLATTR_ACCESS_TOKEN, null);
  }

  public boolean hasAccessToken() {
    return getAccessToken() != null;
  }
}
