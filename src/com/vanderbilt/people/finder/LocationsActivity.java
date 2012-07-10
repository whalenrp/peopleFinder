package com.vanderbilt.people.finder;

import com.vanderbilt.people.finder.Provider.Constants;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import android.database.Cursor;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.AsyncTask;

import android.os.Bundle;
import android.view.View;

import android.widget.Toast;
import android.provider.Settings;



import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.util.NoSuchElementException;

import java.io.PrintStream;
import java.net.Socket;

import java.util.ArrayList;

import java.util.List;
import java.util.ListIterator;


import android.util.Log;

public class LocationsActivity extends MapActivity implements LocationListener
{
	private SendPositionTask task = new SendPositionTask(this);
	private Location mLocation = null;
	private LocationManager myLocalManager;
	private MapView mapthumb;
//	private GeoPoint center;
	private MyLocationOverlay me = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations);

		// init variables
		mapthumb = (MapView)findViewById(R.id.map);

		Cursor myInfo = getContentResolver().query(Constants.CONTENT_URI, 
			new String[] {Constants.NAME, Constants.LATITUDE, Constants.LONGITUDE},
			null,null,null);

		initMap(myInfo);
		myInfo.close();

		myLocalManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		myLocalManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0, this);
		mLocation = myLocalManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		//myLocalManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, this);
    }
	
	public void onResume(){
		super.onResume();
		me.enableMyLocation();
	}

	public void onPause(){
		super.onPause();
		me.disableMyLocation();
	}

	///////////////////////////////////////////
	//   Button click handlers
	///////////////////////////////////////////

	/**
	 * Called when the 'Update My Position' button is clicked.
	 * Sends the phone's latest position fix to the other phones
	 * in the local content provider.
	 */
	public void postPosition(View view){
		// if no gps
			// launch dialog to enable
		// if GPS enabled
			// if no location, make toast telling to wait
			// else call AsyncTask.execute()
		mLocation = me.getLastFix();
		if (!myLocalManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			buildAlertMessageNoGPS();
		}else{
			if (mLocation == null){
				Toast.makeText(this, "Please wait. I'm getting a fix on your location",
					Toast.LENGTH_LONG)
					.show();
			}else{
				if (!task.isRunning())
					task.execute();
			}
		}
	}

	/**
	 * Refreshes the list of peers by attempting to sync with the 
	 * directory server. 
	 */
	public void refreshPeers(View view){
//		String tmp = getMyExternalIp();
//		Log.i("LocationsActivity", tmp);
	}

	// Private function for constructing a dialog in the event of no GPS
	private void buildAlertMessageNoGPS(){
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS is not enabled. We need to get a fix on your location"+
			" before we can send it to your friends. Would you like to enable GPS now?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface d, int which){
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
		final AlertDialog alert = builder.show();
		alert.show();
	}

	///////////////////////////////////////////
	// BEGIN LocationListener implementations
	///////////////////////////////////////////
	@Override
	public void onLocationChanged(Location loc) {
		mLocation = loc;
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();		 

		Log.i("LocationsActivity", "New coordintates: " + latitude + " " + longitude);
		
	}

	@Override
	public void onProviderDisabled(String provider) {}
	 
	@Override
	public void onProviderEnabled(String provider){
		
	}
	 
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras){}


	/////////////////////////////////////////////
	//  Map rendering logic
	/////////////////////////////////////////////

	/**
	 * Returns the geopoint associated with a given 
	 * longitude and latitude
	 */
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

	///////////////////////////////////////////////////////
	///   Background Networking Logic
	///////////////////////////////////////////////////////

	/**
	 * This implementation of AsyncTask handles the transmission
	 * of the best current position to all known peers.
	 * If no last known position has been specified, it will prompt the user
	 * to enable GPS to get a fix on position.
	 */
	private class SendPositionTask extends AsyncTask<Void, Void, Void>{
		private Context context;
		private String myIp = "";
		private boolean isRunning;

		public SendPositionTask(Context context){
			this.context = context;
		}

		@Override
		protected void onPreExecute(){
			isRunning = true;
		}

		@Override
		protected Void doInBackground(Void... items){
			// Get list of IPs to send position to
			Cursor c = context.getContentResolver().query(Constants.CONTENT_URI,
				new String[]{Constants.IP},
				null, null, null);

			// Get device's IP address
			if (myIp.equals("")){
				myIp = new String(getMyExternalIp());
			}

			// initialize Connect objects for every IP in the database
			ArrayList<Connection> mPeers = new ArrayList<Connection>();
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
				mPeers.add(new Connection(c.getString(c.getColumnIndex(Constants.IP))));


			// Open ports and Send updates to all peers.
			ListIterator<Connection> iter = mPeers.listIterator(0);
			while (iter.hasNext()){
				Connection conn = iter.next();
				if (conn.openConnection()){
					conn.putString("update " + myIp + " " + 
						mLocation.getLatitude() + " " + mLocation.getLongitude());
				}
				conn.closeConnection();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void empty){
			isRunning = false;
		}

		public boolean isRunning(){
			return isRunning;
		}
		
	}

	/**
	 * Private helper class for managing individual connections.
	 * Holds a connection open and can send strings through the 
	 * connection if it has been established.
	 */
	private class Connection{
		Socket socket = null; 
		PrintStream outStream = null;
		String ip;
		boolean valid;

		public Connection(String ip){
			this.ip = ip;
			valid=false;
			Log.i("LocationsActivity.Connection", 
				"Created Connection object with ip: " + ip);
		}

		public boolean openConnection(){
			try{
				socket = new Socket(ip, 5567);
				outStream = new PrintStream(socket.getOutputStream());
				valid = true;
				return true;
			}catch(Exception e){
				Log.i("LocationsActivity.Connection", 
					"Failed to open port with ip: " + ip);
				valid=false;
				return false;
			}
		}

		public void putString(String s){
			if (valid && socket != null && outStream != null)
				outStream.println(s);
		}

		public void closeConnection(){
			if (outStream != null)
				outStream.close();
			if (socket != null){
				try{
					socket.close();
				}catch(IOException e){
					Log.i(getClass().getName(), "Could not close socket with ip: " + ip);
				}
			}
			valid = false;
		}
	}

	private String getMyExternalIp(){
		try{
			URL url = null;
			HttpURLConnection conn = null;

			url = new URL("http://api.externalip.net/ip/");
			conn = (HttpURLConnection)url.openConnection();

			InputStream in = null;
			try{
				in = new BufferedInputStream(conn.getInputStream());
			}catch(IOException e){
				e.printStackTrace();
				return "";
			}
			String responseString = convertStreamToString(in);

			if (responseString.length() == 0) return "";
			conn.disconnect();

			return responseString;


		}catch(MalformedURLException e){
			Log.w("SyncService", "URL no longer valid");
			e.printStackTrace();
		}catch(IOException e){
			Log.w("SyncService", "Connection could not be established.");
			e.printStackTrace();
		}
		return "";
	}

    // Helper function for reading input stream
    // retrieved from http://stackoverflow.com/a/5445161/793208
    private String convertStreamToString(InputStream is){
        try{
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        }catch(NoSuchElementException e){
            return "";
        }
    }
    
}
