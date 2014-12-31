package com.concur.mobile.core.travel.activity;

import android.content.res.Configuration;

import com.concur.core.R;
import com.concur.mobile.core.activity.Html5WebViewActivity;

/**
 * @author sunill
 * 
 *         This Class will show the detail information about the Agency.
 * */
public class AgencyInformation extends Html5WebViewActivity {

    private final static String HASH = "#mobile?pageId=travel-agency-info-page&";

    public AgencyInformation() {
        super(true);
    }

    // URL to redirect TODO Get URL From Chris.
    @Override
    protected String getUrl() {
        return getServerUrl() + ENDPOINT + HASH + buildSessionIdParam() + "&" + buildLocaleParam();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected String getImageScreenTitle() {
        // TODO Ask Chris and Andy for Title. Should we required title?
        // return getResources().getString(R.string.travel_request_attachments_title);
        CharSequence charSequence = getText(R.string.travel_agency_information);
        return (charSequence != null) ? (charSequence.toString()) : "";
    }

}
