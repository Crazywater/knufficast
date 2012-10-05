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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import de.knufficast.App;
import de.knufficast.logic.model.Queue;

public class MediaButtonReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
      final KeyEvent event = (KeyEvent) intent
          .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
      if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
        QueuePlayer player = App.get().getPlayer();
        Queue queue = App.get().getQueue();
        if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
          player.togglePlaying();
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) {
          queue.rotateUpward();
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
          queue.rotateDownward();
        }
      }
    }
  }

}
