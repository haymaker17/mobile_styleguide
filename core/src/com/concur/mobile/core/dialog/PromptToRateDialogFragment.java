package com.concur.mobile.core.dialog;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;

/**
 * PromptToRateDialogFragment is the fragment to prompt the user to rate our application on the Play Store. It is an
 * AlertDialogFragment with two button options. Old references use Const.DIALOG_PROMPT_TO_RATE.
 * 
 * @author westonw
 * 
 */
public class PromptToRateDialogFragment extends AlertDialogFragment {

    public PromptToRateDialogFragment() {
        super();
        
        // Set all visible text.
        Bundle args = new Bundle();
        args.putInt(TITLE_RESOURCE_ID, R.string.dlg_prompt_to_rate_title);
        args.putInt(MESSAGE_RESOURCE_ID, R.string.dlg_prompt_to_rate_message);
        args.putInt(POSITIVE_BUTTON_RESOURCE_ID, R.string.dlg_prompt_to_rate_yes);
        args.putInt(NEGATIVE_BUTTON_RESOURCE_ID, R.string.dlg_prompt_to_rate_no);
        setArguments(args);

        // Set up positive listener which will launch the Play Store to rate our app.
        setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
                Preferences.setPromptedToRate(prefs);

                Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.concur.breeze"));
                market.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    activity.startActivity(market);
                } catch (ActivityNotFoundException e) {
                    Log.e(Const.LOG_TAG, "No activity found to handle market:// URI");
                }
            }
        });

        // Negative listener sets that user was prompted to rate and dismisses the dialog.
        setNegativeButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
                Preferences.setPromptedToRate(prefs);
            }
        });
    }

}
