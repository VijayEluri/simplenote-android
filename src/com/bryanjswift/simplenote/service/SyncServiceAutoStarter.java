package com.bryanjswift.simplenote.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver to start up background synchronization service when the
 * application is launched
 * @author bryanjswift
 */
public class SyncServiceAutoStarter extends BroadcastReceiver {
	/**
	 * Start the SyncService
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, SyncService.class));
	}
}
