package com.vanderbilt.people.finder;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vanderbilt.people.finder.Provider.Constants;

/**
 * Activity used for first-time initialization of the app. 
 * Only shown when the user first uses an app, it provides
 * an interface in which to register the user with the 
 * remote server and to establish an account to be used
 * in conjunction with a sync adapter. The activity will 
 * no longer appear on startup once the user has successfully
 * completed the registration process.
 *
 */
public class StartupActivity extends AccountAuthenticatorActivity 
{
	private static final String TAG = "StartupActivity";
	
	private long syncFreqSeconds;
	
	private EditText nameEditText;
	private EditText statusEditText;
	
	private Button registerButton;
	private RadioGroup syncGroup;
	
	private ProgressDialog progress;
	
	 public void onCreate(Bundle icicle)
	 {
		 super.onCreate(icicle);
		 requestWindowFeature(Window.FEATURE_LEFT_ICON);
	     setContentView(R.layout.startup);
	     getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);
	     
	     nameEditText = (EditText)findViewById(R.id.s_name_edit);
	     statusEditText = (EditText)findViewById(R.id.s_status_edit);
	     registerButton = (Button)findViewById(R.id.register_button);
	     syncGroup = (RadioGroup)findViewById(R.id.sync_freq);
	     syncGroup.check(R.id.r_auto);
	     syncFreqSeconds = -1;
	     
	     // Disable if the device is running an api version lower than 8. Periodic 
	     // syncing was not implemented until 8 and could only be simulated by 
	     // timers, which are not implemented here. 
	     RadioButton fifteen = (RadioButton)syncGroup.findViewById(R.id.r_fifteen);
	     RadioButton hour = (RadioButton)syncGroup.findViewById(R.id.r_hour);
	     if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1)
	     {
	    	 Log.i(TAG, "Phone has API Level lower than 8. Periodic syncs have been disabled.");
	    	 fifteen.setEnabled(false);
	    	 hour.setEnabled(false);
	     }
	     
	     syncGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() 
	     {
			public void onCheckedChanged(RadioGroup group, int checkedId) 
			{
				switch(checkedId)
				{
					case R.id.r_auto:
						Log.v(TAG, "set to -1");
						syncFreqSeconds = -1;
						break;
					case R.id.r_fifteen:
						syncFreqSeconds = 15 * 60;
						break;
					case R.id.r_hour:
						syncFreqSeconds = 60 * 60;
						break;
				}
			}
		});
	     
	    registerButton.setOnClickListener(new View.OnClickListener() 
	    {
			public void onClick(View v) 
			{
				if (nameEditText.getText().length() > 0)
				{
					new UploadNewUserTask().execute();
				}
			}
		});
	 }
	 
	 /**
	  * Registers an account with the system account manager.
	  * The account can be viewed in the settings app under
	  * Accounts and Sync. Currently, this is the only way to add
	  * accounts. Adding one from the settings menu with either 
	  * refuse to function or crash the settings app. 
	  * <p>
	  * Sync frequency is also established here based on the 
	  * options selected by the user. 
	  */
	@SuppressLint("NewApi")
	private void registerAccount()
	 {
		 final Account account = new Account(nameEditText.getText().toString(), AccountConstants.ACCOUNT_TYPE);
		 AccountManager accountManager = AccountManager.get(this);
		 accountManager.addAccountExplicitly(account, null, null);
		 UserData.establishAccount(getApplicationContext(), account);
		 
		 Log.v(TAG, "The sync frequency is: " + (syncFreqSeconds == -1 ? "auto" : syncFreqSeconds));
		 ContentResolver.setSyncAutomatically(account, Constants.AUTHORITY, true);
		 if (syncFreqSeconds != -1)
		 {
			 ContentResolver.addPeriodicSync(account, Constants.AUTHORITY, new Bundle(), syncFreqSeconds);
		 }
		
		 // Setting result to show success in RootActivity
		 final Intent intent = new Intent();
	     setResult(RESULT_OK, intent);
	 }
	 
	/**
	 * Background task registering user with the server. Sends user's 
	 * IP address to server and receives back its identifying key. This
	 * task will end the activity upon completion.
	 */
	 private class UploadNewUserTask extends AsyncTask<Void, Void, Long>
	 {
		 private String externalIp;
		 
		 protected void onPreExecute()
		 {
			 progress = new ProgressDialog(StartupActivity.this);
			 progress.setMessage("Registering...");
			 progress.show();
		 }
		
		protected Long doInBackground(Void... params) 
		{
			externalIp = NetworkUtilities.getIp();
			DataModel d = new DataModel();
			d.setIpAddress(externalIp);
			
			return NetworkUtilities.pushDataToServer(d);
		}
		
		protected void onPostExecute(Long l)
		{
			Log.v(TAG, "new account stored with key: " + l);
			UserData.establishKey(StartupActivity.this, l);
			
			ContentValues cv = new ContentValues();
			cv.put(Constants.SERVER_KEY, UserData.getKey(StartupActivity.this));
			cv.put(Constants.NAME, nameEditText.getText().toString());
			cv.put(Constants.MESSAGE, statusEditText.getText().toString());
			cv.put(Constants.IP, externalIp);
			
			Uri newUser = getContentResolver().insert(Constants.CONTENT_URI, cv);
			Log.v(TAG, "New user stored at: " + newUser.toString());
	
			registerAccount();
			UserData.establishInitialization(getApplicationContext());
			
			if (progress.isShowing())
				progress.dismiss();
			
			finish();
		}
	 }
}
