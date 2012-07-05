package com.vanderbilt.people.finder;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

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
	try
	{
		ServerSocket listener = new ServerSocket(port);
	    }
	
	catch (IOException ioe) 
	    {
		System.out.println("IOException on socket listen: " + ioe);
		ioe.printStackTrace();
	    }
	}

	@Override
	public void onDestroy() {
		
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		try 
		{
		    Response myResponse = new Response();
		    Thread t = new Thread(myResponse);
		    t.start();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}

class Response implements Runnable{

    public void run () {
	try {
	    ServerSocket listener = new ServerSocket(port);
	    while(true)
		{	
		    Socket server = null;
		    server = listener.accept();
		    PrintWriter out = new PrintWriter(server.getOutputStream(), true);
		    DataInputStream in = new DataInputStream (server.getInputStream());
		    String command = in.readLine();
		    if(command.equals("location"))
			{
			    LocationManager myLocalManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
			    Criteria locationCritera = new Criteria();
			    locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
			    String provider = myLocalManager.getBestProvider(locationCritera, true);
			    Location location = myLocalManager.getLastKnownLocation(provider);
			    out.println(location.getLatitude());//query db for location
			    out.println(location.getLongitude());
			}
		    out.close();
		    in.close();
		    server.close();
		}
	}
	catch (IOException e) 
	    {
		e.printStackTrace();
	    }
    }
}
