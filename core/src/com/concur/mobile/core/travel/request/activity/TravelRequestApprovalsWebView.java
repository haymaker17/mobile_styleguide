/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.request.activity;

import android.content.res.Configuration;

import com.concur.core.R;
import com.concur.mobile.core.activity.Html5WebViewActivity;

/**
 * Launches a <code>WebView</code> to display list of trips to approve.
 * 
 * @author Chris N. Diaz
 * 
 */
public class TravelRequestApprovalsWebView extends Html5WebViewActivity {

    private final static String HASH = "#mobile?pageId=travel-request-approvals-list-page&";

    /**
     * Default constructor with caching enabled.
     */
    public TravelRequestApprovalsWebView() {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.Html5WebViewActivity#getUrl()
     */
    @Override
    protected String getUrl() {
        return getServerUrl() + ENDPOINT + HASH + buildSessionIdParam() + "&" + buildLocaleParam();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // MOB-9426 - Don't do anything so WebView won't refresh the page.
        super.onConfigurationChanged(newConfig);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.Html5WebViewActivity#getImageScreenTitle()
     */
    @Override
    protected String getImageScreenTitle() {
        return getResources().getString(R.string.travel_request_attachments_title);
    }

}
