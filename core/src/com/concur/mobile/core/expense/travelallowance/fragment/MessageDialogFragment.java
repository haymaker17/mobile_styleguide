package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
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

    private DialogInterface.OnClickListener onOkListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.TADialog);
        Bundle arguments= getArguments();
        if (arguments != null) {
            if (arguments.containsKey(BundleId.MESSAGE)) {
                Message msg = (Message) arguments.getSerializable(BundleId.MESSAGE);
                builder.setMessage(msg.getMessageText(getActivity().getApplicationContext()));
                builder.setPositiveButton(R.string.general_ok, null);
            }
            if (arguments.containsKey(BundleId.MESSAGE_TEXT)) {
                builder.setMessage(arguments.getString(BundleId.MESSAGE_TEXT));
                builder.setPositiveButton(R.string.general_ok, this.onOkListener);
                builder.setNeutralButton(R.string.cancel, null);
                builder.setCancelable(true);
            }
        }
        return builder.create();
    }

    public void setOnOkListener (DialogInterface.OnClickListener onOkListener) {
        this.onOkListener = onOkListener;
    }
}
