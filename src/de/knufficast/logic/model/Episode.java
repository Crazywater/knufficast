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
package de.knufficast.logic.model;

import java.io.Serializable;

import de.knufficast.App;
import de.knufficast.events.EpisodeDownloadProgressEvent;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.FlattrStateEvent;

/**
 * An entry in a {@link Feed}. This doesn't necessarily mean that there is an
 * audio file attached - use {@link #hasDownload} to check for that.
 * 
 * @author crazywater
 * 
 */
public class Episode implements Serializable {
  /**
   * Which download state this episode is currently in.
   */
  public enum DownloadState {
    DOWNLOADING, ERROR, FINISHED, NONE, PAUSED
  }

  /**
   * Which state of flattring the episode is currently in.
   */
  public enum FlattrState {
    ENQUEUED, ERROR, FLATTRED, NONE
  }

  /**
   * Which state of playing the episode is currently in.
   */
  public enum PlayState {
    FINISHED, NONE, STARTED_PLAYING
  }

  private static final long serialVersionUID = 2L;

  private String dataUrl;
  private String description;
  private long downloadedBytes;
  private volatile DownloadState downloadState = DownloadState.NONE;
  private int duration;
  private String feedUrl;
  private FlattrState flattrState = FlattrState.NONE;
  private String flattrUrl;
  private String guid;
  private String imgUrl;
  private boolean isNew = true;
  private PlayState playState;
  private int seekLocation;
  private String title;
  private long totalBytes;

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Episode)) {
      return false;
    }
    Episode casted = (Episode) other;

    return casted.getIdentifier().equals(getIdentifier());
  }

  /**
   * Where to download the audio data of this episode.
   */
  public String getDataUrl() {
    return dataUrl;
  }

  /**
   * XML-description or human-readable text description of the episode.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the total download size of this episode. Currently only available
   * after starting a download.
   */
  public long getDownloadedBytes() {
    return downloadedBytes;
  }
  public DownloadState getDownloadState() {
    return downloadState;
  }

  /**
   * Gets the duration of this episode in milliseconds. Returns 0 if this
   * episode has never been prepared by the QueuePlayer.
   */
  public int getDuration() {
    return duration;
  }

  public String getFeedUrl() {
    return feedUrl;
  }

  public String getFileLocation() {
    String urlStr = getDataUrl();
    String[] splitted = urlStr.split("/");
    return Math.abs(urlStr.hashCode()) + splitted[splitted.length - 1];
  }

  public FlattrState getFlattrState() {
    return flattrState;
  }

  public String getFlattrUrl() {
    return flattrUrl;
  }

  /**
   * Returns the GUID (globally unique identifier) of this episode. Identifies
   * an episode uniquely in a feed.
   */
  public String getGuid() {
    return guid;
  }

  public EpisodeIdentifier getIdentifier() {
    return new EpisodeIdentifier(feedUrl, guid);
  }

  /**
   * URL of the episode icon. Empty string if none.
   */
  public String getImgUrl() {
    return imgUrl;
  }

  public PlayState getPlayState() {
    return playState;
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
   * Human-readable episode title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the total download size of this episode. Currently only available
   * after starting a download (0 otherwise).
   */
  public long getTotalBytes() {
    return totalBytes;
  }

  /**
   * Whether this episode even comes with a
   * 
   * @return
   */
  public boolean hasDownload() {
    return !dataUrl.equals("");
  }

  public boolean hasFlattr() {
    return !("".equals(flattrUrl));
  }

  @Override
  public int hashCode() {
    return getIdentifier().hashCode();
  }

  /**
   * Whether this episode is "new". Used to check if we have to add it to the
   * queue.
   */
  public boolean isNew() {
    return isNew;
  }

  public void setDataUrl(String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public void setDescription(String description) {
    this.description = description;
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

  /**
   * Sets the duration of this episode in milliseconds.
   */
  public void setDuration(int duration) {
    this.duration = duration;
  }

  public void setFeedUrl(String feedUrl) {
    this.feedUrl = feedUrl;
  }

  public void setFlattrState(FlattrState flattrState) {
    this.flattrState = flattrState;
    App.get().getEventBus().fireEvent(new FlattrStateEvent());
  }

  public void setFlattrUrl(String flattrUrl) {
    this.flattrUrl = flattrUrl;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  /**
   * Sets the "new" state to false.
   */
  public void setNoLongerNew() {
    isNew = false;
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

  public void setTitle(String title) {
    this.title = title;
  }
}
