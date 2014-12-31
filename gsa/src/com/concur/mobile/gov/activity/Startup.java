package com.concur.mobile.gov.activity;

import java.util.Calendar;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.net.LoginRequest;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.platform.util.Format;

public class Startup extends BaseActivity implements Handler.Callback {

    public static final String CLS_TAG = Startup.class.getSimpleName();

    protected final int SPLASH_DELAY = 2500;
    protected boolean isSplashDone = false;
    protected boolean isLoginDone = false;

    protected Intent startIntent;

    protected boolean onStartDelayed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Product p = ((GovAppMobile) getApplication()).getProduct();
        switch (p) {
        case GOV:
            setContentView(R.layout.splash);

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onServiceAvailable()
     */
    @Override
    protected void onServiceAvailable() {

        // Start-up code delayed?
        if (onStartDelayed) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: start-up delayed, running start-up code now.");
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
            Log.d(Const.LOG_TAG, CLS_TAG + ".onStart: service available running start-up code.");
            onStartInit();
        } else {
            onStartDelayed = true;
            Log.d(Const.LOG_TAG, CLS_TAG + ".onStart: service unavailable, delayed running start-up code.");
        }
    }

    protected void onStartInit() {

        // TODO: Now that we are caching images we should eventually put in some code (possibly
        // here) that will maintain the local cache at some certain size based on cached file age.

        // Initialization
        initPrefs();

        // If we are already running and logged in then just go straight to home.
        // If preferences contains an expiration at this point then our session ID is still good.
        // See initPrefs().
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Check whether the session ID is present, if not, then clear the session expiration/duration.
        // Currently, during a database upgrade, the session ID can be removed from persistence, so a
        // check for that needs to be performed.
        String sessionId = Preferences.getSessionId();
        if (sessionId == null || sessionId.length() == 0) {
            Preferences.clearUser(prefs);
        }
        if (prefs.contains(Const.PREF_SESSION_EXPIRATION)) {
            startHomeScreen();
            doLoginFinish();
        } else {

            // Determine whether a company sign-on URL has been cached. If so,
            // take the end-user to the company sign-on screen.
            GovAppMobile concurMobile = (GovAppMobile) getApplication();
            ConcurService concurService = concurMobile.getService();
            CorpSsoQueryReply ssoReply = concurService.getCorpSsoQueryReply();
            if (ssoReply != null && ssoReply.ssoEnabled && ssoReply.ssoUrl != null) {
                // Perform a company sign-on based login.
                startCompanySignOn();
                doLoginFinish();
            } else {
                // Perform a pin-based login.

                // See how we are going to start
                boolean autoLogin = prefs.getBoolean(Const.PREF_AUTO_LOGIN, false);

                boolean needLogin = true;
                if (autoLogin) {
                    needLogin = !doLogin();
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
            startActivity(startIntent);
            finish();
        }
    }

    private void startLoginScreen() {
        startIntent = new Intent(this, Login.class);
    }

    private void startHomeScreen() {

        // Prior to the starting the home screen, initialize the system/user configuration
        // information.
        ((GovAppMobile) getApplication()).initSystemConfig();
        ((GovAppMobile) getApplication()).initUserConfig();
        // clear cache
        ((GovAppMobile) getApplication()).clearCaches();
        startIntent = new Intent(this, Home.class);
    }

    private void startCompanySignOn() {
        // NOTE: This intent actually starts the Login activity, but passing a boolean value
        // indicating the Login activity should immediately start the company sign-on process.
        startIntent = new Intent(this, Login.class);
        startIntent.putExtra(CompanyLogin.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return ((GovAppMobile) getApplication()).createDialog(this, id);
    }

    /**
     * Perform basic initialization of preferences
     */
    private void initPrefs() {

        final GovAppMobile app = (GovAppMobile) getApplication();

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
        if (!GovAppMobile.isConnected() || (expire <= now)) {
            Editor e = prefs.edit();
            e.remove(Const.PREF_SESSION_DURATION);
            e.remove(Const.PREF_SESSION_EXPIRATION);
            e.commit();
        }
    }

    /**
     * Attempt a login. Will spawn a LoginThread to do the work so this method will return before the login
     * has completed.
     * 
     * When not connected just return true if we have a login ID and pin saved.
     * 
     * @return true if the login was attempted, false otherwise
     */
    protected boolean doLogin() {

        Log.d(Const.LOG_TAG, "attempting autologin");

        // Grab values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String loginId = Preferences.getLogin(prefs, null);
        String pin = Preferences.getPin(prefs, null);

        if (loginId == null || pin == null) {
            Log.d(Const.LOG_TAG, "cannot autologin");
            return false;
        }

        if (GovAppMobile.isConnected()) {

            // Animate in the messsage about authentication happening.
            TextView txtView = (TextView) findViewById(R.id.splash_message);
            if (txtView != null) {
                Animation fadeInAnim = AnimationUtils.loadAnimation(Startup.this, R.anim.fade_in);
                fadeInAnim.setFillAfter(true);
                txtView.setVisibility(View.VISIBLE);
                txtView.startAnimation(fadeInAnim);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".doLogin: can't locate splash text view!");
            }

            String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

            // Attempt to authenticate
            // Re-entry to the UI will be via handleMessage() below
            GovAppMobile app = (GovAppMobile) getApplication();
            (new LoginRequest(app, new Handler(this), app.getProduct(), serverAdd, loginId, pin)).start();

            return true;

        } else {
            // If disconnected with auto-login enabled and both values saved then say we logged in.
            // Move us on and finish this activity
            startHomeScreen();
            doLoginFinish();

            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(Message msg) {

        if (msg.what == Const.MSG_LOGIN_RESULT) {
            HashMap<String, Object> responses = (HashMap<String, Object>) msg.obj;
            String sessionId = (String) responses.get(Const.LR_SESSION_ID);

            if (sessionId == null) {
                startLoginScreen();

                // Luckily a toast exists outside the bounds of the activity/screen...
                String status = (String) responses.get(Const.LR_STATUS);
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();

            } else {

                // Save the login information.
                ConcurCore.saveLoginResponsePreferences(sessionId, (GovAppMobile) getApplication(), responses);

                startHomeScreen();
            }

            // Don't come back here
            doLoginFinish();

        }

        return true;
    }
}