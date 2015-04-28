package com.concur.mobile.corp.activity;

import android.content.Context;
import android.support.v7.app.ActionBar;
// TODO - MOB-23434 - mulitbuild jira - do not check in the change in package name into develop
import com.concur.breeze.jarvis.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.service.ResetMobilePassword;

public class MobilePasswordSet extends BasePasswordSet {

    // Done
    @Override
    protected void setActionBarDetails() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_white_background));
        actionBar.setTitle(R.string.mobile_password_reset_password_button);
    }

    @Override
    protected boolean validatePasswords(String passwordField, String passwordConfirmField) {
        if (passwordField.length() < 1) {
            // Throw up a dialog saying the password is blank
            DialogFragmentFactory.getAlertOkayInstance(R.string.mobile_password_invalid_title,
                    R.string.mobile_password_blank).show(getSupportFragmentManager(), null);
            return false;
        } else if (!passwordField.equals(passwordConfirmField)) {
            // Throw up a dialog saying the texts in the password fields don't match
            DialogFragmentFactory.getAlertOkayInstance(R.string.password_dont_match_title,
                    R.string.password_dont_match_message).show(getSupportFragmentManager(), null);
            return false;
        }
        // All is well, passwords have been validated.
        return true;
    }

    @Override
    protected void executeResetUserPassword(Context context, int id, BaseAsyncResultReceiver receiver, String email,
            String keyPartA, String keyPartB, String mobilePassword) {

        new ResetMobilePassword(context, id, receiver, email, keyPartA, keyPartB, mobilePassword).execute();
    }

}
