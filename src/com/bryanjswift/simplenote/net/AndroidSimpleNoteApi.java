package com.bryanjswift.simplenote.net;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;

/**
 * Adds Android specific handling of API methods
 * @author bryanjswift
 */
public class AndroidSimpleNoteApi extends SimpleNoteApi {
	private static final String LOGGING_TAG = Constants.TAG + "AndroidSimpleNoteApi";
	// Private immutable fields
	private final Context context;
	private final SimpleNoteDao dao;
	private final Credentials credentials;
	private final Handler handler;
	/**
	 * Setup needed fields from the context
	 * @param context under which the API calls will be made
	 * @param handler to handle note updates
	 */
	public AndroidSimpleNoteApi(final Context context, final Handler handler) {
		this.context = context;
		this.dao = new SimpleNoteDao(context);
		this.credentials = Preferences.getLoginPreferences(context);
		this.handler = handler;
	}
	/**
	 * Pull down notes from the SimpleNote server and if the server note is newer update the note
	 * in the database
	 */
	private void syncDown() {
		// Fetch the notes from the server
		Log.d(LOGGING_TAG, "::syncDown");
		Note[] notes = SimpleNoteApi.index(credentials, HttpCallback.EMPTY);
		Message message = null;
		for (Note serverNote : notes) {
			Note dbNote = dao.retrieveByKey(serverNote.getKey());
			if (dbNote == null || (serverNote.getDateModified().compareTo(dbNote.getDateModified()) > 0)) {
				// if we don't have the note or the note on the server is newer
				// then retrieve from the server and save it
				serverNote = SimpleNoteApi.retrieve(serverNote, credentials, HttpCallback.EMPTY);
				if (dbNote != null) { // if it's already in the db make sure the id is set
					serverNote = serverNote.setId(dbNote.getId());
				}
				serverNote = serverNote.setSynced(true);
				dbNote = dao.save(serverNote);
				message = Message.obtain(handler, Constants.MESSAGE_UPDATE_NOTE);
				final Bundle data = new Bundle();
				data.putSerializable(Note.class.getName(), dbNote);
				message.setData(data);
				message.sendToTarget();
			} else {
				// we have a note and it is up to date or more recent than the note on the server
			}
		}
	}
	/**
	 * Get unsynchronized notes from the database and push them up to the SimpleNote server
	 */
	private void syncUp() {
		// Fetch the notes from the database
		Log.d(LOGGING_TAG, "::syncUp");
		Note[] notes = dao.retrieveUnsynced();
		for (Note dbNote : notes) {
			if (dbNote.getKey().equals(Constants.DEFAULT_KEY)) {
				SimpleNoteApi.create(dbNote, credentials, new ServerCreateCallback(context, dbNote));
			} else {
				SimpleNoteApi.update(dbNote, credentials, new ServerSaveCallback(context, dbNote));
			}
		}
	}
	/**
	 * Send appropriate messages to the handler and call synchronization methods in order
	 */
	public void sync() {
		Message.obtain(handler, Constants.MESSAGE_UPDATE_STARTED).sendToTarget();
		syncDown();
		syncUp();
		Message.obtain(handler, Constants.MESSAGE_UPDATE_FINISHED).sendToTarget();
	}
}
