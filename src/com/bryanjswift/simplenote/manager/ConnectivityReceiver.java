package com.bryanjswift.simplenote.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Handles the internals of determining if the CONNECTIVITY_ACTION was a connection or
 * disconnection and calls handlers as appropriate
 * @author bryanjswift
 */
public abstract class ConnectivityReceiver extends BroadcastReceiver {
    /** Whether or not there was a connection before this broadcast was received */
    private boolean wasConnected;
    public ConnectivityReceiver(final Context context) {
        this.wasConnected = Connectivity.hasInternet(context);
    }
    @Override
    public final void onReceive(Context context, Intent intent) {
        boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        if (info == null) {
            info = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
        }
        if (isConnected && !wasConnected) {
            handleConnected(info);
        } else if (!isConnected && wasConnected) {
            handleDisconnected(info);
        }
        wasConnected = isConnected;
    }
    /**
     * What to do when the network is connected, this must be implemented
     * @param info on the network that is connected
     */
    public abstract void handleConnected(NetworkInfo info);

    /**
     * What to do when the network is disconnected, may not be implemented
     * @param info on the network attached to the broadcast intent
     */
    public void handleDisconnected(NetworkInfo info) { }
}
