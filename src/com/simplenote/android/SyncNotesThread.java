package com.simplenote.android;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class SyncNotesThread extends Thread {
	public static final int STATE_DONE = 0;
	public static final int STATE_RUNNING = 1;

	private final Handler mHandler;
	private final NotesDbAdapter dbHelper;
	private final String email;
	private final String token;

	public SyncNotesThread(Handler h, NotesDbAdapter dbHelper, String email, String token) {
		mHandler = h;
		this.dbHelper = dbHelper;
		this.email = email;
		this.token = token;
	}

	@Override
	public void run() {
		// Fetch the notes from the server
		APIHelper apiHelper = new APIHelper();
		apiHelper.clearAndRefreshNotes(dbHelper, token, email);
		mHandler.sendMessage(new Message());
	}
}