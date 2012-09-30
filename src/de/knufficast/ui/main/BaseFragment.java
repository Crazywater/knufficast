package de.knufficast.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Base fragment class for the two fragments in the main view (
 * {@link FeedsFragment} and {@link QueueFragment}.
 * 
 * @author crazywater
 * 
 */
public abstract class BaseFragment extends Fragment {
  public abstract int getTitleId();
  protected abstract int getLayoutId();
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(getLayoutId(), container, false);
  }

  @SuppressWarnings("unchecked")
  protected <T> T findView(int id) {
    return (T) getView().findViewById(id);
  }

  protected Context getContext() {
    return getActivity();
  }
}
