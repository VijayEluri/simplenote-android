package com.bryanjswift.simplenote.thread;

import android.app.Activity;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.ServerCreateCallback;
import com.bryanjswift.simplenote.net.ServerSaveCallback;
import com.bryanjswift.simplenote.net.SimpleNoteApi;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;

public class SendNotesThread extends Thread {
	private final Activity context;
	private final SimpleNoteDao dao;
	private final String email;
	private final String token;
	/**
	 * Create a thread with all the pieces it needs to update notes
	 * @param context Android Activity context in which this thread runs
	 * @param dao interface to the SimpleNote database
	 * @param email of account to sync notes for
	 * @param token authentication token for email
	 */
	public SendNotesThread(Activity context, SimpleNoteDao dao, String email, String token) {
		this.context = context;
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
		Note[] notes = dao.retrieveUnsynced();
		for (Note dbNote : notes) {
			if (dbNote.getKey().equals(Constants.DEFAULT_KEY)) {
				SimpleNoteApi.create(dbNote, token, email, new ServerCreateCallback(context, dbNote));
			} else {
				SimpleNoteApi.update(dbNote, token, email, new ServerSaveCallback(context, dbNote));
			}
		}
	}
}
