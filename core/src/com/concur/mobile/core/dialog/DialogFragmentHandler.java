package com.concur.mobile.core.dialog;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Provides an abstract class implementing two dialog related callbacks, onClick and onCancel events.
 */
public abstract class DialogFragmentHandler implements ProgressDialogFragment.OnCancelListener,
        AlertDialogFragment.OnClickListener {

    public static void dismiss(FragmentManager fragMngr, String fragTag) {
        if (fragMngr != null && fragTag != null) {
            Fragment frag = fragMngr.findFragmentByTag(fragTag);
            if (frag instanceof DialogFragment) {
                DialogFragment dlgFrag = (DialogFragment) frag;
                dlgFrag.dismiss();
            }
        }
    }

    @Override
    public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
    }

    @Override
    public void onCancel(FragmentActivity activity, DialogInterface dialog) {
    }

}
