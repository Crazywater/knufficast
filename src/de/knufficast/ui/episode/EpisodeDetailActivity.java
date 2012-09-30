package de.knufficast.ui.episode;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EpisodeDownloadProgressEvent;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
import de.knufficast.logic.model.Episode.PlayState;
import de.knufficast.logic.model.Feed;
import de.knufficast.logic.model.Queue;
import de.knufficast.ui.feed.FeedDetailActivity;
import de.knufficast.ui.main.MainActivity;
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

  private WebView description;
  private TextView title;
  private TextView downloadState;
  private ImageView icon;

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
    downloadState = (TextView) findViewById(R.id.episode_download_state);

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
  }

  @Override
  public void onStop() {
    super.onStop();
    eventBus.removeListener(EpisodeDownloadStateEvent.class,
        downloadStateListener);
    eventBus.removeListener(EpisodeDownloadProgressEvent.class,
        downloadProgressListener);
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
    boolean listened = episode.getSeekLocation() > 0;
    menu.setGroupEnabled(R.id.menugroup_downloaded, downloaded);
    menu.setGroupEnabled(R.id.menugroup_listened, listened);
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
    case R.id.menu_delete_download:
      new QueueDownloader(getApplicationContext()).deleteDownload(episode);
      updateDownloadState();
      return true;
    case R.id.menu_set_unlistened:
      episode.setSeekLocation(0);
      episode.setPlayState(PlayState.NONE);
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
    updateButton();
  }

  private String toMegaBytes(long bytes) {
    double result = (double) bytes / (1000 * 1000);
    DecimalFormat df = new DecimalFormat("#.##");
    return df.format(result);
  }

  private void updateDownloadState() {
    DownloadState state = episode.getDownloadState();
    String text = "";
    String downloadStatus = " (" + toMegaBytes(episode.getDownloadedBytes())
        + "/" + toMegaBytes(episode.getTotalBytes()) + " MB)";
    if (!episode.hasDownload()) {
      text = getString(R.string.download_state_no_download);
    } else if (state == DownloadState.NONE) {
      text = getString(R.string.download_state_none);
    } else if (state == DownloadState.DOWNLOADING) {
      text = getString(R.string.download_state_downloading) + downloadStatus;
    } else if (state == DownloadState.ERROR) {
      text = getString(R.string.download_state_error) + downloadStatus;
    } else if (state == DownloadState.FINISHED) {
      text = getString(R.string.download_state_finished);
    } else if (state == DownloadState.PAUSED) {
      text = getString(R.string.download_state_paused) + downloadStatus;
    }
    downloadState.setText(text);
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
