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
package de.knufficast.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.knufficast.ui.main.FeedsFragment;
import de.knufficast.ui.main.QueueFragment;

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

  protected void runOnUiThread(Runnable r) {
    getActivity().runOnUiThread(r);
  }
}
