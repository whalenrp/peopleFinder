package com.vanderbilt.people.finder;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanderbilt.people.finder.Provider.Constants;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class LocationResponder extends Service{
	
	private static final String TAG = "LocationResponder";
	private static final int PORT = 5567;
	
//	private ServerSocket listener = null;
//	private static String location = "location";
	
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
		//new LeaveNetworkTask().execute();
	}
	
	@Override
	public void onStart(Intent intent, int startid) 
	{
		Response myResponse = new Response();
		Thread t = new Thread(myResponse);
		t.start();
	}
	
	class Response implements Runnable
	{
//		public Response(Context c){
//			context = c;
//		}

	    public void run () 
	    {
			try 
			{
				ServerSocket listener = new ServerSocket(PORT);
//				Looper.prepare();
				
				while (true)
				{
					Socket server = listener.accept();
					Log.v(TAG, "has accepted connection");
//					PrintWriter out = new PrintWriter(server.getOutputStream(), true);
//					DataInputStream in = new DataInputStream (server.getInputStream());
					
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
	   						new String[] { Constants.SERVER_KEY },
	   						Constants.SERVER_KEY+"="+d.getKey(), null, null);

					ContentValues cv = d.toContentValues();
					if (c.getCount() == 0)
					{
					Uri uri = getContentResolver().insert(Constants.CONTENT_URI, cv);
					Log.v(TAG, "Inserted: " + uri.toString());
					}
					else if (d.isMarkedRemoved())
					{
					int i = getContentResolver().delete(Constants.CONTENT_URI,
										Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.v(TAG, "Deleted " + i + "item(s).");
					}
					else
					{
					int i = getContentResolver().update(Constants.CONTENT_URI, cv,
										Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.v(TAG, "Updated " + i + "item(s).");
					}
					
					c.close();
//					String command = in.readLine();
//					command = command.trim();
//					String args[] = command.split(" ");
//					if(args[0].equals("update"))
//					{
//						String ip = args[1];
//						double latitude = Double.parseDouble(args[2]);
//						double longitude = Double.parseDouble(args[3]);
					
//						ContentValues cv = new ContentValues();
//						cv.put(Constants.IP, ip);
//						cv.put(Constants.LATITUDE, latitude);
//						cv.put(Constants.LONGITUDE, longitude);
//						int i = context.getContentResolver().update(Constants.CONTENT_URI,
//							cv, 
//							Constants.IP + "=?",
//							new String[]{ip});
//						Log.i("MainActivity", "Updated " + i + " entries.");

//					}
//					out.close();
//					in.close();
					server.close();

//					Looper.loop();
					Log.d(TAG, "Exited out the loop.");
				}
				}
				
			catch (IOException e) 
			{
				e.printStackTrace();
			}
	    }
	    
	    private String convertStreamToString(InputStream is)
	    {
	        try{
	            return new java.util.Scanner(is).useDelimiter("\\A").next();
	        }catch(NoSuchElementException e){
	            return "";
	        }
	    }
	}
	
//	class LeaveNetworkTask extends AsyncTask<Void, Void, Void>
//	{
//		@Override
//		protected Void doInBackground(Void... params) 
//		{
//			boolean b = NetworkUtilities.requestRemoval(UserData.getId(LocationResponder.this));
//			if (!b)
//				Log.w(TAG, "User could not unregister from server.");
//			
//			return null;
//		}
//	}
}



