package com.concur.mobile.platform.ui.common.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.concur.mobile.platform.ui.common.util.Const;

/**
 * An extension of <code>ProgressBar</code> that listens for the broadcast events with actions
 * <code>Const.ACTION_NETWORK_ACTIVITY_START</code> and <code>Const.ACTION_NETWORK_ACTIVITY_STOP</code>.
 * 
 * When network activity has started and stopped, this indicator's visibility will change from <code>View.VISIBLE</code> and
 * <code>View.INVISIBLE</code>; respectfully.
 * 
 * @author AndrewK
 */
public class BroadcastReceiverProgressIndicator extends ProgressBar {

    private static final String CLS_TAG = BroadcastReceiverProgressIndicator.class.getSimpleName();

    /**
     * Contains the resource id name that should be specified for an instance of this component within a layout file.
     */
    public static final String PROGRESS_INDICATOR_ID_STRING = "progress_activity_indicator";

    /**
     * Contains the XML attribute specifying an action to start the progress indicator.
     */
    public static final String PROGRESS_BAR_START_XML_ATTRIBUTE = "start_progress_action";

    /**
     * Contains the XML attribute specifying an action to stop the progress indicator.
     */
    public static final String PROGRESS_BAR_STOP_XML_ATTRIBUTE = "stop_progress_action";

    // Contains the progress start action name.
    private String progressStartActionName = Const.ACTION_NETWORK_ACTIVITY_START;

    // Contains the progress stop action name.
    private String progressStopActionName = Const.ACTION_NETWORK_ACTIVITY_STOP;

    // Contains a reference to the broadcast receiver.
    private BroadcastReceiver receiver = new ProgressBroadcastReceiver();

    // Contains a reference to the intent filter used to register receiver.
    private static final IntentFilter filter;

    static {
        // Construct the intent filter used to register receivers.
        filter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        filter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);
    }

    /**
     * Constructs an instance of <code>BroadcastReceiverProgressIndicator</code>.
     * 
     * @param context
     *            the application context.
     */
    public BroadcastReceiverProgressIndicator(Context context) {
        super(context);
        setIndeterminate(true);
    }

    /**
     * Constructs an instance of <code>NetworkActivityIndicator</code>.
     * 
     * @param context
     *            the application context.
     * @param attrs
     *            the attribute set.
     */
    public BroadcastReceiverProgressIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIndeterminate(true);
        registerReceiver(context, attrs);
    }

    /**
     * Constructs an instance of <code>NetworkActivityIndicator</code>.
     * 
     * @param context
     *            the application context.
     * @param attrs
     *            the attribute set.
     * @param defStyle
     *            the default style.
     */
    public BroadcastReceiverProgressIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setIndeterminate(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onAttachedToWindow()
     */
    @Override
    protected void onAttachedToWindow() {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onAttachedToWindow: ");
        super.onAttachedToWindow();
        // Register receiver
        getContext().registerReceiver(receiver, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDetachedFromWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onDetachedFromWindow: ");
        super.onDetachedFromWindow();
        // Unregister the receiver.
        getContext().unregisterReceiver(receiver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onWindowVisibilityChanged(int)
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        String visibleStr = "";
        switch (visibility) {
        case View.VISIBLE:
            visibleStr = "visible";
            break;
        case View.INVISIBLE:
            visibleStr = "invisible";
            break;
        case View.GONE:
            visibleStr = "gone";
            break;
        }
        Log.d(Const.LOG_TAG, CLS_TAG + ".onWindowVisibilityChanged: visibility -> " + visibleStr);
        super.onWindowVisibilityChanged(visibility);
    }

    /**
     * Registers the broadcast receiver based on actions defined within <code>attrs</code>
     * 
     * @param context
     *            the context.
     * @param attrs
     *            the attribute set.
     */
    private void registerReceiver(Context context, AttributeSet attrs) {

        receiver = new ProgressBroadcastReceiver();
        context.registerReceiver(receiver, filter);

        // Resources resources = context.getResources();
        // int resId = resources.getIdentifier(PROGRESS_INDICATOR_ID_STRING, "id",
        // context.getPackageName());
        // if( resId != 0 ) {
        // XmlResourceParser xmlResParser = resources.getXml(R.id.progress_activity_indicator);
        // AttributeSet attributes = Xml.asAttributeSet(xmlResParser);
        // // Set up the intent filter.
        // progressStartActionName = attributes.getAttributeValue("", PROGRESS_BAR_START_XML_ATTRIBUTE);
        // if( progressStartActionName != null && progressStartActionName.length() > 0) {
        // filter = new IntentFilter(progressStartActionName);
        // } else {
        // Log.w(LOG_TAG, CLS_TAG + ".registerReceiver: no progress start action defined!");
        // }
        // progressStopActionName = attributes.getAttributeValue("", PROGRESS_BAR_STOP_XML_ATTRIBUTE);
        // if( progressStopActionName != null && progressStopActionName.length() > 0) {
        // if( filter == null ) {
        // filter = new IntentFilter(progressStopActionName);
        // } else {
        // filter.addAction(progressStopActionName);
        // }
        // } else {
        // Log.w(LOG_TAG, CLS_TAG + ".registerReceiver: no progress stop action defined!");
        // }
        // if( filter != null ) {
        // // Register the receiver.
        // receiver = new ProgressBroadcastReceiver();
        // getContext().registerReceiver(receiver, filter);
        // } else {
        // Log.w(LOG_TAG, CLS_TAG + ".registerReceiver: no broadcast receiver set up!");
        // }
        // } else {
        // Log.w(LOG_TAG, CLS_TAG + ".registerReceiver: can't find " + CLS_TAG + " by id '" +
        // PROGRESS_INDICATOR_ID_STRING + "'.");
        // }

    }

    // /**
    // * Unregisters the broadcast receiver.
    // */
    // private void unregisterReceiver() {
    // if( receiver != null ) {
    // getContext().unregisterReceiver(receiver);
    // }
    // }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling network activity start/stop broadcast messages.
     * 
     * @author AndrewK
     */
    class ProgressBroadcastReceiver extends BroadcastReceiver {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            if (progressStartActionName != null && intent.getAction().equalsIgnoreCase(progressStartActionName)) {
                setVisibility(View.VISIBLE);
            } else if (progressStopActionName != null && intent.getAction().equalsIgnoreCase(progressStopActionName)) {
                setVisibility(View.INVISIBLE);
            }

        }

    }
}
