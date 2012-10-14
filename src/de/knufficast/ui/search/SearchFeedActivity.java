package de.knufficast.ui.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.knufficast.R;
import de.knufficast.logic.AddFeedTask;
import de.knufficast.ui.main.MainActivity;
import de.knufficast.ui.settings.SettingsActivity;

public class SearchFeedActivity extends Activity implements
    AddFeedTask.Presenter {
  private TextView addText;
  private Button addButton;
  private ProgressBar addProgress;
  private AddFeedTask addFeedTask;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.activity_feed_search);

    addButton = (Button) findViewById(R.id.add_feed_button);
    addProgress = (ProgressBar) findViewById(R.id.add_feed_progress);
    addText = (TextView) findViewById(R.id.add_feed_text);
  }

  @Override
  public void onStart() {
    super.onStart();

    Uri uri = getIntent().getData();
    if (uri != null) {
      addText.setText(uri.toString());
    }

    addButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View unused) {
        String input = addText.getText().toString();
        if (!"".equals(input)) {
          addFeedTask = new AddFeedTask(SearchFeedActivity.this);
          addFeedTask.execute(input);
        }
      }
    });
    updateAddButtonVisibility();
  }

  @Override
  public void onStop() {
    super.onStop();
    if (addFeedTask != null) {
      addFeedTask.cancel(true);
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
    addButton.setVisibility(adding ? View.GONE : View.VISIBLE);
  }

  @Override
  public void onFeedAdded() {
    addFeedTask = null;
    addText.setText("");
    updateAddButtonVisibility();
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