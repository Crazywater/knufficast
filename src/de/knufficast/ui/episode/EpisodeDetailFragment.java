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

import android.R.color;
import android.os.Bundle;
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
import de.knufficast.logic.model.DBEpisode;
import de.knufficast.logic.model.DBEpisode.DownloadState;
import de.knufficast.logic.model.DBEpisode.FlattrState;
import de.knufficast.logic.model.DBEpisode.PlayState;
import de.knufficast.logic.model.DBFeed;
import de.knufficast.logic.model.Queue;
import de.knufficast.ui.BaseFragment;
import de.knufficast.util.TimeUtil;

/**
 * A fragment to show detailed information about a single episode.
 * 
 * @author crazywater
 */
public class EpisodeDetailFragment extends BaseFragment {
  private static final String EPISODE_ID = "EpisodeID";
  private DBEpisode episode;
  private DBFeed feed;
  private EventBus eventBus;

  private Queue queue;

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
  private Button enqueueButton;

  private Listener<EpisodeDownloadProgressEvent> downloadProgressListener = new Listener<EpisodeDownloadProgressEvent>() {
    @Override
    public void onEvent(EpisodeDownloadProgressEvent event) {
      if (event.getIdentifier() == episode.getId()) {
        updateDownloadState();
      }
    }
  };
  private Listener<EpisodeDownloadStateEvent> downloadStateListener = new Listener<EpisodeDownloadStateEvent>() {
    @Override
    public void onEvent(EpisodeDownloadStateEvent event) {
      if (event.getIdentifier() == episode.getId()) {
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
  public void onStart() {
    super.onStart();
    enqueueButton = findView(R.id.episode_enqueue_button);
    title = findView(R.id.episode_title_text);
    description = findView(R.id.episode_description_text);
    icon = findView(R.id.episode_icon);
    episodeState = findView(R.id.episode_state);
    downloadProgress = findView(R.id.episode_download_progress);
    listeningProgress = findView(R.id.episode_listening_progress);
    flattringProgress = findView(R.id.episode_flattring_progress);
    downloadProgressText = findView(R.id.episode_download_progress_text);
    listeningProgressText = findView(R.id.episode_listening_progress_text);
    flattringProgressText = findView(R.id.episode_flattring_progress_text);
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

    eventBus = App.get().getEventBus();

    eventBus
        .addListener(EpisodeDownloadStateEvent.class, downloadStateListener);
    eventBus.addListener(EpisodeDownloadProgressEvent.class,
        downloadProgressListener);
    eventBus.addListener(PlayerProgressEvent.class, playerProgressListener);
    eventBus.addListener(FlattrStateEvent.class, flattrStateListener);

    description.setBackgroundColor(color.background_light);
    updateState();
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      episode = new DBEpisode(savedInstanceState.getLong(EPISODE_ID));
    }
    feed = episode.getFeed();
    queue = App.get().getQueue();
  }

  public void setEpisode(DBEpisode episode) {
    this.episode = episode;
  }

  @Override
  public void onSaveInstanceState(Bundle bundle) {
    bundle.putLong(EPISODE_ID, episode.getId());
  }

  private void updateState() {
    title.setText(episode.getTitle());
    String contentType = feed.getEncoding() == null ? "text/html"
        : "text/html; charset=" + feed.getEncoding();
    String content = episode.getContent();
    if ("".equals(content)) {
      content = episode.getDescription();
    }
    description.loadData(content, contentType, feed.getEncoding());
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

  public void updatePlayingState(int played, int duration) {
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
    } else if (state == PlayState.FINISHED) {
      progress = 100;
    }
    if (episode.getDownloadState() == DownloadState.FINISHED && state != PlayState.FINISHED) {
      episodeState.setText(text);
    }
    setProgress(listeningProgress, listeningProgressText, progress);
  }

  public void updateFlattringState() {
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

  public void updateDownloadState() {
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
    enqueueButton.setVisibility(episode.hasDownload() ? View.VISIBLE : View.GONE);
    if (queue.contains(episode)) {
      enqueueButton.setText(getString(R.string.dequeue));
    } else {
      enqueueButton.setText(getString(R.string.enqueue));
    }
  }

  @Override
  public int getTitleId() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_episode_detail;
  }
}
