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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.NewImageEvent;
import de.knufficast.logic.model.DBFeed;
import de.knufficast.ui.BaseFragment;

/**
 * Fragment in the main window that displays the "feeds" tab.
 * 
 * @author crazywater
 * 
 */
public class FeedsFragment extends BaseFragment {
  private Presenter presenter;
  private EventBus eventBus;
  private FeedsAdapter feedsAdapter;
  private ListView feedsList;

  private List<DBFeed> feeds = new ArrayList<DBFeed>();

  private Listener<NewImageEvent> newImageListener = new Listener<NewImageEvent>() {
    @Override
    public void onEvent(NewImageEvent event) {
      refreshFeeds();
    }
  };

  @Override
  public int getLayoutId() {
    return R.layout.fragment_feeds;
  }

  @Override
  public int getTitleId() {
    return R.string.title_feeds;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    feedsAdapter = new FeedsAdapter(getContext(), R.layout.feed_list_item,
        feeds);

    eventBus = App.get().getEventBus();
  }


  @Override
  public void onStart() {
    super.onStart();
    refreshFeeds();

    feedsList = findView(R.id.feeds_list_view);

    feedsList.setAdapter(feedsAdapter);
    feedsList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
        presenter.feedClicked(feeds.get(position));
      }
    });

    eventBus.addListener(NewImageEvent.class, newImageListener);
  }

  @Override
  public void onStop() {
    super.onStop();
    eventBus.removeListener(NewImageEvent.class, newImageListener);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      presenter = (Presenter) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement "
          + this.getClass().getName() + ".Presenter");
    }
  }

  /**
   * Refreshes the displayed feeds.
   */
  public void refreshFeeds() {
    feeds.clear();
    feeds.addAll(App.get().getConfiguration().getAllFeeds());
    feedsAdapter.notifyDataSetChanged();
  }

  public interface Presenter {
    void feedClicked(DBFeed feed);
  }
}
