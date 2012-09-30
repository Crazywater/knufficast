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
package de.knufficast.ui.feed;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.logic.ImageCache;
import de.knufficast.logic.model.Episode;

/**
 * Adapter that transforms episodes into a list.
 * 
 * @author crazywater
 * 
 */
public class EpisodesAdapter extends ArrayAdapter<Episode> {
  private final Context context;
  private final int layoutResourceId;
  private final List<Episode> data;
  private final Presenter presenter;
  private final ImageCache imageCache;

  public EpisodesAdapter(Context context, int layoutResourceId,
      List<Episode> data, Presenter presenter) {
    super(context, layoutResourceId, data);
    this.layoutResourceId = layoutResourceId;
    this.context = context;
    this.data = data;
    this.presenter = presenter;

    imageCache = App.get().getImageCache();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;

    if (row == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      row = inflater.inflate(layoutResourceId, parent, false);
    }

    final Episode episode = data.get(position);

    row.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        presenter.episodeClicked(episode);
      }
    });

    TextView textView = (TextView) row.findViewById(R.id.episode_list_title);
    textView.setText(episode.getTitle());
    ImageView imageView = (ImageView) row.findViewById(R.id.episode_list_icon);
    imageView.setImageDrawable(imageCache.getResource(episode.getImgUrl()));
    return row;
  }

  /**
   * Presenter interface of this adapter.
   * 
   * @author crazywater
   */
  public interface Presenter {
    void episodeClicked(Episode episode);
  }
}
