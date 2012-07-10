package com.vanderbilt.people.finder;


import com.vanderbilt.people.finder.Provider.Constants;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;
import android.graphics.drawable.Drawable;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
				
			}
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
