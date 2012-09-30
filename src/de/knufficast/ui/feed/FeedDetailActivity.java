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
package de.knufficast.ui.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Feed;
import de.knufficast.ui.episode.EpisodeDetailActivity;
import de.knufficast.ui.main.MainActivity;

/**
 * Activity that displays the details of a {@link Feed}, including the list of
 * episodes.
 * 
 * @author crazywater
 * 
 */
public class FeedDetailActivity extends Activity implements
    EpisodesAdapter.Presenter {
  public static final String FEED_URL_INTENT = "feedUrlIntent";

  private boolean descriptionVisible = false;
  private String feedUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.activity_feed_detail);

    LinearLayout titleBar = (LinearLayout) findViewById(R.id.feed_title_bar);
    titleBar.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleDescription();
      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();
    String feedUrl = getIntent().getExtras().getString(FEED_URL_INTENT);
    Feed feed = App.get().getConfiguration().getFeed(feedUrl);
    setFeed(feed);
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
    case R.id.menu_unsubscribe_feed:
      // fire "are you sure" dialog
      new AlertDialog.Builder(this)
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setTitle(R.string.delete_feed_title)
          .setMessage(R.string.delete_feed_message)
          .setPositiveButton(R.string.delete_feed_confirm,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  App.get().getConfiguration().deleteFeed(feedUrl);
                  finish();
                }
              }).setNegativeButton(R.string.delete_feed_cancel, null).show();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_feed_detail, menu);
    return true;
  }

  private void setFeed(Feed feed) {
    feedUrl = feed.getFeedUrl();
    TextView descriptionText = (TextView) findViewById(R.id.feed_description_text);
    descriptionText.setText(feed.getDescription());
    TextView titleText = (TextView) findViewById(R.id.feed_title_text);
    titleText.setText(feed.getTitle());
    ImageView feedIcon = (ImageView) findViewById(R.id.feed_icon);
    feedIcon.setImageDrawable(App.get().getImageCache()
        .getResource(feed.getImgUrl()));
    ListView episodeList = (ListView) findViewById(R.id.feed_episode_list);
    episodeList.setAdapter(new EpisodesAdapter(this,
        R.layout.episode_list_item, feed.getEpisodes(), this));
  }

  private void toggleDescription() {
    View description = findViewById(R.id.feed_description_scrollview);
    descriptionVisible = !descriptionVisible;
    description.setVisibility(descriptionVisible ? View.VISIBLE : View.GONE);
  }

  @Override
  public void episodeClicked(Episode episode) {
    Intent intent = new Intent(this, EpisodeDetailActivity.class);
    intent.putExtra(FEED_URL_INTENT, feedUrl);
    intent.putExtra(EpisodeDetailActivity.EPISODE_GUID_INTENT,
        episode.getGuid());
    startActivity(intent);
  }
}
