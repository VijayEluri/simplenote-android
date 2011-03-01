package com.bryanjswift.swiftnote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.net.Uri;

/**
 * Hold constants for the SwiftNote application
 */
public class Constants {
    /** Logging tag prefix */
    public static final String TAG = "SwiftNote:";
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
    // Activity result for saving new note
    public static final int RESULT_NEW = -479212390;
    // Notifications
    public static final String NOTIFICATION_TYPE = "__NOTIFICATION_TYPE__";
    public static final int NOTIFICATION_CREDENTIALS = 98074121;
    public static final int NOTIFICATION_SYNCING = 137988;
    // Broadcast Strings
    public static final String BROADCAST_REFRESH_NOTES = Constants.class.getPackage().getName() + ".REFRESH_NOTES";
    public static final String BROADCAST_SYNC_NOTES = Constants.class.getPackage().getName() + ".SYNC_NOTES";
    // API Base URL
    public static final String BASE_URL = "https://simple-note.appspot.com";
    public static final String CREATE_ACCOUNT_URL = BASE_URL + "/createaccount.html";
    public static final String API_BASE_URL   = BASE_URL + "/api";
    public static final String API_LOGIN_URL  = API_BASE_URL + "/login";                // POST
    public static final String API_REGISTER_URL = BASE_URL + "/create";                 // POST
    public static final String API_NOTES_URL  = API_BASE_URL + "/index";                // GET
    public static final String API_NOTE_URL   = API_BASE_URL + "/note";                 // GET
    public static final String API_UPDATE_URL = API_BASE_URL + "/note";                 // POST
    public static final String API_DELETE_URL = API_BASE_URL + "/delete";               // GET
    public static final String API_SEARCH_URL = API_BASE_URL + "/search";               // GET
    // Create Account URL
    public static final Uri URL_CREATE_ACCOUNT = (new Uri.Builder())
            .scheme("http").authority("simple-note.appspot.com").path("/createaccount.html").build();
    public static final int NO_CONNECTION = 499;
    // Licensing
    public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApWp9FPULIkog/BMygZkogn1OKmmake7SlgdyLwZIql63n/yFAsYpo/11dWQSG5KTCVDFJm0ElpBIJbYuv133ghrNOU3dQzwA6Y/lVyfxXRAPoobfbF8tcUeLAYim2W9GtiqHqLXIVMYJF/55Atxf9sCUyyIgZWD3xuqdcUYchJ3nToQV6QazWJt8S+CnEMxlbkwqSd4gjTy+W1KPsWEXc0ZYJbe2icABFqWNkwifCKxfSIXwAHZYGAFzKM1+Zvl4qZYxx3SVINHp+gy7TtHJatBvqsdTL2uTCIYHjVH1yZX/0rY1/Tj/Fi7AP0yiCqzUY5PtfQcX23sWBCjfBuI2QQIDAQAB";
    public static final byte[] SALT = new byte[] {
        -21, 42, 98, -74, -98, -43, 53, -123, 99, 84,
        -47, -87, 16, -80, -90, -53, -102, 40, -55, 95
    };
}
