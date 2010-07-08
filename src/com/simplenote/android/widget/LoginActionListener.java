package com.simplenote.android.widget;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.net.Api.Response;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.net.SimpleNoteApi;
import com.simplenote.android.thread.LoginWithCredentials;

public class LoginActionListener implements OnEditorActionListener {
	private static final String LOGGING_TAG = Constants.TAG + LoginActionListener.class.getName();
	private final Activity context;
	private final EditText email;
	private final EditText password;
	private final String loggingIn;
	private final String authenticating;
	private final HttpCallback callback;
	/**
	 * 
	 * @param context
	 * @param email
	 * @param password
	 */
	public LoginActionListener(Activity context, EditText email, EditText password) {
		this(context, email, password, null);
	}
	/**
	 * 
	 * @param context
	 * @param email
	 * @param password
	 * @param callback
	 */
	public LoginActionListener(Activity context, EditText email, EditText password, HttpCallback callback) {
		this.context = context;
		this.email = email;
		this.password = password;
		this.authenticating = context.getString(R.string.status_authenticating);
		this.loggingIn = context.getString(R.string.status_loggingin);
		this.callback = callback;
	}
	/**
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// TODO: Do I need to kill/dismiss this dialog onPause of the Activity?
		final ProgressDialog dialog = ProgressDialog.show(context, loggingIn, authenticating);
		Log.d(LOGGING_TAG, "Created progressDialog: " + dialog.toString());
		Log.i(LOGGING_TAG, "Starting login thread");
		final String emailValue = email.getText().toString();
		final String passwordValue = password.getText().toString();
		final HashMap<String,String> credentials =
			Preferences.setLoginPreferences(context, emailValue, passwordValue);
		// Login with credentials here
		(new LoginWithCredentials(context, credentials, new HttpCallback() {
			/**
			 * @see com.simplenote.android.net.HttpCallback#on200(com.simplenote.android.net.Api.Response)
			 */
			@Override
			public void on200(Response response) {
				dialog.dismiss();
			}
			/**
			 * @see com.simplenote.android.net.HttpCallback#onError(com.simplenote.android.net.Api.Response)
			 */
			@Override
			public void onError(Response response) {
				context.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(context, R.string.error_authentication, Toast.LENGTH_LONG).show();
					}
				});
				Preferences.setPassword(context, null);
				dialog.dismiss();
			}
			/**
			 * If an HttpCallback was provided in the constructor execute them here
			 * @see com.simplenote.android.net.HttpCallback#onComplete(com.simplenote.android.net.Api.Response)
			 */
			@Override
			public void onComplete(final Response response) {
				if (callback != null) {
					SimpleNoteApi.handleResponse(callback, response);
				}
			}
			/**
			 * @see com.simplenote.android.net.HttpCallback#onException(java.lang.String, java.lang.String, java.lang.Throwable)
			 */
			@Override
			public void onException(final String url, final String data, final Throwable t) {
				if (callback != null) {
					callback.onException(url, data, t);
				}
			}
		})).start();
		return false;
	}
}
