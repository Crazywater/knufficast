package de.knufficast.search;

import java.util.List;

import de.knufficast.util.BooleanCallback;

public interface PodcastSearch {
  public interface Result {
    public String getTitle();
    public String getDescription();
    public String getFeedUrl();
    public String getImgUrl();
    public String getWebsite();
  };

  public void search(final String query,
      final BooleanCallback<List<Result>, String> callback);
}
