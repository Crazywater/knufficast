package de.knufficast.util;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * An utility to find out the current network status of the device.
 * 
 * @author crazywater
 * 
 */
public class NetUtil {
  private Context context;

  public NetUtil(Context context) {
    this.context = context;
  }

  private NetworkInfo getNetInfo() {
    return ((ConnectivityManager) context
        .getSystemService(Service.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
  }

  /**
   * Whether the device is connected to the internet.
   */
  public boolean isOnline() {
    NetworkInfo net = getNetInfo();
    if (net == null) {
      return false;
    }
    return net.isConnected();
  }

  /**
   * Whether the device has WiFi connectivity.
   */
  public boolean isOnWifi() {
    NetworkInfo net = getNetInfo();
    if (net == null) {
      return false;
    }
    return getNetInfo().getType() == ConnectivityManager.TYPE_WIFI;
  }
}
