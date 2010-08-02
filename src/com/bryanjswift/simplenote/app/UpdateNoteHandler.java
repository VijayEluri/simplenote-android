package com.bryanjswift.simplenote.app;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.model.Note;

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
	 * Messages are received from SyncNotesTask when a Note is updated or retrieved from the server
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
        // If the note was deleted then send broadcast to sync notes
		if (note.isDeleted()) {
            Log.d(LOGGING_TAG, "Send broadcast to sync notes");
            context.sendBroadcast(new Intent(Constants.BROADCAST_SYNC_NOTES));
		}
		// Only refresh if told to.. should only be told to if it's an update from this Activity
		if (refreshEach && !note.isDeleted()) {
            Log.d(LOGGING_TAG, "Send broadcast to refresh notes");
			context.sendBroadcast(new Intent(Constants.BROADCAST_REFRESH_NOTES));
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
