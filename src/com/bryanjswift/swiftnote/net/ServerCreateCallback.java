package com.bryanjswift.swiftnote.net;

import android.content.Context;
import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.model.Note;
import com.bryanjswift.swiftnote.net.Api.Response;

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
	public ServerCreateCallback(final Context context, final Note note) {
		super(context, note);
	}
	/**
	 * Update the key for the note that was saved
	 * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
	 */
	public void on200(Response response) {
		super.on200(response);
		Log.d(LOGGING_TAG, "Updating key for created note");
		// this only modifies the key, no need to refresh list
		dao.save(this.note.setKey(response.body));
	}
}
