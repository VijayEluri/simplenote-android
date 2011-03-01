package com.bryanjswift.swiftnote.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * IntentService to do work while the device CPU remains awake
 *
 * Code taken from git://github.com/commonsguy/cw-advandroid.git
 * @author bryanjswift
 */
public abstract class WakefulIntentService extends IntentService {
	private static String LOCK_NAME = "com.bryanjswift.swiftnote.util.WakefulIntentService.Static";
	private static WakeLock lock = null;
	/**
	 * Default constructor to create the WakefulIntentService
	 */
	public WakefulIntentService() {
		super(LOCK_NAME);
	}
	/**
	 * Get a WakeLock, do some work then release the lock
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected final void onHandleIntent(Intent intent) {
		try {
			acquireLock(this);
			handleWakefulIntent(intent);
		} finally {
			releaseLock(this);
		}
	}
	/**
	 * Get a static reference to the WakeLock lock in case it is null
	 * @param context for which to get the PowerManager system service
	 * @return a reference counted PowerManager.WakeLock
	 */
	synchronized private static WakeLock getLock(Context context) {
		if (lock == null) {
			PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME);
			lock.setReferenceCounted(true);
		}
		return lock;
	}
	/**
	 * Acquires the WakeLock so the device's CPU doesn't sleep
	 * @param context for which to get the PowerManager system service to provice the WakeLock
	 */
	synchronized private static void acquireLock(Context context) {
		getLock(context).acquire();
	}
	/**
	 * Releases the WakeLock so the device is permitted to sleep again
	 * @param context for which to get the PowerManager system service to provice the WakeLock
	 */
	synchronized private static void releaseLock(Context context) {
		getLock(context).release();
	}
	/**
	 * Method to actually do the work of handling the intent
	 * @param intent The value passed to startService(Intent).
	 */
	protected abstract void handleWakefulIntent(Intent intent);
}
