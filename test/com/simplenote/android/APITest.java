package com.simplenote.android;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.util.Log;

import com.simplenote.android.APIBase.Response;
import com.simplenote.android.model.Note;

public class APITest extends AndroidTestCase {
	public static final String emailAddress = "simplenote@solidstategroup.com";
	public static final String password = "simplenote1234";
	public static final String LOGGING_TAG = Constants.TAG + "APITest";

	private String logInToken;

	public void testLogin() throws Throwable {
		Response authResponse = APIHelper.getLoginResponse(emailAddress, "wegfewrg");
		Assert.assertTrue(authResponse.statusCode == 401);

		authResponse = APIHelper.getLoginResponse(emailAddress, password);
		Assert.assertTrue(authResponse.statusCode == 200);
		logInToken = authResponse.resp.replaceAll("(\\r|\\n)", "");

		authResponse = APIBase.HTTPGet(Constants.API_NOTES_URL + "?auth=" + logInToken + "&email=" + emailAddress);
		Log.i(LOGGING_TAG, "Log In Token: " + logInToken);
		Log.i(LOGGING_TAG, "Index response: " + authResponse.resp);
		
		JSONArray jsonNotes = new JSONArray(authResponse.resp);
		
		NotesDbAdapter mDbHelper = new NotesDbAdapter(getContext());
		mDbHelper.open();
		for (int i = 0; i < jsonNotes.length(); ++i) {
			JSONObject jsonNote = jsonNotes.getJSONObject(i);
			String key = jsonNote.getString("key");
			authResponse = APIBase.HTTPGet(Constants.API_NOTE_URL + "?key=" + key + "&auth=" + logInToken + "&email=" + emailAddress);

			;
			mDbHelper.createNote(new Note(authResponse.resp, jsonNote.getString("key"), jsonNote.getString("modify")));
		}
		mDbHelper.close();
	}
}