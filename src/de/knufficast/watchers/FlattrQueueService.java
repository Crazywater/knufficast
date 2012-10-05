package de.knufficast.watchers;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import de.knufficast.App;
import de.knufficast.flattr.FlattrApi;
import de.knufficast.flattr.FlattrQueue;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.FlattrState;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.NetUtil;

public class FlattrQueueService extends IntentService {
  private final FlattrApi flattrApi = new FlattrApi();
  private final NetUtil netUtil;

  public FlattrQueueService() {
    super("FlattrQueueService");
    netUtil = new NetUtil(this);
  }

  public void processQueue() {
    if (netUtil.isOnline()) {
      final FlattrQueue flattrQueue = App.get().getFlattrQueue();
      while (!flattrQueue.isEmpty()) {
        final Episode episode = flattrQueue.pop();
        flattrApi.flattr(episode.getFlattrUrl(),
            new BooleanCallback<String, String>() {
              @Override
              public void success(String flattrId) {
                Log.d("Flattring successful", "Flattring successful: "
                    + flattrId);

                episode.setFlattrState(FlattrState.FLATTRED);
              }

              @Override
              public void fail(String error) {
                Log.d("FlattrQueueService", "Flattring failed: " + error);
                if (error.equals(FlattrApi.ERROR_CONNECTION)) {
                  flattrQueue.enqueue(episode);
                } else {
                  episode.setFlattrState(FlattrState.ERROR);
                }
              }
            });
      }
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    processQueue();
  }
}
