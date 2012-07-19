package com.vanderbilt.people.finder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

public final class UserData 
{
	private static final String USER_ID = "user_server_key";
	private static final String ACCOUNT_NAME = "account_name";
	
	// Non-instantiable, it's a singleton.
	private UserData() {}
	
	public static boolean needsInitialization(Context context)
	{
		return (getAccount(context) == null && getId(context) == null);
	}
	
	public static void clearAccount(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(ACCOUNT_NAME, null);
		editor.commit();
	}
	
	public static void clearId(Context context)
	{
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(USER_ID, -1);
		editor.commit();
	}
	
	public static void setAccount(Context context, Account account)
	{
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(ACCOUNT_NAME, account.name);
		editor.commit();
	}
	
	/**
	 * Will return null if the Account isn't found.
	 * @param context
	 * @return
	 */
	public static Account getAccount(Context context)
	{ 
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		String accountName = settings.getString(ACCOUNT_NAME, null);
		AccountManager accountManager = AccountManager.get(context);
		if (accountName != null)
		{
			for (Account a : accountManager.getAccountsByType(AccountConstants.ACCOUNT_TYPE))
			{
				if (accountName.equals(a.name))
					return a;
			}
		}
		
		return null;
	}

	/**
	 * Sets the id for the app user for easy access during Content
	 * Provider operations.
	 * this call. 
	 * 
	 * @param context
	 * @param id
	 */
	public static void setId(Context context, long id)
	{
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(USER_ID, id);
		editor.commit();
	}
	
	/**
	 * Returns the established id for the user. If an id has not been set, will return
	 * the value -1. The id should ALWAYS be established during primary initialization 
	 * of the app's first start-up.
	 * 
	 * @param context
	 * @return
	 */
	public static Long getId(Context context)
	{ 
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		Long id = settings.getLong(USER_ID, -1);
		if (id == -1)
		{
			return null;
		}
		else
		{
			return id;
		}
	}
}
