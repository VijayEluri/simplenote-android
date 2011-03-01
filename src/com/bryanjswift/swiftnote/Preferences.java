package com.bryanjswift.swiftnote;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.bryanjswift.swiftnote.app.Notifications;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.service.SyncService;
import com.bryanjswift.swiftnote.ui.FireIntent;

public class Preferences extends PreferenceActivity {
	// Constants for the values of preferences
	public static final String EMAIL = "email"; // String
	public static final String PASSWORD = "password"; // String
	public static final String TOKEN = "token"; // String
	public static final String BACKGROUND_ENABLED = "background_enabled"; // boolean
	public static final String BACKGROUND = "background"; // int

    @Override
    protected void onResume() {
        super.onResume();
        FireIntent.finishIfUnauthorized(this);
    }

    /**
	 * Set up the preferences view
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
        Notifications.CancelCredentials(this);
	}
	/**
	 * Schedule a broadcast with the new preferences
	 * @see android.preference.PreferenceActivity#onStop()
	 */
	@Override
	protected void onStop() {
		SyncService.scheduleBroadcast(this);
		super.onStop();
	}
	/**
	 * Get login data from preferences
	 * @param context from which to retrieve preferences
	 * @return a HashMap with preference keys and values
	 */
	public static Api.Credentials getLoginPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new Api.Credentials(prefs.getString(EMAIL, ""), prefs.getString(PASSWORD, ""), prefs.getString(TOKEN, ""));
	}
	/**
	 * Save login data into preferences
	 * @param context from which to retrieve preferences
	 * @param email to save
	 * @param password to save
	 * @param auth to save
	 * @return a HashMap with preference keys and values
	 */
	public static Api.Credentials setLoginPreferences(Context context, String email, String password, String auth) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(EMAIL, email).putString(PASSWORD, password).putString(TOKEN, auth);
		editor.commit();
		return new Api.Credentials(email, password, auth);
	}
	/**
	 * Save login data into preferences, shortcut for setLoginPreferences(context, email, password, null);
	 * @param context from which to retrieve preferences
	 * @param email to save
	 * @param password to save
	 * @return a HashMap with preference keys and values
	 */
	public static Api.Credentials setLoginPreferences(Context context, String email, String password) {
		return setLoginPreferences(context, email, password, null);
	}
	/**
	 * Save authorization token into preferences
	 * @param context from which to retrieve preferences
	 * @param auth to save
	 * @return true if the new values were successfully written to persistent storage.
	 */
	public static boolean setAuthToken(Context context, String auth) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(TOKEN, auth);
		return editor.commit();
	}
	/**
	 * Save password into preferences
	 * @param context from which to retrieve preferences
	 * @param password to save
	 * @return true if the new values were successfully written to persistent storage.
	 */
	public static boolean setPassword(Context context, String password) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(PASSWORD, password);
		return editor.commit();
	}
}
