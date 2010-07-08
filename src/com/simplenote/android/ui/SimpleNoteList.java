package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.persistence.SimpleNoteDao;
import com.simplenote.android.thread.LoginWithCredentials;
import com.simplenote.android.thread.SyncNotesThread;

/**
 * 'Main' Activity to List notes
 * @author bryanjswift
 */
public class SimpleNoteList extends ListActivity {
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteList";
	/** Interface for accessing the SimpleNote database on the device */
	private final SimpleNoteDao dao;
	/**
	 * Create a dao to store using this as the context
	 */
	public SimpleNoteList() {
		super();
		this.dao = new SimpleNoteDao(this);
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			Log.d(LOGGING_TAG, "Resuming from a saved state");
			FireIntent.EditNote(this, savedInstanceState.getLong(BaseColumns._ID),
					savedInstanceState.getString(SimpleNoteDao.BODY));
		}
		// Set content view based on Notes currently in the database
		setContentView(R.layout.notes_list);
		Cursor notes = dao.retrieveAll();
		// Create an array to specify the fields we want to display in the list
		String[] from = new String[] { SimpleNoteDao.TITLE, SimpleNoteDao.MODIFY };
		// and an array of the fields we want to bind those fields to
		int[] to = new int[] { R.id.text_title, R.id.text_date };
		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notesAdapter = new SimpleCursorAdapter(this, R.layout.notes_row, notes, from, to);
		setListAdapter(notesAdapter);
		// check the token exists first and if not authenticate with existing username/password
		HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
		if (credentials.containsKey(Preferences.TOKEN)) {
			// sync notes in a background thread
			syncNotes(credentials.get(Preferences.EMAIL), credentials.get(Preferences.TOKEN));
		} else {
			(new LoginWithCredentials(this, credentials)).start();
		}
	}
	/**
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(LOGGING_TAG, String.format("onActivityResult firing with resultCode: %d", resultCode));
		switch (requestCode) {
			case Constants.REQUEST_LOGIN: handleSigninResult(resultCode, data); break;
			case Constants.REQUEST_EDIT: handleNoteEditResult(resultCode, data); break;
		}
	}
	/**
	 *Edit notes when clicked
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		FireIntent.EditNote(this, id, null);
	}
	/**
	 * If SimpleNoteEdit saved state then retrieve it and go back to editing
	 * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		if (state != null) {
			Log.d(LOGGING_TAG, "Resuming from a saved state");
			FireIntent.EditNote(this, state.getLong(BaseColumns._ID), state.getString(SimpleNoteDao.BODY));
		}
	}
	/**
	 * Message handler which should update the UI when a message with a Note is received
	 */
	private Handler updateNoteHandler = new Handler() {
		/**
		 * Messages are received from SyncNotesThread when a Note is updated or retrieved from the server
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// update the UI with the new note by forcing the ListAdapter to requery
			runOnUiThread(new Runnable() {
				public void run() {
					((CursorAdapter) getListAdapter()).getCursor().requery();
				}
			});
		}
	};
	/**
	 * Start up a note syncing thread
	 * @param email account identifier for notes
	 * @param auth token used for access after login API call
	 */
	private void syncNotes(String email, String auth) {
		final Thread t = new SyncNotesThread(updateNoteHandler, dao, email, auth);
		t.start();
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity start
	 * @param resultCode how the LoginDialog Activity finished
	 * @param data the intent that started the LoginDialog Activity
	 */
	private void handleSigninResult(final int resultCode, final Intent data) {
		// Assume this only gets called when LoginDialog completed successfully
		if (resultCode == RESULT_OK) {
			final Bundle extras = data.getExtras();
			syncNotes(extras.getString(Preferences.EMAIL), extras.getString(Preferences.TOKEN));
		}
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity
	 * @param resultCode how the SimpleNoteEdit Activity finished
	 * @param data the intent that started the SimpleNoteEdit Activity
	 */
	private void handleNoteEditResult(final int resultCode, final Intent data) {
		switch (resultCode) {
			case RESULT_OK:
				// Note modified, refresh the list
				Message message = Message.obtain(updateNoteHandler, Constants.MESSAGE_UPDATE_NOTE);
				message.sendToTarget();
				// Send updated note to the server
				break;
			case RESULT_CANCELED:
				// not modified
				// Since none of the notes were changed nothing should need to be done here
				break;
		}
	}
}
