package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;

import com.concur.core.R;

/**
 * This progress dialog is cancelable after a certain delay. The default is 15 seconds but it is possible to pass a custom value
 * via {@link #setArguments(Bundle)}. Use {@link #BUNDLE_ID_THRESHOLD} and pass a value of type long in milliseconds.
 *
 * Created by Patricius Komarnicki on 10.09.2015.
 */
public class ProgressDialogFragment extends DialogFragment {

    /**
     * The bundle ID used for passing the threshold.
     */
    public static final String BUNDLE_ID_THRESHOLD = "threshold";

    private static final long DEFAULT_THRESHOLD = 15000;

    private long threshold = DEFAULT_THRESHOLD;

    private long startTime;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - startTime) > threshold) {
                    super.onBackPressed();
                }
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - startTime) > threshold) {
                    return super.onTouchEvent(event);
                }
                 return false;
            }
        };
        dialog.setMessage(getString(R.string.general_in_progress));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Bundle arguments = getArguments();
        if (arguments != null) {
            threshold = arguments.getLong(BUNDLE_ID_THRESHOLD, DEFAULT_THRESHOLD);
        }
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime", 0);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("startTime", startTime);
        super.onSaveInstanceState(outState);
    }

    
    @Override
    public int show(FragmentTransaction transaction, String tag) {
        startTime = System.currentTimeMillis();
        return super.show(transaction, tag);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        startTime = System.currentTimeMillis();
        super.show(manager, tag);
    }

}
