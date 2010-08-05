package com.bryanjswift.simplenote.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.R;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.Api;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;
import com.bryanjswift.simplenote.thread.LoginTask;
import com.bryanjswift.simplenote.thread.UpdateNoteTask;
import com.bryanjswift.simplenote.widget.NotesAdapter;

/**
 * 'Main' Activity to List notes
 * @author bryanjswift
 */
public class SimpleNoteList extends NoteListActivity {
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteList";
	/**
	 * Create a dao to store using this as the context
	 */
	public SimpleNoteList() {
		super();
	}
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
        FireIntent.finishIfUnauthorized(this);
		int scrollY = 0;
		if (savedState != null && savedState.getInt(Constants.REQUEST_KEY) == Constants.REQUEST_EDIT) {
			Log.d(LOGGING_TAG, "Resuming note editing from a saved state");
			FireIntent.EditNote(this, savedState.getLong(BaseColumns._ID), savedState.getString(SimpleNoteDao.BODY));
		} else if (savedState != null) {
			scrollY = savedState.getInt(SCROLL_POSITION, 0);
		}
		Log.d(LOGGING_TAG, "Firing up the note list");
		// Now get notes and create a note adapter and set it to display
        final Object data = getLastNonConfigurationInstance();
        final Note[] notes;
        if (data == null) {
            notes = dao.retrieveAll();
        } else {
            notes = (Note[]) data;
        }
		setListAdapter(new NotesAdapter(this, notes));
        // Set content view based on Notes currently in the database
        setContentView(R.layout.notes_list);
		// Make sure onCreateContextMenu is called for long press of notes
		registerForContextMenu(getListView());
		// check the token exists first and if not authenticate with existing username/password
        final Api.Credentials credentials = Preferences.getLoginPreferences(this);
		if (credentials.hasAuth()) {
			// sync notes in a background thread
			if (data == null) {
                syncNotes();
            }
		} else {
			(new LoginTask(this, credentials)).execute();
		}
        // bind click listener to new note button
        findViewById(R.id.note_add).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FireIntent.EditNote(SimpleNoteList.this, Constants.DEFAULT_ID, "");
            }
        });
		// restore the scroll position
		getListView().scrollTo(0, scrollY);
	}
	/**
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(LOGGING_TAG, String.format("onActivityResult firing with resultCode: %d", resultCode));
		switch (requestCode) {
			case Constants.REQUEST_LOGIN: handleSigninResult(resultCode); break;
			case Constants.REQUEST_EDIT: handleNoteEditResult(resultCode, data); break;
		}
	}
	/**
	 * Deal with the results of the REQUEST_LOGIN Activity start
	 * @param resultCode how the LoginDialog Activity finished
     */
	private void handleSigninResult(final int resultCode) {
		// Assume this only gets called when LoginDialog completed successfully
		if (resultCode == RESULT_OK) {
			syncNotes();
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
                (new UpdateNoteTask(this))
                        .execute((Note) data.getExtras().getSerializable(Note.class.getName()));
				break;
			case RESULT_CANCELED:
				// not modified
				// Since the note wasn't changed nothing should need to be done here
				break;
		}
	}
	/**
	 * Internal method to trigger a note refresh
	 * @param note causing the refresh
	 */
	protected void updateNotesFor(final Note note) {
		// Note modified, refresh the list
		final Message message = Message.obtain(updateNoteHandler, Constants.MESSAGE_UPDATE_NOTE);
		message.setData(new Bundle());
		final Bundle data = message.getData();
		data.putSerializable(Note.class.getName(), note);
		message.sendToTarget();
	}
	/**
	 * Pull notes from database and update the NotesAdapter
	 */
	protected void refreshNotes() {
        Log.d(LOGGING_TAG, "Refreshing notes from DB");
        final NotesAdapter adapter = ((NotesAdapter) getListAdapter());
        adapter.setNotes(dao.retrieveAll());
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
                updateShadow();
			}
		});
	}
}
