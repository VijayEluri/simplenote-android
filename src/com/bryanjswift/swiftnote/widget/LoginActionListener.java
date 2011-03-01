package com.bryanjswift.swiftnote.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.Api.Response;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.net.SwiftNoteApi;
import com.bryanjswift.swiftnote.thread.LoginTask;

/**
 * OnEditorActionListener and OnClickListener to handle logging in from SwiftNoteSplash screen
 * @author bryanjswift
 */
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
        validateAndLogin();
        return true;
    }
    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        validateAndLogin();
    }
    /**
     * Attempt login only if information in fields is valid
     */
    private void validateAndLogin() {
        if (isValid()) {
            login();
        }
    }
    /**
     * Check if the login fields are filled out and valid
     * @return true if email and password both have values
     */
    private boolean isValid() {
        final String emailValue = email.getText().toString();
        final String passwordValue = password.getText().toString();
        boolean valid = true;
        if (emailValue == null || emailValue.equals("") || passwordValue == null || passwordValue.equals("")) {
            Toast.makeText(context, R.string.error_empty, Toast.LENGTH_LONG).show();
            valid = false;
        }
        return valid;
    }
    /**
     * Handle the process of logging a user in when they perform an action that justifies it
     */
    private void login() {
        Log.i(LOGGING_TAG, "Checking credentials and attempting login");
        final String emailValue = email.getText().toString();
        final String passwordValue = password.getText().toString();
        final ProgressDialog dialog = ProgressDialog.show(context, loggingIn, authenticating);
        Log.d(LOGGING_TAG, "Created progressDialog: " + dialog.toString());
        final Api.Credentials credentials = Preferences.setLoginPreferences(context, emailValue, passwordValue);
        // Login with credentials here
        (new LoginTask(context, credentials, new HttpCallback() {
            /**
             * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
             */
            @Override
            public void on200(Response response) {
                dialog.dismiss();
            }
            /**
             * @see com.bryanjswift.swiftnote.net.HttpCallback#onError(com.bryanjswift.swiftnote.net.Api.Response)
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
             * @see com.bryanjswift.swiftnote.net.HttpCallback#onComplete(com.bryanjswift.swiftnote.net.Api.Response)
             */
            @Override
            public void onComplete(final Response response) {
                if (callback != null) {
                    SwiftNoteApi.handleResponse(callback, response);
                }
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
        })).execute();
    }
}
