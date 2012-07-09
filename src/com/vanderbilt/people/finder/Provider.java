package com.vanderbilt.people.finder;


import android.content.UriMatcher;
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
  private static final int LOCATIONS = 1;
  private static final int LOCATION_ID = 2;
  private static final UriMatcher MATCHER;

 
  public static final class Constants implements BaseColumns {

	public static final String AUTHORITY = "com.vanderbilt.people.finder.Provider";
    public static final Uri CONTENT_URI=
        Uri.parse("content://com.vanderbilt.people.finder.Provider/locations");
    static final String MESSAGE = "message";
    static final String NAME = "name";
    static final String ID = "_id";
    static final String SERVER_KEY = "server_key";
    static final String IP = "ip";
    public static final String TITLE="title";
    static final String LONGITUDE = "longitude";
    static final String LATITUDE = "latitude";
	public static final String DEFAULT_SORT_ORDER = NAME;
  }

  static{
		MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		MATCHER.addURI("com.vanderbilt.people.finder.Provider", 
			"locations", LOCATIONS);
		MATCHER.addURI("com.vanderbilt.people.finder.Provider", 
			"locations/#", LOCATION_ID);
  }

  private PeopleDB db = null;
  
  @Override
  public boolean onCreate() 
  {
	  
	  db=new PeopleDB(getContext());
	  db.getWritableDatabase();
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
	  if (isCollectionUri(url))
	 	return("vnd.people.cursor.dir/locations");
	  else
	 	return("vnd.people.cursor.item/locations");
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

  private boolean isCollectionUri(Uri url){
      return (MATCHER.match(url) == LOCATIONS);
  }
  
}
