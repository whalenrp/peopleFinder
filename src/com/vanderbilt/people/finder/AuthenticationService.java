package com.vanderbilt.people.finder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Spawns the specific AccountAuthenticator. This service is
 * instantiated when the user attempts to add a new account 
 * in Settings. 
 * <p>
 * Additional required configuration is found in res/xml/auth.xml
 */
public class AuthenticationService extends Service 
{
	private static final String TAG = "AuthenticationService";
	private Authenticator _authenticator;
	
	public void onCreate()
	{
		Log.v(TAG, "SyncAdapter authentication service started.");
		_authenticator = new Authenticator(this);
	}
	
	public void onDestroy()
	{
		Log.v(TAG, "SyncAdapter authentication service stopped.");
	}
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		Log.v(TAG, "getBinder()...  returning the AccountAuthenticator binder for intent " + intent);
		return _authenticator.getIBinder();
	}
}
