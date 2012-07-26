package com.vanderbilt.people.finder;

import java.util.List;

import com.vanderbilt.people.finder.Provider.Constants;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

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
	
	public SyncAdapter(Context context, boolean autoInitialize)
	{
		super(context, autoInitialize);
	}

	/**
	 * Performs the meat of the syncing operation. User's data 
	 * (in the case of peer-to-peer, only key and IP address) is 
	 * packaged and sent to the central remote server. Afterwards, all
	 * peer IP addresses registered on the server are pulled down
	 * and stored.
	 */
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult)
	{	
		Cursor c = null;
		try
		{
			c = provider.query(Constants.CONTENT_URI,
							   new String[] { Constants.SERVER_KEY, Constants.IP },
							   Constants.SERVER_KEY+"="+UserData.getKey(getContext()), null, null);
		
			DataModel dataToSend = null;
			if (c.moveToFirst())
			{
				dataToSend = new DataModel(c.getLong(c.getColumnIndex(Constants.SERVER_KEY)));
				dataToSend.setIpAddress(c.getString(c.getColumnIndex(Constants.IP)));
			}
		
			// Send data to server, get peer data back
			Log.v(TAG, "Pushing ip address to server.");
			long key = NetworkUtilities.pushDataToServer(dataToSend);
			Log.v(TAG, "returned key: " + key);
			Log.v(TAG, "Downloading peer ip addresses.");
			List<DataModel> returnedItems = NetworkUtilities.pullPeerAddresses(UserData.getKey(getContext()));
		
			for (DataModel d : returnedItems)
			{
				c = provider.query(Constants.CONTENT_URI, new String[] { Constants.SERVER_KEY },
								   Constants.SERVER_KEY+"="+d.getKey(), null, null);
			
				ContentValues cv = d.toContentValues();
				if (!d.isMarkedRemoved() && c.getCount() == 0)
				{
					Uri uri = provider.insert(Constants.CONTENT_URI, cv);
					Log.v(TAG, "Inserted: " + uri.toString());
				}
				else if (d.isMarkedRemoved())
				{
					int i = provider.delete(Constants.CONTENT_URI, Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.v(TAG, "Deleted " + i + " item(s).");
				}
				else
				{
					int i = provider.update(Constants.CONTENT_URI, cv, Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.v(TAG, "Updated " + i + " item(s).");
				}
			}
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
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
