package com.vanderbilt.people.finder;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanderbilt.people.finder.Provider.Constants;

import android.content.ContentValues;

public final class DataModel 
{
	public static final String KEY = "key";
	public static final String IP = "ip";
	public static final String LAT = "lat";
	public static final String LONG = "long";
	public static final String STATUS = "status";
	public static final String NAME = "name";
	public static final String REMOVED = "removed";
	public static final String CONN_TYPE = "conn_type";
	
	private Long key;
	private String ipAddress;
	private double latitude;
	private double longitude;
	private String status;
	private String name;
	private final boolean markedRemoved;
	private ConnectionType connectionType;
	
	public DataModel()
	{
		markedRemoved = false;
	}
	
	public DataModel(JSONObject o) throws JSONException
	{
		// Required fields
		key = o.getLong(KEY);
		ipAddress = o.getString(IP);
		markedRemoved = o.getBoolean(REMOVED);
		connectionType = ConnectionType.getConnectionType(o.getString(CONN_TYPE));
		
		// Optional
		if (o.has(LAT))
			latitude = o.getDouble(LAT);
		if (o.has(LONG))
			longitude = o.getDouble(LONG);
		if (o.has(STATUS))
			status = o.getString(STATUS);
		if (o.has(NAME))
			name = o.getString(NAME);
	}
	
	public JSONObject toJSON() throws JSONException
	{
		JSONObject o = new JSONObject();
		
		if (getKey() != null)
			o.put(KEY, key);
		o.put(IP, ipAddress);
		o.put(LAT, latitude);
		o.put(LONG, longitude);
		o.put(STATUS, status);
		o.put(NAME, name);
		o.put(REMOVED, markedRemoved);
		o.put(CONN_TYPE, connectionType.name());
		
		return o;
	}
	
	public ContentValues toContentValues()
	{
		ContentValues cv = new ContentValues();
		if (key != null)
			cv.put(Constants.KEY, key);
		if (name != null)
			cv.put(Constants.NAME, name);
		if (ipAddress != null)
			cv.put(Constants.ADDRESS, ipAddress);
		if (status != null)
			cv.put(Constants.STATUS, status);
		if (latitude != 0.0d)
			cv.put(Constants.LATITUDE, latitude);
		if (longitude != 0.0d)
			cv.put(Constants.LONGITUDE, longitude);
		if (connectionType != null)
			cv.put(Constants.CONN_TYPE, connectionType.name());
		
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
	public void setKey(Long key){
		this.key = key;
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

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

}
