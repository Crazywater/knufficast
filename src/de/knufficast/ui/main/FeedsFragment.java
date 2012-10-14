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
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.NewImageEvent;
import de.knufficast.logic.AddFeedTask;
import de.knufficast.logic.model.DBFeed;
import de.knufficast.ui.BaseFragment;

/**
 * Fragment in the main window that displays the "feeds" tab.
 * 
 * @author crazywater
 * 
 */
public class FeedsFragment extends BaseFragment implements
    AddFeedTask.Presenter {
  private Presenter presenter;
  private EventBus eventBus;
  private FeedsAdapter feedsAdapter;
  private AddFeedTask addFeedTask;
  private TextView addText;
  private Button addButton;
  private ProgressBar addProgress;
  private ListView feedsList;

  private List<DBFeed> feeds = new ArrayList<DBFeed>();

  private CharSequence feedText;

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

  private void updateAddButtonVisibility() {
    boolean adding = addFeedTask != null;
    addProgress.setVisibility(adding ? View.VISIBLE : View.GONE);
    addButton.setVisibility(adding ? View.GONE : View.VISIBLE);
  }

  @Override
  public void onStart() {
    super.onStart();
    refreshFeeds();

    addButton = findView(R.id.add_feed_button);
    addProgress = findView(R.id.add_feed_progress);
    addText = findView(R.id.add_feed_text);
    feedsList = findView(R.id.feeds_list_view);

    if (feedText != null) {
      addText.setText(feedText);
    }
    addButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View unused) {
        String input = addText.getText().toString();
        if (!"".equals(input)) {
          addFeedTask = new AddFeedTask(FeedsFragment.this);
          addFeedTask.execute(input);
        }
      }
    });

    feedsList.setAdapter(feedsAdapter);
    feedsList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
        presenter.feedClicked(feeds.get(position));
      }
    });

    updateAddButtonVisibility();

    eventBus.addListener(NewImageEvent.class, newImageListener);
  }

  @Override
  public void onStop() {
    super.onStop();
    if (addFeedTask != null) {
      addFeedTask.cancel(true);
    }
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

  @Override
  public void onFeedAdded() {
    addFeedTask = null;
    addText.setText("");
    updateAddButtonVisibility();
    refreshFeeds();
  }

  @Override
  public void onFeedAddError(String error) {
    addFeedTask = null;
    updateAddButtonVisibility();
    new AlertDialog.Builder(getContext()).setTitle(R.string.add_feed_failed)
        .setMessage(error).show();
  }

  @Override
  public void onStartAddingFeed() {
    feedText = null;
    updateAddButtonVisibility();
  }

  /**
   * Enters a predefined text into the "add feed" text box.
   * 
   * @param text
   */
  public void prepareForFeedText(CharSequence text) {
    feedText = text;
  }

  public interface Presenter {
    void feedClicked(DBFeed feed);
  }
}
