package com.vanderbilt.people.finder;

import com.vanderbilt.people.finder.Provider.Constants;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;
import android.graphics.drawable.Drawable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class LocationsActivity extends MapActivity implements LocationListener
{
	private static final String TAG = "LocationsActivity";
	
	private Location mLocation = null;
	private LocationManager myLocalManager;
//	private Button updateBtn;
	private Button refreshBtn;
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
//		updateBtn = (Button)findViewById(R.id.postPos);
		refreshBtn = (Button)findViewById(R.id.refresh);
		mapthumb = (MapView)findViewById(R.id.map);

		Cursor myInfo = getContentResolver().query(Constants.CONTENT_URI, 
			new String[] {Constants.NAME, Constants.LATITUDE, Constants.LONGITUDE},
			null,null,null);

		initMap(myInfo);
		
		myLocalManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		myLocalManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0, this);
		mLocation = myLocalManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		
		// Since we are making use of a sync adapter, and the 
		// sync adapter performs the functionality of both of
		// these buttons in one fell swoop, they will essentially
		// be the same.
//		updateBtn.setOnClickListener(new View.OnClickListener()
//		{
//			public void onClick(View v)
//			{
//				if (mLocation == null)
//				{
//					mLocation = myLocalManager.getLastKnownLocation(
//							LocationManager.NETWORK_PROVIDER);
//					if (mLocation == null)
//						return;
//				}
//				ContentValues cv = new ContentValues(2);
//				cv.put(Constants.LATITUDE, mLocation.getLatitude());
//				cv.put(Constants.LONGITUDE, mLocation.getLongitude());
//				getContentResolver().update(Constants.CONTENT_URI, cv, 
//						Constants.SERVER_KEY+"="+UserData.getId(LocationsActivity.this), null);
//				ContentResolver.requestSync(UserData.getAccount(getApplicationContext()), 
//						Constants.AUTHORITY, new Bundle());
//			}
//		});

		refreshBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (mLocation == null)
				{
					mLocation = myLocalManager.getLastKnownLocation(
							LocationManager.NETWORK_PROVIDER);
					if (mLocation == null)
						return;
				}
				ContentValues cv = new ContentValues(2);
				cv.put(Constants.LATITUDE, mLocation.getLatitude());
				cv.put(Constants.LONGITUDE, mLocation.getLongitude());
				getContentResolver().update(Constants.CONTENT_URI, cv, 
						Constants.SERVER_KEY+"="+UserData.getId(LocationsActivity.this), null);
				ContentResolver.requestSync(UserData.getAccount(getApplicationContext()), 
						Constants.AUTHORITY, new Bundle());
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
		mapthumb.getController().setZoom(3);
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

	@Override
	public void onLocationChanged(Location loc)
	{
		mLocation = loc;		 
		Log.v(TAG, "New coordintates! Lat: " + loc.getLatitude() + " Long: " + loc.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		// Nothing
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		// Nothing.
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// Nothing.
	}
}
