package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.concur.mobile.core.expense.travelallowance.service.IRequestListener;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;

/**
 * Handles service requests of an {@link Activity}. This {@link Fragment} is parametrized via the
 * arguments bundle given to an object of this class. The following arguments are evaluated:
 *
 * {@link ServiceRequestListenerFragment#BUNDLE_ID_REQUEST_SUCCESS_MSG},
 * {@link ServiceRequestListenerFragment#BUNDLE_ID_REQUEST_FAILED_MSG}
 *
 * When receiving a request response, this object notifies the attached {@link Activity} using
 * callback interface {@link IFragmentCallback}, which should be implemented by the attached
 * {@link Activity}. Thereby the fragment messages associated with the corresponding argument key
 * is used for notification.
 *
 * Created by D049515 on 09.09.2015.
 */
public class ServiceRequestListenerFragment extends Fragment implements IRequestListener {
    public static final String CLASS_TAG = ServiceRequestListenerFragment.class.getSimpleName();

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}.
     * This value denotes the fragment message sent to callback object (the {@link Activity} attached)
     * when request was successful.
     */
    public static final String BUNDLE_ID_REQUEST_SUCCESS_MSG = "success.message";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}.
     * This value denotes the fragment message sent to callback object (the {@link Activity} attached)
     * when request failed.
     */
    public static final String BUNDLE_ID_REQUEST_FAILED_MSG = "failed.message";

    private IFragmentCallback callback;

    private boolean notifySuccessAfterAttach = false;
    private boolean notifyFailedAfterAttach = false;

    private String requestFailedMsg;

    private String requestSuccessMsg;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle arguments = getArguments();
        if (arguments != null) {
            requestSuccessMsg = arguments.getString(BUNDLE_ID_REQUEST_SUCCESS_MSG);
            requestFailedMsg = arguments.getString(BUNDLE_ID_REQUEST_FAILED_MSG);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onAttach", "Call"));
        this.callback = (IFragmentCallback) activity;
        if (notifySuccessAfterAttach) {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "onAttach", "notify success after attach."));
            notifySuccessAfterAttach = false;
            callback.handleFragmentMessage(requestSuccessMsg, null);
        }

        if (notifyFailedAfterAttach) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onAttach", "notify failed after attach."));
            notifyFailedAfterAttach = false;
            callback.handleFragmentMessage(requestFailedMsg, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onDetach", "Call"));
        callback = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestSuccess(Bundle resultData) {
        if (requestSuccessMsg == null) {
            requestSuccessMsg = "";
        }

        if (callback != null) {
            callback.handleFragmentMessage(requestSuccessMsg, resultData);
        } else {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "onRequestSuccess", "callback null. notify after attach."));
            notifySuccessAfterAttach = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestFailed() {
        if (requestFailedMsg == null) {
            requestFailedMsg = "";
        }

        if (callback != null) {
            callback.handleFragmentMessage(requestFailedMsg, null);
        } else {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed", "callback null. notify after attach."));
            notifyFailedAfterAttach = true;
        }
    }
}
