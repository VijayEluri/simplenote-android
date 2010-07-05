package com.simplenote.android;

import com.simplenote.android.APIBase.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String LOGGING_TAG = Constants.TAG + "AlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.i(LOGGING_TAG, "Running Alarm!");

			Bundle bundle = intent.getExtras();
			String email = bundle.getString(Preferences.EMAIL);
			String password = bundle.getString(Preferences.PASSWORD);

			String authBody = APIBase.encode("email=" + email + "&password=" + password, true);
			Response authResponse = APIBase.HTTPPost(Constants.API_LOGIN_URL, authBody);

			if (authResponse.statusCode == 200) { // successful auth login
				Log.i(Constants.TAG, "Login auth success with API server.");
				String token = authResponse.resp.replaceAll("(\\r|\\n)", "");
				APIHelper.refreshNotes(new NotesDbAdapter(context), token, email);
			}
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
}