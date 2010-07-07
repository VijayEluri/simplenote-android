package com.simplenote.android.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.net.SimpleNoteApi;
import com.simplenote.android.ui.FireIntent;

/**
 * Uses the SimpleNoteApi to login with existing credentials which must be provided
 * @author bryanjswift
 */
public class LoginWithExistingCredentials extends Thread {
	private static final String LOGGING_TAG = Constants.TAG + "LoginWithExistingCredentials";
	private final Activity context;
	private final HashMap<String,String> credentials;
	/**
	 * Create new specialized Thread with credentials information
	 * @param credentials information to use when attempting to re-authenticate
	 */
	public LoginWithExistingCredentials(Activity context, HashMap<String,String> credentials) {
		this.context = context;
		this.credentials = credentials;
	}
	public void run() {
		SimpleNoteApi.login(credentials.get(Preferences.EMAIL), credentials.get(Preferences.PASSWORD), new HttpCallback() {
			/**
			 * Authentication was successful, store the token in the preferences and start the list activity
			 * @see com.simplenote.android.net.HttpCallback#on200(java.lang.String)
			 */
			public void on200(String auth) {
				SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
				Editor editor = prefs.edit();
				// API successfully returned, store token
				editor.putString(Preferences.TOKEN, auth);
				if (editor.commit()) {
					Log.i(LOGGING_TAG, "Successfully saved new authentication token");
				} else {
					Log.i(LOGGING_TAG, "Failed to save new authentication token, uh oh.");
				}
				// start note list activity
				FireIntent.SimpleNoteList(context);
			}
			/**
			 * Authentication failed, show login dialog
			 * @see com.simplenote.android.net.HttpCallback#on401(java.lang.String)
			 */
			public void onError(int status, String body, Map<String, List<String>> headers) {
				FireIntent.SigninDialog(context);
				Log.d(LOGGING_TAG, String.format("Authentication failed with status code %i", status));
				Toast.makeText(context, R.string.error_authentication_stored, Toast.LENGTH_LONG).show();
			}
		});
	}
}
