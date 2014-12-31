package com.concur.mobile.corp.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.concur.breeze.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.service.ResetPassword;

public class PasswordSet extends BasePasswordSet {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EditText setPasswordHint = (EditText) findViewById(R.id.setPassword);
        setPasswordHint.setHint(R.string.password_hint);
        EditText setPasswordConfirmHint = (EditText) findViewById(R.id.setPasswordConfirm);
        setPasswordConfirmHint.setHint(R.string.password_hint_confirm);

        Button setPasswordButton = (Button) findViewById(R.id.setPasswordButton);
        setPasswordButton.setText(R.string.password_reset_password_button);

        String goodPasswordDescription = Preferences.getGoodPasswordMessageString();
        if (!TextUtils.isEmpty(goodPasswordDescription)) {
            TextView passwordRequirements = (TextView) findViewById(R.id.passwordRequirementDescription);
            passwordRequirements.setText(goodPasswordDescription);
        }

        findViewById(R.id.mobilePasswordInfo).setVisibility(View.GONE);
    }

    @Override
    protected void setActionBarDetails() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_white_background));
        actionBar.setTitle(R.string.password_reset_password_button);
    }

    @Override
    protected boolean validatePasswords(String passwordField, String passwordConfirmField) {
        if (passwordField.length() < 1) {
            // Throw up a dialog saying the password is blank
            DialogFragmentFactory.getAlertOkayInstance(R.string.password_invalid_title, R.string.password_blank).show(
                    getSupportFragmentManager(), null);
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

        new ResetPassword(context, id, receiver, email, keyPartA, keyPartB, mobilePassword).execute();
    }

}
