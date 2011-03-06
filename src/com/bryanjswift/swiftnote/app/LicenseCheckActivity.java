package com.bryanjswift.swiftnote.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.bryanjswift.swiftnote.Constants;

/** @author bryanjswift */
public abstract class LicenseCheckActivity extends Activity {
    private LicenseChecker checker;
    private Handler licenseHandler = new SwiftNoteLicenseHandler(this);

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checker = checkLicense();
    }

    /**
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        checker.onDestroy();
    }

    /**
     * Method to instantiate and call out to the LicenseChecker interface using the policy
     * specified.
     */
    private LicenseChecker checkLicense() {
        final String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        final LicenseChecker checker = new LicenseChecker(this,
                new ServerManagedPolicy(this, new AESObfuscator(
                        Constants.SALT, getPackageName(), deviceId)
                ), Constants.BASE64_PUBLIC_KEY
        );
        final LicenseCheckerCallback callback = new SwiftNoteLicenseCallback(licenseHandler);
        checker.checkAccess(callback);
        return checker;
    }
}
