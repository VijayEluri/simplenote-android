package com.bryanjswift.simplenote.net;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.Preferences;
import com.bryanjswift.simplenote.model.Note;
import com.bryanjswift.simplenote.net.Api.Response;
import com.bryanjswift.simplenote.persistence.SimpleNoteDao;

/**
 * Specialized HttpCallback to handle respsonses when trying to save notes to the server
 * @author bryanjswift
 */
public class ServerSaveCallback extends HttpCallback {
	private static final String LOGGING_TAG = Constants.TAG + "ServerSaveCallback";
	// Inheritable fields
	protected final SimpleNoteDao dao;
	protected final Context context;
	protected final Note note;
	/**
	 * Create a callback related to the note which was saved
	 * @param note trying to be saved to the server
	 */
	public ServerSaveCallback(final Context context, final Note note) {
		super();
		this.note = note;
		this.context = context;
		this.dao = new SimpleNoteDao(context);
	}
	/**
	 * @see com.bryanjswift.simplenote.net.HttpCallback#on200(com.bryanjswift.simplenote.net.Api.Response)
	 */
	@Override
	public void on200(final Response response) {
		super.on200(response);
		Log.d(LOGGING_TAG, String.format("Successfully saved note '%s' on server", response.body));
		// Set needs sync to false in db
		dao.markSynced(note);
	}
	/**
	 * @see com.bryanjswift.simplenote.net.HttpCallback#on401(com.bryanjswift.simplenote.net.Api.Response)
	 */
	@Override
	public void on401(final Response response) {
		super.on401(response);
		Log.d(LOGGING_TAG, "Unauthorized to save note on server");
		// TODO: User unauthorized, automatically attempt API login, if login fails post notification
	}
	/**
	 * @see com.bryanjswift.simplenote.net.HttpCallback#on404(com.bryanjswift.simplenote.net.Api.Response)
	 */
	@Override
	public void on404(final Response response) {
		super.on404(response);
		Log.d(LOGGING_TAG, "Note not found on server");
		// Note doesn't exist, create it
		final HashMap<String,String> credentials = Preferences.getLoginPreferences(context);
		final String email = credentials.get(Preferences.EMAIL);
		final String auth = credentials.get(Preferences.TOKEN);
		SimpleNoteApi.create(note, auth, email, new ServerCreateCallback(context, note));
	}
	/**
	 * @see com.bryanjswift.simplenote.net.HttpCallback#onError(com.bryanjswift.simplenote.net.Api.Response)
	 */
	@Override
	public void onError(final Response response) {
		super.onError(response);
		Log.d(LOGGING_TAG, String.format("A %d result was returned", response.status));
	}
	/**
	 * @see com.bryanjswift.simplenote.net.HttpCallback#onException(com.bryanjswift.simplenote.net.Api.Response)
	 */
	@Override
	public void onException(final String url, final String data, final Throwable t) {
		super.onException(url, data, t);
		Log.e(LOGGING_TAG, String.format("An exception was thrown for %s with %s", url, data), t);
	}
}