package com.simplenote.android;

import com.simplenote.android.APIBase.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (Constants.LOGGING) { Log.i(Constants.TAG, "Running Alarm!"); }

			APIHelper apiHelper = new APIHelper();
			Bundle bundle = intent.getExtras();
			String email = bundle.getString(Preferences.EMAIL);
			String password = bundle.getString(Preferences.PASSWORD);

			String authBody = APIBase.encode("email=" + email + "&password=" + password, true);
			Response authResponse = APIBase.HTTPPost(Constants.API_LOGIN_URL, authBody);

			if (authResponse.statusCode == 200) { // successful auth login
				String logInToken = authResponse.resp.replaceAll("(\\r|\\n)", "");
				apiHelper.refreshNotes(new NotesDbAdapter(context), logInToken, email);
				Log.i(Constants.TAG, "Login auth success with API server.");
			}
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
}