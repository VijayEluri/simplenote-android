package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;

import com.simplenote.android.R;
import com.simplenote.android.Preferences;
import com.simplenote.android.persistence.SimpleNoteDao;

/**
 * Main Activity for SimpleNote application
 * @author bryanjswift
 */
public class SimpleNote extends Activity {
	/** Database Adapter to simplify interactions with the SQLite database */
	private final SimpleNoteDao dao;
	public SimpleNote() {
		super();
		dao = new SimpleNoteDao(this);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		setContentView(R.layout.main);
		if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.TOKEN)) { // valid token stored
			// sync notes
			// start note list activity
		} else if (credentials.containsKey(Preferences.EMAIL) && credentials.containsKey(Preferences.PASSWORD)) { // token expired
			// get new token from API
			// when API successfully returns
				// sync notes
				// start note list activity
		} else {
			// set splash layout?
			// should splash be the main Activity that starts SimpleNote if logged in?
			// start login dialog for result
			// when dialog successfully returns
				// sync notes
				// start note list activity
		}
	}
	/**
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
	/**
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	/**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}
	/**
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}
	/**
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}
}
