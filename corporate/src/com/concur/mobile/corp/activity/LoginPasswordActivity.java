package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.EditText;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.AutoLoginRequestTask;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.LoginResponseKeys;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment.OnClickListener;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment.OnCancelListener;
import com.concur.mobile.platform.ui.common.login.LoginPasswordFragment;
import com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks;
import com.concur.mobile.platform.ui.common.util.ViewUtil;
import com.concur.platform.PlatformProperties;

import org.apache.http.HttpStatus;

import java.util.Locale;

@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_LOGIN_PASSWORD)
public class LoginPasswordActivity extends BaseActivity implements LoginPasswordCallbacks {

    public final static String TAG_LOGIN_WAIT_DIALOG = "tag.login.wait.dialog";

    protected final static String TAG_LOGIN_REMOTE_WIPE_DIALOG = "tag.login.remote.wipe.dialog";

    private boolean fromNotification;

    private BaseAsyncResultReceiver autoLoginReceiver;

    private LoginPasswordFragment loginPasswordFragment;

    private AutoLoginRequestTask autoLoginRequestTask;

    private ProgressDialogFragment progressDialog;

    private OnCancelListener loginPasswordFragCancelListener;

    private String userId = "";
    private String pinOrPassword = "";
    private String signInMethod = "";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_drive_main);

        // set title
        getSupportActionBar().setTitle(R.string.login_title);

        // push notification
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(ConcurMobile.FROM_NOTIFICATION)) {
            fromNotification = getIntent().getExtras().getBoolean(ConcurMobile.FROM_NOTIFICATION);
        }

        FragmentManager fm = getSupportFragmentManager();
        loginPasswordFragment = (LoginPasswordFragment) fm.findFragmentByTag(LoginPasswordFragment.CLS_TAG);
        if (loginPasswordFragment == null) {
            loginPasswordFragment = new LoginPasswordFragment();

            // Be sure to show the Login Help button and pass in the Login results.
            Bundle args = new Bundle();

            // Check if we have the login bundle.
            if (getIntent().getExtras().containsKey(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE)) {
                args.putBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE,
                        getIntent().getExtras().getBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE));
            }

            args.putBoolean(LoginPasswordFragment.ARGS_SHOW_LOGIN_HELP, true);
            loginPasswordFragment.setArguments(args);

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, loginPasswordFragment, LoginPasswordFragment.CLS_TAG);
            ft.commit();
        }

        // Initialize the progress dialog.
        progressDialog = DialogFragmentFactory.getProgressDialog(getText(R.string.dlg_logging_in).toString(), true,
                true, null);

        autoLoginReceiver = new BaseAsyncResultReceiver(new Handler());
        autoLoginReceiver.setListener(new AsyncReplyListener() {

            /*
             * (non-Javadoc)
             * 
             * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestSuccess(android.os.Bundle)
             */
            public void onRequestSuccess(Bundle resultData) {

                if (loginPasswordFragment.isDetached()) {
                    return;
                }

                saveCredentials(userId, pinOrPassword, signInMethod);

                Bundle extras = getIntent().getExtras();
                Bundle emailLookupBundle = null;
                if (extras != null && extras.containsKey(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE)) {
                    emailLookupBundle = extras.getBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE);
                }
                UserAndSessionInfoUtil.updateUserAndSessionInfo(ConcurCore.getContext(), emailLookupBundle);

                // GA analytics

                trackLoginStatus(true, signInMethod);

                // remove autologin if SSO user
                if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)) {
                    Preferences.isHideAutoLogin(true);
                } else {
                    Preferences.isHideAutoLogin(false);
                }

                // close dialog
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

                // Go to homescreen ...
                startHomeScreen(emailLookupBundle);

                // Set this back to 0 so we don't record this attempt
                // if the user goes back to try and register. We only
                // want to record if the user failed to sign in.
                Preferences.setTestDriveSigninTryAgainCount(0);
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestFail(android.os.Bundle)
             */
            public void onRequestFail(Bundle resultData) {

                // close dialog
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

                // MOB-22674 - Show error dialog on failure.
                DialogFragmentFactory.getAlertOkayInstance(getString(R.string.dlg_system_unavailable_title),
                        getString(R.string.dlg_system_unavailable_message)).show(
                        LoginPasswordActivity.this.getSupportFragmentManager(), null);
                trackLoginStatus(false, signInMethod);
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestCancel(android.os.Bundle)
             */
            public void onRequestCancel(Bundle resultData) {
            }

            /*
             * (non-Javadoc)
             * 
             * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#cleanup()
             */
            public void cleanup() {
            }

        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.BaseActivity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        EventTracker.INSTANCE.activityStart(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.BaseActivity#onStop()
     */
    @Override
    public void onStop() {
        super.onStop();
        EventTracker.INSTANCE.activityStop(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && (getIntent().getBooleanExtra(com.concur.mobile.core.util.Const.EXTRA_LOGIN_LAUNCHED_FROM_PRE_LOGIN,
                false) || getIntent().getBooleanExtra(
                com.concur.mobile.core.util.Const.EXTRA_LOGIN_LAUNCHED_FROM_TEST_DRIVE_REGISTRATION, false))) {

            //EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_BACK_BUTTON_CLICK);
        }
        return super.onKeyDown(keyCode, event);
    }

    // ############### LoginPasswordCallbacks implementations ############# //

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#onLoginRequestSuccess(android.os
     * .Bundle)
     */
    public void onLoginRequestSuccess(Bundle resultData) {

        if (loginPasswordFragment.isDetached()) {
            return;
        }

        // Record the number of times the user tries to sign in.
        Preferences.incrementTestDriveSigninTryAgainCount();

        if (resultData != null && resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY)) {

            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            remoteWipe();

        } else {

            // GLS Server issue.
            if (resultData != null) {
                String serverUrl = resultData.getString(LoginResponseKeys.SERVER_URL_KEY);
                if (serverUrl != null && serverUrl.trim().length() > 0) {
                    UserAndSessionInfoUtil.setServerAddress(serverUrl);
                } else {
                    UserAndSessionInfoUtil.setServerAddress(PlatformProperties.getServerAddress());
                }
            }

            // Authentication was successful. Now perform an full AutoLoginRequest in order to get
            // all the user roles, site settings, car configs, etc. and all that other good stuff.
            autoLoginRequestTask = new AutoLoginRequestTask(ConcurCore.getContext(), 0, autoLoginReceiver,
                    Locale.getDefault());
            autoLoginRequestTask.execute();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#onLoginRequestFail(android.os.Bundle
     * )
     */
    public void onLoginRequestFail(Bundle resultData) {
        if (loginPasswordFragment.isDetached()) {
            return;
        }

        // Record the number of times the user tries to sign in.
        Preferences.incrementTestDriveSigninTryAgainCount();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (resultData != null && resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY)) {
            remoteWipe();
        } else {
            // Check the HTTP response for tracking
            Integer httpStatus = (Integer) resultData.get(BaseAsyncRequestTask.HTTP_STATUS_CODE);
            if (httpStatus != null && httpStatus == HttpStatus.SC_FORBIDDEN) {
                StringBuilder title = new StringBuilder(getText(R.string.login_403_error_title));
                StringBuilder message = new StringBuilder(getText(R.string.login_403_error_message));
                loginPasswordFragment.show403PasswordError(title, message);
                trackLoginStatus(false, Flurry.LABEL_FORBIDDEN);
            } else if (httpStatus != null && httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                StringBuilder title = new StringBuilder(getText(R.string.login_500_error_title));
                StringBuilder message = new StringBuilder(getText(R.string.login_500_error_message));
                loginPasswordFragment.show500PasswordError(title, message);
                trackLoginStatus(false, Flurry.LABEL_SERVER_ERROR);
            } else if (httpStatus != null && httpStatus == HttpStatus.SC_UNAUTHORIZED) {
                loginPasswordFragment.showInvalidPasswordError(new StringBuilder(
                        getText(R.string.login_password_or_pin_invalid)));
                trackLoginStatus(false, Flurry.LABEL_SERVER_ERROR);
            } else {
                loginPasswordFragment.showInvalidPasswordError(new StringBuilder(
                        getText(R.string.login_password_or_pin_invalid)));
                trackLoginStatus(false, Flurry.LABEL_BAD_CREDENTIALS);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#isNetworkConnected()
     */
    public boolean isNetworkConnected() {
        return ConcurCore.isConnected();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#onLoginHelpButtonPressed(java.lang
     * .String)
     */
    public void onLoginHelpButtonPressed(String signInMethod) {
        Intent i = new Intent(this, LoginHelp.class);
        i.putExtra(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, signInMethod);
        startActivity(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#hasSavedCredentials()
     */
    public boolean hasSavedCredentials() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String savedLoginId = Preferences.getLogin(prefs, null);
        String savedPin = Preferences.getPin(prefs, null);

        return (savedLoginId != null && savedPin != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#trackLoginFailure(java.lang.String)
     */
    public void trackLoginStatus(boolean success, String method) {
        if (success) {
            trackLoginSuccess(method);
        } else {

            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SIGN_IN_FAIL_METHOD,
                    Flurry.LABEL_MANUAL, null);

            if ((com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO).equalsIgnoreCase(signInMethod)) {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_CREDENTIAL_TYPE,
                        Flurry.LABEL_LOGIN_USING_SSO, null);
            } else if ((com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD).equalsIgnoreCase(signInMethod)) {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_CREDENTIAL_TYPE,
                        Flurry.LABEL_LOGIN_USING_MOBILE_PASSWORD, null);
            } else {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_CREDENTIAL_TYPE,
                        Flurry.LABEL_LOGIN_USING_PASSWORD, null);
            }

            if (method.equalsIgnoreCase(Flurry.LABEL_REMOTE_WIPE)) {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                        Flurry.LABEL_REMOTE_WIPE, null);
            } else if (method.equalsIgnoreCase(Flurry.LABEL_FORBIDDEN)) {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                        Flurry.LABEL_FORBIDDEN, null);
            } else if (method.equalsIgnoreCase(Flurry.LABEL_BAD_CREDENTIALS)) {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                        Flurry.LABEL_BAD_CREDENTIALS, null);
            } else {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                        Flurry.LABEL_SERVER_ERROR, null);
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#onSignInButtonClicked(java.lang
     * .String, java.lang.String, java.lang.String)
     */
    public void onSignInButtonClicked(String userId, String pinOrPassword, String signInMethod) {

        this.userId = userId;
        this.pinOrPassword = pinOrPassword;
        this.signInMethod = signInMethod;

        if (progressDialog != null && !progressDialog.isVisible()) {

            progressDialog.setCancelListener(new OnCancelListener() {

                public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                    if (loginPasswordFragCancelListener != null) {
                        loginPasswordFragCancelListener.onCancel(activity, dialog);
                    }

                    if (autoLoginRequestTask != null) {
                        autoLoginRequestTask.cancel(true);
                    }
                }
            });

            progressDialog.show(getSupportFragmentManager(), TAG_LOGIN_WAIT_DIALOG);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#setProgressDialogCancelListener
     * (com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment.OnCancelListener)
     */
    public void setProgressDialogCancelListener(final OnCancelListener cancelListener) {
        this.loginPasswordFragCancelListener = cancelListener;
    }

    // ############### end LoginPasswordCallbacks implementations ############# //

    // ################## HELPER METHODS ##################### //

    private void remoteWipe() {

        // Notify the user about the "Remote Wipe".
        AlertDialogFragment adf = DialogFragmentFactory.getAlertOkayInstance(R.string.account_locked,
                R.string.account_locked_message);
        adf.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                EditText pinView = (EditText) activity.findViewById(R.id.loginPin);
                if (pinView != null) {
                    pinView.setText("");
                    // Show the keyboard
                    ViewUtil.showSoftKeyboard(activity, activity.getResources().getConfiguration());
                    pinView.requestFocus();
                }
            }
        });
        adf.show(getSupportFragmentManager(), TAG_LOGIN_REMOTE_WIPE_DIALOG);

        // NOTE: Calling clearContent() literally deletes the whole DB file,
        // so there is no need to delete anything else except Properties stuff.
        // ConfigUtil.removeLoginInfo(activity);
        // ConfigUtil.remoteWipe(activity);
        ConfigUtil.clearContent(this);
        // clear expense content provider
        ExpenseUtil.clearContent(this);
        // Clear Platform Properties.
        PlatformProperties.setAccessToken(null);
        PlatformProperties.setSessionId(null);

        // Clear any A/B Test information.
        Preferences.clearABTestInfo(PreferenceManager.getDefaultSharedPreferences(this));

        // Clear out the Web View cache and cookies.
        com.concur.mobile.core.util.ViewUtil.clearWebViewCookies(this);

        trackLoginStatus(false, Flurry.LABEL_REMOTE_WIPE);
    }

    private void startHomeScreen(Bundle emailLookup) {
        if (RolesUtil.isGovUser(this)) {
            DialogFragmentFactory.getPositiveDialogFragment(getText(R.string.login_failure).toString(),
                    getText(R.string.login_unathorized).toString(), getText(R.string.okay).toString(),
                    new OnClickListener() {

                        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }

                        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }).show(getSupportFragmentManager(), null);

        } else {
            ((ConcurMobile) this.getApplication()).initSystemConfig();
            ((ConcurMobile) this.getApplication()).initUserConfig();
            this.setResult(Activity.RESULT_OK);
            if (!fromNotification) {
                gotoHome(emailLookup);
            }
        }
    }

    private void gotoHome(Bundle emailLookup) {
        Intent i=new Intent(this, Home.class);
        logUserTimings(emailLookup);
        startActivity(i);
        this.setResult(Activity.RESULT_OK);
        this.finish();
    }

    private void saveCredentials(String loginId, String pinOrPassword, String signInMethod) {

        // Don't want to throw any null pointer exceptions!
        if (loginId == null || pinOrPassword == null || signInMethod == null) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // We always save these bits in order to make sure offline
        // works. The preference only
        // controls if we show the login value on the login screen.
        Preferences.saveLogin(prefs, loginId);
        // If the user logged in using password,
        // we should *not* save it! Only save PINs.
        if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD)) {
            Preferences.savePin(prefs, pinOrPassword);
        }
    }

    private static void trackLoginSuccess(String signInMethod) {
        EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SIGN_IN_SUCCESS_METHOD,
                Flurry.LABEL_MANUAL, null);
        if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)) {
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SUCCESS_CREDENTIAL_TYPE,
                    Flurry.LABEL_LOGIN_USING_SSO, null);
        } else if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD)) {
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SUCCESS_CREDENTIAL_TYPE,
                    Flurry.LABEL_LOGIN_USING_MOBILE_PASSWORD, null);
        } else {
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SUCCESS_CREDENTIAL_TYPE,
                    Flurry.LABEL_LOGIN_USING_PASSWORD, null);
        }
    }

    private void logUserTimings(Bundle emailLookup){
        if (ConcurCore.userEntryAppTimer > 0 && emailLookup != null) {
            ConcurCore.userSuccessfulLoginTimer = System.currentTimeMillis();
            long totalWaitTime = ConcurCore.userSuccessfulLoginTimer - ConcurCore.userEntryAppTimer;
            String signInMethod = emailLookup.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
            // Log to Google Analytics
            if (totalWaitTime <= 0) {
                totalWaitTime = 0;
            }

            if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)) {
                signInMethod=Flurry.LABEL_LOGIN_USING_SSO;
            } else if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD)) {
                signInMethod=Flurry.LABEL_LOGIN_USING_MOBILE_PASSWORD;
            } else {
                signInMethod=Flurry.LABEL_LOGIN_USING_PASSWORD;
            }

            EventTracker.INSTANCE.trackTimings(Flurry.CATEGORY_SIGN_IN, totalWaitTime,signInMethod, null);
            ConcurCore.resetUserTimers();
        }
    }

}