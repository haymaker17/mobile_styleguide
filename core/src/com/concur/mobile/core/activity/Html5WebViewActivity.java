/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.activity;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * 
 * @author Chris N. Diaz
 * 
 */
public abstract class Html5WebViewActivity extends FragmentActivity {

    private final static String TAG = Html5WebViewActivity.class.getSimpleName();

    private final static String EXTERNAL_LINK_FLAG = "#_cnqrexternal";

    // private final static String APP_CACH_DIR = "/html5webview";

    public final static String ENDPOINT = "/mobile/web/signin";

    private boolean enableCache;

    private long loadDelay;

    protected WebView webView;

    private ProgressDialog pd;

    protected boolean backButtonPressed = false;

    /**
     * 
     * @param enableCache
     */
    protected Html5WebViewActivity(boolean enableCache) {
        this(enableCache, 0L);
    }

    /**
     * 
     * @param enableCache
     * @param loadDelay
     *            time, in milliseconds, to dismiss the loading dialog.
     */
    protected Html5WebViewActivity(boolean enableCache, long loadDelay) {
        this.enableCache = enableCache;
        this.loadDelay = loadDelay;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Create and configure a WebView.
        setContentView(R.layout.web_view);
        webView = (WebView) findViewById(R.id.webview);

        // Disable scrollbars
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        // Scrollbar Overlay Content
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        // Be sure to enable JavaScript!
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Enable app caching.
        if (enableCache) {

            webSettings.setDomStorageEnabled(true);

            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // MOB-11842 - Using WebSettings.LOAD_CACHE_ELSE_NETWORK can cause
                                                                // issues with dynamically updating pages.
            webSettings.setDatabaseEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);

            // MOB-12567 - disabling WebView AppCache altogether cause it seems to cause issue
            // when switching between users. Disabling is OK since we are not utilizing "offline mode".
            // // Enable WebView AppCahce for offline mode (which is set by the HTML Manifest).
            // // MOB-9735 - saving cache in Internal storage for better security.
            // File cacheDir = getCacheDir();
            // if(cacheDir.exists() && cacheDir.canWrite()) {
            //
            // // MOB-12567 - Clearing out the WebView AppCache directory every time
            // // we invoke a new Html5WebView so we don't fill it up or
            // // re-use an old/expired cached page.
            // File appCacheDir = new File(cacheDir.getAbsolutePath() + APP_CACH_DIR);
            // if(appCacheDir.exists()) {
            // // Note we're not deleting on exit/close of the view because
            // // it might not get called during a crash. So just delete
            // // old cache before even launching the WebView.
            // String[] children = appCacheDir.list();
            // for (int i = 0; i < children.length; i++) {
            // new File(appCacheDir, children[i]).delete();
            // }
            // } else {
            // appCacheDir.mkdir();
            // }
            //
            // if(appCacheDir.exists()) {
            // // Set cache size to 5MB.
            // webSettings.setAppCacheMaxSize(1024*1024*5);
            // webSettings.setAppCachePath(appCacheDir.getAbsolutePath());
            // webSettings.setAllowFileAccess(true);
            // webSettings.setAppCacheEnabled(true);
            // }
            // }

            if (getUrl() != null && getUrl().indexOf(ENDPOINT + "#mobile?") != -1) {

                webView.clearHistory();
            }

        }

        final Activity activity = this;
        webView.setWebViewClient(new WebViewClient() {

            /*
             * (non-Javadoc)
             * 
             * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // MOB-9035 - If the URL contains "#_cnqrexternal" (without quotes),
                // then we need to launch the URL in an external browser.
                int index = url.indexOf(EXTERNAL_LINK_FLAG);
                if (index != -1) {

                    url = url.replace(EXTERNAL_LINK_FLAG, "");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);

                } else {
                    // Just load the URL within the WebView.
                    view.loadUrl(url);
                }

                return true;
            }

            // here you execute an action when the URL you want is about to load
            @Override
            public void onLoadResource(WebView view, final String url) {

                String tmpUrl = url.toUpperCase(Locale.US);
                if (tmpUrl.endsWith(".PDF") || tmpUrl.indexOf("/GETPDF?") != -1 || tmpUrl.indexOf("/GETIMAGE?") != -1) {
                    displayImage(url);
                } else {

                    // MOB-12606 - The imaging URL has undergone some change,
                    // so we need to check for special parameter.
                    Uri uri = Uri.parse(url);
                    try {
                        String clientExt = uri.getQueryParameter("clientExt");
                        if (clientExt != null && clientExt.equalsIgnoreCase("PDF")) {
                            displayImage(url);
                        }
                    } catch (UnsupportedOperationException e) {
                        Log.e(Const.LOG_TAG, TAG
                                + ".onLoadResource : UnsupportedOperationException this URI is not hierarchical ");
                        // we can use public abstract boolean isHierarchical () to check
                        // Returns true if this URI is hierarchical like "http://google.com". Absolute URIs are hierarchical if
                        // the scheme-specific part starts with a '/'. Relative URIs are always hierarchical.
                    }
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see android.webkit.WebViewClient#onReceivedError(android.webkit.WebView, int, java.lang.String, java.lang.String)
             */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (pd != null) {
                    pd.cancel();
                }
                Toast.makeText(activity,
                        "Error loading page (" + failingUrl + "): ErrorCode=" + errorCode + "; " + description,
                        Toast.LENGTH_LONG).show();
            }

            /*
             * (non-Javadoc)
             * 
             * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            /*
             * (non-Javadoc)
             * 
             * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
             */
            @Override
            public void onPageFinished(WebView view, String url) {

                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        if (pd != null) {
                            pd.cancel();
                        }
                    }
                };

                new Timer().schedule(task, loadDelay);

                // HACK: This is needed so when user presses the Back button on their Android device,
                // the WebView will be popped off the Activity stack and we go back to the native app
                // because the HTML5 framework uses a re-direct that messes up the history by 1.
                // MOB-16018
                // 4.4 calls onPageFinished right when the redirect happens and finishes the activity *before* the page change
                // (and likewise the url change) occurs. backButtonPressed makes sure this doesn't occur.
                if (url != null && url.indexOf(ENDPOINT + "#mobile?") != -1 && backButtonPressed) {
                    backButtonPressed = false;
                    Html5WebViewActivity.this.finish();
                }

            }

        });

        // Set custom WebChromeClient so we can see the
        // console.log() output in LogCat and also
        // update the app cache quota.
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                Log.d(Const.LOG_TAG,
                        TAG + ".WebConsole : " + consoleMessage.message() + "\n\tFrom " + consoleMessage.sourceId()
                                + "\n\tAt line " + consoleMessage.lineNumber());

                return true;
            }

            @Override
            public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
                    WebStorage.QuotaUpdater quotaUpdater) {
                if (enableCache) {
                    quotaUpdater.updateQuota(spaceNeeded * 2);
                }
            }
        });

        // Finally, load the URL.
        webView.loadUrl(getUrl());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {

            webView.goBack();
            // backButtonPressed is used in a check on whether or not to finish the activity
            backButtonPressed = true;
            return true;
        }

        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Returns the URL of the page to display in this <code>WebView</code>.
     * 
     * @return
     */
    protected abstract String getUrl();

    /**
     * Returns the title to display when viewing images, attachments, etc.
     * 
     * @return the title to display when viewing images, attachments, etc.
     */
    protected abstract String getImageScreenTitle();

    /**
     * Attempts to display the PDF image using <code>com.concur.mobile.core.expense.activity.ViewImage</code>, which will try to
     * see if there is a PDF viewer installed on the Android device.
     * 
     * @param url
     *            the PDF url to display.
     */
    protected void displayImage(String url) {
        if (url != null) {
            Intent i = new Intent(this, ViewImage.class);
            i.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, url);
            i.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getImageScreenTitle());
            i.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
            startActivity(i);
        }
    }

    /**
     * Returns the mobile web server URL.
     * 
     * @return the mobile web server URL.
     */
    protected String getServerUrl() {
        return Format.formatServerAddress(true, Preferences.getServerAddress());
    }

    /**
     * Build the session ID parameter - i.e. <code>sessionId=XXXXXX</code>
     * 
     * @return the session ID parameter - i.e. <code>sessionId=XXXXXX</code>
     */
    protected String buildSessionIdParam() {
        return "sessionId=" + Preferences.getSessionId();
    }

    /**
     * Builds the locale parameter containing the language and country - i.e. <code>locale=en-US</code>
     * 
     * @return the locale parameter containing the language and country - i.e. <code>locale=en-US</code>
     */
    protected String buildLocaleParam() {
        Locale locale = this.getResources().getConfiguration().locale;
        return "locale=" + locale.getLanguage() + "-" + locale.getCountry();
    }

    @Override
    protected void onDestroy() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        pd = ProgressDialog.show(Html5WebViewActivity.this, null, getString(R.string.general_loading), true, true);
    }

}
