package com.vanderbilt.people.finder;

import android.app.ListActivity;
import android.os.Bundle;

public class MainActivity extends ListActivity
	implements LoaderManager.LoaderCallbacks<Cursor>
{
	private SimpleCursorAdapter mAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		// Set up Adapter
		mAdapter = new SimpleCursorAdapter(getActivity(), 
			android.R.layout.simple_list_item_2, null, 
			new String[] {Contract.USERNAME, Contract.IP_ADDRESS},
			new int[] {android.R.id.text1, android.R.id.text2}, 0);
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);	
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args){
		return new CursorLoader(this, 
			Contract.URI,// URI
			projection,
			selection,
			seletionArgs,
			sortOrder);
			
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
