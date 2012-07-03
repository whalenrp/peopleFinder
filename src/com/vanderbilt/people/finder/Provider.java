package com.vanderbilt.people.finder;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;


public class Provider extends ContentProvider {
  private static final String TABLE="locations";

  public static final class Constants implements BaseColumns {

    public static final Uri CONTENT_URI=
        Uri.parse("content://com.vanderbilt.people.finderProvider/Constants");
    static final String TEXT = "text";
    static final String NAME = "name";
    static final String ID = "_id";
    static final String SERVER_KEY = "server_key";
    static final String IP = "ip";
    public static final String TITLE="title";
    static final String LONGITUDE = "longitude";
    static final String LATITUDE = "latitude";
	public static final String DEFAULT_SORT_ORDER = "title";
  }

  private PeopleDB db = null;
  
  @Override
  public boolean onCreate() 
  {
	  db=new PeopleDB(getContext());
	  return((db == null) ? false : true);
  }
  
  @Override
  public Cursor query(Uri url, String[] projection, String selection,
                      String[] selectionArgs, String sort) {
    SQLiteQueryBuilder qb=new SQLiteQueryBuilder();
    qb.setTables(TABLE);
    String orderBy;
    if (TextUtils.isEmpty(sort)) 
    {
    	orderBy=Constants.DEFAULT_SORT_ORDER;
    }
    else 
    {
    	orderBy=sort;
    }

    
    Cursor queryCursor= qb.query(db.getReadableDatabase(), projection, selection,
                 selectionArgs, null, null, orderBy);

    queryCursor.setNotificationUri(getContext().getContentResolver(), url);

    return(queryCursor);
  }
  
  @Override
  public String getType(Uri url) 
  {
	  return("com.location.item/location");
  }
  

  @Override
  public Uri insert(Uri url, ContentValues initialValues) {
    long rowID = db.getWritableDatabase().insert(TABLE, Constants.TITLE,
                                        initialValues);
    if (rowID > 0) 
    {
      Uri uri = ContentUris.withAppendedId(Provider.Constants.CONTENT_URI,
                                     rowID);
      getContext().getContentResolver().notifyChange(uri, null);
      return(uri);
    }
    throw new SQLException("Failed to insert row into " + url);
  }


  @Override
  public int delete(Uri url, String where, String[] whereArgs) 
  {
	  int count=db.getWritableDatabase().delete(TABLE, where, whereArgs);
	  getContext().getContentResolver().notifyChange(url, null);
	  return(count);
  }

  @Override
  public int update(Uri url, ContentValues values, String where,
                    String[] whereArgs) 
  {
	  int count = db.getWritableDatabase()
			  		.update(TABLE, values, where, whereArgs);
	  getContext().getContentResolver().notifyChange(url, null);
	  return(count);
  }

  
}