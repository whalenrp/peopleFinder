package com.vanderbilt.people.finder;

import android.content.Context;
import android.content.SharedPreferences;

public final class UserId 
{
	private static final String USER_ID = "user_server_key";
	// Non-instantiable, it's a singleton.
	private UserId() {}

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
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
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
		SharedPreferences settings = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
		return settings.getLong(USER_ID, -1);
	}
}
