package com.bryanjswift.swiftnote.thread;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.app.Notifications;
import com.bryanjswift.swiftnote.manager.Connectivity;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.Api.Response;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.net.SwiftNoteApi;
import com.bryanjswift.swiftnote.ui.FireIntent;

/**
 * Uses the SwiftNoteApi to login with existing credentials which must be provided
 * @author bryanjswift
 */
public class LoginTask extends AsyncTask<Void, Void, Response> {
    private static final String LOGGING_TAG = Constants.TAG + "LoginTask";
    // Immutable fields
    private final Context context;
    private final Api.Credentials credentials;
    private final HttpCallback callback;
    /**
     * Default HttpCallback used if none provided
     */
    private final HttpCallback defaultCallback = new HttpCallback() {
        /**
         * Go to the SwiftNoteList screen
         * @param response contents of the response
         */
        @Override
        public void on200(final Response response) {
            FireIntent.SimpleNoteList(context);
        }
        /**
         * Show notification about failed login
         * @param response contents of the response
         */
        @Override
        public void onError(final Response response) {
            Notifications.Credentials(context);
        }
    };
    /**
     * Create new specialized task with credentials information
     * @param context from which the task was invoked
     * @param credentials information to use when attempting to re-authenticate
     */
    public LoginTask(Context context, Api.Credentials credentials) {
        this(context, credentials, null);
    }

    /**
     * Create new task to perform user authentication in background
     * @param context from which the task was invoked
     * @param credentials information to use when attempting to re-authenticate
     * @param callback to run instead of the default
     */
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
            Log.i(LOGGING_TAG, "Connected, attempting login");
            response = SwiftNoteApi.login(credentials, new HttpCallback() {
                /**
                 * Authentication was successful, store the token in the preferences and start the list activity
                 * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
                 */
                @Override
                public void on200(final Response response) {
                    Log.i(LOGGING_TAG, "Setting new authentication token");
                    Preferences.setAuthToken(context, response.body);
                }

                /**
                 * Authentication failed, show login dialog
                 * @see com.bryanjswift.swiftnote.net.HttpCallback#onError(com.bryanjswift.swiftnote.net.Api.Response)
                 */
                @Override
                public void onError(final Response response) {
                    Log.d(LOGGING_TAG, String.format("Authentication failed with status code %d", response.status));
                }

                /**
                 * @see com.bryanjswift.swiftnote.net.HttpCallback#onException(java.lang.String, java.lang.String, java.lang.Throwable)
                 */
                @Override
                public void onException(final String url, final String data, final Throwable t) {
                    if (callback != null) {
                        callback.onException(url, data, t);
                    }
                }
            });
        } else {
            Log.i(LOGGING_TAG, "No internet connection, returning timeout status code");
            response = new Response();
            response.status = Constants.NO_CONNECTION;
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
            SwiftNoteApi.handleResponse(defaultCallback, response);
        } else {
            SwiftNoteApi.handleResponse(callback, response);
        }
    }

}
