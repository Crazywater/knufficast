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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.events.EventBus;
import de.knufficast.events.FlattrStatusEvent;
import de.knufficast.events.Listener;
import de.knufficast.logic.FlattrConfiguration;
import de.knufficast.logic.FlattrConfiguration.FlattrStatus;
import de.knufficast.watchers.UpdaterService;

/**
 * The fragment displayed inside the settings activity.
 * 
 * @author crazywater
 * 
 */
public class SettingsFragment extends PreferenceFragment {
  private ListPreference updateFreqPreference;
  private Preference flattrPreference;
  private EventBus eventBus;
  private static final String KEY_UPDATE_FREQ = "pref_key_update_freq";
  private final Listener<FlattrStatusEvent> flattrStatusListener = new Listener<FlattrStatusEvent>() {
    @Override
    public void onEvent(final FlattrStatusEvent event) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          updateFlattrPref(event.getStatus());
        }
      });
    }
  };

  /**
   * Listener that is used to change the descriptive text of preferences upon
   * changes by the user.
   */
  private final OnSharedPreferenceChangeListener changeListener = new OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
        String key) {
      if (key.equals(KEY_UPDATE_FREQ)) {
        UpdaterService.init();
        updateFreqPref();
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings_fragment);
    updateFreqPreference = (ListPreference) findPreference("pref_key_update_freq");
    flattrPreference = findPreference("pref_key_flattr_intent");
    updateFreqPref();
    updateFlattrPref(App.get().getConfiguration().getFlattrConfig().getStatus());
  }

  private void updateFreqPref() {
    updateFreqPreference.setSummary(getString(
        R.string.pref_summary_update_freq, updateFreqPreference.getEntry()));
  }

  private void updateFlattrPref(FlattrConfiguration.FlattrStatus status) {
    if (status == FlattrStatus.NOT_AUTHENTICATED) {
      flattrPreference.setSummary(R.string.flattr_auth_error);
    } else if (status == FlattrStatus.AUTHENTICATED) {
      flattrPreference.setSummary(R.string.flattr_no_error);
    } else if (status == FlattrStatus.AUTHENTICATING) {
      flattrPreference.setSummary(R.string.flattr_authenticating);
    } else if (status == FlattrStatus.ERROR) {
      flattrPreference.setSummary(R.string.flattr_auth_error);
    } else if (status == FlattrStatus.NO_MEANS) {
      flattrPreference.setSummary(R.string.flattr_no_means);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    eventBus = App.get().getEventBus();
    App.get().getConfiguration().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(changeListener);
    eventBus.addListener(FlattrStatusEvent.class, flattrStatusListener);
  }

  @Override
  public void onStop() {
    super.onStop();
    App.get().getConfiguration().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(changeListener);
    eventBus.removeListener(FlattrStatusEvent.class, flattrStatusListener);
  }
}
