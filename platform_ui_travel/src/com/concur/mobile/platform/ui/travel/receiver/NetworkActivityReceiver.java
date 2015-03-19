package com.concur.mobile.platform.ui.travel.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * An extension of <code>BroadcastReceiver</code> for hiding/showing the network activity progress indicator. This class is a copy
 * form core NetworkActivityReceiver
 * 
 * @see com.concur.mobile.core.receiver.NetworkActivityReceiver
 * 
 * @author RatanK
 */
public class NetworkActivityReceiver extends BroadcastReceiver {

    /**
     * An interface for providing feedback to an instance of <code>NetworkActivityReceiver</code> as to whether particular network
     * activity is of interest to an activity.
     * 
     * @author AndrewK
     */
    public static interface INetworkActivityListener {

        /**
         * Whether or not network activity of a certain type should cause any progress indicator contained within the associated
         * activity to become visibile.
         * 
         * @param networkMsgType
         *            the network activity type, i.e., <code>Const.MSG_*</code>.
         * @return whether a progress indicator contained within the associated activity should be displayed.
         */
        boolean isNetworkRequestInteresting(int networkMsgType);

        /**
         * Notifies the listener that network activity of a certain type has started.
         * 
         * @param networkMsgType
         *            the network activity type, i.e., <code>Const.MSG_*</code>.
         */
        void networkActivityStarted(int networkMsgType);

        /**
         * Notifies the listener that network activity of a certain type has stopped.
         * 
         * @param networkMsgType
         *            the network activity type, i.e., <code>Const.MSG_*</code>.
         */
        void networkActivityStopped(int networkMsgType);

        /**
         * Gets the text that should be displayed under the progress bar.
         * 
         * @param networkMsgType
         *            the network msg type.
         * @param defaultText
         *            the default text that will be displayed.
         * 
         * @return the text that should be displayed under the progress bar. If <code>null</code> is returned, then no text will
         *         be displayed.
         */
        String getNetworkActivityText(int networkMsgType, String defaultText);

    }

    private final String CLS_TAG = NetworkActivityReceiver.class.getSimpleName();

    /**
     * Contains the associated activity.
     */
    private Activity activity;

    /**
     * Contains the network activity listener.
     */
    private INetworkActivityListener listener;

    public NetworkActivityReceiver(Activity activity, INetworkActivityListener listener) {
        super();
        this.activity = activity;
        this.listener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO - need to handle this code ???

        // if (intent.getAction().equalsIgnoreCase(Const.ACTION_NETWORK_ACTIVITY_START)) {
        // int networkMsgType = intent.getIntExtra(Const.ACTION_NETWORK_ACTIVITY_TYPE, -1);
        // if (networkMsgType != -1) {
        // if (listener != null && listener.isNetworkRequestInteresting(networkMsgType)) {
        // String networkActivityText = listener.getNetworkActivityText(networkMsgType,
        // intent.getStringExtra(Const.ACTION_NETWORK_ACTIVITY_TEXT));
        // ViewUtil.setNetworkActivityIndicatorVisibility(activity, View.VISIBLE, networkActivityText);
        // listener.networkActivityStarted(networkMsgType);
        // }
        // }
        // } else if (intent.getAction().equalsIgnoreCase(Const.ACTION_NETWORK_ACTIVITY_STOP)) {
        // int networkMsgType = intent.getIntExtra(Const.ACTION_NETWORK_ACTIVITY_TYPE, -1);
        // if (networkMsgType != -1) {
        // if (listener != null && listener.isNetworkRequestInteresting(networkMsgType)) {
        // ViewUtil.setNetworkActivityIndicatorVisibility(activity, View.INVISIBLE, null);
        // listener.networkActivityStopped(networkMsgType);
        // }
        // }
        // } else {
        // Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unrecognized action -- " + intent.getAction());
        // }
    }
}