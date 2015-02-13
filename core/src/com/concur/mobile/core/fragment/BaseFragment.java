package com.concur.mobile.core.fragment;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>Fragment</code> providing some basic functionality.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.fragment.PlatformFragment} instead.
 */
@Deprecated
public abstract class BaseFragment extends Fragment {

    protected BaseActivity activity;

    /**
     * A base interface for reporting fragments events.
     */
    public interface FragmentListener {
    };

    /**
     * Contains last error message returned from a service request.
     */
    public String actionStatusErrorMessage;

    /**
     * Contains the last http error message returned from a service request.
     */
    protected String lastHttpErrorMessage;

    /**
     * Contains whether or not the building of the view is delayed until the service is available.
     */
    protected boolean buildViewDelay;

    /**
     * Return a resource ID of the title that will be used for this fragment where needed (e.g. page title bars).
     * 
     * @return An Integer holding the title resource ID or null if there should be no title
     */
    public Integer getTitleResource() {
        return null;
    }

    /**
     * Return the BaseActivity this fragment is currently associated with
     */
    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof BaseActivity)) {
            throw new RuntimeException("This fragment must be hosted in an activity that extends BaseActivity");
        }

        super.onAttach(activity);
        this.activity = (BaseActivity) activity;
    }

    protected ConcurCore getConcurCore() {
        return activity.getConcurCore();
    }

    /**
     * Handles a notification that the Concur service is available.
     */
    public void onServiceAvailable() {
    }

    /**
     * Handles a notification that the Concur service is unavailable.
     */
    public void onServiceUnavailable() {
    }

    /**
     * 
     * An abstract extension of <code>BroadcastReceiver</code>.
     * 
     * @param <A>
     *            an extension Type of <code>BaseFragment</code>
     * @param <S>
     *            an extension Type of <code>ServiceRequest</code>
     */
    protected abstract static class BaseBroadcastReceiver<A extends BaseFragment, S extends ServiceRequest> extends
            BroadcastReceiver {

        private static final String CLS_TAG = BaseBroadcastReceiver.class.getSimpleName();

        /**
         * Contains the fragment associated with this receiver.
         */
        protected A fragment;
        /**
         * Contains the intent that was passed to the receiver's 'onReceive' method.
         */
        protected Intent intent;

        /**
         * Contains a reference to an outstanding service request for which this receiver is waiting on a reply.
         */
        protected S serviceRequest;

        /**
         * Constructs an instance of <code>BaseBroadcastReceiver</code> associated with <code>fragment</code>.
         * 
         * @param fragment
         *            the associated fragment.
         */
        protected BaseBroadcastReceiver(A fragment) {
            this.fragment = fragment;
        }

        /**
         * Sets the fragment associated with this broadcast receiver.
         * 
         * @param fragment
         *            the activity associated with this broadcast receiver.
         */
        public void setFragment(A fragment) {
            this.fragment = fragment;
            if (this.fragment != null) {
                setFragmentServiceRequest(serviceRequest);
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setFragment', so process
                    // the intent now.
                    onReceive(fragment.getActivity().getApplicationContext(), intent);
                }
            }
        }

        /**
         * Gets the activity associated with this broadcast receiver.
         * 
         * @return the activity associated with this broadcast receiver.
         */
        public A getActivity() {
            return this.fragment;
        }

        /**
         * Gets the service request associated with this broadcast receiver.
         * 
         * @return the servicer request associated with this broadcast receiver.
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
         * Sets the service request associated with this receiver on the associated fragment.
         * 
         * @param request
         *            the request.
         */
        protected abstract void setFragmentServiceRequest(S request);

        /**
         * Clears the request associated with a fragment.
         * 
         * @param fragment
         *            the fragment with which to clear the request.
         */
        protected abstract void clearFragmentServiceRequest(A fragment);

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
         * Will handle dismissing any dialog displayed while the receiver is waiting on a result. <br>
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
            if (fragment != null) {

                // Unregister the receiver.
                unregisterReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (Const.REPLY_STATUS_SUCCESS.equalsIgnoreCase(intent
                                        .getStringExtra(Const.REPLY_STATUS))
                                        || Const.REPLY_STATUS_OK.equalsIgnoreCase(intent
                                                .getStringExtra(Const.REPLY_STATUS))) {
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
                                    fragment.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + fragment.actionStatusErrorMessage);

                                    handleFailure(context, intent);

                                    try {
                                        // Dismiss the dialog.
                                        dismissRequestDialog(context, intent);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else if (httpStatusCode == 403) {
                                // Temporary Fix for MOB-21386
                                try {
                                    // Dismiss the dialog.
                                    dismissRequestDialog(context, intent);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }

                                Intent intent2 = new Intent("temp.image.upload.fix");
                                LocalBroadcastManager.getInstance(ConcurCore.getContext()).sendBroadcast(intent2);
                            } else {
                                // Set the error message.
                                fragment.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + fragment.lastHttpErrorMessage);
                                fragment.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                                try {
                                    // Dismiss the dialog.
                                    dismissRequestDialog(context, intent);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                            try {
                                // Dismiss the dialog.
                                dismissRequestDialog(context, intent);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        if (serviceRequest != null && !serviceRequest.isCanceled()) {
                            fragment.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + fragment.lastHttpErrorMessage);
                            fragment.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            try {
                                // Dismiss the dialog.
                                dismissRequestDialog(context, intent);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        dismissRequestDialog(context, intent);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the request reference.
                clearFragmentServiceRequest(fragment);
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

}
