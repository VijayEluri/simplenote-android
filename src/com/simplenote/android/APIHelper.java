package com.simplenote.android;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.simplenote.android.APIBase.Response;

public class APIHelper {
    private NotesDbAdapter mDbHelper;
   
	public void clearAndRefreshNotes(Context context, String token, String email) {
        mDbHelper = new NotesDbAdapter(context);
        mDbHelper.open();
	    mDbHelper.deleteAllNotes();
        mDbHelper.close();
        
        refreshNotes(context, token, email);
	}
	
	public void refreshNotes(Context context, String token, String email) {
        mDbHelper = new NotesDbAdapter(context);

		Response authResponse = APIBase.HTTPGet(Constants.API_NOTES_URL + "?auth=" + token + "&email=" + email);
		
		JSONArray jsonNotes;
		try {
			jsonNotes = new JSONArray(authResponse.resp);
	        mDbHelper.open();

			for (int i = 0; i < jsonNotes.length(); ++i) {
			    JSONObject jsonNote = jsonNotes.getJSONObject(i);
			    String key = jsonNote.getString("key");
			    Date modify = parseDate(jsonNote.getString("modify"));
			    
			    if (! (checkNoteExists(key) && ! mDbHelper.checkNewerNote(key, modify))) {		
			    	if (mDbHelper.checkNewerNote(key, modify)) {
						if ( Constants.LOGGING ) { Log.i(Constants.TAG, "Note " + key + " is newer on server - retrieving"); }
			    		mDbHelper.deleteNote(key);
			    	} else {
			    		if ( Constants.LOGGING ) { Log.i(Constants.TAG, "Note " + key + " is missing from device - retrieving"); }
			    	}
				    
				    authResponse = APIBase.HTTPGet(Constants.API_NOTE_URL + "?key=" + key + "&auth=" + token + "&email=" + email);
				    String title = authResponse.resp;
				    if (title.indexOf('\n') > -1) {
				    	title = title.substring(0, title.indexOf('\n'));
				    }
					
			        mDbHelper.createNote(key, title, authResponse.resp, jsonNote.getString("modify"));
			    }
			}	
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
	        mDbHelper.close();
		}
	}

	private boolean checkNoteExists(String key) {
		return (mDbHelper.fetchNote(key).getCount() > 0);
	}
	
	public boolean storeNote(Context context, long rowId, String key, String title, String body, String dateModified) {
		// Get a new token
		SharedPreferences mPrefs = context.getSharedPreferences(Constants.PREFS_NAME, 0);
		mPrefs.getString("token", null);

		String authBody = APIBase.encode( "email=" + mPrefs.getString("email", "")
				+ "&password=" + mPrefs.getString("password", ""), true );
		Response authResponse = APIBase.HTTPPost( Constants.API_LOGIN_URL, authBody );
		
		if (authResponse.statusCode == 200) { // successful auth login
			if ( Constants.LOGGING ) { Log.i(Constants.TAG, "Login auth success with API server."); }
			String token = authResponse.resp;
			authBody = APIBase.encode(body , true);
			authResponse = APIBase.HTTPPost( Constants.API_UPDATE_URL + "?email=" + mPrefs.getString("email", "") 
					+ "&auth=" + token, authBody);
			
			// Update the note key
	        mDbHelper = new NotesDbAdapter(context);
	        mDbHelper.open();
	        mDbHelper.addKeyToNote(rowId, authResponse.resp.replaceAll("(\\r|\\n)", ""));
	        mDbHelper.close();
		}
		
		return false;
	}
	
	public static Date parseDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return new Date();
	}
}
