/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.core.invoice.activity;

import android.content.res.Configuration;

import com.concur.core.R;
import com.concur.mobile.core.activity.Html5WebViewActivity;

/**
 * @author Chris N. Diaz
 * 
 */
public class PurchaseRequestsWebView extends Html5WebViewActivity {

    private final static String HASH = "#mobile?pageId=purchaserequest-home-page&";

    /**
     * Default constructor with caching enabled.
     */
    public PurchaseRequestsWebView() {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.Html5WebViewActivity#getUrl()
     */
    @Override
    protected String getUrl() {
        return getServerUrl() + ENDPOINT + HASH + buildSessionIdParam() + "&" + buildLocaleParam();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.Html5WebViewActivity#getImageScreenTitle()
     */
    @Override
    protected String getImageScreenTitle() {
        return getResources().getString(R.string.invoice);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
