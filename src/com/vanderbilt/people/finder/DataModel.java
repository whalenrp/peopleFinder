package com.vanderbilt.people.finder;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanderbilt.people.finder.Provider.Constants;

import android.content.ContentValues;

public final class DataModel 
{
	private final Long key;
	private String ipAddress;
	private double latitude;
	private double longitude;
	private String status;
	private String name;
	private final boolean markedRemoved;
	
	/**
	 * Simple default constructor. Used for composing
	 * data items to be sent for initial insertion to 
	 * the server database. 
	 */
	public DataModel()
	{
		key = null;
		markedRemoved = false;
	}
	
	/**
	 * Alternative constructor used for composing data 
	 * items to be sent to the server database for 
	 * updating.
	 * @param key only use preexisting keys returned from the server
	 */
	public DataModel(Long skey)
	{
		key = skey;
		markedRemoved = false;
	}
	
	public DataModel(Long skey, String ipAddr)
	{
		key = skey;
		ipAddress = ipAddr;
		markedRemoved = false;
	}
	
	/**
	 * Constructor expects the following keys from the parameter:
	 * skey, ip, lat, long, status, name. Will throw if one is 
	 * missing.
	 * 
	 * @param o 
	 * @throws JSONException
	 */
	public DataModel(JSONObject o) throws JSONException
	{
		key = o.getLong("skey");
		ipAddress = o.getString("ip");
		latitude = o.getDouble("lat");
		longitude = o.getDouble("long");
		status = o.getString("status");
		name = o.getString("name");
		markedRemoved = o.getBoolean("removed");
	}
	
	/**
	 * Packages data into a JSONObject. The following
	 * keys are required: lat, long, status, name. Both
	 * skey and ip are optional depending on the use case.
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject o = new JSONObject();
		if (getKey() != null)
			o.put("skey", key);
		if (getIpAddress() != null)
			o.put("ip", ipAddress);
		o.put("lat", latitude);
		o.put("long", longitude);
		o.put("status", status);
		o.put("name", name);
		o.put("removed", markedRemoved);
		return o;
	}
	
	/**
	 * Returns the full data model as a Content
	 * Values object for easier interfacing with 
	 * content provider services.
	 * 
	 * @return object containing all keys
	 */
	public ContentValues toContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(Constants.SERVER_KEY, key);
		cv.put(Constants.NAME, name);
		cv.put(Constants.IP, ipAddress);
		cv.put(Constants.LATITUDE, latitude);
		cv.put(Constants.LONGITUDE, longitude);
		cv.put(Constants.MESSAGE, status);
		return cv;
	}
	
	public boolean isMarkedRemoved() {
		return markedRemoved;
	}

	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getKey() {
		return key;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress){
		this.ipAddress = ipAddress;
	}

}
