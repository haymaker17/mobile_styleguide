package com.concur.mobile.platform.ui.common.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A standard extension of {@link DialogFragment} to display progress dialogs. This class is a copy of ProgressDialogFragment but
 * has no dependency on support.v4 library
 */
public class ProgressDialogFragmentV1 extends DialogFragment {

    // Message text/resource ID argument keys.
    private static final String MESSAGE_RESOURCE_ID = "msg.id";
    private static final String MESSAGE_TEXT = "msg.text";

    // Indetermine setting.
    private static final String INDETERMINATE = "indeterminate";

    // Cancel On Touch Outside setting
    private static final String CANCEL_ON_TOUCH_OUTSIDE = "cancel.on.touch.outside";

    public interface OnCancelListener {

        /**
         * Provides a notification of a progress dialog that has been cancelled.
         * 
         * @param activity
         *            contains a reference to the current <code>Activity</code> associated with the dialog fragment. <b>NOTE:</b>
         *            this value may change due to device orientation.
         * @param dialog
         *            contains a reference to the <code>DialogInterface</code> object.
         */
        public void onCancel(Activity activity, DialogInterface dialog);
    }

    // Contains the cancel listener.
    private ProgressDialogFragmentV1.OnCancelListener mCancelListener;

    public ProgressDialogFragmentV1() {
        setRetainInstance(true);
    }

    /**
     * Sets the text of the dialog message.
     * 
     * @param message
     *            contains the text of the dialog message.
     */
    public void setMessage(String message) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(MESSAGE_TEXT, message);
        setArguments(args);
    }

    /**
     * Sets the text resource ID of the dialog message.
     * 
     * @param title
     *            contains the resource ID of the dialog message.
     */
    public void setMessage(int message) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putInt(MESSAGE_RESOURCE_ID, message);
        setArguments(args);
    }

    /**
     * Sets whether the progress dialog is indeterminate.
     * 
     * @param indeterminate
     *            contains whether the progress dialog is indeterminate.
     */
    public void setIndeterminate(boolean indeterminate) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putBoolean(INDETERMINATE, indeterminate);
        setArguments(args);
    }

    /**
     * Sets whether the progress dialog can be cancelled when touched outside the window.
     * 
     * @param cancelOnTouchOutside
     *            contains whether the progress dialog can be cancelled.
     */
    public void setCanceledOnTouchOutside(boolean cancelOnTouchOutside) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putBoolean(CANCEL_ON_TOUCH_OUTSIDE, cancelOnTouchOutside);
        setArguments(args);
    }

    /**
     * Sets the cancel listener for the dialog.
     * 
     * @param listener
     *            contains a reference to an instance of <code>ProgressDialogFragment.OnCancelListener</code>.
     */
    public void setCancelListener(final ProgressDialogFragmentV1.OnCancelListener listener) {
        this.mCancelListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog progDlg = new ProgressDialog(getActivity());

        // Construct or reference the current arguments.
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        configureMessage(progDlg, args);
        configureIndeterminate(progDlg, args);

        configureCancelOnTouchOutside(progDlg, args);

        return progDlg;
    }

    /**
     * Will configure a message within the progress dialog.
     * 
     * @param progDlg
     *            contains an instance of <code>ProgressDialog</code> used to configure the message.
     * 
     * @param args
     *            contains an instance of <code>Bundle</code> containing the dialog arguments.
     */
    protected void configureMessage(ProgressDialog progDlg, Bundle args) {

        // Set the message using the resource ID or the text.
        if (args.containsKey(MESSAGE_RESOURCE_ID)) {
            progDlg.setMessage(getActivity().getText(args.getInt(MESSAGE_RESOURCE_ID)));
        } else if (args.containsKey(MESSAGE_TEXT)) {
            progDlg.setMessage(args.getString(MESSAGE_TEXT));
        }
    }

    /**
     * Will configure a message within the progress dialog.
     * 
     * @param progDlg
     *            contains an instance of <code>ProgressDialog</code> used to configure the message.
     * 
     * @param args
     *            contains an instance of <code>Bundle</code> containing the dialog arguments.
     */
    protected void configureIndeterminate(ProgressDialog progDlg, Bundle args) {
        progDlg.setIndeterminate(args.getBoolean(INDETERMINATE));
    }

    /**
     * Will configure cancel on touch outside the progress dialog window.
     * 
     * @param progDlg
     *            contains an instance of <code>ProgressDialog</code> used to configure the cancel on touch outside.
     * 
     * @param args
     *            contains an instance of <code>Bundle</code> containing the dialog arguments.
     */
    protected void configureCancelOnTouchOutside(ProgressDialog progDlg, Bundle args) {
        // Configure the setting if explicitly set by the dialog creator
        if (args.containsKey(CANCEL_ON_TOUCH_OUTSIDE)) {
            progDlg.setCanceledOnTouchOutside(args.getBoolean(CANCEL_ON_TOUCH_OUTSIDE));
        } else {
            // default setting to allow cancel
            progDlg.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mCancelListener != null) {
            mCancelListener.onCancel(getActivity(), dialog);
        }
    }

    @Override
    public void onDestroyView() {
        // NOTE: This code below will permit the dialog to be reshown after a device orientation change
        // if 'onRetainInstance' is set to 'true'.
        if (getDialog() != null && getRetainInstance()) {
            // getDialog().setOnDismissListener(null);
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

}
