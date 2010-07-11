package com.simplenote.android.net;

import android.app.Activity;
import android.util.Log;

import com.simplenote.android.Constants;
import com.simplenote.android.model.Note;
import com.simplenote.android.net.Api.Response;

/**
 * Specialized ServerSaveCallback for creating a new note
 * @author bryanjswift
 */
public class ServerCreateCallback extends ServerSaveCallback {
	private static final String LOGGING_TAG = Constants.TAG + "ServerCreateCallback";
	/**
	 * Create a callback related to the note which was created
	 * @param note trying to be saved to the server
	 */
	public ServerCreateCallback(final Activity context, final Note note) {
		super(context, note);
	}
	/**
	 * Update the key for the note that was saved
	 * @see com.simplenote.android.net.HttpCallback#on200(com.simplenote.android.net.Api.Response)
	 */
	public void on200(Response response) {
		super.on200(response);
		Log.d(LOGGING_TAG, "Updating key for created note");
		// this only modifies the key, no need to refresh list
		dao.save(this.note.setKey(response.body));
	}
}