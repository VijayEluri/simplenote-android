package com.bryanjswift.simplenote.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.net.Api;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.net.HttpCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;
import com.bryanjswift.simplenote.thread.LoginTask;

public class LoginActionListener implements OnEditorActionListener, View.OnClickListener {
	private static final String LOGGING_TAG = Constants.TAG + "LoginActionListener";
	private final Activity context;
	private final EditText email;
	private final EditText password;
	private final String loggingIn;
	private final String authenticating;
	private final HttpCallback callback;
	/**
     * Create an Listener to handle login actions
	 * @param context for the login action
	 * @param email text view containing email address information
	 * @param password text view containing password information
	 */
	public LoginActionListener(Activity context, EditText email, EditText password) {
		this(context, email, password, null);
	}
	/**
	 * Create an Listener to handle login actions
	 * @param context for the login action
	 * @param email text view containing email address information
     * @param password text view containing password information
	 * @param callback to run after those supplied by the Login action
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
        login();
        return true;
	}
    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        login();
    }
    /**
     * Handle the process of logging a user in when they perform an action that justifies it
     */
    public void login() {
        final ProgressDialog dialog = ProgressDialog.show(context, loggingIn, authenticating);
        Log.d(LOGGING_TAG, "Created progressDialog: " + dialog.toString());
        Log.i(LOGGING_TAG, "Starting login thread");
        final String emailValue = email.getText().toString();
        final String passwordValue = password.getText().toString();
        final Api.Credentials credentials = Preferences.setLoginPreferences(context, emailValue, passwordValue);
        // Login with credentials here
        (new LoginTask(context, credentials, new HttpCallback() {
            /**
             * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
             */
            @Override
            public void on200(Response response) {
                dialog.dismiss();
            }
            /**
             * @see com.bryanjswift.simplenote.net.HttpCallback#onError(com.bryanjswift.simplenote.net.Api.Response)
             */
            @Override
            public void onError(Response response) {
                if (response.status == Constants.NO_CONNECTION) {
                    Toast.makeText(context, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.error_authentication, Toast.LENGTH_LONG).show();
                }
                Preferences.setPassword(context, null);
                dialog.dismiss();
            }
            /**
             * If an HttpCallback was provided in the constructor execute them here
             * @see com.bryanjswift.simplenote.net.HttpCallback#onComplete(com.bryanjswift.simplenote.net.Api.Response)
             */
            @Override
            public void onComplete(final Response response) {
                if (callback != null) {
                    SimpleNoteApi.handleResponse(callback, response);
                }
            }
            /**
             * @see com.bryanjswift.simplenote.net.HttpCallback#onException(java.lang.String, java.lang.String, java.lang.Throwable)
             */
            @Override
            public void onException(final String url, final String data, final Throwable t) {
                if (callback != null) {
                    callback.onException(url, data, t);
                }
            }
        })).execute();
    }
}
