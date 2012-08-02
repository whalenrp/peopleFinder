package com.vanderbilt.people.finder;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.vanderbilt.people.finder.Provider.Constants;

public class LocationsActivity extends MapActivity implements LocationListener
{
	private static final String TAG = "LocationsActivity";
	private static final String UPDATED_PROVIDER_FILTER = "com.vanderbilt.people.finder.updated-provider-filter";
	private static final int INITIAL_ZOOM = 17;
	private static final String[] PROJECTION = new String[] { Constants.NAME, Constants.STATUS,
															  Constants.LATITUDE, Constants.LONGITUDE};
	private Location mLocation = null;
	private LocationManager myLocalManager;
	private MapView mapView;
	private MyLocationOverlay myLocOverlay = null;
	private Button postPositionPeers;
	private ProviderUpdateReceiver receiver; 
	private LocationOverlay locationOverlay;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations);
        
        // Instantiate receiver. Will be registered in onResume()
        receiver = new ProviderUpdateReceiver();
        
        // If only using the server, disable the Send Data button,
        // as it is useless. 
        postPositionPeers = (Button)findViewById(R.id.postPos);
        if (UserData.getConnectionType(this) == ConnectionType.CLIENT_SERVER)
        {
        	postPositionPeers.setEnabled(false);
        }
        
        // Initialize location services
        myLocalManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        myLocalManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0, this);
        mLocation = myLocalManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Set up map view
		mapView = (MapView)findViewById(R.id.map);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(INITIAL_ZOOM);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		
		// Initialize user location overlay
		myLocOverlay = new MyLocationOverlay(this, mapView);
		mapOverlays.add(myLocOverlay);	
		myLocOverlay.runOnFirstFix(new Runnable() 
		{
			public void run() 
			{
				mapView.getController().animateTo(myLocOverlay.getMyLocation());
			}
		}); 
		
		// Initialize peer locations overlay
		locationOverlay = getLocationOverlay();
		mapOverlays.add(locationOverlay);
    }
    
    protected void onResume()
	{
		super.onResume();
		
		myLocOverlay.enableMyLocation();
		IntentFilter filter = new IntentFilter(UPDATED_PROVIDER_FILTER);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
	}

	protected void onPause()
	{
		super.onPause();
		
		myLocOverlay.disableMyLocation();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

	// Required method for MapActivity
	protected boolean isRouteDisplayed()
    {
		return false;
	}

	/**
	 * Called when the 'Send Data' button is clicked.
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
		if (!myLocalManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
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
				cv.put(Constants.ADDRESS, NetworkUtilities.getIp());
				cv.put(Constants.LATITUDE, mLocation.getLatitude());
				cv.put(Constants.LONGITUDE, mLocation.getLongitude());
				int i = getContentResolver().update(Constants.CONTENT_URI, cv,
						Constants.KEY+"="+UserData.getKey(this), null);
				Log.v(TAG, i + " item(s) updated.");
				new SendPositionTask().execute();
			}
		}
	}

	/**
	 * Refreshes the list of peers by attempting to sync with the 
	 * directory server. Called when 'Refresh' button is pressed.
	 */
	public void refreshPeers(View view)
	{
		ContentResolver.requestSync(UserData.getAccount(getApplicationContext()),
									Constants.AUTHORITY, new Bundle());
	}

	
	// LocationListener implementations
	@Override
	public void onLocationChanged(Location loc)
	{
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

	// Returns an initialized LocationOverlay containing all information
	// required for presentation on the map, taken from the content 
	// provider.
	private LocationOverlay getLocationOverlay()
	{
		Drawable marker = getResources().getDrawable(R.drawable.pushpin);
		LocationOverlay lo = new LocationOverlay(getApplicationContext(), marker);
		
		Cursor c = getContentResolver().query(Constants.CONTENT_URI, PROJECTION, 
				Constants.KEY+"!="+UserData.getKey(this), null, null);
		
		while (c.moveToNext())
		{
			String name = c.getString(c.getColumnIndex(Constants.NAME));
			String status = c.getString(c.getColumnIndex(Constants.STATUS));
			double latitude = c.getDouble(c.getColumnIndex(Constants.LATITUDE));
			double longitude = c.getDouble(c.getColumnIndex(Constants.LONGITUDE));
			
			OverlayItem oItem = getOverlayItem(name, status, latitude, longitude);
			lo.addOverlay(oItem);
		}
		c.close();
		
		return lo;
	}

	private OverlayItem getOverlayItem(String name, String status, double lat, double lon)
	{
		GeoPoint gp = new GeoPoint((int)(lat*1000000), (int)(lon*1000000));
		return new OverlayItem(gp, name, status);
	}

	// Private function for constructing a dialog in the event of no GPS
	private void buildAlertMessageNoGPS()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS is not enabled. We need to get a fix on your location"+
			" before we can send it to your friends. Would you like to enable GPS now?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface d, int which)
				{
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			});
		builder.show();
	}

	private class ProviderUpdateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			Log.v(TAG, "Refreshing overlays.");
			
			// Remove current LocationOverlay, give it updated
			// data, then re-add it.
			mapView.getOverlays().remove(locationOverlay);
			locationOverlay = getLocationOverlay();
			mapView.getOverlays().add(locationOverlay);
			
			// Request re-drawing of map.
			mapView.invalidate();
		}
	}

	/**
	 * This implementation of AsyncTask handles the transmission
	 * of the best current position to all known peers. Will only
	 * be used when the user is part of either a peer-to-peer or 
	 * mixed network. The button that triggers this task will be
	 * disabled if the user is on a server-only network.
	 */
	private class SendPositionTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... items)
		{
			long key = UserData.getKey(LocationsActivity.this);
			
			// Package user info to send via JSON to peers
			Cursor uData = getContentResolver().query(Constants.CONTENT_URI, null,
														 Constants.KEY+"="+key, null, null);
			DataModel d = new DataModel();
			if (uData.moveToFirst())
			{
				d.setKey(key);
				d.setName(uData.getString(uData.getColumnIndex(Constants.NAME)));
				d.setStatus(uData.getString(uData.getColumnIndex(Constants.STATUS)));
				d.setLatitude(uData.getDouble(uData.getColumnIndex(Constants.LATITUDE)));
				d.setLongitude(uData.getDouble(uData.getColumnIndex(Constants.LONGITUDE)));
				d.setIpAddress(uData.getString(uData.getColumnIndex(Constants.ADDRESS)));
				d.setConnectionType(ConnectionType.getConnectionType(
						uData.getString(uData.getColumnIndex(Constants.CONN_TYPE))));
			}
			uData.close();
			
			// Get list of IPs to send position to
			Cursor c = getContentResolver().query(Constants.CONTENT_URI,
						  new String[]{Constants.ADDRESS, Constants.CONN_TYPE},
						  Constants.KEY+"!="+key, null, null);
			
			List<String> ipAddresses = new ArrayList<String>();
			while (c.moveToNext())
			{
				// If user is on a MIXED network, there may be db entries 
				// for CLIENT_SERVER peers. Since they can't receive p2p
				// connections, don't add them to the list.
				ConnectionType ct = ConnectionType.getConnectionType(
						c.getString(c.getColumnIndex(Constants.CONN_TYPE)));
				if (ct != ConnectionType.CLIENT_SERVER)
				{
					ipAddresses.add(c.getString(c.getColumnIndex(Constants.ADDRESS)));
				}
			}
			c.close();
			
			Log.v(TAG, "list size: " + ipAddresses.size());
			
			NetworkUtilities.pushDataToPeers(d, ipAddresses);
			return null;
		}
	}
}
