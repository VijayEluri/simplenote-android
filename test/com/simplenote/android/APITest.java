package com.simplenote.android;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simplenote.android.NotesDbAdapter;
import com.simplenote.android.APIBase.Response;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import android.util.Log;

public class APITest extends AndroidTestCase {
	String logInToken;
	private NotesDbAdapter mDbHelper;
	public static final String emailAddress = "simplenote@solidstategroup.com";
	public static final String password = "simplenote1234";

	public void testLogin() throws Throwable {
		String authBody = APIBase.encode( "email=" + emailAddress + "&password=wegfewrg", true );
		Response authResponse = APIBase.HTTPPost( Constants.API_LOGIN_URL, authBody );
		Assert.assertTrue(authResponse.statusCode == 401);
		
		authBody = APIBase.encode( "email=" + emailAddress + "&password=" + password, true );
		authResponse = APIBase.HTTPPost( Constants.API_LOGIN_URL, authBody );
		Assert.assertTrue(authResponse.statusCode == 200);
		logInToken = authResponse.resp.replaceAll("(\\r|\\n)", "");
		
		authResponse = APIBase.HTTPGet(Constants.API_NOTES_URL + "?auth=" + logInToken + "&email=" + emailAddress);
		Log.i(Constants.TAG, "Log In Token: " + logInToken);
		Log.i(Constants.TAG, "Index response: " + authResponse.resp);
		
		JSONArray jsonNotes = new JSONArray(authResponse.resp);
		
		for (int i = 0; i < jsonNotes.length(); ++i) {
		    JSONObject jsonNote = jsonNotes.getJSONObject(i);
		    String key = jsonNote.getString("key");
		    authResponse = APIBase.HTTPGet(Constants.API_NOTE_URL + "?key=" + key + "&auth=" + logInToken + "&email=" + emailAddress);
			
			NotesDbAdapter mDbHelper = new NotesDbAdapter(getContext());
	        mDbHelper.open();
	        mDbHelper.deleteAllNotes();
	        mDbHelper.createNote(authResponse.resp, authResponse.resp, jsonNote.getString("modify"));
		}	
    }
}