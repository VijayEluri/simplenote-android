package com.bryanjswift.simplenote.net;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.bryanjswift.simplenote.Constants;
import com.bryanjswift.simplenote.model.Note;

/**
 * A sensible interface to the SimpleNote server API
 * @author bryanjswift
 */
public class SimpleNoteApi extends Api {
	public static final AtomicInteger count = new AtomicInteger(0);
	/** Prefix for logging statements from the SimpleNoteApi object */
	private static final String LOGGING_TAG = Constants.TAG + "SimpleNoteApi";
	/**
	 * Method to invoke the SimpleNote login API
	 * @param email address to login with
	 * @param password to use for logging in
	 * @param callback method collection that handles the response
	 * @return the token resulting from the API login call
	 */
	public static String login(final String email, final String password, final HttpCallback callback) {
		Log.d(LOGGING_TAG, "Attempting login authentication with API server.");
		String data = encode("email=" + email + "&password=" + password, true, true);
		String token = null;
		try {
			token = handleResponse(callback, Post(Constants.API_LOGIN_URL, data)).body;
		} catch (IOException ioe) {
			callback.onException(Constants.API_LOGIN_URL, data, ioe);
		} finally {
			increment();
		}
		return token;
	}
	/**
	 * Method to invoke the SimpleNote index API
	 * @param auth token from login call
	 * @param email identifying account to retrieve notes for
	 * @param callback method collection that handles the response
	 * @return a Note[] with the bare bones of each note on the server
	 */
	public static Note[] index(final String auth, final String email, final HttpCallback callback) {
		Log.d(LOGGING_TAG, String.format("Retrieving note index from simplenote server for %s", email));
		final String data = String.format("?auth=%s&email=%s", auth, email);
		Note[] notes = new Note[0];
		try {
			Response response = handleResponse(callback, Get(Constants.API_NOTES_URL + data));
			if (response.status == 200) {
				JSONArray jsonNotes = new JSONArray(response.body);
				int length = jsonNotes.length();
				notes = new Note[length];
				for (int i = 0; i < length; i++) {
					notes[i] = new Note(jsonNotes.getJSONObject(i));
				}
			}
		} catch (IOException ioe) {
			callback.onException(Constants.API_NOTES_URL, data, ioe);
		} catch (JSONException jsone) {
			callback.onException(Constants.API_NOTES_URL, data, jsone);
		} finally {
			increment();
		}
		Log.d(LOGGING_TAG, String.format("%d notes retrieved from server", notes.length));
		return notes;
	}
	/**
	 * Pull a full note from the server
	 * @param n - bare bones information about the note to retrieve
	 * @param auth token from login call
	 * @param email identifying account to retrieve notes for
	 * @param callback method collection that handles the response
	 * @return a Note with all data available on the simplenote servers
	 */
	public static Note retrieve(final Note n, final String auth, final String email, final HttpCallback callback) {
		Log.d(LOGGING_TAG, String.format("Retrieving note with key %s from simplenote server", n.getKey()));
		final String data = String.format("?key=%s&auth=%s&email=%s", n.getKey(), auth, email);
		Note note = n;
		try {
			Response response = handleResponse(callback, Get(Constants.API_NOTE_URL + data));
			if (response.status == 200) {
				// May need to get header information from the Response object in order to be sure modify date is in sync with server
				Map<String, List<String>> headers = response.headers;
				note = n.setTitleAndBody(response.body)
						.setDeleted(new Boolean(headers.get("note-deleted").get(0)))
						.setKey(headers.get("note-key").get(0))
						.setDateModified(headers.get("note-modifydate").get(0));
			}
		} catch (IOException ioe) {
			callback.onException(Constants.API_NOTE_URL, data, ioe);
		} finally {
			increment();
		}
		return note;
	}
	/**
	 * Update a note on the server
	 * @param n - note information to update on the server
	 * @param auth token from login call
	 * @param email identifying account to retrieve notes for
	 * @param callback method collection that handles the response
	 * @return whether or not the note was successfully updated
	 */
	public static boolean update(final Note n, final String auth, final String email, final HttpCallback callback) {
		Log.d(LOGGING_TAG, String.format("Updating note with key '%s' on simplenote server", n.getKey()));
		final String modifiedDate = encode(n.getDateModified(), false, true);
		final String urlData = String.format("?key=%s&auth=%s&email=%s&modify=%s", n.getKey(), auth, email, modifiedDate);
		final String data = encode(n.getTitleAndBody(), true, false);
		boolean success = false;
		try {
			Response response = handleResponse(callback, Post(Constants.API_NOTE_URL + urlData, data));
			success = response.status == 200 && response.body.equals(n.getKey());
		} catch (IOException ioe) {
			callback.onException(Constants.API_NOTE_URL, data, ioe);
		} finally {
			increment();
		}
		return success;
	}
	/**
	 * Create a note on the server
	 * @param n - note information to add on the server
	 * @param auth token from login call
	 * @param email identifying account to retrieve notes for
	 * @param callback method collection that handles the response
	 * @return whether or not the note was successfully updated
	 */
	public static boolean create(final Note n, final String auth, final String email, final HttpCallback callback) {
		Log.d(LOGGING_TAG, String.format("Creating note with id %d on simplenote server", n.getId()));
		final String modifiedDate = encode(n.getDateModified(), false, true);
		final String urlData = String.format("?auth=%s&email=%s&modify=%s", auth, email, modifiedDate);
		final String data = encode(n.getTitleAndBody(), true, false);
		boolean success = false;
		try {
			Response response = handleResponse(callback, Post(Constants.API_NOTE_URL + urlData, data));
			success = response.status == 200 && response.body.length() > 0;
		} catch (IOException ioe) {
			callback.onException(Constants.API_NOTE_URL, data, ioe);
		} finally {
			increment();
		}
		return success;
	}
	/**
	 * Mark a note as deleted from server
	 * @param n - note information to remove from the server
	 * @param auth token from login call
	 * @param email identifying account to retrieve notes for
	 * @param callback method collection that handles the response
	 * @return whether or not the note was successfully removed
	 */
	public static boolean delete(final Note n, final String auth, final String email, final HttpCallback callback) {
		Log.d(LOGGING_TAG, String.format("Deleting not with key %s on simplenote server", n.getKey()));
		final String data = String.format("?key=%s&auth=%s&email%s", n.getKey(), auth, email);
		boolean success = false;
		try {
			handleResponse(callback, Get(Constants.API_DELETE_URL + data));
			success = true;
		} catch (IOException ioe) {
			callback.onException(Constants.API_DELETE_URL, data, ioe);
		} finally {
			increment();
		}
		return success;
	}
	/**
	 * Calls the appropriate methods on the HttpCallback object for the given response
	 * @param callback method collection to handle response
	 * @param response object from an HTTP request
	 */
	public static Response handleResponse(final HttpCallback callback, final Response response) {
		switch (response.status) {
			case 200: callback.on200(response); break;
			case 400: callback.on400(response); break;
			case 401: callback.on401(response); break;
			case 403: callback.on403(response); break;
			case 404: callback.on404(response); break;
			case 500: callback.on500(response); break;
		}
		if (response.status != 200) {
			callback.onError(response);
		}
		callback.onComplete(response);
		return response;
	}
	/**
	 * Increments and logs the API count
	 * @return count after incrementing
	 */
	private synchronized static int increment() {
		int count = SimpleNoteApi.count.incrementAndGet();
		Log.d(LOGGING_TAG, String.format("That makes %d API calls since reset", count));
		return count;
	}
}
