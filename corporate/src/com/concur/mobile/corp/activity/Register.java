package com.concur.mobile.corp.activity;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.concur.breeze.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.net.PasswordLoginRequest;
import com.concur.mobile.core.util.net.RegisterRequest;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.util.Format;

public class Register extends Activity implements View.OnClickListener, Handler.Callback {

    private static final String LOGGED_IN_KEY = "LoggedIn";
    private static final String LOGIN_ID_KEY = "LoginId";
    private static final String SESS_ID_KEY = "SessionId";

    private static final int DIALOG_EMPTY_LOGIN = 1;
    private static final int DIALOG_EMPTY_PASSWORD = 2;

    private String loginId;
    private String regSessionId;

    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        loggedIn = false;
        if (savedInstanceState != null) {
            loggedIn = savedInstanceState.getBoolean(LOGGED_IN_KEY);
            loginId = savedInstanceState.getString(LOGIN_ID_KEY);
            regSessionId = savedInstanceState.getString(SESS_ID_KEY);
        }

        final Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        final Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);

        final View tipView = findViewById(R.id.registerTip);
        tipView.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        setControlsVisibility();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(LOGGED_IN_KEY, loggedIn);
        outState.putString(LOGIN_ID_KEY, loginId);
        outState.putString(SESS_ID_KEY, regSessionId);
    }

    private void setControlsVisibility() {
        LinearLayout loginControls = (LinearLayout) findViewById(R.id.loginControls);
        LinearLayout pinControls = (LinearLayout) findViewById(R.id.pinControls);

        if (loggedIn) {
            pinControls.setVisibility(View.VISIBLE);
            loginControls.setVisibility(View.GONE);
        } else {
            pinControls.setVisibility(View.GONE);
            loginControls.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {

        CharSequence msg = null;

        switch (id) {

        case DIALOG_EMPTY_LOGIN: {
            msg = getText(R.string.register_loginId_blank);
            break;
        }
        case DIALOG_EMPTY_PASSWORD:
            msg = getText(R.string.register_password_blank);
            break;
        } // end switch/case

        if (msg != null) {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.login_failure);
            dlgBldr.setMessage(msg);
            dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return dlgBldr.create();
        } else {
            return ((ConcurMobile) getApplication()).createDialog(this, id);
        }
    }

    public void onClick(View v) {

        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

        ConcurMobile app;
        switch (v.getId()) {
        case R.id.loginButton:

            // Grab values
            final TextView loginIdView = (TextView) findViewById(R.id.registerId);
            final TextView passwordView = (TextView) findViewById(R.id.registerPassword);
            loginId = loginIdView.getText().toString();
            String password = passwordView.getText().toString();

            // Make sure user entered valid values.
            if (loginId.trim().length() == 0) {
                showDialog(DIALOG_EMPTY_LOGIN);
                return;
            } else if (password.trim().length() == 0) {
                showDialog(DIALOG_EMPTY_PASSWORD);
                return;
            }

            showDialog(Const.DIALOG_LOGIN_WAIT);

            // Attempt to authenticate
            // Re-entry to the UI will be via handleMessage() below
            app = (ConcurMobile) getApplication();
            PasswordLoginRequest doLogin = new PasswordLoginRequest(this, new Handler(this), app.getProduct(),
                    serverAdd, loginId, password);
            doLogin.start();

            break;
        case R.id.submitButton:

            showDialog(Const.DIALOG_REGISTER_PIN);

            // Grab values
            final TextView pinView = (TextView) findViewById(R.id.registerPin);
            final TextView pinConfirmView = (TextView) findViewById(R.id.registerPinConfirm);
            String pin = pinView.getText().toString();
            String pin2 = pinConfirmView.getText().toString();

            // Validate the pins
            int msg = 0;
            if (pin.length() < 1) {
                msg = R.string.register_pin_blank;
            } else if (!pin.equals(pin2)) {
                msg = R.string.register_pin_mismatch;
            }

            if (msg != 0) {

                clearPins();

                // Bail out if they don't match
                dismissDialog(Const.DIALOG_REGISTER_PIN);

                DialogInterface.OnClickListener dummyListener = new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                    }
                };

                new AlertDialog.Builder(this).setMessage(msg).setPositiveButton(R.string.general_ok, dummyListener)
                        .show();

                break;
            }

            // Good pins, send them to the server
            app = (ConcurMobile) getApplication();
            RegisterRequest doRegister = new RegisterRequest(new Handler(this), app.getProduct(), serverAdd,
                    regSessionId, pin);
            doRegister.start();

            break;
        case R.id.registerTip:
            createRegisterTipDialog().show();
            break;
        }
    }

    protected Dialog createRegisterTipDialog() {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setTitle(R.string.register_pin_tip_title);
        dlgBldr.setMessage(R.string.register_pin_tip_text);
        dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dlgBldr.create();
    }

    protected Dialog createRegStatusDialog(String title, String msg, final boolean finish) {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setTitle(title);
        dlgBldr.setMessage(msg);
        dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (finish) {
                    finish();
                }
            }
        });
        return dlgBldr.create();
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {

        if (msg.what == Const.MSG_LOGIN_RESULT) {
            HashMap<String, Object> responses = (HashMap<String, Object>) msg.obj;
            regSessionId = (String) responses.get(Const.LR_SESSION_ID);

            if (regSessionId != null) {

                // Toggle the controls
                loggedIn = true;
                setControlsVisibility();

                // Dismiss the dialog
                dismissDialog(Const.DIALOG_LOGIN_WAIT);

                TextView pin = (TextView) findViewById(R.id.registerPin);
                pin.requestFocus();

            } else {

                String status = (String) responses.get(Const.LR_STATUS);

                // Fail the login if no session
                Dialog dlg = createRegStatusDialog(status, getText(R.string.register_failure_msg).toString(), false);
                dlg.show();

                // Clear the password field and focus on it
                TextView password = (TextView) findViewById(R.id.registerPassword);
                password.setText("");
                password.requestFocus();

                dismissDialog(Const.DIALOG_LOGIN_WAIT);
            }
        } else if (msg.what == Const.MSG_REGISTER_RESULT) {
            HashMap<String, Object> responses = (HashMap<String, Object>) msg.obj;

            // If any problem occurred (either with the request or with the action on the server side)
            // then a status message will be in the responses.
            if (!responses.containsKey(Const.RR_STATUS_MESSAGE)) {

                // Save the login ID
                ((ConcurMobile) getApplication()).savePreference(this, Const.PREF_LOGIN_TEMP, loginId);

                Dialog dlg = createRegStatusDialog(getText(R.string.register_status_title).toString(),
                        getText(R.string.register_successful).toString(), true);
                dlg.show();

                dismissDialog(Const.DIALOG_REGISTER_PIN);

            } else {

                String statusMsg = (String) responses.get(Const.RR_STATUS_MESSAGE);

                Dialog dlg = createRegStatusDialog(getText(R.string.register_failure).toString(), statusMsg, false);
                dlg.show();

                // Clear the pins
                clearPins();

                dismissDialog(Const.DIALOG_REGISTER_PIN);

            }
        }

        return true;
    }

    protected void clearPins() {
        TextView pin = (TextView) findViewById(R.id.registerPin);
        pin.setText("");
        pin.requestFocus();
        TextView pin2 = (TextView) findViewById(R.id.registerPinConfirm);
        pin2.setText("");
    }

}
