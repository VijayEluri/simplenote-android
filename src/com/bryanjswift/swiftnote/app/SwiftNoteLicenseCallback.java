package com.bryanjswift.swiftnote.app;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.bryanjswift.swiftnote.Constants;

/** @author bryanjswift */
public class SwiftNoteLicenseCallback  implements LicenseCheckerCallback {
    private static final String LOGGING_TAG = Constants.TAG + "SwiftNoteLicenseCallback";
    /** Handler to send messages to the UI thread */
    private final Handler handler;

    /**
     * Enable creation of Licensing callback that accepts a Handler for posting to the
     * UI thread
     * @param handler used to pass messages to UI thread
     */
    public SwiftNoteLicenseCallback(final Handler handler) {
        this.handler = handler;
    }

    /**
     * @see com.android.vending.licensing.LicenseCheckerCallback#allow()
     */
    public void allow() {
        // No need to do anything really
        Message.obtain(handler, Constants.MESSAGE_LICENSE_VALID).sendToTarget();
    }

    /**
     * @see com.android.vending.licensing.LicenseCheckerCallback#applicationError(com.android.vending.licensing.LicenseCheckerCallback.ApplicationErrorCode)
     */
    public void applicationError(final LicenseCheckerCallback.ApplicationErrorCode code) {
        Log.w(LOGGING_TAG, String.format("Error when checking license, %s", code.toString()));
    }

    /**
     * @see com.android.vending.licensing.LicenseCheckerCallback#dontAllow()
     */
    public void dontAllow() {
        // Kill the application and send toast about unlicensed software
        Message.obtain(handler, Constants.MESSAGE_LICENSE_INVALID).sendToTarget();
    }
}
