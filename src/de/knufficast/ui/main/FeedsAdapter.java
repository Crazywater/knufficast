package de.knufficast.ui.main;

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
import de.knufficast.logic.model.Feed;

/**
 * An adapter for displaying {@link Feed}s in a ListView.
 * 
 * @author crazywater
 * 
 */
public class FeedsAdapter extends ArrayAdapter<Feed> {
  private final Context context;
  private final int layoutResourceId;
  private final List<Feed> data;
  private final Presenter presenter;
  private final ImageCache imageCache;

  public FeedsAdapter(Context context, int layoutResourceId, List<Feed> data,
      Presenter presenter) {
    super(context, layoutResourceId, data);
    this.layoutResourceId = layoutResourceId;
    this.context = context;
    this.data = data;
    this.presenter = presenter;

    this.imageCache = App.get().getImageCache();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;

    if (row == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      row = inflater.inflate(layoutResourceId, parent, false);
    }

    final Feed feed = data.get(position);

    row.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        presenter.feedClicked(feed);
      }
    });

    TextView textView = (TextView) row.findViewById(R.id.feed_list_title);
    textView.setText(feed.getTitle());
    ImageView imageView = (ImageView) row.findViewById(R.id.feed_list_icon);
    imageView.setImageDrawable(imageCache.getResource(feed.getImgUrl()));
    return row;
  }

  /**
   * Presenter interface for the {@link FeedsAdapter}.
   * 
   * @author crazywater
   * 
   */
  public interface Presenter {
    void feedClicked(Feed feed);
  }
}
