package de.knufficast.ui.main;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.knufficast.App;
import de.knufficast.R;
import de.knufficast.logic.Configuration;
import de.knufficast.logic.ImageCache;
import de.knufficast.logic.model.Episode;
import de.knufficast.logic.model.Episode.DownloadState;
import de.knufficast.logic.model.Feed;
import de.knufficast.ui.DnDListView;

/**
 * An adapter to display the {@link Queue} in a {@link DnDListView}.
 * 
 * @author crazywater
 * 
 */
public class QueueAdapter extends ArrayAdapter<Episode> {
  private final Context context;
  private final int layoutResourceId;
  private final List<Episode> data;
  private final Presenter presenter;
  private final ImageCache imageCache;
  private final Configuration configuration;

  private final int normalTextColor;
  private final int secondaryTextColor;

  public QueueAdapter(Context context, int layoutResourceId,
      List<Episode> data, Presenter presenter) {
    super(context, layoutResourceId, data);
    this.layoutResourceId = layoutResourceId;
    this.context = context;
    this.data = data;
    this.presenter = presenter;

    imageCache = App.get().getImageCache();
    configuration = App.get().getConfiguration();

    normalTextColor = resolveAttr(android.R.attr.textColorPrimary);
    secondaryTextColor = resolveAttr(android.R.attr.textColorTertiary);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;

    if (row == null) {
      LayoutInflater inflater = ((Activity) context).getLayoutInflater();
      row = inflater.inflate(layoutResourceId, parent, false);
    }

    // needed for drag and drop
    row.setLongClickable(true);

    final Episode episode = data.get(position);

    row.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        presenter.episodeClicked(episode);
      }
    });

    TextView episodeTitle = (TextView) row
        .findViewById(R.id.queue_episode_list_episode_title);
    episodeTitle.setText(episode.getTitle());
    int textColor = episode.getDownloadState() == DownloadState.FINISHED ? normalTextColor
        : secondaryTextColor;
    episodeTitle.setTextColor(textColor);

    Feed feed = configuration.getFeed(episode.getFeedUrl());
    TextView feedTitle = (TextView) row
        .findViewById(R.id.queue_episode_list_feed_title);
    feedTitle.setText(feed.getTitle());

    ImageView imageView = (ImageView) row
        .findViewById(R.id.queue_episode_list_icon);
    Drawable episodeIcon = imageCache.getResource(episode.getImgUrl());
    if (episodeIcon == imageCache.getDefaultIcon()) {
      episodeIcon = imageCache.getResource(feed.getImgUrl());
    }
    imageView.setImageDrawable(episodeIcon);
    return row;
  }

  /**
   * Resolve a color attribute.
   */
  private int resolveAttr(int resource) {
    TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(resource, value, true);
    return context.getResources().getColor(value.resourceId);
  }

  /**
   * Presenter interface for this adapter.
   * 
   * @author crazywater
   * 
   */
  public interface Presenter {
    void episodeClicked(Episode episode);
  }
}
