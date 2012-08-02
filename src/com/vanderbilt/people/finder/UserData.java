package com.vanderbilt.people.finder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Wrapper class for accessing user settings data.
 */
public final class UserData 
{
	private static final String USER_DATA = "UserData";
	private static final String USER_KEY = "user_server_key";
	private static final String ACCOUNT_NAME = "account_name";
	private static final String NEEDS_INIT = "needs_init";
	private static final String CONNECTION_TYPE = "connection_type";
	
	private UserData() {}
	
	/**
	 * Sets the connection type for the app. The type of connection
	 * determines which peers the user can "see." 
	 * 
	 * @param context
	 * @param cType
	 */
	public static void setConnectionType(Context context, ConnectionType cType)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(CONNECTION_TYPE, cType.name());
		editor.commit();
	}
	
	/**
	 * Returns the app's current connection type.
	 * @param context
	 */
	public static ConnectionType getConnectionType(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		return ConnectionType.getConnectionType(
			   settings.getString(CONNECTION_TYPE, ConnectionType.CLIENT_SERVER.name()));
	}
	
	/**
	 * Returns whether or not the app needs to be initialized. 
	 * 
	 * @param context
	 * @return 
	 */
	public static boolean needsInitialization(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		return settings.getBoolean(NEEDS_INIT, true);
	}
	
	/**
	 * Set whether or not the app has performed first-run initialization.
	 * 
	 * @param context
	 */
	public static void establishInitialization(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		if (!settings.contains(NEEDS_INIT))
		{
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(NEEDS_INIT, false);
			editor.commit();
		}
	}
	
	/**
	 * Stores information pertaining to the Account associated 
	 * with the user. The account is necessary for syncing, and
	 * is created during the first-run initialization of the app.
	 * 
	 * @param context
	 * @param account Account created for the user.
	 */
	public static void establishAccount(Context context, Account account)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		if (!settings.contains(ACCOUNT_NAME))
		{
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(ACCOUNT_NAME, account.name);
			editor.commit();
		}
	}
	
	/**
	 * Returns the established account for the user. An account is 
	 * necessary for syncing with the central remote server via
	 * the sync adapter. Will return null if the account doesn't 
	 * exist.
	 * 
	 * @param context
	 * @return Account object for the user's registered account.
	 */
	public static Account getAccount(Context context)
	{ 
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		String accountName = settings.getString(ACCOUNT_NAME, null);
		AccountManager accountManager = AccountManager.get(context);
		for (Account a : accountManager.getAccountsByType(AccountConstants.ACCOUNT_TYPE))
		{
			if (accountName.equals(a.name))
				return a;
		}
		return null;
	}

	/**
	 * Establishes the key for the app user for easy access during content
	 * provider operations. The key will not be set if it has already been
	 * established once.
	 * 
	 * @param context
	 * @param key Returned from the server after registering the user.
	 */
	public static void establishKey(Context context, long key)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		if (!settings.contains(USER_KEY))
		{
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(USER_KEY, key);
			editor.commit();
		}
	}
	
	/**
	 * Returns the established key for the user. If a key has not been set, will return
	 * the value -1. The key should ALWAYS be established during primary initialization 
	 * of the app's first start-up.
	 * 
	 * @param context 
	 * @return The key, used for identification with the server, for the user. If not set, 
	 * will return -1. 
	 */
	public static long getKey(Context context)
	{ 
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		return settings.getLong(USER_KEY, -1);
	}
}
