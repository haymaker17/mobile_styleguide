/**
 * 
 */
package com.concur.mobile.platform.travel.provider;

import java.util.HashMap;

import com.concur.mobile.platform.provider.UriMatcherInfo;

/**
 * This class provides utility methods used by the Travel Provider class.
 */
public class TravelProviderUtil {

    /**
     * Will initialize the Trip projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initTripProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.TripColumns._ID, Travel.TripColumns._ID);
        retVal.put(Travel.TripColumns._COUNT, Travel.TripColumns._COUNT);

        retVal.put(Travel.TripColumns.CLIENT_LOCATOR, Travel.TripColumns.CLIENT_LOCATOR);
        retVal.put(Travel.TripColumns.CLIQBOOK_TRIP_ID, Travel.TripColumns.CLIQBOOK_TRIP_ID);
        retVal.put(Travel.TripColumns.DESCRIPTION, Travel.TripColumns.DESCRIPTION);
        retVal.put(Travel.TripColumns.END_DATE_LOCAL, Travel.TripColumns.END_DATE_LOCAL);
        retVal.put(Travel.TripColumns.END_DATE_UTC, Travel.TripColumns.END_DATE_UTC);
        retVal.put(Travel.TripColumns.ITIN_LOCATOR, Travel.TripColumns.ITIN_LOCATOR);
        retVal.put(Travel.TripColumns.RECORD_LOCATOR, Travel.TripColumns.RECORD_LOCATOR);
        retVal.put(Travel.TripColumns.START_DATE_LOCAL, Travel.TripColumns.START_DATE_LOCAL);
        retVal.put(Travel.TripColumns.START_DATE_UTC, Travel.TripColumns.START_DATE_UTC);
        retVal.put(Travel.TripColumns.STATE, Travel.TripColumns.STATE);
        retVal.put(Travel.TripColumns.TRIP_NAME, Travel.TripColumns.TRIP_NAME);
        retVal.put(Travel.TripColumns.ALLOW_ADD_AIR, Travel.TripColumns.ALLOW_ADD_AIR);
        retVal.put(Travel.TripColumns.ALLOW_ADD_CAR, Travel.TripColumns.ALLOW_ADD_CAR);
        retVal.put(Travel.TripColumns.ALLOW_ADD_HOTEL, Travel.TripColumns.ALLOW_ADD_HOTEL);
        retVal.put(Travel.TripColumns.ALLOW_ADD_RAIL, Travel.TripColumns.ALLOW_ADD_RAIL);
        retVal.put(Travel.TripColumns.ALLOW_CANCEL, Travel.TripColumns.ALLOW_CANCEL);
        retVal.put(Travel.TripColumns.USER_ID, Travel.TripColumns.USER_ID);
        return retVal;
    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Trip</code>.
     */
    public static UriMatcherInfo initTripsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.TripColumns.CONTENT_TYPE;
        info.tableName = Travel.TripColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripColumns.USER_ID;
        info.contentIdUriBase = Travel.TripColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.TripColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.tripProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Trip</code>.
     */
    public static UriMatcherInfo initTripUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.TripColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.TripColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripColumns.USER_ID;
        info.contentIdUriBase = Travel.TripColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.TripColumns._ID;
        info.projectionMap = TravelProvider.tripProjectionMap;
        info.defaultSortOrder = Travel.TripColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.TripColumns.TRIP_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Enhancement Day projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initEnhancementDayProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.EnhancementDayColumns._ID, Travel.EnhancementDayColumns._ID);
        retVal.put(Travel.EnhancementDayColumns._COUNT, Travel.EnhancementDayColumns._COUNT);
        retVal.put(Travel.EnhancementDayColumns.TRIP_ID, Travel.EnhancementDayColumns.TRIP_ID);
        retVal.put(Travel.EnhancementDayColumns.TYPE, Travel.EnhancementDayColumns.TYPE);
        retVal.put(Travel.EnhancementDayColumns.TRIP_LOCAL_DATE, Travel.EnhancementDayColumns.TRIP_LOCAL_DATE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Enhancement Day</code>.
     */
    public static UriMatcherInfo initEnhancementDaysUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.EnhancementDayColumns.CONTENT_TYPE;
        info.tableName = Travel.EnhancementDayColumns.TABLE_NAME;
        info.nullColumnName = Travel.EnhancementDayColumns.TYPE;
        info.contentIdUriBase = Travel.EnhancementDayColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.EnhancementDayColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.enhancementDayProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Enhancement Day</code>.
     */
    public static UriMatcherInfo initEnhancementDayUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.EnhancementDayColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.EnhancementDayColumns.TABLE_NAME;
        info.nullColumnName = Travel.EnhancementDayColumns.TYPE;
        info.contentIdUriBase = Travel.EnhancementDayColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.EnhancementDayColumns._ID;
        info.projectionMap = TravelProvider.enhancementDayProjectionMap;
        info.defaultSortOrder = Travel.EnhancementDayColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.EnhancementDayColumns.ENHANCEMENT_DAY_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Sortable Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initSortableSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.SortableSegmentColumns._ID, Travel.SortableSegmentColumns._ID);
        retVal.put(Travel.SortableSegmentColumns._COUNT, Travel.SortableSegmentColumns._COUNT);
        retVal.put(Travel.SortableSegmentColumns.ENHANCEMENT_DAY_ID, Travel.SortableSegmentColumns.ENHANCEMENT_DAY_ID);
        retVal.put(Travel.SortableSegmentColumns.BOOKING_SOURCE, Travel.SortableSegmentColumns.BOOKING_SOURCE);
        retVal.put(Travel.SortableSegmentColumns.RECORD_LOCATOR, Travel.SortableSegmentColumns.RECORD_LOCATOR);
        retVal.put(Travel.SortableSegmentColumns.SEGMENT_KEY, Travel.SortableSegmentColumns.SEGMENT_KEY);
        retVal.put(Travel.SortableSegmentColumns.SEGMENT_SIDE, Travel.SortableSegmentColumns.SEGMENT_SIDE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Sortable Segment</code>.
     */
    public static UriMatcherInfo initSortableSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.SortableSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.SortableSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.SortableSegmentColumns.SEGMENT_SIDE;
        info.contentIdUriBase = Travel.SortableSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.SortableSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.sortableSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Sortable Segment</code>.
     */
    public static UriMatcherInfo initSortableSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.SortableSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.SortableSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.SortableSegmentColumns.SEGMENT_SIDE;
        info.contentIdUriBase = Travel.SortableSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.SortableSegmentColumns._ID;
        info.projectionMap = TravelProvider.sortableSegmentProjectionMap;
        info.defaultSortOrder = Travel.SortableSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.SortableSegmentColumns.SORTABLE_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Enhancement Offer projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initEnhancementOfferProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.EnhancementOfferColumns._ID, Travel.EnhancementOfferColumns._ID);
        retVal.put(Travel.EnhancementOfferColumns._COUNT, Travel.EnhancementOfferColumns._COUNT);

        retVal.put(Travel.EnhancementOfferColumns.TRIP_ID, Travel.EnhancementOfferColumns.TRIP_ID);
        retVal.put(Travel.EnhancementOfferColumns.ID, Travel.EnhancementOfferColumns.ID);
        retVal.put(Travel.EnhancementOfferColumns.DESCRIPTION, Travel.EnhancementOfferColumns.DESCRIPTION);
        retVal.put(Travel.EnhancementOfferColumns.TYPE, Travel.EnhancementOfferColumns.TYPE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Enhancement Offer</code>.
     */
    public static UriMatcherInfo initEnhancementOffersUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.EnhancementOfferColumns.CONTENT_TYPE;
        info.tableName = Travel.EnhancementOfferColumns.TABLE_NAME;
        info.nullColumnName = Travel.EnhancementOfferColumns.TYPE;
        info.contentIdUriBase = Travel.EnhancementOfferColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.EnhancementOfferColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.enhancementOfferProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Enhancement Offer</code>.
     */
    public static UriMatcherInfo initEnhancementOfferUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.EnhancementOfferColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.EnhancementOfferColumns.TABLE_NAME;
        info.nullColumnName = Travel.EnhancementOfferColumns.TYPE;
        info.contentIdUriBase = Travel.EnhancementOfferColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.EnhancementOfferColumns._ID;
        info.projectionMap = TravelProvider.enhancementOfferProjectionMap;
        info.defaultSortOrder = Travel.EnhancementOfferColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.EnhancementOfferColumns.ENHANCEMENT_OFFER_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Offer Link projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initOfferLinkProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.OfferLinkColumns._ID, Travel.OfferLinkColumns._ID);
        retVal.put(Travel.OfferLinkColumns._COUNT, Travel.OfferLinkColumns._COUNT);

        retVal.put(Travel.OfferLinkColumns.ENHANCEMENT_OFFER_ID, Travel.OfferLinkColumns.ENHANCEMENT_OFFER_ID);
        retVal.put(Travel.OfferLinkColumns.BOOKING_SOURCE, Travel.OfferLinkColumns.BOOKING_SOURCE);
        retVal.put(Travel.OfferLinkColumns.RECORD_LOCATOR, Travel.OfferLinkColumns.RECORD_LOCATOR);
        retVal.put(Travel.OfferLinkColumns.SEGMENT_KEY, Travel.OfferLinkColumns.SEGMENT_KEY);
        retVal.put(Travel.OfferLinkColumns.SEGMENT_SIDE, Travel.OfferLinkColumns.SEGMENT_SIDE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Offer Link</code>.
     */
    public static UriMatcherInfo initOfferLinksUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.OfferLinkColumns.CONTENT_TYPE;
        info.tableName = Travel.OfferLinkColumns.TABLE_NAME;
        info.nullColumnName = Travel.OfferLinkColumns.SEGMENT_SIDE;
        info.contentIdUriBase = Travel.OfferLinkColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.OfferLinkColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.offerLinkProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Offer Link</code>.
     */
    public static UriMatcherInfo initOfferLinkUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.OfferLinkColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.OfferLinkColumns.TABLE_NAME;
        info.nullColumnName = Travel.OfferLinkColumns.SEGMENT_SIDE;
        info.contentIdUriBase = Travel.OfferLinkColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.OfferLinkColumns._ID;
        info.projectionMap = TravelProvider.offerLinkProjectionMap;
        info.defaultSortOrder = Travel.OfferLinkColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.OfferLinkColumns.OFFER_LINK_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Offer Content projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initOfferContentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.OfferContentColumns._ID, Travel.OfferContentColumns._ID);
        retVal.put(Travel.OfferContentColumns._COUNT, Travel.OfferContentColumns._COUNT);

        retVal.put(Travel.OfferContentColumns.ENHANCEMENT_OFFER_ID, Travel.OfferContentColumns.ENHANCEMENT_OFFER_ID);
        retVal.put(Travel.OfferContentColumns.TITLE, Travel.OfferContentColumns.TITLE);
        retVal.put(Travel.OfferContentColumns.VENDOR, Travel.OfferContentColumns.VENDOR);
        retVal.put(Travel.OfferContentColumns.ACTION, Travel.OfferContentColumns.ACTION);
        retVal.put(Travel.OfferContentColumns.APPLICATION, Travel.OfferContentColumns.APPLICATION);
        retVal.put(Travel.OfferContentColumns.IMAGE_NAME, Travel.OfferContentColumns.IMAGE_NAME);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Offer Content</code>.
     */
    public static UriMatcherInfo initOfferContentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.OfferContentColumns.CONTENT_TYPE;
        info.tableName = Travel.OfferContentColumns.TABLE_NAME;
        info.nullColumnName = Travel.OfferContentColumns.IMAGE_NAME;
        info.contentIdUriBase = Travel.OfferContentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.OfferContentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.offerContentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Offer Content</code>.
     */
    public static UriMatcherInfo initOfferContentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.OfferContentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.OfferContentColumns.TABLE_NAME;
        info.nullColumnName = Travel.OfferContentColumns.IMAGE_NAME;
        info.contentIdUriBase = Travel.OfferContentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.OfferContentColumns._ID;
        info.projectionMap = TravelProvider.offerContentProjectionMap;
        info.defaultSortOrder = Travel.OfferContentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.OfferContentColumns.OFFER_CONTENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Content Link projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initContentLinkProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.ContentLinkColumns._ID, Travel.ContentLinkColumns._ID);
        retVal.put(Travel.ContentLinkColumns._COUNT, Travel.ContentLinkColumns._COUNT);

        retVal.put(Travel.ContentLinkColumns.OFFER_CONTENT_ID, Travel.ContentLinkColumns.OFFER_CONTENT_ID);
        retVal.put(Travel.ContentLinkColumns.TITLE, Travel.ContentLinkColumns.TITLE);
        retVal.put(Travel.ContentLinkColumns.ACTION_URL, Travel.ContentLinkColumns.ACTION_URL);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Content Link</code>.
     */
    public static UriMatcherInfo initContentLinksUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.ContentLinkColumns.CONTENT_TYPE;
        info.tableName = Travel.ContentLinkColumns.TABLE_NAME;
        info.nullColumnName = Travel.ContentLinkColumns.TITLE;
        info.contentIdUriBase = Travel.ContentLinkColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.ContentLinkColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.contentLinkProjectionMap;

        return info;
    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Content Link</code>.
     */
    public static UriMatcherInfo initContentLinkUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.ContentLinkColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.ContentLinkColumns.TABLE_NAME;
        info.nullColumnName = Travel.ContentLinkColumns.TITLE;
        info.contentIdUriBase = Travel.ContentLinkColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.ContentLinkColumns._ID;
        info.projectionMap = TravelProvider.contentLinkProjectionMap;
        info.defaultSortOrder = Travel.ContentLinkColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.ContentLinkColumns.CONTENT_LINK_ID_PATH_POSITION;
        return info;
    }

    /**
     * Will initialize the Map Display projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initMapDisplayProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.MapDisplayColumns._ID, Travel.MapDisplayColumns._ID);
        retVal.put(Travel.MapDisplayColumns._COUNT, Travel.MapDisplayColumns._COUNT);

        retVal.put(Travel.MapDisplayColumns.OFFER_CONTENT_ID, Travel.MapDisplayColumns.OFFER_CONTENT_ID);
        retVal.put(Travel.MapDisplayColumns.LATITUDE, Travel.MapDisplayColumns.LATITUDE);
        retVal.put(Travel.MapDisplayColumns.LONGITUDE, Travel.MapDisplayColumns.LONGITUDE);
        retVal.put(Travel.MapDisplayColumns.DIMENSION_KM, Travel.MapDisplayColumns.DIMENSION_KM);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Map Display</code>.
     */
    public static UriMatcherInfo initMapDisplaysUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.MapDisplayColumns.CONTENT_TYPE;
        info.tableName = Travel.MapDisplayColumns.TABLE_NAME;
        info.nullColumnName = Travel.MapDisplayColumns.DIMENSION_KM;
        info.contentIdUriBase = Travel.MapDisplayColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.MapDisplayColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.mapDisplayProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Map Display</code>.
     */
    public static UriMatcherInfo initMapDisplayUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.MapDisplayColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.MapDisplayColumns.TABLE_NAME;
        info.nullColumnName = Travel.MapDisplayColumns.DIMENSION_KM;
        info.contentIdUriBase = Travel.MapDisplayColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.MapDisplayColumns._ID;
        info.projectionMap = TravelProvider.mapDisplayProjectionMap;
        info.defaultSortOrder = Travel.MapDisplayColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.MapDisplayColumns.MAP_DISPLAY_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Display Overlay projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initDisplayOverlayProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.DisplayOverlayColumns._ID, Travel.DisplayOverlayColumns._ID);
        retVal.put(Travel.DisplayOverlayColumns._COUNT, Travel.DisplayOverlayColumns._COUNT);

        retVal.put(Travel.DisplayOverlayColumns.MAP_DISPLAY_ID, Travel.DisplayOverlayColumns.MAP_DISPLAY_ID);
        retVal.put(Travel.DisplayOverlayColumns.NAME, Travel.DisplayOverlayColumns.NAME);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Display Overlay</code>.
     */
    public static UriMatcherInfo initDisplayOverlaysUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.DisplayOverlayColumns.CONTENT_TYPE;
        info.tableName = Travel.DisplayOverlayColumns.TABLE_NAME;
        info.nullColumnName = Travel.DisplayOverlayColumns.NAME;
        info.contentIdUriBase = Travel.DisplayOverlayColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.DisplayOverlayColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.displayOverlayProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Display Overlay</code>.
     */
    public static UriMatcherInfo initDisplayOverlayUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.DisplayOverlayColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.DisplayOverlayColumns.TABLE_NAME;
        info.nullColumnName = Travel.DisplayOverlayColumns.NAME;
        info.contentIdUriBase = Travel.DisplayOverlayColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.DisplayOverlayColumns._ID;
        info.projectionMap = TravelProvider.displayOverlayProjectionMap;
        info.defaultSortOrder = Travel.DisplayOverlayColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.DisplayOverlayColumns.DISPLAY_OVERLAY_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Validity Location projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initValidityLocationProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.ValidityLocationColumns._ID, Travel.ValidityLocationColumns._ID);
        retVal.put(Travel.ValidityLocationColumns._COUNT, Travel.ValidityLocationColumns._COUNT);

        retVal.put(Travel.ValidityLocationColumns.ENHANCEMENT_OFFER_ID,
                Travel.ValidityLocationColumns.ENHANCEMENT_OFFER_ID);
        retVal.put(Travel.ValidityLocationColumns.LATITUDE, Travel.ValidityLocationColumns.LATITUDE);
        retVal.put(Travel.ValidityLocationColumns.LONGITUDE, Travel.ValidityLocationColumns.LONGITUDE);
        retVal.put(Travel.ValidityLocationColumns.PROXIMITY, Travel.ValidityLocationColumns.PROXIMITY);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Validity Location</code>.
     */
    public static UriMatcherInfo initValidityLocationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.ValidityLocationColumns.CONTENT_TYPE;
        info.tableName = Travel.ValidityLocationColumns.TABLE_NAME;
        info.nullColumnName = Travel.ValidityLocationColumns.PROXIMITY;
        info.contentIdUriBase = Travel.ValidityLocationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.ValidityLocationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.validityLocationProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Validity Location</code>.
     */
    public static UriMatcherInfo initValidityLocationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.ValidityLocationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.ValidityLocationColumns.TABLE_NAME;
        info.nullColumnName = Travel.ValidityLocationColumns.PROXIMITY;
        info.contentIdUriBase = Travel.ValidityLocationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.ValidityLocationColumns._ID;
        info.projectionMap = TravelProvider.validityLocationProjectionMap;
        info.defaultSortOrder = Travel.ValidityLocationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.ValidityLocationColumns.VALIDITY_LOCATION_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Validity Time Range projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initValidityTimeRangeProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.ValidityTimeRangeColumns._ID, Travel.ValidityTimeRangeColumns._ID);
        retVal.put(Travel.ValidityTimeRangeColumns._COUNT, Travel.ValidityTimeRangeColumns._COUNT);

        retVal.put(Travel.ValidityTimeRangeColumns.ENHANCEMENT_OFFER_ID,
                Travel.ValidityTimeRangeColumns.ENHANCEMENT_OFFER_ID);
        retVal.put(Travel.ValidityTimeRangeColumns.START_DATE_TIME_UTC,
                Travel.ValidityTimeRangeColumns.START_DATE_TIME_UTC);
        retVal.put(Travel.ValidityTimeRangeColumns.END_DATE_TIME_UTC, Travel.ValidityTimeRangeColumns.END_DATE_TIME_UTC);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Validity Time Range</code>.
     */
    public static UriMatcherInfo initValidityTimeRangesUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.ValidityTimeRangeColumns.CONTENT_TYPE;
        info.tableName = Travel.ValidityTimeRangeColumns.TABLE_NAME;
        info.nullColumnName = Travel.ValidityTimeRangeColumns.END_DATE_TIME_UTC;
        info.contentIdUriBase = Travel.ValidityTimeRangeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.ValidityTimeRangeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.validityTimeRangeProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Validity Time Range</code>.
     */
    public static UriMatcherInfo initValidityTimeRangeUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.ValidityTimeRangeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.ValidityTimeRangeColumns.TABLE_NAME;
        info.nullColumnName = Travel.ValidityTimeRangeColumns.END_DATE_TIME_UTC;
        info.contentIdUriBase = Travel.ValidityTimeRangeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.ValidityTimeRangeColumns._ID;
        info.projectionMap = TravelProvider.validityTimeRangeProjectionMap;
        info.defaultSortOrder = Travel.ValidityTimeRangeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.ValidityTimeRangeColumns.VALIDITY_TIME_RANGE_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Trip Rule Violation projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initTripRuleViolationProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.TripRuleViolationColumns._ID, Travel.TripRuleViolationColumns._ID);
        retVal.put(Travel.TripRuleViolationColumns._COUNT, Travel.TripRuleViolationColumns._COUNT);

        retVal.put(Travel.TripRuleViolationColumns.TRIP_ID, Travel.TripRuleViolationColumns.TRIP_ID);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Trip Rule Violation</code>.
     */
    public static UriMatcherInfo initTripRuleViolationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.TripRuleViolationColumns.CONTENT_TYPE;
        info.tableName = Travel.TripRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripRuleViolationColumns.TRIP_ID;
        info.contentIdUriBase = Travel.TripRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.TripRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.tripRuleViolationProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Trip Rule Violation</code>.
     */
    public static UriMatcherInfo initTripRuleViolationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.TripRuleViolationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.TripRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.TripRuleViolationColumns.TRIP_ID;
        info.contentIdUriBase = Travel.TripRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.TripRuleViolationColumns._ID;
        info.projectionMap = TravelProvider.tripRuleViolationProjectionMap;
        info.defaultSortOrder = Travel.TripRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.TripRuleViolationColumns.TRIP_RULE_VIOLATION_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Car Rule Violation projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initCarRuleViolationProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.CarRuleViolationColumns._ID, Travel.CarRuleViolationColumns._ID);
        retVal.put(Travel.CarRuleViolationColumns._COUNT, Travel.CarRuleViolationColumns._COUNT);

        retVal.put(Travel.CarRuleViolationColumns.TRIP_ID, Travel.CarRuleViolationColumns.TRIP_ID);
        retVal.put(Travel.CarRuleViolationColumns.DAILY_RATE, Travel.CarRuleViolationColumns.DAILY_RATE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Car Rule Violation</code>.
     */
    public static UriMatcherInfo initCarRuleViolationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.CarRuleViolationColumns.CONTENT_TYPE;
        info.tableName = Travel.CarRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.CarRuleViolationColumns.DAILY_RATE;
        info.contentIdUriBase = Travel.CarRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.CarRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.carRuleViolationProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Car Rule Violation</code>.
     */
    public static UriMatcherInfo initCarRuleViolationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.CarRuleViolationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.CarRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.CarRuleViolationColumns.DAILY_RATE;
        info.contentIdUriBase = Travel.CarRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.CarRuleViolationColumns._ID;
        info.projectionMap = TravelProvider.carRuleViolationProjectionMap;
        info.defaultSortOrder = Travel.CarRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.CarRuleViolationColumns.CAR_RULE_VIOLATION_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Hotel Rule Violation projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelRuleViolationProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelRuleViolationColumns._ID, Travel.HotelRuleViolationColumns._ID);
        retVal.put(Travel.HotelRuleViolationColumns._COUNT, Travel.HotelRuleViolationColumns._COUNT);

        retVal.put(Travel.HotelRuleViolationColumns.TRIP_ID, Travel.HotelRuleViolationColumns.TRIP_ID);
        retVal.put(Travel.HotelRuleViolationColumns.RATE, Travel.HotelRuleViolationColumns.RATE);
        retVal.put(Travel.HotelRuleViolationColumns.NAME, Travel.HotelRuleViolationColumns.NAME);
        retVal.put(Travel.HotelRuleViolationColumns.ADDRESS, Travel.HotelRuleViolationColumns.ADDRESS);
        retVal.put(Travel.HotelRuleViolationColumns.DESCRIPTION, Travel.HotelRuleViolationColumns.DESCRIPTION);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Hotel Rule Violation</code>.
     */
    public static UriMatcherInfo initHotelRuleViolationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelRuleViolationColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.HotelRuleViolationColumns.NAME;
        info.contentIdUriBase = Travel.HotelRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.HotelRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.hotelRuleViolationProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Hotel Rule Violation</code>.
     */
    public static UriMatcherInfo initHotelRuleViolationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelRuleViolationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.HotelRuleViolationColumns.NAME;
        info.contentIdUriBase = Travel.HotelRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelRuleViolationColumns._ID;
        info.projectionMap = TravelProvider.hotelRuleViolationProjectionMap;
        info.defaultSortOrder = Travel.HotelRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.HotelRuleViolationColumns.HOTEL_RULE_VIOLATION_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Flight Rule Violation projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initFlightRuleViolationProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.FlightRuleViolationColumns._ID, Travel.FlightRuleViolationColumns._ID);
        retVal.put(Travel.FlightRuleViolationColumns._COUNT, Travel.FlightRuleViolationColumns._COUNT);

        retVal.put(Travel.FlightRuleViolationColumns.TRIP_ID, Travel.FlightRuleViolationColumns.TRIP_ID);
        retVal.put(Travel.FlightRuleViolationColumns.REFUNDABLE, Travel.FlightRuleViolationColumns.REFUNDABLE);
        retVal.put(Travel.FlightRuleViolationColumns.COST, Travel.FlightRuleViolationColumns.COST);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Flight Rule Violation</code>.
     */
    public static UriMatcherInfo initFlightRuleViolationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.FlightRuleViolationColumns.CONTENT_TYPE;
        info.tableName = Travel.FlightRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.FlightRuleViolationColumns.COST;
        info.contentIdUriBase = Travel.FlightRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.FlightRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.flightRuleViolationProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Flight Rule Violation</code>.
     */
    public static UriMatcherInfo initFlightRuleViolationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.FlightRuleViolationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.FlightRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.FlightRuleViolationColumns.COST;
        info.contentIdUriBase = Travel.FlightRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.FlightRuleViolationColumns._ID;
        info.projectionMap = TravelProvider.flightRuleViolationProjectionMap;
        info.defaultSortOrder = Travel.FlightRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.FlightRuleViolationColumns.FLIGHT_RULE_VIOLATION_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Rail Rule Violation projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initRailRuleViolationProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.RailRuleViolationColumns._ID, Travel.RailRuleViolationColumns._ID);
        retVal.put(Travel.RailRuleViolationColumns._COUNT, Travel.RailRuleViolationColumns._COUNT);

        retVal.put(Travel.RailRuleViolationColumns.TRIP_ID, Travel.RailRuleViolationColumns.TRIP_ID);
        retVal.put(Travel.RailRuleViolationColumns.RATE, Travel.RailRuleViolationColumns.RATE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Rail Rule Violation</code>.
     */
    public static UriMatcherInfo initRailRuleViolationsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.RailRuleViolationColumns.CONTENT_TYPE;
        info.tableName = Travel.RailRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.RailRuleViolationColumns.RATE;
        info.contentIdUriBase = Travel.RailRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.RailRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.railRuleViolationProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Rail Rule Violation</code>.
     */
    public static UriMatcherInfo initRailRuleViolationUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.RailRuleViolationColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.RailRuleViolationColumns.TABLE_NAME;
        info.nullColumnName = Travel.RailRuleViolationColumns.RATE;
        info.contentIdUriBase = Travel.RailRuleViolationColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.RailRuleViolationColumns._ID;
        info.projectionMap = TravelProvider.railRuleViolationProjectionMap;
        info.defaultSortOrder = Travel.RailRuleViolationColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.RailRuleViolationColumns.RAIL_RULE_VIOLATION_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Rule projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initRuleProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.RuleColumns._ID, Travel.RuleColumns._ID);
        retVal.put(Travel.RuleColumns._COUNT, Travel.RuleColumns._COUNT);

        retVal.put(Travel.RuleColumns.TEXT, Travel.RuleColumns.TEXT);
        retVal.put(Travel.RuleColumns.TRIP_RULE_VIOLATION_ID, Travel.RuleColumns.TRIP_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleColumns.CAR_RULE_VIOLATION_ID, Travel.RuleColumns.CAR_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleColumns.HOTEL_RULE_VIOLATION_ID, Travel.RuleColumns.HOTEL_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleColumns.FLIGHT_RULE_VIOLATION_ID, Travel.RuleColumns.FLIGHT_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleColumns.RAIL_RULE_VIOLATION_ID, Travel.RuleColumns.RAIL_RULE_VIOLATION_ID);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Rule</code>.
     */
    public static UriMatcherInfo initRulesUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.RuleColumns.CONTENT_TYPE;
        info.tableName = Travel.RuleColumns.TABLE_NAME;
        info.nullColumnName = Travel.RuleColumns.TEXT;
        info.contentIdUriBase = Travel.RuleColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.RuleColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.ruleProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Rule</code>.
     */
    public static UriMatcherInfo initRuleUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.RuleColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.RuleColumns.TABLE_NAME;
        info.nullColumnName = Travel.RuleColumns.TEXT;
        info.contentIdUriBase = Travel.RuleColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.RuleColumns._ID;
        info.projectionMap = TravelProvider.ruleProjectionMap;
        info.defaultSortOrder = Travel.RuleColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.RuleColumns.RULE_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Rule Violation Reason projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initRuleViolationReasonProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.RuleViolationReasonColumns._ID, Travel.RuleViolationReasonColumns._ID);
        retVal.put(Travel.RuleViolationReasonColumns._COUNT, Travel.RuleViolationReasonColumns._COUNT);

        retVal.put(Travel.RuleViolationReasonColumns.REASON, Travel.RuleViolationReasonColumns.REASON);
        retVal.put(Travel.RuleViolationReasonColumns.COMMENTS, Travel.RuleViolationReasonColumns.COMMENTS);
        retVal.put(Travel.RuleViolationReasonColumns.TRIP_RULE_VIOLATION_ID,
                Travel.RuleViolationReasonColumns.TRIP_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleViolationReasonColumns.CAR_RULE_VIOLATION_ID,
                Travel.RuleViolationReasonColumns.CAR_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleViolationReasonColumns.HOTEL_RULE_VIOLATION_ID,
                Travel.RuleViolationReasonColumns.HOTEL_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleViolationReasonColumns.FLIGHT_RULE_VIOLATION_ID,
                Travel.RuleViolationReasonColumns.FLIGHT_RULE_VIOLATION_ID);
        retVal.put(Travel.RuleViolationReasonColumns.RAIL_RULE_VIOLATION_ID,
                Travel.RuleViolationReasonColumns.RAIL_RULE_VIOLATION_ID);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Rule Violation Reason</code>.
     */
    public static UriMatcherInfo initRuleViolationReasonsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.RuleViolationReasonColumns.CONTENT_TYPE;
        info.tableName = Travel.RuleViolationReasonColumns.TABLE_NAME;
        info.nullColumnName = Travel.RuleViolationReasonColumns.COMMENTS;
        info.contentIdUriBase = Travel.RuleViolationReasonColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.RuleViolationReasonColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.ruleViolationReasonProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Rule Violation Reason</code>.
     */
    public static UriMatcherInfo initRuleViolationReasonUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.RuleViolationReasonColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.RuleViolationReasonColumns.TABLE_NAME;
        info.nullColumnName = Travel.RuleViolationReasonColumns.COMMENTS;
        info.contentIdUriBase = Travel.RuleViolationReasonColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.RuleViolationReasonColumns._ID;
        info.projectionMap = TravelProvider.ruleViolationReasonProjectionMap;
        info.defaultSortOrder = Travel.RuleViolationReasonColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.RuleViolationReasonColumns.RULE_VIOLATION_REASON_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Travel Point projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initTravelPointProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.TravelPointColumns._ID, Travel.TravelPointColumns._ID);
        retVal.put(Travel.TravelPointColumns._COUNT, Travel.TravelPointColumns._COUNT);

        retVal.put(Travel.TravelPointColumns.TRIP_ID, Travel.TravelPointColumns.TRIP_ID);
        retVal.put(Travel.TravelPointColumns.SEGMENT_ID, Travel.TravelPointColumns.SEGMENT_ID);
        retVal.put(Travel.TravelPointColumns.BENCHMARK, Travel.TravelPointColumns.BENCHMARK);
        retVal.put(Travel.TravelPointColumns.BENCHMARK_CURRENCY, Travel.TravelPointColumns.BENCHMARK_CURRENCY);
        retVal.put(Travel.TravelPointColumns.POINTS_POSTED, Travel.TravelPointColumns.POINTS_POSTED);
        retVal.put(Travel.TravelPointColumns.POINTS_PENDING, Travel.TravelPointColumns.POINTS_PENDING);
        retVal.put(Travel.TravelPointColumns.TOTAL_POINTS, Travel.TravelPointColumns.TOTAL_POINTS);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Travel Point</code>.
     */
    public static UriMatcherInfo initTravelPointsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.TravelPointColumns.CONTENT_TYPE;
        info.tableName = Travel.TravelPointColumns.TABLE_NAME;
        info.nullColumnName = Travel.TravelPointColumns.TOTAL_POINTS;
        info.contentIdUriBase = Travel.TravelPointColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.TravelPointColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.travelPointProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Travel Point</code>.
     */
    public static UriMatcherInfo initTravelPointUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.TravelPointColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.TravelPointColumns.TABLE_NAME;
        info.nullColumnName = Travel.TravelPointColumns.TOTAL_POINTS;
        info.contentIdUriBase = Travel.TravelPointColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.TravelPointColumns._ID;
        info.projectionMap = TravelProvider.travelPointProjectionMap;
        info.defaultSortOrder = Travel.TravelPointColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.TravelPointColumns.TRAVEL_POINT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Booking projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initBookingProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.BookingColumns._ID, Travel.BookingColumns._ID);
        retVal.put(Travel.BookingColumns._COUNT, Travel.BookingColumns._COUNT);

        retVal.put(Travel.BookingColumns.TRIP_ID, Travel.BookingColumns.TRIP_ID);
        retVal.put(Travel.BookingColumns.AGENCY_PCC, Travel.BookingColumns.AGENCY_PCC);
        retVal.put(Travel.BookingColumns.BOOKING_SOURCE, Travel.BookingColumns.BOOKING_SOURCE);
        retVal.put(Travel.BookingColumns.COMPANY_ACCOUNTING_CODE, Travel.BookingColumns.COMPANY_ACCOUNTING_CODE);
        retVal.put(Travel.BookingColumns.DATE_BOOKED_LOCAL, Travel.BookingColumns.DATE_BOOKED_LOCAL);
        retVal.put(Travel.BookingColumns.IS_CLIQBOOK_SYSTEM_OF_RECORD,
                Travel.BookingColumns.IS_CLIQBOOK_SYSTEM_OF_RECORD);
        retVal.put(Travel.BookingColumns.RECORD_LOCATOR, Travel.BookingColumns.RECORD_LOCATOR);
        retVal.put(Travel.BookingColumns.TRAVEL_CONFIG_ID, Travel.BookingColumns.TRAVEL_CONFIG_ID);
        retVal.put(Travel.BookingColumns.TYPE, Travel.BookingColumns.TYPE);
        retVal.put(Travel.BookingColumns.IS_GDS_BOOKING, Travel.BookingColumns.IS_GDS_BOOKING);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Booking</code>.
     */
    public static UriMatcherInfo initBookingsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.BookingColumns.CONTENT_TYPE;
        info.tableName = Travel.BookingColumns.TABLE_NAME;
        info.nullColumnName = Travel.BookingColumns.TYPE;
        info.contentIdUriBase = Travel.BookingColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.BookingColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.bookingProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Booking</code>.
     */
    public static UriMatcherInfo initBookingUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.BookingColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.BookingColumns.TABLE_NAME;
        info.nullColumnName = Travel.BookingColumns.TYPE;
        info.contentIdUriBase = Travel.BookingColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.BookingColumns._ID;
        info.projectionMap = TravelProvider.bookingProjectionMap;
        info.defaultSortOrder = Travel.BookingColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.BookingColumns.BOOKING_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Airline Ticket projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initAirlineTicketProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.AirlineTicketColumns._ID, Travel.AirlineTicketColumns._ID);
        retVal.put(Travel.AirlineTicketColumns._COUNT, Travel.AirlineTicketColumns._COUNT);

        retVal.put(Travel.AirlineTicketColumns.BOOKING_ID, Travel.AirlineTicketColumns.BOOKING_ID);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Airline Ticket</code>.
     */
    public static UriMatcherInfo initAirlineTicketsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.AirlineTicketColumns.CONTENT_TYPE;
        info.tableName = Travel.AirlineTicketColumns.TABLE_NAME;
        info.nullColumnName = Travel.AirlineTicketColumns.BOOKING_ID;
        info.contentIdUriBase = Travel.AirlineTicketColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.AirlineTicketColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.airlineTicketProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Airline Ticket</code>.
     */
    public static UriMatcherInfo initAirlineTicketUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.AirlineTicketColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.AirlineTicketColumns.TABLE_NAME;
        info.nullColumnName = Travel.AirlineTicketColumns.BOOKING_ID;
        info.contentIdUriBase = Travel.AirlineTicketColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.AirlineTicketColumns._ID;
        info.projectionMap = TravelProvider.airlineTicketProjectionMap;
        info.defaultSortOrder = Travel.AirlineTicketColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.AirlineTicketColumns.AIRLINE_TICKET_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Passenger projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initPassengerProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.PassengerColumns._ID, Travel.PassengerColumns._ID);
        retVal.put(Travel.PassengerColumns._COUNT, Travel.PassengerColumns._COUNT);

        retVal.put(Travel.PassengerColumns.BOOKING_ID, Travel.PassengerColumns.BOOKING_ID);
        retVal.put(Travel.PassengerColumns.FIRST_NAME, Travel.PassengerColumns.FIRST_NAME);
        retVal.put(Travel.PassengerColumns.LAST_NAME, Travel.PassengerColumns.LAST_NAME);
        retVal.put(Travel.PassengerColumns.IDENTIFIER, Travel.PassengerColumns.IDENTIFIER);
        retVal.put(Travel.PassengerColumns.PASSENGER_KEY, Travel.PassengerColumns.PASSENGER_KEY);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Passenger</code>.
     */
    public static UriMatcherInfo initPassengersUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.PassengerColumns.CONTENT_TYPE;
        info.tableName = Travel.PassengerColumns.TABLE_NAME;
        info.nullColumnName = Travel.PassengerColumns.IDENTIFIER;
        info.contentIdUriBase = Travel.PassengerColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.PassengerColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.passengerProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Passenger</code>.
     */
    public static UriMatcherInfo initPassengerUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.PassengerColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.PassengerColumns.TABLE_NAME;
        info.nullColumnName = Travel.PassengerColumns.IDENTIFIER;
        info.contentIdUriBase = Travel.PassengerColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.PassengerColumns._ID;
        info.projectionMap = TravelProvider.passengerProjectionMap;
        info.defaultSortOrder = Travel.PassengerColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.PassengerColumns.PASSENGER_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Frequent Traveler Program projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initFrequentTravelerProgramProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.FrequentTravelerProgramColumns._ID, Travel.FrequentTravelerProgramColumns._ID);
        retVal.put(Travel.FrequentTravelerProgramColumns._COUNT, Travel.FrequentTravelerProgramColumns._COUNT);

        retVal.put(Travel.FrequentTravelerProgramColumns.PASSENGER_ID,
                Travel.FrequentTravelerProgramColumns.PASSENGER_ID);
        retVal.put(Travel.FrequentTravelerProgramColumns.AIRLINE_VENDOR,
                Travel.FrequentTravelerProgramColumns.AIRLINE_VENDOR);
        retVal.put(Travel.FrequentTravelerProgramColumns.PROGRAM_NUMBER,
                Travel.FrequentTravelerProgramColumns.PROGRAM_NUMBER);
        retVal.put(Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR,
                Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR);
        retVal.put(Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR_CODE,
                Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR_CODE);
        retVal.put(Travel.FrequentTravelerProgramColumns.STATUS, Travel.FrequentTravelerProgramColumns.STATUS);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Frequent Traveler Program</code>.
     */
    public static UriMatcherInfo initFrequentTravelerProgramsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.FrequentTravelerProgramColumns.CONTENT_TYPE;
        info.tableName = Travel.FrequentTravelerProgramColumns.TABLE_NAME;
        info.nullColumnName = Travel.FrequentTravelerProgramColumns.STATUS;
        info.contentIdUriBase = Travel.FrequentTravelerProgramColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.FrequentTravelerProgramColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.frequentTravelerProgramProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Frequent Traveler Program</code>.
     */
    public static UriMatcherInfo initFrequentTravelerProgramUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.FrequentTravelerProgramColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.FrequentTravelerProgramColumns.TABLE_NAME;
        info.nullColumnName = Travel.FrequentTravelerProgramColumns.STATUS;
        info.contentIdUriBase = Travel.FrequentTravelerProgramColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.FrequentTravelerProgramColumns._ID;
        info.projectionMap = TravelProvider.frequentTravelerProgramProjectionMap;
        info.defaultSortOrder = Travel.FrequentTravelerProgramColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.FrequentTravelerProgramColumns.FREQUENT_TRAVELER_PROGRAM_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.SegmentColumns._ID, Travel.SegmentColumns._ID);
        retVal.put(Travel.SegmentColumns._COUNT, Travel.SegmentColumns._COUNT);

        retVal.put(Travel.SegmentColumns.BOOKING_ID, Travel.SegmentColumns.BOOKING_ID);
        retVal.put(Travel.SegmentColumns.START_DATE_UTC, Travel.SegmentColumns.START_DATE_UTC);
        retVal.put(Travel.SegmentColumns.END_DATE_UTC, Travel.SegmentColumns.END_DATE_UTC);
        retVal.put(Travel.SegmentColumns.START_DATE_LOCAL, Travel.SegmentColumns.START_DATE_LOCAL);
        retVal.put(Travel.SegmentColumns.END_DATE_LOCAL, Travel.SegmentColumns.END_DATE_LOCAL);
        retVal.put(Travel.SegmentColumns.CONFIRMATION_NUMBER, Travel.SegmentColumns.CONFIRMATION_NUMBER);
        retVal.put(Travel.SegmentColumns.CREDIT_CARD_ID, Travel.SegmentColumns.CREDIT_CARD_ID);
        retVal.put(Travel.SegmentColumns.CREDIT_CARD_LAST_FOUR, Travel.SegmentColumns.CREDIT_CARD_LAST_FOUR);
        retVal.put(Travel.SegmentColumns.CREDIT_CARD_TYPE, Travel.SegmentColumns.CREDIT_CARD_TYPE);
        retVal.put(Travel.SegmentColumns.CREDIT_CARD_TYPE_LOCALIZED, Travel.SegmentColumns.CREDIT_CARD_TYPE_LOCALIZED);
        retVal.put(Travel.SegmentColumns.CURRENCY, Travel.SegmentColumns.CURRENCY);
        retVal.put(Travel.SegmentColumns.ERECEIPT_STATUS, Travel.SegmentColumns.ERECEIPT_STATUS);
        retVal.put(Travel.SegmentColumns.END_ADDRESS, Travel.SegmentColumns.END_ADDRESS);
        retVal.put(Travel.SegmentColumns.END_ADDRESS_2, Travel.SegmentColumns.END_ADDRESS_2);
        retVal.put(Travel.SegmentColumns.END_CITY, Travel.SegmentColumns.END_CITY);
        retVal.put(Travel.SegmentColumns.END_CITY_CODE, Travel.SegmentColumns.END_CITY_CODE);
        retVal.put(Travel.SegmentColumns.END_CITY_CODE_LOCALIZED, Travel.SegmentColumns.END_CITY_CODE_LOCALIZED);
        retVal.put(Travel.SegmentColumns.END_COUNTRY, Travel.SegmentColumns.END_COUNTRY);
        retVal.put(Travel.SegmentColumns.END_COUNTRY_CODE, Travel.SegmentColumns.END_COUNTRY_CODE);
        retVal.put(Travel.SegmentColumns.END_LATITUDE, Travel.SegmentColumns.END_LATITUDE);
        retVal.put(Travel.SegmentColumns.END_LONGITUDE, Travel.SegmentColumns.END_LONGITUDE);
        retVal.put(Travel.SegmentColumns.END_POSTAL_CODE, Travel.SegmentColumns.END_POSTAL_CODE);
        retVal.put(Travel.SegmentColumns.END_STATE, Travel.SegmentColumns.END_STATE);
        retVal.put(Travel.SegmentColumns.FREQUENT_TRAVELER_ID, Travel.SegmentColumns.FREQUENT_TRAVELER_ID);
        retVal.put(Travel.SegmentColumns.IMAGE_VENDOR_URI, Travel.SegmentColumns.IMAGE_VENDOR_URI);
        retVal.put(Travel.SegmentColumns.NUM_PERSONS, Travel.SegmentColumns.NUM_PERSONS);
        retVal.put(Travel.SegmentColumns.OPERATED_BY_VENDOR, Travel.SegmentColumns.OPERATED_BY_VENDOR);
        retVal.put(Travel.SegmentColumns.OPERATED_BY_VENDOR_NAME, Travel.SegmentColumns.OPERATED_BY_VENDOR_NAME);
        retVal.put(Travel.SegmentColumns.PHONE_NUMBER, Travel.SegmentColumns.PHONE_NUMBER);
        retVal.put(Travel.SegmentColumns.RATE_CODE, Travel.SegmentColumns.RATE_CODE);
        retVal.put(Travel.SegmentColumns.SEGMENT_KEY, Travel.SegmentColumns.SEGMENT_KEY);
        retVal.put(Travel.SegmentColumns.SEGMENT_LOCATOR, Travel.SegmentColumns.SEGMENT_LOCATOR);
        retVal.put(Travel.SegmentColumns.SEGMENT_NAME, Travel.SegmentColumns.SEGMENT_NAME);
        retVal.put(Travel.SegmentColumns.START_ADDRESS, Travel.SegmentColumns.START_ADDRESS);
        retVal.put(Travel.SegmentColumns.START_ADDRESS_2, Travel.SegmentColumns.START_ADDRESS_2);
        retVal.put(Travel.SegmentColumns.START_CITY, Travel.SegmentColumns.START_CITY);
        retVal.put(Travel.SegmentColumns.START_CITY_CODE, Travel.SegmentColumns.START_CITY_CODE);
        retVal.put(Travel.SegmentColumns.START_COUNTRY, Travel.SegmentColumns.START_COUNTRY);
        retVal.put(Travel.SegmentColumns.START_COUNTRY_CODE, Travel.SegmentColumns.START_COUNTRY_CODE);
        retVal.put(Travel.SegmentColumns.START_LATITUDE, Travel.SegmentColumns.START_LATITUDE);
        retVal.put(Travel.SegmentColumns.START_LONGITUDE, Travel.SegmentColumns.START_LONGITUDE);
        retVal.put(Travel.SegmentColumns.START_POSTAL_CODE, Travel.SegmentColumns.START_POSTAL_CODE);
        retVal.put(Travel.SegmentColumns.START_STATE, Travel.SegmentColumns.START_STATE);
        retVal.put(Travel.SegmentColumns.STATUS, Travel.SegmentColumns.STATUS);
        retVal.put(Travel.SegmentColumns.STATUS_LOCALIZED, Travel.SegmentColumns.STATUS_LOCALIZED);
        retVal.put(Travel.SegmentColumns.TIMEZONE_ID, Travel.SegmentColumns.TIMEZONE_ID);
        retVal.put(Travel.SegmentColumns.TOTAL_RATE, Travel.SegmentColumns.TOTAL_RATE);
        retVal.put(Travel.SegmentColumns.TYPE, Travel.SegmentColumns.TYPE);
        retVal.put(Travel.SegmentColumns.TYPE_LOCALIZED, Travel.SegmentColumns.TYPE_LOCALIZED);
        retVal.put(Travel.SegmentColumns.VENDOR, Travel.SegmentColumns.VENDOR);
        retVal.put(Travel.SegmentColumns.VENDOR_NAME, Travel.SegmentColumns.VENDOR_NAME);
        retVal.put(Travel.SegmentColumns.VENDOR_URL, Travel.SegmentColumns.VENDOR_URL);
        retVal.put(Travel.SegmentColumns.ETICKET, Travel.SegmentColumns.ETICKET);
        retVal.put(Travel.SegmentColumns.ID_KEY, Travel.SegmentColumns.ID_KEY);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Segment</code>.
     */
    public static UriMatcherInfo initSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.SegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.SegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.SegmentColumns.STATUS;
        info.contentIdUriBase = Travel.SegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.SegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.segmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Segment</code>.
     */
    public static UriMatcherInfo initSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.SegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.SegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.SegmentColumns.STATUS;
        info.contentIdUriBase = Travel.SegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.SegmentColumns._ID;
        info.projectionMap = TravelProvider.segmentProjectionMap;
        info.defaultSortOrder = Travel.SegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.SegmentColumns.SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Air Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initAirSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.AirSegmentColumns._ID, Travel.AirSegmentColumns._ID);
        retVal.put(Travel.AirSegmentColumns._COUNT, Travel.AirSegmentColumns._COUNT);

        retVal.put(Travel.AirSegmentColumns.BOOKING_ID, Travel.AirSegmentColumns.BOOKING_ID);
        retVal.put(Travel.AirSegmentColumns.SEGMENT_ID, Travel.AirSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.AirSegmentColumns.AIRCRAFT_CODE, Travel.AirSegmentColumns.AIRCRAFT_CODE);
        retVal.put(Travel.AirSegmentColumns.AIRCRAFT_NAME, Travel.AirSegmentColumns.AIRCRAFT_NAME);
        retVal.put(Travel.AirSegmentColumns.CABIN, Travel.AirSegmentColumns.CABIN);
        retVal.put(Travel.AirSegmentColumns.CHECKED_BAGGAGE, Travel.AirSegmentColumns.CHECKED_BAGGAGE);
        retVal.put(Travel.AirSegmentColumns.CLASS_OF_SERVICE, Travel.AirSegmentColumns.CLASS_OF_SERVICE);
        retVal.put(Travel.AirSegmentColumns.CLASS_OF_SERVICE_LOCALIZED,
                Travel.AirSegmentColumns.CLASS_OF_SERVICE_LOCALIZED);
        retVal.put(Travel.AirSegmentColumns.DURATION, Travel.AirSegmentColumns.DURATION);
        retVal.put(Travel.AirSegmentColumns.END_AIRPORT_CITY, Travel.AirSegmentColumns.END_AIRPORT_CITY);
        retVal.put(Travel.AirSegmentColumns.END_AIRPORT_COUNTRY, Travel.AirSegmentColumns.END_AIRPORT_COUNTRY);
        retVal.put(Travel.AirSegmentColumns.END_AIRPORT_COUNTRY_CODE, Travel.AirSegmentColumns.END_AIRPORT_COUNTRY_CODE);
        retVal.put(Travel.AirSegmentColumns.END_AIRPORT_NAME, Travel.AirSegmentColumns.END_AIRPORT_NAME);
        retVal.put(Travel.AirSegmentColumns.END_AIRPORT_STATE, Travel.AirSegmentColumns.END_AIRPORT_STATE);
        retVal.put(Travel.AirSegmentColumns.END_GATE, Travel.AirSegmentColumns.END_GATE);
        retVal.put(Travel.AirSegmentColumns.END_TERMINAL, Travel.AirSegmentColumns.END_TERMINAL);
        retVal.put(Travel.AirSegmentColumns.FARE_BASIS_CODE, Travel.AirSegmentColumns.FARE_BASIS_CODE);
        retVal.put(Travel.AirSegmentColumns.FLIGHT_NUMBER, Travel.AirSegmentColumns.FLIGHT_NUMBER);
        retVal.put(Travel.AirSegmentColumns.LEG_ID, Travel.AirSegmentColumns.LEG_ID);
        retVal.put(Travel.AirSegmentColumns.MEALS, Travel.AirSegmentColumns.MEALS);
        retVal.put(Travel.AirSegmentColumns.MILES, Travel.AirSegmentColumns.MILES);
        retVal.put(Travel.AirSegmentColumns.NUM_STOPS, Travel.AirSegmentColumns.NUM_STOPS);
        retVal.put(Travel.AirSegmentColumns.OPERATED_BY_FLIGHT_NUMBER,
                Travel.AirSegmentColumns.OPERATED_BY_FLIGHT_NUMBER);
        retVal.put(Travel.AirSegmentColumns.SPECIAL_INSTRUCTIONS, Travel.AirSegmentColumns.SPECIAL_INSTRUCTIONS);
        retVal.put(Travel.AirSegmentColumns.START_AIRPORT_CITY, Travel.AirSegmentColumns.START_AIRPORT_CITY);
        retVal.put(Travel.AirSegmentColumns.START_AIRPORT_COUNTRY, Travel.AirSegmentColumns.START_AIRPORT_COUNTRY);
        retVal.put(Travel.AirSegmentColumns.START_AIRPORT_COUNTRY_CODE,
                Travel.AirSegmentColumns.START_AIRPORT_COUNTRY_CODE);
        retVal.put(Travel.AirSegmentColumns.START_AIRPORT_NAME, Travel.AirSegmentColumns.START_AIRPORT_NAME);
        retVal.put(Travel.AirSegmentColumns.START_AIRPORT_STATE, Travel.AirSegmentColumns.START_AIRPORT_STATE);
        retVal.put(Travel.AirSegmentColumns.START_GATE, Travel.AirSegmentColumns.START_GATE);
        retVal.put(Travel.AirSegmentColumns.START_TERMINAL, Travel.AirSegmentColumns.START_TERMINAL);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Air Segment</code>.
     */
    public static UriMatcherInfo initAirSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.AirSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.AirSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.AirSegmentColumns.START_GATE;
        info.contentIdUriBase = Travel.AirSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.AirSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.airSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Air Segment</code>.
     */
    public static UriMatcherInfo initAirSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.AirSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.AirSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.AirSegmentColumns.START_GATE;
        info.contentIdUriBase = Travel.AirSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.AirSegmentColumns._ID;
        info.projectionMap = TravelProvider.airSegmentProjectionMap;
        info.defaultSortOrder = Travel.AirSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.AirSegmentColumns.AIR_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Flight Status projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initFlightStatusProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.FlightStatusColumns._ID, Travel.FlightStatusColumns._ID);
        retVal.put(Travel.FlightStatusColumns._COUNT, Travel.FlightStatusColumns._COUNT);

        retVal.put(Travel.FlightStatusColumns.AIR_SEGMENT_ID, Travel.FlightStatusColumns.AIR_SEGMENT_ID);
        retVal.put(Travel.FlightStatusColumns.EQUIPMENT_SCHEDULED, Travel.FlightStatusColumns.EQUIPMENT_SCHEDULED);
        retVal.put(Travel.FlightStatusColumns.EQUIPMENT_ACTUAL, Travel.FlightStatusColumns.EQUIPMENT_ACTUAL);
        retVal.put(Travel.FlightStatusColumns.EQUIPMENT_REGISTRATION, Travel.FlightStatusColumns.EQUIPMENT_REGISTRATION);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_TERMINAL_SCHEDULED,
                Travel.FlightStatusColumns.DEPARTURE_TERMINAL_SCHEDULED);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_TERMINAL_ACTUAL,
                Travel.FlightStatusColumns.DEPARTURE_TERMINAL_ACTUAL);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_GATE, Travel.FlightStatusColumns.DEPARTURE_GATE);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_SCHEDULED, Travel.FlightStatusColumns.DEPARTURE_SCHEDULED);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_ESTIMATED, Travel.FlightStatusColumns.DEPARTURE_ESTIMATED);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_ACTUAL, Travel.FlightStatusColumns.DEPARTURE_ACTUAL);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_STATUS_REASON,
                Travel.FlightStatusColumns.DEPARTURE_STATUS_REASON);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_SHORT_STATUS, Travel.FlightStatusColumns.DEPARTURE_SHORT_STATUS);
        retVal.put(Travel.FlightStatusColumns.DEPARTURE_LONG_STATUS, Travel.FlightStatusColumns.DEPARTURE_LONG_STATUS);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_TERMINAL_SCHEDULED,
                Travel.FlightStatusColumns.ARRIVAL_TERMINAL_SCHEDULED);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_TERMINAL_ACTUAL,
                Travel.FlightStatusColumns.ARRIVAL_TERMINAL_ACTUAL);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_GATE, Travel.FlightStatusColumns.ARRIVAL_GATE);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_SCHEDULED, Travel.FlightStatusColumns.ARRIVAL_SCHEDULED);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_ESTIMATED, Travel.FlightStatusColumns.ARRIVAL_ESTIMATED);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_ACTUAL, Travel.FlightStatusColumns.ARRIVAL_ACTUAL);
        retVal.put(Travel.FlightStatusColumns.BAGGAGE_CLAIM, Travel.FlightStatusColumns.BAGGAGE_CLAIM);
        retVal.put(Travel.FlightStatusColumns.DIVERSION_CITY, Travel.FlightStatusColumns.DIVERSION_CITY);
        retVal.put(Travel.FlightStatusColumns.DIVERSION_AIRPORT, Travel.FlightStatusColumns.DIVERSION_AIRPORT);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_STATUS_REASON, Travel.FlightStatusColumns.ARRIVAL_STATUS_REASON);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_SHORT_STATUS, Travel.FlightStatusColumns.ARRIVAL_SHORT_STATUS);
        retVal.put(Travel.FlightStatusColumns.ARRIVAL_LONG_STATUS, Travel.FlightStatusColumns.ARRIVAL_LONG_STATUS);
        retVal.put(Travel.FlightStatusColumns.CLIQBOOK_MESSAGE, Travel.FlightStatusColumns.CLIQBOOK_MESSAGE);
        retVal.put(Travel.FlightStatusColumns.LAST_UPDATED_UTC, Travel.FlightStatusColumns.LAST_UPDATED_UTC);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Flight Status</code>.
     */
    public static UriMatcherInfo initFlightStatusesUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.FlightStatusColumns.CONTENT_TYPE;
        info.tableName = Travel.FlightStatusColumns.TABLE_NAME;
        info.nullColumnName = Travel.FlightStatusColumns.BAGGAGE_CLAIM;
        info.contentIdUriBase = Travel.FlightStatusColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.FlightStatusColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.flightStatusProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Flight Status</code>.
     */
    public static UriMatcherInfo initFlightStatusUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.FlightStatusColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.FlightStatusColumns.TABLE_NAME;
        info.nullColumnName = Travel.FlightStatusColumns.BAGGAGE_CLAIM;
        info.contentIdUriBase = Travel.FlightStatusColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.FlightStatusColumns._ID;
        info.projectionMap = TravelProvider.flightStatusProjectionMap;
        info.defaultSortOrder = Travel.FlightStatusColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.FlightStatusColumns.FLIGHT_STATUS_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Seat projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initSeatProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.SeatColumns._ID, Travel.SeatColumns._ID);
        retVal.put(Travel.SeatColumns._COUNT, Travel.SeatColumns._COUNT);

        retVal.put(Travel.SeatColumns.AIR_SEGMENT_ID, Travel.SeatColumns.AIR_SEGMENT_ID);
        retVal.put(Travel.SeatColumns.PASSENGER_RPH, Travel.SeatColumns.PASSENGER_RPH);
        retVal.put(Travel.SeatColumns.SEAT_NUMBER, Travel.SeatColumns.SEAT_NUMBER);
        retVal.put(Travel.SeatColumns.STATUS_CODE, Travel.SeatColumns.STATUS_CODE);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Seat</code>.
     */
    public static UriMatcherInfo initSeatsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.SeatColumns.CONTENT_TYPE;
        info.tableName = Travel.SeatColumns.TABLE_NAME;
        info.nullColumnName = Travel.SeatColumns.STATUS_CODE;
        info.contentIdUriBase = Travel.SeatColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.SeatColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.seatProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Seat</code>.
     */
    public static UriMatcherInfo initSeatUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.SeatColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.SeatColumns.TABLE_NAME;
        info.nullColumnName = Travel.SeatColumns.STATUS_CODE;
        info.contentIdUriBase = Travel.SeatColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.SeatColumns._ID;
        info.projectionMap = TravelProvider.seatProjectionMap;
        info.defaultSortOrder = Travel.SeatColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.SeatColumns.SEAT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Hotel Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initHotelSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.HotelSegmentColumns._ID, Travel.HotelSegmentColumns._ID);
        retVal.put(Travel.HotelSegmentColumns._COUNT, Travel.HotelSegmentColumns._COUNT);

        retVal.put(Travel.HotelSegmentColumns.BOOKING_ID, Travel.HotelSegmentColumns.BOOKING_ID);
        retVal.put(Travel.HotelSegmentColumns.SEGMENT_ID, Travel.HotelSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.HotelSegmentColumns.CHECK_IN_TIME, Travel.HotelSegmentColumns.CHECK_IN_TIME);
        retVal.put(Travel.HotelSegmentColumns.CHECK_OUT_TIME, Travel.HotelSegmentColumns.CHECK_OUT_TIME);
        retVal.put(Travel.HotelSegmentColumns.DISCOUNT_CODE, Travel.HotelSegmentColumns.DISCOUNT_CODE);
        retVal.put(Travel.HotelSegmentColumns.NUM_ROOMS, Travel.HotelSegmentColumns.NUM_ROOMS);
        retVal.put(Travel.HotelSegmentColumns.RATE_CODE, Travel.HotelSegmentColumns.RATE_CODE);
        retVal.put(Travel.HotelSegmentColumns.ROOM_TYPE, Travel.HotelSegmentColumns.ROOM_TYPE);
        retVal.put(Travel.HotelSegmentColumns.ROOM_TYPE_LOCALIZED, Travel.HotelSegmentColumns.ROOM_TYPE_LOCALIZED);
        retVal.put(Travel.HotelSegmentColumns.DAILY_RATE, Travel.HotelSegmentColumns.DAILY_RATE);
        retVal.put(Travel.HotelSegmentColumns.TOTAL_RATE, Travel.HotelSegmentColumns.TOTAL_RATE);
        retVal.put(Travel.HotelSegmentColumns.CANCELLATION_POLICY, Travel.HotelSegmentColumns.CANCELLATION_POLICY);
        retVal.put(Travel.HotelSegmentColumns.SPECIAL_INSTRUCTIONS, Travel.HotelSegmentColumns.SPECIAL_INSTRUCTIONS);
        retVal.put(Travel.HotelSegmentColumns.ROOM_DESCRIPTION, Travel.HotelSegmentColumns.ROOM_DESCRIPTION);
        retVal.put(Travel.HotelSegmentColumns.RATE_TYPE, Travel.HotelSegmentColumns.RATE_TYPE);
        retVal.put(Travel.HotelSegmentColumns.PROPERTY_ID, Travel.HotelSegmentColumns.PROPERTY_ID);
        retVal.put(Travel.HotelSegmentColumns.PROPERTY_IMAGE_COUNT, Travel.HotelSegmentColumns.PROPERTY_IMAGE_COUNT);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Hotel Segment</code>.
     */
    public static UriMatcherInfo initHotelSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.HotelSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.HotelSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.HotelSegmentColumns.PROPERTY_IMAGE_COUNT;
        info.contentIdUriBase = Travel.HotelSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.HotelSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.hotelSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Hotel Segment</code>.
     */
    public static UriMatcherInfo initHotelSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.HotelSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.HotelSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.HotelSegmentColumns.PROPERTY_IMAGE_COUNT;
        info.contentIdUriBase = Travel.HotelSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.HotelSegmentColumns._ID;
        info.projectionMap = TravelProvider.hotelSegmentProjectionMap;
        info.defaultSortOrder = Travel.HotelSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.HotelSegmentColumns.HOTEL_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Car Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initCarSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.CarSegmentColumns._ID, Travel.CarSegmentColumns._ID);
        retVal.put(Travel.CarSegmentColumns._COUNT, Travel.CarSegmentColumns._COUNT);

        retVal.put(Travel.CarSegmentColumns.BOOKING_ID, Travel.CarSegmentColumns.BOOKING_ID);
        retVal.put(Travel.CarSegmentColumns.SEGMENT_ID, Travel.CarSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.CarSegmentColumns.AIR_CONDITION, Travel.CarSegmentColumns.AIR_CONDITION);
        retVal.put(Travel.CarSegmentColumns.AIR_CONDITION_LOCALIZED, Travel.CarSegmentColumns.AIR_CONDITION_LOCALIZED);
        retVal.put(Travel.CarSegmentColumns.BODY, Travel.CarSegmentColumns.BODY);
        retVal.put(Travel.CarSegmentColumns.BODY_LOCALIZED, Travel.CarSegmentColumns.BODY_LOCALIZED);
        retVal.put(Travel.CarSegmentColumns.CLASS_OF_CAR, Travel.CarSegmentColumns.CLASS_OF_CAR);
        retVal.put(Travel.CarSegmentColumns.CLASS_OF_CAR_LOCALIZED, Travel.CarSegmentColumns.CLASS_OF_CAR_LOCALIZED);
        retVal.put(Travel.CarSegmentColumns.DAILY_RATE, Travel.CarSegmentColumns.DAILY_RATE);
        retVal.put(Travel.CarSegmentColumns.DISCOUNT_CODE, Travel.CarSegmentColumns.DISCOUNT_CODE);
        retVal.put(Travel.CarSegmentColumns.END_AIRPORT_CITY, Travel.CarSegmentColumns.END_AIRPORT_CITY);
        retVal.put(Travel.CarSegmentColumns.END_AIRPORT_COUNTRY, Travel.CarSegmentColumns.END_AIRPORT_COUNTRY);
        retVal.put(Travel.CarSegmentColumns.END_AIRPORT_COUNTRY_CODE, Travel.CarSegmentColumns.END_AIRPORT_COUNTRY_CODE);
        retVal.put(Travel.CarSegmentColumns.END_AIRPORT_NAME, Travel.CarSegmentColumns.END_AIRPORT_NAME);
        retVal.put(Travel.CarSegmentColumns.END_AIRPORT_STATE, Travel.CarSegmentColumns.END_AIRPORT_STATE);
        retVal.put(Travel.CarSegmentColumns.END_LOCATION, Travel.CarSegmentColumns.END_LOCATION);
        retVal.put(Travel.CarSegmentColumns.IMAGE_CAR_URI, Travel.CarSegmentColumns.IMAGE_CAR_URI);
        retVal.put(Travel.CarSegmentColumns.NUM_CARS, Travel.CarSegmentColumns.NUM_CARS);
        retVal.put(Travel.CarSegmentColumns.RATE_TYPE, Travel.CarSegmentColumns.RATE_TYPE);
        retVal.put(Travel.CarSegmentColumns.SPECIAL_EQUIPMENT, Travel.CarSegmentColumns.SPECIAL_EQUIPMENT);
        retVal.put(Travel.CarSegmentColumns.START_AIRPORT_CITY, Travel.CarSegmentColumns.START_AIRPORT_CITY);
        retVal.put(Travel.CarSegmentColumns.START_AIRPORT_COUNTRY, Travel.CarSegmentColumns.START_AIRPORT_COUNTRY);
        retVal.put(Travel.CarSegmentColumns.START_AIRPORT_COUNTRY_CODE,
                Travel.CarSegmentColumns.START_AIRPORT_COUNTRY_CODE);
        retVal.put(Travel.CarSegmentColumns.START_AIRPORT_NAME, Travel.CarSegmentColumns.START_AIRPORT_NAME);
        retVal.put(Travel.CarSegmentColumns.START_AIRPORT_STATE, Travel.CarSegmentColumns.START_AIRPORT_STATE);
        retVal.put(Travel.CarSegmentColumns.START_LOCATION, Travel.CarSegmentColumns.START_LOCATION);
        retVal.put(Travel.CarSegmentColumns.TRANSMISSION, Travel.CarSegmentColumns.TRANSMISSION);
        retVal.put(Travel.CarSegmentColumns.TRANSMISSION_LOCALIZED, Travel.CarSegmentColumns.TRANSMISSION_LOCALIZED);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Car Segment</code>.
     */
    public static UriMatcherInfo initCarSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.CarSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.CarSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.CarSegmentColumns.RATE_TYPE;
        info.contentIdUriBase = Travel.CarSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.CarSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.carSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Car Segment</code>.
     */
    public static UriMatcherInfo initCarSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.CarSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.CarSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.CarSegmentColumns.RATE_TYPE;
        info.contentIdUriBase = Travel.CarSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.CarSegmentColumns._ID;
        info.projectionMap = TravelProvider.carSegmentProjectionMap;
        info.defaultSortOrder = Travel.CarSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.CarSegmentColumns.CAR_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Rail Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initRailSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.RailSegmentColumns._ID, Travel.RailSegmentColumns._ID);
        retVal.put(Travel.RailSegmentColumns._COUNT, Travel.RailSegmentColumns._COUNT);

        retVal.put(Travel.RailSegmentColumns.BOOKING_ID, Travel.RailSegmentColumns.BOOKING_ID);
        retVal.put(Travel.RailSegmentColumns.SEGMENT_ID, Travel.RailSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.RailSegmentColumns.AMENITIES, Travel.RailSegmentColumns.AMENITIES);
        retVal.put(Travel.RailSegmentColumns.CABIN, Travel.RailSegmentColumns.CABIN);
        retVal.put(Travel.RailSegmentColumns.CLASS_OF_SERVICE, Travel.RailSegmentColumns.CLASS_OF_SERVICE);
        retVal.put(Travel.RailSegmentColumns.DISCOUNT_CODE, Travel.RailSegmentColumns.DISCOUNT_CODE);
        retVal.put(Travel.RailSegmentColumns.DURATION, Travel.RailSegmentColumns.DURATION);
        retVal.put(Travel.RailSegmentColumns.END_PLATFORM, Travel.RailSegmentColumns.END_PLATFORM);
        retVal.put(Travel.RailSegmentColumns.END_RAIL_STATION, Travel.RailSegmentColumns.END_RAIL_STATION);
        retVal.put(Travel.RailSegmentColumns.END_RAIL_STATION_LOCALIZED,
                Travel.RailSegmentColumns.END_RAIL_STATION_LOCALIZED);
        retVal.put(Travel.RailSegmentColumns.LEG_ID, Travel.RailSegmentColumns.LEG_ID);
        retVal.put(Travel.RailSegmentColumns.MEALS, Travel.RailSegmentColumns.MEALS);
        retVal.put(Travel.RailSegmentColumns.MILES, Travel.RailSegmentColumns.MILES);
        retVal.put(Travel.RailSegmentColumns.NUM_STOPS, Travel.RailSegmentColumns.NUM_STOPS);
        retVal.put(Travel.RailSegmentColumns.OPERATED_BY_TRAIN_NUMBER,
                Travel.RailSegmentColumns.OPERATED_BY_TRAIN_NUMBER);
        retVal.put(Travel.RailSegmentColumns.PIN, Travel.RailSegmentColumns.PIN);
        retVal.put(Travel.RailSegmentColumns.START_PLATFORM, Travel.RailSegmentColumns.START_PLATFORM);
        retVal.put(Travel.RailSegmentColumns.START_RAIL_STATION, Travel.RailSegmentColumns.START_RAIL_STATION);
        retVal.put(Travel.RailSegmentColumns.START_RAIL_STATION_LOCALIZED,
                Travel.RailSegmentColumns.START_RAIL_STATION_LOCALIZED);
        retVal.put(Travel.RailSegmentColumns.TRAIN_NUMBER, Travel.RailSegmentColumns.TRAIN_NUMBER);
        retVal.put(Travel.RailSegmentColumns.TRAIN_TYPE_CODE, Travel.RailSegmentColumns.TRAIN_TYPE_CODE);
        retVal.put(Travel.RailSegmentColumns.WAGON_NUMBER, Travel.RailSegmentColumns.WAGON_NUMBER);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Rail Segment</code>.
     */
    public static UriMatcherInfo initRailSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.RailSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.RailSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.RailSegmentColumns.MEALS;
        info.contentIdUriBase = Travel.RailSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.RailSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.railSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Rail Segment</code>.
     */
    public static UriMatcherInfo initRailSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.RailSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.RailSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.RailSegmentColumns.MEALS;
        info.contentIdUriBase = Travel.RailSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.RailSegmentColumns._ID;
        info.projectionMap = TravelProvider.railSegmentProjectionMap;
        info.defaultSortOrder = Travel.RailSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.RailSegmentColumns.RAIL_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Dining Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initDiningSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.DiningSegmentColumns._ID, Travel.DiningSegmentColumns._ID);
        retVal.put(Travel.DiningSegmentColumns._COUNT, Travel.DiningSegmentColumns._COUNT);

        retVal.put(Travel.DiningSegmentColumns.BOOKING_ID, Travel.DiningSegmentColumns.BOOKING_ID);
        retVal.put(Travel.DiningSegmentColumns.SEGMENT_ID, Travel.DiningSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.DiningSegmentColumns.RESERVATION_ID, Travel.DiningSegmentColumns.RESERVATION_ID);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Dining Segment</code>.
     */
    public static UriMatcherInfo initDiningSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.DiningSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.DiningSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.DiningSegmentColumns.RESERVATION_ID;
        info.contentIdUriBase = Travel.DiningSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.DiningSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.diningSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Dining Segment</code>.
     */
    public static UriMatcherInfo initDiningSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.DiningSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.DiningSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.DiningSegmentColumns.RESERVATION_ID;
        info.contentIdUriBase = Travel.DiningSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.DiningSegmentColumns._ID;
        info.projectionMap = TravelProvider.diningSegmentProjectionMap;
        info.defaultSortOrder = Travel.DiningSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.DiningSegmentColumns.DINING_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Event Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initEventSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.EventSegmentColumns._ID, Travel.EventSegmentColumns._ID);
        retVal.put(Travel.EventSegmentColumns._COUNT, Travel.EventSegmentColumns._COUNT);

        retVal.put(Travel.EventSegmentColumns.BOOKING_ID, Travel.EventSegmentColumns.BOOKING_ID);
        retVal.put(Travel.EventSegmentColumns.SEGMENT_ID, Travel.EventSegmentColumns.SEGMENT_ID);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Event Segment</code>.
     */
    public static UriMatcherInfo initEventSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.EventSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.EventSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.EventSegmentColumns.SEGMENT_ID;
        info.contentIdUriBase = Travel.EventSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.EventSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.eventSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Event Segment</code>.
     */
    public static UriMatcherInfo initEventSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.EventSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.EventSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.EventSegmentColumns.SEGMENT_ID;
        info.contentIdUriBase = Travel.EventSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.EventSegmentColumns._ID;
        info.projectionMap = TravelProvider.eventSegmentProjectionMap;
        info.defaultSortOrder = Travel.EventSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.EventSegmentColumns.EVENT_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Parking Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initParkingSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.ParkingSegmentColumns._ID, Travel.ParkingSegmentColumns._ID);
        retVal.put(Travel.ParkingSegmentColumns._COUNT, Travel.ParkingSegmentColumns._COUNT);

        retVal.put(Travel.ParkingSegmentColumns.BOOKING_ID, Travel.ParkingSegmentColumns.BOOKING_ID);
        retVal.put(Travel.ParkingSegmentColumns.SEGMENT_ID, Travel.ParkingSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.ParkingSegmentColumns.PARKING_LOCATION_ID, Travel.ParkingSegmentColumns.PARKING_LOCATION_ID);
        retVal.put(Travel.ParkingSegmentColumns.PIN, Travel.ParkingSegmentColumns.PIN);
        retVal.put(Travel.ParkingSegmentColumns.START_LOCATION, Travel.ParkingSegmentColumns.START_LOCATION);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Parking Segment</code>.
     */
    public static UriMatcherInfo initParkingSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.ParkingSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.ParkingSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.ParkingSegmentColumns.START_LOCATION;
        info.contentIdUriBase = Travel.ParkingSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.ParkingSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.parkingSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Parking Segment</code>.
     */
    public static UriMatcherInfo initParkingSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.ParkingSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.ParkingSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.ParkingSegmentColumns.START_LOCATION;
        info.contentIdUriBase = Travel.ParkingSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.ParkingSegmentColumns._ID;
        info.projectionMap = TravelProvider.parkingSegmentProjectionMap;
        info.defaultSortOrder = Travel.ParkingSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.ParkingSegmentColumns.PARKING_SEGMENT_ID_PATH_POSITION;
        return info;

    }

    /**
     * Will initialize the Ride Segment projection map.
     * 
     * @return returns a <code>HashMap</code> projection map.
     */
    public static HashMap<String, String> initRideSegmentProjectionMap() {

        HashMap<String, String> retVal = new HashMap<String, String>();

        retVal.put(Travel.RideSegmentColumns._ID, Travel.RideSegmentColumns._ID);
        retVal.put(Travel.RideSegmentColumns._COUNT, Travel.RideSegmentColumns._COUNT);

        retVal.put(Travel.RideSegmentColumns.BOOKING_ID, Travel.RideSegmentColumns.BOOKING_ID);
        retVal.put(Travel.RideSegmentColumns.SEGMENT_ID, Travel.RideSegmentColumns.SEGMENT_ID);
        retVal.put(Travel.RideSegmentColumns.CANCELLATION_POLICY, Travel.RideSegmentColumns.CANCELLATION_POLICY);
        retVal.put(Travel.RideSegmentColumns.DROP_OFF_INSTRUCTIONS, Travel.RideSegmentColumns.DROP_OFF_INSTRUCTIONS);
        retVal.put(Travel.RideSegmentColumns.DURATION, Travel.RideSegmentColumns.DURATION);
        retVal.put(Travel.RideSegmentColumns.MEETING_INSTRUCTIONS, Travel.RideSegmentColumns.MEETING_INSTRUCTIONS);
        retVal.put(Travel.RideSegmentColumns.MILES, Travel.RideSegmentColumns.MILES);
        retVal.put(Travel.RideSegmentColumns.NUMBER_OF_HOURS, Travel.RideSegmentColumns.NUMBER_OF_HOURS);
        retVal.put(Travel.RideSegmentColumns.PICK_UP_INSTRUCTIONS, Travel.RideSegmentColumns.PICK_UP_INSTRUCTIONS);
        retVal.put(Travel.RideSegmentColumns.RATE, Travel.RideSegmentColumns.RATE);
        retVal.put(Travel.RideSegmentColumns.RATE_DESCRIPTION, Travel.RideSegmentColumns.RATE_DESCRIPTION);
        retVal.put(Travel.RideSegmentColumns.RATE_TYPE, Travel.RideSegmentColumns.RATE_TYPE);
        retVal.put(Travel.RideSegmentColumns.START_LOCATION, Travel.RideSegmentColumns.START_LOCATION);
        retVal.put(Travel.RideSegmentColumns.START_LOCATION_CODE, Travel.RideSegmentColumns.START_LOCATION_CODE);
        retVal.put(Travel.RideSegmentColumns.START_LOCATION_NAME, Travel.RideSegmentColumns.START_LOCATION_NAME);

        return retVal;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for a directory of <code>Ride Segment</code>.
     */
    public static UriMatcherInfo initRideSegmentsUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Travel.RideSegmentColumns.CONTENT_TYPE;
        info.tableName = Travel.RideSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.RideSegmentColumns.RATE_TYPE;
        info.contentIdUriBase = Travel.RideSegmentColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Travel.RideSegmentColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = TravelProvider.rideSegmentProjectionMap;

        return info;

    }

    /**
     * Will initialize a <code>UriMatcherInfo</code> object for one item of type <code>Ride Segment</code>.
     */
    public static UriMatcherInfo initRideSegmentUriMatcherInfo() {

        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Travel.RideSegmentColumns.CONTENT_ITEM_TYPE;
        info.tableName = Travel.RideSegmentColumns.TABLE_NAME;
        info.nullColumnName = Travel.RideSegmentColumns.RATE_TYPE;
        info.contentIdUriBase = Travel.RideSegmentColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Travel.RideSegmentColumns._ID;
        info.projectionMap = TravelProvider.rideSegmentProjectionMap;
        info.defaultSortOrder = Travel.RideSegmentColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Travel.RideSegmentColumns.RIDE_SEGMENT_ID_PATH_POSITION;
        return info;

    }
}
