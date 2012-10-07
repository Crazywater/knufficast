package de.knufficast.logic.model;

import java.util.ArrayList;
import java.util.List;

public class XMLFeed {
  private String dataUrl = "";
  private String title = "";
  private String description = "";
  private String encoding = "";
  private long lastUpdated = 0;
  private String eTag = "";
  private String imgUrl = "";

  private final List<XMLEpisode> episodes = new ArrayList<XMLEpisode>();

  public String getDataUrl() {
    return dataUrl;
  }

  public void setDataUrl(String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public long getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(long lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(String eTag) {
    this.eTag = eTag;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public void addEpisode(XMLEpisode episode) {
    episodes.add(episode);
  }

  public List<XMLEpisode> getEpisodes() {
    return episodes;
  }
}
