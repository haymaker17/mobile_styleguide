package com.concur.mobile.core.dialog;

import android.os.Bundle;

import com.concur.core.R;

/**
 * SystemUnavailableDialogFragment is the dialog fragment for a very generic message indicating that the system is unavailable at
 * this time. This is used throughout our code and was previous related to the Const.DIALOG_SYSTEM_UNAVAILABLE.
 * 
 * @author westonw
 * 
 */
public class SystemUnavailableDialogFragment extends AlertDialogFragment {

    public SystemUnavailableDialogFragment() {
        super();

        // Set all visible text.
        Bundle args = new Bundle();
        args.putInt(TITLE_RESOURCE_ID, R.string.dlg_system_unavailable_title);
        args.putInt(MESSAGE_RESOURCE_ID, R.string.dlg_system_unavailable_message);
        args.putInt(POSITIVE_BUTTON_RESOURCE_ID, R.string.okay);
        setArguments(args);
    }
}
