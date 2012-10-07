package de.knufficast.logic.model;

public class XMLEpisode {
  private String title = "";
  private String description = "";
  private String guid = "";
  private String dataUrl = "";
  private String imgUrl = "";
  private String flattrUrl = "";

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

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public String getDataUrl() {
    return dataUrl;
  }

  public void setDataUrl(String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public String getFlattrUrl() {
    return flattrUrl;
  }

  public void setFlattrUrl(String flattrUrl) {
    this.flattrUrl = flattrUrl;
  }
}
