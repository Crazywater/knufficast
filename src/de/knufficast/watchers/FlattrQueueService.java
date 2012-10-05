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

/**
 * A service that processes the {@link FlattrQueue} and flattrs the entries. Can
 * be started via intent.
 * 
 * @author crazywater
 * 
 */
public class FlattrQueueService extends IntentService {
  private final FlattrApi flattrApi = new FlattrApi();
  private final NetUtil netUtil;

  public FlattrQueueService() {
    super("FlattrQueueService");
    netUtil = new NetUtil(this);
  }

  private void processQueue() {
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
