package com.vanderbilt.people.finder;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

	    public void run () {
			try {
				ServerSocket listener = new ServerSocket(PORT);
				Looper.prepare();

					Socket server = listener.accept();
					//PrintWriter out = new PrintWriter(server.getOutputStream(), true);
					//DataInputStream in = new DataInputStream (server.getInputStream());
					
					String message = convertStreamToString(server.getInputStream());
					Log.v(TAG, message);
					//String command = in.readLine();
					//command = command.trim();
					//String args[] = command.split(" ");
//					if(args[0].equals("update"))
//					{
//						String ip = args[1];
//						double latitude = Double.parseDouble(args[2]);
//						double longitude = Double.parseDouble(args[3]);
	//
//						ContentValues cv = new ContentValues();
//						cv.put(Constants.IP, ip);
//						cv.put(Constants.LATITUDE, latitude);
//						cv.put(Constants.LONGITUDE, longitude);
//						int i = context.getContentResolver().update(Constants.CONTENT_URI,
//							cv, 
//							Constants.IP + "=?",
//							new String[]{ip});
//						Log.i("MainActivity", "Updated " + i + " entries.");
	//
//					}
					//out.close();
//					in.close();
					server.close();

				Looper.loop();
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



