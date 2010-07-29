package com.bryanjswift.simplenote.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.ui.LoginDialog;
import com.bryanjswift.simplenote.ui.SimpleNoteList;

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
				R.drawable.simplenote_status_3, ticker, System.currentTimeMillis());
		final PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(context, LoginDialog.class), 0);
		notification.setLatestEventInfo(context.getApplicationContext(), title, description, intent);
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
		if (android.os.Build.VERSION.SDK_INT < 7) {
			// Use a UI blocking dialog because it's a less powerful device
		}
		if (!notifyingSync) {
			final NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			final Notification notification = new Notification(
					R.drawable.simplenote_status, null, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.setLatestEventInfo(
					context.getApplicationContext(),
					context.getString(R.string.status_syncing_title),
					context.getString(R.string.status_syncing_description),
					PendingIntent.getActivity(context, Constants.REQUEST_SYNCING, new Intent(context, SimpleNoteList.class), PendingIntent.FLAG_UPDATE_CURRENT));
			notifier.notify(Constants.NOTIFICATION_SYNCING, notification);
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
		notifier.cancel(Constants.NOTIFICATION_SYNCING);
	}
}
