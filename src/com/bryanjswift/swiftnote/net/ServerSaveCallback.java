package com.bryanjswift.swiftnote.net;

import android.content.Context;
import android.util.Log;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.Preferences;
import com.bryanjswift.swiftnote.app.Notifications;
import com.bryanjswift.swiftnote.model.Note;
import com.bryanjswift.swiftnote.net.Api.Response;
import com.bryanjswift.swiftnote.persistence.SimpleNoteDao;
import com.bryanjswift.swiftnote.thread.LoginTask;

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
     * @param context for which the callback is created
     * @param note trying to be saved to the server
     */
    public ServerSaveCallback(final Context context, final Note note) {
        super();
        this.note = note;
        this.context = context;
        this.dao = new SimpleNoteDao(context);
    }
    /**
     * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
     */
    @Override
    public void on200(final Response response) {
        super.on200(response);
        Log.d(LOGGING_TAG, String.format("Successfully saved note '%s' on server", response.body));
        // Set needs sync to false in db
        dao.markSynced(note);
    }
    /**
     * @see com.bryanjswift.swiftnote.net.HttpCallback#on401(com.bryanjswift.swiftnote.net.Api.Response)
     */
    @Override
    public void on401(final Response response) {
        super.on401(response);
        Log.d(LOGGING_TAG, "Unauthorized to save note on server");
        final Api.Credentials credentials = Preferences.getLoginPreferences(context);
        (new LoginTask(context, credentials, new HttpCallback() {
            /**
             * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
             */
            @Override
            public void on200(Response response) {
                super.on200(response);
                // ideally we could retry here...
            }
            /**
             * @see com.bryanjswift.swiftnote.net.HttpCallback#onError(com.bryanjswift.swiftnote.net.Api.Response)
             */
            @Override
            public void onError(Response response) {
                super.onError(response);
                Notifications.Credentials(context);
            }
        })).execute();
    }
    /**
     * @see com.bryanjswift.swiftnote.net.HttpCallback#on404(com.bryanjswift.swiftnote.net.Api.Response)
     */
    @Override
    public void on404(final Response response) {
        super.on404(response);
        Log.d(LOGGING_TAG, "Note not found on server");
        // Note doesn't exist, create it
        final Api.Credentials credentials = Preferences.getLoginPreferences(context);
        SwiftNoteApi.create(note, credentials, new ServerCreateCallback(context, note));
    }
    /**
     * @see com.bryanjswift.swiftnote.net.HttpCallback#onError(com.bryanjswift.swiftnote.net.Api.Response)
     */
    @Override
    public void onError(final Response response) {
        super.onError(response);
        Log.d(LOGGING_TAG, String.format("A %d result was returned", response.status));
    }
    /**
     * @see com.bryanjswift.swiftnote.net.HttpCallback#onException(String, String, Throwable)
     */
    @Override
    public void onException(final String url, final String data, final Throwable t) {
        super.onException(url, data, t);
        Log.e(LOGGING_TAG, String.format("An exception was thrown for %s with %s", url, data), t);
    }
}
