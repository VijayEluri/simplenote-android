package com.simplenote.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.EditText;

import com.simplenote.android.R;
import com.simplenote.android.model.Note;
import com.simplenote.android.persistence.SimpleNoteDao;

/**
 * Handle the note editing
 * @author bryanjswift
 */
public class SimpleNoteEdit extends Activity {
	// Final variables
	private final SimpleNoteDao dao;
	// Mutable instance 
	private long mNoteId = 0L;
	/**
	 * Default constructor to setup final fields
	 */
	public SimpleNoteEdit() {
		this.dao = new SimpleNoteDao(this);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_note);
		if (savedInstanceState == null) {
			mNoteId = getIntent().getExtras().getLong(BaseColumns._ID);
		} else {
			mNoteId = savedInstanceState.getLong(BaseColumns._ID);
		}
		Note dbNote = dao.retrieve(mNoteId);
		setTitle(getString(R.string.app_name) + " - " + dbNote.getTitle());
		((EditText) findViewById(R.id.body)).setText(dbNote.getBody());
	}
	/**
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// save note
	}
	/**
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Make sure the note id is set in the saved state
		outState.putLong(BaseColumns._ID, mNoteId);
	}
}
