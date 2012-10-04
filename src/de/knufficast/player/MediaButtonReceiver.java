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
