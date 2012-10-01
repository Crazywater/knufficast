/*******************************************************************************
 * Copyright 2012 Crazywater
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.knufficast.logic.model;

import java.io.Serializable;

import de.knufficast.App;
import de.knufficast.events.EpisodeDownloadProgressEvent;
import de.knufficast.events.EpisodeDownloadStateEvent;

/**
 * An entry in a {@link Feed}. This doesn't necessarily mean that there is an
 * audio file attached - use {@link #hasDownload} to check for that.
 * 
 * @author crazywater
 * 
 */
public class Episode implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String feedUrl;
  private final String title;
  private final String imgUrl;
  private final String description;
  private final String dataUrl;
  private final String guid;

  private volatile DownloadState downloadState = DownloadState.NONE;
  private int seekLocation;

  private PlayState playState;

  private boolean isNew;

  private long downloadedBytes;
  private long totalBytes;

  private String downloadedFileName;

  public Episode(String feedUrl, String title, String imgUrl,
      String description, String dataUrl, String guid) {
    this.feedUrl = feedUrl;
    this.title = title;
    this.imgUrl = imgUrl;
    this.description = description;
    this.dataUrl = dataUrl;
    this.guid = guid;
  }

  public String getFeedUrl() {
    return feedUrl;
  }

  public EpisodeIdentifier getIdentifier() {
    return new EpisodeIdentifier(feedUrl, guid);
  }

  public String getFileLocation() {
    String urlStr = getDataUrl();
    String[] splitted = urlStr.split("/");
    return Math.abs(urlStr.hashCode()) + splitted[splitted.length - 1];
  }

  /**
   * Human-readable episode title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * URL of the episode icon. Empty string if none.
   */
  public String getImgUrl() {
    return imgUrl;
  }

  /**
   * XML-description or human-readable text description of the episode.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Where to download the audio data of this episode.
   */
  public String getDataUrl() {
    return dataUrl;
  }

  /**
   * Create a new, mutable Episode.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the GUID (globally unique identifier) of this episode. Identifies
   * an episode uniquely in a feed.
   */
  public String getGuid() {
    return guid;
  }

  /**
   * Which download state this episode is currently in.
   */
  public enum DownloadState {
    NONE, DOWNLOADING, PAUSED, ERROR, FINISHED
  }

  /**
   * Which state of playing the episode is currently in.
   */
  public enum PlayState {
    NONE, STARTED_PLAYING, FINISHED
  }

  /**
   * Whether this episode is "new". Used to check if we have to add it to the
   * queue.
   */
  public boolean isNew() {
    return isNew;
  }

  /**
   * Sets the "new" state to false.
   */
  public void setNoLongerNew() {
    isNew = false;
  }

  public DownloadState getDownloadState() {
    return downloadState;
  }

  /**
   * Sets the download state of this episode. May fire an
   * {@link EpisodeDownloadStateEvent} as a side-effect.
   * 
   * @param downloadState
   */
  public void setDownloadState(DownloadState downloadState) {
    this.downloadState = downloadState;
    App.get().getEventBus()
        .fireEvent(new EpisodeDownloadStateEvent(getIdentifier()));
  }

  public PlayState getPlayState() {
    return playState;
  }

  public void setPlayState(PlayState playState) {
    this.playState = playState;
  }

  /**
   * Sets the seek location (=how much the user has already listened to).
   * 
   * @param location
   *          the location in milliseconds
   */
  public void setSeekLocation(int location) {
    this.seekLocation = location;
  }

  /**
   * Gets the seek location (=how much the user has already listened to).
   * 
   * @param location
   *          the location in milliseconds
   */
  public int getSeekLocation() {
    return seekLocation;
  }

  /**
   * Sets the download progress of this episode.
   * 
   * @param downloadedBytes
   *          the number of bytes already downloaded
   * @param totalBytes
   *          total size of the episode download
   */
  public void setDownloadProgress(long downloadedBytes, long totalBytes) {
    this.downloadedBytes = downloadedBytes;
    this.totalBytes = totalBytes;
    App.get().getEventBus()
        .fireEvent(new EpisodeDownloadProgressEvent(getIdentifier()));
  }

  /**
   * Gets the total download size of this episode. Currently only available
   * after starting a download (0 otherwise).
   */
  public long getTotalBytes() {
    return totalBytes;
  }

  /**
   * Gets the total download size of this episode. Currently only available
   * after starting a download.
   */
  public long getDownloadedBytes() {
    return downloadedBytes;
  }

  /**
   * Whether this episode even comes with a
   * 
   * @return
   */
  public boolean hasDownload() {
    return !dataUrl.equals("");
  }

  @Override
  public int hashCode() {
    return getIdentifier().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Episode)) {
      return false;
    }
    Episode casted = (Episode) other;

    return casted.getIdentifier().equals(getIdentifier());
  }

  public String getDownloadFileName() {
    return downloadedFileName;
  }

  public void setDownloadedFileName(String downloadedFileName) {
    this.downloadedFileName = downloadedFileName;
  }

  /**
   * A mutable version of {@link Episode}.
   * 
   * @author crazywater
   */
  public static class Builder {
    private String feedUrl = "";
    private String title = "";
    private String imgUrl = "";
    private String description = "";
    private String dataUrl = "";
    private String guid = "";

    public Builder feedUrl(String feedUrl) {
      this.feedUrl = feedUrl;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder imgUrl(String imgUrl) {
      this.imgUrl = imgUrl;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder dataUrl(String dataUrl) {
      this.dataUrl = dataUrl;
      return this;
    }

    public Builder guid(String guid) {
      this.guid = guid;
      return this;
    }

    public Episode build() {
      // take the dataUrl as a GUID if none is specified
      String ourGuid = (guid.equals("")) ? dataUrl : guid;
      return new Episode(feedUrl, title, imgUrl, description, dataUrl, ourGuid);
    }
  }
}
