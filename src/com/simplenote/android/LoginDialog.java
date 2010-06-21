package com.simplenote.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.simplenote.android.APIBase.Response;

public class LoginDialog extends Activity {
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor;
	public JSONObject mUserData;
	public ProgressDialog mProgressDialog;
	
	private Thread mThread;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences( Constants.PREFS_NAME, 0);
		mPrefsEditor = mPrefs.edit();

		setContentView(R.layout.login);

		EditText loginBox = (EditText) findViewById(R.id.email);
		EditText passwordBox = (EditText) findViewById(R.id.password);
		
		loginBox.setText( mPrefs.getString("email", "") );
		passwordBox.setText( mPrefs.getString("password", "") );

		Button loginBtn = (Button) findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(mLoginButtonClick);
	}
	
	@Override
	public void onPause() {
		if ( mThread != null && mThread.isAlive() ) { mThread.stop(); };
		if ( mProgressDialog != null && mProgressDialog.isShowing() ) { mProgressDialog.dismiss(); }
		
		super.onPause();
	}
	
	private Runnable threadProcLogin = new Runnable() {
		public void run() {
			EditText loginBox = (EditText) findViewById(R.id.email);
			EditText passwordBox = (EditText) findViewById(R.id.password);
			
			String email = loginBox.getText().toString();
			String password = passwordBox.getText().toString();
			
			runOnUiThread( new Runnable() {
				public void run() {
					mProgressDialog.setMessage("Authenticating...");
					mProgressDialog.show();
				}
			});
			
			if ( Constants.LOGGING ) { 
				Log.d(Constants.TAG, "Attempting login authentication with API server."); 
				Log.d(Constants.TAG, "email: " + email + ", password: " + password); 
			}

			String authBody = APIBase.encode( "email=" + email + "&password=" + password, true );
			if ( Constants.LOGGING ) { Log.d(Constants.TAG, "encoded authBody: " + authBody); }
			Response authResponse = APIBase.HTTPPost( Constants.API_LOGIN_URL, authBody );
						
			if ( authResponse.statusCode == 401 ) { // failed auth login
				if ( Constants.LOGGING ) { Log.i(Constants.TAG, "Login auth failed with API server."); }
				runOnUiThread( new Runnable() {
					public void run() {
						mProgressDialog.dismiss();
						Toast.makeText( LoginDialog.this, "Error authenticating with server", Toast.LENGTH_LONG).show();
					}
				});
			} else if (authResponse.statusCode == 200) { // successful auth login
				if ( Constants.LOGGING ) { Log.i(Constants.TAG, "Login auth success with API server."); }
				mPrefsEditor.putString("email", email);
				mPrefsEditor.putString("password", password);
				mPrefsEditor.putString("token", authResponse.resp);
				mPrefsEditor.commit();
				
				// Refresh the notes when logging in. TODO: Make this happen in the background
				String logInToken = authResponse.resp.replaceAll("(\\r|\\n)", "");
				APIHelper apiHelper = new APIHelper();
				apiHelper.clearAndRefreshNotes(getApplicationContext(), logInToken, email);
				
				runOnUiThread( new Runnable() {
					public void run() {
						mProgressDialog.dismiss();
						Intent intent = new Intent( LoginDialog.this, SimpleNote.class );
						startActivity(intent);
						LoginDialog.this.finish();
					}
				});
			}
		}
	};
	
	private OnClickListener mLoginButtonClick = new OnClickListener() {
		public void onClick(View v) {
			mThread = new Thread( threadProcLogin );
			mProgressDialog = ProgressDialog.show( LoginDialog.this, "Logging in...", "Initializing...");
			if ( Constants.LOGGING ) { Log.i(Constants.TAG, "Starting login thread"); }
			mThread.start();
		}
	};
}
