package com.concur.mobile.corp.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.concur.breeze.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.AuthenticationResponseHandler;
import com.concur.mobile.core.util.net.LoginRequest;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.util.Format;

/**
 * @deprecated - use {@link com.concur.mobile.corp.activity.EmailLookupActivity} instead.
 */
public class Login extends ActionBarActivity implements View.OnClickListener {

    private static final String CLS_TAG = Login.class.getSimpleName();

    private static final String EXTRA_ADVANCE_TO_COMPANY_SIGN_ON = "advance.to.company.sign.on";

    protected final static String LOGIN_STATE_KEY = "login";
    protected final static String PIN_STATE_KEY = "pin";
    protected final static String ORIENTATION_CHANGED = "orientation_changed";

    private final SparseArray<Dialog> dialogs = new SparseArray<Dialog>(3);

    protected EditText loginView;
    protected EditText pinView;

    private Button loginButton;
    private Button registerButton;
    private TextView loginText;

    // Contains whether this activity was launched with a logged out flag.
    protected boolean loggedOut;

    private LoginResponseHandler loginResponseHandler;

    private ConnectivityReceiver connectivityReceiver;

    private IntentFilter connectivityFilter;

    private boolean connectivityReceiverRegistered;

    private boolean orientationChange;

    private boolean fromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MOB-17060 Get orientation change for SSO auto-redirect
        if (savedInstanceState != null) {
            orientationChange = savedInstanceState.getBoolean(ORIENTATION_CHANGED);
        }

        // Determine whether this activity should immediately forward to the
        // Company SignIn activity.
        boolean advanceToCompanyLogon = getIntent().getBooleanExtra(EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, false);
        // MOB-17060 If just orientation change, do not auto-direct to SSO
        if (!orientationChange && advanceToCompanyLogon) {
            String companyCode = getIntent().getStringExtra(Const.EXTRA_SSO_COMPANY_CODE);
            Intent i = new Intent(this, CompanyCodeLoginActivity.class);
            i.putExtra(EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
            if (companyCode != null && companyCode.length() > 0) {
                i.putExtra(Const.EXTRA_SSO_COMPANY_CODE, companyCode);
            }
            startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
        }

        setContentView(R.layout.login);

        getSupportActionBar().setTitle(R.string.login_title);

        // Set the 'loggedOut' state.
        if (savedInstanceState != null) {
            loggedOut = savedInstanceState.getBoolean(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, false);
        } else {
            loggedOut = getIntent().getBooleanExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, false);
        }

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        loginText = (TextView) findViewById(R.id.loginText);

        // footer layout
        TextView ssoLoginView = (TextView) findViewById(R.id.company_sso_login);
        if (ssoLoginView != null) {
            ssoLoginView.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    Intent i = new Intent(Login.this, CompanyCodeLoginActivity.class);
                    startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
                }
            });
        }

        connectivityReceiver = new ConnectivityReceiver();
        connectivityFilter = new IntentFilter(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
        connectivityFilter.addAction(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);

        loginView = (EditText) findViewById(R.id.loginId);
        pinView = (EditText) findViewById(R.id.loginPin);
        // set default type face so user can see login and password editview hint in same font style.
        pinView.setTypeface(Typeface.DEFAULT);

        // Set the default action for the pin view to do the submit
        pinView.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    Login.this.onClick(loginButton);
                    return true;
                }
                return false;
            }
        });

        // Restore settings if this is a re-creation
        if (savedInstanceState != null) {
            loginView.setText(savedInstanceState.getCharSequence(LOGIN_STATE_KEY));
            if (!loggedOut) {
                pinView.setText(savedInstanceState.getCharSequence(PIN_STATE_KEY));
            }
        }

        // Check if the response handler was retained due to a configuration
        // change instance.
        if (getLastCustomNonConfigurationInstance() instanceof LoginResponseHandler) {
            loginResponseHandler = (LoginResponseHandler) getLastCustomNonConfigurationInstance();
            loginResponseHandler.setActivity(this);
        }

        // Register the data connectivity receiver.
        Intent stickyIntent = registerReceiver(connectivityReceiver, connectivityFilter);
        if (stickyIntent != null) {
            updateUIForConnectivity(stickyIntent.getAction());
        } else {
            updateUIForConnectivity(null);
        }
        connectivityReceiverRegistered = true;
        // push notification
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(ConcurMobile.FROM_NOTIFICATION)) {
            fromNotification = getIntent().getExtras().getBoolean(ConcurMobile.FROM_NOTIFICATION);
        }

        startActivity(new Intent(Login.this, LoginPasswordActivity.class));

    }

    /**
     * Will update the UI based on the current state of connectivity.
     */
    private void updateUIForConnectivity(String action) {

        boolean connected = false;
        if (action != null) {
            if (action.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE)) {
                connected = true;
            } else if (action.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE)) {
                connected = false;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateUIForConnectivity: unhandled action '" + action + "'.");
                connected = ConcurMobile.isConnected();
            }
        } else {
            connected = ConcurMobile.isConnected();
        }

        if (connected) {
            registerButton.setVisibility(View.GONE);
            loginText.setVisibility(View.GONE);
            loginButton.setEnabled(true);

            // Hide the "offline" header.
            View view = findViewById(R.id.login_offline_header);
            if (view != null) {
                View offlineHeader = view.findViewById(R.id.offline_header);
                if (offlineHeader != null && offlineHeader.getVisibility() == View.VISIBLE) {
                    offlineHeader.setVisibility(View.GONE);
                }
            }

        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String savedLoginId = Preferences.getLogin(prefs, null);
            String savedPin = Preferences.getPin(prefs, null);

            registerButton.setVisibility(View.GONE);

            // Hide show the "offline" header.
            View offlineHeader = findViewById(R.id.offline_header);
            if (offlineHeader != null && offlineHeader.getVisibility() == View.GONE) {
                offlineHeader.setVisibility(View.VISIBLE);
            }

            if (savedLoginId != null && savedPin != null && !loggedOut) {
                // If there are saved credentials and the user hasn't logged
                // out, then hide the notice and allow login
                loginButton.setEnabled(true);
                loginText.setVisibility(View.GONE);
            } else {
                // Otherwise, show the login disallowed message and disable the
                // button
                loginButton.setEnabled(false);
                loginText.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_CODE_SSO_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                // SSO login succeeded, so just finish this activity.
                setResult(resultCode, data);
                finish();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (loginResponseHandler != null) {
            // The new Login instance will reset the Login reference.
            loginResponseHandler.setActivity(null);
            return loginResponseHandler;
        } else {
            return super.onRetainCustomNonConfigurationInstance();
        }
    }

    /*
     * 
     */
    @Override
    protected void onStart() {
        super.onStop();
        EventTracker.INSTANCE.activityStart(this);
    }

    /*
     * 
     */
    @Override
    protected void onStop() {
        super.onStop();
        EventTracker.INSTANCE.activityStop(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the data connectivity receiver.
        if (connectivityReceiverRegistered) {
            unregisterReceiver(connectivityReceiver);
            connectivityReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-register connectivity receiver.
        if (!connectivityReceiverRegistered) {
            // Register the receiver.
            Intent stickyIntent = registerReceiver(connectivityReceiver, connectivityFilter);
            if (stickyIntent != null) {
                updateUIForConnectivity(stickyIntent.getAction());
            }
            connectivityReceiverRegistered = true;
        }

        // MOB-11389
        // If we simply rotated the device, don't revert back to ID saved in
        // preferences, keep what we have typed.
        if (!orientationChange) {
            // Populate controls with saved values
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.contains(Const.PREF_LOGIN_TEMP)) {
                // If there is a temp login (from registration) then use it and
                // clear it
                loginView.setText(prefs.getString(Const.PREF_LOGIN_TEMP, ""));
                prefs.edit().remove(Const.PREF_LOGIN_TEMP).commit();
            } else if (prefs.getBoolean(Const.PREF_SAVE_LOGIN, false)) {
                // Restore login ID
                String savedText = Preferences.getLogin(prefs, "");
                if (savedText != null && !savedText.equals("")) {
                    loginView.setText(savedText);
                }
            }
        } else {
            orientationChange = false;
        }

        Editable loginText = loginView.getText();
        if (loginText != null && loginText.length() > 0) {
            // Focus on the pin
            pinView.requestFocus();
        } else {
            // Focus on the login
            loginView.requestFocus();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(LOGIN_STATE_KEY, loginView.getText());
        outState.putCharSequence(PIN_STATE_KEY, pinView.getText());
        outState.putBoolean(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, loggedOut);

        orientationChange = (getChangingConfigurations() != 0);
        outState.putBoolean(ORIENTATION_CHANGED, orientationChange);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menuSettings:
            Intent i = new Intent(this, Preferences.class);
            startActivity(i);
            break;
        // case R.id.menuViewLog:
        // i = new Intent(this, LogView.class);
        // startActivity(i);
        // break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = dialogs.get(id);

        if (dlg == null) {
            switch (id) {
            case Const.DIALOG_COMPANY_SIGNON_REQUIRED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.login_sso_required_title);
                dlgBldr.setMessage(getText(R.string.login_sso_required_msg));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Take the end-user to the company sign-on
                        // page.
                        Intent i = new Intent(Login.this, CompanyCodeLoginActivity.class);
                        startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            default: {
                dlg = ((ConcurMobile) getApplication()).createDialog(this, id);
                dialogs.put(id, dlg);
            }
            }
        }
        return dlg;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && (getIntent().getBooleanExtra(Const.EXTRA_LOGIN_LAUNCHED_FROM_PRE_LOGIN, false) || getIntent()
                        .getBooleanExtra(Const.EXTRA_LOGIN_LAUNCHED_FROM_TEST_DRIVE_REGISTRATION, false))) {

            EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_BACK_BUTTON_CLICK);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.loginButton:
            // Record the number of times the user tries to sign in.
            Preferences.incrementTestDriveSigninTryAgainCount();

            // Grab values
            String loginId = loginView.getText().toString();
            String pinOrPassword = pinView.getText().toString();

            // Hide the soft keyboard when user clicks sign in. Keyboard will not auto-hide if there's any AlertDialog in home
            // screen and will cover the AlertDialog on orientation change.
            com.concur.mobile.platform.ui.common.util.ViewUtil.hideSoftKeyboard(this, pinView.getWindowToken());

            // MOB-16163 - validate username and password
            boolean isValid = true;
            StringBuilder msgBuilder = new StringBuilder();

            // check for one occurrence of '@' character in the loginId
            if ((!loginId.contains("@") || (loginId.indexOf("@") != loginId.lastIndexOf("@")))) {
                isValid = false;
                msgBuilder.append(getText(R.string.login_id_invalid));
                msgBuilder.append(' ');
                msgBuilder.append(getText(R.string.login_try_again_help));

            } else if (pinOrPassword.trim().length() == 0) {
                isValid = false;
                msgBuilder.append(getText(R.string.login_password_or_pin_invalid));
                msgBuilder.append(' ');
                msgBuilder.append(getText(R.string.login_try_again_help));
            }

            if (isValid) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

                String loginViewText = loginView.getText().toString();
                if (loginViewText != null && !loginViewText.equals("")) {
                    Preferences.saveLogin(prefs, loginViewText);
                }

                if (ConcurMobile.isConnected()) {
                    showDialog(Const.DIALOG_LOGIN_WAIT);

                    // We always save these bits in order to make sure offline
                    // works. The preference only
                    // controls if we show the login value on the login screen.
                    Preferences.saveLogin(prefs, loginId);
                    Preferences.savePin(prefs, pinOrPassword);

                    String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

                    // Attempt to authenticate
                    // Re-entry to the UI will be via handleMessage() below
                    ConcurMobile app = (ConcurMobile) getApplication();
                    if (loginResponseHandler == null) {
                        loginResponseHandler = new LoginResponseHandler(this);
                    }
                    // Ensure any previous message is cleared out.
                    loginResponseHandler.clearMessage();
                    LoginRequest doLogin = new LoginRequest(app, new Handler(loginResponseHandler), app.getProduct(),
                            serverAdd, loginId, pinOrPassword);
                    doLogin.start();
                } else {
                    // Offline login
                    String savedLoginId = Preferences.getLogin(prefs, null);
                    String savedPin = Preferences.getPin(prefs, null);
                    // TODO: How to handle if user logs in offline with password?
                    if (loginId.equalsIgnoreCase(savedLoginId) && pinOrPassword.equals(savedPin)) {
                        // Good login.
                        // Since this is offline, clear out some stuff to signal to
                        // lower levels
                        // that this was an offline login.
                        Preferences.clearSession(prefs, true);

                        // Also clear out the oAuth access token.
                        Preferences.clearAccessToken();

                        // Go to the home screen
                        startHomeScreen();

                        // And don't come back here
                        finish();

                    } else {
                        // Tell them no
                        Dialog dlg = createLoginFailureDialog(getText(R.string.login_invalid_concur_credentials)
                                .toString(), getText(R.string.login_try_again_help).toString(), R.string.try_again,
                                true);
                        dlg.show();

                        // Clear the login and pin fields and focus on login
                        loginView.setText("");
                        pinView.setText("");
                        loginView.requestFocus();

                    }
                }
            } else {
                // Tell them no
                Dialog dlg = createLoginFailureDialog(getText(R.string.login_invalid_concur_credentials).toString(),
                        msgBuilder.toString(), R.string.try_again, true);
                dlg.show();

                loginView.requestFocus();
            }
            break;
        case R.id.registerButton: {
            Intent i = new Intent(this, Register.class);
            startActivity(i);

            break;
        }
        case R.id.login_help: {
            Intent i = new Intent(Login.this, LoginHelp.class);
            startActivity(i);
        }
        }
    }

    private void startHomeScreen() {
        ((ConcurMobile) getApplication()).initSystemConfig();
        ((ConcurMobile) getApplication()).initUserConfig();
        setResult(RESULT_OK);
        if (!fromNotification) {
            gotoHome();
        }
    }

    public void gotoHome() {
        Intent i = null;
        i = new Intent(this, Home.class);
        startActivity(i);
    }

    protected Dialog createLoginFailureDialog(CharSequence title, CharSequence msg, final int dismissButtonId,
            boolean showHelpButton) {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setTitle(title);
        dlgBldr.setMessage(msg);
        dlgBldr.setPositiveButton(getText(dismissButtonId), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (dismissButtonId == R.string.try_again) {
                    // Increment the try count
                    Preferences.incrementLoginTryAgainCount();

                    // Keyboard will have been hidden here, show it.
                    com.concur.mobile.platform.ui.common.util.ViewUtil.showSoftKeyboard(Login.this, getResources().getConfiguration());
                }

                dialog.dismiss();
            }
        });

        if (showHelpButton) {
            dlgBldr.setNeutralButton(R.string.get_help, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    // Track the overall login attempt event
                    trackLoginOverall("Get Help");

                    dialog.dismiss();

                    Intent i = new Intent(Login.this, LoginHelp.class);
                    startActivity(i);
                }
            });
        }

        return dlgBldr.create();
    }

    /**
     * An extension of <code>AuthenticationResponseHandler</code> for handling pin-based authentications.
     * 
     * @author AndrewK
     */
    private static class LoginResponseHandler extends AuthenticationResponseHandler {

        /**
         * Constructs an instance of <code>LoginResponseHandler</code> with a current login object.
         * 
         * @param login
         *            the current login handler.
         */
        LoginResponseHandler(Login login) {
            super(login);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.util.net.AuthenticationResponseHandler# handleFailure(java.util.Map)
         */
        @Override
        protected void handleFailure(Map<String, Object> responses) {
            Login login = (Login) activity;
            if (login != null) {
                boolean wiped = responses.containsKey(Const.LR_WIPED);
                if (wiped) {

                    // Track it
                    trackLoginFailure("Remote Wipe");

                    // Let 'em know.
                    Dialog dlg = login.createLoginFailureDialog(login.getText(R.string.account_locked),
                            login.getText(R.string.account_locked_message), R.string.close, false);
                    dlg.show();

                    // Clear both fields
                    login.pinView.setText("");
                    login.loginView.setText("");
                    login.loginView.requestFocus();
                    // Clear out the web view cache.
                    login.loginView.post(new Runnable() {

                        public void run() {
                            ViewUtil.clearWebViewCookies(activity);                            
                        }
                    });

                } else {
                    String status = (String) responses.get(Const.LR_STATUS);

                    if (status != null && status.equalsIgnoreCase("SSO login required")) {
                        login.showDialog(Const.DIALOG_COMPANY_SIGNON_REQUIRED);
                    } else {
                        boolean showHelpButton = true;
                        // Fail the login if no session
                        String title = status;

                        if (title == null) {
                            title = login.getText(R.string.login_failure).toString();
                        }

                        String message = login.getText(R.string.login_try_again_help).toString();

                        if (Const.LR_STATUS_DISABLED.equalsIgnoreCase(title)) {
                            title = login.getText(R.string.login_disabled_title).toString();
                            message = login.getText(R.string.login_disabled_message).toString();
                            showHelpButton = false;
                        } else if (Const.LR_STATUS_EXPIRED.equalsIgnoreCase(title)) {
                            title = login.getText(R.string.login_pin_expired_title).toString();
                            showHelpButton = false;
                        }

                        // Check the HTTP response for tracking
                        Integer httpStatus = (Integer) responses.get(Const.REPLY_HTTP_STATUS_CODE);
                        if (httpStatus != null && httpStatus == HttpStatus.SC_FORBIDDEN) {
                            trackLoginFailure("Forbidden");
                        } else if (httpStatus != null && httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                            trackLoginFailure("Server Error");
                        } else {
                            trackLoginFailure("Bad Credentials");
                        }

                        Dialog dlg = login.createLoginFailureDialog(title, message, R.string.try_again, showHelpButton);
                        dlg.show();
                    }

                    // Clear the pin field and focus on it
                    login.pinView.setText("");
                    login.pinView.requestFocus();
                }
                login.dismissDialog(Const.DIALOG_LOGIN_WAIT);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.util.net.AuthenticationResponseHandler# handleSuccess(java.util.Map)
         */
        @Override
        protected void handleSuccess(Map<String, Object> responses) {

            String sessionId = (String) responses.get(Const.LR_SESSION_ID);

            Login login = (Login) activity;

            // Save the login response information.
            ConcurCore.saveLoginResponsePreferences(sessionId, (ConcurMobile) login.getApplication(), responses);

            trackLoginSuccess(Flurry.PARAM_VALUE_PIN_OR_PASSWORD);

            // Track the overall login attempt event
            trackLoginOverall("Success");

            // Go to homescreen ...
            login.startHomeScreen();

            // Dismiss the dialog after firing off the intent.
            // This avoids having the login screen visible for some time before
            // the home screen appears. Happens when
            // things
            // are slow.
            login.dismissDialog(Const.DIALOG_LOGIN_WAIT);

            // And don't come back here
            login.finish();

            // Set this back to 0 so we don't record this attempt
            // if the user goes back to try and register. We only
            // want to record if the user failed to sign in.
            Preferences.setTestDriveSigninTryAgainCount(0);
        }

    }

    public static void trackLoginSuccess(String credType) {
        // NOTE: This relies on preferences so it must be called AFTER the login response is parsed and saved

        // Track the login success
        Map<String, String> params = new HashMap<String, String>();
        params.put("Credential Type", credType);

        String paramValue = Flurry.PARAM_VALUE_CTE;

        Context ctx = ConcurCore.getContext();

        if (ViewUtil.isBreezeUser(ctx)) {
            paramValue = Flurry.PARAM_VALUE_BREEZE;
        } else if (!ViewUtil.isExpenser(ctx) && !ViewUtil.isTravelUser(ctx) && ViewUtil.isExpenseApprover(ctx)) {
            paramValue = "Approval Only";
        } else if (!ViewUtil.isTravelUser(ctx)) {
            paramValue = Flurry.PARAM_VALUE_EXPENSE_ONLY;
        } else if (!ViewUtil.isExpenser(ctx)) {
            paramValue = Flurry.PARAM_VALUE_TRAVEL_ONLY;
        }
        params.put("User Type", paramValue);

        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_SUCCESS, params);
    }

    public static void trackLoginFailure(String failureType) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("Type", failureType);

        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_FAILURE, params);

    }

    public static void trackLoginOverall(String finalState) {

        Map<String, String> params = new HashMap<String, String>();
        int tryAgainCount = Preferences.getLoginTryAgainCount();
        String bucket = (tryAgainCount <= 5 ? Integer.toString(tryAgainCount) : "Over 5");
        params.put("Try Again Count", bucket);

        params.put("Final", finalState);

        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_OVERALL, params);

    }

    /**
     * 
     * @deprecated - use {@link com.concur.mobile.corp.activity.EmailLookupActivity.ConnectivityReceiver} instead.
     * 
     *             An extension of <code>BroadcastReceiver</code> for handling broadcasted changes in data connectivity.
     * 
     * @author AndrewK
     */
    class ConnectivityReceiver extends BroadcastReceiver {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUIForConnectivity(intent.getAction());
        }

    }
}
