package com.simplenote.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.simplenote.android.net.Api.Response;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.net.SimpleNoteApi;
import com.simplenote.android.persistence.SimpleNoteDao;
import com.simplenote.android.thread.SyncNotesThread;

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
				 * @see com.simplenote.android.net.HttpCallback#on200(com.simplenote.android.net.Api.Response)
				 */
				@Override
				public void on200(Response response) {
					Log.i(Constants.TAG, "Login auth success with API server.");
					String auth = response.body;
					final Thread t = new SyncNotesThread(updateNoteHandler, dao, email, auth);
					t.start();
				}
				/**
				 * @see com.simplenote.android.net.HttpCallback#onError(com.simplenote.android.net.Api.Response)
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