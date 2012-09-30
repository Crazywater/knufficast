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
