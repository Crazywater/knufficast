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
package de.knufficast.ui.search;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EventBus;
import de.knufficast.events.Listener;
import de.knufficast.events.NewImageEvent;
import de.knufficast.logic.AddFeedTask;
import de.knufficast.search.ITunesSearch;
import de.knufficast.search.PodcastSearch;
import de.knufficast.search.PodcastSearch.Result;
import de.knufficast.ui.main.MainActivity;
import de.knufficast.ui.settings.SettingsActivity;
import de.knufficast.util.BooleanCallback;

public class SearchFeedActivity extends Activity implements
    AddFeedTask.Presenter {
  private SearchView searchView;
  private ProgressBar searchProgress;
  private AddFeedTask addFeedTask;
  private ListView searchResultsList;
  private ProgressDialog progressDialog;

  private final PodcastSearch podcastSearch = new ITunesSearch();
  private final List<Result> searchResults = new ArrayList<Result>();

  private final Listener<NewImageEvent> newImageListener = new Listener<NewImageEvent>() {
    @Override
    public void onEvent(NewImageEvent event) {
      searchResultsAdapter.notifyDataSetChanged();
    }
  };

  private final OnItemClickListener addFeedListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,
        long arg3) {
      // toggle details
      addFeed(searchResults.get(position).getFeedUrl());
    }
  };
  
  private final OnQueryTextListener queryListener = new OnQueryTextListener() {
    @Override
    public boolean onQueryTextChange(String newText) {
      return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
      String input = searchView.getQuery().toString();
      if (!"".equals(input)) {
        if (input.startsWith("http://") || input.startsWith("https://")
            || input.startsWith("www.")) {
          addFeed(input);
        } else {
          searchProgress.setVisibility(View.VISIBLE);
          podcastSearch.search(input, searchCallback);
        }
      }
      return true;
    }
  };

  private SearchResultsAdapter searchResultsAdapter;
  private EventBus eventBus;

  private final BooleanCallback<List<Result>, String> searchCallback = new BooleanCallback<List<Result>, String>() {
    @Override
    public void success(List<Result> a) {
      searchResults.clear();
      searchResults.addAll(a);
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          searchProgress.setVisibility(View.GONE);
          searchResultsAdapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void fail(String error) {
      searchResults.clear();
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          searchProgress.setVisibility(View.GONE);
          searchResultsAdapter.notifyDataSetChanged();
        }
      });
    }
  };
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.activity_feed_search);

    searchProgress = (ProgressBar) findViewById(R.id.add_feed_progress);
    searchResultsList = (ListView) findViewById(R.id.add_feed_search_results);

    searchResultsAdapter = new SearchResultsAdapter(this,
        R.layout.search_result_list_item, searchResults);
    searchResultsList.setAdapter(searchResultsAdapter);

    searchResultsList.setOnItemClickListener(addFeedListener);
  }

  @Override
  public void onStart() {
    super.onStart();

    eventBus = App.get().getEventBus();
    eventBus.addListener(NewImageEvent.class, newImageListener);
  }

  private void addFeed(String url) {
    addFeedTask = new AddFeedTask(this);
    addFeedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
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
    case R.id.menu_settings:
      Intent intent = new Intent(this, SettingsActivity.class);
      startActivity(intent);
      return true;
    default:
      return false;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_feed_search, menu);

    searchView = (SearchView) menu.findItem(R.id.add_feed_search)
        .getActionView();

    Uri uri = getIntent().getData();
    if (uri != null) {
      searchView.setQuery(uri.toString(), true);
    }

    searchView.setOnQueryTextListener(queryListener);
    searchView.setIconifiedByDefault(false);
    searchView.requestFocus();
    return true;
  }

  @Override
  public void onFeedAdded() {
    addFeedTask = null;
    disableProgressDialog();
    finish();
  }

  @Override
  public void onFeedAddError(String error) {
    addFeedTask = null;
    disableProgressDialog();
    new AlertDialog.Builder(this).setTitle(R.string.add_feed_failed)
        .setMessage(error).show();
  }

  @Override
  public void onStartAddingFeed() {
    enableProgressDialog();
  }

  private void enableProgressDialog() {
    String title = getString(R.string.add_feed_progress_title);
    String message = getString(R.string.add_feed_progress_message);
    progressDialog = ProgressDialog.show(this, title, message);
    progressDialog.setCancelable(true);
    progressDialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        addFeedTask.cancel(true);
      }
    });
    // lock orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
  }

  private void disableProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
      progressDialog = null;
    }
    // unlock orientation
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
  }
}
