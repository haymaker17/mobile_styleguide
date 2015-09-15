package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.concur.mobile.core.expense.travelallowance.service.IRequestListener;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;

/**
 * Created by D049515 on 09.09.2015.
 */
public class ServiceRequestListenerFragment extends Fragment implements IRequestListener {
    public static final String CLASS_TAG = ServiceRequestListenerFragment.class.getSimpleName();

    public static final String BUNDLE_ID_REQUEST_SUCCESS_MSG = "success.message";
    public static final String BUNDLE_ID_REQUEST_FAILED_MSG = "failed.message";

    private IFragmentCallback callback;

    private boolean notifySuccessAfterAttach = false;
    private boolean notifyFailedAfterAttach = false;

    private String requestFailedMsg;

    private String requestSuccessMsg;

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onAttach", "Call"));
        this.callback = (IFragmentCallback) activity;
        if (notifySuccessAfterAttach) {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "onAttach", "notify success after attach."));
            notifySuccessAfterAttach = false;
            callback.sendMessage(requestSuccessMsg);
        }

        if (notifyFailedAfterAttach) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onAttach", "notify failed after attach."));
            notifyFailedAfterAttach = false;
            callback.sendMessage(requestFailedMsg);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onDetach", "Call"));
        callback = null;
    }


    @Override
    public void onRequestSuccess() {
        if (requestSuccessMsg == null) {
            requestSuccessMsg = "";
        }

        if (callback != null) {
            callback.sendMessage(requestSuccessMsg);
        } else {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "onRequestSuccess", "callback null. notify after attach."));
            notifySuccessAfterAttach = true;
        }
    }

    @Override
    public void onRequestFailed() {
        if (requestFailedMsg == null) {
            requestFailedMsg = "";
        }

        if (callback != null) {
            callback.sendMessage(requestFailedMsg);
        } else {
            Log.d(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "onRequestFailed", "callback null. notify after attach."));
            notifyFailedAfterAttach = true;
        }
    }
}
