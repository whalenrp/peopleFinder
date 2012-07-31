package com.vanderbilt.people.finder;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Static class used to interface with both central and 
 * peer-to-peer networks. The aim is to isolate all code
 * relating to network functions, including JSON parsing.
 */
public final class NetworkUtilities 
{
	private static final String TAG = "NetworkUtilities";
	
	private static final int PEER_PORT = 5567;
	
	private static final String BASE_URL = "http://vuandroidserver.appspot.com";
	private static final String DOWNLOAD = "/download";
	private static final String UPLOAD = "/upload";
	private static final String REMOVE = "/remove";

	private static final String IP = "/ip";
	
	private static final String GET_PARAM_SKEY = DataModel.KEY;
	private static final String GET_PARAM_CONN_TYPE = DataModel.CONN_TYPE;
	
	private static final String POST_PARAM_JSON_PACKAGE = "json_package";
	private static final String POST_PARAM_REMOVAL_KEY = "removal_key";
	
	
	private NetworkUtilities() {}
	
	private static HttpClient getHttpClient()
	{
		HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
        ConnManagerParams.setTimeout(params, 30 * 1000);
        return httpClient;
	}
	
	/**
	 * Grabs the IP address of the device that is connectable 
	 * from the network. May be performed on the main thread.
	 * 
	 * @return Either IPv4 or IPv6 address string.
	 */
	public static String getIp()
	{
		try
	    {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	    	while (en.hasMoreElements())
	    	{
	    		NetworkInterface intf = en.nextElement();
	    		Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
	    		while (enumIpAddr.hasMoreElements())
	    		{
	    			InetAddress inetAddress = enumIpAddr.nextElement();
	    			if (!inetAddress.isLoopbackAddress())
	    			{
	    				return inetAddress.getHostAddress().toString();    				
	    			}
	    		}
	    	}
	    }
	    catch (Exception e)
	    {
	    	Log.w(TAG, "Error while obtaining IP address.");
	    	e.printStackTrace();
	    }
		
		return null;
	}

	/**
	 * Pulls peer IP addresses from central server. Must not be 
	 * performed on the main thread.
	 * 
	 * @param key User's identifying key. Used to avoid returning 
	 * user's own data with peer data.
	 * @return List containing peer keys and IP addresses. Will never be null.
	 */
	public static List<DataModel> pullPeerAddresses(Long key)
	{
		String urlFull = BASE_URL + IP;
		if (key != null)
			urlFull += "?" + GET_PARAM_SKEY + "=" + key;
		HttpClient httpClient = getHttpClient();
		List<DataModel> objectsReturned = new ArrayList<DataModel>();
		try
		{
			final HttpGet httpget = new HttpGet(urlFull);
			Log.v(TAG, "executing request " + httpget.getURI());
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpClient.execute(httpget, responseHandler);
			Log.v(TAG, responseBody);
			JSONArray array = new JSONArray(responseBody);
			
			for (int i = 0; i < array.length(); i++)
			{
				JSONObject o = array.getJSONObject(i);
				DataModel dataModel = new DataModel();
				dataModel.setKey(o.getLong(DataModel.KEY));
				dataModel.setIpAddress(o.getString(DataModel.IP));
				
				objectsReturned.add(dataModel);
			}
		}
		catch (Exception e)
		{
			
			e.printStackTrace();
		}
		finally
		{
			httpClient.getConnectionManager().shutdown();
		}
		
		return objectsReturned;
	}
	
	/**
	 * Pulls all peer updates from the server database.
	 * 
	 * @param key used to determine which client is 
	 * requesting data. Since obtaining its own data would be
	 * redundant, the client's data is not returned. If null, 
	 * all data is returned, regardless of owner.
	 * @return
	 */
	public static List<DataModel> pullPeerData(Long key, ConnectionType ct)
	{
		String urlFull = BASE_URL + DOWNLOAD;
		urlFull += "?" + GET_PARAM_SKEY + "=" + key + "&" + GET_PARAM_CONN_TYPE + "=" + ct.name();
		HttpClient httpClient = getHttpClient();
		List<DataModel> objectsReturned = new ArrayList<DataModel>();
		try
		{
			final HttpGet httpget = new HttpGet(urlFull);
			Log.i(TAG, "executing request " + httpget.getURI());

			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpClient.execute(httpget, responseHandler);
			Log.d(TAG, responseBody);
			JSONArray array = new JSONArray(responseBody);

			for (int i = 0; i < array.length(); i++)
			{
				DataModel obj = new DataModel(array.getJSONObject(i));
				objectsReturned.add(obj);
			}
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
		finally
		{
			httpClient.getConnectionManager().shutdown();
		}

		return objectsReturned;
	}

	/**
	 * Pushes supplied data to all given peers. This method will
	 * give no indication of failure beyond logging exceptions. It
	 * assumes that all peers are reachable and accepting connections. 
	 * If either parameter is null, the method will return early. Must
	 * not be performed on the main thread.
	 * @param d The data to be sent to peers. Assumes all fields are present and valid.
	 * @param ipAddresses List of peers as given by their IP addresses. 
	 * Accepts IPv4 and IPv6 address formats. 
	 */
	public static void pushDataToPeers(DataModel d, List<String> ipAddresses)
	{
		if (d == null || ipAddresses == null)
		{
			Log.w(TAG, "Null parameter(s) passed to pushDataToPeers()");
			return;
		}
			
		// initialize Connection objects for every IP in the database
		ArrayList<Connection> mPeers = new ArrayList<Connection>();
		for (String ip : ipAddresses)
		{
			mPeers.add(new Connection(ip));
		}
		
		try
		{
			String delivery = d.toJSON().toString();
			
			for (Connection conn : mPeers)
			{
				if (conn.open())
				{
					conn.putString(delivery);
				}
				conn.close();
			}
		}
		catch (JSONException e)
		{
			Log.e(TAG, e.toString());
			Log.e(TAG, "Invalid JSON, was not sent to peers.");
		}
	}
	
	/**
	 * Send the client's data to the server database for 
	 * processing. This method allows either insertion 
	 * or updating of the client data. In order to update
	 * the server key of the client must be included in
	 * the parameter object. Must not be performed on the main thread.
	 * 
	 * @param d Data to be sent to the server. In peer-to-peer implementations,
	 * only an IP address (and key, if updating) is necessary. In a central 
	 * server model, all data should be included.
	 * @return The server key established for this client. 
	 * Will return -1 if the transaction was unsuccessful.
	 */
	public static Long pushDataToServer(DataModel d, ConnectionType ct) 
	{
		HttpClient httpClient = getHttpClient();
		String urlFull = BASE_URL + UPLOAD;
		Long returnedSkey = Long.valueOf(-1);
		try
		{	
			String paramString = "";
			if (ct == ConnectionType.PEER_TO_PEER)
			{
				JSONObject tmp = new JSONObject();
				if (d.getKey() != null)
				{
					tmp.put(DataModel.KEY, d.getKey());
				}
				tmp.put(DataModel.IP, d.getIpAddress());
				tmp.put(DataModel.REMOVED, d.isMarkedRemoved());
				tmp.put(DataModel.CONN_TYPE, d.getConnectionType());
				paramString = tmp.toString();
			}
			else
			{
				paramString = d.toJSON().toString();
			}
			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(POST_PARAM_JSON_PACKAGE, paramString));
			HttpEntity entity = new UrlEncodedFormEntity(params);
			
			final HttpPost httpPost = new HttpPost(urlFull);
			httpPost.addHeader(entity.getContentType());
	        httpPost.setEntity(entity);
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        String responseBody = httpClient.execute(httpPost, responseHandler);
			JSONObject o = new JSONObject(responseBody);
			returnedSkey = o.getLong(DataModel.KEY);
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
		}
		finally
		{
			httpClient.getConnectionManager().shutdown();
		}
		
		return returnedSkey;
	}

	/**
	 * Attempts to remove the user from the network. If successful,
	 * the server will no longer list the user's IP address in the 
	 * directory table. Must not be performed on the main thread.
	 * 
	 * @param key User's identification key.
	 * @return Whether the removal was successful or not. 
	 */
	public static boolean requestRemovalFromServer(Long key)
	{
		String urlFull = BASE_URL + REMOVE;

		HttpClient httpClient = getHttpClient();
		try
		{
			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(POST_PARAM_REMOVAL_KEY, key.toString()));
			HttpEntity entity = new UrlEncodedFormEntity(params);
			
			final HttpPost httpPost = new HttpPost(urlFull);
			httpPost.addHeader(entity.getContentType());
		    httpPost.setEntity(entity);
		    ResponseHandler<String> responseHandler = new BasicResponseHandler();
		    String responseBody = httpClient.execute(httpPost, responseHandler);
			JSONObject o = new JSONObject(responseBody);
			return o.getBoolean("removed");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			httpClient.getConnectionManager().shutdown();
		}
		
		return false;
	}
	
	/**
	 * Private helper class for managing individual connections.
	 * Holds a connection open and can send strings through the 
	 * connection if it has been established.
	 */
	private static class Connection
	{
		private final String ipAddress;
		private Socket socket; 
		private PrintStream outStream;
		private boolean valid;
	
		public Connection(String ip)
		{
			socket = null;
			outStream = null;
			ipAddress = ip;
			valid=false;
			Log.v(TAG, "Created connection with ip: " + ip);
		}
	
		public boolean open()
		{
			try
			{
				socket = new Socket(ipAddress, PEER_PORT);
				outStream = new PrintStream(socket.getOutputStream());
				valid = true;
				return true;
			}
			catch(Exception e)
			{
				Log.w(TAG, e.toString());
				valid=false;
				return false;
			}
		}
	
		public void putString(String s)
		{
			if (valid && socket != null && outStream != null)
				outStream.println(s);
		}
	
		public void close()
		{
			if (outStream != null)
			{
				outStream.close();
			}
			if (socket != null)
			{
				try
				{
					socket.close();
				}
				catch(IOException e)
				{
					Log.w(TAG, e.toString());
				}
			}
			valid = false;
		}
	}
}
