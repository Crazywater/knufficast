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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * A connection to the Android-internal SQLite3 database. Caches writes so that
 * multiple writes only write the last result. Also caches reads, so multiple
 * reads don't go to the database.
 * 
 * @author crazywater
 * 
 */
public class Database {
  private final SQLiteHelper dbHelper;
  private final DBUpdater dbUpdater;
  private SQLiteDatabase database;

  private class ColId {
    ColId(String table, String column, long id) {
      this.table = table;
      this.column = column;
      this.id = id;
    }

    String table;
    String column;
    long id;

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ColId)) {
        return false;
      }
      ColId c = (ColId) other;
      return id == c.id && table.equals(c.table) && column.equals(c.column);
    }

    @Override
    public int hashCode() {
      return table.hashCode() ^ column.hashCode() ^ Long.valueOf(id).hashCode();
    }
  }

  private Map<ColId, String> cache = new ConcurrentHashMap<ColId, String>();

  public Database(Context context) {
    dbHelper = new SQLiteHelper(context);
    dbUpdater = new DBUpdater();
    dbUpdater.start();
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  /**
   * Gets all row ids from the table.
   */
  public List<Long> getIds(String table) {
    String[] id = { SQLiteHelper.C_ID };
    Cursor cursor = database.query(table, id, null, null, null, null,
        SQLiteHelper.C_ID + " DESC");
    return getAllIds(cursor);
  }

  /**
   * Querys the database for rows which have column=value.
   */
  public List<Long> query(String table, String column, String value) {
    String[] id = { SQLiteHelper.C_ID };
    String[] values = { value };
    Cursor cursor = database.query(table, id, column + " = ?", values, null,
        null, SQLiteHelper.C_ID + " DESC");
    return getAllIds(cursor);
  }

  public void delete(String table, long id) {
    database.delete(table, SQLiteHelper.C_ID + " = " + id, null);
  }

  /**
   * Gets a value from the database. Might be cached.
   */
  public String get(String table, long id, String column) {
    String[] col = { column };
    ColId key = new ColId(table, column, id);
    if (cache.containsKey(key)) {
      String result = cache.get(key);
      return result;
    }
    Cursor cursor = database.query(table, col, SQLiteHelper.C_ID + " = "
        + id, null, null, null, null);
    cursor.moveToFirst();
    String result = cursor.getString(0);
    cursor.close();
    return result;
  }

  /**
   * Returns a long value. Is only used for referencing rows of other tables.
   */
  public long getLong(String table, long id, String column) {
    String[] col = { column };
    Cursor cursor = database.query(table, col, SQLiteHelper.C_ID + " = " + id,
        null, null, null, null);
    cursor.moveToFirst();
    long result = cursor.getLong(0);
    cursor.close();
    return result;
  }

  /**
   * Set a value in the table.
   * 
   * @param table
   *          the table
   * @param id
   *          the row ID
   * @param column
   *          the column name
   * @param value
   *          the value
   */
  public void put(String table, long id, String column, String value) {
    ColId colId = new ColId(table, column, id);
    cache.put(colId, value);
    dbUpdater.postUpdate(colId, value);
  }

  /**
   * Creates a new row in the table.
   * 
   * @return the ID of the row
   */
  public long create(String table, Collection<String> columns,
      Iterable<String> values) {
    ContentValues cvs = new ContentValues();
    Iterator<String> it = values.iterator();
    for (String col : columns) {
      String val = it.next();
      cvs.put(col, val == null ? "" : val);
    }
    long id = database.insert(table, null, cvs);
    return id;
  }

  private List<Long> getAllIds(Cursor cursor) {
    cursor.moveToFirst();

    List<Long> results = new ArrayList<Long>();
    while (!cursor.isAfterLast()) {
      results.add(cursor.getLong(0));
      cursor.moveToNext();
    }
    cursor.close();
    return results;
  }

  /**
   * A simple updater thread that does writes to the database in the background.
   * "Batches up" writes to the same location, so that only the last write is
   * executed. Writes are generally executed only after a time of WAIT_TIME.
   */
  private class DBUpdater extends Thread {
    private Map<ColId, String> toUpdate = new ConcurrentHashMap<ColId, String>();
    private static final long WAIT_TIME = 10 * 1000; // 10s
    private long lastWrite = 0;

    @Override
    public void run() {
      while (true) {
        try {
          synchronized (this) {
            this.wait();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        long diff = System.currentTimeMillis() - lastWrite;
        if (diff < WAIT_TIME) {
          try {
            Thread.sleep(WAIT_TIME - diff);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        lastWrite = System.currentTimeMillis();
        while (!toUpdate.isEmpty()) {
          Iterator<Entry<ColId, String>> it = toUpdate.entrySet().iterator();
          while (it.hasNext()) {
            Entry<ColId, String> entry = it.next();
            ColId id = entry.getKey();
            ContentValues cvs = new ContentValues();
            cvs.put(id.column, entry.getValue());
            database.update(id.table, cvs, SQLiteHelper.C_ID + " = " + id.id,
                null);
            it.remove();
          }
        }
      }
    }

    /**
     * Request a database update from this thread.
     */
    void postUpdate(ColId id, String value) {
      toUpdate.put(id, value);
      synchronized (this) {
        this.notify();
      }
    }
  }
}
