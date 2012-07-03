package com.vanderbilt.people.finder;

import java.util.List;

import com.vanderbilt.people.finder.Provider.Constants;

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
			new String[] {Constants.NAME, Constants.IP}, // replace these
			new int[] {android.R.id.text1, android.R.id.text2}, 0);
		mList.setAdapter(mAdapter);

		getSupportLoaderManager().initLoader(0, null, this);
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args){
		/* Fill in these parameters */
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
			for (DataModel d : list)
			{
				Cursor c = getContentResolver().query(Constants.CONTENT_URI,
										   new String[] { Constants.SERVER_KEY },
										   Constants.SERVER_KEY+"="+d.getKey(), null, null);
				
				ContentValues cv = new ContentValues(6);
				cv.put(Constants.SERVER_KEY, d.getKey());
				cv.put(Constants.NAME, d.getName());
				cv.put(Constants.IP, d.getIpAddress());
				cv.put(Constants.LATITUDE, d.getLatitude());
				cv.put(Constants.LONGITUDE, d.getLongitude());
				cv.put(Constants.MESSAGE, d.getStatus());
				
				if (c.getCount() == 0)
				{
					Uri uri = getContentResolver().insert(Constants.CONTENT_URI, cv);
					Log.i(TAG, "Inserted: " + uri.toString());
				}
				else
				{
					int i = getContentResolver().update(Constants.CONTENT_URI, cv,
														Constants.SERVER_KEY+"="+d.getKey(), null);
					Log.i(TAG, "Updated " + i + "item(s).");
				}
			}
		}
	}

}
