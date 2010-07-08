package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.widget.LoginActionListener;

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
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		// Get fields
		final EditText email = (EditText) findViewById(R.id.email);
		final EditText password = (EditText) findViewById(R.id.password);
		// Set values to what is stored in preferences
		email.setText(credentials.get(Preferences.EMAIL));
		password.setText(credentials.get(Preferences.PASSWORD));
		// When user presses an action button when in the password field authenticate the user
		password.setOnEditorActionListener(new LoginActionListener(this, email, password));
	}
}
