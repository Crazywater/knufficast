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
