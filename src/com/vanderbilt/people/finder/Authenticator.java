package com.vanderbilt.people.finder;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class handles the addition and managing of the
 * accounts for this app (specifically, the account type
 * specified in AccountConstants). 
 * <p>
 * It should be noted that as of this time, there exists 
 * incomplete support for interacting with the user's 
 * account in the Settings app. In fact, there is 
 * currently no way to add an additional account (which
 * would be unnecessary unless the original was first 
 * removed). The user is able, however, to turn on/off 
 * the syncing service.
 */
public class Authenticator extends AbstractAccountAuthenticator 
{
	private static final String TAG = "Authenticator";
	private final Context _context;
	
	public Authenticator(Context context) 
	{
		super(context);
		_context = context;
	}
	
	/*
	 * Specifies functionality for adding an account. Currently, spawns the
	 * StartupActivity class, which will crash Settings since it was not
	 * designed to be invoked outside of the application's context. 
	 */
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
			String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException 
	{
		Log.e(TAG, "Settings will crash!");
		final Intent intent = new Intent(_context, StartupActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException 
	{
		Log.v(TAG, "confirmCredentials");
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) 
	{
		Log.v(TAG, "editProperties()");
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
			String authTokenType, Bundle loginOptions) throws NetworkErrorException 
	{
		// Will not make use of auth tokens 
		Log.v(TAG, "getAuthToken()");
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) 
	{
		Log.v(TAG, "getAuthTokenLabel()");
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
			String[] features) throws NetworkErrorException 
	{
		Log.v(TAG, "hasFeatures()");
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle loginOptions)
			throws NetworkErrorException 
	{
		Log.v(TAG, "updateCredentials()");
		return null;
	}

}
