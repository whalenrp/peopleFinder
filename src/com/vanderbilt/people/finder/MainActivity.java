package com.vanderbilt.people.finder;

import java.util.List;

import com.vanderbilt.people.finder.Provider.Constants;

import android.content.Intent;
import android.view.View;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ListView;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends FragmentActivity
	implements LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String[] PROJECTION = new String[] { Constants.ID, Constants.NAME, Constants.IP };
	private static final String TAG = "MainActivity";
	
	private DownloadDataTask downloader = new DownloadDataTask();
	private SimpleCursorAdapter mAdapter;
	private ListView mList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mList = (ListView)findViewById(R.id.list);

		// Set up Adapter
		mAdapter = new SimpleCursorAdapter(this, 
			android.R.layout.simple_list_item_2, null, 
			new String[] {Constants.NAME, Constants.IP}, 
			new int[] {android.R.id.text1, android.R.id.text2}, 0);
		mList.setAdapter(mAdapter);
		
		downloader.execute();

		getSupportLoaderManager().initLoader(0, null, this);
    }

	// Called when the button at the bottom of the screen is clicked
	public void launchMap(View view){
		startActivity( new Intent(this, LocationsActivity.class) );
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args){
		return new CursorLoader(this, 
			Constants.CONTENT_URI,// URI
			PROJECTION,// needed fields: _id, username, and IP
			null, // Selection : null defaults to all entries
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
	
	private class DownloadDataTask extends AsyncTask<Void, Void, List<DataModel>>
	{
		protected List<DataModel> doInBackground(Void... params) 
		{
			// We don't have our own data key yet, so pass null.
			return NetworkUtilities.getPeerUpdates(null);
		}
		
		protected void onPostExecute(List<DataModel> list)
		{
			Log.i("MainActivity", "List size: " + list.size());
			for (DataModel d : list)
			{
				
				Cursor c = getContentResolver().query(Constants.CONTENT_URI,
										   new String[] { Constants.SERVER_KEY },
										   Constants.SERVER_KEY+"="+d.getKey(), null, null);
				
				
				
				ContentValues cv = d.toContentValues();
				if (c.getCount() == 0)
				{
					Uri uri = getContentResolver().insert(Constants.CONTENT_URI, cv);
					Log.v(TAG, "Inserted: " + uri.toString());
				}
				else if (d.isMarkedRemoved())
				{
					int i = getContentResolver().delete(Constants.CONTENT_URI,
														Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.v(TAG, "Deleted " + i + "item(s).");
				}
				else
				{
					int i = getContentResolver().update(Constants.CONTENT_URI, cv,
														Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.v(TAG, "Updated " + i + "item(s).");
				}
				
				c.close();
			}
		}
	}
}
