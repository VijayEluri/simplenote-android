package com.bryanjswift.simplenote.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;

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
	private boolean mNoteSaved = false;
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
		getWindow().setFormat(PixelFormat.RGBA_8888);
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
		final String title;
		if (dbNote != null) {
			title = dbNote.getTitle();
			((EditText) findViewById(R.id.note_body)).setText(dbNote.getBody());
		} else {
			title = getString(R.string.new_note);
		}
		((TextView) findViewById(R.id.note_title)).setText(title);
        findViewById(R.id.note_delete).setOnClickListener(new View.OnClickListener() {
            /**
             * Perform deletion of the note
             * @param view
             */
            public void onClick(View view) {
                delete();
            }
        });
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
		outState.putInt(Constants.REQUEST_KEY, Constants.REQUEST_EDIT);
	}
	/**
	 * When user leaves this view save the note and set a result
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGGING_TAG, "Firing onPause and handling note saving if needed");
		// if text is unchanged send a CANCELLED result, otherwise save and send an OK result
		if (needsSave() && !mActivityStateSaved) {
			save();
		} else {
			Intent intent = getIntent();
			setResult(RESULT_CANCELED, intent);
		}
	}
	/**
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			handled = handleBackPressed();
		}
		if (!handled) {
			handled = super.onKeyDown(keyCode, event);
		}
		return handled;
	}
	/**
	 * The logic to handle the press of the back button
	 * @return whether or not the event was handled
	 */
	private boolean handleBackPressed() {
		Log.d(LOGGING_TAG, "Back button pressed");
		boolean handled = false;
		if (needsSave()) {
			// save finishes the Activity with an OK result
			save();
			handled = true;
		}
		return handled;
	}
	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_edit, menu);
		return true;
	}
	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				save(); // save returns ok
				return true;
			case R.id.menu_delete:
				delete(); // delete returns ok if there was a note to delete, cancelled otherwise
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	/**
	 * Checks if the body of the note has been updated compared to what was in the DB
	 * when this Activity was created
	 * @return whether or note the note body has changed
	 */
	private boolean needsSave() {
		final String body = ((EditText) findViewById(R.id.note_body)).getText().toString();
		return !(mNoteSaved || mOriginalBody.equals(body));
	}
	/**
	 * Saves the note with data from the view and finishes this Activity with an OK result
	 */
	private void save() {
		final String body = ((EditText) findViewById(R.id.note_body)).getText().toString();
		final String now = Constants.serverDateFormat.format(new Date());
		final Intent intent = getIntent();
		// get the note as it is from the db, set new fields values and save it
		final Note dbNote = dao.retrieve(mNoteId);
		final Note note;
		if (!(dbNote == null || dbNote.getKey().equals(Constants.DEFAULT_KEY))) {
			note = dao.save(dbNote.setBody(body).setDateModified(now).setSynced(false));
			Log.d(LOGGING_TAG, String.format("Saved the note '%d' with updated values", note.getId()));
		} else {
			note = dao.save(new Note(body, now));
			Log.d(LOGGING_TAG, String.format("Created the note '%d'", note.getId()));
		}
		mNoteId = note.getId();
		mNoteSaved = true;
		intent.putExtra(Note.class.getName(), note);
		setResult(RESULT_OK, intent);
		finish();
	}
	/**
	 * Deletes the note if it exists in the db otherwise cancel
	 */
	private void delete() {
		final Note dbNote = dao.retrieve(mNoteId);
		final Intent intent = getIntent();
		if (dbNote != null) {
			Log.d(LOGGING_TAG, "Note exists, marking it as deleted and finishing successfully");
			dao.delete(dbNote);
			// Have to re-retrieve because deleting doesn't update the note passed to delete
			intent.putExtra(Note.class.getName(), dao.retrieve(mNoteId));
			setResult(RESULT_OK, intent);
		} else {
			Log.d(LOGGING_TAG, "Note doesn't exist, cancelling");
			setResult(RESULT_CANCELED, intent);
		}
		mNoteSaved = true;
		finish();
	}
}
