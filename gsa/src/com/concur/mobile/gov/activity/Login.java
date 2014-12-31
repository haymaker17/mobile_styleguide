package com.concur.mobile.gov.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.AuthenticationResponseHandler;
import com.concur.mobile.core.util.net.LoginRequest;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovMessagesReply;
import com.concur.mobile.gov.service.GovMessagesRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GeneralBaseReceiver;
import com.concur.mobile.platform.util.Format;

public class Login extends ActionBarActivity implements View.OnClickListener {

    private static final String CLS_TAG = Login.class.getSimpleName();

    protected final static String LOGIN_STATE_KEY = "login";
    protected final static String PIN_STATE_KEY = "pin";

    private final static int DIALOG_WARNING_MORE = 10;
    private final static int DIALOG_PAN_MORE = 11;

    private final SparseArray<Dialog> dialogs = new SparseArray<Dialog>(3);

    protected EditText loginView;
    protected EditText pinView;

    private Button loginButton;
    private Button registerButton;
    private TextView loginText;

    // Contains whether this activit was launched with a logged out flag.
    protected boolean loggedOut;

    private LoginResponseHandler loginResponseHandler;

    private ConnectivityReceiver connectivityReceiver;

    private IntentFilter connectivityFilter;

    private boolean connectivityReceiverRegistered;

    private GovAppMobile app;
    private GovMessagesRequest request;
    private GovMsgReciever msgReceiver;
    private IntentFilter msgsFilter;
    private String errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        app = (GovAppMobile) getApplication();

        // Set the 'loggedOut' state.
        if (savedInstanceState != null) {
            loggedOut = savedInstanceState.getBoolean(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, false);
        } else {
            loggedOut = getIntent().getBooleanExtra(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, false);
        }

        getSupportActionBar().setTitle(R.string.login_button);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        loginText = (TextView) findViewById(R.id.loginText);

        TextView footerTxtView = (TextView) findViewById(R.id.footer_navigation_bar_status);
        footerTxtView.setVisibility(View.GONE);
        connectivityReceiver = new ConnectivityReceiver();
        connectivityFilter = new IntentFilter(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
        connectivityFilter.addAction(com.concur.mobile.platform.ui.common.util.Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);

        loginView = (EditText) findViewById(R.id.loginId);
        pinView = (EditText) findViewById(R.id.loginPin);

        // Set the default action for the pin view to do the submit
        pinView.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
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

        // Check if the response handler was retained due to a configuration change instance.
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
        TextView txtView = (TextView) findViewById(R.id.companySignInText);
        txtView.setVisibility(View.VISIBLE);
        // get privacy act notice
        getPrivacyActNotice();
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
                connected = GovAppMobile.isConnected();
            }
        } else {
            connected = GovAppMobile.isConnected();
        }

        if (connected) {
            registerButton.setVisibility(View.GONE);
            loginText.setVisibility(View.GONE);
            loginButton.setEnabled(true);

            // Hide the "offline" header.
            View offlineHeader = findViewById(R.id.offline_header);
            if (offlineHeader != null && offlineHeader.getVisibility() == View.VISIBLE) {
                offlineHeader.setVisibility(View.GONE);
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
                // If there are saved credentials and the user hasn't logged out, then hide the notice and allow login
                loginButton.setEnabled(true);
                loginText.setVisibility(View.GONE);
            } else {
                // Otherwise, show the login disallowed message and disable the button
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
        if (msgReceiver != null) {
            unregisterMsgReceiver();
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

        if (msgReceiver == null) {
            registerMsgReceiver();
        }

        // Populate controls with saved values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(Const.PREF_LOGIN_TEMP)) {
            // If there is a temp login (from registration) then use it and clear it
            loginView.setText(prefs.getString(Const.PREF_LOGIN_TEMP, ""));
            prefs.edit().remove(Const.PREF_LOGIN_TEMP).commit();
        } else if (prefs.getBoolean(Const.PREF_SAVE_LOGIN, false)) {
            // Restore login ID
            loginView.setText(Preferences.getLogin(prefs, ""));
        }

        // Focus on the pin
        loginView.requestFocus();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(LOGIN_STATE_KEY, loginView.getText());
        outState.putCharSequence(PIN_STATE_KEY, pinView.getText());
        outState.putBoolean(com.concur.mobile.platform.ui.common.util.Const.EXTRA_LOGOUT, loggedOut);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = dialogs.get(id);
        if (dlg == null) {
            switch (id) {
            case com.concur.mobile.gov.util.Const.DIALOG_GOV_MSGS_INPROGRESS: {
                ProgressDialog pDialog = new ProgressDialog(this);
                pDialog.setMessage(this.getText(R.string.gov_retrieve_msgs));
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(true);
                pDialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Dismiss the dialog.
                        dialog.dismiss();
                        // Attempt to cancel the request.
                        if (request != null) {
                            request.cancel();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: request is null!");
                        }
                    }
                });
                dlg = pDialog;
                break;
            }
            case com.concur.mobile.gov.util.Const.DIALOG_GOV_MSGS_FAIL: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.gov_login_warning_msg_fails);
                dlgBldr.setMessage("");
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case DIALOG_WARNING_MORE: {
                GovAppMobile concurMobile = (GovAppMobile) getApplication();
                GovMessagesReply reply = concurMobile.getMsgs();
                String msg = "";
                if (reply != null) {
                    msg = reply.warningText;
                }
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.gov_login_warining_title);
                dlgBldr.setMessage(msg);
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case DIALOG_PAN_MORE: {
                GovAppMobile concurMobile = (GovAppMobile) getApplication();
                GovMessagesReply reply = concurMobile.getMsgs();
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                if (reply != null) {
                    dlgBldr.setTitle(reply.privacyTitle);
                    dlgBldr.setMessage(reply.privacyText);
                } else {
                    dlgBldr.setTitle(R.string.gov_privacy_act_notice_title);
                    dlgBldr.setMessage("");
                }
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            default: {
                dlg = ((GovAppMobile) getApplication()).createDialog(this, id);
                dialogs.put(id, dlg);
            }
            }
        }
        return dlg;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_GOV_MSGS_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (errorMsg != null) {
                alertDlg.setMessage(errorMsg);
            }
            break;
        }

        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.loginButton: {
            // Grab values
            String loginId = loginView.getText().toString();
            String pin = pinView.getText().toString();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            // we need to show privacy act notice rules.
            app.setShowPrivacyActNotice(true);

            if (GovAppMobile.isConnected()) {
                showDialog(Const.DIALOG_LOGIN_WAIT);

                // We always save these bits in order to make sure offline works. The preference only
                // controls if we show the login value on the login screen.
                Preferences.saveLogin(prefs, loginId);
                Preferences.savePin(prefs, pin);

                String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

                // Attempt to authenticate
                // Re-entry to the UI will be via handleMessage() below
                GovAppMobile app = (GovAppMobile) getApplication();
                if (loginResponseHandler == null) {
                    loginResponseHandler = new LoginResponseHandler(this);
                }
                // Ensure any previous message is cleared out.
                loginResponseHandler.clearMessage();
                LoginRequest doLogin = new LoginRequest(app, new Handler(loginResponseHandler), app.getProduct(),
                    serverAdd, loginId, pin);
                doLogin.start();
            } else {
                // Offline login
                String savedLoginId = Preferences.getLogin(prefs, null);
                String savedPin = Preferences.getPin(prefs, null);

                if (isgovUser() && loginId.equalsIgnoreCase(savedLoginId) && pin.equals(savedPin)) {
                    // Good login.
                    // Since this is offline, clear out some stuff to signal to lower levels
                    // that this was an offline login.
                    Preferences.clearSession(prefs, true);

                    // Go to the home screen

                    startHomeScreen();

                    // And don't come back here
                    finish();

                } else {
                    // Tell them no
                    Dialog dlg = createLoginFailureDialog(getText(R.string.login_unathorized).toString(),
                        getText(R.string.login_failure_help).toString());
                    dlg.show();

                    // Clear the login and pin fields and focus on login
                    loginView.setText("");
                    pinView.setText("");
                    loginView.requestFocus();

                }
            }
            break;
        }
        case R.id.warning_more: {
            showDialog(DIALOG_WARNING_MORE);
            break;
        }
        case R.id.privacy_more: {
            showDialog(DIALOG_PAN_MORE);
            break;
        }
        case R.id.companySignInText: {
            Intent i = new Intent(Login.this, CompanyLogin.class);
            startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
        }
        }
    }

    private void startHomeScreen() {

        // Prior to the starting the home screen, initialize the system/user configuration
        // information.
        ((GovAppMobile) getApplication()).initSystemConfig();
        ((GovAppMobile) getApplication()).initUserConfig();
        // clear cache
        ((GovAppMobile) getApplication()).clearCaches();
        Intent i = null;
        i = new Intent(this, Home.class);
        startActivity(i);
    }

    /**
     * Whether the currently logged in end-user is a gov user.
     * 
     * @return whether the currently logged in end-user is a gov user.
     */
    private boolean isgovUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean returnVal = prefs.getBoolean(Const.PREF_CAN_GOV_USER, false);
        return returnVal;
    }

    protected Dialog createLoginFailureDialog(String title, String msg) {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setTitle(title);
        dlgBldr.setMessage(msg);
        dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dlgBldr.create();
    }

    /**
     * An extension of <code>AuthenticationResponseHandler</code> for handling pin-based
     * authentications.
     * 
     * @author AndrewK
     */
    private static class LoginResponseHandler extends AuthenticationResponseHandler {

        /**
         * Constructs an instance of <code>LoginResponseHandler</code> with
         * a current login object.
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
         * @see com.concur.mobile.util.net.AuthenticationResponseHandler#handleFailure(java.util.Map)
         */
        @Override
        protected void handleFailure(Map<String, Object> responses) {
            Login login = (Login) activity;
            if (login != null) {
                boolean wiped = responses.containsKey(Const.LR_WIPED);
                if (wiped) {
                    // Let 'em know.
                    Dialog dlg = login.createLoginFailureDialog(login.getText(R.string.login_remote_wipe_title)
                        .toString(), login.getText(R.string.login_remote_wipe_msg).toString());
                    dlg.show();

                    // Clear both fields
                    login.pinView.setText("");
                    login.loginView.setText("");
                    login.loginView.requestFocus();
                    // Clear out the web view cache.
                    login.loginView.post(new Runnable() {

                        @Override
                        public void run() {
                            ViewUtil.clearWebViewCache(activity);
                        }
                    });

                } else {
                    String status = (String) responses.get(Const.LR_STATUS);

                    if (status != null && status.equalsIgnoreCase("SSO login required")) {
                        login.showDialog(Const.DIALOG_COMPANY_SIGNON_REQUIRED);
                    } else {
                        // Fail the login if no session
                        String title = status;
                        if (title == null) {
                            title = login.getText(R.string.login_failure).toString();
                        }
                        String message = login.getText(R.string.login_failure_help).toString();
                        if (title.equalsIgnoreCase("Pin Expired")) {
                            title = login.getText(R.string.login_pin_expired_title).toString();
                        }

                        Dialog dlg = login.createLoginFailureDialog(title, message);
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
         * @see com.concur.mobile.util.net.AuthenticationResponseHandler#handleSuccess(java.util.Map)
         */
        @Override
        protected void handleSuccess(Map<String, Object> responses) {
            String sessionId = (String) responses.get(Const.LR_SESSION_ID);

            Login login = (Login) activity;
            // Flurry 'SignIn' Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_PIN_OR_PASSWORD);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_AUTHENTICATION,
                params);
            // Save the login response information.
            ConcurCore.saveLoginResponsePreferences(sessionId, (GovAppMobile) login.getApplication(), responses);

            if (login.isgovUser()) {
                // Go to homescreen ...
                login.startHomeScreen();
                login.dismissDialog(Const.DIALOG_LOGIN_WAIT);
                String paramValue = null;
                // And don't come back here
                paramValue = Flurry.PARAM_VALUE_GOV;
                if (paramValue != null) {
                    params.put(Flurry.PARAM_NAME_TYPE, paramValue);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_USER, Flurry.EVENT_NAME_TYPE, params);
                }
                login.finish();
            } else {
                login.dismissDialog(Const.DIALOG_LOGIN_WAIT);
                // Tell them no
                Dialog dlg = login.createLoginFailureDialog(login.getText(R.string.login_unathorized).toString(),
                    login.getText(R.string.login_failure_help).toString());
                dlg.show();
                // Clear the login and pin fields and focus on login
                login.loginView.setText("");
                login.pinView.setText("");
                login.loginView.requestFocus();

            }
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling broadcasted changes
     * in data connectivity.
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

    //
    private void getPrivacyActNotice() {
        GovAppMobile concurMobile = (GovAppMobile) getApplication();
        final GovService service = concurMobile.getService();
        // check privacy act notice is in database.
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... params) {
                MobileDatabase db = service.getMobileDatabase();
                Cursor cur = db.loadGovWarningMsgs();
                return cur;
            }

            @Override
            protected void onPostExecute(Cursor cur) {
                if (cur.getCount() > 0) {
                    if (cur.moveToFirst()) {
                        GovMessagesReply reply = new GovMessagesReply(cur);
                        // store to application level.
                        GovAppMobile concurMobile = (GovAppMobile) getApplication();
                        concurMobile.setMsgs(reply);
                        if (GovAppMobile.isConnected()) {
                            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            Calendar lastRefresh = reply.lastRefreshTime;
                            int diff = FormatUtil.getMonthsDifference(lastRefresh, now);
                            if (diff == -1 || diff > 6) {
                                sendPrivacyActNoticeRequest();
                            } else {
                                onHandleDocDetailSuccess(reply);
                            }
                        } else {
                            onHandleDocDetailSuccess(reply);
                        }
                    } else {
                        Log.e(CLS_TAG, " .getPrivacyActNotice : cursor is not empty but cursor.movetofirst is false");
                    }
                } else {
                    sendPrivacyActNoticeRequest();
                }
            }
        }.execute();

    }

    private void sendPrivacyActNoticeRequest() {
        if (GovAppMobile.isConnected()) {
            GovAppMobile concurMobile = (GovAppMobile) getApplication();
            GovService govService = concurMobile.getService();
            if (govService != null) {
                registerMsgReceiver();
                request = govService.sendPrivacyActNoticeRequest();
                if (request == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendPrivacyActNoticeRequest: unable to create request to get Govt. warning messages!");
                    unregisterMsgReceiver();
                } else {
                    // set service request.
                    msgReceiver.setServiceRequest(request);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_MSGS_INPROGRESS);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        }
    }

    /**
     * register document detail receiver
     * */
    protected void registerMsgReceiver() {
        if (msgReceiver == null) {
            msgReceiver = new GovMsgReciever(this);
            if (msgsFilter == null) {
                msgsFilter = new IntentFilter(
                    com.concur.mobile.gov.util.Const.ACTION_GOV_MSGS);
            }
            getApplicationContext().registerReceiver(msgReceiver, msgsFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerMsgReceiver is not null");
        }
    }

    /**
     * unregister document detail receiver
     * */
    protected void unregisterMsgReceiver() {
        if (msgReceiver != null) {
            getApplicationContext().unregisterReceiver(msgReceiver);
            msgReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterMsgReceiver is null!");
        }
    }

    /**
     * An extension of {@link GeneralBaseReceiver} for the purposes of handling
     * the response for document detail.
     */
    class GovMsgReciever extends GeneralBaseReceiver<Login, GovMessagesRequest>
    {

        private final String CLS_TAG = Login.CLS_TAG + "."
            + GovMsgReciever.class.getSimpleName();

        protected GovMsgReciever(Login activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(Login activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_MSGS_INPROGRESS);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
            activity.errorMsg = actionStatusErrorMessage;
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_MSGS_FAIL);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovMessagesReply reply = app.getMsgs();
            if (reply != null) {
                onHandleDocDetailSuccess(reply);
            } else {
                handleFailure(context, intent);
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(GovMessagesRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterMsgReceiver();
        }
    }

    private void onHandleDocDetailSuccess(GovMessagesReply reply) {
        TextView warningTitle = (TextView) findViewById(R.id.warningTitle);
        TextView warningShortMsg = (TextView) findViewById(R.id.warningmsg);
        TextView warningMore = (TextView) findViewById(R.id.warning_more);
        TextView panTitle = (TextView) findViewById(R.id.privacyANTitle);
        TextView panShortMsg = (TextView) findViewById(R.id.privacyANMsg);
        TextView panMore = (TextView) findViewById(R.id.privacy_more);

        if (reply == null) {
            warningTitle.setText(getString(R.string.gov_login_warining_title).toString());
            warningMore.setVisibility(View.GONE);
            warningShortMsg.setText("");
            panTitle.setText(getString(R.string.gov_privacy_act_notice_title).toString());
            panMore.setVisibility(View.GONE);
            panShortMsg.setText("");
        } else {
            warningTitle.setText(reply.warningTitle);
            warningMore.setVisibility(View.VISIBLE);
            warningMore.setOnClickListener(this);
            warningShortMsg.setText(reply.warningTextShort);
            panTitle.setText(reply.privacyTitle);
            panMore.setVisibility(View.VISIBLE);
            panMore.setOnClickListener(this);
            panShortMsg.setText(reply.privacyTextShort);
        }

    }
}
