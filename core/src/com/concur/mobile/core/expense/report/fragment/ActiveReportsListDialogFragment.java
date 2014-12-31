package com.concur.mobile.core.expense.report.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.activity.ActiveReportsListAdapter;

public class ActiveReportsListDialogFragment extends DialogFragment {

    // Sets the click listener connected to the report list
    protected DialogInterface.OnClickListener mClickListener;
    // Cancel Listener
    protected DialogInterface.OnCancelListener mCancelListener;

    protected DialogInterface.OnCancelListener externalCancelListener;
    protected DialogInterface.OnClickListener externalClickListener;

    // Holds the list adapter for the list of reports.
    protected ActiveReportsListAdapter mListAdapter;

    public ActiveReportsListDialogFragment() {
        setRetainInstance(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (mListAdapter == null) {
            mListAdapter = new ActiveReportsListAdapter(activity, true, false);
        }
        setCancelButton(builder);
        if (mClickListener == null) {
            mClickListener = new ClickListener();
        }
        builder.setSingleChoiceItems(mListAdapter, -1, mClickListener);

        // Construct a custom title view, one that contains a progress bar to indicate
        // an active report list is being updated.
        LayoutInflater inflater = LayoutInflater.from(activity);
        View titleView = inflater.inflate(R.layout.active_report_dialog_list_title, null);
        TextView txtView = (TextView) titleView.findViewById(R.id.title);
        if (txtView != null) {
            txtView.setText(R.string.select_report);
        }
        ProgressBar progBar = (ProgressBar) titleView.findViewById(R.id.progress);
        mListAdapter.setProgressBar(progBar);

        AlertDialog dialog = builder.create();
        // Set the custom title.
        dialog.setCustomTitle(titleView);
        return dialog;
    }

    // Set the list adapter.
    public void setActiveReportsListAdapter(ActiveReportsListAdapter listAdapter) {
        mListAdapter = listAdapter;
    }

    public ActiveReportsListAdapter getActiveReportsListAdapter() {
        return mListAdapter;
    }

    public void setClickListener(DialogInterface.OnClickListener clickListener) {
        externalClickListener = clickListener;
    }

    public void setCancelListener(final DialogInterface.OnCancelListener cancelListener) {
        externalCancelListener = cancelListener;
    }

    protected void setCancelButton(AlertDialog.Builder builder) {
        if (mCancelListener == null) {
            mCancelListener = new CancelListener();
        }

        builder.setOnCancelListener(mCancelListener);
        // Set up a cancel button which just forwards to the 'cancelListener' object.
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            /*
             * (non-Javadoc)
             * 
             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
             */
            public void onClick(DialogInterface dialog, int which) {
                if (externalCancelListener != null) {
                    externalCancelListener.onCancel(dialog);
                }
            }
        });

    }

    // This override must be made should this fragment retain instance in the future.
    @Override
    public void onDestroyView() {
        // NOTE: This code below will permit the dialog to be reshown after a device orientation change if 'onRetainInstance' is
        // set to 'true'.
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * If mClickListener is null, it creates this ClickListener. This will occur if this fragment is showing and we rotate the
     * device. The external listener will be set by the class calling this fragment when it is recreated.
     */
    class ClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (externalClickListener != null) {
                externalClickListener.onClick(dialog, which);
            }
        }
    }

    /**
     * If mCancelListener is null, it creates this ClickListener. This will occur if this fragment is showing and we rotate the
     * device. The external listener will be set by the class calling this fragment when it is recreated.
     */
    class CancelListener implements DialogInterface.OnCancelListener {

        @Override
        public void onCancel(DialogInterface dialog) {
            if (externalCancelListener != null) {
                externalCancelListener.onCancel(dialog);
            }
        }
    }

}
