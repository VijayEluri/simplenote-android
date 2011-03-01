package com.bryanjswift.swiftnote.net;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.model.Note;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * A sensible interface to the SimpleNote server API
 * @author bryanjswift
 */
public class SwiftNoteApi extends Api {
    public static final AtomicInteger count = new AtomicInteger(0);
    /** Prefix for logging statements from the SwiftNoteApi object */
    private static final String LOGGING_TAG = Constants.TAG + "SwiftNoteApi";
    /**
     * Method to invoke the SimpleNote login API
     * @param credentials data used to access swiftnote servers
     * @param callback method collection that handles the response
     * @return the response from the API call
     */
    public static Response login(final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, "Attempting login authentication with API server.");
        String data = encode("email=" + credentials.email + "&password=" + credentials.password, true, true);
        Response response = null;
        try {
            response = handleResponse(callback, Post(Constants.API_LOGIN_URL, data));
        } catch (IOException ioe) {
            callback.onException(Constants.API_LOGIN_URL, data, ioe);
        } finally {
            increment();
        }
        return response;
    }

    /**
     * Method to invoke the SimpleNOte create API
     * @param credentials data used to create account
     * @param callback method collection that handles the response
     * @return the response from the API call
     */
    public static Response register(final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, "Attempting to create new user");
        String data = encode("api=1&email=" + credentials.email + "&password=" + credentials.password, true, false);
        Response response = null;
        try {
            response = handleResponse(callback, Post(Constants.API_REGISTER_URL, data));
        } catch (IOException ioe) {
            callback.onException(Constants.API_REGISTER_URL, data, ioe);
        } finally {
            increment();
        }
        return response;
    }
    /**
     * Method to invoke the SimpleNote index API
     * @param credentials data used to access swiftnote servers
     * @param callback method collection that handles the response
     * @return a Note[] with the bare bones of each note on the server
     */
    public static Note[] index(final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, String.format("Retrieving note index from swiftnote server for %s", credentials.email));
        final String data = String.format("?auth=%s&email=%s", credentials.auth, credentials.email);
        Note[] notes = new Note[0];
        try {
            Response response = handleResponse(callback, Get(Constants.API_NOTES_URL + data));
            if (response.status == HttpStatus.SC_OK) {
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
     * @param credentials data used to access swiftnote servers
     * @param callback method collection that handles the response
     * @return a Note with all data available on the swiftnote servers
     */
    public static Note retrieve(final Note n, final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, String.format("Retrieving note with key %s from swiftnote server", n.getKey()));
        final String data = String.format("?key=%s&auth=%s&email=%s", n.getKey(), credentials.auth, credentials.email);
        Note note = n;
        try {
            Response response = handleResponse(callback, Get(Constants.API_NOTE_URL + data));
            if (response.status == HttpStatus.SC_OK) {
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
     * @param credentials data used to access swiftnote servers
     * @param callback method collection that handles the response
     * @return whether or not the note was successfully updated
     */
    public static boolean update(final Note n, final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, String.format("Updating note with key '%s' on swiftnote server", n.getKey()));
        final String modifiedDate = encode(n.getDateModified(), false, true);
        final String urlData = String.format("?key=%s&auth=%s&email=%s&modify=%s", n.getKey(), credentials.auth, credentials.email, modifiedDate);
        final String data = encode(n.getTitleAndBody(), true, false);
        boolean success = false;
        try {
            Response response = handleResponse(callback, Post(Constants.API_NOTE_URL + urlData, data));
            success = response.status == HttpStatus.SC_OK && response.body.equals(n.getKey());
        } catch (IOException ioe) {
            callback.onException(Constants.API_UPDATE_URL, data, ioe);
        } finally {
            increment();
        }
        return success;
    }
    /**
     * Create a note on the server
     * @param n - note information to add on the server
     * @param credentials data used to access swiftnote servers
     * @param callback method collection that handles the response
     * @return whether or not the note was successfully updated
     */
    public static boolean create(final Note n, final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, String.format("Creating note with id %d on swiftnote server", n.getId()));
        final String modifiedDate = encode(n.getDateModified(), false, true);
        final String urlData = String.format("?auth=%s&email=%s&modify=%s", credentials.auth, credentials.email, modifiedDate);
        final String data = encode(n.getTitleAndBody(), true, false);
        boolean success = false;
        try {
            Response response = handleResponse(callback, Post(Constants.API_NOTE_URL + urlData, data));
            success = response.status == HttpStatus.SC_OK && response.body.length() > 0;
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
     * @param credentials data used to access swiftnote servers
     * @param callback method collection that handles the response
     * @return whether or not the note was successfully removed
     */
    public static boolean delete(final Note n, final Credentials credentials, final HttpCallback callback) {
        Log.d(LOGGING_TAG, String.format("Deleting not with key %s on swiftnote server", n.getKey()));
        final String data = String.format("?key=%s&auth=%s&email=%s", n.getKey(), credentials.auth, credentials.email);
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
            case HttpStatus.SC_OK: callback.on200(response); break;
            case HttpStatus.SC_BAD_REQUEST: callback.on400(response); break;
            case HttpStatus.SC_UNAUTHORIZED: callback.on401(response); break;
            case HttpStatus.SC_FORBIDDEN: callback.on403(response); break;
            case HttpStatus.SC_NOT_FOUND: callback.on404(response); break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR: callback.on500(response); break;
        }
        if (response.status != HttpStatus.SC_OK) {
            callback.onError(response);
        }
        callback.onComplete(response);
        return response;
    }
    /**
     * Increments and logs the API count
     * @return count after incrementing
     */
    private static int increment() {
        int count = SwiftNoteApi.count.incrementAndGet();
        Log.d(LOGGING_TAG, String.format("That makes %d API calls since reset", count));
        return count;
    }
}
