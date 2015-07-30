package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.Message;

/**
 * Created by Michael Becherer on 29-Jul-15.
 */
public class MessageDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle arguments= getArguments();
        if (arguments != null && arguments.containsKey(BundleId.MESSAGE)) {
            Message msg = (Message) arguments.getSerializable(BundleId.MESSAGE);
            builder.setMessage(msg.getMessageText(getActivity().getApplicationContext()));
            builder.setPositiveButton(R.string.general_ok, null);
        }
        return builder.create();
    }
}
