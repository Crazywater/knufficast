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
package de.knufficast.ui.search;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.search.PodcastSearch.Result;

public class SearchResultsAdapter extends ArrayAdapter<Result> {
  private final List<Result> data;
  private final int layoutResourceId;
  private final Context context;

  public SearchResultsAdapter(Context context, int layoutResourceId,
      List<Result> data) {
    super(context, layoutResourceId, data);
    this.layoutResourceId = layoutResourceId;
    this.data = data;
    this.context = context;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;

    if (row == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      row = inflater.inflate(layoutResourceId, parent, false);
    }

    Result result = data.get(position);
    TextView title = (TextView) row.findViewById(R.id.search_result_list_title);
    title.setText(Html.fromHtml("<a href='"
        + TextUtils.htmlEncode(result.getWebsite()) + "'>"
        + TextUtils.htmlEncode(result.getTitle()) + "</a>"));
    // weird hacks to make links clickable, but also the rest for subscribing
    title.setMovementMethod(LinkMovementMethod.getInstance());
    title.setFocusable(false);
    title.setFocusableInTouchMode(false);
    TextView description = (TextView) row
        .findViewById(R.id.search_result_description);
    description.setText(result.getDescription());
    TextView feedUrl = (TextView) row.findViewById(R.id.search_result_feed_url);
    feedUrl.setText(context.getString(R.string.search_result_feed_url,
        result.getFeedUrl()));

    ImageView icon = (ImageView) row.findViewById(R.id.search_result_list_icon);
    icon.setImageDrawable(App.get().getImageCache()
        .getResource(result.getImgUrl()));

    return row;
  }
}
