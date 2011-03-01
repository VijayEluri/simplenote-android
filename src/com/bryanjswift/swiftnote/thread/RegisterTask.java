package com.bryanjswift.swiftnote.thread;

import android.content.Context;
import android.os.AsyncTask;

import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.manager.Connectivity;
import com.bryanjswift.swiftnote.net.Api;
import com.bryanjswift.swiftnote.net.HttpCallback;
import com.bryanjswift.swiftnote.net.SwiftNoteApi;
import com.bryanjswift.swiftnote.ui.FireIntent;

/**
 * Execute a call to the register API in the background
 * @author bryanjswift
 */
public class RegisterTask extends AsyncTask<Api.Credentials, Void, Api.Response> {
    private final Context context;
    private final HttpCallback callback;
    private final HttpCallback defaultCallback = new HttpCallback() {
        /**
         * @see com.bryanjswift.swiftnote.net.HttpCallback#on200(com.bryanjswift.swiftnote.net.Api.Response)
         */
        @Override
        public void on200(Api.Response response) {
            super.on200(response);
            FireIntent.SimpleNoteList(context);
        }
        /**
         * @see com.bryanjswift.swiftnote.net.HttpCallback#onError(com.bryanjswift.swiftnote.net.Api.Response)
         */
        @Override
        public void onError(Api.Response response) {
            super.onError(response);
        }
    };
    public RegisterTask(final Context context, final HttpCallback callback) {
        this.context = context;
        this.callback = callback;
    }
    /**
     *
     * @param credentialses
     * @return
     */
    @Override
    protected Api.Response doInBackground(Api.Credentials... credentialses) {
        final Api.Response response;
        if (Connectivity.hasInternet(context)) {
            Api.Credentials credentials = credentialses[0];
            response = SwiftNoteApi.register(credentials, HttpCallback.EMPTY);
        } else {
            response = new Api.Response();
            response.status = Constants.NO_CONNECTION;
        }
        return response;
    }
    /**
     *
     * @param response
     */
    @Override
    protected void onPostExecute(Api.Response response) {
        if (callback == null) {
            SwiftNoteApi.handleResponse(defaultCallback, response);
        } else {
            SwiftNoteApi.handleResponse(callback, response);
        }
    }
}
