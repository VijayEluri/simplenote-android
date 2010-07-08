package com.simplenote.android;


public class Constants {
	/** Name of stored preferences */
	public static final String PREFS_NAME = "SimpleNotePrefs";
	/** Logging tag prefix */
	public static final String TAG = "SimpleNote:";
	// Message Codes
	public static final int MESSAGE_UPDATE_NOTE = 12398;
	// Activity for result request Codes
	public static final int REQUEST_LOGIN = 32568;
	public static final int REQUEST_EDIT = 9138171;
	// API Base URL
	public static final String API_BASE_URL   = "https://simple-note.appspot.com/api";
	public static final String API_LOGIN_URL  = API_BASE_URL + "/login";					// POST
	public static final String API_NOTES_URL  = API_BASE_URL + "/index";					// GET
	public static final String API_NOTE_URL   = API_BASE_URL + "/note";						// GET
	public static final String API_UPDATE_URL = API_BASE_URL + "/note";						// POST
	public static final String API_DELETE_URL = API_BASE_URL + "/delete";					// GET
	public static final String API_SEARCH_URL = API_BASE_URL + "/search";					// GET
}
