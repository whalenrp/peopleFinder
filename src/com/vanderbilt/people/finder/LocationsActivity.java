package com.vanderbilt.people.finder;


import com.vanderbilt.people.finder.Provider.Constants;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;
import android.graphics.drawable.Drawable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class LocationsActivity extends MapActivity
{
	private Button updateBtn;
	private Button refreshBtn;
	private MapView mapthumb;
	private GeoPoint center;
	private MyLocationOverlay me = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations);
		// init variables
		updateBtn = (Button)findViewById(R.id.postPos);
		refreshBtn = (Button)findViewById(R.id.refresh);
		mapthumb = (MapView)findViewById(R.id.map);

		Cursor myInfo = getContentResolver().query(Constants.CONTENT_URI, 
			new String[] {Constants.NAME, Constants.LATITUDE, Constants.LONGITUDE},
			null,null,null);

		initMap(myInfo);

		updateBtn.setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v){
		        LocationManager myLocalManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		        Criteria locationCritera = new Criteria();
		        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
		        locationCritera.setAltitudeRequired(false);
		        locationCritera.setBearingRequired(false);
		        String provider = myLocalManager.getBestProvider(locationCritera, true);
		        myLocalManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, onLocationChange);
			}
			LocationListener onLocationChange=new LocationListener() {
		        public void onLocationChanged(Location loc) {
		            double latitude = loc.getLatitude();
		            double longitude = loc.getLongitude();		 

		            Log.i("UPDATE", latitude + " " + longitude);
					Socket MyClient = null;
			    	DataInputStream input = null;
			    	PrintStream output = null;
				try
				    {
					//for(int i = 0; i < friends.length(); ++i){
					//    Myclient MyClient = new Socket(friends[i], 5567);
					MyClient = new Socket("129.59.69.68", 5567);
					Log.i("UPDATE", "HI");
					input = new DataInputStream(MyClient.getInputStream());
					output = new PrintStream(MyClient.getOutputStream());
					
					output.println("update" + " localhost "+ latitude + " " + longitude);
					output.close();
					input.close();
					MyClient.close();
					//}
				    }
				
				catch (IOException ioe) 
				    {
					System.out.println("IOException on socket listen: " + ioe);
					ioe.printStackTrace();
				    }
					
		        }
		         
		        public void onProviderDisabled(String provider) 
		        {
		        	//Not needed
		        }
		         
		        public void onProviderEnabled(String provider) 
		        {
		        	//Not needed
		        }
		         
		        public void onStatusChanged(String provider, int status, Bundle extras)
		        {
		        	//Not needed
		        }
		    };
		
		});

		refreshBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				
			}
		});
		
		myInfo.close();
    }

	public void onResume(){
		super.onResume();
		me.enableMyLocation();
	}

	public void onPause(){
		super.onPause();
		me.disableMyLocation();
	}

	private GeoPoint getPoint(double lat, double lon){
		Log.i("LocationsActivity", "Latitude: " + lat + ", Longitude: " + lon);
		return new GeoPoint((int)(lat*1000000), (int)(lon*1000000));
	}

	private void initMap(Cursor c){
		me = new MyLocationOverlay(this, mapthumb);
		mapthumb.getOverlays().add(me);	
		me.runOnFirstFix(new Runnable() {
			public void run() {
				mapthumb.getController().animateTo(me.getMyLocation());
				}
		}); 
		mapthumb.getController().setZoom(0);
		//add destination marker
		Drawable marker = getResources().getDrawable(R.drawable.pushpin);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		mapthumb.getOverlays().add(new SiteOverlay(marker, c));
		// Add location marker
	}

	private class SiteOverlay extends ItemizedOverlay<OverlayItem>{
		private List<OverlayItem> positions = new ArrayList<OverlayItem>();

		public SiteOverlay(Drawable marker, Cursor c){
			super(marker);
			boundCenterBottom(marker);
			
			c.moveToFirst();
			while (!c.isAfterLast()){
				GeoPoint point = getPoint(c.getDouble(c.getColumnIndex(Constants.LATITUDE)), 
					c.getDouble(c.getColumnIndex(Constants.LONGITUDE)));
				positions.add(new OverlayItem(point,
					c.getString(c.getColumnIndex(Constants.NAME)), 
					null));
				c.moveToNext();
			}
			populate();
		}

		@Override
		public int size(){
			return positions.size();
		}

		@Override
		protected OverlayItem createItem(int index){
			return positions.get(index);
		}
	}
	
	@Override
	protected boolean isRouteDisplayed(){
		return false;
	}
}
	
/*
	private class SendLocation extends AsyncTask<Void, Void, Void>
	{
		//private double longitude, latitude;
		protected Void doInBackground(Void... params) 
		{
			/*
			Socket MyClient = null;
	    	DataInputStream input = null;
	    	PrintStream output = null;
		try
		    {
			//for(int i = 0; i < friends.length(); ++i){
			//    Myclient MyClient = new Socket(friends[i], 5567);
			MyClient = new Socket("129.59.69.68", 5567);
			Log.i("LOOKHERE", "HI");
			input = new DataInputStream(MyClient.getInputStream());
			output = new PrintStream(MyClient.getOutputStream());
			
	        LocationManager myLocalManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	        Criteria locationCritera = new Criteria();
	        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
	        locationCritera.setAltitudeRequired(false);
	        locationCritera.setBearingRequired(false);
	        String provider = myLocalManager.getBestProvider(locationCritera, true);
	        myLocalManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, onLocationChange);
/*
			output.println("update" + " localhost "+ latitude + " " + longitude);
			output.close();
			input.close();
			MyClient.close();
			//}
		    }
		
		catch (IOException ioe) 
		    {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		    }	
		return null;
		}
		
		LocationListener onLocationChange=new LocationListener() {
	        public void onLocationChanged(Location loc) {
	            double latitude = loc.getLatitude();
	            double longitude = loc.getLongitude();		 

	            Log.i("UPDATE", latitude + " " + longitude);
				Socket MyClient = null;
		    	DataInputStream input = null;
		    	PrintStream output = null;
			try
			    {
				//for(int i = 0; i < friends.length(); ++i){
				//    Myclient MyClient = new Socket(friends[i], 5567);
				MyClient = new Socket("129.59.69.68", 5567);
				Log.i("UPDATE", "HI");
				input = new DataInputStream(MyClient.getInputStream());
				output = new PrintStream(MyClient.getOutputStream());
				
				output.println("update" + " localhost "+ latitude + " " + longitude);
				output.close();
				input.close();
				MyClient.close();
				//}
			    }
			
			catch (IOException ioe) 
			    {
				System.out.println("IOException on socket listen: " + ioe);
				ioe.printStackTrace();
			    }
				
	        }
	         
	        public void onProviderDisabled(String provider) 
	        {
	        	//Not needed
	        }
	         
	        public void onProviderEnabled(String provider) 
	        {
	        	//Not needed
	        }
	         
	        public void onStatusChanged(String provider, int status, Bundle extras)
	        {
	        	//Not needed
	        }
	    };
		}
		
		protected void onPostExecute(Void nothing)
		{
		}
	}*/

