/**
 * 
 */
package com.concur.mobile.corp.activity;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.concur.breeze.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.AlertDialogFragment.OnClickListener;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.dialog.ProgressDialogFragment;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.RolesUtil;
import com.concur.mobile.core.util.UserAndSessionInfoUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.CorpSSOLoginRequestTask;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.util.Format;
import com.concur.platform.PlatformProperties;

/**
 * An extension of <code>BaseActivity</code> for the purposes of displaying a web-view containing company sign-on pages.
 */
@EventTracker.EventTrackerClassName(getClassName = "SSO")
public class CompanySignOnActivity extends BaseActivity {

    private static final String CLS_TAG = CompanySignOnActivity.class.getSimpleName();

    private static final String WEB_SESSION_COOKIE_NAME = "OTSESSIONAABQRN";

    private static final String MOBILE_SESSION_COOKIE_NAME = "MABQRN";

    private static final String CORP_SSO_LOGIN_RECEIVER = "corp.sso.login.request.receiver";

    private static final boolean LOG_PAGE_LOADS = false;

    protected final static String TAG_CONFIG_SESSION_WAIT_DIALOG = "tag.config.sessionwait.dialog";

    // Contains a reference to the web view object.
    private WebView webView;

    // Contains a reference to the cookie manager.
    private CookieManager cookieMngr;

    // Contains a reference to the cookie sync manager.
    private CookieSyncManager cookieSyncMngr;

    private BaseAsyncResultReceiver corpSSOLoginReceiver;

    private CorpSSOLoginRequestTask corpSSOLoginTask;

    private String serverUrl;

    // Whether the user has cancelled the SSO login session
    private boolean isShuttingDown;

    // Contains whether or not a login request is being processed.
    private boolean processingLoginRequest;

    // email lookup bundle
    private Bundle emailLookupBundle;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.company_signon);

        getSupportActionBar().setTitle(R.string.login_company_sign_on);
        // Get the server url.
        serverUrl = PlatformProperties.getServerAddress();
        String signInMethod = null;
        String ssoUrl = null;
        // Get the sso url.
        ConcurCore concurMobile = getConcurCore();
        CorpSsoQueryReply reply = concurMobile.getCorpSsoQueryReply();
        if (reply != null) {
            if (reply.ssoEnabled) {
                if (reply.ssoUrl != null) {
                    ssoUrl = reply.ssoUrl;
                }
            }
        }
        Intent i = getIntent();
        emailLookupBundle = i.getExtras().getBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE);
        if (emailLookupBundle == null) {
            emailLookupBundle = new Bundle();
            signInMethod = com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO;
            // Set the server url.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY, serverUrl);
            // Set the sign-in method.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, signInMethod);
            // Set the sso url.
            emailLookupBundle.putString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY, ssoUrl);
        } else {
            ssoUrl = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_SSO_URL_KEY);
            serverUrl = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_SERVER_URL_KEY);
            signInMethod = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
        }
        if (serverUrl != null) {
            UserAndSessionInfoUtil.setServerAddress(serverUrl);
        }

        if (ssoUrl != null) {
            webView = (WebView) findViewById(R.id.web_view);
            if (webView != null) {
                // webView.clearCache(true);
                WebSettings webSettings = webView.getSettings();
                webSettings.setCacheMode(WebSettings.LOAD_NORMAL);
                webSettings.setUserAgentString(Const.HTTP_HEADER_USER_AGENT_VALUE);
                // Ensure cookies are enabled.
                cookieMngr = CookieManager.getInstance();
                cookieMngr.setAcceptCookie(true);
                // cookieMngr.removeSessionCookie();
                // Init a cookie sync manager.
                cookieSyncMngr = CookieSyncManager.createInstance(this);
                // Enable Javascript.
                webSettings.setJavaScriptEnabled(true);
                // Enable Javascript opening windows.
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                // Set up a handler to inspect the response headers to catch a
                // cookie stating that
                // SSO has succeeded.
                // Ensure that all subsequent URL loading is handled by this
                // webview instance.
                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (LOG_PAGE_LOADS) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".shouldOverrideUrlLoading: url -> '" + url + "'.");
                        }
                        // logCookies(url);
                        view.loadUrl(url);
                        return true;
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see android.webkit.WebViewClient#onPageStarted(android.webkit .WebView, java.lang.String,
                     * android.graphics.Bitmap)
                     */
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        if (LOG_PAGE_LOADS) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".onPageStarted: url -> '" + url + "'");
                        }
                        if (!processingLoginRequest && url.contains("wait.asp")) {
                            inspectCookies(url);
                        }
                        // TODO: Comment out the following line prior to
                        // shipment.
                        // logCookies(url);
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see android.webkit.WebViewClient#onPageFinished(android.webkit .WebView, java.lang.String)
                     */
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (LOG_PAGE_LOADS) {
                            Log.d(Const.LOG_TAG, CLS_TAG + ".onPageFinished: url -> '" + url + "'");
                        }
                        // logCookies(url);
                    }

                });
                // Set the URL.
                webView.loadUrl(ssoUrl);
                // webView.loadUrl("http://172.17.96.59/samlForm.html");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate web view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: corp sso query reply is null!");
        }

        // Restore any receivers.
        restoreReceivers();
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

    /**
     * Inspects the current set of cookies looking for the session.
     */
    private void inspectCookies(String url) {
        // Perform a sync of the cookies.
        cookieSyncMngr.sync();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException intExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".inspectCookies: interrupted in a sleep!", intExc);
        }
        String webSessionId = null;
        // Look for the presence of the session cookie.
        if (cookieMngr.hasCookies()) {
            String cookies = cookieMngr.getCookie(url);
            if (cookies != null && cookies.length() > 0) {
                cookies = cookies.trim();
                // First, look for the mobile session cookie value.
                webSessionId = getCookieValue(MOBILE_SESSION_COOKIE_NAME, cookies);
                if (webSessionId == null) {
                    // Mobile session cookie not found! Look for web session
                    // cookie value.
                    webSessionId = getCookieValue(WEB_SESSION_COOKIE_NAME, cookies);
                    if (webSessionId != null) {
                        Log.i(Const.LOG_TAG, CLS_TAG + ".inspectCs: found wsc.");
                    }
                } else {
                    Log.i(Const.LOG_TAG, CLS_TAG + ".inspectCs: found msc.");
                }
            }
        }
        if (webSessionId != null && webSessionId.length() > 0) {
            if (LOG_PAGE_LOADS) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".inspectCookies: webSessionId -> '" + webSessionId + "'.");
            }
            // Set the flag that a login request will be processed.
            processingLoginRequest = true;
            // Halt the web view.
            haltWebView();
            // Hide the web view.
            webView.setVisibility(View.INVISIBLE);
            // Send the login request.
            sendCompanySignOnLoginRequest(webSessionId);
            // Punt the web session cookie.
            removeWebSessionCookie(url, WEB_SESSION_COOKIE_NAME);
        }
    }

    /**
     * Given a cookie name, will attempt to retrieve the value for that cookie out of a list of cookies.
     * 
     * @param cookieName
     *            the name of the cookie to obtain.
     * @param cookies
     *            the list of cookies.
     * @return an instance of <code>String</code> containing the cookie value, or <code>null</code> if not found.
     */
    private String getCookieValue(String cookieName, String cookies) {

        String cookieValue = null;
        int cookieIndex = cookies.indexOf(cookieName);
        if (cookieIndex != -1) {
            int equalsIndex = cookies.indexOf('=', cookieIndex);
            if (equalsIndex != -1) {
                int semicolonIndex = cookies.indexOf(';', equalsIndex);
                if (semicolonIndex != -1) {
                    // Cookie in middle of cookie string.
                    cookieValue = cookies.substring(equalsIndex + 1, semicolonIndex).trim();
                } else {
                    // Cookie at end.
                    cookieValue = cookies.substring(equalsIndex + 1).trim();
                }
            }
        }
        return cookieValue;
    }

    private void haltWebView() {
        webView.stopLoading();
        webView.loadData("", "text/html", "UTF-8");
    }

    /**
     * Will remove the web session cookie for <code>url</code> by setting its value to the empty string and setting an expiration
     * time in the past.
     * 
     * @param url
     *            the url.
     * @param cookieName
     *            the cookie name.
     */
    private void removeWebSessionCookie(String url, String cookieName) {
        // Log.d(Const.LOG_TAG, CLS_TAG +
        // ".removeWebSessionCookie: removing web session cookie for url " +
        // url);
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(cookieName);
        strBldr.append("=;expires=Mon, 17 Oct 2011 10:47:11 UTC; path=/");
        cookieMngr.setCookie(url, strBldr.toString());
        cookieSyncMngr.sync();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException intExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".removeWebSessionCookie: interrupted during sync sleep!", intExc);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(keyCode, event);
            setResult(RESULT_OK);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        // Save the Corp SSO login response handler.
        if (corpSSOLoginReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            corpSSOLoginReceiver.setListener(null);
            // Store it in the retainer.
            retainer.put(CORP_SSO_LOGIN_RECEIVER, corpSSOLoginReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Restore any retained data
        if (retainer != null && retainer.contains(CORP_SSO_LOGIN_RECEIVER)) {
            corpSSOLoginReceiver = (BaseAsyncResultReceiver) retainer.get(CORP_SSO_LOGIN_RECEIVER);
            corpSSOLoginReceiver.setListener(new CorpSSOLoginListener());
        }
    }

    protected Dialog createLoginFailureDialog(String title, String msg) {
        AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
        dlgBldr.setTitle(title);
        dlgBldr.setMessage(msg);
        dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return dlgBldr.create();
    }

    @SuppressWarnings("deprecation")
    private void startHomeScreen() {
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
            // Prior to the starting the home screen, initialize the system/user
            // configuration
            // information.
            ((ConcurMobile) getApplication()).initSystemConfig();
            ((ConcurMobile) getApplication()).initUserConfig();
            boolean fromNotification = false;
            if (getIntent() != null && getIntent().getExtras() != null) {
                fromNotification = getIntent().getExtras().getBoolean(ConcurMobile.FROM_NOTIFICATION);
            }
            if (!fromNotification) {
                Intent i = null;
                i = new Intent(this, Home.class);
                startActivity(i);
            }

            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Will initiate a company sign-on based login request to the server.
     * 
     * @param webSessionId
     *            the web session ID used to construct a mobile login session.
     */
    private void sendCompanySignOnLoginRequest(String webSessionId) {

        Log.i(Const.LOG_TAG, CLS_TAG + ".sendCompanySignOnLoginRequest: sending mobile session login...");

        initCompanySignOnLoginResponseHandler(webSessionId);
        // Ensure any previous message is cleared out.
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        if (!isFinishing()) {
            DialogFragmentFactory.getProgressDialog(getText(R.string.login_sso_configure_mobile_session).toString(),
                    true, true, new ProgressDialogFragment.OnCancelListener() {

                        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                            isShuttingDown = true;
                            // Closing down this page, when the user cancels.
                            // Goes back to Email Lookup
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }).show(getSupportFragmentManager(), TAG_CONFIG_SESSION_WAIT_DIALOG);
        }
        corpSSOLoginTask.execute();
    }

    private void initCompanySignOnLoginResponseHandler(String webSessionId) {
        isShuttingDown = false;

        if (corpSSOLoginReceiver == null) {
            corpSSOLoginReceiver = new BaseAsyncResultReceiver(new Handler());
            corpSSOLoginReceiver.setListener(new CorpSSOLoginListener());

            Locale locale = getResources().getConfiguration().locale;
            corpSSOLoginTask = new CorpSSOLoginRequestTask(getApplicationContext(), 1, corpSSOLoginReceiver,
                    webSessionId, locale.toString());
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initCompanySignOnLoginResponseHandler: response handler is not null!");
        }
    }

    protected class CorpSSOLoginListener implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {
            Log.i(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: mobile session login successful!");

            UserAndSessionInfoUtil.updateUserAndSessionInfo(CompanySignOnActivity.this, emailLookupBundle);

            // Track the success
            Login.trackLoginSuccess(Flurry.PARAM_VALUE_LOGIN_USING_SSO);

            if (!isShuttingDown) {
                String signInMethod = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
                // remove autologin if SSO user
                if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_SSO)) {
                    Preferences.isHideAutoLogin(true);
                } else {
                    Preferences.isHideAutoLogin(false);
                }
                // Go to homescreen ...
                startHomeScreen();
            }

            // Dismiss the dialog after firing off the intent.
            // This avoids having the login screen visible for some time
            // before the home screen appears. Happens when
            // things
            // are slow.
            DialogFragment df = (DialogFragment) getSupportFragmentManager().findFragmentByTag(
                    TAG_CONFIG_SESSION_WAIT_DIALOG);
            if (df != null)
                df.dismiss();

            // Clear the webview cache.
            webView.clearHistory();
            webView.clearFormData();
            webView.clearCache(true);

            // Set the result and don't come back here
            setResult(Activity.RESULT_OK);
            finish();
        }

        public void onRequestFail(Bundle resultData) {
            boolean wiped = resultData.containsKey(Const.LR_WIPED);
            if (wiped) {
                Log.i(Const.LOG_TAG, CLS_TAG + ".onRequestFail: mobile session login failed, remote wipe!");

                DialogFragmentFactory.getAlertDialog(getString(R.string.login_remote_wipe_title),
                        getString(R.string.login_remote_wipe_msg), R.string.okay, -1, -1,
                        new AlertDialogFragment.OnClickListener() {

                            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                            }

                            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                                // Go back to EmailLookup
                                finish();
                            }
                        }, null, null, null).show(getSupportFragmentManager(), null);

                // Clear out the webview cache.
                webView.post(new Runnable() {

                    public void run() {
                        // Clear the webview cache.
                        webView.clearHistory();
                        webView.clearFormData();
                        webView.clearCache(true);
                    }
                });
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".onRequestFail: mobile session login failed!");

                DialogFragmentFactory.getAlertDialog(
                        getString(R.string.login_sso_configure_mobile_session_failed_title),
                        getString(R.string.login_sss_configure_mobile_session_failed_msg), R.string.okay, -1, -1,
                        new AlertDialogFragment.OnClickListener() {

                            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                            }

                            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                                // Go back to EmailLookup
                                finish();
                            }

                        }, null, null, null).show(getSupportFragmentManager(), null);
            }
            DialogFragment df = (DialogFragment) getSupportFragmentManager().findFragmentByTag(
                    TAG_CONFIG_SESSION_WAIT_DIALOG);
            if (df != null)
                df.dismiss();
        }

        public void onRequestCancel(Bundle resultData) {
            Log.i(Const.LOG_TAG, CLS_TAG + ".onRequestCancel: mobile session login failed, user canceled!");
            cleanup();
        }

        public void cleanup() {
            corpSSOLoginReceiver = null;
        }
    }

}
