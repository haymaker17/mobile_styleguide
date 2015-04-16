/**
 * 
 */
package com.concur.mobile.platform.travel.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import com.concur.mobile.platform.config.provider.Config.UserColumns;

/**
 * This class provide a contract between the travel content provider and calling application code.
 * 
 * @author andrewk
 */
public final class Travel {

    /**
     * The authority for the Travel provider.
     */
    public static final String AUTHORITY = "com.concur.mobile.platform.travel";

    /**
     * A content:// style uri to the authority for the config provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Models trip summary information.
     */
    public static final class TripSummaryColumns implements BaseColumns {

        // Prevent instantiation.
        private TripSummaryColumns() {
        }

        /**
         * Contains the trip summary table name.
         */
        public static final String TABLE_NAME = "TRIP_SUMMARY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Trip Summaries URI
         */
        private static final String PATH_TRIP_SUMMARIES = "/trip_summaries";

        /**
         * Path part for the Trip Summary ID URI
         */
        private static final String PATH_TRIP_SUMMARY_ID = "/trip_summaries/";

        /**
         * 0-relative position of a trip summary ID segment in the path part of a trip summary ID URI
         */
        public static final int TRIP_SUMMARY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_SUMMARIES);

        /**
         * The content URI base for a single trip summary. Callers must append a numeric trip summary id to this Uri to retrieve a
         * trip summary.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_SUMMARY_ID);

        /**
         * The content URI match pattern for a single trip summary, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_SUMMARY_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of trip summaries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.trip_summaries";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single trip summary.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.trip_summary";

        // Column definitions

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the approval status (TEXT) column name.
         */
        public static final String APPROVAL_STATUS = "APPROVAL_STATUS";

        /**
         * Contains the approver id (TEXT) column name.
         */
        public static final String APPROVER_ID = "APPROVER_ID";

        /**
         * Contains the approver name (TEXT) column name.
         */
        public static final String APPROVER_NAME = "APPROVER_NAME";

        /**
         * Contains the authorization number (TEXT) column name.
         */
        public static final String AUTHORIZATION_NUMBER = "AUTHORIZATION_NUMBER";

        /**
         * Contains the "booked via" (TEXT) column name.
         */
        public static final String BOOKED_VIA = "BOOKED_VIA";

        /**
         * Contains the "booking source" (TEXT) column name.
         */
        public static final String BOOKING_SOURCE = "BOOKING_SOURCE";

        /**
         * Contains the "can be expensed" (BOOLEAN) column name.
         */
        public static final String CAN_BE_EXPENSED = "CAN_BE_EXPENSED";

        /**
         * Contains the "cliqbook state" (INTEGER) column name.
         */
        public static final String CLIQ_BOOK_STATE = "CLIQ_BOOK_STATE";

        /**
         * Contains the "end date local" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_LOCAL = "END_DATE_LOCAL";

        /**
         * Contains the "end date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_UTC = "END_DATE_UTC";

        /**
         * Contains the "has others" (BOOLEAN) column name.
         */
        public static final String HAS_OTHERS = "HAS_OTHERS";

        /**
         * Contains the "has tickets" (BOOLEAN) column name.
         */
        public static final String HAS_TICKETS = "HAS_TICKETS";

        /**
         * Contains the "is expensed" (BOOLEAN) column name.
         */
        public static final String IS_EXPENSED = "IS_EXPENSED";

        /**
         * Contains the "is GDS booking" (BOOLEAN) column name.
         */
        public static final String IS_GDS_BOOKING = "IS_GDS_BOOKING";

        /**
         * Contains the "is personal" (BOOLEAN) column name.
         */
        public static final String IS_PERSONAL = "IS_PERSONAL";

        /**
         * Contains the "is withdrawn" (BOOLEAN) column name.
         */
        public static final String IS_WITHDRAWN = "IS_WITHDRAWN";

        /**
         * Contains the "is public" (BOOLEAN) column name.
         */
        public static final String IS_PUBLIC = "IS_PUBLIC";

        /**
         * Contains the "itinerary id" (INTEGER) column name.
         */
        public static final String ITIN_ID = "ITIN_ID";

        /**
         * Contains the "itinerary locator" (TEXT) column name.
         */
        public static final String ITIN_LOCATOR = "ITIN_LOCATOR";

        /**
         * Contains the "itinerary source list" (TEXT) column name.
         */
        public static final String ITIN_SOURCE_LIST = "ITIN_SOURCE_LIST";

        /**
         * Contains the "record locator" (TEXT) column name.
         */
        public static final String RECORD_LOCATOR = "RECORD_LOCATOR";

        /**
         * Contains the "segment types" (TEXT) column name.
         */
        public static final String SEGMENT_TYPES = "SEGMENT_TYPES";

        /**
         * Contains the "start date local" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_LOCAL = "START_DATE_LOCAL";

        /**
         * Contains the "start date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_UTC = "START_DATE_UTC";

        /**
         * Contains the "trip id" (INTEGER) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "trip key" (TEXT) column name.
         */
        public static final String TRIP_KEY = "TRIP_KEY";

        /**
         * Contains the "trip name" (TEXT) column name.
         */
        public static final String TRIP_NAME = "TRIP_NAME";

        /**
         * Contains the "trip status" (INTEGER) column name.
         */
        public static final String TRIP_STATUS = "TRIP_STATUS";

        /**
         * Contains the column name of the user id associated with this trip summary.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models trip summary message information.
     */
    public static final class TripSummaryMessageColumns implements BaseColumns {

        // Prevent instantiation.
        private TripSummaryMessageColumns() {
        }

        /**
         * Contains the trip summary message table name.
         */
        public static final String TABLE_NAME = "TRIP_SUMMARY_MESSAGE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Trip Summary Messages URI
         */
        private static final String PATH_TRIP_SUMMARY_MESSAGES = "/trip_summary_messages";

        /**
         * Path part for the Trip Summary Message ID URI
         */
        private static final String PATH_TRIP_SUMMARY_MESSAGE_ID = "/trip_summary_messages/";

        /**
         * 0-relative position of a trip summary message ID segment in the path part of a trip summary message ID URI
         */
        public static final int TRIP_SUMMARY_MESSAGE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_SUMMARY_MESSAGES);

        /**
         * The content URI base for a single trip summary message. Callers must append a numeric trip summary message id to this
         * Uri to retrieve a trip summary message.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_SUMMARY_MESSAGE_ID);

        /**
         * The content URI match pattern for a single trip summary message, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_SUMMARY_MESSAGE_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of trip summary messages.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.trip_summary_messages";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single trip summary message.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.trip_summary_message";

        // Column definitions

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the "message" (TEXT) column name.
         */
        public static final String MESSAGE = "MESSAGE";

        /**
         * Contains the "trip id" (INTEGER) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the column name of the user id associated with this trip summary message.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models trip information.
     */
    public static final class TripColumns implements BaseColumns {

        // Prevent instantiation.
        private TripColumns() {
        }

        /**
         * Contains the trip summary table name.
         */
        public static final String TABLE_NAME = "TRIP";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Trips URI
         */
        private static final String PATH_TRIPS = "/trips";

        /**
         * Path part for the Trip ID URI
         */
        private static final String PATH_TRIP_ID = "/trips/";

        /**
         * 0-relative position of a trip ID segment in the path part of a trip ID URI
         */
        public static final int TRIP_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TRIPS);

        /**
         * The content URI base for a single trip. Callers must append a numeric trip id to this Uri to retrieve a trip.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_ID);

        /**
         * The content URI match pattern for a single trip, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of trips.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.trips";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single trip.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.trip";

        // Column definitions

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the "client locator" (TEXT) column name.
         */
        public static final String CLIENT_LOCATOR = "CLIENT_LOCATOR";

        /**
         * Contains the "cliqbook trip id" (INTEGER) column name.
         */
        public static final String CLIQBOOK_TRIP_ID = "CLIQBOOK_TRIP_ID";

        /**
         * Contains the "description" (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the "end date local" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_LOCAL = "END_DATE_LOCAL";

        /**
         * Contains the "end date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_UTC = "END_DATE_UTC";

        /**
         * Contains the "itinerary locator" (TEXT) column name.
         */
        public static final String ITIN_LOCATOR = "ITIN_LOCATOR";

        /**
         * Contains the "record locator" (TEXT) column name.
         */
        public static final String RECORD_LOCATOR = "RECORD_LOCATOR";

        /**
         * Contains the "start date local" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_LOCAL = "START_DATE_LOCAL";

        /**
         * Contains the "start date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_UTC = "START_DATE_UTC";

        /**
         * Contains the "state" (INTEGER) column name.
         */
        public static final String STATE = "STATE";

        /**
         * Contains the "trip name" (TEXT) column name.
         */
        public static final String TRIP_NAME = "TRIP_NAME";

        /**
         * Contains the boolean "allow add air" (INTEGER) column name.
         */
        public static final String ALLOW_ADD_AIR = "ALLOW_ADD_AIR";

        /**
         * Contains the boolean "allow add car" (INTEGER) column name.
         */
        public static final String ALLOW_ADD_CAR = "ALLOW_ADD_CAR";

        /**
         * Contains the boolean "allow add hotel" (INTEGER) column name.
         */
        public static final String ALLOW_ADD_HOTEL = "ALLOW_ADD_HOTEL";

        /**
         * Contains the boolean "allow add rail" (INTEGER) column name.
         */
        public static final String ALLOW_ADD_RAIL = "ALLOW_ADD_RAIL";

        /**
         * Contains the boolean "allow cancel" (INTEGER) column name.
         */
        public static final String ALLOW_CANCEL = "ALLOW_CANCEL";

        /**
         * Contains the column name of the user id associated with this trip.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models Trip Enhancement Day information.
     */
    public static final class EnhancementDayColumns implements BaseColumns {

        // Prevent instantiation.
        private EnhancementDayColumns() {
        }

        /**
         * Contains the enhancement day table name.
         */
        public static final String TABLE_NAME = "ENHANCEMENT_DAY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Enhancement Days URI
         */
        private static final String PATH_ENHANCEMENT_DAYS = "/enhancement_days";

        /**
         * Path part for the Enhancement Day ID URI
         */
        private static final String PATH_ENHANCEMENT_DAY_ID = "/enhancement_days/";

        /**
         * 0-relative position of an Enhancement Day ID segment in the path part of a Enhancement Day ID URI
         */
        public static final int ENHANCEMENT_DAY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ENHANCEMENT_DAYS);

        /**
         * The content URI base for an Enhancement Id. Callers must append a numeric Enhancement Day id to this Uri to retrieve an
         * Enhancement Day.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_ENHANCEMENT_DAY_ID);

        /**
         * The content URI match pattern for a single Enhancement Day, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_ENHANCEMENT_DAY_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Enhancement Days.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.enhancement_days";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Enhancement Day.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.enhancement_day";

        // Column definitions

        /**
         * Contains the trip id (INTEGER REFERENCES TRIP._ID) colum name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "type" (TEXT) column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "trip local date" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String TRIP_LOCAL_DATE = "TRIP_LOCAL_DATE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Sortable Segment information.
     */
    public static final class SortableSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private SortableSegmentColumns() {
        }

        /**
         * Contains the Sortable Segment table name.
         */
        public static final String TABLE_NAME = "SORTABLE_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Sortable Segments URI
         */
        private static final String PATH_SORTABLE_SEGMENTS = "/sortable_segments";

        /**
         * Path part for the Sortable Segment ID URI
         */
        private static final String PATH_SORTABLE_SEGMENT_ID = "/sortable_segments/";

        /**
         * 0-relative position of a Sortable Segment ID segment in the path part of a Sortable Segment ID URI
         */
        public static final int SORTABLE_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SORTABLE_SEGMENTS);

        /**
         * The content URI base for a Sortable Segment. Callers must append a numeric Sortable Segment id to this Uri to retrieve
         * a Sortable Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SORTABLE_SEGMENT_ID);

        /**
         * The content URI match pattern for a single trip summary message, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri
                .parse(SCHEME + AUTHORITY + PATH_SORTABLE_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Sortable Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.sortable_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Sortable Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.sortable_segment";

        // Column definitions

        /**
         * Contains the "Enhancement Day Id" (INTEGER REFERENCES ENHANCEMENT_DAY._ID) column name.
         */
        public static String ENHANCEMENT_DAY_ID = "ENHANCEMENT_DAY_ID";

        /**
         * Contains the "booking source" (TEXT) column name.
         */
        public static String BOOKING_SOURCE = "BOOKING_SOURCE";

        /**
         * Contains the "record locator" (TEXT) column name.
         */
        public static String RECORD_LOCATOR = "RECORD_LOCATOR";

        /**
         * Contains the "segment key" (TEXT) column name.
         */
        public static String SEGMENT_KEY = "SEGMENT_KEY";

        /**
         * Contains the "segment side" (TEXT) column name.
         */
        public static String SEGMENT_SIDE = "SEGMENT_SIDE";

        /**
         * Contains the "sort value" (TEXT) column name.
         */
        public static String SORT_VALUE = "SORT_VALUE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Enhance Offer information.
     */
    public static final class EnhancementOfferColumns implements BaseColumns {

        // Prevent instantiation.
        private EnhancementOfferColumns() {
        }

        /**
         * Contains the Enhancement Offer table name.
         */
        public static final String TABLE_NAME = "ENHANCEMENT_OFFER";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Enhancement Offer Messages URI
         */
        private static final String PATH_ENHANCEMENT_OFFERS = "/enhancement_offers";

        /**
         * Path part for the Enhancement Offer ID URI
         */
        private static final String PATH_ENHANCEMENT_OFFER_ID = "/enhancement_offers/";

        /**
         * 0-relative position of an Enhancement Offer ID segment in the path part of an Enhancement Offer ID URI
         */
        public static final int ENHANCEMENT_OFFER_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ENHANCEMENT_OFFERS);

        /**
         * The content URI base for an Enhancement Offer. Callers must append a numeric Enhancement Offer id to this Uri to
         * retrieve an Enhancement Offer.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_ENHANCEMENT_OFFER_ID);

        /**
         * The content URI match pattern for a single Enhancement Offer, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_ENHANCEMENT_OFFER_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Enhancement Offers.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.enhancement_offers";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Enhancement Offer.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.enhancement_offer";

        // Column definitions
        /**
         * Contains the trip id (INTEGER REFERENCES TRIP._ID) colum name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "id" (TEXT) column name.
         */
        public static final String ID = "ID";

        /**
         * Contains the "description" (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the "type" (TEXT) column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Offer Link information.
     */
    public static final class OfferLinkColumns implements BaseColumns {

        // Prevent instantiation.
        private OfferLinkColumns() {
        }

        /**
         * Contains the Offer Link table name.
         */
        public static final String TABLE_NAME = "OFFER_LINK";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Offer Links URI
         */
        private static final String PATH_OFFER_LINKS = "/offer_links";

        /**
         * Path part for the Offer Link ID URI
         */
        private static final String PATH_OFFER_LINK_ID = "/offer_links/";

        /**
         * 0-relative position of an Offer Link ID segment in the path part of an Offer Link ID URI
         */
        public static final int OFFER_LINK_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_OFFER_LINKS);

        /**
         * The content URI base for an Offer Link. Callers must append a numeric Offer Link id to this Uri to retrieve an Offer
         * Link.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_OFFER_LINK_ID);

        /**
         * The content URI match pattern for a single Offer Link, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_OFFER_LINK_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of <items>.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.offer_links";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single <item>.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.offer_link";

        // Column definitions

        /**
         * Contains the "enhancement offer id" (INTEGER REFERENCES ENHANCEMENT_OFFER._ID) column name.
         */
        public static final String ENHANCEMENT_OFFER_ID = "ENHANCEMENT_OFFER_ID";
        /**
         * Contains the "booking source" (TEXT) column name.
         */
        public static final String BOOKING_SOURCE = "BOOKING_SOURCE";

        /**
         * Contains the "record locator" (TEXT) column name.
         */
        public static final String RECORD_LOCATOR = "RECORD_LOCATOR";

        /**
         * Contains the "segment key" (TEXT) column name.
         */
        public static final String SEGMENT_KEY = "SEGMENT_KEY";

        /**
         * Contains the "segment side" (TEXT) column name.
         */
        public static final String SEGMENT_SIDE = "SEGMENT_SIDE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Offer Content information.
     */
    public static final class OfferContentColumns implements BaseColumns {

        // Prevent instantiation.
        private OfferContentColumns() {
        }

        /**
         * Contains the Offer Content table name.
         */
        public static final String TABLE_NAME = "OFFER_CONTENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Offer Contents URI
         */
        private static final String PATH_OFFER_CONTENTS = "/offer_contents";

        /**
         * Path part for the Offer Content ID URI
         */
        private static final String PATH_OFFER_CONTENT_ID = "/offer_contents/";

        /**
         * 0-relative position of a Offer Content ID segment in the path part of an Offer Content ID URI.
         */
        public static final int OFFER_CONTENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_OFFER_CONTENTS);

        /**
         * The content URI base for an Offer Content. Callers must append a numeric Offer Content id to this Uri to retrieve an
         * Offer Content.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_OFFER_CONTENT_ID);

        /**
         * The content URI match pattern for a single Offer Content, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_OFFER_CONTENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Offer Contents.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.offer_contents";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Offer Content.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.offer_content";

        // Column definitions

        /**
         * Contains the "enhancement offer id" (INTEGER REFERENCES ENHANCEMENT_OFFER._ID) column name.
         */
        public static final String ENHANCEMENT_OFFER_ID = "ENHANCEMENT_OFFER_ID";

        /**
         * Contains the "title" (TEXT) column name.
         */
        public static final String TITLE = "TITLE";

        /**
         * Contains the "vendor" (TEXT) column name.
         */
        public static final String VENDOR = "VENDOR";

        /**
         * Contains the "action" (TEXT) column name.
         */
        public static final String ACTION = "ACTION";

        /**
         * Contains the "application" (TEXT) column name.
         */
        public static final String APPLICATION = "APPLICATION";

        /**
         * Contains the "image name" (TEXT) column name.
         */
        public static final String IMAGE_NAME = "IMAGE_NAME";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Content Link information.
     */
    public static final class ContentLinkColumns implements BaseColumns {

        // Prevent instantiation.
        private ContentLinkColumns() {
        }

        /**
         * Contains the Content Link table name.
         */
        public static final String TABLE_NAME = "CONTENT_LINK";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Content Links URI
         */
        private static final String PATH_CONTENT_LINKS = "/content_links";

        /**
         * Path part for the Content Link ID URI
         */
        private static final String PATH_CONTENT_LINK_ID = "/content_links/";

        /**
         * 0-relative position of a Content Link ID segment in the path part of a Content Link ID URI
         */
        public static final int CONTENT_LINK_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CONTENT_LINKS);

        /**
         * The content URI base for a Content Link. Callers must append a numeric Content Link id to this Uri to retrieve a
         * Content Link.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CONTENT_LINK_ID);

        /**
         * The content URI match pattern for a single Content Link, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CONTENT_LINK_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Content Links.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.content_links";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Content Link.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.content_link";

        // Column definitions

        /**
         * Contains the "offer content id" (INTEGER REFERENCES OFFER_CONTENT._ID) column name.
         */
        public static final String OFFER_CONTENT_ID = "OFFER_CONTENT_ID";

        /**
         * Contains the "title" (TEXT) column name.
         */
        public static final String TITLE = "TITLE";

        /**
         * Contains the "action URL" (TEXT) column name.
         */
        public static final String ACTION_URL = "ACTION_URL";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models MAP DISPLAY information.
     */
    public static final class MapDisplayColumns implements BaseColumns {

        // Prevent instantiation.
        private MapDisplayColumns() {
        }

        /**
         * Contains the Map Display table name.
         */
        public static final String TABLE_NAME = "MAP_DISPLAY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Map Displays URI
         */
        private static final String PATH_MAP_DISPLAYS = "/map_displays";

        /**
         * Path part for the Map Display ID URI
         */
        private static final String PATH_MAP_DISPLAY_ID = "/map_displays/";

        /**
         * 0-relative position of a Map Display ID segment in the path part of a Map Display ID URI
         */
        public static final int MAP_DISPLAY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MAP_DISPLAYS);

        /**
         * The content URI base for a Map Display. Callers must append a numeric Map Display id to this Uri to retrieve a Map
         * Display.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_MAP_DISPLAY_ID);

        /**
         * The content URI match pattern for a single Map Display, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_MAP_DISPLAY_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Map Displays.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.map_displays";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Map Display.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.map_display";

        // Column definitions

        /**
         * Contains the "offer content id" (INTEGER REFERENCES OFFER_CONTENT._ID) column name.
         */
        public static final String OFFER_CONTENT_ID = "OFFER_CONTENT_ID";

        /**
         * Contains the "latitude" (REAL) column name.
         */
        public static final String LATITUDE = "LATITUDE";

        /**
         * Contains the "longitude" (REAL) column name.
         */
        public static final String LONGITUDE = "LONGITUDE";

        /**
         * Contains the "dimension km" (REAL) column name.
         */
        public static final String DIMENSION_KM = "DIMENSION_KM";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models DISPLAY OVERLAY information.
     */
    public static final class DisplayOverlayColumns implements BaseColumns {

        // Prevent instantiation.
        private DisplayOverlayColumns() {
        }

        /**
         * Contains the Display Overlay table name.
         */
        public static final String TABLE_NAME = "DISPLAY_OVERLAY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Display Overlays URI
         */
        private static final String PATH_DISPLAY_OVERLAYS = "/display_overlays";

        /**
         * Path part for the Display Overlay ID URI
         */
        private static final String PATH_DISPLAY_OVERLAY_ID = "/display_overlays/";

        /**
         * 0-relative position of a Display Overlay ID segment in the path part of a Display Overlay ID URI
         */
        public static final int DISPLAY_OVERLAY_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_DISPLAY_OVERLAYS);

        /**
         * The content URI base for a Display Overlay. Callers must append a numeric Display Overlay id to this Uri to retrieve a
         * Display Overlay.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_DISPLAY_OVERLAY_ID);

        /**
         * The content URI match pattern for a single Display Overlay, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_DISPLAY_OVERLAY_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Display Overlays.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.display_overlays";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Display Overlay.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.display_overlay";

        // Column definitions

        /**
         * Contains the "map display id" (INTEGER REFERENCES MAP_DISPLAY._ID) column name.
         */
        public static final String MAP_DISPLAY_ID = "MAP_DISPLAY_ID";

        /**
         * Contains the "name" (TEXT) column name.
         */
        public static final String NAME = "NAME";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Validity Location information.
     */
    public static final class ValidityLocationColumns implements BaseColumns {

        // Prevent instantiation.
        private ValidityLocationColumns() {
        }

        /**
         * Contains the Validity Location table name.
         */
        public static final String TABLE_NAME = "VALIDITY_LOCATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Validity Location URI
         */
        private static final String PATH_VALIDITY_LOCATIONS = "/validity_locations";

        /**
         * Path part for the Validity Location ID URI
         */
        private static final String PATH_VALIDITY_LOCATION_ID = "/validity_locations/";

        /**
         * 0-relative position of a Validity Location ID segment in the path part of a Validity Location ID URI
         */
        public static final int VALIDITY_LOCATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_VALIDITY_LOCATIONS);

        /**
         * The content URI base for a Validity Location. Callers must append a numeric Validity Location id to this Uri to
         * retrieve a Validity Location.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_VALIDITY_LOCATION_ID);

        /**
         * The content URI match pattern for a single Validity Location, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_VALIDITY_LOCATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Validity Locations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.validity_locations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Validity Location.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.validity_location";

        // Column definitions

        /**
         * Contains the "enhancement offer id" (INTEGER REFERENCES ENHANCEMENT_OFFER._ID) column name.
         */
        public static final String ENHANCEMENT_OFFER_ID = "ENHANCEMENT_OFFER_ID";

        /**
         * Contains the "latitude" (REAL) column name.
         */
        public static final String LATITUDE = "LATITUDE";

        /**
         * Contains the "longitude" (REAL) column name.
         */
        public static final String LONGITUDE = "LONGITUDE";

        /**
         * Contains the "proximity" (REAL) column name.
         */
        public static final String PROXIMITY = "PROXIMITY";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Validity Time Range information.
     */
    public static final class ValidityTimeRangeColumns implements BaseColumns {

        // Prevent instantiation.
        private ValidityTimeRangeColumns() {
        }

        /**
         * Contains the Validity Time Range table name.
         */
        public static final String TABLE_NAME = "VALIDITY_TIME_RANGE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Validity Time Ranges URI
         */
        private static final String PATH_VALIDITY_TIME_RANGES = "/validity_time_ranges";

        /**
         * Path part for the Validity Time Range ID URI
         */
        private static final String PATH_VALIDITY_TIME_RANGE_ID = "/validity_time_ranges/";

        /**
         * 0-relative position of a Validity Time Range ID segment in the path part of a Validity Time Range ID URI
         */
        public static final int VALIDITY_TIME_RANGE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_VALIDITY_TIME_RANGES);

        /**
         * The content URI base for a Validity Time Range. Callers must append a numeric Validity Time Range id to this Uri to
         * retrieve a Validity Time Range.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_VALIDITY_TIME_RANGE_ID);

        /**
         * The content URI match pattern for a single Validity Time Range, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_VALIDITY_TIME_RANGE_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Validity Time Ranges.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.validity_time_ranges";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Validity Time Range.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.validity_time_range";

        // Column definitions

        /**
         * Contains the "enhancement offer id" (INTEGER REFERENCES ENHANCEMENT_OFFER._ID) column name.
         */
        public static final String ENHANCEMENT_OFFER_ID = "ENHANCEMENT_OFFER_ID";

        /**
         * Contains the "start date time UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_TIME_UTC = "START_DATE_TIME_UTC";

        /**
         * Contains the "end date time UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_TIME_UTC = "END_DATE_TIME_UTC";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Trip Rule Violation information.
     */
    public static final class TripRuleViolationColumns implements BaseColumns {

        // Prevent instantiation.
        private TripRuleViolationColumns() {
        }

        /**
         * Contains the Trip Rule Violation table name.
         */
        public static final String TABLE_NAME = "TRIP_RULE_VIOLATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Trip Rule Violations URI
         */
        private static final String PATH_TRIP_RULE_VIOLATIONS = "/trip_rule_violations";

        /**
         * Path part for the Trip Rule Violation ID URI
         */
        private static final String PATH_TRIP_RULE_VIOLATION_ID = "/trip_rule_violations/";

        /**
         * 0-relative position of a Trip Rule Violation ID segment in the path part of a Trip Rule Violation ID URI
         */
        public static final int TRIP_RULE_VIOLATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_RULE_VIOLATIONS);

        /**
         * The content URI base for a Trip Rule Violation. Callers must append a numeric Trip Rule Violation id to this Uri to
         * retrieve a Trip Rule Violation.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_RULE_VIOLATION_ID);

        /**
         * The content URI match pattern for a single trip rule violation, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TRIP_RULE_VIOLATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Trip Rule Violations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.trip_rule_violations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Trip Rule Violation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.trip_rule_violation";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Car Rule Violation information.
     */
    public static final class CarRuleViolationColumns implements BaseColumns {

        // Prevent instantiation.
        private CarRuleViolationColumns() {
        }

        /**
         * Contains the Car Rule Violation table name.
         */
        public static final String TABLE_NAME = "CAR_RULE_VIOLATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Car Rule Violations URI
         */
        private static final String PATH_CAR_RULE_VIOLATIONS = "/car_rule_violations";

        /**
         * Path part for the Car Rule Violation ID URI
         */
        private static final String PATH_CAR_RULE_VIOLATION_ID = "/car_rule_violations/";

        /**
         * 0-relative position of a Car Rule Violation ID segment in the path part of a Car Rule Violation ID URI
         */
        public static final int CAR_RULE_VIOLATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_RULE_VIOLATIONS);

        /**
         * The content URI base for a Car Rule Violation. Callers must append a numeric Car Rule Violation id to this Uri to
         * retrieve a Car Rule Violation.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_RULE_VIOLATION_ID);

        /**
         * The content URI match pattern for a single Car Rule Violation, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_RULE_VIOLATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Car Rule Violations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.car_rule_violations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Car Rule Violation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.car_rule_violation";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "daily rate" (TEXT) column name.
         */
        public static final String DAILY_RATE = "DAILY_RATE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Hotel Rule Violation information.
     */
    public static final class HotelRuleViolationColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelRuleViolationColumns() {
        }

        /**
         * Contains the Hotel Rule Violation table name.
         */
        public static final String TABLE_NAME = "HOTEL_RULE_VIOLATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Rule Violation URI
         */
        private static final String PATH_HOTEL_RULE_VIOLATIONS = "/hotel_rule_violations";

        /**
         * Path part for the Hotel Rule Violation ID URI
         */
        private static final String PATH_HOTEL_RULE_VIOLATION_ID = "/hotel_rule_violations/";

        /**
         * 0-relative position of a Hotel Rule Violation ID segment in the path part of a Hotel Rule Violation ID URI
         */
        public static final int HOTEL_RULE_VIOLATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_RULE_VIOLATIONS);

        /**
         * The content URI base for a Hotel Rule Violation. Callers must append a numeric <item> id to this Uri to retrieve a
         * Hotel Rule Violation.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_RULE_VIOLATION_ID);

        /**
         * The content URI match pattern for a single Hotel Rule Violation, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_RULE_VIOLATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Rule Violations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_rule_violations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Rule Violation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_rule_violation";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "rate" (TEXT) column name.
         */
        public static final String RATE = "RATE";

        /**
         * Contains the "name" (TEXT) column name.
         */
        public static final String NAME = "NAME";

        /**
         * Contains the "address" (TEXT) column name.
         */
        public static final String ADDRESS = "ADDRESS";

        /**
         * Contains the "description" (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Flight Rule Violation information.
     */
    public static final class FlightRuleViolationColumns implements BaseColumns {

        // Prevent instantiation.
        private FlightRuleViolationColumns() {
        }

        /**
         * Contains the Flight Rule Violation table name.
         */
        public static final String TABLE_NAME = "FLIGHT_RULE_VIOLATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Flight Rule Violations URI
         */
        private static final String PATH_FLIGHT_RULE_VIOLATIONS = "/flight_rule_violations";

        /**
         * Path part for the Flight Rule Violation ID URI
         */
        private static final String PATH_FLIGHT_RULE_VIOLATION_ID = "/flight_rule_violations/";

        /**
         * 0-relative position of a Flight Rule Violation ID segment in the path part of a Flight Rule Violation ID URI
         */
        public static final int FLIGHT_RULE_VIOLATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_FLIGHT_RULE_VIOLATIONS);

        /**
         * The content URI base for a Flight Rule Violation. Callers must append a numeric Flight Rule Violation id to this Uri to
         * retrieve a Flight Rule Violation.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_FLIGHT_RULE_VIOLATION_ID);

        /**
         * The content URI match pattern for a single Flight Rule Violation, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_FLIGHT_RULE_VIOLATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Flight Rule Violations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.flight_rule_violations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Flight Rule Violation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.flight_rule_violation";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the boolean "refundable" (INTEGER) column name.
         */
        public static final String REFUNDABLE = "REFUNDABLE";

        /**
         * Contains the "cost" (TEXT) column name.
         */
        public static final String COST = "COST";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Rail Rule Violation information.
     */
    public static final class RailRuleViolationColumns implements BaseColumns {

        // Prevent instantiation.
        private RailRuleViolationColumns() {
        }

        /**
         * Contains the Rail Rule Violation table name.
         */
        public static final String TABLE_NAME = "RAIL_RULE_VIOLATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Rail Rule Violations URI
         */
        private static final String PATH_RAIL_RULE_VIOLATIONS = "/rail_rule_violations";

        /**
         * Path part for the Rail Rule Violation ID URI
         */
        private static final String PATH_RAIL_RULE_VIOLATION_ID = "/rail_rule_violations/";

        /**
         * 0-relative position of a Rail Rule Violation ID segment in the path part of a Rail Rule Violation ID URI
         */
        public static final int RAIL_RULE_VIOLATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RAIL_RULE_VIOLATIONS);

        /**
         * The content URI base for a Rail Rule Violation. Callers must append a numeric Rail Rule Violation id to this Uri to
         * retrieve a Rail Rule Violation.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RAIL_RULE_VIOLATION_ID);

        /**
         * The content URI match pattern for a single rail rule violation, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RAIL_RULE_VIOLATION_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Rail Rule Violations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.rail_rule_violations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Rail Rule Violation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.rail_rule_violation";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "rate" (TEXT) column name.
         */
        public static final String RATE = "RATE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Rule information.
     */
    public static final class RuleColumns implements BaseColumns {

        // Prevent instantiation.
        private RuleColumns() {
        }

        /**
         * Contains the Rule table name.
         */
        public static final String TABLE_NAME = "RULE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Rules URI
         */
        private static final String PATH_RULES = "/rules";

        /**
         * Path part for the Rule ID URI
         */
        private static final String PATH_RULE_ID = "/rules/";

        /**
         * 0-relative position of a Rule ID segment in the path part of a Rule ID URI
         */
        public static final int RULE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RULES);

        /**
         * The content URI base for a Rule. Callers must append a numeric Rule id to this Uri to retrieve a Rule.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_ID);

        /**
         * The content URI match pattern for a single Rule, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Rules.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.rules";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Rule.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.rule";

        // Column definitions

        /**
         * Contains the "text" (TEXT) column name.
         */
        public static final String TEXT = "TEXT";

        /**
         * Contains the "trip rule violation ID" (INTEGER REFERENCES TRIP_RULE_VIOLATION._ID) column name.
         */
        public static final String TRIP_RULE_VIOLATION_ID = "TRIP_RULE_VIOLATION_ID";

        /**
         * Contains the "car rule violation ID" (INTEGER REFERENCES CAR_RULE_VIOLATION._ID) column name.
         */
        public static final String CAR_RULE_VIOLATION_ID = "CAR_RULE_VIOLATION_ID";

        /**
         * Contains the "hotel rule violation ID" (INTEGER REFERENCES HOTEL_RULE_VIOLATION._ID) column name.
         */
        public static final String HOTEL_RULE_VIOLATION_ID = "HOTEL_RULE_VIOLATION_ID";

        /**
         * Contains the "flight rule violation ID" (INTEGER REFERENCES FLIGHT_RULE_VIOLATION._ID) column name.
         */
        public static final String FLIGHT_RULE_VIOLATION_ID = "FLIGHT_RULE_VIOLATION_ID";

        /**
         * Contains the "rail rule violation ID" (INTEGER REFERENCES RAIL_RULE_VIOLATION._ID) column name.
         */
        public static final String RAIL_RULE_VIOLATION_ID = "RAIL_RULE_VIOLATION_ID";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Rule Violation Reason information.
     */
    public static final class RuleViolationReasonColumns implements BaseColumns {

        // Prevent instantiation.
        private RuleViolationReasonColumns() {
        }

        /**
         * Contains the Rule Violation Reason table name.
         */
        public static final String TABLE_NAME = "RULE_VIOLATION_REASON";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Rule Violation Reasons URI
         */
        private static final String PATH_RULE_VIOLATION_REASONS = "/rule_violation_reasons";

        /**
         * Path part for the Rule Violation Reason ID URI
         */
        private static final String PATH_RULE_VIOLATION_REASON_ID = "/rule_violation_reasons/";

        /**
         * 0-relative position of a Rule Violation Reason ID segment in the path part of a Rule Violation Reason ID URI
         */
        public static final int RULE_VIOLATION_REASON_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_VIOLATION_REASONS);

        /**
         * The content URI base for a Rule Violation Reason. Callers must append a numeric Rule Violation Reason id to this Uri to
         * retrieve a Rule Violation Reason.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_VIOLATION_REASON_ID);

        /**
         * The content URI match pattern for a single Rule Violation Reason, specified by its ID. Use this to match incoming URIs
         * or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RULE_VIOLATION_REASON_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Rule Violation Reasons.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.rule_violation_reasons";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Rule Violation Reason.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.rule_violation_reason";

        // Column definitions

        /**
         * Contains the "reason" (TEXT) column name.
         */
        public static final String REASON = "REASON";

        /**
         * Contains the "comments" (TEXT) column name.
         */
        public static final String COMMENTS = "COMMENTS";

        /**
         * Contains the "trip rule violation ID" (INTEGER REFERENCES TRIP_RULE_VIOLATION._ID) column name.
         */
        public static final String TRIP_RULE_VIOLATION_ID = "TRIP_RULE_VIOLATION_ID";

        /**
         * Contains the "car rule violation ID" (INTEGER REFERENCES CAR_RULE_VIOLATION._ID) column name.
         */
        public static final String CAR_RULE_VIOLATION_ID = "CAR_RULE_VIOLATION_ID";

        /**
         * Contains the "hotel rule violation ID" (INTEGER REFERENCES HOTEL_RULE_VIOLATION._ID) column name.
         */
        public static final String HOTEL_RULE_VIOLATION_ID = "HOTEL_RULE_VIOLATION_ID";

        /**
         * Contains the "flight rule violation ID" (INTEGER REFERENCES FLIGHT_RULE_VIOLATION._ID) column name.
         */
        public static final String FLIGHT_RULE_VIOLATION_ID = "FLIGHT_RULE_VIOLATION_ID";

        /**
         * Contains the "rail rule violation ID" (INTEGER REFERENCES RAIL_RULE_VIOLATION._ID) column name.
         */
        public static final String RAIL_RULE_VIOLATION_ID = "RAIL_RULE_VIOLATION_ID";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Travel Point information.
     */
    public static final class TravelPointColumns implements BaseColumns {

        // Prevent instantiation.
        private TravelPointColumns() {
        }

        /**
         * Contains the Travel Point table name.
         */
        public static final String TABLE_NAME = "TRAVEL_POINT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Travel Points URI
         */
        private static final String PATH_TRAVEL_POINTS = "/travel_points";

        /**
         * Path part for the Travel Point ID URI
         */
        private static final String PATH_TRAVEL_POINT_ID = "/travel_points/";

        /**
         * 0-relative position of a Travel Point ID segment in the path part of a Travel Point ID URI
         */
        public static final int TRAVEL_POINT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TRAVEL_POINTS);

        /**
         * The content URI base for a Travel Point. Callers must append a numeric Travel Point id to this Uri to retrieve a Travel
         * Point.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TRAVEL_POINT_ID);

        /**
         * The content URI match pattern for a single Travel Point, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TRAVEL_POINT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Travel Points.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.travel_points";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Travel Point.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.travel_point";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "segment id" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "benchmark" (TEXT) column name.
         */
        public static final String BENCHMARK = "BENCHMARK";

        /**
         * Contains the "benchmark currency" (TEXT) column name.
         */
        public static final String BENCHMARK_CURRENCY = "BENCHMARK_CURRENCY";

        /**
         * Contains the "points posted" (TEXT) column name.
         */
        public static final String POINTS_POSTED = "POINTS_POSTED";

        /**
         * Contains the "points pending" (TEXT) column name.
         */
        public static final String POINTS_PENDING = "POINTS_PENDING";

        /**
         * Contains the "total points" (TEXT) column name.
         */
        public static final String TOTAL_POINTS = "TOTAL_POINTS";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Booking information.
     */
    public static final class BookingColumns implements BaseColumns {

        // Prevent instantiation.
        private BookingColumns() {
        }

        /**
         * Contains the Booking table name.
         */
        public static final String TABLE_NAME = "BOOKING";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Bookings URI
         */
        private static final String PATH_BOOKINGS = "/bookings";

        /**
         * Path part for the Booking ID URI
         */
        private static final String PATH_BOOKING_ID = "/bookings/";

        /**
         * 0-relative position of a Booking ID segment in the path part of a Booking ID URI
         */
        public static final int BOOKING_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_BOOKINGS);

        /**
         * The content URI base for a Booking. Callers must append a numeric Booking id to this Uri to retrieve a Booking.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_BOOKING_ID);

        /**
         * The content URI match pattern for a single Booking, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_BOOKING_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Bookings.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.bookings";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Booking.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.booking";

        // Column definitions

        /**
         * Contains the "trip id" (INTEGER REFERENCES TRIP._ID) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the "agency pcc" (TEXT) column name.
         */
        public static final String AGENCY_PCC = "AGENCY_PCC";

        /**
         * Contains the "booking source" (TEXT) column name.
         */
        public static final String BOOKING_SOURCE = "BOOKING_SOURCE";

        /**
         * Contains the "company accounting code" (TEXT) column name.
         */
        public static final String COMPANY_ACCOUNTING_CODE = "COMPANY_ACCOUNTING_CODE";

        /**
         * Contains the "date booked local" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String DATE_BOOKED_LOCAL = "DATE_BOOKED_LOCAL";

        /**
         * Contains the boolean "is cliqbook system of record" (INTEGER) column name.
         */
        public static final String IS_CLIQBOOK_SYSTEM_OF_RECORD = "IS_CLIQBOOK_SYSTEM_OF_RECORD";

        /**
         * Contains the "record locator" (TEXT) column name.
         */
        public static final String RECORD_LOCATOR = "RECORD_LOCATOR";

        /**
         * Contains the "travel config id" (TEXT) column name.
         */
        public static final String TRAVEL_CONFIG_ID = "TRAVEL_CONFIG_ID";

        /**
         * Contains the "type" (TEXT) column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the boolean "is gds booking" (INTEGER) column name.
         */
        public static final String IS_GDS_BOOKING = "IS_GDS_BOOKING";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Airline Ticket information.
     */
    public static final class AirlineTicketColumns implements BaseColumns {

        // Prevent instantiation.
        private AirlineTicketColumns() {
        }

        /**
         * Contains the Airine Ticket table name.
         */
        public static final String TABLE_NAME = "AIRLINE_TICKET";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Airline Tickets URI
         */
        private static final String PATH_AIRLINE_TICKETS = "/airline_tickets";

        /**
         * Path part for the Airline Ticket ID URI
         */
        private static final String PATH_AIRLINE_TICKET_ID = "/airline_tickets/";

        /**
         * 0-relative position of an Airline Ticket ID segment in the path part of an Airline Ticket ID URI
         */
        public static final int AIRLINE_TICKET_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_AIRLINE_TICKETS);

        /**
         * The content URI base for an Airline Ticket. Callers must append a numeric Airline Ticket id to this Uri to retrieve an
         * Airline Ticket.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_AIRLINE_TICKET_ID);

        /**
         * The content URI match pattern for a single Airline Ticket, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_AIRLINE_TICKET_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Airline Tickets.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.airline_tickets";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Airline Ticket.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.airline_ticket";

        // Column definitions

        /**
         * Contains the "booking id" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        // TODO: Need to determine what other possible columns should be defined in this table.

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Passenger information.
     */
    public static final class PassengerColumns implements BaseColumns {

        // Prevent instantiation.
        private PassengerColumns() {
        }

        /**
         * Contains the Passenger table name.
         */
        public static final String TABLE_NAME = "PASSENGER";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Passengers URI
         */
        private static final String PATH_PASSENGERS = "/passengers";

        /**
         * Path part for the Passengers ID URI
         */
        private static final String PATH_PASSENGER_ID = "/passengers/";

        /**
         * 0-relative position of a Passenger ID segment in the path part of a Passenger ID URI
         */
        public static final int PASSENGER_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PASSENGERS);

        /**
         * The content URI base for a Passenger. Callers must append a numeric Passenger id to this Uri to retrieve a Passenger.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PASSENGER_ID);

        /**
         * The content URI match pattern for a single Passenger, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PASSENGER_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Passengers.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.passengers";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Passenger.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.passenger";

        // Column definitions

        /**
         * Contains the "booking id" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "first name" (TEXT) column name.
         */
        public static final String FIRST_NAME = "FIRST_NAME";

        /**
         * Contains the "last name" (TEXT) column name.
         */
        public static final String LAST_NAME = "LAST_NAME";

        /**
         * Contains the "identifier" (TEXT) column name.
         */
        public static final String IDENTIFIER = "IDENTIFIER";

        /**
         * Contains the "passenger key" (TEXT) column name.
         */
        public static final String PASSENGER_KEY = "PASSENGER_KEY";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Frequent Traveler Program information.
     */
    public static final class FrequentTravelerProgramColumns implements BaseColumns {

        // Prevent instantiation.
        private FrequentTravelerProgramColumns() {
        }

        /**
         * Contains the Frequent Traveler Program table name.
         */
        public static final String TABLE_NAME = "FREQUENT_TRAVELER_PROGRAM";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Frequent Traveler Programs URI
         */
        private static final String PATH_FREQUENT_TRAVELER_PROGRAMS = "/frequent_traveler_programs";

        /**
         * Path part for the Frequent Traveler Program ID URI
         */
        private static final String PATH_FREQUENT_TRAVELER_PROGRAM_ID = "/frequent_traveler_programs/";

        /**
         * 0-relative position of a Frequent Traveler Program ID segment in the path part of a Frequent Traveler Program ID URI
         */
        public static final int FREQUENT_TRAVELER_PROGRAM_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_FREQUENT_TRAVELER_PROGRAMS);

        /**
         * The content URI base for a Frequent Traveler Program. Callers must append a numeric Frequent Traveler Program id to
         * this Uri to retrieve a Frequent Traveler Program.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_FREQUENT_TRAVELER_PROGRAM_ID);

        /**
         * The content URI match pattern for a single Frequent Traveler Program, specified by its ID. Use this to match incoming
         * URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY
                + PATH_FREQUENT_TRAVELER_PROGRAM_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Frequent Traveler Programs.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.frequent_traveler_programs";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Frequent Traveler Program.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.frequent_traveler_program";

        // Column definitions

        /**
         * Contains the "passenger id" (INTEGER REFERENCES PASSENGER._ID) column name.
         */
        public static final String PASSENGER_ID = "PASSENGER_ID";

        /**
         * Contains the "airline vendor" (TEXT) column name.
         */
        public static final String AIRLINE_VENDOR = "AIRLINE_VENDOR";

        /**
         * Contains the "program number" (TEXT) column name.
         */
        public static final String PROGRAM_NUMBER = "PROGRAM_NUMBER";

        /**
         * Contains the "program vendor" (TEXT) column name.
         */
        public static final String PROGRAM_VENDOR = "PROGRAM_VENDOR";

        /**
         * Contains the "program vendor code" (TEXT) column name.
         */
        public static final String PROGRAM_VENDOR_CODE = "PROGRAM_VENDOR_CODE";

        /**
         * Contains the "status" (TEXT) column name.
         */
        public static final String STATUS = "STATUS";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Segment information.
     */
    public static final class SegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private SegmentColumns() {
        }

        /**
         * Contains the Segment table name.
         */
        public static final String TABLE_NAME = "SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Segments URI
         */
        private static final String PATH_SEGMENTS = "/segments";

        /**
         * Path part for the Segment ID URI
         */
        private static final String PATH_SEGMENT_ID = "/segments/";

        /**
         * 0-relative position of a Segment ID segment in the path part of a Segment ID URI
         */
        public static final int SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SEGMENTS);

        /**
         * The content URI base for a Segment. Callers must append a numeric Segment id to this Uri to retrieve a Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.segment";

        // Column definitions

        /**
         * Contains the "booking id" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "start date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_UTC = "START_DATE_UTC";

        /**
         * Contains the "end date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_UTC = "END_DATE_UTC";

        /**
         * Contains the "start date local" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String START_DATE_LOCAL = "START_DATE_LOCAL";

        /**
         * Contains the "end date UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String END_DATE_LOCAL = "END_DATE_LOCAL";

        /**
         * Contains the "confirmation number" (TEXT) column name.
         */
        public static final String CONFIRMATION_NUMBER = "CONFIRMATION_NUMBER";

        /**
         * Contains the "credit card id" (TEXT) column name.
         */
        public static final String CREDIT_CARD_ID = "CREDIT_CARD_ID";

        /**
         * Contains the "credit card last four" (TEXT) column name.
         */
        public static final String CREDIT_CARD_LAST_FOUR = "CREDIT_CARD_LAST_FOUR";

        /**
         * Contains the "credit card type" (TEXT) column name.
         */
        public static final String CREDIT_CARD_TYPE = "CREDIT_CARD_TYPE";

        /**
         * Contains the "credit card type localized" (TEXT) column name.
         */
        public static final String CREDIT_CARD_TYPE_LOCALIZED = "CREDIT_CARD_TYPE_LOCALIZED";

        /**
         * Contains the "currency" (TEXT) column name.
         */
        public static final String CURRENCY = "CURRENCY";

        /**
         * Contains the "ereceipt status" (TEXT) column name.
         */
        public static final String ERECEIPT_STATUS = "ERECEIPT_STATUS";

        /**
         * Contains the "end address" (TEXT) column name.
         */
        public static final String END_ADDRESS = "END_ADDRESS";

        /**
         * Contains the "end address 2" (TEXT) column name.
         */
        public static final String END_ADDRESS_2 = "END_ADDRESS_2";

        /**
         * Contains the "end city" (TEXT) column name.
         */
        public static final String END_CITY = "END_CITY";

        /**
         * Contains the "end city code" (TEXT) column name.
         */
        public static final String END_CITY_CODE = "END_CITY_CODE";

        /**
         * Contains the "end city code localized" (TEXT) column name.
         */
        public static final String END_CITY_CODE_LOCALIZED = "END_CITY_CODE_LOCALIZED";

        /**
         * Contains the "end country" (TEXT) column name.
         */
        public static final String END_COUNTRY = "END_COUNTRY";

        /**
         * Contains the "end country code" (TEXT) column name.
         */
        public static final String END_COUNTRY_CODE = "END_COUNTRY_CODE";
        /**
         * Contains the "end latitude" (REAL) column name.
         */
        public static final String END_LATITUDE = "END_LATITUDE";
        /**
         * Contains the "end longitude" (REAL) column name.
         */
        public static final String END_LONGITUDE = "END_LONGITUDE";

        /**
         * Contains the "end postal code" (TEXT) column name.
         */
        public static final String END_POSTAL_CODE = "END_POSTAL_CODE";

        /**
         * Contains the "end state" (TEXT) column name.
         */
        public static final String END_STATE = "END_STATE";

        /**
         * Contains the "frequent traveler id" (TEXT) column name.
         */
        public static final String FREQUENT_TRAVELER_ID = "FREQUENT_TRAVELER_ID";

        /**
         * Contains the "image vendor URI" (TEXT) column name.
         */
        public static final String IMAGE_VENDOR_URI = "IMAGE_VENDOR_URI";

        /**
         * Contains the "num persons" (INTEGER) column name.
         */
        public static final String NUM_PERSONS = "NUM_PERSONS";

        /**
         * Contains the "operated by vendor" (TEXT) column name.
         */
        public static final String OPERATED_BY_VENDOR = "OPERATED_BY_VENDOR";

        /**
         * Contains the "operated by vendor name" (TEXT) column name.
         */
        public static final String OPERATED_BY_VENDOR_NAME = "OPERATED_BY_VENDOR_NAME";

        /**
         * Contains the "phone number" (TEXT) column name.
         */
        public static final String PHONE_NUMBER = "PHONE_NUMBER";

        /**
         * Contains the "rate code" (TEXT) column name.
         */
        public static final String RATE_CODE = "RATE_CODE";

        /**
         * Contains the "segment key" (TEXT) column name.
         */
        public static final String SEGMENT_KEY = "SEGMENT_KEY";

        /**
         * Contains the "segment locator" (TEXT) column name.
         */
        public static final String SEGMENT_LOCATOR = "SEGMENT_LOCATOR";

        /**
         * Contains the "segment name" (TEXT) column name.
         */
        public static final String SEGMENT_NAME = "SEGMENT_NAME";

        /**
         * Contains the "start address" (TEXT) column name.
         */
        public static final String START_ADDRESS = "START_ADDRESS";

        /**
         * Contains the "start address 2" (TEXT) column.
         */
        public static final String START_ADDRESS_2 = "START_ADDRESS_2";

        /**
         * Contains the "start city" (TEXT) column.
         */
        public static final String START_CITY = "START_CITY";

        /**
         * Contains the "start city code (TEXT) column name.
         */
        public static final String START_CITY_CODE = "START_CITY_CODE";

        /**
         * Contains the "start country" (TEXT) column name.
         */
        public static final String START_COUNTRY = "START_COUNTRY";

        /**
         * Contains the "start country code" (TEXT) column name.
         */
        public static final String START_COUNTRY_CODE = "START_COUNTRY_CODE";

        /**
         * Contains the "start latitude" (REAL) column name.
         */
        public static final String START_LATITUDE = "START_LATITUDE";

        /**
         * Contains the "start longitude" (REAL) column name.
         */
        public static final String START_LONGITUDE = "START_LONGITUDE";

        /**
         * Contains the "start postal code" (TEXT) column name.
         */
        public static final String START_POSTAL_CODE = "START_POSTAL_CODE";

        /**
         * Contains the "start state" (TEXT) column name.
         */
        public static final String START_STATE = "START_STATE";

        /**
         * Contains the "status" (TEXT) column name.
         */
        public static final String STATUS = "STATUS";

        /**
         * Contains the "status localized" (TEXT) column name.
         */
        public static final String STATUS_LOCALIZED = "STATUS_LOCALIZED";

        /**
         * Contains the "timezone ID" (TEXT) column name.
         */
        public static final String TIMEZONE_ID = "TIMEZONE_ID";

        /**
         * Contains the "total rate" (REAL) column name.
         */
        public static final String TOTAL_RATE = "TOTAL_RATE";

        /**
         * Contains the "type" (TEXT) column name.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "type localized" (TEXT) column name.
         */
        public static final String TYPE_LOCALIZED = "TYPE_LOCALIZED";

        /**
         * Contains the "vendor" (TEXT) column name.
         */
        public static final String VENDOR = "VENDOR";

        /**
         * Contains the "vendor name" (TEXT) column name.
         */
        public static final String VENDOR_NAME = "VENDOR_NAME";

        /**
         * Contains the "vendor URL" (TEXT) column name.
         */
        public static final String VENDOR_URL = "VENDOR_URL";

        /**
         * Contains the "eticket" (TEXT) column name.
         */
        public static final String ETICKET = "ETICKET";

        /**
         * Contains the "id key" (TEXT) column name.
         */
        public static final String ID_KEY = "ID_KEY";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Air Segment information.
     */
    public static final class AirSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private AirSegmentColumns() {
        }

        /**
         * Contains the Air Segment table name.
         */
        public static final String TABLE_NAME = "AIR_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Air Segments URI
         */
        private static final String PATH_AIR_SEGMENTS = "/air_segments";

        /**
         * Path part for the Air Segment ID URI
         */
        private static final String PATH_AIR_SEGMENT_ID = "/air_segments/";

        /**
         * 0-relative position of an Air Segment ID segment in the path part of an Air Segment ID URI
         */
        public static final int AIR_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_AIR_SEGMENTS);

        /**
         * The content URI base for an Air Segment. Callers must append a numeric Air Segment id to this Uri to retrieve an Air
         * Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_AIR_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Air Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_AIR_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Air Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.air_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Air Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.air_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "aircraft code" (TEXT) column name.
         */
        public static final String AIRCRAFT_CODE = "AIRCRAFT_CODE";

        /**
         * Contains the "aircraft name" (TEXT) column name.
         */
        public static final String AIRCRAFT_NAME = "AIRCRAFT_NAME";

        /**
         * Contains the "cabin" (TEXT) column name.
         */
        public static final String CABIN = "CABIN";

        /**
         * Contains the "checked baggage" (TEXT) column name.
         */
        public static final String CHECKED_BAGGAGE = "CHECKED_BAGGAGE";

        /**
         * Contains the "class of service" (TEXT) column name.
         */
        public static final String CLASS_OF_SERVICE = "CLASS_OF_SERVICE";

        /**
         * Contains the "class of service localized" (TEXT) column name.
         */
        public static final String CLASS_OF_SERVICE_LOCALIZED = "CLASS_OF_SERVICE_LOCALIZED";

        /**
         * Contains the "duration" (INTEGER) column name.
         */
        public static final String DURATION = "DURATION";

        /**
         * Contains the "end airport city" (TEXT) column name.
         */
        public static final String END_AIRPORT_CITY = "END_AIRPORT_CITY";

        /**
         * Contains the "end airport country" (TEXT) column name.
         */
        public static final String END_AIRPORT_COUNTRY = "END_AIRPORT_COUNTRY";

        /**
         * Contains the "end airport country code" (TEXT) column name.
         */
        public static final String END_AIRPORT_COUNTRY_CODE = "END_AIRPORT_COUNTRY_CODE";

        /**
         * Contains the "end airport name" (TEXT) column name.
         */
        public static final String END_AIRPORT_NAME = "END_AIRPORT_NAME";

        /**
         * Contains the "end airport state" (TEXT) column name.
         */
        public static final String END_AIRPORT_STATE = "END_AIRPORT_STATE";

        /**
         * Contains the "end gate" (TEXT) column name.
         */
        public static final String END_GATE = "END_GATE";

        /**
         * Contains the "end terminal" (TEXT) column name.
         */
        public static final String END_TERMINAL = "END_TERMINAL";

        /**
         * Contains the "fare basis code" (TEXT) column name.
         */
        public static final String FARE_BASIS_CODE = "FARE_BASIS_CODE";

        /**
         * Contains the "flight number" (TEXT) column name.
         */
        public static final String FLIGHT_NUMBER = "FLIGHT_NUMBER";

        /**
         * Contains the "leg ID" (INTEGER) column name.
         */
        public static final String LEG_ID = "LEG_ID";

        /**
         * Contains the "meals" (TEXT) column name.
         */
        public static final String MEALS = "MEALS";

        /**
         * Contains the "miles" (INTEGER) column name.
         */
        public static final String MILES = "MILES";

        /**
         * Contains the "num stops" (INTEGER) column name.
         */
        public static final String NUM_STOPS = "NUM_STOPS";

        /**
         * Contains the "operated by flight number" (TEXT) column name.
         */
        public static final String OPERATED_BY_FLIGHT_NUMBER = "OPERATED_BY_FLIGHT_NUMBER";

        /**
         * Contains the "special instructions" (TEXT) column name.
         */
        public static final String SPECIAL_INSTRUCTIONS = "SPECIAL_INSTRUCTIONS";

        /**
         * Contains the "start airport city" (TEXT) column name.
         */
        public static final String START_AIRPORT_CITY = "START_AIRPORT_CITY";

        /**
         * Contains the "start airport country" (TEXT) column name.
         */
        public static final String START_AIRPORT_COUNTRY = "START_AIRPORT_COUNTRY";

        /**
         * Contains the "start airport country code" (TEXT) column name.
         */
        public static final String START_AIRPORT_COUNTRY_CODE = "START_AIRPORT_COUNTRY_CODE";

        /**
         * Contains the "start airport name" (TEXT) column name.
         */
        public static final String START_AIRPORT_NAME = "START_AIRPORT_NAME";

        /**
         * Contains the "start airport state" (TEXT) column name.
         */
        public static final String START_AIRPORT_STATE = "START_AIRPORT_STATE";

        /**
         * Contains the "start gate" (TEXT) column name.
         */
        public static final String START_GATE = "START_GATE";

        /**
         * Contains the "start terminal" (TEXT) column name.
         */
        public static final String START_TERMINAL = "START_TERMINAL";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Flight Status information.
     */
    public static final class FlightStatusColumns implements BaseColumns {

        // Prevent instantiation.
        private FlightStatusColumns() {
        }

        /**
         * Contains the Flight Status table name.
         */
        public static final String TABLE_NAME = "FLIGHT_STATUS";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Flight Statuses URI
         */
        private static final String PATH_FLIGHT_STATUSES = "/flight_statuses";

        /**
         * Path part for the Flight Status ID URI
         */
        private static final String PATH_FLIGHT_STATUS_ID = "/flight_statuses/";

        /**
         * 0-relative position of a Flight Status ID segment in the path part of a Flight Status ID URI
         */
        public static final int FLIGHT_STATUS_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_FLIGHT_STATUSES);

        /**
         * The content URI base for a Flight Status. Callers must append a numeric Flight Status id to this Uri to retrieve a
         * Flight Status.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_FLIGHT_STATUS_ID);

        /**
         * The content URI match pattern for a single Flight Status, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_FLIGHT_STATUS_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Flight Statuses.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.flight_statuses";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Flight Status.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.flight_status";

        // Column definitions

        /**
         * Contains the "air segment ID" (INTEGER REFERENCES AIR_SEGMENT._ID) column name.
         */
        public static final String AIR_SEGMENT_ID = "AIR_SEGMENT_ID";

        /**
         * Contains the "equipment scheduled" (TEXT) column name.
         */
        public static final String EQUIPMENT_SCHEDULED = "EQUIPMENT_SCHEDULED";

        /**
         * Contains the "equipment actual" (TEXT) column name.
         */
        public static final String EQUIPMENT_ACTUAL = "EQUIPMENT_ACTUAL";

        /**
         * Contains the "equipment registration" (TEXT) column name.
         */
        public static final String EQUIPMENT_REGISTRATION = "EQUIPMENT_REGISTRATION";

        /**
         * Contains the "departure terminal scheduled" (TEXT) column name.
         */
        public static final String DEPARTURE_TERMINAL_SCHEDULED = "DEPARTURE_TERMINAL_SCHEDULED";

        /**
         * Contains the "departure terminal actual" (TEXT) column name.
         */
        public static final String DEPARTURE_TERMINAL_ACTUAL = "DEPARTURE_TERMINAL_ACTUAL";

        /**
         * Contains the "departure gate" (TEXT) column name.
         */
        public static final String DEPARTURE_GATE = "DEPARTURE_GATE";

        /**
         * Contains the "departure scheduled" (TEXT) column name.
         */
        public static final String DEPARTURE_SCHEDULED = "DEPARTURE_SCHEDULED";

        /**
         * Contains the "departure estimated" (TEXT) column name.
         */
        public static final String DEPARTURE_ESTIMATED = "DEPARTURE_ESTIMATED";

        /**
         * Contains the "departure actual" (TEXT) column name.
         */
        public static final String DEPARTURE_ACTUAL = "DEPARTURE_ACTUAL";

        /**
         * Contains the "departure status reason" (TEXT) column name.
         */
        public static final String DEPARTURE_STATUS_REASON = "DEPARTURE_STATUS_REASON";

        /**
         * Contains the "departure short status" (TEXT) column name.
         */
        public static final String DEPARTURE_SHORT_STATUS = "DEPARTURE_SHORT_STATUS";

        /**
         * Contains the "departure long status" (TEXT) column name.
         */
        public static final String DEPARTURE_LONG_STATUS = "DEPARTURE_LONG_STATUS";

        /**
         * Contains the "arrival terminal scheduled" (TEXT) column name.
         */
        public static final String ARRIVAL_TERMINAL_SCHEDULED = "ARRIVAL_TERMINAL_SCHEDULED";

        /**
         * Contains the "arrival terminal actual" (TEXT) column name.
         */
        public static final String ARRIVAL_TERMINAL_ACTUAL = "ARRIVAL_TERMINAL_ACTUAL";

        /**
         * Contains the "arrival gate" (TEXT) column name.
         */
        public static final String ARRIVAL_GATE = "ARRIVAL_GATE";

        /**
         * Contains the "arrival estimated" (TEXT) column name.
         */
        public static final String ARRIVAL_SCHEDULED = "ARRIVAL_SCHEDULED";

        /**
         * Contains the "arrival estimated" (TEXT) column name.
         */
        public static final String ARRIVAL_ESTIMATED = "ARRIVAL_ESTIMATED";

        /**
         * Contains the "arrival actual" (TEXT) column name.
         */
        public static final String ARRIVAL_ACTUAL = "ARRIVAL_ACTUAL";

        /**
         * Contains the "baggage claim" (TEXTS) column name.
         */
        public static final String BAGGAGE_CLAIM = "BAGGAGE_CLAIM";

        /**
         * Contains the "diversion city" (TEXT) column name.
         */
        public static final String DIVERSION_CITY = "DIVERSION_CITY";

        /**
         * Contains the "diversion airport" (TEXT) column name.
         */
        public static final String DIVERSION_AIRPORT = "DIVERSION_AIRPORT";

        /**
         * Contains the "arrival status reason" (TEXT) column name.
         */
        public static final String ARRIVAL_STATUS_REASON = "ARRIVAL_STATUS_REASON";

        /**
         * Contains the "arrival short status" (TEXT) column name.
         */
        public static final String ARRIVAL_SHORT_STATUS = "ARRIVAL_SHORT_STATUS";

        /**
         * Contains the "arrival long status" (TEXT) column name.
         */
        public static final String ARRIVAL_LONG_STATUS = "ARRIVAL_LONG_STATUS";

        /**
         * Contains the "cliqbook message" (TEXT) column name.
         */
        public static final String CLIQBOOK_MESSAGE = "CLIQBOOK_MESSAGE";

        /**
         * Contains the "last updated UTC" (TEXT) column name. The text is stored in "YYYY-MM-DDTHH:MM:SS" format.
         */
        public static final String LAST_UPDATED_UTC = "LAST_UPDATED_UTC";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Seat information.
     */
    public static final class SeatColumns implements BaseColumns {

        // Prevent instantiation.
        private SeatColumns() {
        }

        /**
         * Contains the Seat table name.
         */
        public static final String TABLE_NAME = "SEAT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Seats URI
         */
        private static final String PATH_SEATS = "/seats";

        /**
         * Path part for the Seat ID URI
         */
        private static final String PATH_SEAT_ID = "/seats/";

        /**
         * 0-relative position of a Seat ID segment in the path part of a Seat ID URI
         */
        public static final int SEAT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SEATS);

        /**
         * The content URI base for a Seat. Callers must append a numeric Seat id to this Uri to retrieve a Seat.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SEAT_ID);

        /**
         * The content URI match pattern for a single Seat, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SEAT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Seats.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.seats";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Seat.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.seat";

        // Column definitions

        /**
         * Contains the "air segment ID" (INTEGER REFERENCES AIR_SEGMENT._ID) column name.
         */
        public static final String AIR_SEGMENT_ID = "AIR_SEGMENT_ID";

        /**
         * Contains the "passenger RPH" (TEXT) column name.
         */
        public static final String PASSENGER_RPH = "PASSENGER_RPH";

        /**
         * Contains the "seat number" (TEXT) column name.
         */
        public static final String SEAT_NUMBER = "SEAT_NUMBER";

        /**
         * Contains the "status code" (TEXT) column name.
         */
        public static final String STATUS_CODE = "STATUS_CODE";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Hotel Segment information.
     */
    public static final class HotelSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelSegmentColumns() {
        }

        /**
         * Contains the Hotel Segment table name.
         */
        public static final String TABLE_NAME = "HOTEL_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Segments URI
         */
        private static final String PATH_HOTEL_SEGMENTS = "/hotel_segments";

        /**
         * Path part for the Hotel Segment ID URI
         */
        private static final String PATH_HOTEL_SEGMENT_ID = "/hotel_segments/";

        /**
         * 0-relative position of a Hotel Segment ID segment in the path part of a Hotel Segment ID URI
         */
        public static final int HOTEL_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_SEGMENTS);

        /**
         * The content URI base for a Hotel Segment. Callers must append a numeric Hotel Segment id to this Uri to retrieve a
         * Hotel Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Hotel Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "check-in time" (TEXT) column name.
         */
        public static final String CHECK_IN_TIME = "CHECK_IN_TIME";

        /**
         * Contains the "check-out time" (TEXT) column name.
         */
        public static final String CHECK_OUT_TIME = "CHECK_OUT_TIME";

        /**
         * Contains the "discount code" (TEXT) column name.
         */
        public static final String DISCOUNT_CODE = "DISCOUNT_CODE";

        /**
         * Contains the "num rooms" (INTEGER) column name.
         */
        public static final String NUM_ROOMS = "NUM_ROOMS";

        /**
         * Contains the "rate code" (TEXT) column name.
         */
        public static final String RATE_CODE = "RATE_CODE";

        /**
         * Contains the "room type" (TEXT) column name.
         */
        public static final String ROOM_TYPE = "ROOM_TYPE";

        /**
         * Contains the "room type localized" (TEXT) column name.
         */
        public static final String ROOM_TYPE_LOCALIZED = "ROOM_TYPE_LOCALIZED";

        /**
         * Contains the "daily rate" (REAL) column name.
         */
        public static final String DAILY_RATE = "DAILY_RATE";

        /**
         * Contains the "total rate" (REAL) column name.
         */
        public static final String TOTAL_RATE = "TOTAL_RATE";

        /**
         * Contains the "cancellation policy" (TEXT) column name.
         */
        public static final String CANCELLATION_POLICY = "CANCELLATION_POLICY";

        /**
         * Contains the "special instructions" (TEXT) column name.
         */
        public static final String SPECIAL_INSTRUCTIONS = "SPECIAL_INSTRUCTIONS";

        /**
         * Contains the "room description" (TEXT) column name.
         */
        public static final String ROOM_DESCRIPTION = "ROOM_DESCRIPTION";

        /**
         * Contains the "rate type" (TEXT) column name.
         */
        public static final String RATE_TYPE = "RATE_TYPE";

        /**
         * Contains the "property ID" (TEXT) column name.
         */
        public static final String PROPERTY_ID = "PROPERTY_ID";

        /**
         * Contains the "property image count" (INTEGER) column name.
         */
        public static final String PROPERTY_IMAGE_COUNT = "PROPERTY_IMAGE_COUNT";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Car Segment information.
     */
    public static final class CarSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private CarSegmentColumns() {
        }

        /**
         * Contains the Car Segment table name.
         */
        public static final String TABLE_NAME = "CAR_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Car Segments URI
         */
        private static final String PATH_CAR_SEGMENTS = "/car_segments";

        /**
         * Path part for the Car Segment ID URI
         */
        private static final String PATH_CAR_SEGMENT_ID = "/car_segments/";

        /**
         * 0-relative position of a Car Segment ID segment in the path part of a Car Segment ID URI
         */
        public static final int CAR_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_SEGMENTS);

        /**
         * The content URI base for a Car Segment. Callers must append a numeric Car Segment id to this Uri to retrieve a Car
         * Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Car Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CAR_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Car Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.car_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Car Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.car_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "air condition" (TEXT) column name.
         */
        public static final String AIR_CONDITION = "AIR_CONDITION";

        /**
         * Contains the "air condition localized" (TEXT) column name.
         */
        public static final String AIR_CONDITION_LOCALIZED = "AIR_CONDITION_LOCALIZED";

        /**
         * Contains the "body" (TEXT) column name.
         */
        public static final String BODY = "BODY";

        /**
         * Contains the "body localized" (TEXT) column name.
         */
        public static final String BODY_LOCALIZED = "BODY_LOCALIZED";

        /**
         * Contains the "class of car" (TEXT) column name.
         */
        public static final String CLASS_OF_CAR = "CLASS_OF_CAR";

        /**
         * Contains the "class of car localized" (TEXT) column name.
         */
        public static final String CLASS_OF_CAR_LOCALIZED = "CLASS_OF_CAR_LOCALIZED";

        /**
         * Contains the "daily rate" (REAL) column name.
         */
        public static final String DAILY_RATE = "DAILY_RATE";

        /**
         * Contains the "discount code" (TEXT) column name.
         */
        public static final String DISCOUNT_CODE = "DISCOUNT_CODE";

        /**
         * Contains the "end airport city" (TEXT) column name.
         */
        public static final String END_AIRPORT_CITY = "END_AIRPORT_CITY";

        /**
         * Contains the "end airport country" (TEXT) column name.
         */
        public static final String END_AIRPORT_COUNTRY = "END_AIRPORT_COUNTRY";

        /**
         * Contains the "end airport country code" (TEXT) column name.
         */
        public static final String END_AIRPORT_COUNTRY_CODE = "END_AIRPORT_COUNTRY_CODE";

        /**
         * Contains the "end airport name" (TEXT) column name.
         */
        public static final String END_AIRPORT_NAME = "END_AIRPORT_NAME";

        /**
         * Contains the "end airport state" (TEXT) column name.
         */
        public static final String END_AIRPORT_STATE = "END_AIRPORT_STATE";

        /**
         * Contains the "end location" (TEXT) column name.
         */
        public static final String END_LOCATION = "END_LOCATION";

        /**
         * Contains the "image car URI" (TEXT) column name.
         */
        public static final String IMAGE_CAR_URI = "IMAGE_CAR_URI";

        /**
         * Contains the "num cars" (INTEGER) column name.
         */
        public static final String NUM_CARS = "NUM_CARS";

        /**
         * Contains the "rate type" (TEXT) column name.
         */
        public static final String RATE_TYPE = "RATE_TYPE";

        /**
         * Contains the "special equipment" (TEXT) column name.
         */
        public static final String SPECIAL_EQUIPMENT = "SPECIAL_EQUIPMENT";

        /**
         * Contains the "start airport city" (TEXT) column name.
         */
        public static final String START_AIRPORT_CITY = "START_AIRPORT_CITY";

        /**
         * Contains the "start airport country" (TEXT) column name.
         */
        public static final String START_AIRPORT_COUNTRY = "START_AIRPORT_COUNTRY";

        /**
         * Contains the "start airport country code" (TEXT) column name.
         */
        public static final String START_AIRPORT_COUNTRY_CODE = "START_AIRPORT_COUNTRY_CODE";

        /**
         * Contains the "start airport name" (TEXT) column name.
         */
        public static final String START_AIRPORT_NAME = "START_AIRPORT_NAME";

        /**
         * Contains the "start airport state" (TEXT) column name.
         */
        public static final String START_AIRPORT_STATE = "START_AIRPORT_STATE";

        /**
         * Contains the "start location" (TEXT) column name.
         */
        public static final String START_LOCATION = "START_LOCATION";

        /**
         * Contains the "transmission" (TEXT) column name.
         */
        public static final String TRANSMISSION = "TRANSMISSION";

        /**
         * Contains the "transmission localized" (TEXT) column name.
         */
        public static final String TRANSMISSION_LOCALIZED = "TRANSMISSION_LOCALIZED";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Rail Segment information.
     */
    public static final class RailSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private RailSegmentColumns() {
        }

        /**
         * Contains the Rail Segment table name.
         */
        public static final String TABLE_NAME = "RAIL_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Rail Segments URI
         */
        private static final String PATH_RAIL_SEGMENTS = "/rail_segments";

        /**
         * Path part for the Rail Segment ID URI
         */
        private static final String PATH_RAIL_SEGMENT_ID = "/rail_segments/";

        /**
         * 0-relative position of a Rail Segment ID segment in the path part of a Rail Segment ID URI
         */
        public static final int RAIL_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RAIL_SEGMENTS);

        /**
         * The content URI base for a Rail Segment. Callers must append a numeric Rail Segment id to this Uri to retrieve a Rail
         * Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RAIL_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Rail Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RAIL_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Rail Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.rail_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Rail Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.rail_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "amenities" (TEXT) column name.
         */
        public static final String AMENITIES = "AMENITIES";

        /**
         * Contains the "cabin" (TEXT) column name.
         */
        public static final String CABIN = "CABIN";

        /**
         * Contains the "class of service" (TEXT) column name.
         */
        public static final String CLASS_OF_SERVICE = "CLASS_OF_SERVICE";

        /**
         * Contains the "discount code" (TEXT) column name.
         */
        public static final String DISCOUNT_CODE = "DISCOUNT_CODE";

        /**
         * Contains the "duration" (INTEGER) column name.
         */
        public static final String DURATION = "DURATION";

        /**
         * Contains the "end platform" (TEXT) column name.
         */
        public static final String END_PLATFORM = "END_PLATFORM";

        /**
         * Contains the "end rail station" (TEXT) column name.
         */
        public static final String END_RAIL_STATION = "END_RAIL_STATION";

        /**
         * Contains the "end rail station localized" (TEXT) column name.
         */
        public static final String END_RAIL_STATION_LOCALIZED = "END_RAIL_STATION_LOCALIZED";

        /**
         * Contains the "leg ID" (INTEGER) column name.
         */
        public static final String LEG_ID = "LEG_ID";

        /**
         * Contains the "meals" (TEXT) column name.
         */
        public static final String MEALS = "MEALS";

        /**
         * Contains the "miles" (INTEGER) column name.
         */
        public static final String MILES = "MILES";

        /**
         * Contains the "num stops" (INTEGER) column name.
         */
        public static final String NUM_STOPS = "NUM_STOPS";

        /**
         * Contains the "operated by train number" (TEXT) column name.
         */
        public static final String OPERATED_BY_TRAIN_NUMBER = "OPERATED_BY_TRAIN_NUMBER";

        /**
         * Contains the "pin" (TEXT) column name.
         */
        public static final String PIN = "PIN";

        /**
         * Contains the "start platform" (TEXT) column name.
         */
        public static final String START_PLATFORM = "START_PLATFORM";

        /**
         * Contains the "start rail station" (TEXT) column name.
         */
        public static final String START_RAIL_STATION = "START_RAIL_STATION";

        /**
         * Contains the "start rail station localized" (TEXT) column name.
         */
        public static final String START_RAIL_STATION_LOCALIZED = "START_RAIL_STATION_LOCALIZED";

        /**
         * Contains the "train number" (TEXT) column name.
         */
        public static final String TRAIN_NUMBER = "TRAIN_NUMBER";

        /**
         * Contains the "train type code" (TEXT) column name.
         */
        public static final String TRAIN_TYPE_CODE = "TRAIN_TYPE_CODE";

        /**
         * Contains the "wagon number" (TEXT) column name.
         */
        public static final String WAGON_NUMBER = "WAGON_NUMBER";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Dining Segment information.
     */
    public static final class DiningSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private DiningSegmentColumns() {
        }

        /**
         * Contains the Dining Segment table name.
         */
        public static final String TABLE_NAME = "DINING_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Dining Segment URI
         */
        private static final String PATH_DINING_SEGMENTS = "/dining_segments";

        /**
         * Path part for the Dining Segment ID URI
         */
        private static final String PATH_DINING_SEGMENT_ID = "/dining_segments/";

        /**
         * 0-relative position of a Dining Segment ID segment in the path part of a Dining Segment ID URI
         */
        public static final int DINING_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_DINING_SEGMENTS);

        /**
         * The content URI base for a Dining Segment. Callers must append a numeric Dining Segment id to this Uri to retrieve a
         * Dining Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_DINING_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Dining Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_DINING_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Dining Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.dining_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Dining Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.dining_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "reservation ID" (TEXT) column name.
         */
        public static final String RESERVATION_ID = "RESERVATION_ID";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Event Segment information.
     */
    public static final class EventSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private EventSegmentColumns() {
        }

        /**
         * Contains the Event Segment table name.
         */
        public static final String TABLE_NAME = "EVENT_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Event Segments URI
         */
        private static final String PATH_EVENT_SEGMENTS = "/event_segments";

        /**
         * Path part for the Event Segment ID URI
         */
        private static final String PATH_EVENT_SEGMENT_ID = "/event_segments/";

        /**
         * 0-relative position of an Event Segment ID segment in the path part of an Event Segment ID URI
         */
        public static final int EVENT_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_EVENT_SEGMENTS);

        /**
         * The content URI base for an Event Segment. Callers must append a numeric Event Segment id to this Uri to retrieve an
         * Event Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_EVENT_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Event Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_EVENT_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Event Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.event_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Event Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.event_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Parking Segment information.
     */
    public static final class ParkingSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private ParkingSegmentColumns() {
        }

        /**
         * Contains the Parking Segment table name.
         */
        public static final String TABLE_NAME = "PARKING_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Parking Segments URI
         */
        private static final String PATH_PARKING_SEGMENTS = "/parking_segments";

        /**
         * Path part for the Parking Segment ID URI
         */
        private static final String PATH_PARKING_SEGMENT_ID = "/parking_segments/";

        /**
         * 0-relative position of a Parking Segment ID segment in the path part of a Parking Segment ID URI
         */
        public static final int PARKING_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PARKING_SEGMENTS);

        /**
         * The content URI base for a Parking Segment. Callers must append a numeric Parking Segment id to this Uri to retrieve a
         * Parking Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PARKING_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Parking Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PARKING_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Parking Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.parking_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Parking Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.parking_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "parking location ID" (TEXT) column name.
         */
        public static final String PARKING_LOCATION_ID = "PARKING_LOCATION_ID";

        /**
         * Contains the "pin" (TEXT) column name.
         */
        public static final String PIN = "PIN";

        /**
         * Contains the "start location" (TEXT) column name.
         */
        public static final String START_LOCATION = "START_LOCATION";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Ride Segment information.
     */
    public static final class RideSegmentColumns implements BaseColumns {

        // Prevent instantiation.
        private RideSegmentColumns() {
        }

        /**
         * Contains the Ride Segment table name.
         */
        public static final String TABLE_NAME = "RIDE_SEGMENT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Ride Segments URI
         */
        private static final String PATH_RIDE_SEGMENTS = "/ride_segments";

        /**
         * Path part for the Ride Segment ID URI
         */
        private static final String PATH_RIDE_SEGMENT_ID = "/ride_segments/";

        /**
         * 0-relative position of a Ride Segment ID segment in the path part of a Ride Segment ID URI
         */
        public static final int RIDE_SEGMENT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RIDE_SEGMENTS);

        /**
         * The content URI base for a Ride Segment. Callers must append a numeric Ride Segment id to this Uri to retrieve a Ride
         * Segment.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RIDE_SEGMENT_ID);

        /**
         * The content URI match pattern for a single Ride Segment, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RIDE_SEGMENT_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Ride Segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.ride_segments";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Ride Segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.ride_segment";

        // Column definitions

        /**
         * Contains the "booking ID" (INTEGER REFERENCES BOOKING._ID) column name.
         */
        public static final String BOOKING_ID = "BOOKING_ID";

        /**
         * Contains the "segment ID" (INTEGER REFERENCES SEGMENT._ID) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the "cancellation policy" (TEXT) column name.
         */
        public static final String CANCELLATION_POLICY = "CANCELLATION_POLICY";

        /**
         * Contains the "drop-off instructions" (TEXT) column name.
         */
        public static final String DROP_OFF_INSTRUCTIONS = "DROP_OFF_INSTRUCTIONS";

        /**
         * Contains the "duration" (INTEGER) column name.
         */
        public static final String DURATION = "DURATION";

        /**
         * Contains the "meeting instructions" (TEXT) column name.
         */
        public static final String MEETING_INSTRUCTIONS = "MEETING_INSTRUCTIONS";

        /**
         * Contains the "miles" (INTEGER) column name.
         */
        public static final String MILES = "MILES";

        /**
         * Contains the "number of hours" (REAL) column name.
         */
        public static final String NUMBER_OF_HOURS = "NUMBER_OF_HOURS";

        /**
         * Contains the "pick-up instructions" (TEXT) column name.
         */
        public static final String PICK_UP_INSTRUCTIONS = "PICK_UP_INSTRUCTIONS";

        /**
         * Contains the "rate" (REAL) column name.
         */
        public static final String RATE = "RATE";

        /**
         * Contains the "rate description" (TEXT) column name.
         */
        public static final String RATE_DESCRIPTION = "RATE_DESCRIPTION";

        /**
         * Contains the "rate type" (TEXT) column name.
         */
        public static final String RATE_TYPE = "RATE_TYPE";

        /**
         * Contains the "start location" (TEXT) column name.
         */
        public static final String START_LOCATION = "START_LOCATION";

        /**
         * Contains the "start location code" (TEXT) column name.
         */
        public static final String START_LOCATION_CODE = "START_LOCATION_CODE";

        /**
         * Contains the "start location name" (TEXT) column name.
         */
        public static final String START_LOCATION_NAME = "START_LOCATION_NAME";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Location Choice information.
     */
    public static final class LocationChoiceColumns implements BaseColumns {

        // Prevent instantiation.
        private LocationChoiceColumns() {
        }

        /**
         * Contains the Location Choice table name.
         */
        public static final String TABLE_NAME = "LOCATION_CHOICE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Location Choice URI
         */
        private static final String PATH_LOCATION_CHOICES = "/location_choices";

        /**
         * Path part for the Location Choice ID URI
         */
        private static final String PATH_LOCATION_CHOICE_ID = "/location_choices/";

        /**
         * 0-relative position of a Location Choice ID segment in the path part of a Location Choice ID URI
         */
        public static final int LOCATION_CHOICE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_LOCATION_CHOICES);

        /**
         * The content URI base for a Location Choice. Callers must append a numeric Location Choice id to this Uri to retrieve a
         * Location Choice.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_LOCATION_CHOICE_ID);

        /**
         * The content URI match pattern for a single Location Choice, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_LOCATION_CHOICE_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Location Choices.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.location_choices";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Location Choice.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.location_choice";

        // Column definitions

        /**
         * Contains the city (TEXT) column name.
         */
        public static final String CITY = "CITY";

        /**
         * Contains the country (TEXT) column name.
         */
        public static final String COUNTRY = "COUNTRY";

        /**
         * Contains the "country abbreviation" (TEXT) column name.
         */
        public static final String COUNTRY_ABBREVIATION = "COUNTRY_ABBREVIATION";

        /**
         * Contain the iata (TEXT) column name.
         */
        public static final String IATA = "IATA";

        /**
         * Contains the location (TEXT) column name.
         */
        public static final String LOCATION = "LOCATION";

        /**
         * Contains the state (TEXT) column name.
         */
        public static final String STATE = "STATE";

        /**
         * Contains the latitude (REAL) column name.
         */
        public static final String LAT = "LAT";

        /**
         * Contains the longitude (REAL) column name.
         */
        public static final String LON = "LON";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Hotel Detail information. Has a foreign key reference to Hotel Search Result table
     */
    public static final class HotelDetailColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelDetailColumns() {
        }

        /**
         * Contains the Hotel Detail table name.
         */
        public static final String TABLE_NAME = "HOTEL_DETAIL";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Detail URI
         */
        private static final String PATH_HOTEL_DETAILS = "/hotel_details";

        /**
         * Path part for the Hotel Detail ID URI
         */
        private static final String PATH_HOTEL_DETAIL_ID = "/hotel_details/";

        /**
         * 0-relative position of a Hotel Detail ID segment in the path part of a Hotel Detail ID URI
         */
        public static final int HOTEL_DETAIL_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_DETAILS);

        /**
         * The content URI base for a Hotel Detail. Callers must append a numeric Hotel Detail id to this Uri to retrieve a Hotel
         * Detail.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_DETAIL_ID);

        /**
         * The content URI match pattern for a single Hotel Detail, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_DETAIL_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Details.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_details";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Detail.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_detail";

        // Column definitions

        /**
         * Contains the Hotel Search Result ID (INTEGER REFERENCES HOTEL_SEARCH_RESULT._ID) column name.
         */
        public static final String HOTEL_SEARCH_RESULT_ID = "HOTEL_SEARCH_RESULT_ID";

        /**
         * Contains the hotel name (TEXT) column name.
         */
        public static final String NAME = "NAME";

        /**
         * Contains the address line 1 (TEXT) column name.
         */
        public static final String ADDRESS_LINE_1 = "ADDRESS_LINE_1";

        /**
         * Contains the street (TEXT) column name.
         */
        public static final String STREET = "STREET";

        /**
         * Contains the city (TEXT) column name.
         */
        public static final String CITY = "CITY";

        /**
         * Contains the state (TEXT) column name.
         */
        public static final String STATE = "STATE";

        /**
         * Contains the country (TEXT) column name.
         */
        public static final String COUNTRY = "COUNTRY";

        /**
         * Contains the country code (TEXT) column name.
         */
        public static final String COUNTRY_CODE = "COUNTRY_CODE";

        /**
         * Contains the phone (TEXT) column name.
         */
        public static final String PHONE = "PHONE";

        /**
         * Contains the toll free phone (TEXT) column name.
         */
        public static final String TOLL_FREE_PHONE = "TOLL_FREE_PHONE";

        /**
         * Contains the zip (TEXT) column name.
         */
        public static final String ZIP = "ZIP";

        /**
         * Contain the distance (REAL) column name.
         */
        public static final String DISTANCE = "DISTANCE";

        /**
         * Contain the distance unit (TEXT) column name.
         */
        public static final String DISTANCE_UNIT = "DISTANCE_UNIT";

        /**
         * Contain the currency code (TEXT) column name.
         */
        public static final String CURRENCY_CODE = "CURRENCY_CODE";

        /**
         * Contain the price to beat (REAL) column name.
         */
        public static final String PRICE_TO_BEAT = "PRICE_TO_BEAT";

        /**
         * Contain the lowest rate (REAL) column name.
         */
        public static final String LOWEST_RATE = "LOWEST_RATE";

        /**
         * Contain the travel points for the lowest rate (INTEGER) column name.
         */
        public static final String TRAVEL_POINTS_FOR_LOWEST_RATE = "TRAVEL_POINTS_FOR_LOWEST_RATE";

        /**
         * Contain the lowest enforcement level (INTEGER) column name.
         */
        public static final String LOWEST_ENF_LEVEL = "LOWEST_ENF_LEVEL";

        /**
         * Contain the chain code (TEXT) column name.
         */
        public static final String CHAIN_CODE = "CHAIN_CODE";

        /**
         * Contain the chain name (TEXT) column name.
         */
        public static final String CHAIN_NAME = "CHAIN_NAME";

        /**
         * Contain the rates URL (TEXT) column name.
         */
        public static final String RATES_URL = "RATES_URL";

        /**
         * Contain the star rating (TEXT) column name.
         */
        public static final String STAR_RATING = "STAR_RATING";

        /**
         * Contain the company preference (TEXT) column name.
         */
        public static final String COMPANY_PREFERENCE = "COMPANY_PREFERENCE";

        /**
         * Contain the recommended (suggested) category (TEXT) column name.
         */
        public static final String SUGESTED_CATEGORY = "SUGESTED_CATEGORY";

        /**
         * Contain the recommended (suggested) score (REAL) column name.
         */
        public static final String SUGESTED_SCORE = "SUGESTED_SCORE";

        /**
         * Contain the availability error code (TEXT) column name.
         */
        public static final String AVAILABILITY_ERROR_CODE = "AVAILABILITY_ERROR_CODE";

        /**
         * Contain the thumbnail image URL (TEXT) column name.
         */
        public static final String THUMBNAIL_URL = "THUMBNAIL_URL";

        /**
         * Contains the latitude (REAL) column name.
         */
        public static final String LAT = "LAT";

        /**
         * Contains the longitude (REAL) column name.
         */
        public static final String LON = "LON";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

    }

    /**
     * Models Hotel Image Pair information i.e. URLs of thumnail and image
     */
    public static final class HotelImagePairColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelImagePairColumns() {
        }

        /**
         * Contains the Hotel Image Pair table name.
         */
        public static final String TABLE_NAME = "HOTEL_IMAGE_PAIR";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Image Pair URI
         */
        private static final String PATH_HOTEL_IMAGE_PAIRS = "/hotel_image_pairs";

        /**
         * Path part for the Hotel Image Pair ID URI
         */
        private static final String PATH_HOTEL_IMAGE_PAIR_ID = "/hotel_image_pairs/";

        /**
         * 0-relative position of a Hotel Image Pair ID segment in the path part of a Hotel Image Pair ID URI
         */
        public static final int HOTEL_IMAGE_PAIR_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_IMAGE_PAIRS);

        /**
         * The content URI base for a Hotel Image Pair. Callers must append a numeric Hotel Image Pair id to this Uri to retrieve
         * a Hotel Image Pair.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_IMAGE_PAIR_ID);

        /**
         * The content URI match pattern for a single Hotel Image Pair, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri
                .parse(SCHEME + AUTHORITY + PATH_HOTEL_IMAGE_PAIR_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Image Pairs.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_image_pairs";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Image Pair.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_image_pair";

        // Column definitions

        /**
         * Contains the Hotel Detail ID (INTEGER REFERENCES HOTEL_DETAIL._ID) column name.
         */
        public static final String HOTEL_DETAIL_ID = "HOTEL_DETAIL_ID";

        /**
         * Contains the image URL (TEXT) column name.
         */
        public static final String IMAGE_URL = "IMAGE_URL";

        /**
         * Contains the thumbnail URL (TEXT) column name.
         */
        public static final String THUMBNAIL_URL = "THUMBNAIL_URL";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Hotel Rate Detail information
     */
    public static final class HotelRateDetailColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelRateDetailColumns() {
        }

        /**
         * Contains the Hotel Rate Detail table name.
         */
        public static final String TABLE_NAME = "HOTEL_RATE_DETAIL";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Rate Detail URI
         */
        private static final String PATH_HOTEL_RATE_DETAILS = "/hotel_rate_details";

        /**
         * Path part for the Hotel Rate Detail ID URI
         */
        private static final String PATH_HOTEL_RATE_DETAIL_ID = "/hotel_rate_details/";

        /**
         * 0-relative position of a Hotel Rate Detail ID segment in the path part of a Hotel Image Pair ID URI
         */
        public static final int HOTEL_RATE_DETAIL_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_RATE_DETAILS);

        /**
         * The content URI base for a Hotel Rate Detail. Callers must append a numeric Hotel Rate Detail id to this Uri to
         * retrieve a Hotel Rate Detail.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_RATE_DETAIL_ID);

        /**
         * The content URI match pattern for a single Hotel Rate Detail, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_RATE_DETAIL_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Rate Details.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_rate_details";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Image Pair.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_rate_detail";

        // Column definitions

        /**
         * Contains the Hotel Detail ID (INTEGER REFERENCES HOTEL_DETAIL._ID) column name.
         */
        public static final String HOTEL_DETAIL_ID = "HOTEL_DETAIL_ID";

        /**
         * Contains the rate id (TEXT) column name.
         */
        public static final String RATE_ID = "RATE_ID";

        /**
         * Contains the amount (REAL) column name.
         */
        public static final String AMOUNT = "AMOUNT";

        /**
         * Contain the currency code (TEXT) column name.
         */
        public static final String CURRENCY_CODE = "CURRENCY_CODE";

        /**
         * Contains the source (TEXT) column name.
         */
        public static final String SOURCE = "SOURCE";

        /**
         * Contains the room type (TEXT) column name.
         */
        public static final String ROOM_TYPE = "ROOM_TYPE";

        /**
         * Contains the description (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the estimated bed type (TEXT) column name.
         */
        public static final String ESTIMATED_BED_TYPE = "ESTIMATED_BED_TYPE";

        /**
         * Contains the guarantee surcharge (TEXT) column name.
         */
        public static final String GUARANTEE_SURCHARGE = "GUARANTEE_SURCHARGE";

        /**
         * Contains the rate changes overstay (BOOLEAN) column name.
         */
        public static final String RATE_CHANGES_OVERSTAY = "RATE_CHANGES_OVERSTAY";

        /**
         * Contains the max enforcement level (INTEGER) column name.
         */
        public static final String MAX_ENF_LEVEL = "MAX_ENF_LEVEL";

        /**
         * Contains the sell options URL (TEXT) column name.
         */
        public static final String SELL_OPTIONS_URL = "SELL_OPTIONS_URL";

        /**
         * Contains the violation value ids (TEXT) column name - comma seperated values
         */
        public static final String VIOLATION_VALUE_IDS = "VIOLATION_VALUE_IDS";

        /**
         * Contains the max enforcement level (INTEGER) column name.
         */
        public static final String TRAVEL_POINTS = "TRAVEL_POINTS";

        /**
         * Contains the can redeem travel points against violation (BOOLEAN) column name.
         */
        public static final String CAN_REDEEM_TP_AGAINST_VIOLATIONS = "CAN_REDEEM_TP_AGAINST_VIOLATIONS";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";
    }

    /**
     * Models Hotel Search Result information
     */
    public static final class HotelSearchResultColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelSearchResultColumns() {
        }

        /**
         * Contains the Hotel Rate Detail table name.
         */
        public static final String TABLE_NAME = "HOTEL_SEARCH_RESULT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Search Result URI
         */
        private static final String PATH_HOTEL_SEARCH_RESULT = "/hotel_search_result";

        /**
         * Path part for the Hotel Search Result ID URI
         */
        private static final String PATH_HOTEL_SEARCH_RESULT_ID = "/hotel_search_result/";

        /**
         * 0-relative position of a Hotel Search Result ID segment in the path part of a Hotel Search Result ID URI
         */
        public static final int HOTEL_SEARCH_RESULT_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_SEARCH_RESULT);

        /**
         * The content URI base for a Hotel Search Result. Callers must append a numeric Hotel Search Result id to this Uri to
         * retrieve a Hotel Search Result.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_SEARCH_RESULT_ID);

        /**
         * The content URI match pattern for a single Hotel Search Result, specified by its ID. Use this to match incoming URIs or
         * to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_SEARCH_RESULT_ID
                + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Search Result.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_search_result";

        // Column definitions

        /**
         * Contains the currency (TEXT) column name.
         */
        public static final String CURRENCY = "CURRENCY";

        /**
         * Contains the distanceUnit (TEXT) column name.
         */
        public static final String DISTANCE_UNIT = "DISTANCE_UNIT";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        /**
         * Contains the search criteria (TEXT) column name.
         */
        public static final String SEARCH_CRITERIA_URL = "SEARCH_CRITERIA_URL";

        /**
         * Contains the insert time (DATETIME) column name.
         */
        public static final String INSERT_DATETIME = "INSERT_DATETIME";

        /**
         * Contains the expiry time (DATETIME) column name.
         */
        public static final String EXPIRY_DATETIME = "EXPIRY_DATETIME";
    }

    /**
     * Models Hotel Violation information. Has a foreign key reference to Hotel Search Result table
     */
    public static final class HotelViolationColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelViolationColumns() {
        }

        /**
         * Contains the Hotel Violation table name.
         */
        public static final String TABLE_NAME = "HOTEL_VIOLATION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Violation URI
         */
        private static final String PATH_HOTEL_VIOLATION = "/hotel_violations";

        /**
         * Path part for the Hotel Violation ID URI
         */
        private static final String PATH_HOTEL_VIOLATION_ID = "/hotel_violations/";

        /**
         * 0-relative position of a Hotel Violation ID segment in the path part of a Hotel Violation ID URI
         */
        public static final int HOTEL_VIOLATION_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_VIOLATION);

        /**
         * The content URI base for a Hotel Violation. Callers must append a numeric Hotel Violation id to this Uri to retrieve a
         * Hotel Violation
         * 
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_VIOLATION_ID);

        /**
         * The content URI match pattern for a single Hotel Violation, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_VIOLATION_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Violation.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_violations";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Violation.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_violation";

        // Column definitions

        /**
         * Contains the Hotel Search Result ID (INTEGER REFERENCES HOTEL_SEARCH_RESULT._ID) column name.
         */
        public static final String HOTEL_SEARCH_RESULT_ID = "HOTEL_SEARCH_RESULT_ID";

        /**
         * Contains the enforcementLevel (TEXT) column name.
         */
        public static final String ENFORCEMENT_LEVEL = "ENFORCEMENT_LEVEL";

        /**
         * Contains the message (TEXT) column name.
         */
        public static final String MESSAGE = "MESSAGE";

        /**
         * Contains the violationValueId (TEXT) column name.
         */
        public static final String VIOLATION_VALUE_ID = "VIOLATION_VALUE_ID";

    }

    /**
     * Models Hotel Benchmarks information. Has a foreign key reference to Hotel Search Result table
     */
    public static final class HotelBenchmarkColumns implements BaseColumns {

        // Prevent instantiation.
        private HotelBenchmarkColumns() {
        }

        /**
         * Contains the Hotel Benchmark table name.
         */
        public static final String TABLE_NAME = "HOTEL_BENCHMARK";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the Hotel Benchmark URI
         */
        private static final String PATH_HOTEL_BENCHMARK = "/hotel_benchmarks";

        /**
         * Path part for the Hotel Benchmark ID URI
         */
        private static final String PATH_HOTEL_BENCHMARK_ID = "/hotel_benchmarks/";

        /**
         * 0-relative position of a Hotel Violation ID segment in the path part of a Hotel Violation ID URI
         */
        public static final int HOTEL_BENCHMARK_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_BENCHMARK);

        /**
         * The content URI base for a Hotel Benchmark. Callers must append a numeric Hotel Benchmark id to this Uri to retrieve a
         * Hotel Benchmark
         *
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_BENCHMARK_ID);

        /**
         * The content URI match pattern for a single Hotel Benchmark, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_HOTEL_BENCHMARK_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of Hotel Benchmark.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.travel.hotel_benchmarks";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single Hotel Benchmark.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.travel.hotel_benchmark";

        // Column definitions

        /**
         * Contains the Hotel Search Result ID (INTEGER REFERENCES HOTEL_SEARCH_RESULT._ID) column name.
         */
        public static final String HOTEL_SEARCH_RESULT_ID = "HOTEL_SEARCH_RESULT_ID";

        /**
         * Contains the location name (TEXT) column name.
         */
        public static final String LOCATION_NAME = "LOCATION_NAME";

        /**
         * Contains the currency code (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * Contains the currency code (REAL) column name.
         */
        public static final String PRICE = "PRICE";

        /**
         * Contains the subDivCode (TEXT) column name.
         */
        public static final String SUB_DIV_CODE = "SUB_DIV_CODE";

    }

}
