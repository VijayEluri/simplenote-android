package com.bryanjswift.simplenote.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.ui.LoginDialog;

/**
 * Helper methods for posting Notifications
 * @author bryanjswift
 */
public class Notifications {
	/**
	 * Post credentials Notification with given messages
	 * @param context under which the Notification is created
	 * @param ticker message to show when Notification is posted
	 * @param title of Notification in pull down
	 * @param description of Notification in pull down
	 */
	public static void Credentials(Context context, CharSequence ticker, CharSequence title, CharSequence description) {
		final NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification notification = new Notification(R.drawable.simplenote_status, title, System.currentTimeMillis());
		final PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(context, LoginDialog.class), 0);
		notification.setLatestEventInfo(context.getApplicationContext(), title, description, intent);
		notification.iconLevel = 3;
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
}
