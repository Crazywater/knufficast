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

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class LockManager {
  private final Context context;
  private final Set<Object> wifiLocks = new HashSet<Object>();
  private final Set<Object> cpuLocks = new HashSet<Object>();
  private WifiLock wifiLock;
  private WakeLock cpuLock;

  public LockManager(Context context) {
    this.context = context;
  }

  public void init() {
    wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
        .createWifiLock(WifiManager.WIFI_MODE_FULL, "knufficastWifiLock");
    cpuLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "knufficastCpuLock");
  }

  public synchronized void lockCpu(Object object) {
    if (cpuLocks.isEmpty()) {
      cpuLock.acquire();
    }
    cpuLocks.add(object);
  }

  public synchronized void unlockCpu(Object object) {
    cpuLocks.remove(object);
    if (cpuLocks.isEmpty()) {
      cpuLock.release();
    }
  }

  public synchronized void lockWifi(Object object) {
    lockCpu(object);
    if (wifiLocks.isEmpty()) {
      wifiLock.acquire();
    }
    wifiLocks.add(object);
  }

  public synchronized void unlockWifi(Object object) {
    wifiLocks.remove(object);
    if (wifiLocks.isEmpty()) {
      wifiLock.release();
    }
    unlockCpu(object);
  }
}
