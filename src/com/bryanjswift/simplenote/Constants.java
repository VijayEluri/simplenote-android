package com.bryanjswift.simplenote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Hold constants for the SimpleNote application
 */
public class Constants {
	/** Logging tag prefix */
	public static final String TAG = "SimpleNote:";
	// Note Default Values
	public static final long DEFAULT_ID = -1L;
	public static final String DEFAULT_KEY = "__SN__DEFAULT__KEY__";
	// Dates
	public static final DateFormat serverDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// Message Codes
	public static final int MESSAGE_UPDATE_NOTE = 12398;
	public static final int MESSAGE_UPDATE_STARTED = 8621034;
	public static final int MESSAGE_UPDATE_FINISHED = 9732145;
	// Activity for result request Codes
	public static final String REQUEST_KEY = "ActivityRequest";
	public static final int REQUEST_LOGIN = 32568;
	public static final int REQUEST_EDIT = 9138171;
	public static final int REQUEST_SYNCING = 6387981;
	// Notifications
	public static final int NOTIFICATION_CREDENTIALS = 98074121;
	public static final int NOTIFICATION_SYNCING = 137988;
	// Broadcast Strings
	public static final String BROADCAST_REFRESH_NOTES = Constants.class.getPackage().getName() + ".REFRESH_NOTES";
    public static final String BROADCAST_SYNC_NOTES = Constants.class.getPackage().getName() + ".SYNC_NOTES";
	// API Base URL
	public static final String API_BASE_URL   = "https://simple-note.appspot.com/api";
	public static final String API_LOGIN_URL  = API_BASE_URL + "/login";					// POST
	public static final String API_NOTES_URL  = API_BASE_URL + "/index";					// GET
	public static final String API_NOTE_URL   = API_BASE_URL + "/note";						// GET
	public static final String API_UPDATE_URL = API_BASE_URL + "/note";						// POST
	public static final String API_DELETE_URL = API_BASE_URL + "/delete";					// GET
	public static final String API_SEARCH_URL = API_BASE_URL + "/search";					// GET
}
