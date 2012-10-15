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
package de.knufficast.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.knufficast.logic.db.DBEpisode;
import de.knufficast.ui.main.MainActivity;

/**
 * Controls the notification area.
 * 
 * @author crazywater
 * 
 */
public class NotificationAreaController {
  private static final int NOTIFICATION_ID = 1;

  private final PlayerService player;
  private final Context context;

  public NotificationAreaController(PlayerService player) {
    this.player = player;
    this.context = player.getApplicationContext();
  }

  public void register(DBEpisode episode) {
    PendingIntent pi = PendingIntent.getActivity(context, 0,
        new Intent(context, MainActivity.class),
        PendingIntent.FLAG_UPDATE_CURRENT);

    String title = episode.getTitle();
    String moreInfo = episode.getFeed().getTitle();
    Notification notification = new Notification.Builder(context)
        .setSmallIcon(android.R.drawable.ic_media_play).setOngoing(true)
        .setContentTitle(title).setContentText(moreInfo).setContentIntent(pi)
        .getNotification();

    player.startForeground(NOTIFICATION_ID, notification);
  }

  public void unregister() {
    player.stopForeground(true);
  }
}
