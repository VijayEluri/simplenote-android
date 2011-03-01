package com.bryanjswift.swiftnote.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.ui.SimpleNoteList;
import com.bryanjswift.swiftnote.ui.SwiftNoteSplash;

/**
 * Helper methods for posting Notifications
 * @author bryanjswift
 */
public class Notifications {
    private static final String LOGGING_TAG = Constants.TAG + "Notifications";
    // Whether or not the Syncing notification is active
    private static boolean notifyingSync = false;
    /**
     * Post credentials Notification with given messages
     * @param context under which the Notification is created
     * @param ticker message to show when Notification is posted
     * @param title of Notification in pull down
     * @param description of Notification in pull down
     */
    public static void Credentials(Context context, CharSequence ticker, CharSequence title, CharSequence description) {
        final NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = new Notification(
                R.drawable.swiftnote_status_3, ticker, System.currentTimeMillis());
        final Intent intent = new Intent(context, SwiftNoteSplash.class);
        intent.putExtra(Constants.NOTIFICATION_TYPE, Constants.NOTIFICATION_CREDENTIALS);
        notification.setLatestEventInfo(
                context.getApplicationContext(),
                title,
                description,
                PendingIntent.getActivity(context, Constants.REQUEST_LOGIN, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notifier.notify(Constants.NOTIFICATION_CREDENTIALS, notification);
    }
    /**
     * Post Notification with default invalid credentials messages
     * @param context under which the Notification is created
     */
    public static void Credentials(Context context) {
        Credentials(context,
                context.getString(R.string.status_credentials_invalid_ticker),
                context.getString(R.string.status_credentials_invalid_title),
                context.getString(R.string.status_credentials_invalid_description));
    }

    /**
     * Cancel the Notification about invalid credentials
     * @param context under which the Notification is cancelled
     */
    public static void CancelCredentials(Context context) {
        final NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifier.cancel(Constants.NOTIFICATION_CREDENTIALS);
    }
    /**
     * Post credentials Notification with given messages
     * @param context under which the Notification is created
     */
    public synchronized static void Syncing(Context context) {
        if (!notifyingSync) {
            final NotificationManager notifier =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification notification = new Notification(
                    R.drawable.swiftnote_status, null, System.currentTimeMillis());
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            final Intent intent = new Intent(context, SimpleNoteList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notification.setLatestEventInfo(
                    context.getApplicationContext(),
                    context.getString(R.string.status_syncing_title),
                    context.getString(R.string.status_syncing_description),
                    PendingIntent.getActivity(context, Constants.REQUEST_SYNCING, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            notifier.notify(Constants.NOTIFICATION_SYNCING, notification);
            Log.d(LOGGING_TAG, "Starting sync notify");
            notifyingSync = true;
        }
    }
    /**
     * Cancel the ongoing syncing notification
     * @param context under which to get the NotificationManager
     */
    public synchronized static void CancelSyncing(Context context) {
        notifyingSync = false;
        final NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(LOGGING_TAG, "Cancelling sync notify");
        notifier.cancel(Constants.NOTIFICATION_SYNCING);
    }
}
