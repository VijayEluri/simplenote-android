package com.bryanjswift.simplenote.service;

import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.net.HttpCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;
import com.bryanjswift.simplenote.util.WakefulIntentService;

/**
 * Service to keep an up to date authentication token and to reset the API count
 * @author bryanjswift
 */
public class DailyService extends WakefulIntentService {
	private static final String LOGGING_TAG = Constants.TAG + "DailyService";
	/**
	 * @see com.bryanjswift.simplenote.util.WakefulIntentService#handleWakefulIntent(android.content.Intent)
	 */
	@Override
	protected void handleWakefulIntent(Intent intent) {
		Log.d(LOGGING_TAG, "Handling DailyService business");
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.PASSWORD)) {
			SimpleNoteApi.login(credentials.get(Preferences.EMAIL), credentials.get(Preferences.PASSWORD), new HttpCallback() {
				/**
				 * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
				 */
				@Override
				public void on200(Response response) {
					super.on200(response);
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyService.this);
					Editor editor = prefs.edit();
					// API successfully returned, store token
					editor.putString(Preferences.TOKEN, response.body);
					if (editor.commit()) {
						Log.i(LOGGING_TAG, "Successfully saved new authentication token");
					} else {
						Log.i(LOGGING_TAG, "Failed to save new authentication token, uh oh.");
					}
				}
				/**
				 * @see com.bryanjswift.simplenote.net.HttpCallback#onError(com.bryanjswift.simplenote.net.Api.Response)
				 */
				@Override
				public void onError(Response response) {
					super.onError(response);
					// TODO: Post notification that credentials are bad
				}
				
			});
		}
		Log.d(LOGGING_TAG, "Resetting SimpleNoteApi.count");
		SimpleNoteApi.count.set(0);
	}
	/**
	 * Schedule an alarm for DailyService
	 */
	public static void scheduleBroadcast(Context context) {
		Log.d(LOGGING_TAG, "Scheduling DailyService.Starter broadcast");
		final Intent intent = new Intent(context, DailyService.Starter.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		final AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, (SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY),
				AlarmManager.INTERVAL_DAY, pendingIntent);
	}
	/**
	 * BroadcastReceiver to start up the DailyService
	 * @author bryanjswift
	 */
	public static class Starter extends BroadcastReceiver {
		/**
		 * Start the DailyService
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOGGING_TAG, "Starting the DailyService");
			context.startService(new Intent(context, DailyService.class));
		}
		
	}
}
