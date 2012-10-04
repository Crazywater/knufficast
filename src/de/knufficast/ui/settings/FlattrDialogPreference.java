package de.knufficast.ui.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class FlattrDialogPreference extends DialogPreference {

  public FlattrDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);
  }
}
