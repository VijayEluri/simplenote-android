package com.simplenote.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Hold constants for the SimpleNote application
 */
public class Constants {
	/** Name of stored preferences */
	public static final String PREFS_NAME = "SimpleNotePrefs";
	/** Logging tag prefix */
	public static final String TAG = "SimpleNote:";
	// Note Default Values
	public static final long DEFAULT_ID = -1L;
	// Dates
	public static final DateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ssssss");
	public static final DateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy");
	// Message Codes
	public static final int MESSAGE_UPDATE_NOTE = 12398;
	public static final int MESSAGE_UPDATE_FINISHED = 9732145;
	// Activity for result request Codes
	public static final String REQUEST_KEY = "ActivityRequest";
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
