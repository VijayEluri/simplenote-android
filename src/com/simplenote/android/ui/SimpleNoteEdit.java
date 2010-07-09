package com.simplenote.android.ui;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.EditText;

import com.simplenote.android.Constants;
import com.simplenote.android.R;
import com.simplenote.android.model.Note;
import com.simplenote.android.persistence.SimpleNoteDao;

/**
 * Handle the note editing
 * @author bryanjswift
 */
public class SimpleNoteEdit extends Activity {
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteEdit";
	// Final variables
	private final SimpleNoteDao dao;
	// Mutable instance variables
	private long mNoteId = 0L;
	private String mOriginalBody = "";
	private boolean mActivityStateSaved = false;
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
		Log.d(LOGGING_TAG, "Running creating new SimpleNoteEdit Activity");
		setContentView(R.layout.edit_note);
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			mNoteId = extras.getLong(BaseColumns._ID);
			mOriginalBody = extras.getString(SimpleNoteDao.BODY);
		} else {
			mNoteId = savedInstanceState.getLong(BaseColumns._ID);
		}
		final Note dbNote = dao.retrieve(mNoteId);
		if (savedInstanceState == null && mOriginalBody == null) {
			mOriginalBody = dbNote.getBody();
		} else if (savedInstanceState != null && mOriginalBody == null) {
			mOriginalBody = savedInstanceState.getString(SimpleNoteDao.BODY);
		}
		setTitle(getString(R.string.app_name) + " - " + dbNote.getTitle());
		((EditText) findViewById(R.id.body)).setText(dbNote.getBody());
	}
	/**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOGGING_TAG, "Resuming SimpleNoteEdit");
		mActivityStateSaved = false;
	}
	/**
	 * Called before on pause, save data here so SimpleEditNote can be relaunched
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LOGGING_TAG, "Saving instance state");
		mActivityStateSaved = true;
		// Make sure the note id is set in the saved state
		outState.putLong(BaseColumns._ID, mNoteId);
		outState.putString(SimpleNoteDao.BODY, mOriginalBody);
	}
	/**
	 * When user leaves this view save the note and set a result
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGGING_TAG, "Firing onPause and saving note");
		// save note to database
		String now = DateFormat.format("yyyy-MM-dd HH:mm:ss.ssssss", new Date()).toString();
		String body = ((EditText) findViewById(R.id.body)).getText().toString();
		// get the note as it is from the db
		final Note dbNote = dao.retrieve(mNoteId);
		Intent intent = getIntent();
		// if text is unchanged send a CANCELLED result, otherwise save and send an OK result
		if (mOriginalBody.equals(body)) {
			setResult(RESULT_CANCELED, intent);
		} else {
			// set new fields on existing note and save it
			final Note note = dao.save(dbNote.setBody(body).setDateModified(now));
			intent.putExtra(BaseColumns._ID, note.getId());
			intent.putExtra(SimpleNoteDao.BODY, note.getBody());
			intent.putExtra(SimpleNoteDao.MODIFY, note.getDateModified());
			setResult(RESULT_OK, intent);
		}
		if (!mActivityStateSaved) {
			finish();
		}
	}
}
