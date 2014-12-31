package com.concur.mobile.base.util;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A general purpose class for monitoring connectivity state and notifying listeners when the state changes.
 */
public class DataConnectivityManager extends BroadcastReceiver {

    /**
     * The listener interface used by {@link DataConnectivityManager} to publish connectivity changes.
     * 
     */
    public interface ConnectivityListener {

        /**
         * This method will be invoked on a listener when a data connection is established.
         */
        void connectionEstablished(int connectionType);

        /**
         * This method will be invoked on a listener when a data connection is lost.
         */
        void connectionLost(int connectionType);
    }

    // /////////////////////////////////////
    // Static bits

    private static DataConnectivityManager instance;

    protected static final IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    /**
     * Retrieve the single instance of the connectivity manager for the application.
     * 
     * @param app
     *            The {@link Application} reference for the calling context
     * 
     * @param listener
     *            A {@link ConnectivityListener} to register for connection change notifications. If this listener is already
     *            registered it will not be registered again.
     * 
     * @return The connectivity manager for the application. A new instance will be created if one does not exist.
     */
    public static DataConnectivityManager getInstance(Context context, ConnectivityListener listener) {
        if (instance == null) {
            instance = new DataConnectivityManager();
            ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conn.getActiveNetworkInfo();
            instance.updateConnected(networkInfo);
        }

        if (listener != null && !instance.listeners.contains(listener)) {
            instance.listeners.add(listener);
        }

        if (instance.listeners.size() > 0) {
            context.registerReceiver(instance, connectivityFilter);
        }

        return instance;
    }

    /**
     * Tell the connectivity manager that a listener is no longer listening for connection status changes. This will typically be
     * called from {@link Activity#onPause()}
     * 
     * @param app
     *            The {@link Application} reference for the calling context.
     * 
     * @param listener
     *            The {@link ConnectivityListener} that is no longer listening for changes.
     */
    public static void removeListener(Context context, ConnectivityListener listener) {
        if (instance != null) {
            instance.listeners.remove(listener);
            if (instance.listeners.size() == 0) {
                context.unregisterReceiver(instance);
            }
        }
    }

    // /////////////////////////////////////

    protected ArrayList<ConnectivityListener> listeners;

    private boolean isConnected;
    private int connectionType;

    // /////////////////////////////////////

    // Disallow instantiation
    private DataConnectivityManager() {
        // Anticipate very few simultaneous listeners
        listeners = new ArrayList<ConnectivityListener>(3);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        updateConnectedAndNotify(networkInfo);
    }

    synchronized protected void updateConnected(NetworkInfo networkInfo) {
        if (networkInfo == null) {
            isConnected = false;
            connectionType = -1;
        } else {
            isConnected = networkInfo.isConnected();
            connectionType = networkInfo.getType();
        }
    }

    synchronized protected void updateConnectedAndNotify(NetworkInfo networkInfo) {
        boolean oldConnected = isConnected;
        updateConnected(networkInfo);

        if (oldConnected != isConnected) {
            if (isConnected) {
                for (ConnectivityListener listener : listeners) {
                    listener.connectionEstablished(connectionType);
                }
            } else {
                for (ConnectivityListener listener : listeners) {
                    listener.connectionLost(connectionType);
                }
            }
        }
    }

    /**
     * Return the latest known connection state for the active network.
     * 
     * @return true if a data connection is present, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Return the latest known connection type for the active network.
     * 
     * @return An int indicating the connection type as described in {@link ConnectivityManager}
     */
    public int getConnectionType() {
        return connectionType;
    }

    /**
     * Return whether there is an active connection of type {@link ConnectivityManager.TYPE_WIFI}
     * 
     * @return true if a WIFI connection is established, false otherwise
     */
    public boolean isWifi() {
        return isConnected && (connectionType == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Return whether there is an active connection of type {@link ConnectivityManager.TYPE_WIFI},
     * {@link ConnectivityManager.TYPE_WIFI_DUN}, {@link ConnectivityManager.TYPE_WIFI_HIPRI},
     * {@link ConnectivityManager.TYPE_WIFI_MMS}, or {@link ConnectivityManager.TYPE_WIFI_SUPL}
     * 
     * @return true if a mobile connection is established, false otherwise
     */
    public boolean isMobile() {
        return isConnected
                && (connectionType == ConnectivityManager.TYPE_MOBILE
                        || connectionType == ConnectivityManager.TYPE_MOBILE_DUN
                        || connectionType == ConnectivityManager.TYPE_MOBILE_HIPRI
                        || connectionType == ConnectivityManager.TYPE_MOBILE_MMS || connectionType == ConnectivityManager.TYPE_MOBILE_SUPL);
    }
}
