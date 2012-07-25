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

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
	private static final String TAG = "SyncAdapter";
	
	public SyncAdapter(Context context, boolean autoInitialize)
	{
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult)
	{	
		Cursor c = null;
		try
		{
			c = provider.query(Constants.CONTENT_URI,
							   new String[] { Constants.SERVER_KEY, Constants.IP },
							   Constants.SERVER_KEY+"="+UserData.getId(getContext()), null, null);
		
			DataModel dataToSend = null;
			if (c.moveToFirst())
			{
				dataToSend = new DataModel(c.getLong(c.getColumnIndex(Constants.SERVER_KEY)));
				dataToSend.setIpAddress(c.getString(c.getColumnIndex(Constants.IP)));
			}
		
			// Send data to server, get peer data back
			Log.v(TAG, "Pushing ip address to server.");
			long key = NetworkUtilities.pushClientStatus(dataToSend);
			Log.v(TAG, "returned key: " + key);
			Log.v(TAG, "Downloading peer ip addresses.");
			List<DataModel> returnedItems = NetworkUtilities.requestIpAddresses(UserData.getId(getContext()));
		
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
