package com.bryanjswift.simplenote.net;

import android.app.Activity;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.Api.Response;

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
	 * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
	 */
	public void on200(Response response) {
		super.on200(response);
		Log.d(LOGGING_TAG, "Updating key for created note");
		// this only modifies the key, no need to refresh list
		dao.save(this.note.setKey(response.body));
	}
}