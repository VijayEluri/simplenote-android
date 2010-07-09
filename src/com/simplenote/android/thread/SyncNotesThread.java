package com.simplenote.android.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.simplenote.android.Constants;
import com.simplenote.android.model.Note;
import com.simplenote.android.net.HttpCallback;
import com.simplenote.android.net.SimpleNoteApi;
import com.simplenote.android.persistence.SimpleNoteDao;

public class SyncNotesThread extends Thread {
	public static final int STATE_DONE = 0;
	public static final int STATE_RUNNING = 1;

	private final Handler handler;
	private final SimpleNoteDao dao;
	private final String email;
	private final String token;
	/**
	 * Create a thread with all the pieces it needs to update notes
	 * @param handler Android message handler to post updates to
	 * @param dao interface to the SimpleNote database
	 * @param email of account to sync notes for
	 * @param token authentication token for email
	 */
	public SyncNotesThread(Handler handler, SimpleNoteDao dao, String email, String token) {
		this.handler = handler;
		this.dao = dao;
		this.email = email;
		this.token = token;
	}

	/**
	 * Pulls down notes from the server if the server version is newer than the note in the
	 * database
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// Fetch the notes from the server
		Note[] notes = SimpleNoteApi.index(token, email, HttpCallback.EMPTY);
		Message message = null;
		for (Note serverNote : notes) {
			Note dbNote = dao.retrieveByKey(serverNote.getKey());
			if (dbNote == null || (serverNote.getDateModified().compareTo(dbNote.getDateModified()) > 0)) {
				// if we don't have the note or the note on the server is newer
				// then retrieve from the server and save it
				serverNote = SimpleNoteApi.retrieve(serverNote, token, email, HttpCallback.EMPTY);
				if (dbNote != null) { // if it's already in the db make sure the id is set
					serverNote = serverNote.setId(dbNote.getId());
				}
				dbNote = dao.save(serverNote);
			} else {
				// we have a note and it is up to date
			}
			message = Message.obtain(handler, Constants.MESSAGE_UPDATE_NOTE);
			message.setData(new Bundle());
			message.getData().putSerializable(Note.class.getName(), dbNote);
			message.sendToTarget();
		}
		Message.obtain(handler, Constants.MESSAGE_UPDATE_FINISHED).sendToTarget();
	}
}
