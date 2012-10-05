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
package de.knufficast.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import de.knufficast.flattr.FlattrApi;
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

    // get the intent
    String scheme = getIntent().getScheme();
    if ("knufficastoauth".equals(scheme)) {
      String data = getIntent().getDataString();
      FlattrApi api = new FlattrApi();
      api.setOauthResponse(data);
    }
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
