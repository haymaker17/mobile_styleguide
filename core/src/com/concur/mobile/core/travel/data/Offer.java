/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.activity.OfferWebView;
import com.concur.mobile.core.travel.data.OfferContent.Link;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * Models an itinerary-based travel offering.
 */
public class Offer {

    public String id;

    public OfferSegmentLink segmentLink;

    public OfferValidity validity;

    public OfferContent content;

    public String description;

    public boolean isValid() {

        if (!Preferences.shouldCheckOfferValidity()) {
            return true;
        }

        boolean validTime = false;
        boolean validLoc = false;

        ConcurCore app = (ConcurCore) ConcurCore.getContext();

        // Check time
        List<TimeRange> timeRanges = validity.timeRanges;
        if (timeRanges != null && timeRanges.size() > 0) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            for (TimeRange tr : timeRanges) {
                if (tr.inRange(now)) {
                    validTime = true;
                    break;
                }
            }
        } else {
            // If there are no validity elements for time then we are valid
            validTime = true;
        }

        // Check location
        if (validTime) {
            List<ValidLocation> locations = validity.locations;
            if (locations != null && locations.size() > 0) {
                Location loc = app.getCurrentLocation();
                if (loc != null) {
                    for (ValidLocation vl : locations) {
                        float dst = loc.distanceTo(vl.asLocation());
                        if (dst <= (vl.proximity * 1000)) {
                            validLoc = true;
                            break;
                        }
                    }
                }
            } else {
                // If there are no validity elements for location then we are valid
                validLoc = true;
            }
        }

        return validTime && validLoc;
    }

    public Intent getOfferLaunchIntent(Context launchContext, String itinLocator) {

        Intent i = null;

        if (content != null && content.action != null) {
            if (OfferContent.ACTION_WEB.equals(content.action)) {
                // There should only be one link. Use it.
                if (content.actionLinks.size() == 1) {
                    i = new Intent(Intent.ACTION_VIEW, Uri.parse(content.actionLinks.get(0).actionUrl));
                }
            } else if (OfferContent.ACTION_MULTI.equals(content.action)) {
                if (segmentLink != null && segmentLink.segment != null) {
                    // i = new Intent(launchContext, OfferList.class);
                    // i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                    // i.putExtra(Const.EXTRA_OFFER_ID, this.id);
                }
            } else if (OfferContent.ACTION_NULL.equals(content.action)) {
                if (content.fuelMap && ViewUtil.isMappingAvailable(launchContext)) {
                    String uri = new StringBuilder("geo:").append(content.geoLat).append(',').append(content.geoLon)
                            .append("?q=").append("gas station").toString();
                    i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                }
            } else if (OfferContent.ACTION_APP.equals(content.action)) {
                if (OfferContent.APP_TAXIMAGIC.equalsIgnoreCase(content.offerApplication)) {
                    i = getTaxiMagicIntent(launchContext);
                }
            } else if (OfferContent.ACTION_SITE.equals(content.action)) {
                // There should only be one link. Use it.
                if (content.actionLinks.size() > 0) {
                    // Need to get the SITE_URL (which begins with /).
                    String actionUrl = "";
                    String querySeparator = "";
                    for (Link link : content.actionLinks) {
                        if (link.actionUrl.startsWith("/")) {
                            actionUrl = link.actionUrl;
                            // Also need to check if there already is a ? query in the URL.
                            if (actionUrl.indexOf("?") == -1) {
                                querySeparator = "?";
                            } else {
                                querySeparator = "&";
                            }

                            break;
                        }
                    }

                    // Construct the URL.
                    String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
                    StringBuffer url = new StringBuffer();
                    url.append(serverAdd).append(actionUrl).append(querySeparator).append("sessionId=")
                            .append(Preferences.getSessionId()).append("&sessionType=concurMobile");

                    // TODO: Need to check other tags to see whether or not to launch
                    // in WebView or in external browser.
                    i = new Intent(launchContext, OfferWebView.class);
                    i.putExtra(OfferWebView.ACTION_URL, url.toString());
                    i.putExtra(OfferWebView.TITLE, launchContext.getString(R.string.general_offer));
                }
            }
        }

        return i;
    }

    private Intent getTaxiMagicIntent(Context context) {

        Intent tm = null;

        PackageManager pm = context.getPackageManager();

        Intent main = new Intent(Intent.ACTION_MAIN, null);

        main.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> launchables = pm.queryIntentActivities(main, 0);

        for (int i = 0; i < launchables.size(); i++) {
            ResolveInfo launchable = launchables.get(i);
            ActivityInfo act = launchable.activityInfo;
            if (act.applicationInfo.packageName.equals("com.ridecharge.android.taximagic")) {
                tm = new Intent(Intent.ACTION_MAIN);
                tm.addCategory(Intent.CATEGORY_LAUNCHER);
                tm.setComponent(new ComponentName(act.applicationInfo.packageName, act.name));
            }
        }

        if (tm == null) {
            tm = new Intent(Intent.ACTION_VIEW, Uri.parse("http://taximagic.com"));
        }

        return tm;
    }

    protected static class OfferSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = OfferSAXHandler.class.getSimpleName();

        private static final String OFFER = "Offer";

        private static final String LINKS = "Links";
        private static final String LINK = "Link";
        private static final String SEGMENT = "Segment";
        private static final String VALIDITY = "Validity";
        private static final String TIME_RANGE = "TimeRange";
        private static final String LOCAL_PROXIMITY = "LocalProximity";
        private static final String LONGITUDE = "Longitude";
        private static final String LATITUDE = "Latitude";
        private static final String PROXIMITY = "proximity";
        private static final String OFFER_CONTENT = "OfferContent";
        private static final String OFFER_APPLICATION = "OfferApplication";
        private static final String OFFER_DESCRIPTION = "OfferDescription";
        private static final String TITLE = "Title";
        private static final String OFFER_VENDOR = "OfferVendor";
        private static final String OFFER_ACTION = "OfferAction";
        private static final String ACTION_URL = "ActionURL";
        private static final String IMAGE_NAME = "ImageName";
        private static final String ID = "Id";
        private static final String END_DATE_TIME_UTC = "endDateTimeUTC";
        private static final String START_DATE_TIME_UTC = "startDateTimeUTC";

        private static final String GEO_LINK = "GeoLink";
        private static final String OVERLAY = "Overlay";
        private static final String GAS_STATION = "GasStation";

        private static final String SEGMENT_SIDE = "SegmentSide";
        private static final String BOOKING_SOURCE = "BookingSource";
        private static final String RECORD_LOCATOR = "RecordLocator";
        private static final String SEGMENT_KEY = "SegmentKey";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the booleans will be a lot faster.
        private boolean inOffer;
        private boolean inValidity;
        private boolean inContent;
        private boolean inGeoLink;
        private boolean inLinks;

        // Holder for our parsed data
        private ArrayList<Offer> offers;
        private Offer offer;
        private TimeRange timeRange;
        private ValidLocation location;
        private String linkTitle;
        private String linkUrl;

        public OfferSAXHandler() {
            offers = new ArrayList<Offer>();
        }

        public ArrayList<Offer> getOffers() {
            return offers;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            chars.append(ch, start, length);
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement().
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(OFFER)) {
                offer = new Offer();
                inOffer = true;
            } else if (inOffer) {
                if (offer != null) {
                    if (inContent) {
                        if (localName.equalsIgnoreCase(LINKS)) {
                            inLinks = true;
                        } else if (localName.equalsIgnoreCase(GEO_LINK)) {
                            inGeoLink = true;
                        }
                    } else if (localName.equalsIgnoreCase(OFFER_CONTENT)) {
                        inContent = true;
                        offer.content = new OfferContent();
                    } else if (inValidity) {
                        if (offer.validity == null) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: offer.validity is null! " + offer.id);
                        } else {
                            if (localName.equalsIgnoreCase(TIME_RANGE)) {
                                timeRange = new TimeRange();
                            } else if (localName.equalsIgnoreCase(LOCAL_PROXIMITY)) {
                                location = new ValidLocation();
                            }
                        }
                    } else if (localName.equalsIgnoreCase(VALIDITY)) {
                        inValidity = true;
                        offer.validity = new OfferValidity();
                    } else if (localName.equalsIgnoreCase(LINK)) {
                        offer.segmentLink = new OfferSegmentLink();
                    } else if (localName.equalsIgnoreCase(SEGMENT)) {
                        if (offer.segmentLink != null) {
                            if (attributes != null) {
                                offer.segmentLink.bookingSource = attributes.getValue(BOOKING_SOURCE);
                                if (offer.segmentLink.bookingSource == null) {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".startElement: Segment node missing 'BookingSource' attribute!");
                                }
                                offer.segmentLink.recordLocator = attributes.getValue(RECORD_LOCATOR);
                                if (offer.segmentLink.recordLocator == null) {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".startElement: Segment node missing 'RecordLocator' attribute!");
                                }
                                offer.segmentLink.segmentKey = attributes.getValue(SEGMENT_KEY);
                                if (offer.segmentLink.segmentKey == null) {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".startElement: Segment node missing 'SegmentKey' attribute!");
                                }
                                offer.segmentLink.setSegmentSide(attributes.getValue(SEGMENT_SIDE));
                                if (offer.segmentLink.segmentSide == OfferSegmentLink.SIDE_NONE) {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".startElement: Segment node missing 'SegmentSide' attribute!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: Segment node missing attributes!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: offer.link is null!");
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startElement: offer is null!");
                }
            }
        }

        /**
         * Handle the closing of all elements.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (inOffer) {
                if (offer != null) {
                    final String cleanChars = chars.toString().trim();

                    if (inContent) {
                        if (offer.content == null) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: offer.content is null! " + offer.id);
                        } else if (localName.equalsIgnoreCase(OFFER_CONTENT)) {
                            inContent = false;
                        } else if (inLinks) {
                            if (localName.equalsIgnoreCase(LINKS)) {
                                inLinks = false;
                            } else if (localName.equalsIgnoreCase(LINK)) {
                                if (linkTitle != null && linkUrl != null) {
                                    offer.content.addLink(linkTitle, linkUrl);
                                }
                                linkTitle = null;
                                linkUrl = null;
                            } else if (localName.equalsIgnoreCase(TITLE)) {
                                linkTitle = cleanChars;
                            } else if (localName.equalsIgnoreCase(ACTION_URL)) {
                                linkUrl = cleanChars;
                            }
                        } else if (inGeoLink) {
                            if (localName.equalsIgnoreCase(GEO_LINK)) {
                                inGeoLink = false;
                            } else if (localName.equalsIgnoreCase(LATITUDE)) {
                                offer.content.geoLat = cleanChars;
                            } else if (localName.equalsIgnoreCase(LONGITUDE)) {
                                offer.content.geoLon = cleanChars;
                            } else if (localName.equalsIgnoreCase(OVERLAY)) {
                                if (GAS_STATION.equals(cleanChars)) {
                                    offer.content.fuelMap = true;
                                }
                            }
                        } else {
                            if (localName.equalsIgnoreCase(TITLE)) {
                                offer.content.title = cleanChars;
                            } else if (localName.equalsIgnoreCase(OFFER_VENDOR)) {
                                offer.content.vendor = cleanChars;
                            } else if (localName.equalsIgnoreCase(OFFER_ACTION)) {
                                offer.content.action = cleanChars;
                            } else if (localName.equalsIgnoreCase(ACTION_URL)) {
                                offer.content.addLink(null, cleanChars);
                            } else if (localName.equalsIgnoreCase(IMAGE_NAME)) {
                                offer.content.imageName = cleanChars;
                            } else if (localName.equalsIgnoreCase(OFFER_APPLICATION)) {
                                offer.content.offerApplication = cleanChars;
                            }
                        }
                    } else if (inValidity) {
                        if (offer.validity == null) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: offer.validity is null! " + offer.id);
                        } else if (localName.equalsIgnoreCase(VALIDITY)) {
                            inValidity = false;
                        } else {
                            if (localName.equalsIgnoreCase(END_DATE_TIME_UTC)) {
                                if (timeRange != null) {
                                    timeRange.endDateTimeUTC = Parse.parseXMLTimestamp(cleanChars);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: timeRange is null!");
                                }
                            } else if (localName.equalsIgnoreCase(START_DATE_TIME_UTC)) {
                                if (timeRange != null) {
                                    timeRange.startDateTimeUTC = Parse.parseXMLTimestamp(cleanChars);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: timeRange is null!");
                                }
                            } else if (localName.equalsIgnoreCase(LONGITUDE)) {
                                if (location != null) {
                                    location.longitude = Parse.safeParseDouble(cleanChars);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: location is null!");
                                }
                            } else if (localName.equalsIgnoreCase(LATITUDE)) {
                                if (location != null) {
                                    location.latitude = Parse.safeParseDouble(cleanChars);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: location is null!");
                                }
                            } else if (localName.equalsIgnoreCase(PROXIMITY)) {
                                if (location != null) {
                                    location.proximity = Parse.safeParseDouble(cleanChars);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: location is null!");
                                }
                            } else if (localName.equalsIgnoreCase(LOCAL_PROXIMITY)) {
                                if (location != null) {
                                    offer.validity.addLocation(location);
                                    location = null;
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: location is null!");
                                }
                            } else if (localName.equalsIgnoreCase(TIME_RANGE)) {
                                if (timeRange != null) {
                                    offer.validity.addTimeRange(timeRange);
                                    timeRange = null;
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: timeRange is null!");
                                }
                            }
                        }
                    } else if (localName.equalsIgnoreCase(OFFER_DESCRIPTION)) {
                        // Set the offer description.
                        offer.description = cleanChars;
                    } else if (localName.equalsIgnoreCase(ID)) {
                        // Set the offer id.
                        offer.id = chars.toString().trim();
                    } else if (localName.equalsIgnoreCase(OFFER)) {
                        // Add to the entire offers list.
                        offers.add(offer);

                        // Reset the offer link.
                        offer = null;
                        inOffer = false;
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: offer is null!");
                }

                chars.setLength(0);
            }

        }
    }

}
