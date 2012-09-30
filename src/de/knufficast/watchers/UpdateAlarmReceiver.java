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
package de.knufficast.watchers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A {@link BroadcastReceiver} that receives alarms and kicks off the
 * {@link UpdaterService} to refresh feeds.
 * 
 * @author crazywater
 * 
 */
public class UpdateAlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    context.startService(new Intent(context, UpdaterService.class));
  }
}
