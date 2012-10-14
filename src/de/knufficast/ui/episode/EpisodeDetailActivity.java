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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.flattr.FlattrApi;
import de.knufficast.logic.model.DBEpisode;
import de.knufficast.logic.model.DBEpisode.FlattrState;
import de.knufficast.logic.model.DBFeed;
import de.knufficast.logic.model.Database;
import de.knufficast.logic.model.SQLiteHelper;
import de.knufficast.ui.main.MainActivity;
import de.knufficast.ui.settings.SettingsActivity;
import de.knufficast.util.BooleanCallback;
import de.knufficast.util.NetUtil;
import de.knufficast.watchers.QueueDownloader;

/**
 * An activity that displays details about episodes, either in a feed or the
 * queue. The user can flick through the different episodes.
 * 
 * @author crazywater
 */
public class EpisodeDetailActivity extends FragmentActivity {
  // The episode that should be the current one upon invoking this activity.
  public static final String EPISODE_ID_INTENT = "episodeIdIntent";
  // Request paging through the queue instead of paging through the feed.
  public static final String REQUEST_QUEUE_PAGING_INTENT = "queuePagingIntent";

  private Database db;
  private DBEpisode currentEpisode;
  private ViewPager viewPager;
  private EpisodesPagerAdapter sectionsPagerAdapter;

  private final List<DBEpisode> episodes = new ArrayList<DBEpisode>();

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_episode_detail, menu);
    return true;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_episode_detail);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    db = App.get().getDB();

    Long episodeId = getIntent().getExtras().getLong(EPISODE_ID_INTENT);
    boolean queuePaging = getIntent().getExtras().getBoolean(
        REQUEST_QUEUE_PAGING_INTENT);
    episodes.clear();
    currentEpisode = new DBEpisode(episodeId);
    if (queuePaging) {
      // if we have queue paging, set the episodes according to the queue
      episodes.addAll(App.get().getQueue().asList());
    } else {
      // we have feed paging, page through the feed
      List<Long> ids = db.query(SQLiteHelper.TABLE_EPISODES,
          SQLiteHelper.C_EP_FEED_ID,
          String.valueOf(currentEpisode.getFeed().getId()));
      for (long id : ids) {
        episodes.add(new DBEpisode(id));
      }
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    viewPager = (ViewPager) findViewById(R.id.episode_detail_pager);
    sectionsPagerAdapter = new EpisodesPagerAdapter(
        getSupportFragmentManager(), currentEpisode.getFeed());

    viewPager.setAdapter(sectionsPagerAdapter);
    viewPager.setCurrentItem(episodes.indexOf(currentEpisode));
    viewPager
        .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
            currentEpisode = episodes.get(position);
            requestFlattrUpdate();
          }
        });
  }

  private void requestFlattrUpdate() {
    if (currentEpisode.hasFlattr()) {
      NetUtil netUtil = new NetUtil(this);
      if (netUtil.isOnline()) {
        // check if the flattr state has changed
        FlattrApi flattrApi = new FlattrApi();
        final DBEpisode episode = currentEpisode;
        flattrApi.isFlattred(episode.getFlattrUrl(),
            new BooleanCallback<Boolean, String>() {
              @Override
              public void success(Boolean flattred) {
                if (episode.getFlattrState() != FlattrState.ENQUEUED
                    || flattred) {
                  episode.setFlattrState(flattred ? FlattrState.FLATTRED
                      : FlattrState.NONE);
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
      currentEpisode.setFlattrState(FlattrState.ENQUEUED);
      App.get().getFlattrQueue().enqueue(currentEpisode);
      return true;
    case R.id.menu_settings:
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    case R.id.menu_delete_download:
      QueueDownloader.get().deleteDownload(currentEpisode);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public class EpisodesPagerAdapter extends FragmentPagerAdapter {
    public EpisodesPagerAdapter(FragmentManager fm, DBFeed feed) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {
      EpisodeDetailFragment fragment = new EpisodeDetailFragment();
      fragment.setEpisode(episodes.get(i));
      return fragment;
    }

    @Override
    public int getCount() {
      return episodes.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return episodes.get(position).getTitle();
    }
  }
}
