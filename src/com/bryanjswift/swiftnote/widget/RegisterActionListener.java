package com.bryanjswift.swiftnote.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.thread.LoginTask;
import com.bryanjswift.swiftnote.thread.RegisterTask;
import com.bryanjswift.swiftnote.ui.FireIntent;

/**
 * @author bryanjswift
 */
public class RegisterActionListener implements View.OnClickListener {
    private static final String LOGGING_TAG = Constants.TAG + RegisterActionListener.class.getSimpleName();
    private final Activity context;
    private final EditText email;
    private final EditText password;
    private final String registering;
    private final String authenticating;
    public RegisterActionListener(Activity context, EditText email, EditText password) {
        this.context = context;
        this.email = email;
        this.password = password;
        this.authenticating = context.getString(R.string.status_authenticating);
        this.registering = context.getString(R.string.status_create_account);
    }
    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View view) {
        validateAndRegister();
    }
    /**
     * Attempt register only if information in fields is valid
     */
    private void validateAndRegister() {
        if (isValid()) {
            register();
        }
    }
    /**
     * Check if the register fields are filled out and valid
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
     * Attempt registration in an AsyncTask
     */
    private void register() {
        Log.i(LOGGING_TAG, "Checking credentials and attempting login");
        final String emailValue = email.getText().toString();
        final String passwordValue = password.getText().toString();
        final ProgressDialog dialog = ProgressDialog.show(context, registering, authenticating);
        Log.d(LOGGING_TAG, "Created progressDialog: " + dialog.toString());
        // Register with credentials here
        (new RegisterTask(context, new HttpCallback() {
            /**
             * Called when the response contained a success status code
             * @param response contents of the response
             */
            @Override
            public void on200(Api.Response response) {
                final Api.Credentials credentials = Preferences.setLoginPreferences(context, emailValue, passwordValue);
                Log.d(LOGGING_TAG, "Successfully created new account");
                dialog.setTitle(R.string.status_loggingin);
                (new LoginTask(context, credentials, new HttpCallback() {
                    /**
                     * Called when the response contained a success status code
                     * @param response contents of the response
                     */
                    @Override
                    public void on200(final Api.Response response) {
                        FireIntent.SimpleNoteList(context);
                        context.finish();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                    /**
                     * Called when the request is finished and the status code was anything indicating an error
                     * @param response contents of the response
                     */
                    @Override
                    public void onError(Api.Response response) {
                        Log.d(LOGGING_TAG, "Error logging in with new account");
                    }
                })).execute();
            }
            /**
             * Called when the request is finished and the status code was anything indicating an error
             * @param response contents of the response
             */
            @Override
            public void onError(Api.Response response) {
                Log.d(LOGGING_TAG, "Error creating new account");
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(context, R.string.error_account_unavailable, Toast.LENGTH_SHORT).show();
            }
        })).execute(new Api.Credentials(emailValue, passwordValue, null));
    }
}
