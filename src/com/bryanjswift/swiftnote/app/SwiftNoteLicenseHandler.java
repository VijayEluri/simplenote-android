package com.bryanjswift.swiftnote.app;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.bryanjswift.swiftnote.Constants;
import com.bryanjswift.swiftnote.R;

/** @author bryanjswift */
public class SwiftNoteLicenseHandler extends Handler {
    private static final String LOGGING_TAG = Constants.TAG + "SwiftNoteLicenseHandler";
    private final Activity activity;

    /**
     * Create a SwiftNoteLicenseHandler with the specified activity
     * @param activity to use when performing actions on the UI thread
     */
    public SwiftNoteLicenseHandler(final Activity activity) {
        this.activity = activity;
    }

    /**
     * Messages are received from SwiftNoteLicenseCallback when a License response is received
     * @see android.os.Handler#handleMessage(android.os.Message)
     */
    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case Constants.MESSAGE_LICENSE_INVALID: handleInvalid(); break;
            case Constants.MESSAGE_LICENSE_VALID: handleValid(); break;
        }
    }

    /**
     * When the License is found to be valid perform these actions
     */
    private void handleValid() {
        // Don't do anything license is valid
        Log.d(LOGGING_TAG, "Valid license found");
    }

    /**
     * When the License is found to be invalid perform these actions
     */
    private void handleInvalid() {
        Log.d(LOGGING_TAG, "Invalid license found");
        Toast.makeText(activity, R.string.license_invalid, Toast.LENGTH_LONG).show();
        activity.finish();
    }
}
