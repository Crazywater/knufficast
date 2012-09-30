package de.knufficast.ui.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.watchers.UpdaterService;

/**
 * The fragment displayed inside the settings activity.
 * 
 * @author crazywater
 * 
 */
public class SettingsFragment extends PreferenceFragment {
  private ListPreference updateFreqPreference;
  private static final String KEY_UPDATE_FREQ = "pref_key_update_freq";

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

    updateFreqPreference = (ListPreference) getPreferenceScreen()
        .findPreference("pref_key_update_freq");
    updateFreqPref();
  }

  private void updateFreqPref() {
    updateFreqPreference.setSummary(getString(
        R.string.pref_summary_update_freq, updateFreqPreference.getEntry()));
  }

  @Override
  public void onStart() {
    super.onStart();
    App.get().getConfiguration().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(changeListener);
  }

  @Override
  public void onStop() {
    super.onStop();
    App.get().getConfiguration().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(changeListener);
  }
}
