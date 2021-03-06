package com.vanderbilt.people.finder;

import java.util.List;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vanderbilt.people.finder.Provider.Constants;

/**
 * Class used for syncing with a central remote server.
 * The sync adapter is never invoked manually, instead it
 * is managed by a sync manager, a seperate system service 
 * that handles all sync adapters on the device. Applications 
 * can specifiy the periodicity of the sync adapter, request
 * immediate syncing, or set the sync adapter to sync 
 * "automatically." However, the sync manager has the final
 * call, and may delay syncing until a more appropriate time
 * (if there are other sync adapters already syncing and filling
 * up bandwidth, for example). 
 * <p>
 * Sync adapters are closely tied to accounts and content providers,
 * and are usually configured through their respective interfaces.
 * Sync adapters must also supply several configuration details
 * in the manifest. In this case, the configuration lies in 
 * res/xml/sync.xml.
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
	private static final String TAG = "SyncAdapter";
	private static final String UPDATED_PROVIDER_FILTER = "com.vanderbilt.people.finder.updated-provider-filter";
	
	public SyncAdapter(Context context, boolean autoInitialize)
	{
		super(context, autoInitialize);
	}

	/*
	 * Performs the meat of the syncing operation. For peer-to-peer 
	 * users, only the minimum required data is sent to the server. 
	 * Otherwise, the user's full data is sent. Afterwards, peer 
	 * data is pulled from the server. The pulling is similar to
	 * the pushing in that, for peer-to-peer, only the minimum 
	 * is pulled, while the other connection types receive full data.
	 */
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult)
	{	
		Cursor c = null;
		try
		{
			c = provider.query(Constants.CONTENT_URI, null,
					Constants.KEY+"="+UserData.getKey(getContext()), null, null);
		
			DataModel dataToSend = new DataModel();
			ConnectionType ct = UserData.getConnectionType(getContext());
			if (c.moveToFirst())
			{
				dataToSend.setKey(c.getLong(c.getColumnIndex(Constants.KEY)));
				dataToSend.setIpAddress(c.getString(c.getColumnIndex(Constants.ADDRESS)));
				dataToSend.setConnectionType(ConnectionType.getConnectionType(
						c.getString(c.getColumnIndex(Constants.CONN_TYPE))));
			
				if (ct != ConnectionType.PEER_TO_PEER)
				{
					dataToSend.setName(c.getString(c.getColumnIndex(Constants.NAME)));
					dataToSend.setStatus(c.getString(c.getColumnIndex(Constants.STATUS)));
					dataToSend.setLatitude(c.getDouble(c.getColumnIndex(Constants.LATITUDE)));
					dataToSend.setLongitude(c.getDouble(c.getColumnIndex(Constants.LONGITUDE)));
				}
			}
		
			// Send data to server, get peer data back
			Log.v(TAG, "Pushing ip address to server.");
			long key = NetworkUtilities.pushDataToServer(dataToSend, ct);
			Log.v(TAG, "returned key: " + key);
			
			Log.v(TAG, "Downloading peer ip addresses.");
			List<DataModel> returnedItems;
			if (ct == ConnectionType.PEER_TO_PEER)
			{
				returnedItems = NetworkUtilities.pullPeerAddresses(UserData.getKey(getContext())); 
			}
			else
			{
				returnedItems = NetworkUtilities.pullPeerData(UserData.getKey(getContext()), ct);
			}
		
			for (DataModel d : returnedItems)
			{
				c = provider.query(Constants.CONTENT_URI, new String[] { Constants.KEY },
								   Constants.KEY+"="+d.getKey(), null, null);
			
				ContentValues cv = d.toContentValues();
				if (!d.isMarkedRemoved() && c.getCount() == 0)
				{
					Uri uri = provider.insert(Constants.CONTENT_URI, cv);
					Log.v(TAG, "Inserted: " + uri.toString());
				}
				else if (d.isMarkedRemoved())
				{
					int i = provider.delete(Constants.CONTENT_URI, Constants.KEY+"="+d.getKey(), null);
					Log.v(TAG, "Deleted " + i + " item(s).");
				}
				else
				{
					int i = provider.update(Constants.CONTENT_URI, cv, Constants.KEY+"="+d.getKey(), null);
					Log.v(TAG, "Updated " + i + " item(s).");
				}
			}
			
			// Let the receiver in LocationsActivity know that the content
			// provider's data has changed.
			Log.v(TAG, "broadcasting");
			Intent intent = new Intent(UPDATED_PROVIDER_FILTER);
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
		}
		catch (Exception e)
		{
			Log.w(TAG, e.toString());
		}
		finally
		{
			if (c != null)
			{
				c.close();
			}
		}
	}
}
