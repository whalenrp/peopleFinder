package com.vanderbilt.people.finder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vanderbilt.people.finder.Provider.Constants;

/**
 * Main activity for the app. This activity is the default 
 * starting point after a user has registered. It contains 
 * a list of peer IP addresses, and a method to change the
 * user's status. Also, a button is present to send the user
 * to the map activity.
 * 
 * All content is loaded from the content provider. The 
 * activity makes use of a Loader to automatically refresh
 * the list of peers whenever the content provider's data
 * is updated. This usually occurs when a syncing action
 * is performed in the background by the Sync Manager.
 *
 */
public class MainActivity extends FragmentActivity
	implements LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = "MainActivity";
	private static final String[] PROJECTION = new String[] { Constants.KEY,
															  Constants.NAME,
															  Constants.STATUS,
															  Constants.ADDRESS };
	private CursorAdapter mAdapter;
	private ListView mList;
	private TextView nameLabel;
	private EditText statusEditText;
	private TextView statusLabelText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (UserData.getConnectionType(this) != ConnectionType.CLIENT_SERVER)
        {
        	Log.v(TAG, "Starting LocationResponder service");
        	startService(new Intent(this, LocationResponder.class));
        }
        
        nameLabel = (TextView)findViewById(R.id.name_label);
        statusEditText = (EditText)findViewById(R.id.edit_text_status);
        statusLabelText = (TextView)findViewById(R.id.status_text);
		
     // Set up List and Adapter
        mList = (ListView)findViewById(R.id.list);
		mAdapter = new MainCursorAdapter(this);
		mList.setAdapter(mAdapter);
		

		// Pulls user information from the content provider to populate the
		// various views of the activity. 
		Cursor c = getContentResolver().query(Constants.CONTENT_URI,
				  new String[] { Constants.NAME, Constants.STATUS },
				  Constants.KEY+"="+UserData.getKey(MainActivity.this), null, null);
    	if (c.moveToFirst())
    	{
    		nameLabel.setText(c.getString(c.getColumnIndex(Constants.NAME)));
    		statusLabelText.setText(c.getString(c.getColumnIndex(Constants.STATUS)));
    	}
		c.close();		
		getSupportLoaderManager().initLoader(0, null, this);
    }
    
	// Called when the button at the bottom of the screen is clicked
	public void launchMap(View view)
	{
		startActivity(new Intent(this, LocationsActivity.class));
	}
	
	// Called when button next to status view is pressed. Updates
	// status column for user in the content provider, then updates
	// the UI.
	public void submitStatus(View view)
	{
		String status = statusEditText.getText().toString();
		if (!status.equals(""))
		{
			ContentValues cv = new ContentValues(1);
			cv.put(Constants.STATUS, status);
			int i = getContentResolver().update(Constants.CONTENT_URI, cv,
					Constants.KEY+"="+UserData.getKey(this), null);
			Log.v(TAG, "Updated status for " + i + " item(s).");
			statusLabelText.setText(status);
			statusEditText.setText("");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		// Loader will pull all rows from content provider besides the 
		// user's own data. From these rows, only the _id and IP address
		// columns will be returned. The _id column is used internally
		// by ListAdapters to manage their data. 
		return new CursorLoader(this, Constants.CONTENT_URI, PROJECTION, 
							    Constants.KEY+"!="+UserData.getKey(this),
							    null, Constants.DEFAULT_SORT_ORDER); 
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data){
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader){
		mAdapter.swapCursor(null);
	}
}