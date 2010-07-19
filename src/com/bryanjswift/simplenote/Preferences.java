package com.bryanjswift.simplenote;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.bryanjswift.simplenote.service.SyncService;

public class Preferences extends PreferenceActivity {
	// Constants for the values of preferences
	public static final String EMAIL = "email"; // String
	public static final String PASSWORD = "password"; // String
	public static final String TOKEN = "token"; // String
	public static final String BACKGROUND_ENABLED = "background_enabled"; // boolean
	public static final String BACKGROUND = "background"; // int
	/**
	 * Set up the preferences view
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
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
	public static HashMap<String,String> getLoginPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		HashMap<String,String> values = getStringPreferences(prefs, new String[] { Preferences.EMAIL, Preferences.PASSWORD, Preferences.TOKEN });
		return values;
	}
	/**
	 * Save login data into preferences
	 * @param context from which to retrieve preferences
	 * @param email to save
	 * @param password to save
	 * @param auth to save
	 * @return a HashMap with preference keys and values
	 */
	public static HashMap<String,String> setLoginPreferences(Context context, String email, String password, String auth) {
		HashMap<String,String> values = new HashMap<String,String>();
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(EMAIL, email).putString(PASSWORD, password).putString(TOKEN, auth);
		editor.commit();
		values.put(EMAIL, email);
		values.put(PASSWORD, password);
		values.put(TOKEN, auth);
		return values;
	}
	/**
	 * Save login data into preferences, shortcut for setLoginPreferences(context, email, password, null);
	 * @param context from which to retrieve preferences
	 * @param email to save
	 * @param password to save
	 * @return a HashMap with preference keys and values
	 */
	public static HashMap<String,String> setLoginPreferences(Context context, String email, String password) {
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
	/**
	 * Get a set of String values from the Preferences, key won't exist if the default value is returned
	 * @param context from which to retrieve preferences
	 * @param keys of the preference values to retrieve
	 * @return a HashMap with keys as the keys and the corresponding values
	 */
	private static HashMap<String,String> getStringPreferences(SharedPreferences prefs, String[] keys) {
		HashMap<String,String> hash = new HashMap<String, String>();
		for (String key : keys) {
			String value = prefs.getString(key, "");
			if (!value.equals("")) {
				hash.put(key, prefs.getString(key, ""));
			}
		}
		return hash;
	}
}