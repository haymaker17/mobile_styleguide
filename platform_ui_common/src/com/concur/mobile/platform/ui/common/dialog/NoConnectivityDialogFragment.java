package com.concur.mobile.platform.ui.common.dialog;

import android.os.Bundle;

import com.concur.mobile.platform.ui.common.R;

/**
 * NoConnectivityDialogFragment is the fragment displayed any time the user is attempting to use a feature requiring connectivity
 * and they are not connected. This is used throughout our code and was previous related to the Const.DIALOG_NO_CONNECTIVITY.
 * 
 * @author westonw
 * 
 */
public class NoConnectivityDialogFragment extends AlertDialogFragment {

    public NoConnectivityDialogFragment() {
        super();

        // Set all visible text.
        Bundle args = new Bundle();
        args.putInt(TITLE_RESOURCE_ID, R.string.dlg_no_connectivity_title);
        args.putInt(MESSAGE_RESOURCE_ID, R.string.dlg_no_connectivity_message);
        args.putInt(POSITIVE_BUTTON_RESOURCE_ID, R.string.okay);
        setArguments(args);
    }
}
