/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.net.URI;
import java.util.ArrayList;

import android.content.Context;
import android.util.DisplayMetrics;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.platform.util.Format;

/**
 * Models offer content that has been encoded in XML.
 */
public class OfferContent {

    public class Link {

        public String title;
        public String actionUrl;

        protected Link(String title, String actionUrl) {
            this.title = title;
            this.actionUrl = actionUrl;
        }
    }

    public static final String ACTION_WEB = "WEB_LINK";
    public static final String ACTION_APP = "APP_LINK";
    public static final String ACTION_NULL = "NULL_LINK";
    public static final String ACTION_MULTI = "MULTI_LINK";
    public static final String ACTION_SITE = "SITE_LINK";

    public static final String APP_TAXIMAGIC = "TaxiMagic";

    // Contains the offer title.
    public String title;

    // Contains the offer vendor.
    public String vendor;

    // Contains the vendor logo image name.
    public String imageName;

    // Contains the action, i.e., 'WEB_LINK', 'APP_LINK', etc.
    public String action;

    // The list of action URLs
    public ArrayList<Link> actionLinks = new ArrayList<Link>();

    // Contains the name of the application that should be launched in the
    // case 'action' has a value of 'APP_LINK'. I.e., this is used to identify
    // the name of the application to be launched.
    public String offerApplication;

    public String geoLat;
    public String geoLon;
    public boolean fuelMap;

    public void addLink(String title, String url) {
        addLink(new Link(title, url));
    }

    public void addLink(Link link) {
        actionLinks.add(link);
    }

    public URI getVendorImageURI(Context ctx) {
        URI imageURI = null;

        if (imageName != null) {
            String densityName;
            int density = ctx.getResources().getDisplayMetrics().densityDpi;
            if (density <= DisplayMetrics.DENSITY_MEDIUM) {
                densityName = "mdpi";
            } else {
                densityName = "hdpi";
            }

            StringBuilder sb = new StringBuilder(Format.formatServerAddress(false, Preferences.getServerAddress()))
                    .append("/images/mobile/intouch/android/");
            sb.append(densityName).append('/').append(imageName).append(".png");

            imageURI = URI.create(sb.toString());
        }

        return imageURI;
    }

}
