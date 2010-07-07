package com.simplenote.android;

import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences extends PreferenceActivity {
	public static final String EMAIL = "email"; // String
	public static final String PASSWORD = "password"; // String
	public static final String BACKGROUND_ENABLED = "background_enabled"; // boolean
	public static final String BACKGROUND = "background"; // int
	public static final String TOKEN = "token"; // String

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStop() {
		applyPreferences();
		super.onStop();
	}

	// TODO: Apply these at startup...
	// TODO: Cancel alarm if checkbox disabled
	public void applyPreferences() {
		SharedPreferences mPrefs = getSharedPreferences(Constants.PREFS_NAME, 0);

		// Set up the AlarmManager service
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra(EMAIL, mPrefs.getString(EMAIL, ""));
		intent.putExtra(PASSWORD, mPrefs.getString(PASSWORD, ""));

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (preferences.getBoolean(BACKGROUND_ENABLED, false)) {
			Log.d(Constants.TAG, "Applying alarm every " + preferences.getString(Preferences.BACKGROUND, "2"));
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 
					60 * 60 * 1000 * Integer.parseInt(preferences.getString(BACKGROUND, "2")), pendingIntent);
		}
	}
	/**
	 * Get login data from preferences
	 * @param context from which to retrieve preferences
	 * @return a HashMap with preference keys and values
	 */
	public static HashMap<String,String> getLoginPreferences(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
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
		Editor editor = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).edit();
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
		Editor editor = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(TOKEN, auth);
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