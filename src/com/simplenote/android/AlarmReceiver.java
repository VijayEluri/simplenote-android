package com.simplenote.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.simplenote.android.APIBase.Response;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String LOGGING_TAG = Constants.TAG + "AlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.i(LOGGING_TAG, "Running Alarm!");

			Bundle bundle = intent.getExtras();
			String email = bundle.getString(Preferences.EMAIL);
			String password = bundle.getString(Preferences.PASSWORD);

			Response authResponse = APIHelper.getLoginResponse(email,password);

			if (authResponse.statusCode == 200) { // successful auth login
				Log.i(Constants.TAG, "Login auth success with API server.");
				String token = authResponse.resp.replaceAll("(\\r|\\n)", "");
				APIHelper.refreshNotes(new NotesDbAdapter(context), token, email);
			} else if (authResponse.statusCode == 401) { // failed login
				// post a notification about failed sync due to credentials
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}