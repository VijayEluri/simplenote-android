package com.bryanjswift.simplenote.service;

import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.app.Notifications;
import com.bryanjswift.simplenote.net.AndroidSimpleNoteApi;
import com.bryanjswift.simplenote.ui.SimpleNoteList;
import com.bryanjswift.simplenote.util.WakefulIntentService;

public class SyncService extends WakefulIntentService {
	private static final String LOGGING_TAG = Constants.TAG + "SyncService";
	private static int hoursInMillis = 3600000;
	/**
	 * Create an instance of the service with a SimpleNoteDao
	 */
	public SyncService() {
		super();
	}
	/**
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		scheduleBroadcast(this);
	}
	/**
	 * @see com.bryanjswift.simplenote.util.WakefulIntentService#handleWakefulIntent(android.content.Intent)
	 */
	@Override
	protected void handleWakefulIntent(Intent intent) {
		Log.d(LOGGING_TAG, "Handling synchronization in a wakeful manner");
		final HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.TOKEN)) {
			final AndroidSimpleNoteApi api = new AndroidSimpleNoteApi(this, syncNotesHandler);
			api.sync();
		} else {
			Notifications.Credentials(SyncService.this,
					getString(R.string.status_credentials_missing_ticker),
					getString(R.string.status_credentials_missing_title),
					getString(R.string.status_credentials_missing_description));
		}
		scheduleBroadcast(this);
	}
	/**
	 * Schedule an alarm depending on the value in the Preferences
	 */
	public static void scheduleBroadcast(Context context) {
		Log.d(LOGGING_TAG, "Attempting to schedule a broadcast");
		final Intent intent = new Intent(context, SyncService.Starter.class);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		final long schedule = hoursInMillis * Integer.parseInt(preferences.getString(Preferences.BACKGROUND, "0"), 10);
		if (preferences.getBoolean(Preferences.BACKGROUND_ENABLED, false) && schedule > 0) {
			Log.d(LOGGING_TAG, "Scheduling a broadcast for synchronization");
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + schedule, pendingIntent);
		}
	}
	/**
	 * Message handler which should handle the update started and update finished messages
	 */
	private Handler syncNotesHandler = new Handler() {
		/**
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constants.MESSAGE_UPDATE_FINISHED: handleUpdateFinished(msg); break;
				case Constants.MESSAGE_UPDATE_STARTED: handleUpdateStarted(msg); break;
				default: break;
			}
		}
		/**
		 * Handle the update started message
		 * @param msg with any relevant information
		 */
		private void handleUpdateStarted(final Message msg) {
			Notifications.Syncing(SyncService.this);
		}
		/**
		 * Handle the update finished message
		 * @param msg with any relevant information
		 */
		private void handleUpdateFinished(final Message msg) {
			Notifications.CancelSyncing(SyncService.this);
			sendBroadcast(new Intent(SimpleNoteList.UPDATE));
		}
	};
	/**
	 * Broadcast receiver to start up background synchronization service when an alarm
	 * is received
	 * @author bryanjswift
	 */
	public static class Starter extends BroadcastReceiver {
		private static final String LOGGING_TAG = Constants.TAG + "SyncService.Starter";
		/**
		 * Start the SyncService
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(LOGGING_TAG, "Starting the sync service to synchronize notes");
			context.startService(new Intent(context, SyncService.class));
		}
	}
}
