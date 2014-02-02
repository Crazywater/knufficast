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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A class that keeps information about the SQLite table structure.
 * 
 * @author crazywater
 * 
 */
public class SQLiteHelper extends SQLiteOpenHelper {
  public static final String TABLE_EPISODES = "episodes";
  public static final String TABLE_FEEDS = "feeds";
  public static final String C_ID = "_id";
  public static final String C_EP_FEED_ID = "feedId";
  public static final String C_EP_DATA_URL = "dataUrl";
  public static final String C_EP_TITLE = "title";
  public static final String C_EP_DESCRIPTION = "description";
  public static final String C_EP_CONTENT = "content";
  public static final String C_EP_FLATTR_URL = "flattrUrl";
  public static final String C_EP_GUID = "guid";
  public static final String C_EP_IMG_URL = "imgUrl";
  public static final String C_EP_DOWNLOADED_BYTES = "downloadedBytes";
  public static final String C_EP_TOTAL_BYTES = "totalBytes";
  public static final String C_EP_DOWNLOAD_STATE = "downloadState";
  public static final String C_EP_FLATTR_STATE = "flattrState";
  public static final String C_EP_PLAY_STATE = "playState";
  public static final String C_EP_SEEK_LOCATION = "seekLocation";
  public static final String C_EP_DURATION = "duration";
  public static final String C_EP_IS_NEW = "isNew";
  public static final String C_FD_DESCRIPTION = "description";
  public static final String C_FD_ENCODING = "encoding";
  public static final String C_FD_ETAG = "eTag";
  public static final String C_FD_FEED_URL = "feedUrl";
  public static final String C_FD_IMG_URL = "imgUrl";
  public static final String C_FD_LAST_UPDATED = "lastUpdated";
  public static final String C_FD_TITLE = "title";
  public static final String C_QUEUE_EP_ID = "epId";

  private static final String DATABASE_NAME = "knufficast.db";
  private static final int DATABASE_VERSION = 2;

  private static final String UPDATE = " text not null default '';";
  private static final String NEXT = " text not null default '', ";
  private static final String NEXTINT = " text not null default '0', ";
  private static final String NEXTENUM = " text not null default 'NONE', ";
  private static final String NEXTBOOLEAN = " text not null default '1', ";
  private static final String LAST = " text not null default '');";

  public SQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  private static final String EP_CREATE = "create table "
      + TABLE_EPISODES + "("
      + C_ID + " integer primary key autoincrement, "
      + C_EP_FEED_ID + " integer not null, "
      + C_EP_DATA_URL + NEXT
      + C_EP_TITLE + NEXT
      + C_EP_DESCRIPTION + NEXT
      + C_EP_FLATTR_URL + NEXT
      + C_EP_GUID + NEXT
      + C_EP_IMG_URL + NEXT
      + C_EP_DOWNLOADED_BYTES + NEXTINT
      + C_EP_TOTAL_BYTES + NEXTINT
      + C_EP_DOWNLOAD_STATE + NEXTENUM
      + C_EP_FLATTR_STATE + NEXTENUM
      + C_EP_PLAY_STATE + NEXTENUM
      + C_EP_SEEK_LOCATION + NEXTINT
      + C_EP_DURATION + NEXTINT
      + C_EP_IS_NEW + NEXTBOOLEAN
      + C_EP_CONTENT + NEXT
      + "FOREIGN KEY(" + C_EP_FEED_ID + ") REFERENCES " + TABLE_FEEDS + "(" + C_ID + "));";
  
  private static final String FD_CREATE = "create table "
      + TABLE_FEEDS + "("
      + C_ID + " integer primary key autoincrement, "
      + C_FD_DESCRIPTION + NEXT
      + C_FD_ENCODING + NEXT
      + C_FD_ETAG + NEXT
      + C_FD_FEED_URL + NEXT
      + C_FD_IMG_URL + NEXT
      + C_FD_TITLE + NEXT
      + C_FD_LAST_UPDATED + LAST;

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(FD_CREATE);
    database.execSQL(EP_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d("SQLiteHelper ", oldVersion + "->" + newVersion);
    if (oldVersion < 2) {
      db.execSQL("alter table " + TABLE_EPISODES + " add column "
          + C_EP_CONTENT + UPDATE);
    } else if (oldVersion < DATABASE_VERSION) {
      onCreate(db);
    }
  }
}
