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

import java.io.Serializable;

import de.knufficast.App;
import de.knufficast.events.FlattrStatusEvent;

public class FlattrConfiguration implements Serializable {
  private static final long serialVersionUID = 1L;
  private FlattrStatus status;
  private String authCode;
  private String accessToken;

  public enum FlattrStatus {
    NOT_AUTHENTICATED, AUTHENTICATING, AUTHENTICATED, ERROR, NO_MEANS
  }

  public void setFlattrStatus(FlattrStatus status) {
    if (this.status != status) {
      this.status = status;
    }
    App.get().getEventBus().fireEvent(new FlattrStatusEvent(status));
  }

  public void resetAuthentication() {
    setAccessToken(null);
    setAuthCode(null);
    setFlattrStatus(FlattrStatus.NOT_AUTHENTICATED);
  }

  public FlattrStatus getStatus() {
    return status;
  }

  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAuthCode() {
    return authCode;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public boolean hasAccessToken() {
    return accessToken != null;
  }
}
