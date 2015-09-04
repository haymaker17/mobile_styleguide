package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.AutoLoginRequestTask;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.LoginResponseKeys;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.ui.common.IProgressBarListener;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;
import com.concur.mobile.platform.ui.common.login.EmailLookupFragment;
import com.concur.mobile.platform.ui.common.login.EmailPasswordLookupFragment;
import com.concur.mobile.platform.ui.common.login.NewLoginPasswordFragment;
import com.concur.mobile.platform.ui.common.util.ViewUtil;
import com.concur.platform.PlatformProperties;

import org.apache.http.HttpStatus;

import java.util.Locale;

@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_EMAIL_PASSWORD)
public class EmailPasswordLookupActivity extends BaseActivity implements IProgressBarListener, EmailPasswordLookupFragment.EmailLookupCallbacks, NewLoginPasswordFragment.LoginPasswordCallbacks {

    /**
     * Extra flag used to indicate if we should automatically advanced to the company sigin on screen.
     */
    public static final String EXTRA_ADVANCE_TO_COMPANY_SIGN_ON = "advance_to_company_sign_on";

    private static final String LOG_TAG = EmailPasswordLookupActivity.class.getSimpleName();

    private static final int LOGIN_PASSWORD_REQ_CODE = 1;

    private static final int LOGIN_SSO_REQ_CODE = 2;

    public static final int TEST_DRIVE_REQ_CODE = 3;

    public static int noOfLoginAttempts = 0;

    private static final String FRAGMENT_EMAIL_LOOKUP = "FRAGMENT_EMAIL_LOOKUP";
    private static final String FRAGMENT_LOGIN_PASSWORD = "FRAGMENT_LOGIN_PASSWORD";

    private static final String PROGRESSBAR_VISIBLE = "PROGRESSBAR_VISIBLE";
    private static final String PROGRESSBAR_MESSAGE = "PROGRESSBAR_MESSAGE";

    public final static String TAG_LOGIN_WAIT_DIALOG = "tag.login.wait.dialog";
    protected final static String TAG_LOGIN_REMOTE_WIPE_DIALOG = "tag.login.remote.wipe.dialog";

    protected String emailText;

    private boolean fromNotification;

    private boolean progressbarVisible;
    private String progressDlgMessage;
    private ProgressDialogFragment progressDialog;

    //Two finger duble tap Initialisation.
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;

    private long mFirstDownTime = 0;

    private boolean mSeparateTouches = false;

    private byte mTwoFingerTapCount = 0;

    private NewLoginPasswordFragment loginPasswordFragment;

    private BaseAsyncResultReceiver autoLoginReceiver;

    private AutoLoginRequestTask autoLoginRequestTask;

    private ProgressDialogFragment.OnCancelListener loginPasswordFragCancelListener;

    private String userId = "";
    private String pinOrPassword = "";
    private String signInMethod = "";

    private Bundle emailLookupBundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_password_main);

        // push notification
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(ConcurMobile.FROM_NOTIFICATION)) {
            fromNotification = getIntent().getExtras().getBoolean(ConcurMobile.FROM_NOTIFICATION);
        }

        //reset expiration because if after upgrade user sees this page it means he will login trough normal process.
        Home.forceExpirationHome=false;

        if (ConcurCore.userEntryAppTimer > 0L) {
            ConcurCore.userEntryAppTimer += System.currentTimeMillis();
        } else {
            ConcurCore.userEntryAppTimer = System.currentTimeMillis();
        }
        // Enable the progress mask if needed
        if (savedInstanceState != null) {
            progressbarVisible = savedInstanceState.getBoolean(PROGRESSBAR_VISIBLE, false);
            if (progressbarVisible) {
                showProgressBar();
            }
        }

        ImageView img = (ImageView) findViewById(R.id.helpimg);
        img.setVisibility(View.GONE);

        TextView text = (TextView) findViewById(R.id.helpmsg);
        text.setText(getString(R.string.test_drive_registration));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventTracker.INSTANCE.track(Flurry.CATEGORY_START_UP, "Test Drive Click");
                Intent it = new Intent(EmailPasswordLookupActivity.this, TestDriveRegistration.class);
                startActivityForResult(it, TEST_DRIVE_REQ_CODE);
            }
        });

        // Determine whether this activity should immediately forward to the Company SignIn activity.
        boolean advanceToCompanyLogon = getIntent().getBooleanExtra(EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, false);
        // MOB-17060 If just orientation change, do not auto-direct to SSO
        if (!orientationChange && advanceToCompanyLogon) {
            String companyCode = getIntent().getStringExtra(Const.EXTRA_SSO_COMPANY_CODE);
            Intent i = null;
            Context context = getConcurCore();
            SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
            if (sessionInfo != null && (!TextUtils.isEmpty(sessionInfo.getSSOUrl()))) {
                i = new Intent(this, CompanySignOnActivity.class);
                i.putExtra(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE, getEmailLookUpBundleFromSessionInfo(sessionInfo));
            } else {
                i = new Intent(this, CompanyCodeLoginActivity.class);
                i.putExtra(EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
                if (companyCode != null && companyCode.length() > 0) {
                    i.putExtra(Const.EXTRA_SSO_COMPANY_CODE, companyCode);
                }
            }
            startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
        }

        // See if we're to preload the email address.
        emailText = getIntent().getStringExtra(EmailPasswordLookupFragment.ARGS_EMAIL_TEXT_VALUE);

        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        PlatformFragment emailLookupFragment = (PlatformFragment) fm.findFragmentByTag(FRAGMENT_EMAIL_LOOKUP);
        if (emailLookupFragment == null) {
            emailLookupFragment = new EmailPasswordLookupFragment();

            // If we're supposed to load email lookup with text, push that to the fragment.
            // But first check if it's empty, and if it is and autologin is enabled, get it from the Prefs.
            if (TextUtils.isEmpty(emailText)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (prefs.getBoolean(Const.PREF_SAVE_LOGIN, false)) {
                    // Restore login ID : MOB-18534
                    emailText = Preferences.getLogin(prefs, "");
                }
            }

            bundle.putString(EmailPasswordLookupFragment.ARGS_EMAIL_TEXT_VALUE, emailText);
            emailLookupFragment.setArguments(bundle);

            ((EmailPasswordLookupFragment) emailLookupFragment).setProgressBarListener(this);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, emailLookupFragment, FRAGMENT_EMAIL_LOOKUP);
            ft.commit();
        } else {
            ((EmailPasswordLookupFragment) emailLookupFragment).setProgressBarListener(this);
        }

        setAutoRequestReceiver();
    }

    public void showProgressBar() {
        if (progressDlgMessage == null || progressDlgMessage.isEmpty()) {
            progressDlgMessage = getString(R.string.dlg_logging_in).toString();
        }
        if (progressDialog == null) {
            // Initialize the progress dialog.
            progressDialog = DialogFragmentFactory.getProgressDialog(progressDlgMessage, true,
                    true, null);
        }

        if (progressDialog != null && !progressDialog.isVisible()) {

            progressDialog.setMessage(progressDlgMessage);
            progressDialog.setCancelListener(new ProgressDialogFragment.OnCancelListener() {

                public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                    if (loginPasswordFragCancelListener != null) {
                        loginPasswordFragCancelListener.onCancel(activity, dialog);
                    }

                    if (autoLoginRequestTask != null) {
                        autoLoginRequestTask.cancel(true);
                    }
                }
            });
        }
        progressDialog.show(getSupportFragmentManager(), TAG_LOGIN_WAIT_DIALOG);
        progressbarVisible = true;

    }
    public void hideProgressBar() {
        //View v = findViewById(R.id.progress_mask);
        //RelativeLayout progressBar = (RelativeLayout) v;
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressbarVisible = false;
        //progressBar.setVisibility(View.INVISIBLE);
    }

    public boolean isProgressBarShown() {
        return progressbarVisible;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESSBAR_VISIBLE, progressbarVisible);
        outState.putString(PROGRESSBAR_MESSAGE, progressDlgMessage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOGIN_PASSWORD_REQ_CODE) {
                setResult(Activity.RESULT_OK);
                finish();
            } else if (requestCode == LOGIN_SSO_REQ_CODE) {
                setResult(Activity.RESULT_OK);
                finish();
            } else if (requestCode == Const.REQUEST_CODE_SSO_LOGIN) {
                setResult(Activity.RESULT_OK);
                finish();
            }
            if (requestCode == TEST_DRIVE_REQ_CODE
                    && data != null) {
                ConcurCore.resetUserTimers();

            } else if (requestCode == TEST_DRIVE_REQ_CODE) {

                finish();
            }
        } else {
            if(loginPasswordFragment==null || loginPasswordFragment.isDetached()){
                ConcurCore.resetUserTimers();
                ConcurCore.userEntryAppTimer = System.currentTimeMillis();
            }
        }

    }


    // ############### EmailLookupCallbacks implementations ############# //

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.EmailLookupFragment.EmailLookupCallbacks#onEmailLookupRequestSuccess(android
     * .os.Bundle)
     */
    public void onEmailLookupRequestSuccess(Bundle resultData) {
        // Get the server url.
        String serverUrl = resultData.getString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY);
        // Get the sign-in method. {Values: Password/MobilePassword/SSO}
        String signInMethod = resultData.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
        // Get the sso url.
        String ssoUrl = resultData.getString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY);
        // Get the email id used in email lookup.
        String email = resultData.getString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY);

        if (serverUrl != null) {
            UserAndSessionInfoUtil.setServerAddress(serverUrl);
        } else {
            UserAndSessionInfoUtil.setServerAddress(PlatformProperties.getServerAddress());
        }

        emailLookupBundle= resultData;
        if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD)
                || (signInMethod
                .equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD))) {
            // GA
            if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD)) {// GA
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SUCCESS_CREDENTIAL_TYPE,
                        Flurry.LABEL_LOGIN_USING_PASSWORD, null);
            } else {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SUCCESS_CREDENTIAL_TYPE,
                        Flurry.LABEL_LOGIN_USING_PASSWORD, null);
            }
            startLoginFragment(resultData);
        } else if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)
                && !TextUtils.isEmpty(ssoUrl)) {
            // GA
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_SUCCESS_CREDENTIAL_TYPE,
                    Flurry.LABEL_LOGIN_USING_SSO, null);
            // Launch the company sign-on activity.
            Intent it = new Intent(this, CompanySignOnActivity.class);
            it.putExtra(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE, emailLookupBundle);
            startActivityForResult(it, LOGIN_SSO_REQ_CODE);
        } else {
            // TODO error.
        }

    }

    @Override
    public void onEmailLookupRequestFail(Bundle resultData) {
        trackEmailLookupFailure(Flurry.EVENT_OTHER_ERROR);
        // resultData contains the sign-in method. {Values: Password/MobilePassword/SSO} , and email id used in email lookup.
        startLoginFragment(resultData);
    }

    @Override
    public void openSettings() {
        Intent i = new Intent(this, Preferences.class);
        i.putExtra(Preferences.OPEN_SOURCE_LIBRARY_CLASS, OpenSourceLicenseInfo.class);
        startActivity(i);
    }

    @Override
    public void onLoginHelpButtonPressed(String signInMethod) {
        Intent i = new Intent(this, LoginHelp.class);
        i.putExtra(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, signInMethod);
        startActivityForResult(i, LOGIN_PASSWORD_REQ_CODE);
    }


    private void startLoginFragment(Bundle emailLookupBundle) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        loginPasswordFragment = (NewLoginPasswordFragment) fm.findFragmentByTag(FRAGMENT_LOGIN_PASSWORD);
        if (loginPasswordFragment == null) {
            loginPasswordFragment = new NewLoginPasswordFragment();
        }
        Bundle args = new Bundle();
        args.putBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE, emailLookupBundle);
        loginPasswordFragment.setArguments(args);
        ft.replace(R.id.container, loginPasswordFragment, FRAGMENT_LOGIN_PASSWORD);
        ft.addToBackStack(FRAGMENT_LOGIN_PASSWORD);
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        ft.commit();

    }

    /*
         * (non-Javadoc)
         *
         * @see com.concur.mobile.platform.ui.common.login.EmailLookupFragment.OnCompanyCodePressedListener#onButtonPressed()
         */
    public void onCompanyCodeButtonPressed() {
        Intent i = new Intent(this, CompanyCodeLoginActivity.class);
        startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.ui.common.login.EmailLookupFragment.EmailLookupCallbacks#isNetworkConnected()
     */
    public boolean isNetworkConnected() {
        return ConcurCore.isConnected();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.EmailLookupFragment.EmailLookupCallbacks#trackEmailLookupFrailure(java.lang.
     * String)
     */
    public void trackEmailLookupFailure(String failureType) {
        if (failureType.equalsIgnoreCase(EmailLookupFragment.EmailLookupCallbacks.FAILURE_REASON_FORMAT)) {
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                    Flurry.LABEL_BAD_CREDENTIALS, null);
        } else if (failureType.equalsIgnoreCase(EmailLookupFragment.EmailLookupCallbacks.FAILURE_REASON_OFFLINE)) {
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                    Flurry.LABEL_OFFLINE, null);
        } else {
            EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_REASON,
                    Flurry.LABEL_SERVER_ERROR, null);
        }

    }

    // ############### End of EmailLookupCallbacks implementations ############# //

    // ############### HELPER METHODS ################ //

    private Bundle getEmailLookUpBundleFromSessionInfo(SessionInfo sessionInfo) {
        // Determine whether a company sign-on URL has been cached. If so,
        // take the end-user to the company sign-on screen.
        String signInMethod = null;
        String ssoUrl = null;
        String serverUrl = null;
        String loginId = null;
        Bundle emailLookupBundle = null;
        if (sessionInfo != null) {
            emailLookupBundle = new Bundle();
            signInMethod = sessionInfo.getSignInMethod();
            ssoUrl = sessionInfo.getSSOUrl();
            serverUrl = sessionInfo.getServerUrl();
            loginId = sessionInfo.getLoginId();
            // Set the login id.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY, loginId);
            // Set the server url.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY, (serverUrl != null ? serverUrl
                    : PlatformProperties.getServerAddress()));
            // Set the sign-in method.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, signInMethod);
            // Set the sso url.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY, ssoUrl);
        }
        return emailLookupBundle;
    }

    @Override
    public void setOnTouchListenerForView(View view) {
        ImageView img = (ImageView) view;
        img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_MOVE):
                Log.d(LOG_TAG, "Action was MOVE");
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(LOG_TAG, "Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(LOG_TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            case MotionEvent.ACTION_DOWN:
                if (mFirstDownTime == 0 || event.getEventTime() - mFirstDownTime > TIMEOUT)
                    reset(event.getDownTime());
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2)
                    mTwoFingerTapCount++;
                else
                    mFirstDownTime = 0;
                return true;
            case MotionEvent.ACTION_UP:
                if (!mSeparateTouches)
                    mSeparateTouches = true;
                else if (mTwoFingerTapCount == 2 && event.getEventTime() - mFirstDownTime < TIMEOUT) {
                    onTwoFingerDoubleTap();
                    mFirstDownTime = 0;
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void reset(long time) {
        mFirstDownTime = time;
        mSeparateTouches = false;
        mTwoFingerTapCount = 0;
    }

    public void onTwoFingerDoubleTap() {
        openSettings();
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
        //increase number of attempts
        noOfLoginAttempts++;
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (noOfLoginAttempts > 2) {
            loginPasswordFragment.removeFragment();
            noOfLoginAttempts = 0;
        } else {
            if (loginPasswordFragment.isDetached()) {
                return;
            }

            // Record the number of times the user tries to sign in.
            Preferences.incrementTestDriveSigninTryAgainCount();

//            if (progressDialog != null) {
//                progressDialog.dismiss();
//            }

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
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#onSignInButtonClicked(java.lang
     * .String, java.lang.String, java.lang.String)
     */
    public void onSignInButtonClicked(String userId, String pinOrPassword, String signInMethod) {
        this.userId = userId;
        this.pinOrPassword = pinOrPassword;
        this.signInMethod = signInMethod;

        if (progressDialog != null && !progressDialog.isVisible()) {

            progressDialog.setCancelListener(new ProgressDialogFragment.OnCancelListener() {

                public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                    if (loginPasswordFragCancelListener != null) {
                        loginPasswordFragCancelListener.onCancel(activity, dialog);
                    }

                    if (autoLoginRequestTask != null) {
                        autoLoginRequestTask.cancel(true);
                    }
                }
            });

        }
        showProgressBar();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.concur.mobile.platform.ui.common.login.LoginPasswordFragment.LoginPasswordCallbacks#setProgressDialogCancelListener
     * (com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment.OnCancelListener)
     */
    public void setProgressDialogCancelListener(final ProgressDialogFragment.OnCancelListener cancelListener) {

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
                    new AlertDialogFragment.OnClickListener() {

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
        if (ConcurCore.userEntryAppTimer > 0) {
            ConcurCore.userSuccessfulLoginTimer = System.currentTimeMillis();
            long totalWaitTime = ConcurCore.userSuccessfulLoginTimer - ConcurCore.userEntryAppTimer;
            if(emailLookup!=null){
                signInMethod = emailLookup.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
            }
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

            if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)) {
                EventTracker.INSTANCE.eventTrack(Flurry.CATEGORY_SIGN_IN, Flurry.ACTION_FAIL_CREDENTIAL_TYPE,
                        Flurry.LABEL_LOGIN_USING_SSO, null);
            } else if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD)) {
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


    private void setAutoRequestReceiver() {
        autoLoginReceiver = new BaseAsyncResultReceiver(new Handler());
        autoLoginReceiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {

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
                Bundle bundle = null;
                if (extras != null && extras.containsKey(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE)) {
                    bundle = extras.getBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE);
                }else{
                    bundle=emailLookupBundle;
                }
                UserAndSessionInfoUtil.updateUserAndSessionInfo(ConcurCore.getContext(), bundle);

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

                //reset attempts
                noOfLoginAttempts = 0;
            }

            /*
             * (non-Javadoc)
             *
             * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestFail(android.os.Bundle)
             */
            public void onRequestFail(Bundle resultData) {
                // GA analytics
                trackLoginStatus(false, signInMethod);
                //increase number of attempts
                noOfLoginAttempts++;
                // close dialog
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (noOfLoginAttempts > 2) {
                    loginPasswordFragment.removeFragment();
                    noOfLoginAttempts = 0;
                    return;
                }

                // MOB-22674 - Show error dialog on failure.
                DialogFragmentFactory.getAlertOkayInstance(getString(R.string.dlg_system_unavailable_title),
                        getString(R.string.dlg_system_unavailable_message)).show(
                        EmailPasswordLookupActivity.this.getSupportFragmentManager(), null);

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
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

    }

}

