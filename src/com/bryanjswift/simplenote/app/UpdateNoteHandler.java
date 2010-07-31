package com.bryanjswift.simplenote.app;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.ServerSaveCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;

/**
 * Message handler which should update the UI when a message with a Note is received
 * @author bryanjswift
 */
public class UpdateNoteHandler extends Handler {
	private static final String LOGGING_TAG = Constants.TAG + "UpdateNoteHandler";
	private final Context context;
	private final boolean refreshEach;
	public UpdateNoteHandler(final Context context, final boolean refreshEach) {
		this.context = context;
		this.refreshEach = refreshEach;
	}
	public UpdateNoteHandler(final Context context) {
		this(context, false);
	}
	/**
	 * Messages are received from SyncNotesThread when a Note is updated or retrieved from the server
	 * @see android.os.Handler#handleMessage(android.os.Message)
	 */
	@Override
	public void handleMessage(final Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case Constants.MESSAGE_UPDATE_NOTE: if (refreshEach) { handleUpdateNote(msg); } break;
			case Constants.MESSAGE_UPDATE_FINISHED: handleUpdateFinished(msg); break;
			case Constants.MESSAGE_UPDATE_STARTED: handleUpdateStarted(msg); break;
			default: break;
		}
	}
	/**
	 * Handle the update note message
	 * @param msg with Note information
	 */
	private void handleUpdateNote(Message msg) {
		// update the UI with the new note
		final Note note = (Note) msg.getData().getSerializable(Note.class.getName());
		if (note.isDeleted()) {
			final Preferences.Credentials credentials = Preferences.getLoginPreferences(context);
			final String email = credentials.email;
			final String auth = credentials.auth;
			if (!note.getKey().equals(Constants.DEFAULT_KEY) && note.isDeleted()) {
				Log.d(LOGGING_TAG, "Deleting note on the server");
				SimpleNoteApi.delete(note, auth, email, new ServerSaveCallback(context, note));
			}
		}
		// Only refresh if told to.. should only be told to if it's an update from this Activity
		if (refreshEach) {
			context.sendBroadcast(new Intent(Constants.BROADCAST_UPDATE_NOTES));
		}
	}
	/**
	 * Handle the update started message
	 * @param msg with any relevant information
	 */
	private void handleUpdateStarted(final Message msg) {
		Notifications.Syncing(context);
	}
	/**
	 * Handle the update finished message
	 * @param msg with any relevant information
	 */
	private void handleUpdateFinished(final Message msg) {
		Notifications.CancelSyncing(context);
	}
}
