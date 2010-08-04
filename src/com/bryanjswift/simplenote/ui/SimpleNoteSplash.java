package com.bryanjswift.simplenote.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.app.Notifications;
import com.bryanjswift.simplenote.net.Api;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.net.HttpCallback;
import com.bryanjswift.simplenote.service.DailyService;
import com.bryanjswift.simplenote.service.SyncService;
import com.bryanjswift.simplenote.widget.LoginActionListener;

/**
 * Main Activity for SimpleNote application
 * @author bryanjswift
 */
public class SimpleNoteSplash extends Activity {
    private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteSplash";
    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        SyncService.scheduleBroadcast(this); // schedule alarms when application launches
        DailyService.scheduleBroadcast(this);
        setContentView(R.layout.splash);
        final Api.Credentials credentials = Preferences.getLoginPreferences(this);
        Notifications.CancelCredentials(this);
        if (!credentials.email.equals("") && (!credentials.password.equals("") || !credentials.auth.equals(""))) {
            // valid token stored
            Log.d(LOGGING_TAG, "Auth information stored, going to list");
            FireIntent.SimpleNoteList(this);
            this.finish();
        } else {
            // set up events for the splash screen
            Log.d(LOGGING_TAG, "Need to get credentials, setup events for the splash screen");
            setupSplashFields(credentials);
        }
    }
    /**
     * Attach focus event listeners to the EditTexts in the layout
     * @param credentials used to fill in text fields
     */
    private void setupSplashFields(final Api.Credentials credentials) {
        final Button loginButton = (Button) findViewById(R.id.splash_login);
        final Button signupButton = (Button) findViewById(R.id.splash_signup);
        final EditText email = (EditText) findViewById(R.id.splash_email);
        final EditText password = (EditText) findViewById(R.id.splash_password);
        final LoginActionListener loginAction = new LoginActionListener(this, email, password, new HttpCallback() {
            /**
             * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
             */
            @Override
            public void on200(Response response) {
                FireIntent.SimpleNoteList(SimpleNoteSplash.this);
                SimpleNoteSplash.this.finish();
            }
        });
        if (credentials.email != null && !credentials.email.equals("")) { email.setText(credentials.email); }
        password.setOnEditorActionListener(loginAction);
        loginButton.setOnClickListener(loginAction);
        signupButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Open a way for the user to create an account
             * @param view that was clicked
             */
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Constants.URL_CREATE_ACCOUNT);
                startActivity(intent);
            }
        });
        // FIXME: This doesn't work from styles.xml
        password.setTypeface(Typeface.SANS_SERIF);
    }
}
