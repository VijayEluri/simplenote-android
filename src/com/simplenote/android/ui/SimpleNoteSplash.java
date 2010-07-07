package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
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
		if (credentials.containsKey(Preferences.EMAIL) &&
				(credentials.containsKey(Preferences.TOKEN) || credentials.containsKey(Preferences.PASSWORD))) {
			// valid token stored
			Log.d(LOGGING_TAG, "Auth information stored, going to list");
			FireIntent.SimpleNoteList(this);
			this.finish();
		} else {
			// set up events for the splash screen
			Log.d(LOGGING_TAG, "Need to get credentials, setup events for the splash screen");
			setupSplashFields();
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
					if (value.toString().equals(initial)) {
						field.setTransformationMethod(null);
					}
				}
			};
		email.setOnFocusChangeListener(new TextAsLabelFocusChangeListener(email, getString(R.string.email)));
		password.setOnFocusChangeListener(passwordFocusChangeListener);
	}
}
