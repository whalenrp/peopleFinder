package com.vanderbilt.people.finder;
import com.vanderbilt.people.finder.Provider.Constants;

import android.content.ContentValues;
import java.lang.Double;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;
import android.content.Context;
import java.lang.Runnable;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.IBinder;
import android.util.Log;
import android.text.format.Formatter;

public class LocationResponder extends Service{

	private static int port = 5567;
	private ServerSocket listener = null;
	private static String location = "location";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
	}

	@Override
	public void onDestroy() {
		
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Response myResponse = new Response(this);
		Thread t = new Thread(myResponse);
		t.start();
	}
}

class Response implements Runnable{
	
	private Context context;

	public Response(Context c){
		context = c;
	}

    public void run () {
		try {
			ServerSocket listener = new ServerSocket(5567);
			Looper.prepare();

				Socket server = null;
				server = listener.accept();
				PrintWriter out = new PrintWriter(server.getOutputStream(), true);
				DataInputStream in = new DataInputStream (server.getInputStream());

				String command = in.readLine();
				command = command.trim();
				String args[] = command.split(" ");
				if(args[0].equals("update"))
				{
					String ip = args[1];
					double latitude = Double.parseDouble(args[2]);
					double longitude = Double.parseDouble(args[3]);

					ContentValues cv = new ContentValues();
					cv.put(Constants.IP, ip);
					cv.put(Constants.LATITUDE, latitude);
					cv.put(Constants.LONGITUDE, longitude);
					int i = context.getContentResolver().update(Constants.CONTENT_URI,
						cv, 
						Constants.IP + "=?",
						new String[]{ip});
					Log.i("MainActivity", "Updated " + i + " entries.");

				}
				out.close();
				in.close();
				server.close();

			Looper.loop();
		}
		catch (IOException e) 
			{
			e.printStackTrace();
			}
    }
}
