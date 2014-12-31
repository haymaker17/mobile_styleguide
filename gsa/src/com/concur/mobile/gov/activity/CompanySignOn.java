/**
 * 
 */
package com.concur.mobile.gov.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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

import com.concur.gov.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.net.AuthenticationResponseHandler;
import com.concur.mobile.core.util.net.LoginRequest;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity</code> for the purposes of displaying
 * a web-view containing company sign-on pages.
 */
public class CompanySignOn extends BaseActivity {

    private static final String CLS_TAG = CompanySignOn.class.getSimpleName();

    private static final String WEB_SESSION_COOKIE_NAME = "OTSESSIONAABQRN";

    private static final String MOBILE_SESSION_COOKIE_NAME = "MABQRN";

    private static final String CORP_SSO_LOGIN_RESPONSE_HANDLER_KEY = "corp.sso.login.response.handler";

    private static final boolean LOG_PAGE_LOADS = false;

    private static final int DIALOG_CONFIGURING_MOBILE_SESSION_PROGRESS = 0;
    private static final int DIALOG_CONFIGURING_MOBILE_SESSION_FAILED = 1;
    private static final int DIALOG_REMOTE_WIPE = 2;

    // Contains a reference to the web view object.
    private WebView webView;

    // Contains a reference to the cookie manager.
    private CookieManager cookieMngr;

    // Contains a reference to the cookie sync manager.
    private CookieSyncManager cookieSyncMngr;

    // Contains a reference to a login response handler for a company sign-on based initiation
    // of a mobile session.
    private CompanySignOnResponseHandler loginResponseHandler;

    // Contains whether or not a login request is being processed.
    private boolean processingLoginRequest;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.company_signon);

        getSupportActionBar().setTitle(getString(R.string.login_company_sign_on));

        GovAppMobile concurMobile = (GovAppMobile) getConcurCore();
        CorpSsoQueryReply reply = concurMobile.getCorpSsoQueryReply();
        if (reply != null) {
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
                // Set up a handler to inspect the response headers to catch a cookie stating that
                // SSO has succeeded.
                // Ensure that all subsequent URL loading is handled by this webview instance.
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
                     * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String,
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
                        // TODO: Comment out the following line prior to shipment.
                        // logCookies(url);
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
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
                webView.loadUrl(reply.ssoUrl);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DIALOG_CONFIGURING_MOBILE_SESSION_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.login_sso_configure_mobile_session));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog and set the shutdown flag on the response handler.
                    dialog.dismiss();
                    if (loginResponseHandler != null) {
                        loginResponseHandler.setShuttingDown(true);
                    }
                    // Take the end-user back to the company sign-on activity.
                    Intent intent = new Intent(CompanySignOn.this, CompanyLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            dialog = progDlg;
            break;
        }
        case DIALOG_CONFIGURING_MOBILE_SESSION_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.login_sso_configure_mobile_session_failed_title);
            dlgBldr.setMessage(R.string.login_sss_configure_mobile_session_failed_msg);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Take the end-user back to the company sign-in activity.
                    Intent intent = new Intent(CompanySignOn.this, CompanyLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case DIALOG_REMOTE_WIPE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.login_remote_wipe_title));
            dlgBldr.setMessage(getText(R.string.login_remote_wipe_msg));
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                // Dismiss the dialog.
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // Take the end-user back to the login activity.
                    Intent intent = new Intent(CompanySignOn.this, CompanyLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        default: {
            break;
        }
        }
        return dialog;
    }

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

    // private void logCookies(String url) {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".logCookies: url -> '" + url + "'.");
    // String webSessionId = null;
    // // Look for the presence of the session cookie.
    // if (cookieMngr.hasCookies()) {
    // String cookies = cookieMngr.getCookie(url);
    // if (cookies != null && cookies.length() > 0) {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".logCookies: " + cookies);
    // cookies = cookies.trim();
    // int cookieIndex = cookies.indexOf(WEB_SESSION_COOKIE_NAME);
    // if (cookieIndex != -1) {
    // int equalsIndex = cookies.indexOf('=', cookieIndex);
    // if (equalsIndex != -1) {
    // int semicolonIndex = cookies.indexOf(';', equalsIndex);
    // if (semicolonIndex != -1) {
    // // Cookie in middle of cookie string.
    // webSessionId = cookies.substring(equalsIndex + 1, semicolonIndex).trim();
    // } else {
    // // Cookie at end.
    // webSessionId = cookies.substring(equalsIndex + 1).trim();
    // }
    // }
    // }
    // }
    // }
    // if (webSessionId != null && webSessionId.length() > 0) {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".logCookies: webSessionId -> '" + webSessionId + "'.");
    // }
    // }

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
                    // Mobile session cookie not found! Look for web session cookie value.
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
     * Given a cookie name, will attempt to retrieve the value for that cookie out of
     * a list of cookies.
     * 
     * @param cookieName
     *            the name of the cookie to obtain.
     * @param cookies
     *            the list of cookies.
     * @return
     *         an instance of <code>String</code> containing the cookie value, or <code>null</code> if not found.
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
     * Will remove the web session cookie for <code>url</code> by setting its value to the empty string
     * and setting an expiration time in the past.
     * 
     * @param url
     *            the url.
     * @param cookieName
     *            the cookie name.
     */
    private void removeWebSessionCookie(String url, String cookieName) {
        // Log.d(Const.LOG_TAG, CLS_TAG + ".removeWebSessionCookie: removing web session cookie for url " + url);
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
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the Corp SSO login response handler.
        if (loginResponseHandler != null) {
            // Clear the activity reference, it will be set in the 'onCreate' method.
            loginResponseHandler.setActivity(null);
            // Store it in the retainer.
            retainer.put(CORP_SSO_LOGIN_RESPONSE_HANDLER_KEY, loginResponseHandler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Restore any retained data
        if (retainer.contains(CORP_SSO_LOGIN_RESPONSE_HANDLER_KEY)) {
            loginResponseHandler = (CompanySignOnResponseHandler) retainer.get(CORP_SSO_LOGIN_RESPONSE_HANDLER_KEY);
            loginResponseHandler.setActivity(this);
        }
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
     * Will initiate a company sign-on based login request to the server.
     * 
     * @param webSessionId
     *            the web session ID used to construct a mobile login session.
     */
    private void sendCompanySignOnLoginRequest(String webSessionId) {

        Log.i(Const.LOG_TAG, CLS_TAG + ".sendCompanySignOnLoginRequest: sending mobile session login...");

        initCompanySignOnLoginResponseHandler(webSessionId);
        ConcurCore concurMobile = getConcurCore();
        // Ensure any previous message is cleared out.
        loginResponseHandler.clearMessage();
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        LoginRequest loginRequest = new LoginRequest(concurMobile, new Handler(loginResponseHandler),
            concurMobile.getProduct(), serverAdd, webSessionId);
        showDialog(Const.DIALOG_LOGIN_WAIT);
        loginRequest.start();
    }

    private void initCompanySignOnLoginResponseHandler(String webSessionId) {
        if (loginResponseHandler == null) {
            loginResponseHandler = new CompanySignOnResponseHandler(this, webSessionId);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initCompanySignOnLoginResponseHandler: response handler is not null!");
        }
    }

    private void clearCompanySignOnLoginResponseHandler() {
        loginResponseHandler = null;
    }

    /**
     * An implementation of <code>Handler.Callback</code> designed to be passed
     * to a future instance of <code>Login</code> for such things as handling
     * configuration changes, etc.
     * 
     * @author AndrewK
     */
    private static class CompanySignOnResponseHandler extends AuthenticationResponseHandler {

        private static final String CLS_TAG = CompanySignOn.CLS_TAG + "."
            + CompanySignOnResponseHandler.class.getSimpleName();

        private final String webSessionId;

        // Contains whether the end-user has requested this thread be shut down.
        private boolean shuttingDown;

        /**
         * Constructs an instance of <code>CompanySignOnResponseHandler</code> with
         * a current activity object.
         * 
         * @param login
         *            the current login handler.
         */
        CompanySignOnResponseHandler(Activity activity, String webSessionId) {
            super(activity);
            this.webSessionId = webSessionId;
        }

        synchronized void setShuttingDown(boolean shuttingDown) {
            this.shuttingDown = shuttingDown;
        }

        synchronized boolean isShuttingDown() {
            return shuttingDown;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.util.net.AuthenticationResponseHandler#handleFailure(java.util.Map)
         */
        @Override
        protected void handleFailure(Map<String, Object> responses) {

            final CompanySignOn compLogin = (CompanySignOn) activity;
            // Clear the response handler.
            compLogin.clearCompanySignOnLoginResponseHandler();
            if (!isShuttingDown()) {
                boolean wiped = responses.containsKey(Const.LR_WIPED);
                if (wiped) {
                    Log.i(Const.LOG_TAG, CLS_TAG + ".handleFailure: mobile session login failed, remote wipe!");
                    compLogin.showDialog(DIALOG_REMOTE_WIPE);
                    // Clear out the webview cache.
                    compLogin.webView.post(new Runnable() {

                        @Override
                        public void run() {
                            // Clear the webview cache.
                            compLogin.webView.clearHistory();
                            compLogin.webView.clearFormData();
                            compLogin.webView.clearCache(true);
                        }
                    });
                } else {
                    Log.i(Const.LOG_TAG, CLS_TAG + ".handleFailure: mobile session login failed!");
                    compLogin.showDialog(DIALOG_CONFIGURING_MOBILE_SESSION_FAILED);
                }
                compLogin.dismissDialog(Const.DIALOG_LOGIN_WAIT);
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".handleFailure: mobile session login failed, user canceled!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.util.net.AuthenticationResponseHandler#handleSuccess(java.util.Map)
         */
        @Override
        protected void handleSuccess(Map<String, Object> responses) {

            CompanySignOn compLogin = (CompanySignOn) activity;

            // Clear the response handler.
            compLogin.clearCompanySignOnLoginResponseHandler();

            if (!isShuttingDown()) {

                Log.i(Const.LOG_TAG, CLS_TAG + ".handleSuccess: mobile session login successful!");

                String sessionId = (String) responses.get(Const.LR_SESSION_ID);

                // Save the login response information.
                ConcurCore
                    .saveLoginResponsePreferences(sessionId, (GovAppMobile) compLogin.getApplication(), responses);

                // Flurry Notification
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_SSO);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN,
                    Flurry.EVENT_NAME_AUTHENTICATION, params);

                // Go to homescreen ...
                compLogin.startHomeScreen();

                // Dismiss the dialog after firing off the intent.
                // This avoids having the login screen visible for some time before the home screen appears. Happens when
                // things
                // are slow.
                compLogin.dismissDialog(Const.DIALOG_LOGIN_WAIT);

                // Clear the webview cache.
                compLogin.webView.clearHistory();
                compLogin.webView.clearFormData();
                compLogin.webView.clearCache(true);

                // Set the result and don't come back here
                Intent data = new Intent();
                data.putExtra(Const.EXTRA_COMPANY_SIGN_ON_SESSION_ID, webSessionId);
                compLogin.setResult(Activity.RESULT_OK, data);
                compLogin.finish();
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".handleSuccess: mobile session login successful, but user canceled!");
            }
        }

    }
}
