package com.vanderbilt.people.finder;

import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.MyLocationOverlay;
import android.graphics.drawable.Drawable;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

public class MapActivity extends Activity
{
	private Cursor myInfo;
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
        setContentView(R.layout.main);
		updateBtn = (Button)findViewById(R.id.postPos);
		refreshBtn = (Button)findViewById(R.id.refresh);

		updateBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				
			}
		}

		refreshBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				
			}
		}
    }

	private GeoPoint getCenter(){
		double latitude, longitude;
		latitude = myInfo.getDouble(myInfo.getColumnIndex(EventsDB.XCOORD));
		longitude = myInfo.getDouble(myInfo.getColumnIndex(EventsDB.YCOORD));
		return new GeoPoint((int)(latitude*1000000.0), (int)(longitude*1000000.0));
	}

	private void initMap(){
		mapthumb.getController().setCenter(center);
		mapthumb.getController().setZoom(16);
		//add destination marker
		Drawable marker = getResources().getDrawable(R.drawable.pushpin);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		mapthumb.getOverlays().add(new SiteOverlay(marker));
		// Add location marker
		me = new MyLocationOverlay(this, mapthumb);
		mapthumb.getOverlays().add(me);

	}

	private class SiteOverlay extends ItemizedOverlay<OverlayItem>{
		private List<OverlayItem> location;

		public SiteOverlay(Drawable marker){
			super(marker);
			boundCenterBottom(marker);
			location = new OverlayItem(center, "Destination", 
				topic.getText().toString());
			populate();
		}

		@Override
		public int size(){
			return 1;
		}

		@Override
		protected OverlayItem createItem(int index){
			return location;
		}
	}
}
