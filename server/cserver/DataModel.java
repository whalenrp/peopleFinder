package self.cserver;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@PersistenceCapable
public final class DataModel 
{
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long key;
	
	@Persistent
	private String ipAddress;
	
	@Persistent
	private double latitude;
	
	@Persistent
	private double longitude;
	
	@Persistent
	private String status;
	
	@Persistent 
	private String name;
	
	@Persistent
	private boolean markedRemoved;
	
	public DataModel()
	{
		// Nothing to do here.
	}
	
	/**
	 * Constructor expects the following keys from the parameter:
	 * ip, lat, long, status, name. If the entity already exists
	 * on the server and the call is to update, the 'skey' key 
	 * must be included. IP Address can be obtained in a servlet
	 * environment.
	 * 
	 * @param o 
	 * @param ip
	 * @throws JSONException
	 */
	public DataModel(JSONObject o) throws JSONException
	{
		if (o.has("skey"))
			key = o.getLong("skey");
		if (o.has("ip"))
			ipAddress = o.getString("ip");
		if (o.has("lat"))
			latitude = o.getDouble("lat");
		if (o.has("long"))
			longitude = o.getDouble("long");
		if (o.has("status"))
			status = o.getString("status");
		if (o.has("name"))
			name = o.getString("name");
		markedRemoved = false;
	}
	
	/**
	 * Packages up the data held by the model into a standard
	 * JSON object. The following keys are included in the 
	 * returned object: skey, status, name, lat, long, ip. The key
	 * 'skey' is guaranteed to be unique as long as the model's
	 * represented entity has been stored in the App Engine database. 
	 * 
	 * @return JSON data object used by the org.json package
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject o = new JSONObject();
		
		o.put("skey", key);
		o.put("ip", ipAddress);
		o.put("lat", latitude);
		o.put("long", longitude);
		o.put("status", status);
		o.put("name", name);
		o.put("removed", markedRemoved);
		
		return o;
	}

	public boolean isMarkedRemoved() {
		return markedRemoved;
	}

	public void setMarkedRemoved(boolean markedRemoved) {
		this.markedRemoved = markedRemoved;
	}

	public Long getKey()
	{
		return key;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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
}
