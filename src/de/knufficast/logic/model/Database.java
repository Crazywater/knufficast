package de.knufficast.logic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Database {
  private final SQLiteHelper dbHelper;
  private SQLiteDatabase database;

  public Database(Context context) {
    dbHelper = new SQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }
  
  public List<Long> getIds(String table) {
    String[] id = { SQLiteHelper.C_ID };
    Cursor cursor = database.query(table, id, null, null, null, null, null);
    return getAllIds(cursor);
  }

  public List<Long> query(String table, String column, String value) {
    String[] id = { SQLiteHelper.C_ID };
    Cursor cursor = database.query(table, id, column + " = " + value, null,
        null, null, null);
    return getAllIds(cursor);
  }

  public String get(String table, long id, String column) {
    String[] col = { column };
    Cursor cursor = database.query(table, col, SQLiteHelper.C_ID + " = "
        + id, null, null, null, null);
    cursor.moveToFirst();
    String result = cursor.getString(0);
    cursor.close();
    return result;
  }

  public long getLong(String table, long id, String column) {
    String[] col = { column };
    Cursor cursor = database.query(table, col, SQLiteHelper.C_ID + " = " + id,
        null, null, null, null);
    cursor.moveToFirst();
    long result = cursor.getLong(0);
    cursor.close();
    return result;
  }

  public void put(String table, long id, String column, String value) {
    ContentValues values = new ContentValues();
    values.put(column, value);
    database.update(table, values, SQLiteHelper.C_ID + " = " + id, null);
  }

  public long create(String table, Collection<String> columns,
      Iterable<String> values) {
    ContentValues cvs = new ContentValues();
    Iterator<String> it = values.iterator();
    for (String col : columns) {
      cvs.put(col, it.next());
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
}
