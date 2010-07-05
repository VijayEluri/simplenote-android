package com.simplenote.android;


public class Constants {
	public static final String PREFS_NAME = "SimpleNotePrefs";
	public static final String TAG = "SimpleNote:";
	public static final boolean USE_CACHE = true;

	// API Base URL
	static final String API_BASE_URL   = "https://simple-note.appspot.com/api";
	static final String API_LOGIN_URL  = API_BASE_URL + "/login";					// POST
	static final String API_NOTES_URL  = API_BASE_URL + "/index";					// GET
	static final String API_NOTE_URL   = API_BASE_URL + "/note";					// GET
	static final String API_UPDATE_URL = API_BASE_URL + "/note"; 					// POST
	static final String API_DELETE_URL = API_BASE_URL + "/delete";					// GET
	static final String API_SEARCH_URL = API_BASE_URL + "/search";					// GET
}

