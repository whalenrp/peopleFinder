package com.vanderbilt.people.finder;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class LocationOverlay extends ItemizedOverlay<OverlayItem> 
{
//	private static final String TAG = "LocationOverlay";
	
	private Context context;
	private ArrayList<OverlayItem> overlayList = new ArrayList<OverlayItem>();

	public LocationOverlay(Context context, Drawable defaultMarker) 
	{
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}
	
	public void addOverlay(OverlayItem overlay)
	{
		overlayList.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i)
	{
		return overlayList.get(i);
	}

	@Override
	public int size()
	{
		return overlayList.size();
	}
	
	protected boolean onTap(int index)
	{
		OverlayItem item = overlayList.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		
		return true;
	}

}
