package com.vanderbilt.people.finder;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PeopleDB extends SQLiteOpenHelper{


    private static final String DATABASE_NAME="location.db";
    
    public PeopleDB(Context context)
    {
    	super(context, DATABASE_NAME, null, 1);
    }
    	

    @Override
    public void onCreate(SQLiteDatabase db)
    {
    	 Cursor c=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='location'", null); 
    	    
    	    try { 
    	     	if (c.getCount()==0) { 
    	     		db.execSQL("CREATE TABLE constants (id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT, name TEXT, server_key LONG, ip TEXT, longitude FLOAT, latitude FLOAT);"); 
    	     	}	
    	    }
    	    finally 
    	    { 
    	    	c.close(); 
    	    }
    }
    
    @Override 
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    { 
      db.execSQL("DROP TABLE IF EXISTS location"); 
      onCreate(db); 
    }
}
