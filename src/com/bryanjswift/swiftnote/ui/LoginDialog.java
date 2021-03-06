package com.bryanjswift.swiftnote.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.Api.Response;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.widget.LoginActionListener;

/**
 * Activity to get credentials from user
 * @author bryanjswift
 */
public class LoginDialog extends Activity {
	private static final String LOGGING_TAG = Constants.TAG + LoginDialog.class.getName();
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
		Api.Credentials credentials = Preferences.getLoginPreferences(this);
		// Get fields
		final EditText email = (EditText) findViewById(R.id.email);
		final EditText password = (EditText) findViewById(R.id.password);
		// Set values to what is stored in preferences
		email.setText(credentials.email);
		password.setText(credentials.password);
		// When user presses an action button when in the password field authenticate the user
		password.setOnEditorActionListener(new LoginActionListener(this, email, password, new HttpCallback() {
			/**
			 * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
			 */
			@Override
			public void on200(final Response response) {
				Log.d(LOGGING_TAG, "Authentication successful, closing dialog");
				Intent intent = getIntent();
				intent.putExtra(Preferences.EMAIL, email.getText().toString());
				intent.putExtra(Preferences.TOKEN, response.body);
				LoginDialog.this.setResult(Activity.RESULT_OK, intent);
				LoginDialog.this.finish();
			}
		}));
	}
}
