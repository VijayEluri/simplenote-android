package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.thread.LoginWithExistingCredentials;
import com.simplenote.android.view.TextAsLabelFocusChangeListener;

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
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		setContentView(R.layout.splash);
		if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.TOKEN)) {
			// valid token stored
			FireIntent.SimpleNoteList(this);
		} else if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.PASSWORD)) {
			// TODO: this should not be done here, it should be done on the listing page because we don't want
			// to show the splash page if we don't have to
			// token expired, get new token from API
			(new LoginWithExistingCredentials(this, credentials)).start();
		} else {
			// set up events for the splash screen
			setupSplashFields();
		}
	}
	/**
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case Constants.REQUEST_LOGIN: handleSigninResult(resultCode, data); break;
		}
	}
	/**
	 * Attach focus event listeners to the EditTexts in the layout
	 */
	private void setupSplashFields() {
		EditText email = (EditText) findViewById(R.id.email);
		EditText password = (EditText) findViewById(R.id.password);
		TextAsLabelFocusChangeListener passwordFocusChangeListener =
			new TextAsLabelFocusChangeListener(password, getString(R.string.password)) {
				/**
				 * @see com.simplenote.android.view.TextAsLabelFocusChangeListener#onFocus()
				 */
				@Override
				protected void onFocus() {
					super.onFocus();
					field.setTransformationMethod(new PasswordTransformationMethod());
				}
				/**
				 * @see com.simplenote.android.view.TextAsLabelFocusChangeListener#onBlur()
				 */
				@Override
				protected void onBlur() {
					super.onBlur();
					Editable value = field.getText();
					Log.d(Constants.TAG + "passwordFocusChangeListener", "Initial: " + initial + " :: Current: " + value.toString());
					if (value.toString().equals(initial)) {
						field.setTransformationMethod(null);
					}
				}
			};
		email.setOnFocusChangeListener(new TextAsLabelFocusChangeListener(email, getString(R.string.email)));
		password.setOnFocusChangeListener(passwordFocusChangeListener);
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity start
	 * @param resultCode how the LoginDialog Activity finished
	 * @param data the intent that started the LoginDialog Activity
	 */
	private void handleSigninResult(final int resultCode, final Intent data) {
		// Assume this only gets called when LoginDialog completed successfully
		FireIntent.SimpleNoteList(this);
	}
}
