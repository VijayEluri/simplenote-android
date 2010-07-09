package com.simplenote.android.ui;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.simplenote.android.Constants;
import com.simplenote.android.Preferences;
import com.simplenote.android.R;
import com.simplenote.android.model.Note;
import com.simplenote.android.net.Api.Response;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.net.SimpleNoteApi;
import com.simplenote.android.persistence.SimpleNoteDao;
import com.simplenote.android.thread.LoginWithCredentials;
import com.simplenote.android.thread.SyncNotesThread;
import com.simplenote.android.widget.NotesAdapter;

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
		Note[] notes = dao.retrieveAll();
		// Now create a simple cursor adapter and set it to display
		ListAdapter notesAdapter = new NotesAdapter(this, notes);
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
		public void handleMessage(final Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constants.MESSAGE_UPDATE_NOTE: handleUpdateNote(msg); break;
				case Constants.MESSAGE_UPDATE_FINISHED: break;
				default: break;
			}
		}
		/**
		 * Handle the update note message
		 * @param msg with Note information
		 */
		private void handleUpdateNote(Message msg) {
			// update the UI with the new note by forcing the ListAdapter to requery
			runOnUiThread(new Runnable() {
				public void run() {
					final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
					adapter.setNotes(dao.retrieveAll());
					adapter.notifyDataSetChanged();
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
				final Note dbNote = dao.retrieve(data.getExtras().getLong(BaseColumns._ID));
				Log.d(LOGGING_TAG, String.format("Sending note '%s' to SimpleNoteApi", dbNote.getKey()));
				// Note modified, refresh the list
				Message message = Message.obtain(updateNoteHandler, Constants.MESSAGE_UPDATE_NOTE);
				message.setData(new Bundle());
				message.getData().putSerializable(Note.class.getName(), dbNote);
				message.sendToTarget();
				// Send updated note to the server
				final HashMap<String,String> credentials = Preferences.getLoginPreferences(this);
				SimpleNoteApi.update(dbNote, credentials.get(Preferences.TOKEN), credentials.get(Preferences.EMAIL), new HttpCallback() {
					/**
					 * @see com.simplenote.android.net.HttpCallback#on200(com.simplenote.android.net.Api.Response)
					 */
					@Override
					public void on200(Response response) {
						super.on200(response);
						// Set needs sync to false in db
					}
					/**
					 * @see com.simplenote.android.net.HttpCallback#on401(com.simplenote.android.net.Api.Response)
					 */
					@Override
					public void on401(Response response) {
						super.on401(response);
						// User unauthorized, get new token and try again
					}
					/**
					 * @see com.simplenote.android.net.HttpCallback#on404(com.simplenote.android.net.Api.Response)
					 */
					@Override
					public void on404(Response response) {
						super.on404(response);
						// Note doesn't exist, create it
					}
				});
				break;
			case RESULT_CANCELED:
				// not modified
				// Since the note wasn't changed nothing should need to be done here
				break;
		}
	}
}
