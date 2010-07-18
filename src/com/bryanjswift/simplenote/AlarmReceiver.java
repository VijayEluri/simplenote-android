package com.bryanjswift.simplenote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bryanjswift.simplenote.net.HttpCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.thread.SyncNotesThread;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String LOGGING_TAG = Constants.TAG + "AlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		final SimpleNoteDao dao = new SimpleNoteDao(context);
		try {
			Log.i(LOGGING_TAG, "Running Alarm!");

			final Bundle bundle = intent.getExtras();
			final String email = bundle.getString(Preferences.EMAIL);
			final String password = bundle.getString(Preferences.PASSWORD);

			SimpleNoteApi.login(email, password, new HttpCallback() {
				/**
				 * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
				 */
				@Override
				public void on200(Response response) {
					Log.i(Constants.TAG, "Login auth success with API server.");
					String auth = response.body;
					final Thread t = new SyncNotesThread(updateNoteHandler, dao, email, auth);
					t.start();
				}
				/**
				 * @see com.bryanjswift.simplenote.net.HttpCallback#onError(com.bryanjswift.simplenote.net.Api.Response)
				 */
				@Override
				public void onError(Response response) {
					super.onError(response);
					// post a notification about failed sync due to credentials
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Message handler which should _____ when a message with a Note is received
	 */
	private Handler updateNoteHandler = new Handler() {
		/**
		 * Messages are received from SyncNotesThread when a Note is updated or retrieved from the server
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			// do nothing
		}
	};
}