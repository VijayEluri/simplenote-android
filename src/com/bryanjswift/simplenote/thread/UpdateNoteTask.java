package com.bryanjswift.simplenote.thread;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.manager.Connectivity;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.Api;
import com.bryanjswift.simplenote.net.ServerCreateCallback;
import com.bryanjswift.simplenote.net.ServerSaveCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;

/**
 * Asyncronously send the note to the server
 * @author bryanjswift
 */
public class UpdateNoteTask extends AsyncTask<Note, Void, Void> {
    private static final String LOGGING_TAG = Constants.TAG + UpdateNoteTask.class.getSimpleName();
    /** Context for the update */
    private final Context context;
    /**
     * Default constructor with a context
     * @param context from which to get preferences
     */
    public UpdateNoteTask(Context context) {
        this.context = context;
    }
    /**
     * Send the note to SimpleNote servers using the correct SimpleNoteApi call
     * @param notes to be updated (only the first note is handled)
     * @return null
     */
    @Override
    protected Void doInBackground(Note... notes) {
        if (Connectivity.hasInternet(context)) {
            final Note note = notes[0];
            // Note modified, list refreshed by onResume
            // Send updated note to the server
            final Api.Credentials credentials = Preferences.getLoginPreferences(context);
            if (!note.getKey().equals(Constants.DEFAULT_KEY) && note.isDeleted()) {
                Log.d(LOGGING_TAG, "Deleting note on the server");
                SimpleNoteApi.delete(note, credentials, new ServerSaveCallback(context, note));
            } else if (note.getKey().equals(Constants.DEFAULT_KEY)) {
                Log.d(LOGGING_TAG, "Creating a new note on the server");
                SimpleNoteApi.create(note, credentials, new ServerCreateCallback(context, note));
            } else {
                Log.d(LOGGING_TAG, String.format("Sending note '%s' to SimpleNoteApi", note.getKey()));
                SimpleNoteApi.update(note, credentials, new ServerSaveCallback(context, note));
            }
        } else {
            Log.d(LOGGING_TAG, "Unable to send note, no connectivity");
        }
        return null;
    }
}
