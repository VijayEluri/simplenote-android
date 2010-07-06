package com.simplenote.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.simplenote.android.APIBase.Response;
import com.simplenote.android.model.Note;

public class APIHelper {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final String LOGGING_TAG = Constants.TAG + "APIHelper";

	public static APIBase.Response getLoginResponse(String email, String password) {
		Log.d(LOGGING_TAG, "Attempting login authentication with API server.");
		String authBody = APIBase.encode("email=" + email + "&password=" + password, true, true);
		return APIBase.HTTPPost(Constants.API_LOGIN_URL, authBody);
	}

	public static void clearAndRefreshNotes(NotesDbAdapter dbHelper, String token, String email) {
		dbHelper.open();
		dbHelper.deleteAllNotes();
		
		refreshNotes(dbHelper, token, email);
	}

	public static void refreshNotes(NotesDbAdapter dbHelper, String token, String email) {
		Response authResponse = APIBase.HTTPGet(Constants.API_NOTES_URL + "?auth=" + token + "&email=" + email);

		JSONArray jsonNotes;
		try {
			jsonNotes = new JSONArray(authResponse.resp);
			dbHelper.open();

			for (int i = 0; i < jsonNotes.length(); ++i) {
				JSONObject jsonNote = jsonNotes.getJSONObject(i);
				String key = jsonNote.getString("key");
				Date modify = parseDate(jsonNote.getString("modify"));

				if (!jsonNote.getString("deleted").equals("true")) {
					if (!(checkNoteExists(dbHelper, key) && !dbHelper.checkNewerNote(key, modify))) {
						if (dbHelper.checkNewerNote(key, modify)) {
							Log.i(Constants.TAG, "Note " + key + " is newer on server - retrieving");
							dbHelper.deleteNote(key);
						} else {
							Log.i(Constants.TAG, "Note " + key + " is missing from device - retrieving");
						}
						authResponse = APIBase.HTTPGet(Constants.API_NOTE_URL + "?key=" + key + "&auth=" + token + "&email=" + email);

						dbHelper.createNote(new Note(0, authResponse.resp, key, jsonNote.getString("modify")));
					}
				}
			}	
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			dbHelper.close();
		}
	}

	private static boolean checkNoteExists(NotesDbAdapter dbHelper, String key) {
		return (dbHelper.fetchNote(key).getCount() > 0);
	}

	public static boolean storeNote(Context context, long rowId, String key, String title, String body, String dateModified) {
		// Get a new token
		SharedPreferences mPrefs = context.getSharedPreferences(Constants.PREFS_NAME, 0);
		String email = mPrefs.getString(Preferences.EMAIL, "");
		String token = mPrefs.getString(Preferences.TOKEN, null);

		String noteBody = APIBase.encode(title + "\n" + body , true, false);
		Response res = APIBase.HTTPPost(Constants.API_UPDATE_URL + "?email=" + email + "&auth=" + token, noteBody);

		// Update the note key
		NotesDbAdapter dbHelper = new NotesDbAdapter(context);
		dbHelper.open();
		dbHelper.addKeyToNote(rowId, res.resp.replaceAll("(\\r|\\n)", ""));
		dbHelper.close();

		return false;
	}

	public static Date parseDate(String date) {
		try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Date();
	}
}
