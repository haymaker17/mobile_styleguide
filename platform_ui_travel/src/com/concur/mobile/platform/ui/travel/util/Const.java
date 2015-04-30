package com.concur.mobile.platform.ui.travel.util;

public class Const {

    // Instantiation is a crime.
    private Const() {
    }

    // -------------------------------------------------
    // General
    // -------------------------------------------------
    public static final String LOG_TAG = "CNQR.PLATFORM.UI.TRAVEL";

    // Travel related extra intent key values.
    public static final String EXTRA_TRAVEL_LATITUDE = "travel.latitude";
    public static final String EXTRA_TRAVEL_LONGITUDE = "travel.longitude";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN = "travel.hotel.search.check.in";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR = "travel.hotel.search.check.in.calendar";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT = "travel.hotel.search.check.out";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR = "travel.hotel.search.check.out.calendar";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION = "travel.hotel.search.location";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY = "travel.hotel.search.duration.of.stay";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS = "travel.hotel.search.duration.number.of.nights";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING = "travel.hotel.search.names.containing";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID = "travel.hotel.search.distance.unit";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_SHOW_GDS_NAME = "travel.hotel.search.show.gds.name";
    public static final String EXTRA_LOCATION_SEARCH_ALLOWED_MODES = "loc_search_modes_allowed";
    public static final String EXTRA_LOCATION_SEARCH_MODE_USED = "loc_search_mode_used";
    public static final String EXTRA_TRAVEL_LOCATION = "travel.location";
    public static final String EXTRA_TRAVEL_CLIQBOOK_TRIP_ID = "travel.cliqbook.trip.id";

    public static final String ACTION_HOTEL_SEARCH_RESULTS = "com.concur.mobile.action.HOTELS_FOUND";
    public static final String EXTRA_HOTELS_LIST = "hotel.list";
    public static final String EXTRA_HOTELS_DETAILS = "hotel.details";
    public static final String EXTRA_HOTEL_IMAGE_ITEM = "hotel.image";
    public static final String EXTRA_HOTEL_LOCATION = "hotel.location";
    public static final String EXTRA_HOTEL_IMAGES = "hotel.images";

    // Activity result codes
    public static final int REQUEST_CODE_LOCATION = 1;
    public static final int REQUEST_CODE_BOOK_HOTEL = 2;
    public static final int REQUEST_CODE_BACK_BUTTON_PRESSED = 3;

    public static final String EXTRA_IMAGE_URL = "image.url";
    public static final String EXTRA_IMAGE_TITLE = "image.title";
    public static final String EXTRA_TRAVEL_ITINERARY_LOCATOR = "travel.itinerary.locator";
    public static final String EXTRA_TRAVEL_RECORD_LOCATOR = "travel.record.locator";

    public static final String PREF_INSTALL_ID = "pref_install";
    /**
     * Contains the language to use when performing voice searches.
     */
    public static final String PREF_VOICE_SEARCH_LANGUAGE = "pref_voice_search_language";

    // Sticky broadcast intent action indicating service has been bound.
    // This is used for when the application is re-entered after the system has
    // shut it down.
    public static final String ACTION_CONCUR_SERVICE_BOUND = "com.concur.mobile.action.service.bound";
    public static final String ACTION_CONCUR_SERVICE_UNBOUND = "com.concur.mobile.action.service.unbound";

    /**
     * Sticky broadcast intent action indicating the (server) system is unavailable for some reason.
     */
    public static final String ACTION_NETWORK_SYSTEM_UNAVAILABLE = "com.concur.mobile.action.network.system.unavailable";

    // offline
    public static final String EXTRA_ENABLE_OFFLINE_MODE_NOTIFICATION = "enable_offline_mode_notification";
    public static final String EXTRA_QUEUE_ITEMS_AVAIL_NOTIFICATION = "offline_queue_items_available";
    public static final String ACTION_QUEUE_ITEMS_AVAILABLE = "com.concur.mobile.action.data.offline.items.available";
    public static final String ACTION_QUEUE_ITEMS_UNAVAILABLE = "com.concur.mobile.action.data.offline.items.unavailable";

    // Broadcast action/extra related to on-going network activity.
    public static final String ACTION_NETWORK_ACTIVITY_START = "com.concur.mobile.action.network.activity.start";
    public static final String ACTION_NETWORK_ACTIVITY_STOP = "com.concur.mobile.action.network.activity.stop";
    public static final String ACTION_NETWORK_ACTIVITY_TYPE = "com.concur.mobile.action.network.activity.type";
    public static final String ACTION_NETWORK_ACTIVITY_TEXT = "com.concur.mobile.action.network.activity.text";

    // Extra Intent Values Affecting List Search
    public static final String EXTRA_LIST_SEARCH_IS_MRU = "list.search.is.mru";
    public static final String EXTRA_LIST_SEARCH_FIELD_ID = "list.search.field.id";
    public static final String EXTRA_LIST_SEARCH_FT_CODE = "list.search.ft.code";
    public static final String EXTRA_LIST_SEARCH_LIST_KEY = "list.search.list.key";
    public static final String EXTRA_LIST_SEARCH_PARENT_LI_KEY = "list.search.parent.li.key";
    public static final String EXTRA_LIST_SEARCH_REPORT_KEY = "list.search.report.key";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_KEY = "list.search.selected.list.item.key";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_CODE = "list.search.selected.list.item.code";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_TEXT = "list.search.selected.list.item.text";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_CRN_CODE = "list.search.selected.list.item.crn.code";
    public static final String EXTRA_LIST_SELECTED_LIST_ITEM_CRN_KEY = "list.search.selected.list.item.crn.key";
    public static final String EXTRA_LIST_SEARCH_TITLE = "list.search.title";
    public static final String EXTRA_LIST_SEARCH_EXCLUDE_KEYS = "list.search.exclude.keys";
    public static final String EXTRA_LIST_SHOW_CODES = "list.search.show.codes";
    public static final String EXTRA_LIST_SEARCH_STATIC_LIST = "list.search.static.list";
    public static final String EXTRA_SEARCH_SELECTED_ITEM = "search.selected.item";
    public static final String EXTRA_COMBO_BOX_ACTION = "combo_box_action";
    public static final String EXTRA_COMBO_BOX_INLINE_TEXT = "combo_box_inline_text";
    public static final String ACTION_SEARCH_LIST_UPDATED = "com.concur.mobile.action.SEARCH_LIST_UPDATED";
    public static final int COMBO_BOX_INLINE_TEXT = 0;
    public static final int COMBO_BOX_LIST_SELECTION = 1;
    public static final int MSG_SEARCH_LIST_REQUEST = 45;

    public static final String ENFORCEMENT_REQUIRED_APPROVAL = "RequiresApproval";
    public static final String ENFORCEMENT_REQUIRED_PASSIVE_APPROVAL = "RequiresPassiveApproval";
    public static final String ENFORCEMENT_NOTIFY_MANAGER = "NotifyManager";
    public static final String ENFORCEMENT_LOG_FOR_REPORTS = "LogForReportsOnly";
    public static final String ENFORCEMENT_MESSAGE_ONLY = "Message Only";
    public static final String ENFORCEMENT_AUTO_FAIL = "AutoFail";
    public static final String ENFORCEMENT_AUTO_SUCCEED = "AutoSucceed";

}
