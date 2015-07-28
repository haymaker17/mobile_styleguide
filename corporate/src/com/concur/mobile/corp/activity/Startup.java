package com.concur.mobile.corp.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.appdynamics.eumagent.runtime.Instrumentation;
import com.concur.breeze.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.Notifications;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.AutoLoginRequestTask;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.LoginResponseKeys;
import com.concur.mobile.platform.authentication.PPLoginLightRequestTask;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.platform.PlatformProperties;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventTracker.EventTrackerClassName(getClassName = "Startup")
public class Startup extends BaseActivity {

    public static final String CLS_TAG = Startup.class.getSimpleName();
    public static final String START_TIME = "start time";
    public static final String END_TIME = "end time";

    protected final int SPLASH_DELAY = 2500;
    protected boolean isSplashDone = false;
    protected boolean isLoginDone = false;

    protected Intent startIntent;

    protected boolean onStartDelayed;

    protected boolean fromNotification;

    private final static int AUTO_LOGIN_REQUEST_ID = 1;
    private final static int VALIDATE_PASSWORD_REQUEST_ID = 2;

    private BaseAsyncResultReceiver loginReceiver;

    private Bundle emailLookupBundle = null;

    private SessionInfo sessionInfo;

    // List of languages the Eva API currently supports.
    private static final List<String> TESTDRIVE_USER_COUNTRIES = Arrays.asList(new String[] { "US", "GB", "AU", "CA" });

    // long miliseconds
    private long startTimeMillis, stopTimeMillis, totalTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Product p = ((ConcurMobile) getApplication()).getProduct();
        switch (p) {
        case CORPORATE:
            setContentView(R.layout.splash);
            // MOB-20174 - AppDynaics stuff.
            Instrumentation.start("AD-AAB-AAA-FUF", getApplicationContext(), true);

            new Handler().postDelayed(new Runnable() {

                public void run() {
                    doSplashFinish();
                }

            }, SPLASH_DELAY);

            break;
        default:
            // No splash
            isSplashDone = true;
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            fromNotification = getIntent().getExtras().getBoolean(ConcurMobile.FROM_NOTIFICATION);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onServiceAvailable()
     */
    @Override
    protected void onServiceAvailable() {

        // Start-up code delayed?
        if (onStartDelayed) {
            onStartDelayed = false;
            onStartInit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check for service availability prior to running start-up code below.
        // Determining whether company sso information has been persisted requires
        // calling into the concur service tier.
        if (isServiceAvailable()) {
            onStartInit();
        } else {
            onStartDelayed = true;
        }

    }

    protected void onStartInit() {

        // TODO: Now that we are caching images we should eventually put in some code (possibly
        // here) that will maintain the local cache at some certain size based on cached file age.

        // Initialization
        initPrefs();

        // AWS push
        if (Preferences.allowNotifications()) {
            Notifications notifications = new Notifications(ConcurCore.getContext());
            notifications.initAWSPushService();
        }

        // If we are already running and logged in then just go straight to home.
        // If preferences contains an expiration at this point then our session ID is still good.
        // See initPrefs().
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Check whether the session ID is present, if not, then clear the session expiration/duration.
        // Currently, during a database upgrade, the session ID can be removed from persistence, so a
        // check for that needs to be performed.
        ConcurMobile concurMobile = (ConcurMobile) getApplication();
        sessionInfo = ConfigUtil.getSessionInfo(concurMobile.getApplicationContext());
        String sessionId = (sessionInfo != null ? sessionInfo.getSessionId() : Preferences.getSessionId());
        if (sessionId == null || sessionId.length() == 0) {
            ConfigUtil.removeLoginInfo(this);
        }

        // If the user has a valid session and this is the first time running after an upgrade
        // then force an auto login to make sure that all preferences and module properties are current
        if (prefs.contains(Const.PREF_SESSION_EXPIRATION) && (!Preferences.isFirstTimeRunning(prefs))) {
            startHomeScreen();
            doLoginFinish();
        } else {
            // Determine whether a company sign-on URL has been cached. If so,
            // take the end-user to the company sign-on screen.
            ConcurService concurService = concurMobile.getService();
            CorpSsoQueryReply ssoReply = concurService.getCorpSsoQueryReply();
            if (((ssoReply != null && ssoReply.ssoEnabled && ssoReply.ssoUrl != null))
                    || (sessionInfo != null && (!TextUtils.isEmpty(sessionInfo.getSSOUrl())))) {
                // Perform a company sign-on based login.
                emailLookupBundle = getEmailLookUpBundleFromSessionInfo(sessionInfo);
                // set server url
                String serverUrl = ssoReply.serverUrl;
                if(sessionInfo!=null){
                    sessionInfo.setServerUrl(serverUrl);
                }
                if(serverUrl!=null && !serverUrl.isEmpty()){
                    //set platformproperties
                    PlatformProperties.setServerAddress(serverUrl);
                }
                startCompanySignOn();
                doLoginFinish();
            } else {
                emailLookupBundle = getEmailLookUpBundleFromSessionInfo(sessionInfo);
                String signInMethod = null;
                if (emailLookupBundle != null) {
                    signInMethod = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
                }
                if ((TextUtils.isEmpty(signInMethod))) {
                    startLoginScreen();
                    doLoginFinish();
                } else {
                    // Perform a pin-based login.
                    // See how we are going to start
                    boolean autoLogin = prefs.getBoolean(Const.PREF_AUTO_LOGIN, false);

                    boolean needLogin = true;
                    if (autoLogin) {
                        // TODO if autologin turn on dont do anything just go to homescreen
                        needLogin = false;
                        startHomeScreen();
                        doLoginFinish();
                    }

                    if (needLogin) {
                        // Auto-login wasn't wanted or couldn't happen
                        // Head to the login screen
                        startLoginScreen();
                        doLoginFinish();
                    }
                    // If needLogin is false at this point that means we are attempting auto-login
                    // and the handleMessage() method will forward us to where we need to go when
                    // the attempt completes.
                }
            }
        }
    }

    private Bundle getEmailLookUpBundleFromSessionInfo(SessionInfo sessionInfo) {
        // Determine whether a company sign-on URL has been cached. If so,
        // take the end-user to the company sign-on screen.
        String signInMethod = null;
        String ssoUrl = null;
        String serverUrl = null;
        String loginId = null;
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

    protected void doSplashFinish() {
        isSplashDone = true;
        doFinish();
    }

    protected void doLoginFinish() {
        if (startTimeMillis > 0L) {
            // Google Analytics
            stopTimeMillis = System.currentTimeMillis();
            totalTime = stopTimeMillis - startTimeMillis;
            Log.d(Const.LOG_TAG, CLS_TAG + ".process: request(" + "AutoLogin" + ") took " + (totalTime) + " ms.");
            logTotleTimeForAutoLogin(totalTime);
        }
        isLoginDone = true;
        doFinish();
    }

    private void logTotleTimeForAutoLogin(long totalWaitTime) {
        // Statistics Notification
        EventTracker.INSTANCE.trackTimings(Flurry.CATEGORY_WAIT_TIME, Flurry.ACTION_AUTO_LOGIN_WAIT,
                Flurry.LABEL_WAIT_TIME, totalWaitTime);
    }

    protected void doFinish() {
        if (isSplashDone && isLoginDone) {
            startIntent.putExtra(ConcurMobile.FROM_NOTIFICATION, fromNotification);
            if (fromNotification) {
                startActivityForResult(startIntent, ConcurMobile.START_UP_REQ_CODE);
            } else {
                startActivity(startIntent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ConcurCore.START_UP_REQ_CODE) {
                setResult(RESULT_OK);
            }
        }
        finish();
    }

    private void startLoginScreen() {
        // If this is the first time running, Registration page
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (Preferences.isFirstTimeRunning(prefs)) {
            String cntryCode = ViewUtil.getUserCountryCode(this);
            if (TESTDRIVE_USER_COUNTRIES.contains(cntryCode)) {
                startIntent = new Intent(this, PreLogin.class);
            } else {
                //reset user timers
                ConcurCore.resetUserTimers();
                startIntent = new Intent(this, EmailLookupActivity.class);
            }
            // in home we are already setting this Preferences.setNotFirstTimeRunning(prefs);
        } else {
            //reset user timers
            ConcurCore.resetUserTimers();
            // go to login
            startIntent = new Intent(this, EmailLookupActivity.class);
        }
    }

    private void startHomeScreen() {
        startIntent = new Intent(this, Home.class);

        boolean launchExpList = getIntent().getBooleanExtra(Home.LAUNCH_EXPENSE_LIST, false);
        startIntent.putExtra(Home.LAUNCH_EXPENSE_LIST, launchExpList);
    }

    private void startCompanySignOn() {
        // NOTE: This intent actually starts the Login activity, but passing a boolean value
        // indicating the Login activity should immediately start the company sign-on process.
        startIntent = new Intent(this, CompanySignOnActivity.class);
        startIntent.putExtra(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE, emailLookupBundle);
        startIntent.putExtra(EmailLookupActivity.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return ((ConcurMobile) getApplication()).createDialog(this, id);
    }

    /**
     * Perform basic initialization of preferences
     */
    private void initPrefs() {

        final ConcurMobile app = (ConcurMobile) getApplication();

        // Grab our default preferences and get the server address configured
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains(Const.PREF_MWS_ADDRESS)) {
            app.savePreference(prefs, Const.PREF_MWS_ADDRESS, Const.DEFAULT_MWS_ADDRESS);
        }

        // And default the save login value to true and the
        if (!prefs.contains(Const.PREF_SAVE_LOGIN)) {
            app.savePreference(prefs, Const.PREF_SAVE_LOGIN, true);
            app.savePreference(prefs, Const.PREF_AUTO_LOGIN, true);
        }

        PackageInfo pi = null;
        try {
            pi = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initPrefs: unable to locate package information!");
        }

        // Check for stored version of the application and adjust preferences, if need be.
        if (prefs.contains(Const.PREF_VERSION_NAME) && (pi != null)) {
            // Compare the versions and do something if needed
            String prevVer = prefs.getString(Const.PREF_VERSION_NAME, "");
            if (!prevVer.equals(pi.versionName)) {
                // Do stuff. For now, make these things rerunnable so we don't have to put together
                // a deep version update stack. Since a user could be upgrading from one of many
                // previous versions we either have to build an upgrade stack or just make our
                // upgrades rerunnable. Since these changes are small and infrequent we will make
                // them rerunnable.
                BaseAsyncResultReceiver autoLoginReceiver = new BaseAsyncResultReceiver(new Handler());
                Preferences.upgradePreferences(app, autoLoginReceiver);
            }
        } else {
            // Earlier versions of the app did not contain a version name/code.
            // Punt any session expiration value.
            Editor e = prefs.edit();
            e.remove(Const.PREF_SESSION_EXPIRATION);
            e.remove(Const.PREF_MSG_CENTER_BADGE);
            e.commit();
        }

        // We've checked and done any work. Update the stored version code and name
        if (pi != null) {
            Editor e = prefs.edit();
            e.putString(Const.PREF_VERSION_NAME, pi.versionName);
            e.putInt(Const.PREF_VERSION_CODE, pi.versionCode);
            e.commit();
        }

        // Check the expiration on the session and clear it if needed
        Long expire = prefs.getLong(Const.PREF_SESSION_EXPIRATION, 0);
        long now = Calendar.getInstance().getTimeInMillis();
        if (!ConcurMobile.isConnected() || (expire <= now)) {
            Editor e = prefs.edit();
            e.remove(Const.PREF_SESSION_DURATION);
            e.remove(Const.PREF_SESSION_EXPIRATION);
            e.commit();
        }
    }

    /**
     * Attempt a login. Will spawn a LoginThread to do the work so this method will return before the login has completed.
     * 
     * When not connected just return true if we have a login ID and pin saved.
     * 
     * @return true if the login was attempted, false otherwise
     */
    protected boolean doLogin() {

        loginReceiver = new BaseAsyncResultReceiver(new Handler());
        loginReceiver.setListener(new StartupLoginListner());

        Log.d(Const.LOG_TAG, "attempting autologin");

        // Grab values
        String loginId = (sessionInfo != null ? sessionInfo.getLoginId() : null);
        String pinOrPassword = Preferences.getPin(PreferenceManager.getDefaultSharedPreferences(this), null);
        String accessToken = (sessionInfo != null ? sessionInfo.getAccessToken() : null);

        if (loginId == null || (pinOrPassword == null && accessToken == null)) {
            Log.d(Const.LOG_TAG, "cannot autologin");
            return false;
        }

        if (ConcurMobile.isConnected()) {
            // TODO MOB-23154: Right now only time tracking required for Autologin, in future if we want it for each request
            // this implementation will be in the class PlatFormAsyncTaskRequest/BaseAsyncRequestTask.
            startTimeMillis = System.currentTimeMillis();

            // Animate in the message about authentication happening.
            TextView txtView = (TextView) findViewById(R.id.splash_message);
            if (txtView != null) {
                Animation fadeInAnim = AnimationUtils.loadAnimation(Startup.this, R.anim.fade_in);
                fadeInAnim.setFillAfter(true);
                txtView.setVisibility(View.VISIBLE);
                txtView.startAnimation(fadeInAnim);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".doLogin: can't locate splash text view!");
            }

            UserAndSessionInfoUtil.setServerAddress(sessionInfo.getServerUrl());

            // Attempt to authenticate
            // Re-entry to the UI will be via handleMessage() below
            ConcurMobile app = (ConcurMobile) getApplication();
            if (accessToken != null) {
                AutoLoginRequestTask autoLoginRequestTask = new AutoLoginRequestTask(getApplication()
                        .getApplicationContext(), AUTO_LOGIN_REQUEST_ID, loginReceiver, Locale.getDefault()) {

                    // MOB-20508 - If logging in was initiated by the Receipt Share (notification)
                    // then we should modify the User Agent so it is easily detectable in MobileMetrics.
                    @Override
                    protected String getUserAgent() {
                        return (fromNotification ? super.getUserAgent() + " (AutoLoginV3; From Notification)" : super
                                .getUserAgent());
                    }
                };
                autoLoginRequestTask.execute();
            } else {
                Locale locale = Locale.getDefault();
                PPLoginLightRequestTask ppLoginLightRequestTask = new PPLoginLightRequestTask(
                        app.getApplicationContext(), loginReceiver, VALIDATE_PASSWORD_REQUEST_ID, locale, loginId,
                        pinOrPassword);
                ppLoginLightRequestTask.execute();
            }

            return true;

        } else {
            // If disconnected with auto-login enabled and both values saved then say we logged in.
            // Move us on and finish this activity
            startHomeScreen();
            doLoginFinish();

            return true;
        }
    }

    protected class StartupLoginListner implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {
            final int requestID = resultData.getInt(BaseAsyncRequestTask.REQUEST_ID);

            switch (requestID) {
            case AUTO_LOGIN_REQUEST_ID: {
                // MOB-18782 Check remote wipe.
                if (resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, false)) {
                    showRemoteWipeDialog();
                } else {

                    SessionInfo sessionInfo = ConfigUtil.getSessionInfo(Startup.this);
                    String sessionId = sessionInfo.getSessionId();

                    if (TextUtils.isEmpty(sessionId)) {
                        startLoginScreen();
                    } else {
                        // Save the login information.
                        UserAndSessionInfoUtil.updateUserAndSessionInfo(Startup.this, emailLookupBundle);

                        // Statistics Notification
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_AUTO_LOGIN);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_AUTHENTICATION, params);

                        startHomeScreen();
                    }

                    // Don't come back here
                    doLoginFinish();
                }
                break;
            }
            case VALIDATE_PASSWORD_REQUEST_ID: {
                AutoLoginRequestTask autoLoginRequestTask = new AutoLoginRequestTask(getApplication()
                        .getApplicationContext(), AUTO_LOGIN_REQUEST_ID, loginReceiver, Locale.getDefault());
                autoLoginRequestTask.execute();

                break;
            }
            default: {
                if (resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, false)) {
                    showRemoteWipeDialog();
                } else {
                    displayUnableToLoginDialog("StartupLoginListner.onRequestSuccess(): "
                            + "Login returned sucessfully, but with unknown request ID: " + requestID);
                }
                break;
            }
            }
        }

        public void onRequestFail(Bundle resultData) {
            // MOB-18782 Check remote wipe.
            if (resultData.getBoolean(LoginResponseKeys.REMOTE_WIPE_KEY, false)) {
                showRemoteWipeDialog();
            } else {
                // FIXME if resultData code == BaseAsyncRequestTask.RESULT_ERROR
                // then show network error dialog.
                displayUnableToLoginDialog("StartupLoginListner.onRequestFail(): Login failed!");
            }
            return;
        }

        public void onRequestCancel(Bundle resultData) {
            return;

        }

        public void cleanup() {
            return;

        }

        private void displayUnableToLoginDialog(final String debugMessage) {

            // If login fails for some reason, then go to the EmailLookup screen.
            AlertDialogFragment dialog = DialogFragmentFactory
                    .getPositiveDialogFragment(getText(R.string.general_error).toString(),
                            getText(R.string.login_failure).toString(), getText(R.string.okay).toString(),
                            new AlertDialogFragment.OnClickListener() {

                                public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                                    // nothing to do
                                }

                                public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                                    Log.e(Const.LOG_TAG, debugMessage);
                                    startLoginScreen();
                            doLoginFinish();
                        }

                    });
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), null);
        }

        private void showRemoteWipeDialog() {
            Log.e(Const.LOG_TAG, "Startup.class - Remote Wipe sent down by auto-login.");

            // Notify the user about the "Remote Wipe".
            AlertDialogFragment adf = DialogFragmentFactory.getAlertOkayInstance(R.string.account_locked,
                    R.string.account_locked_message);
            adf.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

                public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                }

                public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                    startLoginScreen();
                    doLoginFinish();
                }
            });
            adf.setCancelable(false);
            adf.show(getSupportFragmentManager(), "tag.login.remote.wipe.dialog");
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(START_TIME, startTimeMillis);
        savedInstanceState.putLong(END_TIME, stopTimeMillis);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.startTimeMillis = savedInstanceState.getLong(START_TIME);
        this.stopTimeMillis = savedInstanceState.getLong(END_TIME);
    }

}