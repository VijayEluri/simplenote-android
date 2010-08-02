package com.bryanjswift.simplenote.thread;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.app.Notifications;
import com.bryanjswift.simplenote.manager.Connectivity;
import com.bryanjswift.simplenote.net.Api;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.net.HttpCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;
import com.bryanjswift.simplenote.ui.FireIntent;

/**
 * Uses the SimpleNoteApi to login with existing credentials which must be provided
 * @author bryanjswift
 */
public class LoginTask extends AsyncTask<Void, Void, Response> {
	private static final String LOGGING_TAG = Constants.TAG + "LoginTask";
	private final Context context;

    private final Api.Credentials credentials;
	private final HttpCallback callback;
    /**
     * Default HttpCallback used if none provided
     */
    private final HttpCallback defaultCallback = new HttpCallback() {
        @Override
        public void on200(final Response response) {
            FireIntent.SimpleNoteList(context);
        }
        public void onError(final Response response) {
            Notifications.Credentials(context);
        }
    };
	/**
	 * Create new specialized Thread with credentials information
	 * @param context from which the thread was invoked
	 * @param credentials information to use when attempting to re-authenticate
	 */
	public LoginTask(Context context, Api.Credentials credentials) {
		this(context, credentials, null);
	}
	public LoginTask(Context context, Api.Credentials credentials, HttpCallback callback) {
		this.context = context;
		this.credentials = credentials;
		this.callback = callback;
	}
    /**
     * Send a login request to the SimpleNote API
     * @param voids empty parameter list
     * @return null
     */
    @Override
    protected Response doInBackground(Void... voids) {
        final Response response;
        if (Connectivity.hasInternet(context)) {
            response = SimpleNoteApi.login(credentials, new HttpCallback() {
                /**
                 * Authentication was successful, store the token in the preferences and start the list activity
                 * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
                 */
                @Override
                public void on200(final Response response) {
                    Log.i(LOGGING_TAG, "Setting new authentication token");
                    Preferences.setAuthToken(context, response.body);
                }
                /**
                 * Authentication failed, show login dialog
                 * @see com.bryanjswift.simplenote.net.HttpCallback#onError(com.bryanjswift.simplenote.net.Api.Response)
                 */
                @Override
                public void onError(final Response response) {
                    Log.d(LOGGING_TAG, String.format("Authentication failed with status code %d", response.status));
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
            });
        } else {
            response = new Response();
            response.status = 408;
            // Maybe should be a status 200 with an retry registered for when the network becomes available
        }
        return response;
	}
    /**
     * Runs on UI thread, handle execution of the passed in callback
     * Uses private defaultCallback if none provided in constructor
     * @param response from the login API call
     */
    @Override
    protected void onPostExecute(final Response response) {
        super.onPostExecute(response);
        if (callback == null) {
            SimpleNoteApi.handleResponse(defaultCallback, response);
        } else {
            SimpleNoteApi.handleResponse(callback, response);
        }
    }

}
