package com.bryanjswift.swiftnote.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.app.Notifications;
import com.bryanjswift.swiftnote.manager.Connectivity;
import com.bryanjswift.swiftnote.manager.ConnectivityReceiver;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.Api.Response;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.net.SimpleNoteApi;
import com.bryanjswift.swiftnote.util.WakefulIntentService;

/**
 * Service to keep an up to date authentication token and to reset the API count
 * @author bryanjswift
 */
public class DailyService extends WakefulIntentService {
	private static final String LOGGING_TAG = Constants.TAG + "DailyService";
	/**
	 * @see com.bryanjswift.swiftnote.util.WakefulIntentService#handleWakefulIntent(android.content.Intent)
	 */
	@Override
	protected void handleWakefulIntent(Intent intent) {
		Log.d(LOGGING_TAG, "Handling DailyService business");
		Api.Credentials credentials = Preferences.getLoginPreferences(this);
		if (Connectivity.hasInternet(this)) {
            if (!credentials.email.equals("") && !credentials.password.equals("")) {
                // Can't do this in AsyncTask because if we do the service thread might be killed while
                // still waiting on response
                SimpleNoteApi.login(credentials, new HttpCallback() {
                    /**
                     * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
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
                     * @see com.bryanjswift.swiftnote.net.HttpCallback#onError(com.bryanjswift.swiftnote.net.Api.Response)
                     */
                    @Override
                    public void onError(Response response) {
                        super.onError(response);
                        Notifications.Credentials(DailyService.this);
                    }
                });
            }
		} else {
            // should register for network connected event to run a DailyService.Starter Intent
            final BroadcastReceiver loginOnConnected = new ConnectivityReceiver(DailyService.this) {
                /**
                 * Send a broadcast to try the login again
                 * @param info on the network that is now connected
                 */
                public void handleConnected(final NetworkInfo info) {
                    sendBroadcast(new Intent(DailyService.this, DailyService.Starter.class));
                    unregisterReceiver(this);
                }
            };
            registerReceiver(loginOnConnected, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        }
		Log.d(LOGGING_TAG, "Resetting SimpleNoteApi.count");
		SimpleNoteApi.count.set(0);
	}
	/**
	 * Schedule an alarm for DailyService
     * @param context for which the broadcast intent is created
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
