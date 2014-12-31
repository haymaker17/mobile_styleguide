package com.concur.mobile.gov.util;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.util.Const;

/**
 * 
 * An abstract extension of <code>BroadcastReceiver</code>.
 * 
 * @param <A>
 *            an extension Type of <code>BaseActivity</code>
 * @param <S>
 *            an extension Type of <code>ServiceRequest</code>
 */
public abstract class GeneralBaseReceiver<A extends Activity, S extends ServiceRequest> extends
    BroadcastReceiver {

    private static final String CLS_TAG = GeneralBaseReceiver.class.getSimpleName();

    /**
     * Contains the activity associated with this receiver.
     */
    protected A activity;
    /**
     * Contains the intent that was passed to the receiver's 'onReceive' method.
     */
    protected Intent intent;

    /**
     * Contains a reference to an outstanding service request for which this receiver
     * is waiting on a reply.
     */
    protected S serviceRequest;

    protected String lastHttpErrorMessage, actionStatusErrorMessage;

    /**
     * Constructs an instance of <code>BaseBroadcastReceiver</code> associated with <code>activity</code>.
     * 
     * @param activity
     *            the associated activity.
     */
    protected GeneralBaseReceiver(A activity) {
        this.activity = activity;
    }

    /**
     * Sets the activity associated with this broadcast receiver.
     * 
     * @param activity
     *            the activity associated with this broadcast receiver.
     */
    public void setActivity(A activity) {
        this.activity = activity;
        if (this.activity != null) {
            setActivityServiceRequest(serviceRequest);
            if (this.intent != null) {
                // The 'onReceive' method was called prior to the 'setActivity', so process
                // the intent now.
                onReceive(activity.getApplicationContext(), intent);
            }
        }
    }

    /**
     * Gets the activity associated with this broadcast receiver.
     * 
     * @return
     *         the activity associated with this broadcast receiver.
     */
    public A getActivity() {
        return this.activity;
    }

    /**
     * Gets the service request associated with this broadcast receiver.
     * 
     * @return
     *         the servicer request associated with this broadcast receiver.
     */
    public S getServiceRequest() {
        return this.serviceRequest;
    }

    /**
     * Sets the service request associated with this broadcast receiver.
     * 
     * @param serviceRequest
     *            the service request to be associated with this broadcast receiver.
     */
    public void setServiceRequest(S serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    /**
     * Sets the service request associated with this receiver on
     * the associated activity.
     * 
     * @param request
     *            the request.
     */
    protected abstract void setActivityServiceRequest(S request);

    /**
     * Clears the request associated with an activity.
     * 
     * @param activity
     *            the activity with which to clear the request.
     */
    protected abstract void clearActivityServiceRequest(A activity);

    /**
     * Unregisters this receiver.
     */
    protected abstract void unregisterReceiver();

    /**
     * Will handle a success scenario.
     * 
     * @param context
     *            the receiver context.
     * @param intent
     *            the intent.
     */
    protected abstract void handleSuccess(Context context, Intent intent);

    /**
     * Will handle a failure scenario.
     * 
     * @param context
     *            the receiver context.
     * @param intent
     *            the intent.
     */
    protected abstract void handleFailure(Context context, Intent intent);

    /**
     * Provides a notification that the status of the request was either not found
     * in <code>intent</code> or was found and did not have a value of <code>Const.SERVICE_REQUEST_STATUS_OKAY</code>.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * Default implementation is a no-op.
     * 
     * @param context
     *            the context.
     * @param intent
     *            the intent.
     * @param requestStatus
     *            the request status within <code>intent</code>. Should be one of
     *            <code>Const.SERVICE_REQUEST_STATUS_INVALID_REQUEST, Const.SERVICE_REQUEST_STATUS_IO_ERROR</code> or
     *            <code>-1</code> if in the case of no request status found in <code>intent</code>.
     */
    protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
        // No-op.
    }

    /**
     * Provides a notification that either the http status is missing from <code>intent</code> or has a value that is not
     * equal to <code>HttpStatus.SC_OK</code>.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * Default implementation is a no-op.
     * 
     * @param context
     *            the context
     * @param intent
     *            the intent
     * @param httpStatus
     *            the http status. Will have a value of <code>-1</code> if no http status code
     *            found in <code>intent</code>.
     * @return
     *         returns whether or not the HttpError was handled.
     */
    protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
        // No-op.
        boolean retVal = false;

        return retVal;
    }

    /**
     * Will handle dismissing any dialog displayed while the receiver is waiting on a result. <br>
     * <br>
     * <b>NOTE:</b><br>
     * Default implementation is a no-op.
     * 
     * @param context
     *            the context.
     * @param intent
     *            the intent.
     */
    protected void dismissRequestDialog(Context context, Intent intent) {
        // No-op.
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Does this receiver have a current activity?
        if (activity != null) {

            // Unregister the receiver.
            unregisterReceiver();

            int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                Const.REPLY_STATUS_SUCCESS)
                                || intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                    Const.REPLY_STATUS_OK)) {
                                // Handle the success.
                                handleSuccess(context, intent);
                                try {
                                    // Dismiss the dialog.
                                    dismissRequestDialog(context, intent);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            } else {
                                // Set the error message.
                                this.actionStatusErrorMessage = intent
                                    .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                    + this.actionStatusErrorMessage);

                                handleFailure(context, intent);

                                try {
                                    // Dismiss the dialog.
                                    dismissRequestDialog(context, intent);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            // Set the error message.
                            this.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                + this.lastHttpErrorMessage);

                            if (!handleHttpError(context, intent, httpStatusCode)) {
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }

                            try {
                                // Dismiss the dialog.
                                dismissRequestDialog(context, intent);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");

                        handleHttpError(context, intent, -1);

                        try {
                            // Dismiss the dialog.
                            dismissRequestDialog(context, intent);
                        } catch (IllegalArgumentException ilaExc) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                        }
                    }
                } else {
                    if (serviceRequest != null && !serviceRequest.isCanceled()) {
                        this.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                            + this.lastHttpErrorMessage);

                        handleRequestFailure(context, intent, serviceRequestStatus);

                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                        try {
                            // Dismiss the dialog.
                            dismissRequestDialog(context, intent);
                        } catch (IllegalArgumentException ilaExc) {
                            Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                        }
                    }
                }
            } else {

                handleRequestFailure(context, intent, -1);

                try {
                    // Dismiss the dialog.
                    dismissRequestDialog(context, intent);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
            }

            // Clear out the request reference.
            clearActivityServiceRequest(activity);
        } else {
            // The new activity has not yet been set on the receiver, defer
            // the processing of this intent until then.
            this.intent = intent;
        }
    }
}