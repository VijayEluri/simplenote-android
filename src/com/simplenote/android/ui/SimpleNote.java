package com.simplenote.android.ui;

import android.app.Activity;
import android.os.Bundle;

import com.simplenote.android.persistence.SimpleNoteDao;

/**
 * Main Activity for SimpleNote application
 * @author bryanjswift
 */
public class SimpleNote extends Activity {
	/** Database Adapter to simplify interactions with the Sqlite database */
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
