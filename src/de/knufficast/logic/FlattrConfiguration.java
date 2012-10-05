package de.knufficast.logic;

import java.io.Serializable;

import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.FlattrStatusEvent;

public class FlattrConfiguration implements Serializable {
  private static final long serialVersionUID = 1L;
  private int status;
  private String authCode;
  private String accessToken;

  public void setFlattrStatus(int stringResource) {
    if (status != stringResource) {
      status = stringResource;
    }
    App.get().getEventBus().fireEvent(new FlattrStatusEvent(status));
  }

  public void resetAuthentication() {
    setAccessToken(null);
    setAuthCode(null);
    setFlattrStatus(R.string.flattr_auth_error);
  }

  public boolean hasError() {
    return status != 0;
  }

  public int getStatusResource() {
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
