package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.AutoLoginRequestTask;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.LoginResponseKeys;
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
    private static final List<String> TESTDRIVE_USER_COUNTRIES = Arrays.asList(new String[]{"US", "GB", "AU", "CA"});

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

        //reset timer
        ConcurMobile.resetUserTimers();
        ;
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
                //MOB-24861 SSO URL is null crash issue.
                String serverUrl = null;
                if (ssoReply != null) {
                    serverUrl = ssoReply.serverUrl;
                } else if (sessionInfo != null) {
                    serverUrl = sessionInfo.getServerUrl();
                }

                if (sessionInfo != null) {
                    sessionInfo.setServerUrl(serverUrl);
                }
                if (serverUrl != null && !serverUrl.isEmpty()) {
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
        isLoginDone = true;
        doFinish();
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
            //reset user timers
            ConcurCore.resetUserTimers();
            startIntent = new Intent(this, EmailPasswordLookupActivity.class);
            // in home we are already setting this Preferences.setNotFirstTimeRunning(prefs);
        } else {
            //reset user timers
            ConcurCore.resetUserTimers();
            // go to login
            startIntent = new Intent(this, EmailPasswordLookupActivity.class);
        }
    }

    private void startHomeScreen() {
        Activity act = Startup.this;
        startIntent = getStartIntent(act);
        boolean launchExpList = getIntent().getBooleanExtra(Home.LAUNCH_EXPENSE_LIST, false);
        startIntent.putExtra(Home.LAUNCH_EXPENSE_LIST, launchExpList);
    }

    public static Intent getStartIntent(Activity activity){
        Intent startIntent = null;
        boolean shownExpenseIt = false, shownTravel = false, shownBoth = false;
        Context ctx = ConcurCore.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean isTravelOnly = RolesUtil.isTravelOnlyUser(activity);
        //check roles
        if (!(RolesUtil.isTestDriveUser()) && Preferences.isExpenseItUser()) {
            if (Preferences.isFirstRunExpUpgradeExpenseIt(prefs)) {
                shownExpenseIt = true;
                Preferences.setFirstRunExpUpgradeExpenseIt(prefs);
            } else {
                shownExpenseIt = false;
            }
        }
        if (!(RolesUtil.isTestDriveUser()) && isTravelOnly) {
            if (Preferences.isFirstRunExpUpgradeTravel(prefs)) {
                shownTravel = true;
                Preferences.setFirstRunExpUpgradeTravel(prefs);
            } else {
                shownTravel = false;
            }
        }

        if (Preferences.isFirstRunExpUpgradeExpenseItTravel(prefs)) {
            shownBoth = true;
            shownExpenseIt = true;
            shownTravel = true;
            Preferences.setFirstRunExpUpgradeExpenseItTravel(prefs);
            Preferences.setFirstRunExpUpgradeTravel(prefs);
            Preferences.setFirstRunExpUpgradeExpenseIt(prefs);
        } else if (shownExpenseIt && shownTravel) {
            shownBoth = true;
            Preferences.setFirstRunExpUpgradeExpenseItTravel(prefs);
        } else {
            shownBoth = false;
        }

        if (!shownBoth && Preferences.isExpenseItUser() && isTravelOnly) {
            Log.d(CLS_TAG,"This is ExpenseIT and Travel User");
            startIntent = new Intent(activity, FirstRunExpItTravelTour.class);
        } else if (!shownExpenseIt && Preferences.isExpenseItUser()) {
            Log.d(CLS_TAG, "This is ExpenseIT Only User");
            startIntent = new Intent(activity, FirstRunExpItTour.class);
        } else if (!shownTravel && RolesUtil.isTraveler(ctx)) {
            Log.d(CLS_TAG, "This is Travel Only User");
            startIntent = new Intent(activity, FirstRunTravelTour.class);
        } else {
            startIntent = new Intent(activity, Home.class);
        }

        return startIntent;
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
                Preferences.upgradePreferences(app);
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
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}