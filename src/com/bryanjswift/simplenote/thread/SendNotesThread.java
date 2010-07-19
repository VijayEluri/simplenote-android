package com.bryanjswift.simplenote.thread;

import com.bryanjswift.simplenote.net.AndroidSimpleNoteApi;

public class SendNotesThread extends Thread {
	private final AndroidSimpleNoteApi api;
	/**
	 * Create a thread with all the pieces it needs to update notes
	 * @param api implementation of SimpleNoteApi that relies on Android classes
	 */
	public SendNotesThread(final AndroidSimpleNoteApi api) {
		this.api = api;
	}
	/**
	 * Pulls down notes from the server if the server version is newer than the note in the
	 * database
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		api.syncUp();
	}
}
