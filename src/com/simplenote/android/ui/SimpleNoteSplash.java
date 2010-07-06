package com.simplenote.android.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.simplenote.android.Constants;
import com.simplenote.android.LoginDialog;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.net.SimpleNoteApi;

/**
 * Main Activity for SimpleNote application
 * @author bryanjswift
 */
public class SimpleNoteSplash extends Activity {
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteSplash";
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		setContentView(R.layout.splash);
		if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.TOKEN)) { // valid token stored
			startSimpleNoteList();
		} else if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.PASSWORD)) { // token expired
			// get new token from API
			(new LoginWithExistingCredentials(credentials)).start();
		} else {
			// start login dialog because we don't have any data stored
			showSigninDialog();
		}
	}
	/**
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case Constants.REQUEST_LOGIN: handleSigninResult(resultCode, data); break;
		}
	}
	/**
	 * Start the SimpleNoteList Activity
	 */
	private void startSimpleNoteList() {
		Intent i = new Intent(SimpleNoteSplash.this, SimpleNoteList.class);
		startActivity(i);
	}
	/**
	 * Start the LoginDialog Activity expecting a result
	 */
	private void showSigninDialog() {
		Intent i = new Intent(SimpleNoteSplash.this, LoginDialog.class);
		startActivityForResult(i, Constants.REQUEST_LOGIN);
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity start
	 * @param resultCode how the LoginDialog Activity finished
	 * @param data the intent that started the LoginDialog Activity
	 */
	private void handleSigninResult(int resultCode, Intent data) {
		// Assume this only gets called when LoginDialog completed successfully
		startSimpleNoteList();
	}
	/**
	 * Uses the SimpleNoteApi to login with existing credentials which must be provided
	 * @author bryanjswift
	 */
	private class LoginWithExistingCredentials extends Thread {
		private final HashMap<String,String> credentials;
		/**
		 * Create new specialized Thread with credentials information
		 * @param credentials information to use when attempting to re-authenticate
		 */
		public LoginWithExistingCredentials(HashMap<String,String> credentials) {
			this.credentials = credentials;
		}
		public void run() {
			SimpleNoteApi.login(credentials.get(Preferences.EMAIL), credentials.get(Preferences.PASSWORD), new HttpCallback() {
				/**
				 * Authentication was successful, store the token in the preferences and start the list activity
				 * @see com.simplenote.android.net.HttpCallback#on200(java.lang.String)
				 */
				public void on200(String auth) {
					SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
					Editor editor = prefs.edit();
					// API successfully returned, store token
					editor.putString(Preferences.TOKEN, auth);
					if (editor.commit()) {
						Log.i(LOGGING_TAG, "Successfully saved new authentication token");
					} else {
						Log.i(LOGGING_TAG, "Failed to save new authentication token, uh oh.");
					}
					// start note list activity
					startSimpleNoteList();
				}
				/**
				 * Authentication failed, show login dialog
				 * @see com.simplenote.android.net.HttpCallback#on401(java.lang.String)
				 */
				public void onError(int status, String body, Map<String, List<String>> headers) {
					showSigninDialog();
					Log.d(LOGGING_TAG, String.format("Authentication failed with status code %i", status));
					Toast.makeText(getApplicationContext(), R.string.error_authentication_stored, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
