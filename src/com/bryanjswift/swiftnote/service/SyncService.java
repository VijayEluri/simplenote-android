package com.bryanjswift.swiftnote.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.app.Notifications;
import com.bryanjswift.swiftnote.app.UpdateNoteHandler;
import com.bryanjswift.swiftnote.manager.Connectivity;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.thread.SyncNotesTask;
import com.bryanjswift.swiftnote.util.WakefulIntentService;

public class SyncService extends WakefulIntentService {
    private static final String LOGGING_TAG = Constants.TAG + "SyncService";
    private static int hoursInMillis = 3600000;
    /**
     * Create an instance of the service with a SwiftNoteDao
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
     * @see com.bryanjswift.swiftnote.util.WakefulIntentService#handleWakefulIntent(android.content.Intent)
     */
    @Override
    protected void handleWakefulIntent(Intent intent) {
        if (Connectivity.isBackgroundDataEnabled(this)) {
            Log.d(LOGGING_TAG, "Handling synchronization in a wakeful manner");
            final Api.Credentials credentials = Preferences.getLoginPreferences(this);
            if (!credentials.email.equals("") && !credentials.auth.equals("")) {
                (new SyncNotesTask(this, syncNotesHandler)).execute();
            } else {
                Notifications.Credentials(SyncService.this,
                        getString(R.string.status_credentials_missing_ticker),
                        getString(R.string.status_credentials_missing_title),
                        getString(R.string.status_credentials_missing_description));
            }
        } else {
            Log.d(LOGGING_TAG, "Background data off, skipping this sync");
        }
        scheduleBroadcast(this);
    }
    /**
     * Schedule an alarm depending on the value in the Preferences
     * @param context for which the notification Intent is created
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
    private Handler syncNotesHandler = new UpdateNoteHandler(this);
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
