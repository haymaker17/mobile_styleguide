/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.activity;

import com.concur.mobile.core.activity.Html5WebViewActivity;

/**
 * @author Chris N. Diaz
 * 
 */
public class OfferWebView extends Html5WebViewActivity {

    public final static String ACTION_URL = "OFFER_ACTION_URL";
    public final static String TITLE = "OFFER_TITLE";

    /**
     * Default constructor.
     * 
     */
    public OfferWebView() {
        super(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.Html5WebViewActivity#getUrl()
     */
    @Override
    protected String getUrl() {
        return getIntent().getStringExtra(ACTION_URL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.Html5WebViewActivity#getImageScreenTitle()
     */
    @Override
    protected String getImageScreenTitle() {
        return getIntent().getStringExtra(TITLE);
    }

}
