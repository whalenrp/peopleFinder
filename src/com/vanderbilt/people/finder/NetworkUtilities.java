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
 * Static class used to interface with the central server.
 * 
 * @author lane
 *
 */
public final class NetworkUtilities 
{
	private static final String TAG = "NetworkUtilities";
	
	private NetworkUtilities() {}
	
	private static final int PEER_PORT = 5567;
	
	private static final String BASE_URL = "http://vuandroidserver.appspot.com";
	private static final String DOWNLOAD = "/download";
	private static final String UPLOAD = "/upload";
	private static final String REMOVE = "/remove";
	private static final String IP = "/ip";
	
	private static final String GET_PARAM_SKEY = "?skey=";
	private static final String POST_PARAM_JSON_PACKAGE = "json_package";
	private static final String POST_PARAM_REMOVAL_KEY = "removal_key";
	
	private static HttpClient getHttpClient()
	{
		HttpClient httpClient = new DefaultHttpClient();
        final HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000);
        HttpConnectionParams.setSoTimeout(params, 30 * 1000);
        ConnManagerParams.setTimeout(params, 30 * 1000);
        return httpClient;
	}
	
	public static void pushUpdateToPeers(DataModel d, List<String> ipAddresses)
	{
		// initialize Connect objects for every IP in the database
		ArrayList<Connection> mPeers = new ArrayList<Connection>();
		for (String ip : ipAddresses)
		{
			mPeers.add(new Connection(ip));
		}
		
		String delivery = "";
		try
		{
			delivery = d.toJSON().toString();
		}
		catch (JSONException e)
		{
			Log.e(TAG, e.toString());
		}
		
		for (Connection conn : mPeers)
		{
			if (conn.open())
			{
				conn.putString(delivery);
			}
			conn.close();
		}
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
				e.printStackTrace();
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
	
	public static String getMyExternalIp()
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
        	e.printStackTrace();
        }
		
		return null;
	}
	
	public static List<DataModel> requestIpAddresses(Long key)
	{
		String urlFull = BASE_URL + IP;
		if (key != null)
			urlFull += GET_PARAM_SKEY + key;
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
				Long skey = o.getLong("skey");
				String ipAddr = o.getString("ip");
				DataModel dataModel = new DataModel(skey, ipAddr);
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
	
	public static boolean requestRemoval(Long key)
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
	 * Send the client's data to the server database for 
	 * processing. This method allows either insertion 
	 * or updating of the client data. In order to update
	 * the server key of the client must be included in
	 * the parameter object. 
	 * 
	 * @param d
	 * @return the server key established for this client
	 */
	public static Long pushClientStatus(DataModel d) 
	{
		HttpClient httpClient = getHttpClient();
		String urlFull = BASE_URL + UPLOAD;
		Long returnedSkey = Long.valueOf(-1);
	
		try
		{	
			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(POST_PARAM_JSON_PACKAGE, d.toJSON().toString()));
			HttpEntity entity = new UrlEncodedFormEntity(params);
			
			final HttpPost httpPost = new HttpPost(urlFull);
			httpPost.addHeader(entity.getContentType());
	        httpPost.setEntity(entity);
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        String responseBody = httpClient.execute(httpPost, responseHandler);
			JSONObject o = new JSONObject(responseBody);
			returnedSkey = o.getLong("skey");
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
	 * Pulls all peer updates from the server database.
	 * 
	 * @param key used to determine which client is 
	 * requesting data. Since obtaining its own data would be
	 * redundant, the client's data is not returned. If null, 
	 * all data is returned, regardless of owner.
	 * @return
	 */
	public static List<DataModel> getPeerUpdates(Long key)
	{
		String urlFull = BASE_URL + DOWNLOAD;
		if (key != null)
			urlFull += GET_PARAM_SKEY + key;
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
				DataModel obj = new DataModel(array.getJSONObject(i));
				objectsReturned.add(obj);
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
}
