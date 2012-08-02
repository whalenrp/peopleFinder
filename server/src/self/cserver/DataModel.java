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
	public static final String KEY = "key";
	public static final String IP = "ip";
	public static final String LAT = "lat";
	public static final String LONG = "long";
	public static final String STATUS = "status";
	public static final String NAME = "name";
	public static final String REMOVED = "removed";
	public static final String CONN_TYPE = "conn_type";
	
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
	
	@Persistent
	private ConnectionType connectionType;
	
	public DataModel()
	{
		// Nothing to do here.
	}
	
	/**
	 * The following fields are required: ip, conn_type.
	 * The remaining fields are optional depending on the
	 * connection type, and whether the data being held
	 * has a registered key yet.
	 * 
	 * @param o 
	 * @throws JSONException
	 */
	public DataModel(JSONObject o) throws JSONException
	{
		markedRemoved = false;
		
		// Data may not contain key if app is registering
		// for the first time. 
		if (o.has(KEY))
			key = o.getLong(KEY);
		
		// Required fields, will throw exception
		// if not included.
		ipAddress = o.getString(IP);
		connectionType = ConnectionType.getConnectionType(o.getString(CONN_TYPE));
		
		// The following are optional depending on the 
		// connection type.
			
		if (o.has(LAT))
			latitude = o.getDouble(LAT);
		if (o.has(LONG))
			longitude = o.getDouble(LONG);
		if (o.has(STATUS))
			status = o.getString(STATUS);
		if (o.has(NAME))
			name = o.getString(NAME);
	}
	
	/**
	 * Packages up the data held by the model into a standard
	 * JSON object. All fields are included, even if they are null. 
	 * The field 'key' is guaranteed to be unique as long as the model's
	 * represented entity has been stored in the App Engine database. 
	 * 
	 * @return JSON data object used by the org.json package
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject o = new JSONObject();
		
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

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}
}


/**
 * Enum used to replace raw strings in the DataModel object.
 * Allows compile-time type checking, making ConnectionType
 * values less error-prone than plain strings.
 */
enum ConnectionType
{
	CLIENT_SERVER,
	PEER_TO_PEER,
	MIXED;
	
	static ConnectionType getConnectionType(String name)
	{
		if (name.equals(PEER_TO_PEER.name()))
		{
			return PEER_TO_PEER;
		}
		else if (name.equals(MIXED.name()))
		{
			return MIXED;
		}
		else
		{
			return CLIENT_SERVER;
		}
	}
}
