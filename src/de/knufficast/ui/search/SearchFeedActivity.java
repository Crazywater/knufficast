package de.knufficast.ui.search;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import de.knufficast.gpodder.GPodderSearch;
import de.knufficast.gpodder.GPodderSearch.Result;
import de.knufficast.logic.AddFeedTask;
import de.knufficast.ui.main.MainActivity;
import de.knufficast.ui.settings.SettingsActivity;
import de.knufficast.util.BooleanCallback;

public class SearchFeedActivity extends Activity implements
    AddFeedTask.Presenter {
  private SearchView searchView;
  private ProgressBar addProgress;
  private AddFeedTask addFeedTask;
  private ListView searchResultsList;

  private final GPodderSearch gPodderSearch = new GPodderSearch();
  private final List<Result> searchResults = new ArrayList<Result>();

  private final Listener<NewImageEvent> newImageListener = new Listener<NewImageEvent>() {
    @Override
    public void onEvent(NewImageEvent event) {
      searchResultsAdapter.notifyDataSetChanged();
    }
  };

  private SearchResultsAdapter searchResultsAdapter;
  private EventBus eventBus;

  private final BooleanCallback<List<Result>, String> searchCallback = new BooleanCallback<List<Result>, String>() {
    @Override
    public void success(List<Result> a) {
      Log.d("SearchFeedActivity", "GPodder success callback: " + a.size());
      searchResults.clear();
      searchResults.addAll(a);
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          searchResultsAdapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void fail(String error) {
      Log.d("SearchFeedActivity", "GPodder error callback: " + error);
      searchResults.clear();
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
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

    addProgress = (ProgressBar) findViewById(R.id.add_feed_progress);
    searchView = (SearchView) findViewById(R.id.add_feed_search);
    searchResultsList = (ListView) findViewById(R.id.add_feed_search_results);

    searchResultsAdapter = new SearchResultsAdapter(this,
        R.layout.search_result_list_item, searchResults);
    searchResultsList.setAdapter(searchResultsAdapter);

    searchResultsList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0, View view, int arg2,
          long arg3) {
        // toggle details
        View details = view.findViewById(R.id.search_result_details);
        if (details.getVisibility() == View.VISIBLE) {
          details.setVisibility(View.GONE);
        } else {
          details.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();

    Uri uri = getIntent().getData();
    if (uri != null) {
      searchView.setQuery(uri.toString(), true);
    }

    searchView.setOnQueryTextListener(new OnQueryTextListener() {
      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }

      @Override
      public boolean onQueryTextSubmit(String query) {
        String input = searchView.getQuery().toString();
        if (!"".equals(input)) {
          if (input.startsWith("http://") || input.startsWith("https://")
              || input.startsWith("www")) {
            addFeed(input);
          } else {
            Log.d("SearchFeedActivity", "gpodder search for " + input);
            gPodderSearch.search(input, searchCallback);
          }
        }
        return true;
      }
    });
    updateAddButtonVisibility();

    eventBus = App.get().getEventBus();
    eventBus.addListener(NewImageEvent.class, newImageListener);
  }

  private void addFeed(String url) {
    addFeedTask = new AddFeedTask(this);
    addFeedTask.execute(url);
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
    return true;
  }

  private void updateAddButtonVisibility() {
    boolean adding = addFeedTask != null;
    addProgress.setVisibility(adding ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onFeedAdded() {
    addFeedTask = null;
    finish();
  }

  @Override
  public void onFeedAddError(String error) {
    addFeedTask = null;
    updateAddButtonVisibility();
    new AlertDialog.Builder(this).setTitle(R.string.add_feed_failed)
        .setMessage(error).show();
  }

  @Override
  public void onStartAddingFeed() {
    updateAddButtonVisibility();
  }
}