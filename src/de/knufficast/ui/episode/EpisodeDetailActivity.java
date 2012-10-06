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
package de.knufficast.ui.episode;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EpisodeDownloadProgressEvent;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.FlattrStateEvent;
import de.knufficast.events.Listener;
import de.knufficast.events.PlayerProgressEvent;
import de.knufficast.flattr.FlattrApi;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
import de.knufficast.logic.model.Episode.FlattrState;
import de.knufficast.logic.model.Episode.PlayState;
import de.knufficast.logic.model.Feed;
import de.knufficast.logic.model.Queue;
import de.knufficast.ui.feed.FeedDetailActivity;
import de.knufficast.ui.main.MainActivity;
import de.knufficast.ui.settings.SettingsActivity;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.NetUtil;
import de.knufficast.util.TimeUtil;
import de.knufficast.watchers.QueueDownloader;

/**
 * An activity to show detailed information about an episode.
 * 
 * @author crazywater
 */
public class EpisodeDetailActivity extends Activity {
  public static final String EPISODE_GUID_INTENT = "episodeGuidIntent";

  private Episode episode;
  private Feed feed;

  private Queue queue;
  private EventBus eventBus;

  private final TimeUtil timeUtil = new TimeUtil();

  private WebView description;
  private TextView title;
  private TextView episodeState;
  private ImageView icon;

  private ProgressBar downloadProgress;
  private ProgressBar listeningProgress;
  private ProgressBar flattringProgress;
  private TextView downloadProgressText;
  private TextView listeningProgressText;
  private TextView flattringProgressText;

  private Listener<EpisodeDownloadProgressEvent> downloadProgressListener = new Listener<EpisodeDownloadProgressEvent>() {
    @Override
    public void onEvent(EpisodeDownloadProgressEvent event) {
      if (event.getIdentifier().equals(episode.getIdentifier())) {
        updateDownloadState();
      }
    }
  };
  private Listener<EpisodeDownloadStateEvent> downloadStateListener = new Listener<EpisodeDownloadStateEvent>() {
    @Override
    public void onEvent(EpisodeDownloadStateEvent event) {
      if (event.getIdentifier().equals(episode.getIdentifier())) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            updateDownloadState();
          }
        });
      }
    }
  };
  private Listener<PlayerProgressEvent> playerProgressListener = new Listener<PlayerProgressEvent>() {
    @Override
    public void onEvent(final PlayerProgressEvent event) {
      if (episode.equals(event.getEpisode())) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (episode.equals(event.getEpisode())) {
              updatePlayingState(event.getProgress(), event.getTotal());
            }
          }
        });
      }
    }
  };
  private Listener<FlattrStateEvent> flattrStateListener = new Listener<FlattrStateEvent>() {
    @Override
    public void onEvent(final FlattrStateEvent event) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          updateFlattringState();
        }
      });
    }
  };


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.activity_episode_detail);

    queue = App.get().getQueue();
    eventBus = App.get().getEventBus();

    Button enqueueButton = (Button) findViewById(R.id.episode_enqueue_button);
    enqueueButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (queue.contains(episode)) {
          queue.remove(episode);
        } else {
          queue.add(episode);
        }
        updateButton();
      }
    });

    title = (TextView) findViewById(R.id.episode_title_text);
    description = (WebView) findViewById(R.id.episode_description_text);
    icon = (ImageView) findViewById(R.id.episode_icon);
    episodeState = (TextView) findViewById(R.id.episode_state);
    downloadProgress = (ProgressBar) findViewById(R.id.episode_download_progress);
    listeningProgress = (ProgressBar) findViewById(R.id.episode_listening_progress);
    flattringProgress = (ProgressBar) findViewById(R.id.episode_flattring_progress);
    downloadProgressText = (TextView) findViewById(R.id.episode_download_progress_text);
    listeningProgressText = (TextView) findViewById(R.id.episode_listening_progress_text);
    flattringProgressText = (TextView) findViewById(R.id.episode_flattring_progress_text);

    // make the background transparent
    description.setBackgroundColor(0);
  }

  @Override
  public void onStart() {
    super.onStart();
    String episodeGuid = getIntent().getExtras().getString(EPISODE_GUID_INTENT);
    String feedUrl = getIntent().getExtras().getString(
        FeedDetailActivity.FEED_URL_INTENT);
    feed = App.get().getConfiguration().getFeed(feedUrl);
    episode = feed.getEpisode(episodeGuid);
    setEpisode(episode);
    eventBus
        .addListener(EpisodeDownloadStateEvent.class, downloadStateListener);
    eventBus.addListener(EpisodeDownloadProgressEvent.class,
        downloadProgressListener);
    eventBus.addListener(PlayerProgressEvent.class, playerProgressListener);
    eventBus.addListener(FlattrStateEvent.class, flattrStateListener);

    downloadProgress.setMax(100);
    listeningProgress.setMax(100);
    flattringProgress.setMax(100);

    if (episode.hasFlattr()) {
      NetUtil netUtil = new NetUtil(this);
      if (netUtil.isOnline()) {
        // check if the flattr state has changed
        FlattrApi flattrApi = new FlattrApi();
        Log.d("EpisodeDetailActivity", "Finding flattr state");
        flattrApi.isFlattred(episode.getFlattrUrl(),
            new BooleanCallback<Boolean, String>() {
              @Override
              public void success(Boolean flattred) {
                // successfully got new flattring state
                Log.d("EpisodeDetailActivity", "Episode got new flattr state "
                    + flattred);
                if (episode.getFlattrState() != FlattrState.ENQUEUED
                    || flattred) {
                  episode.setFlattrState(flattred ? FlattrState.FLATTRED
                      : FlattrState.NONE);
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      updateFlattringState();
                    }
                  });
                }
              }
              @Override
              public void fail(String error) {
                // do nothing, might just not have a good connection...
              }
            });
      }
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    eventBus.removeListener(EpisodeDownloadStateEvent.class,
        downloadStateListener);
    eventBus.removeListener(EpisodeDownloadProgressEvent.class,
        downloadProgressListener);
    eventBus.removeListener(PlayerProgressEvent.class, playerProgressListener);
    eventBus.removeListener(FlattrStateEvent.class, flattrStateListener);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_episode_detail, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    DownloadState state = episode.getDownloadState();
    boolean downloaded = state == DownloadState.FINISHED
        || state == DownloadState.PAUSED || state == DownloadState.ERROR;
    menu.setGroupEnabled(R.id.menugroup_downloaded, downloaded);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      // The Android way to ensure correct behavior of the "Up" button in the
      // action bar
      Intent parentActivityIntent = new Intent(this, MainActivity.class);
      parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
          | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(parentActivityIntent);
      finish();
      return true;
    case R.id.menu_flattr:
      episode.setFlattrState(FlattrState.ENQUEUED);
      App.get().getFlattrQueue().enqueue(episode);
      updateFlattringState();
      return true;
    case R.id.menu_settings:
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    case R.id.menu_delete_download:
      new QueueDownloader(getApplicationContext()).deleteDownload(episode);
      updateDownloadState();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setEpisode(Episode episode) {
    title.setText(episode.getTitle());
    String contentType = feed.getEncoding() == null ? "text/html"
        : "text/html; charset=" + feed.getEncoding();
    description.loadData(episode.getDescription(), contentType,
        feed.getEncoding());
    String imgUrl = episode.getImgUrl() != null ? episode.getImgUrl() : feed
        .getImgUrl();
    icon.setImageDrawable(App.get().getImageCache().getResource(imgUrl));
    updateDownloadState();
    updatePlayingState(episode.getSeekLocation(), episode.getDuration());
    updateFlattringState();
    updateButton();
  }

  private String toMegaBytes(long bytes) {
    double result = (double) bytes / (1000 * 1000);
    DecimalFormat df = new DecimalFormat("#.##");
    return df.format(result);
  }

  private void updatePlayingState(int played, int duration) {
    String text = "";
    int progress = 0;
    PlayState state = episode.getPlayState();
    if (state == PlayState.NONE) {
      text = getString(R.string.playing_state_none);
      progress = 0;
    } else if (state == PlayState.STARTED_PLAYING) {
      text = getString(R.string.playing_state_playing,
          timeUtil.formatTime(played), timeUtil.formatTime(duration));
      progress = (int) ((double) 100 * played / duration);
      Log.d("EpisodeDetailActivity", "Progress is " + progress);
    } else if (state == PlayState.FINISHED) {
      progress = 100;
    }
    if (episode.getDownloadState() == DownloadState.FINISHED && state != PlayState.FINISHED) {
      episodeState.setText(text);
    }
    setProgress(listeningProgress, listeningProgressText, progress);
  }

  private void updateFlattringState() {
    if (episode.hasFlattr()) {
      int progress = 0;
      String text = "";
      if (episode.getFlattrState() == FlattrState.NONE) {
        text = getString(R.string.flattring_state_none);
        progress = 0;
      } else if (episode.getFlattrState() == FlattrState.ENQUEUED) {
        text = getString(R.string.flattring_state_enqueued);
        progress = 50;
      } else if (episode.getFlattrState() == FlattrState.FLATTRED) {
        text = getString(R.string.flattring_state_error);
        progress = 100;
      } else if (episode.getFlattrState() == FlattrState.FLATTRED) {
        text = getString(R.string.flattring_state_flattred);
        progress = 100;
      }
      if (episode.getPlayState() == PlayState.FINISHED) {
        episodeState.setText(text);
      }
      setProgress(flattringProgress, flattringProgressText, progress);
    } else {
      flattringProgress.setVisibility(View.GONE);
      flattringProgressText.setVisibility(View.GONE);
    }
  }

  private void updateDownloadState() {
    DownloadState state = episode.getDownloadState();
    String text = "";
    String downloadStatus = " (" + toMegaBytes(episode.getDownloadedBytes())
        + "/" + toMegaBytes(episode.getTotalBytes()) + " MB)";
    int progress = 0;
    if (episode.getTotalBytes() > 0) {
      progress = (int) (((double) 100 * episode
          .getDownloadedBytes() / episode.getTotalBytes()));
    }
    if (!episode.hasDownload()) {
      text = getString(R.string.download_state_no_download);
    } else if (state == DownloadState.NONE) {
      text = getString(R.string.download_state_none);
    } else if (state == DownloadState.DOWNLOADING) {
      text = getString(R.string.download_state_downloading) + downloadStatus;
    } else if (state == DownloadState.ERROR) {
      text = getString(R.string.download_state_error) + downloadStatus;
    } else if (state == DownloadState.FINISHED) {
      progress = 100;
    } else if (state == DownloadState.PAUSED) {
      text = getString(R.string.download_state_paused) + downloadStatus;
    }
    if (state != DownloadState.FINISHED) {
      episodeState.setText(text);
    }
    setProgress(downloadProgress, downloadProgressText, progress);
  }

  private void setProgress(ProgressBar progressBar, TextView textView,
      int progress) {
    progressBar.setProgress(progress);
    int color = getResources().getColor(android.R.color.tertiary_text_light);
    if (progress == 100) {
      color = getResources().getColor(android.R.color.holo_blue_dark);
    }
    textView.setTextColor(color);
  }

  private void updateButton() {
    Button enqueueButton = (Button) findViewById(R.id.episode_enqueue_button);
    enqueueButton.setVisibility(episode.hasDownload() ? View.VISIBLE : View.GONE);
    if (queue.contains(episode)) {
      enqueueButton.setText(getString(R.string.dequeue));
    } else {
      enqueueButton.setText(getString(R.string.enqueue));
    }
  }
}
