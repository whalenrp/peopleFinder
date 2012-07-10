package com.vanderbilt.people.finder;

import com.vanderbilt.people.finder.Provider.Constants;

import android.content.Intent;
import android.view.View;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public class MainActivity extends FragmentActivity
	implements LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String[] PROJECTION = new String[] { Constants.ID, Constants.NAME, Constants.IP };
	private static final String TAG = "MainActivity";
	
	private SimpleCursorAdapter mAdapter;
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
        
        nameLabel = (TextView)findViewById(R.id.name_label);
        statusEditText = (EditText)findViewById(R.id.edit_text_status);
        statusLabelText = (TextView)findViewById(R.id.status_text);
        
		mList = (ListView)findViewById(R.id.list);
		
		// Set up Adapter
		mAdapter = new SimpleCursorAdapter(this, 
			android.R.layout.simple_list_item_2, null, 
			new String[] {Constants.NAME, Constants.IP}, 
			new int[] {android.R.id.text1, android.R.id.text2}, 0);
		mList.setAdapter(mAdapter);
		

		Cursor c = getContentResolver().query(Constants.CONTENT_URI,
				  new String[] { Constants.NAME, Constants.MESSAGE },
				  Constants.SERVER_KEY+"="+UserData.getId(MainActivity.this), null, null);
    	if (c.moveToFirst())
    	{
    		nameLabel.setText(c.getString(c.getColumnIndex(Constants.NAME)));
    		statusLabelText.setText(c.getString(c.getColumnIndex(Constants.MESSAGE)));
    	}
		c.close();		
		getSupportLoaderManager().initLoader(0, null, this);
    }
    
	// Called when the button at the bottom of the screen is clicked
	public void launchMap(View view){
		startActivity( new Intent(this, LocationsActivity.class) );
	}
	
	// Called when button next to status view is pressed.
	public void submitStatus(View view)
	{
		String status = statusEditText.getText().toString();
		if (!status.equals(""))
		{
			ContentValues cv = new ContentValues(1);
			cv.put(Constants.MESSAGE, status);
			int i = getContentResolver().update(Constants.CONTENT_URI, cv,
					Constants.SERVER_KEY+"="+UserData.getId(this), null);
			Log.v(TAG, "Updated status for " + i + " item(s).");
			statusLabelText.setText(status);
			statusEditText.setText("");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args){
		return new CursorLoader(this, 
			Constants.CONTENT_URI,// URI
			PROJECTION,// needed fields: _id, username, and IP
			Constants.SERVER_KEY+"!="+UserData.getId(this), // Selection : get all peers
			null, // SelectionArgs
			Constants.DEFAULT_SORT_ORDER); // ORDER BY 
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