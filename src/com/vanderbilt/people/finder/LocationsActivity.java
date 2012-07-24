package com.vanderbilt.people.finder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.vanderbilt.people.finder.Provider.Constants;

public class LocationsActivity extends MapActivity implements LocationListener
{
	private static final String TAG = "LocationsActivity";
//	private SendPositionTask task = new SendPositionTask();
	private Location mLocation = null;
	private LocationManager myLocalManager;
	private MapView mapView;
//	private GeoPoint center;
	private MyLocationOverlay myLocOverlay = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations);

		// init variables
		mapView = (MapView)findViewById(R.id.map);

		Cursor myInfo = getContentResolver().query(Constants.CONTENT_URI, 
			new String[] {Constants.NAME, Constants.LATITUDE, Constants.LONGITUDE},
			Constants.SERVER_KEY+"!="+UserData.getId(this), null, null);

		initMap(myInfo);
		myInfo.close();

		myLocalManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		myLocalManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0, this);
		mLocation = myLocalManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		//myLocalManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, this);
    }
	
	public void onResume(){
		super.onResume();
		myLocOverlay.enableMyLocation();
	}

	public void onPause(){
		super.onPause();
		myLocOverlay.disableMyLocation();
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
		mLocation = myLocOverlay.getLastFix();
		if (!myLocalManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			buildAlertMessageNoGPS();
		}
		else
		{
			if (mLocation == null)
			{
				Toast.makeText(this, "Please wait. I'm getting a fix on your location",
					Toast.LENGTH_LONG)
					.show();
			}
			else
			{
				ContentValues cv = new ContentValues(3);
				cv.put(Constants.IP, NetworkUtilities.getMyExternalIp());
				cv.put(Constants.LATITUDE, mLocation.getLatitude());
				cv.put(Constants.LONGITUDE, mLocation.getLongitude());
				int i = getContentResolver().update(Constants.CONTENT_URI, cv,
						Constants.SERVER_KEY+"="+UserData.getId(this), null);
				Log.v(TAG, i + " item(s) updated.");
				new SendPositionTask().execute();
//				if (!task.isRunning())
//					task.execute();
			}
		}
	}

	/**
	 * Refreshes the list of peers by attempting to sync with the 
	 * directory server. Also updates positions on map.
	 */
	public void refreshPeers(View view)
	{
		ContentResolver.requestSync(UserData.getAccount(getApplicationContext()),
									Constants.AUTHORITY, new Bundle());
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

		Log.v("LocationsActivity", "New coordintates: " + latitude + " " + longitude);
		
	}

	@Override
	public void onProviderDisabled(String provider) {}
	 
	@Override
	public void onProviderEnabled(String provider){}
	 
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
		Log.v("LocationsActivity", "Latitude: " + lat + ", Longitude: " + lon);
		return new GeoPoint((int)(lat*1000000), (int)(lon*1000000));
	}

	private void initMap(Cursor c)
	{
		myLocOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocOverlay);	
		myLocOverlay.runOnFirstFix(new Runnable() 
		{
			public void run() 
			{
				mapView.getController().animateTo(myLocOverlay.getMyLocation());
			}
		}); 
		mapView.getController().setZoom(16);
		//add destination marker
		Drawable marker = getResources().getDrawable(R.drawable.pushpin);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		mapView.getOverlays().add(new SiteOverlay(marker, c));
		c.close();
		// Add location marker
	}


	private class SiteOverlay extends ItemizedOverlay<OverlayItem>{
		private List<OverlayItem> positions = new ArrayList<OverlayItem>();

		public SiteOverlay(Drawable marker, Cursor c){
			super(marker);
			boundCenterBottom(marker);
			while (c.moveToNext())
			{
				GeoPoint point = getPoint(c.getDouble(c.getColumnIndex(Constants.LATITUDE)), 
					c.getDouble(c.getColumnIndex(Constants.LONGITUDE)));
				positions.add(new OverlayItem(point,
					c.getString(c.getColumnIndex(Constants.NAME)), 
					null));
			}
			c.close();
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
//		private Context context;
//		private String myIp = "";
//		private boolean isRunning;

//		public SendPositionTask(Context context){
//			this.context = context;
//		}

//		@Override
//		protected void onPreExecute(){
//			isRunning = true;
//		}

		@Override
		protected Void doInBackground(Void... items)
		{
			long id = UserData.getId(LocationsActivity.this);
			
			// Package user info to send via JSON to peers
			Cursor userData = getContentResolver().query(Constants.CONTENT_URI, null,
														 Constants.SERVER_KEY+"="+id, null, null);
			DataModel d = new DataModel(id);
			if (userData.moveToFirst())
			{
				d.setName(userData.getString(userData.getColumnIndex(Constants.NAME)));
				d.setStatus(userData.getString(userData.getColumnIndex(Constants.MESSAGE)));
				d.setLatitude(userData.getDouble(userData.getColumnIndex(Constants.LATITUDE)));
				d.setLongitude(userData.getDouble(userData.getColumnIndex(Constants.LONGITUDE)));
				d.setIpAddress(userData.getString(userData.getColumnIndex(Constants.IP)));
			}
			userData.close();
			String delivery = "";
			try
			{
				delivery = d.toJSON().toString();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			
			// Get list of IPs to send position to
			Cursor c = getContentResolver().query(Constants.CONTENT_URI,
												  new String[]{Constants.IP},
												  Constants.SERVER_KEY+"!="+UserData.getId(LocationsActivity.this),
												  null, null);

			// Get device's IP address
//			if (myIp.equals("")){
//				myIp = new String(NetworkUtilities.getMyExternalIp());
//			}

			// initialize Connect objects for every IP in the database
			ArrayList<Connection> mPeers = new ArrayList<Connection>();
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
				mPeers.add(new Connection(c.getString(c.getColumnIndex(Constants.IP))));


			// Open ports and Send updates to all peers.
			ListIterator<Connection> iter = mPeers.listIterator(0);
			while (iter.hasNext()){
				Connection conn = iter.next();
				if (conn.openConnection()){
					conn.putString(delivery);
				}
				conn.closeConnection();
			}

			return null;
		}

//		@Override
//		protected void onPostExecute(Void empty){
//			isRunning = false;
//		}

//		public boolean isRunning(){
//			return isRunning;
//		}
		
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
			Log.v(TAG, "Created Connection object with ip: " + ip);
		}

		public boolean openConnection(){
			try{
				socket = new Socket(ip, 5567);
				outStream = new PrintStream(socket.getOutputStream());
				valid = true;
				return true;
			}catch(Exception e){
				Log.w(TAG, "Failed to open connection with error: " + e.toString());
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
					Log.w(getClass().getName(), "Could not close socket with error: " + e.toString());
				}
			}
			valid = false;
		}
	}

//	private String getMyExternalIp(){
//		try{
//			URL url = null;
//			HttpURLConnection conn = null;
//
//			url = new URL("http://api.externalip.net/ip/");
//			conn = (HttpURLConnection)url.openConnection();
//
//			InputStream in = null;
//			try{
//				in = new BufferedInputStream(conn.getInputStream());
//			}catch(IOException e){
//				e.printStackTrace();
//				return "";
//			}
//			String responseString = convertStreamToString(in);
//
//			if (responseString.length() == 0) return "";
//			conn.disconnect();
//
//			return responseString;
//
//
//		}catch(MalformedURLException e){
//			Log.w("SyncService", "URL no longer valid");
//			e.printStackTrace();
//		}catch(IOException e){
//			Log.w("SyncService", "Connection could not be established.");
//			e.printStackTrace();
//		}
//		return "";
//	}

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
