package de.knufficast.ui.search;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.gpodder.GPodderSearch.Result;

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
    title.setText(result.getTitle());
    TextView description = (TextView) row
        .findViewById(R.id.search_result_description);
    description.setText(result.getDescription());
    TextView website = (TextView) row.findViewById(R.id.search_result_website);
    website.setText(result.getWebsite());

    ImageView icon = (ImageView) row.findViewById(R.id.search_result_list_icon);
    icon.setImageDrawable(App.get().getImageCache()
        .getResource(result.getImgUrl()));
    return row;
  }
}
