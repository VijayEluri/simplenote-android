package com.bryanjswift.swiftnote.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.R;
import com.bryanjswift.swiftnote.model.Note;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.persistence.SwiftNoteDao;
import com.bryanjswift.swiftnote.thread.LoginTask;
import com.bryanjswift.swiftnote.thread.UpdateNoteTask;
import com.bryanjswift.swiftnote.widget.NotesAdapter;

/**
 * 'Main' Activity to List notes
 * @author bryanjswift
 */
public class SwiftNoteList extends NoteListActivity {
    private static final String LOGGING_TAG = Constants.TAG + "SwiftNoteList";
    /**
     * Create a dao to store using this as the context
     */
    public SwiftNoteList() {
        super();
    }
    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        FireIntent.finishIfUnauthorized(this);
        if (savedState != null && savedState.getInt(Constants.REQUEST_KEY) == Constants.REQUEST_EDIT) {
            Log.d(LOGGING_TAG, "Resuming note editing from a saved state");
            FireIntent.EditNote(this, savedState.getLong(BaseColumns._ID), savedState.getString(SwiftNoteDao.BODY));
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
                FireIntent.EditNote(SwiftNoteList.this, Constants.DEFAULT_ID, "");
            }
        });
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
     * Deal with the results of the REQUEST_EDIT Activity
     * @param resultCode how the SwiftNoteEdit Activity finished
     * @param data the intent that started the SwiftNoteEdit Activity
     */
    private void handleNoteEditResult(final int resultCode, final Intent data) {
        switch (resultCode) {
            case Constants.RESULT_NEW: // fall through to OK result
            case RESULT_OK:
                // Should not have null data here
                final Note note = (Note) data.getExtras().get(Note.class.getName());
                (new UpdateNoteTask(this)).execute(note);
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
