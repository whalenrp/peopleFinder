package com.vanderbilt.people.finder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

public final class UserData 
{
	private static final String USER_DATA = "UserData";
	private static final String USER_ID = "user_server_key";
	private static final String ACCOUNT_NAME = "account_name";
	private static final String NEEDS_INIT = "needs_init";
	
	// Non-instantiable, it's a singleton.
	private UserData() {}
	
	public static boolean needsInitialization(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		return settings.getBoolean(NEEDS_INIT, true);
	}
	
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
	 * Establishes the id for the app user for easy access during Content
	 * Provider operations. If the id has already been set, it will ignore
	 * this call. 
	 * 
	 * @param context
	 * @param id
	 */
	public static void establishId(Context context, long id)
	{
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		if (!settings.contains(USER_ID))
		{
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(USER_ID, id);
			editor.commit();
		}
	}
	
	/**
	 * Returns the established id for the user. If an id has not been set, will return
	 * the value -1. The id should ALWAYS be established during primary initialization 
	 * of the app's first start-up.
	 * 
	 * @param context
	 * @return
	 */
	public static long getId(Context context)
	{ 
		SharedPreferences settings = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
		return settings.getLong(USER_ID, -1);
	}
}
