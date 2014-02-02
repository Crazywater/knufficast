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

import java.util.ArrayList;
import java.util.List;

import de.knufficast.App;

/**
 * Representation of a Feed. Contains {@link DBEpisode}s.
 * 
 * @author crazywater
 * 
 */
public class DBFeed {
  private static final String TABLE = SQLiteHelper.TABLE_FEEDS;
  private static final String EP_TABLE = SQLiteHelper.TABLE_EPISODES;
  private final long id;
  private final Database db;

  public DBFeed(long id) {
    this.id = id;
    db = App.get().getDB();
  }

  public long getId() {
    return id;
  }

  /**
   * Returns a human-readable description of this feed.
   */
  public String getDescription() {
    return db.get(TABLE, id, SQLiteHelper.C_FD_DESCRIPTION);
  }

  /**
   * Returns the encoding of this feed (e.g. "UTF-8").
   */
  public String getEncoding() {
    return db.get(TABLE, id, SQLiteHelper.C_FD_ENCODING);
  }

  /**
   * Returns the list of episodes of this feed.
   */
  public List<DBEpisode> getEpisodes() {
    List<DBEpisode> result = new ArrayList<DBEpisode>();
    List<Long> children = db.query(EP_TABLE, SQLiteHelper.C_EP_FEED_ID,
        String.valueOf(id));
    for (Long id : children) {
      result.add(new DBEpisode(id));
    }
    return result;
  }

  /**
   * Returns the "ETag" header of the last feed download, if any, or null. This
   * header is used for asking the server if the feed has to be refreshed
   * (re-downloaded).
   */
  public String getETag() {
    return db.get(TABLE, id, SQLiteHelper.C_FD_ETAG);
  }

  /**
   * Returns the URL of this feed. Currently used for identification purposes as
   * well.
   */
  public String getFeedUrl() {
    return db.get(TABLE, id, SQLiteHelper.C_FD_FEED_URL);
  }

  /**
   * Returns the URL of the icon of this feed.
   */
  public String getImgUrl() {
    return db.get(TABLE, id, SQLiteHelper.C_FD_IMG_URL);
  }

  /**
   * When the feed has been last updated, in UNIX time. This time is NOT the
   * system time on the phone, but rather what the server supplied in its "Date"
   * header. Used to check if we need to refresh the feed.
   */
  public long getLastUpdated() {
    String dbStr = db.get(TABLE, id, SQLiteHelper.C_FD_LAST_UPDATED);
    return Long.valueOf(dbStr).longValue();
  }

  /**
   * Returns the human-readable title of this feed.
   */
  public String getTitle() {
    return db.get(TABLE, id, SQLiteHelper.C_FD_TITLE);
  }

  public void setDescription(String description) {
    db.put(TABLE, id, SQLiteHelper.C_FD_DESCRIPTION, description);
  }

  public void setEncoding(String encoding) {
    db.put(TABLE, id, SQLiteHelper.C_FD_ENCODING, encoding);
  }

  public void setETag(String eTag) {
    db.put(TABLE, id, SQLiteHelper.C_FD_ETAG, eTag);
  }

  public void setFeedUrl(String feedUrl) {
    db.put(TABLE, id, SQLiteHelper.C_FD_FEED_URL, feedUrl);
  }

  public void setImgUrl(String imgUrl) {
    db.put(TABLE, id, SQLiteHelper.C_FD_IMG_URL, imgUrl);
  }

  public void setLastUpdated(long lastUpdated) {
    db.put(TABLE, id, SQLiteHelper.C_FD_LAST_UPDATED,
        String.valueOf(lastUpdated));
  }

  public void setTitle(String title) {
    db.put(TABLE, id, SQLiteHelper.C_FD_TITLE, title);
  }
}
