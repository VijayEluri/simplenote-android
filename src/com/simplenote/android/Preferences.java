package com.simplenote.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences extends PreferenceActivity {
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
		intent.putExtra("email", mPrefs.getString("email", ""));
		intent.putExtra("password", mPrefs.getString("email", ""));     

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (preferences.getBoolean("background_enabled", false)) {
			if (Constants.LOGGING) { Log.d(Constants.TAG, "Applying alarm every " + preferences.getString("background", "2")); }

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 
					60 * 60 * 1000 * Integer.parseInt(preferences.getString("background", "2")), pendingIntent);
		}
	}
}