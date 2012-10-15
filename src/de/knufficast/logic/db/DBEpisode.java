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
package de.knufficast.logic.db;

import android.util.Base64;
import de.knufficast.App;
import de.knufficast.events.EpisodeDownloadProgressEvent;
import de.knufficast.events.EpisodeDownloadStateEvent;
import de.knufficast.events.FlattrStateEvent;

/**
 * An entry in a {@link DBFeed}. This doesn't necessarily mean that there is an
 * audio file attached - use {@link #hasDownload} to check for that.
 * 
 * @author crazywater
 * 
 */
public class DBEpisode {
  private static final String TABLE = SQLiteHelper.TABLE_EPISODES;

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

  private Database db;
  private final long id;

  public DBEpisode(long id) {
    this.id = id;
    db = App.get().getDB();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DBEpisode)) {
      return false;
    }
    DBEpisode casted = (DBEpisode) other;

    return id == casted.id;
  }

  public long getId() {
    return id;
  }

  /**
   * Where to download the audio data of this episode.
   */
  public String getDataUrl() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_DATA_URL);
  }

  /**
   * XML-description or human-readable text description of the episode.
   */
  public String getDescription() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_DESCRIPTION);
  }

  /**
   * Gets the total download size of this episode. Currently only available
   * after starting a download.
   */
  public long getDownloadedBytes() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_DOWNLOADED_BYTES);
    return Long.valueOf(dbStr).longValue();
  }

  public DownloadState getDownloadState() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_DOWNLOAD_STATE);
    return DownloadState.valueOf(dbStr);
  }

  /**
   * Gets the duration of this episode in milliseconds. Returns 0 if this
   * episode has never been prepared by the QueuePlayer.
   */
  public int getDuration() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_DURATION);
    return Integer.valueOf(dbStr).intValue();
  }

  public String getFileLocation() {
    String urlStr = getDataUrl();
    String[] splitted = urlStr.split("/");
    return Base64.encodeToString(urlStr.getBytes(), Base64.NO_PADDING)
        + splitted[splitted.length - 1];
  }

  public FlattrState getFlattrState() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_FLATTR_STATE);
    return FlattrState.valueOf(dbStr);
  }

  public String getFlattrUrl() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_FLATTR_URL);
  }

  /**
   * Returns the GUID (globally unique identifier) of this episode. Identifies
   * an episode uniquely in a feed.
   */
  public String getGuid() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_GUID);
  }

  public String getContent() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_CONTENT);
  }

  /**
   * URL of the episode icon. Empty string if none.
   */
  public String getImgUrl() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_IMG_URL);
  }

  public PlayState getPlayState() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_PLAY_STATE);
    return PlayState.valueOf(dbStr);
  }

  /**
   * Gets the seek location (=how much the user has already listened to).
   * 
   * @param location
   *          the location in milliseconds
   */
  public int getSeekLocation() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_SEEK_LOCATION);
    return Integer.valueOf(dbStr).intValue();
  }

  /**
   * Human-readable episode title.
   */
  public String getTitle() {
    return db.get(TABLE, id, SQLiteHelper.C_EP_TITLE);
  }

  /**
   * Gets the total download size of this episode. Currently only available
   * after starting a download (0 otherwise).
   */
  public long getTotalBytes() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_TOTAL_BYTES);
    return Long.valueOf(dbStr).longValue();
  }

  /**
   * Whether this episode even comes with a
   * 
   * @return
   */
  public boolean hasDownload() {
    return !getDataUrl().equals("");
  }

  public boolean hasFlattr() {
    return !("".equals(getFlattrUrl()));
  }

  @Override
  public int hashCode() {
    return Long.valueOf(id).hashCode();
  }

  /**
   * Whether this episode is "new". Used to check if we have to add it to the
   * queue.
   */
  public boolean isNew() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_EP_IS_NEW);
    return dbStr == "1";
  }

  public void setDataUrl(String dataUrl) {
    db.put(TABLE, id, SQLiteHelper.C_EP_DATA_URL, dataUrl);
  }

  public void setDescription(String description) {
    db.put(TABLE, id, SQLiteHelper.C_EP_DESCRIPTION, description);
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
    db.put(TABLE, id, SQLiteHelper.C_EP_DOWNLOADED_BYTES,
        String.valueOf(downloadedBytes));
    db.put(TABLE, id, SQLiteHelper.C_EP_TOTAL_BYTES, String.valueOf(totalBytes));
    App.get().getEventBus().fireEvent(new EpisodeDownloadProgressEvent(id));
  }

  /**
   * Sets the download state of this episode. May fire an
   * {@link EpisodeDownloadStateEvent} as a side-effect.
   * 
   * @param downloadState
   */
  public void setDownloadState(DownloadState downloadState) {
    db.put(TABLE, id, SQLiteHelper.C_EP_DOWNLOAD_STATE, downloadState.name());
    App.get().getEventBus().fireEvent(new EpisodeDownloadStateEvent(id));
  }

  /**
   * Sets the duration of this episode in milliseconds.
   */
  public void setDuration(int duration) {
    db.put(TABLE, id, SQLiteHelper.C_EP_DURATION, String.valueOf(duration));
  }

  public void setFlattrState(FlattrState flattrState) {
    db.put(TABLE, id, SQLiteHelper.C_EP_FLATTR_STATE, flattrState.name());
    App.get().getEventBus().fireEvent(new FlattrStateEvent());
  }

  public void setFlattrUrl(String flattrUrl) {
    db.put(TABLE, id, SQLiteHelper.C_EP_FLATTR_URL, flattrUrl);
  }

  public void setGuid(String guid) {
    db.put(TABLE, id, SQLiteHelper.C_EP_GUID, guid);
  }

  public void setImgUrl(String imgUrl) {
    db.put(TABLE, id, SQLiteHelper.C_EP_IMG_URL, imgUrl);
  }

  public void setNew(boolean isNew) {
    db.put(TABLE, id, SQLiteHelper.C_EP_IS_NEW, isNew ? "1" : "0");
  }

  public void setPlayState(PlayState playState) {
    db.put(TABLE, id, SQLiteHelper.C_EP_PLAY_STATE, playState.name());
  }

  /**
   * Sets the seek location (=how much the user has already listened to).
   * 
   * @param location
   *          the location in milliseconds
   */
  public void setSeekLocation(int location) {
    db.put(TABLE, id, SQLiteHelper.C_EP_SEEK_LOCATION, String.valueOf(location));
  }

  public void setTitle(String title) {
    db.put(TABLE, id, SQLiteHelper.C_EP_TITLE, title);
  }

  public DBFeed getFeed() {
    long feedId = db.getLong(TABLE, id, SQLiteHelper.C_EP_FEED_ID);
    return new DBFeed(feedId);
  }
}
