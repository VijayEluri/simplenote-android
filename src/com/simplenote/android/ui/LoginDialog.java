package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.net.Api.Response;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.thread.LoginWithCredentials;

/**
 * Activity to get credentials from user
 * @author bryanjswift
 */
public class LoginDialog extends Activity {
	private static final String LOGGING_TAG = Constants.TAG + "LoginDialog";
	// Fields
	private final String authenticating;
	private final String loggingIn;
	/**
	 * Default constructor for LoginDialog
	 */
	public LoginDialog() {
		super();
		// Get string values displayed as part of dialog use
		authenticating = getString(R.string.status_authenticating);
		loggingIn = getString(R.string.status_loggingin);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set view and a couple flags
		setContentView(R.layout.login);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		// Get credentials
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		// Get fields
		final EditText email = (EditText) findViewById(R.id.email);
		final EditText password = (EditText) findViewById(R.id.password);
		// Set values to what is stored in preferences
		email.setText(credentials.get(Preferences.EMAIL));
		password.setText(credentials.get(Preferences.PASSWORD));
		// When user presses an action button when in the password field authenticate the user
		password.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO: Do I need to kill/dismiss this dialog onPause of the Activity?
				final ProgressDialog dialog = ProgressDialog.show(LoginDialog.this, loggingIn, authenticating);
				Log.d(LOGGING_TAG, "Created progressDialog: " + dialog.toString());
				Log.i(LOGGING_TAG, "Starting login thread");
				final String emailValue = email.getText().toString();
				final HashMap<String,String> credentials =
					Preferences.setLoginPreferences(LoginDialog.this, emailValue, password.getText().toString());
				// Login with credentials here
				(new LoginWithCredentials(LoginDialog.this, credentials, new HttpCallback() {
					/**
					 * @see com.simplenote.android.net.HttpCallback#on200(com.simplenote.android.net.Api.Response)
					 */
					@Override
					public void on200(Response response) {
						dialog.dismiss();
						Intent intent = new Intent();
						intent.putExtra(Preferences.EMAIL, emailValue);
						intent.putExtra(Preferences.TOKEN, response.body);
						setResult(RESULT_OK, intent);
					}
					/**
					 * @see com.simplenote.android.net.HttpCallback#onError(com.simplenote.android.net.Api.Response)
					 */
					@Override
					public void onError(Response response) {
						Toast.makeText(LoginDialog.this, R.string.error_authentication, Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				})).start();
				return false;
			}
		});
	}
}
