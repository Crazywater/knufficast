package de.knufficast.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import de.knufficast.ui.main.MainActivity;

/**
 * An activity that represents the "Settings" screen.
 * 
 * @author crazywater
 * 
 */
public class SettingsActivity extends PreferenceActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    // display the fragment
    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment()).commit();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      // This is called when the Home (Up) button is pressed
      // in the Action Bar.
      Intent parentActivityIntent = new Intent(this, MainActivity.class);
      parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
          | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(parentActivityIntent);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
