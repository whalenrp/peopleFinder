package com.vanderbilt.people.finder;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanderbilt.people.finder.Provider.Constants;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class LocationResponder extends Service
{
	private static final String TAG = "LocationResponder";
	private static final String UPDATED_PROVIDER_FILTER = "com.vanderbilt.people.finder.updated-provider-filter";
	
	/* This can be any number greater than 1000 and less then 65535*/
	private static final int PORT = 5567;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
	}

	@Override
	public void onDestroy()
	{
	}
	
        /*When the service is started, it starts a new thread.*/
	@Override
	public void onStart(Intent intent, int startid) 
	{
		Response myResponse = new Response();
		Thread t = new Thread(myResponse);
		t.start();
	}
	
        /*This is a runnable object that will be called in a seperate thread.
        * This method does the bulk of the work in the class. First it opens
	* a ServerSocket that listens on a port.  It will block on that port 
	* until a connections is established.  Once a connection is established
	* the data from the other phone is interpreted and placed into the 
	* content provider.
	*/
	class Response implements Runnable
	{

	    @Override
		public void run () 
	    {
			try 
			{
				ServerSocket listener = new ServerSocket(PORT);
				
				while (true)
				{
					Socket server = listener.accept();
					Log.v(TAG, "has accepted connection");
					
					String message = convertStreamToString(server.getInputStream());
					Log.v(TAG, message);
					DataModel d = new DataModel();
					try
					{
						d = new DataModel(new JSONObject(message));
					}
					catch (JSONException e)
					{
						Log.w(TAG, "error processing incoming json: " + e.toString());
					}
					
					Cursor c = getContentResolver().query(Constants.CONTENT_URI,
	   						new String[] { Constants.KEY },
	   						Constants.KEY+"="+d.getKey(), null, null);

					ContentValues cv = d.toContentValues();
					if (!d.isMarkedRemoved() && c.getCount() == 0)
					{
						Uri uri = getContentResolver().insert(Constants.CONTENT_URI, cv);
						Log.v(TAG, "Inserted: " + uri.toString());
					}
					else if (d.isMarkedRemoved())
					{
						int i = getContentResolver().delete(Constants.CONTENT_URI,
										Constants.KEY+"="+d.getKey(), null);
						Log.v(TAG, "Deleted " + i + "item(s).");
					}
					else
					{
						int i = getContentResolver().update(Constants.CONTENT_URI, cv,
										Constants.KEY+"="+d.getKey(), null);
						Log.v(TAG, "Updated " + i + "item(s).");
					}
					
					Log.v(TAG, "broadcasting");
					Intent intent = new Intent(UPDATED_PROVIDER_FILTER);
					LocalBroadcastManager.getInstance(LocationResponder.this).sendBroadcast(intent);
					
					c.close();
					server.close();
				}
			}
				
			catch (IOException e) 
			{
				e.printStackTrace();
			}
	    }
	    /*Converts an entire stream into one large string*/
	    private String convertStreamToString(InputStream is)
	    {
	        try{
	            return new java.util.Scanner(is).useDelimiter("\\A").next();
	        }catch(NoSuchElementException e){
	            return "";
	        }
	    }
	}
}



