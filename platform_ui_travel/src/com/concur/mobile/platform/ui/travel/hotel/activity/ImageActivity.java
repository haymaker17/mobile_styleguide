/**
 * 
 */
package com.concur.mobile.platform.ui.travel.hotel.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * An extension of <code>Activity</code> for the purpose of viewing an image.
 * 
 * @author AndrewK
 */
public class ImageActivity extends Activity {

    private static final String CLS_TAG = ImageActivity.class.getSimpleName();

    /**
     * Contains a reference to the receipt image web view.
     */
    private WebView webView;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.full_image_view);

        // Grab a reference to the web view.
        webView = (WebView) findViewById(R.id.web_view);
        if (webView == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: can't locate web view!");
        }
        Intent intent = getIntent();

        // Set the screen title
        getActionBar().setTitle(intent.getStringExtra(Const.EXTRA_IMAGE_TITLE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (webView != null) {
            // Enable on screen image zooming capabilities.
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

            Intent intent = getIntent();
            String urlStr = intent.getStringExtra(Const.EXTRA_IMAGE_URL);
            if (urlStr != null) {
                // URL url = null;
                // try {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onStart: url -> " + urlStr);
                // url = new URL(urlStr);
                // MOB-13759
                // Setting a WebViewClient ensures that all images are opened in the WebView and not in a browser
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl(urlStr);
                // } catch( MalformedURLException mlfUrlExc ) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: invalid URL", mlfUrlExc);
                // }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: missing URL string!");
            }
        }
    }

}
