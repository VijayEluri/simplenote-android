package com.bryanjswift.simplenote.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

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
	 * Post credentials Notification with given messages
	 * @param context under which the Notification is created
	 */
	public synchronized static void Syncing(Context context) {
		if (!notifyingSync) {
			final NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			final Notification notification = new Notification();
			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification_syncing);
			notification.contentIntent = PendingIntent.getActivity(context, Constants.REQUEST_SYNCING, new Intent(context, SimpleNoteList.class), PendingIntent.FLAG_UPDATE_CURRENT);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.icon = R.drawable.simplenote_status;
			notification.iconLevel = 0;
			notifier.notify(Constants.NOTIFICATION_SYNCING, notification);
			notifyingSync = true;
			Thread t = new Thread(new NotificationRunnable(notifier, notification));
			t.start();
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
	/**
	 * Class to handle the logic of updating the syncing notification icon
	 * @author bryanjswift
	 */
	private static class NotificationRunnable implements Runnable {
		private final NotificationManager manager;
		private final Notification notification;
		public NotificationRunnable(final NotificationManager manager, final Notification notification) {
			this.manager = manager;
			this.notification = notification;
		}
		public void run() {
			while (notifyingSync) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException ie) {
					Log.d(LOGGING_TAG, "Notification animation thread interrupted", ie);
				}
				final int newLevel = (notification.iconLevel + 1) % 4;
				Log.d(LOGGING_TAG, "Updating iconLevel to " + newLevel);
				notification.iconLevel = newLevel;
			}
			// No longer notifying so cancel
			manager.cancel(Constants.NOTIFICATION_SYNCING);
		}
	}
}
