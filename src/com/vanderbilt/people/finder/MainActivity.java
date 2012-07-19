package com.vanderbilt.people.finder;

import java.io.IOException;

import com.vanderbilt.people.finder.Provider.Constants;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
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
	private Button leaveNetworkButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        nameLabel = (TextView)findViewById(R.id.name_label);
        statusEditText = (EditText)findViewById(R.id.edit_text_status);
        statusLabelText = (TextView)findViewById(R.id.status_text);
        leaveNetworkButton = (Button)findViewById(R.id.btn_leave);
        
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
    
    // Called when leave network button is pressed.
    public void leaveNetwork(View view)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Are you sure you want to leave? This will remove both you local and server accounts.")
    		   .setCancelable(true)
    		   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
    		   {
    			   public void onClick(DialogInterface dialog, int which) 
    			   {
    				   dialog.cancel();
    			   }
    		   })
    		   .setPositiveButton("Leave", new DialogInterface.OnClickListener() 
    		   {
    			   public void onClick(DialogInterface dialog, int which) 
    			   {
    				   new LeaveNetworkTask().execute();
    				   dialog.dismiss();
    			   }
    		   });
    	AlertDialog alert = builder.create();
    	alert.show();
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
	
	private class LeaveNetworkTask extends AsyncTask<Void, Void, Boolean>
	{
		private ProgressDialog progress;
		
		protected void onPreExecute()
		{
			progress = new ProgressDialog(MainActivity.this);
			progress.setMessage("Unregistering...");
			progress.show();
		}
		
		protected Boolean doInBackground(Void... params) 
		{
			boolean network = NetworkUtilities.requestRemoval(UserData.getId(getApplicationContext()));
			Log.v(TAG, "removed from network: " + network);
			AccountManagerFuture<Boolean> removal = AccountManager.get(getApplicationContext())
					.removeAccount(UserData.getAccount(getApplicationContext()), null, null);
			boolean account = false;
			try 
			{
				account = removal.getResult();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			Log.v(TAG, "removed account: " + account);
			
			return network && account;
		}
		
		protected void onPostExecute(Boolean b)
		{
			progress.dismiss();
			if (b)
			{
				leaveNetworkButton.setEnabled(false);
				Toast.makeText(getApplicationContext(), "Left network", Toast.LENGTH_SHORT).show();
			}
			else
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
				builder.setMessage("Error unregistering from network. Please try again.")
				       .setCancelable(false)
				       .setPositiveButton("Ok", new DialogInterface.OnClickListener() 
				       {
				    	   public void onClick(DialogInterface dialog, int which) 
				    	   {
				    		   dialog.dismiss();
				    	   }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
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