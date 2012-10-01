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
package de.knufficast.ui.main;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EpisodeDownloadProgressEvent;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.PlayerProgressEvent;
import de.knufficast.events.PlayerStateChangeEvent;
import de.knufficast.events.QueueChangedEvent;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Queue;
import de.knufficast.ui.DnDListView;

/**
 * Fragment for the "queue" tab in the main window.
 * 
 * @author crazywater
 * 
 */
public class QueueFragment extends BaseFragment implements
    QueueAdapter.Presenter {
  private List<Episode> ourQueue = new ArrayList<Episode>();
  private QueueAdapter episodesAdapter;
  private Presenter presenter;
  private EventBus eventBus;

  private SeekBar seekBar;
  private DnDListView list;
  private Button playButton;
  private TextView elapsedTime;
  private TextView totalTime;

  private final Listener<QueueChangedEvent> queueUpdateListener = new Listener<QueueChangedEvent>() {
    @Override
    public void onEvent(QueueChangedEvent event) {
      redrawQueue();
    }
  };
  private final Listener<PlayerStateChangeEvent> playerStateListener = new Listener<PlayerStateChangeEvent>() {
    @Override
    public void onEvent(PlayerStateChangeEvent event) {
      setPlayButtonPlaying(event.isPlaying());
    }
  };
  private final Listener<PlayerProgressEvent> playerProgressListener = new Listener<PlayerProgressEvent>() {
    @Override
    public void onEvent(final PlayerProgressEvent event) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          setControlsEnabled(event.getTotal() > 0);
          setSeekbar(event.getProgress(), event.getTotal());
        }
      });
    }
  };
  private final Listener<EpisodeDownloadStateEvent> episodeDownloadStateListener = new Listener<EpisodeDownloadStateEvent>() {
    @Override
    public void onEvent(EpisodeDownloadStateEvent event) {
      redrawQueue();
    }
  };
  private final Listener<EpisodeDownloadProgressEvent> episodeDownloadProgressListener = new Listener<EpisodeDownloadProgressEvent>() {
    @Override
    public void onEvent(EpisodeDownloadProgressEvent event) {
      for (Episode ep : ourQueue) {
        if (ep.getIdentifier().equals(event.getIdentifier())) {
          redrawQueue();
          break;
        }
      }
    }
  };

  private DnDListView.Listener dndListener = new DnDListView.Listener() {
    @Override
    public void drop(int from, int to) {
      presenter.moveEpisode(ourQueue.get(from), to);
    }

    public void remove(int which) {
      presenter.removeEpisode(ourQueue.get(which));
    }
  };

  @Override
  public int getLayoutId() {
    return R.layout.fragment_queue;
  }

  @Override
  public int getTitleId() {
    return R.string.title_queue;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    episodesAdapter = new QueueAdapter(getContext(),
        R.layout.queue_list_item, ourQueue, this);
    eventBus = App.get().getEventBus();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    // Android way of getting a reference to the presenter
    try {
      presenter = (Presenter) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement "
          + this.getClass().getName() + ".Presenter");
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    // find views
    playButton = findView(R.id.queue_play_button);
    list = findView(R.id.queue_episode_list);
    seekBar = findView(R.id.queue_seek_bar);
    elapsedTime = findView(R.id.queue_time_elapsed);
    totalTime = findView(R.id.queue_time_total);

    list.setListener(dndListener);

    // add listeners to the eventbus
    eventBus.addListener(QueueChangedEvent.class, queueUpdateListener);
    eventBus.addListener(PlayerStateChangeEvent.class, playerStateListener);
    eventBus.addListener(PlayerProgressEvent.class, playerProgressListener);
    eventBus.addListener(EpisodeDownloadStateEvent.class,
        episodeDownloadStateListener);
    eventBus.addListener(EpisodeDownloadProgressEvent.class,
        episodeDownloadProgressListener);

    // add listeners to user events
    playButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        presenter.playClicked();
      }
    });
    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        if (fromUser) {
          // shortcut for immediate UI-response to update elapsed time
          elapsedTime.setText(formatTime(progress));
          presenter.seekTo(progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // set initial state correctly
    updateQueue();
    setControlsEnabled(false);
    list.setAdapter(episodesAdapter);
    setPlayButtonPlaying(App.get().getPlayer().isPlaying());

    // also used to request a PlayerProgressEvent
    App.get().getPlayer().prepareAsync();
  }

  /**
   * Transforms the button to a pause button (true) or play button (false).
   */
  @SuppressWarnings("deprecation")
  public void setPlayButtonPlaying(boolean playing) {
    int id = playing ? android.R.drawable.ic_media_pause
        : android.R.drawable.ic_media_play;
    // TODO: Transition to setBackground (requires newer API).
    playButton.setBackgroundDrawable(getView().getResources().getDrawable(id));
  }

  /**
   * Moves the seekbar to progress/max and updates the total/elapsed time
   * displays.
   */
  public void setSeekbar(int progress, int max) {
    seekBar.setMax(max);
    seekBar.setProgress(progress);
    elapsedTime.setText(formatTime(progress));
    totalTime.setText(formatTime(max));
  }

  /**
   * Zero-pad integers if <10
   */
  private String pad(int toPad) {
    if (toPad < 10) {
      return "0" + toPad;
    } else {
      return "" + toPad;
    }
  }

  private void redrawQueue() {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        episodesAdapter.notifyDataSetChanged();
      }
    });
  }

  private String formatTime(int milliseconds) {
    int hours = milliseconds / (1000 * 60 * 60);
    milliseconds %= 1000 * 60 * 60;
    int minutes = milliseconds / (1000 * 60);
    milliseconds %= 1000 * 60;
    int seconds = milliseconds / 1000;
    if (hours > 0) {
      return hours + ":" + pad(minutes) + ":" + pad(seconds);
    } else {
      return minutes + ":" + pad(seconds);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    eventBus.removeListener(PlayerStateChangeEvent.class, playerStateListener);
    eventBus.removeListener(QueueChangedEvent.class, queueUpdateListener);
    eventBus.removeListener(PlayerProgressEvent.class, playerProgressListener);
    eventBus.removeListener(EpisodeDownloadStateEvent.class,
        episodeDownloadStateListener);
    eventBus.removeListener(EpisodeDownloadProgressEvent.class,
        episodeDownloadProgressListener);
  }

  /**
   * Re-renders the episodes in the queue.
   */
  private void updateQueue() {
    Queue queue = App.get().getQueue();
    ourQueue.clear();
    ourQueue.addAll(queue.asList());
    episodesAdapter.notifyDataSetChanged();
  }

  /**
   * Disables or enables the controls (play/pause, seekbar)
   */
  private void setControlsEnabled(boolean enabled) {
    playButton.setEnabled(enabled);
    seekBar.setEnabled(enabled);
    int visibility = enabled ? View.VISIBLE : View.GONE;
    elapsedTime.setVisibility(visibility);
    totalTime.setVisibility(visibility);
  }

  @Override
  public void episodeClicked(Episode episode) {
    presenter.episodeClicked(episode);
  }

  /**
   * Presenter interface for the {@link QueueFragment}.
   * 
   * @author crazywater
   * 
   */
  public interface Presenter {
    void episodeClicked(Episode episode);
    void playClicked();
    void seekTo(int progress);
    void moveEpisode(Episode episode, int to);
    void removeEpisode(Episode episode);
  }
}
