package com.bryanjswift.simplenote.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bryanjswift.simplenote.persistence.SimpleNoteDao;

public class SyncService extends Service {
	private final SimpleNoteDao dao;
	/**
	 * Create an instance of the service with a SimpleNoteDao
	 */
	public SyncService() {
		this.dao = new SimpleNoteDao(this);
	}
	/**
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}
	/**
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	/**
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	/**
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
