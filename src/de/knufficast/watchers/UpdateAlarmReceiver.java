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
