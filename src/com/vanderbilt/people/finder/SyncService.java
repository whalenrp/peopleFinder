package com.vanderbilt.people.finder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service 
{
	private static final Object _syncAdapterLock = new Object();
	private static SyncAdapter _syncAdapter = null;
	
	public void onCreate()
	{
		synchronized (_syncAdapterLock)
		{
			if (_syncAdapter == null)
			{
				_syncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return _syncAdapter.getSyncAdapterBinder();
	}
}
