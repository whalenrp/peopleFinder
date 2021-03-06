package com.vanderbilt.people.finder;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PeopleDataHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME="location.db";
    
    public PeopleDataHelper(Context context)
    {
    	super(context, DATABASE_NAME, null, 1);

    }
    	
    /*Creates the table with the proper columns.  
     * These columns should be the variables located in the Constants class
     * This still works but is improper
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
	db.execSQL("CREATE TABLE locations (" +
			"_id INTEGER PRIMARY KEY," +
			" status TEXT," +
			" name TEXT," +
			" ip TEXT," +
			" longitude FLOAT," +
			" latitude FLOAT," +
			" connectionType TEXT);"); 
    }
    
    
    @Override 
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    { 
      db.execSQL("DROP TABLE IF EXISTS locations"); 
      onCreate(db); 
    }
}
