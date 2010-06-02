package com.simplenote.android;


public class Constants {
	static final String PREFS_NAME = "SimpleNotePrefs";
	static final String TAG = "SimpleNote";
	static final boolean LOGGING = true;
	
	static final boolean USE_CACHE = true;
	
    // User-defined result codes
    
    // Menu and dialog actions
//    static final int DIALOG_LOGIN = 2;

    // progress dialogs
//    static final int DIALOG_LOGGING_IN = 1000;
    
    
    // JSON values
//    static final String JSON_AFTER = "after";
    
    
    // Preference keys and values

	// API Base URL
	static final String API_BASE_URL   = "https://simple-note.appspot.com/api";
	static final String API_LOGIN_URL  = API_BASE_URL + "/login";					// POST
	static final String API_NOTES_URL  = API_BASE_URL + "/index";					// GET
	static final String API_NOTE_URL   = API_BASE_URL + "/note";					// GET
	static final String API_UPDATE_URL = API_BASE_URL + "/note"; 					// POST
	static final String API_DELETE_URL = API_BASE_URL + "/delete";					// GET
	static final String API_SEARCH_URL = API_BASE_URL + "/search";					// GET
}

