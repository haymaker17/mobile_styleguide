package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.ui.common.IProgressBarListener;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;
import com.concur.mobile.platform.ui.common.login.EmailLookupFragment;
import com.concur.mobile.platform.ui.common.login.EmailLookupFragment.EmailLookupCallbacks;
import com.concur.platform.PlatformProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventTracker.EventTrackerClassName(getClassName = "Email Lookup")
public class EmailLookupActivity extends BaseActivity implements IProgressBarListener, EmailLookupCallbacks {

    /**
     * Extra flag used to indicate if we should automatically advanced to the company sigin on screen.
     */
    public static final String EXTRA_ADVANCE_TO_COMPANY_SIGN_ON = "advance_to_company_sign_on";

    private static final int LOGIN_PASSWORD_REQ_CODE = 1;

    private static final int LOGIN_SSO_REQ_CODE = 2;

    private static final String FRAGMENT_EMAIL_LOOKUP = "FRAGMENT_EMAIL_LOOKUP";
    private boolean progressbarVisible;
    private static final String PROGRESSBAR_VISIBLE = "PROGRESSBAR_VISIBLE";

    protected String emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_drive_main);

        if(ConcurCore.userEntryAppTimer>0L){
            ConcurCore.userEntryAppTimer+=System.currentTimeMillis();
        }else{
            ConcurCore.userEntryAppTimer=System.currentTimeMillis();
        }
        // Enable the progress mask if needed
        if (savedInstanceState != null) {
            progressbarVisible = savedInstanceState.getBoolean(PROGRESSBAR_VISIBLE, false);
            if (progressbarVisible) {
                showProgressBar();
            }
        }

        // set title
        getSupportActionBar().setTitle(R.string.login_title);

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
        emailText = getIntent().getStringExtra(EmailLookupFragment.ARGS_EMAIL_TEXT_VALUE);

        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        PlatformFragment emailLookupFragment = (PlatformFragment) fm.findFragmentByTag(FRAGMENT_EMAIL_LOOKUP);
        if (emailLookupFragment == null) {
            emailLookupFragment = new EmailLookupFragment();

            // If we're supposed to load email lookup with text, push that to the fragment.
            // But first check if it's empty, and if it is and autologin is enabled, get it from the Prefs.
            if (TextUtils.isEmpty(emailText)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (prefs.getBoolean(Const.PREF_SAVE_LOGIN, false)) {
                    // Restore login ID : MOB-18534
                    emailText = Preferences.getLogin(prefs, "");
                }
            }

            bundle.putString(EmailLookupFragment.ARGS_EMAIL_TEXT_VALUE, emailText);
            emailLookupFragment.setArguments(bundle);

            ((EmailLookupFragment) emailLookupFragment).setProgressBarListener(this);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, emailLookupFragment, FRAGMENT_EMAIL_LOOKUP);
            ft.commit();
        } else {
            ((EmailLookupFragment) emailLookupFragment).setProgressBarListener(this);
        }
    }

    public void showProgressBar() {
        View v = findViewById(R.id.progress_mask);
        RelativeLayout progressBar = (RelativeLayout) v;
        progressbarVisible = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        View v = findViewById(R.id.progress_mask);
        RelativeLayout progressBar = (RelativeLayout) v;
        progressbarVisible = false;
        progressBar.setVisibility(View.INVISIBLE);
    }

    public boolean isProgressBarShown() {
        return progressbarVisible;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESSBAR_VISIBLE, progressbarVisible);
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
        }else {
            if(ConcurCore.userEntryAppTimer>0L){
                ConcurCore.userEntryAppTimer+=System.currentTimeMillis();
                Log.d(">>>>>>>> " + Const.LOG_TAG + " >>>>>> ", " Total Time EmailLookup time = " + ConcurCore.userEntryAppTimer);
            }else{
                ConcurCore.userEntryAppTimer=System.currentTimeMillis();
                Log.d(">>>>>>>> "+Const.LOG_TAG + " >>>>>> ", " Total Time EmailLookup  time = " + ConcurCore.userEntryAppTimer);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login, menu);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuSettings:
            Intent i = new Intent(this, Preferences.class);
            i.putExtra(Preferences.OPEN_SOURCE_LIBRARY_CLASS, OpenSourceLicenseInfo.class);
            startActivity(i);
            break;
        }
        return super.onOptionsItemSelected(item);
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

        if (serverUrl != null) {
            UserAndSessionInfoUtil.setServerAddress(serverUrl);
        } else {
            UserAndSessionInfoUtil.setServerAddress(PlatformProperties.getServerAddress());
        }

        Bundle loginBundle = resultData;
        if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD)
                || (signInMethod
                        .equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD))) {
            Intent it = new Intent(this, LoginPasswordActivity.class);
            it.putExtra(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE, loginBundle);
            startActivityForResult(it, LOGIN_PASSWORD_REQ_CODE);
        } else if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)
                && !TextUtils.isEmpty(ssoUrl)) {
            // Launch the company sign-on activity.
            Intent it = new Intent(this, CompanySignOnActivity.class);
            it.putExtra(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE, loginBundle);
            startActivityForResult(it, LOGIN_SSO_REQ_CODE);
        } else {
            // TODO error.
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.platform.ui.common.login.EmailLookupFragment.EmailLookupCallbacks#onEmailLookupRequestFail(android.os
     * .Bundle)
     */
    @SuppressWarnings("unchecked")
    public void onEmailLookupRequestFail(Bundle resultData) {
        boolean dialogShown = false;

        // MOB-15531 - show error message from MWS (only in the case of Akamai related error) and
        // in other cases, show the general email lookup error message
        if (resultData != null && resultData.containsKey(PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY)) {
            List<com.concur.mobile.platform.service.parser.Error> errors = (List<com.concur.mobile.platform.service.parser.Error>) resultData
                    .getSerializable(PlatformAsyncRequestTask.EXTRA_MWS_RESPONSE_STATUS_ERRORS_KEY);
            if (errors != null && errors.size() > 0) {
                if (errors.get(0) != null && errors.get(0).getCode() != null
                        && errors.get(0).getCode().equals("RATE_LIMIT_1")) {

                    // show error message from MWS
                    DialogFragmentFactory.getAlertOkayInstance(getText(R.string.general_network_error).toString(),
                            errors.get(0).getUserMessage()).show(getSupportFragmentManager(), null);

                    dialogShown = true;
                }
            }
        }

        if (!dialogShown) {
            // show the general email lookup error message
            DialogFragmentFactory.getAlertOkayInstance(getText(R.string.email_lookup_unable_to_login_title).toString(),
                    R.string.email_lookup_unable_to_login_msg).show(getSupportFragmentManager(), null);
            trackEmailLookupFailure(Flurry.EVENT_OTHER_ERROR);
        }
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
        Map<String, String> params = new HashMap<String, String>();
        params.put("Type", failureType);

        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_EMAIL_LOOKUP_FAILURE, params);
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
}
