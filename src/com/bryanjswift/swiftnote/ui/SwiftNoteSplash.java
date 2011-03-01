package com.bryanjswift.swiftnote.ui;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.app.Notifications;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.Api.Response;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.service.DailyService;
import com.bryanjswift.swiftnote.service.SyncService;
import com.bryanjswift.swiftnote.widget.LoginActionListener;
import com.bryanjswift.swiftnote.widget.RegisterActionListener;

/**
 * Main Activity for SwiftNote application
 * @author bryanjswift
 */
public class SwiftNoteSplash extends Activity {
    private static final String LOGGING_TAG = Constants.TAG + "SwiftNoteSplash";
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
            FireIntent.List(this);
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
             * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
             */
            @Override
            public void on200(Response response) {
                FireIntent.List(SwiftNoteSplash.this);
                SwiftNoteSplash.this.finish();
            }
        });
        final RegisterActionListener registerAction = new RegisterActionListener(this, email, password);
        if (credentials.email != null && !credentials.email.equals("")) { email.setText(credentials.email); }
        password.setOnEditorActionListener(loginAction);
        loginButton.setOnClickListener(loginAction);
        signupButton.setOnClickListener(registerAction);
        // FIXME: This doesn't work from styles.xml
        password.setTypeface(Typeface.SANS_SERIF);
    }
}
