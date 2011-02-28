package com.bryanjswift.swiftnote.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author bryanjswift
 */
public class Connectivity {
    /**
     * Whether or not background data is on
     * @param ctx to retrieve application context
     * @return true if background data is permitted
     */
    public static boolean isBackgroundDataEnabled(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getBackgroundDataSetting();
    }

    /**
     * Check for network availability
     * @param ctx to retrieve application context
     * @return true if a data network is available and connected
     */
    public static boolean hasInternet(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = cm.getActiveNetworkInfo();
        return active != null && active.isConnected() && !active.isRoaming();
    }
}
