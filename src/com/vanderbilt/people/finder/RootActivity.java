package com.vanderbilt.people.finder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * The first activity launched when the app begins. Having 
 * no interface, it acts as a 'trampoline,' sending the user 
 * to the initialization activity on the first launch, or to 
 * the usual main activity otherwise. 
 *
 */
public class RootActivity extends Activity 
{
	private static final String TAG = "RootActivity";
	private static final int INIT_TAG = 1;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
	    if (UserData.needsInitialization(getApplicationContext()))
	    {
	    	Log.v(TAG, "launching startup activity");
	    	Intent i = new Intent(RootActivity.this, StartupActivity.class);
	    	
	    	// Allow the use of a callback when started activity finishes.
	    	startActivityForResult(i, INIT_TAG);
	    }
	    else
	    {
	    	Log.v(TAG, "starting main activity");
	    	startActivity(new Intent(this, MainActivity.class));
	    	finish();
	    }
	}
	
	/*
	 * When the startup activity finishes, it sends an intent back to its
	 * caller, this class. This permits a callback mechanism, allowing the
	 * class to perform further action as a result of the finished startup. 
	 * In this case, if the startup activity finishes successfully, the class
	 * will then start the main activity and call finish on itself.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	if (requestCode == INIT_TAG && resultCode == RESULT_OK)
    	{
    		Log.v(TAG, "starting main activity");
    		startActivity(new Intent(this, MainActivity.class));
    		finish();
    	}
    }
}
