package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;

/**
 * Created by Michael Becherer on 11-Aug-15.
 */
public class ProgressDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog prgrDlg = new ProgressDialog(getActivity(), R.style.TADialog);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(BundleId.PROGRESS_DIALOG_TEXT)) {
            prgrDlg.setMessage(arguments.getString(BundleId.PROGRESS_DIALOG_TEXT));
        }
        prgrDlg.setCancelable(true);
        prgrDlg.setIndeterminate(true);
        return prgrDlg;
    }

}
