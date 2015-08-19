package com.concur.mobile.core.util;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap.CompressFormat;

public final class Const {

    static {
        // Should be in the form of:
        // User-Agent: Breeze/1.0 (Motorola; Droid; Android 2.0.1; Verizon)
        // so:
        // User-Agent: <product>/<version> (<device-manufacturer>;
        // <device-model>; <os-and-version>; <carrier>)
        // NOTE: This value gets a default here, but is set in
        // 'ConcurMobile.initUserAgentValue'.
        HTTP_HEADER_USER_AGENT_VALUE = "Android";
    }

    private Const() {
        // And never shall this class be constructed...
    }

    // Contains the image source pixel sample size used to reduce the size of a
    // receipt.
    public static final int RECEIPT_SOURCE_BITMAP_SAMPLE_SIZE = 2;
    // Contains the receipt output compression quality (value from 1 to 100).
    public static final int RECEIPT_COMPRESS_BITMAP_QUALITY = 90;
    // Contains the receipt output compression format.
    public static final CompressFormat RECEIPT_COMPRESS_BITMAP_FORMAT = CompressFormat.JPEG;
    // Global log identifier tag
    public static final String LOG_TAG = "CNQR";

    // HTTP header based constants.
    public static final String HTTP_HEADER_XSESSION_ID = "X-SessionID";
    public static final String HTTP_HEADER_USER_AGENT = "User-Agent";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_MESSAGE_ID = "X-MsgID";
    public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_HEADER_CHARACTER_ENCODING = "Character-Encoding";
    public static final String HTTP_HEADER_EXPECTED_LENGTH = "X-ExpectedContentLength";
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

    public static final String HTTP_BODY_CHARACTER_ENCODING = "UTF-8";
    public static final String YOUTUBE_SETUP_VIDEO_URL = "http://youtu.be/x0aQe7H6-Vg";
    // This is not 'final' as it is set from 'ConcurMobile.initUserAgentValue'.
    public static String HTTP_HEADER_USER_AGENT_VALUE;

    public static final String CONCUR_MOBILE_SUPPORT_EMAIL_ADDRESS = "androidmobilefeedback@concur.com";

    // A non-localized placeholder string
    public static final String NA = "--";

    // Server communication component IDs
    public static final int COM_COMPONENT_1 = 1;
    public static final int COM_COMPONENT_2 = 2;
    public static final int COM_COMPONENT_3 = 3;
    public static final int COM_COMPONENT_4 = 4;

    // Preference keys
    // These must match the key value (if it exists) in preferences.xml
    public static final String PREF_INSTALL_ID = "pref_install";
    public static final String PREF_VERSION_NAME = "pref_version_name";
    public static final String PREF_VERSION_CODE = "pref_version_code";
    public static final String PREF_SAVE_LOGIN = "pref_save_login_key";
    public static final String PREF_AUTO_LOGIN = "pref_auto_login_key";
    public static final String PREF_DISABLE_AUTO_LOGIN = "pref_disable_auto_login_key";
    public static final String PREF_CAT_NOTIFICATION = "pref_cat_notif_key";
    public static final String PREF_MWS_ADDRESS = "pref_mws_address_key";
    public static final String PREF_VERSION = "pref_version_key";
    public static final String PREF_LOGIN_TEMP = "pref_saved_temp_login_key";
    public static final String PREF_SESSION_DURATION = "pref_session_duration_key";
    public static final String PREF_SESSION_EXPIRATION = "pref_session_expiration_key";
    public static final String PREF_ABTEST_ID = "pref_abtest_id";
    public static final String PREF_ABTEST_EXP = "pref_abtest_exp";
    public static final String PREF_USER_ID = "pref_saved_user_id";
    public static final String PREF_USER_SHOULD_REFRESH_EXPENSE_LIST = "pref_user_should_refresh_expense_list";
    public static final String PREF_USER_CRN_CODE = "pref_saved_user_crn_code";
    public static final String PREF_USER_COMPANY_NAME = "pref_saved_user_compnay_name";
    public static final String PREF_LAST_USED_CRN_CODE = "pref_last_used_crn_code";
    public static final String PREF_ROLES = "pref_roles";
    // Whether the end-user is an expense report approver.
    public static final String PREF_CAN_EXPENSE_APPROVE = "pref_can_approve";
    // Whether the end-user is an travel request approver.
    public static final String PREF_CAN_TR_APPROVE = "pref_can_tr_approve";
    // Whether the end-user has travel request .
    public static final String PREF_HAS_TR = "pref_has_tr";
    // Whether the end-user is an travel request user.
    public static final String PREF_CAN_TR = "pref_can_tr";
    // Whether the end-user is a travel approver.
    public static final String PREF_CAN_TRAVEL_APPROVE = "pref_can_travel_approve";
    // Whether the end-user is a traveler (trip+car+hotel)
    public static final String PREF_CAN_TRAVEL = "pref_can_travel";
    // Whether the end-user is permitted to book air.
    public static final String PREF_CAN_AIR = "pref_can_air";
    // Whether the end-user is a flex fare user.
    public static final String PREF_CAN_FLEX_FARE = "pref_can_flex_fare";
    // Whether the end-user is an itinerary viewer (non-cliqbook company)
    public static final String PREF_IS_ITIN_VIEWER = "pref_is_itin_viewer";
    // Whether the end-user is an expense user (expense+report)
    public static final String PREF_CAN_EXPENSE = "pref_can_expense";
    // Whether the end-user can approver Invoices.
    public static final String PREF_IS_INVOICE_APPROVER = "pref_is_invoice_approver";
    // Whether the end-user can view and submit Invoices.
    public static final String PREF_IS_INVOICE_USER = "pref_is_invoice_user";
    // Whether the end-user can approver Purchase Requests.
    public static final String PREF_IS_PURCHASE_REQUEST_APPROVER = "pref_is_purchase_request_approver";
    // Whether the end-user can use rail.
    public static final String PREF_CAN_RAIL = "pref_can_rail";
    // Whether the end-user can dine.
    public static final String PREF_CAN_DINE = "pref_can_dine";
    // Whether the end-user can use a taxi.
    public static final String PREF_CAN_TAXI = "pref_can_taxi";
    // Whether the end-user can use an expense it app.
    public static final String PREF_SHOW_EXPENSEIT_AD = "pref_show_expenseit_ad";
    // Whether the end-user can "tax without card".
    public static final String PREF_CAN_TAX_WITHOUT_CARD = "pref_can_tax_without_card";
    // Show the tripit ad
    public static final String PREF_SHOW_TRIPIT_AD = "pref_show_tripit_ad";

    // Show LNA User
    public static final String PREF_CAN_LNA_USER = "pref_show_lna_user";

    // Show GOV User
    public static final String PREF_CAN_GOV_USER = "pref_show_gov_user";

    // Show open booking trips
    public static final String PREF_SHOW_OPEN_BOOKING = "pref_show_open_booking";

    // Show call travel agent
    public static final String PREF_SHOW_CALL_TRAVEL_AGENT = "pref_show_call_travel_agent";

    // What type of end-user, i.e., Breeze or Corporate. Should the value of
    // either
    // 'Const.USER_BREEZE' or 'Const.USER_CORPORATE'.
    public static final String PREF_ENTITY_TYPE = "pref_entity_type";
    // Base of key name for indicating if the application has run since install
    public static final String PREF_FIRST_TIME_RUNNING = "pref_first_time_running_";
    // Last prompt time as a long
    public static final String PREF_START_DELAY_PROMPT_TO_RATE = "pref_start_delay_prompt_to_rate";
    public static final String PREF_LAST_PROMPT_TO_RATE = "pref_last_prompt_to_rate";
    // Following 4 Values from the login response <SiteSetting>.
    public static final String PREF_CAN_CHECK_IN_LOCATION = "pref_can_check_in_location";
    public static final String PREF_CAN_EDIT_CARD_TRANS_DATE = "pref_can_edit_card_trans_date";
    public static final String PREF_CAN_DELETE_CARD_TRANS = "pref_can_delete_card_trans";
    public static final String PREF_CAN_SHOW_PERSONAL_CAR_MILEAGE_HOME = "pref_can_show_personal_car_mileage_home";

    // Boolean preference indicating whether the company has configured custom
    // required fields.
    public static final String PREF_REQUIRED_CUSTOM_FIELDS = "pref_required_custom_fields";
    // Integer preference indicating air profile status.
    public static final String PREF_TRAVEL_PROFILE_STATUS = "pref_travel_profile_status";
    // Whether to show offers on the segment list (not whether or not to
    // validate them)
    public static final String PREF_TRAVEL_SHOW_OFFERS = "pref_travel_show_offers";
    // Whether to validate offers on the segment list using their Validity
    // elements
    public static final String PREF_TRAVEL_CHECK_OFFER_VALIDITY = "pref_travel_check_offer_validity";
    // Contains the last saved date.

    public static final String PREF_LAST_SAVED_DATE = "pref_last_date";
    // Contains the timestamp of the last saved date.
    public static final String PREF_LAST_SAVED_DATE_TIME = "pref_last_date_time";
    // Contains the last saved location selection (report entry).
    public static final String PREF_LAST_SAVED_LOCATION_SELECTION = "pref_last_location_selection";
    // Contains the timestamp of the last saved location selection.
    public static final String PREF_LAST_SAVED_LOCATION_SELECTION_TIME = "pref_last_location_selection_time";
    // Contains the last saved location (quick expense).
    public static final String PREF_LAST_SAVED_LOCATION = "pref_last_location";
    // Contains the timestamp of the last saved location (quick expense).
    public static final String PREF_LAST_SAVED_LOCATION_TIME = "pref_last_location_time";

    // Contains whether or not the Receipt Store UI should be hidden from the
    // end-user.
    public static final String PREF_RECEIPT_STORE_HIDDEN = "pref_receipt_store_hidden";

    /**
     * Contains the language to use when performing voice searches.
     */
    public static final String PREF_VOICE_SEARCH_LANGUAGE = "pref_voice_search_language";

    // Open Source Library Key
    public static final String PREF_OPEN_SOURCE_LIBRARIES = "pref_open_source_libraries";

    // Contains whether or not a non-refundable warning message should be
    // displayed to the end-user
    // when booking a non-refundable fare.
    public static final String PREF_SHOW_NONREFUNDABLE_MESSAGE = "pref_show_nonrefundable_message";

    // Contains whether or not the search list should show codes
    public static final String PREF_SHOW_LIST_CODES = "pref_show_list_codes";

    public static final String PREF_ALLOW_APPROVALS = "pref_allow_approvals";
    public static final String PREF_ALLOW_REPORTS = "pref_allow_reports";
    public static final String PREF_ALLOW_TRAVEL_BOOKING = "pref_allow_travel_booking";
    public static final String PREF_ALLOW_VOICE_BOOKING = "pref_allow_voice_booking";
    public static final String PREF_ENABLE_SPDY = "pref_enable_spdy";

    // TripIt link information. SmartExpense specific
    public static final String PREF_TRIPIT_LINKED = "pref_tripit_linked";
    public static final String PREF_TRIPIT_EMAIL_CONFIRMED = "pref_tripit_email_confirmed";

    // Card agreement. SmartExpense specific
    public static final String PREF_CARD_AGREEMENT_ACCEPTED = "pref_card_agreement_accepted";

    //
    public static final String PREF_NSH_AGREE = "pref_need_safe_harbor_agreement";

    // Cityscape image preference trackers
    public static final String PREF_CITYSCAPE_UPDATE_TIME = "pref_cityscape_update_time";
    public static final String PREF_CURRENT_CITYSCAPE = "pref_cityscape_resource_id";
    public static final String PREF_CITYSCAPE_ACTIVATES_SINCE_SWAP = "pref_cityscape_activates_since_swap";

    // Allow ConditionalField Evaluation to show or hide visibility for fields on form
    public static final String PREF_ALLOW_CONDITIONAL_FIELD_EVALUATION = "pref_allow_conditional_field_evaluation";

    // Push notification settings
    public static final String PREF_PUSH_ALLOW = "pref_push_allow";
    public static final String PREF_PUSH_VIBRATE = "pref_push_vibrate";
    public static final long[] NOTIFICATION_VIBRATION_PATTERN = { 0, 100, 200, 300 };

    public static final String PREF_PIN_RESET_KEY_PART = "pref_pin_reset_key_part";
    public static final String PREF_PIN_RESET_EMAIL = "pref_pin_reset_email";
    public static final String PREF_CAT_GENERAL = "pref_cat_general_key";

    // Analytics tracking
    public static final String PREF_LOGIN_TRY_AGAIN_COUNT = "pref_try_again_count";
    public static final String PREF_TEST_DRIVE_SIGNIN_TRY_AGAIN_COUNT = "pref_test_drive_signin_try_again_count";
    public static final String PREF_TEST_DRIVE_REGISTRATION_ATTEMPT_COUNT = "pref_test_drive_registration_attempt_count";

    // Message Center Badge Settings
    public static final String PREF_MSG_CENTER_BADGE = "pref_msg_center_badge";

    // Increased Min SDK Message Flag
    public static final String PREF_MIN_SDK_INCREASE_MSG = "pref_min_sdk_increase_msg";

    // Test Drive Tip Overlay Settings
    public static final String PREF_TD_SHOW_OVERLAY_HOME = "pref_td_show_overlay_home";
    public static final String PREF_TD_SHOW_OVERLAY_EXPENSES = "pref_td_show_overlay_expenses";
    public static final String PREF_TD_SHOW_OVERLAY_RECEIPT_STORE = "pref_td_show_overlay_receipt_store";
    public static final String PREF_TD_SHOW_OVERLAY_EXPENSE_ACTIVE_REPORTS = "pref_td_show_overlay_expense_active_reports";
    public static final String PREF_TD_SHOW_OVERLAY_APPROVAL = "pref_td_show_overlay_approval";
    public static final String PREF_TD_SHOW_OVERLAY_EXPENSE_ENTRIES = "pref_td_show_overlay_expense_entries";
    public static final String PREF_TD_SHOW_OVERLAY_EXPENSE_ENTRIES_SUBMITTED = "pref_td_show_overlay_expense_entries_submitted";
    // Whether the end-user is test drive user
    public static final String PREF_PRODUCT_OFFERING = "pref_product_offering";
    // Expiration date of Test Drive user account
    public static final String PREF_ACCOUNT_EXPIRATION_DATE = "pref_account_expiration_date";
    // Test Drive expiration message phone number
    public static final String TEST_DRIVE_CONTACT_CONCUR_NUMBER = "1-888-883-8411";

    public static final String PREF_HAS_FIXED_TA = "pref_has_fixed_ta";
    public static final String PREF_HAS_TRAVEL_ALLOWANCE_FIXED = "pref_has_travel_allowance_fixed";

    // Constants identifying specific role names.
    // An approver.
    public static final String MOBILE_EXPENSE_APPROVER = "MOBILE_EXPENSE_MANAGER";
    // An expense user (expenses+reports)
    public static final String MOBILE_EXPENSE_USER = "MOBILE_EXPENSE_TRAVELER";
    // A non-rail traveler (trip+car+hotel)
    public static final String MOBILE_TRAVELER = "TravelUser";
    // A travel approver (trip+car+hotel+rail+air)
    public static final String MOBILE_TRAVEL_APPROVER = "TravelApprover";
    // An itinerary viewer (non-cliqbook company)
    public static final String MOBILE_ITIN_VIEWER = "ItineraryViewer";
    // A rail-only traveler.
    public static final String MOBILE_RAIL_USER = "Amtrak_User";
    // A dining user.
    public static final String MOBILE_DINING_USER = "Dining_User";
    // A taxi user.
    public static final String MOBILE_TAXI_USER = "Taxi_User";
    // An expenseit user.
    public static final String MOBILE_EXPENSEIT_USER = "ExpenseItUser";
    // A "tax without card" user.
    public static final String MOBILE_TAX_WITHOUT_CARD = "Tax_Without_Card";
    // A mobile air user (can book air).
    public static final String MOBILE_AIR_USER = "Air_Booking_Enabled";
    // Show the tripit ad.
    public static final String MOBILE_SHOW_TRIPIT_AD = "TripItAd_User";

    // An LNA user (can check in location).
    public static final String MOBILE_SHOW_LNA_USER = "LNA_User";

    // A GOV user.
    public static final String MOBILE_GOV_USER = "GovTravelManager";

    // Travel Request Approver Role
    public static final String MOBILE_TRAVEL_REQUEST_APPROVER = "Travel_Request_Approver";
    // A traveler that uses flex faring.
    public static final String MOBILE_FLEX_FARE_USER = "Flex_Faring";

    // Invoice Approver Role
    public static final String MOBILE_INVOICE_APPROVER = "MOBILE_INVOICE_PAYMENT_APRVR";

    // Invoice User Role
    public static final String MOBILE_INVOICE_USER = "MOBILE_INVOICE_PAYMENT_USER";

    // Invoice Purchase Request Approver Role
    public static final String MOBILE_INVOICE_PURCHASE_REQUEST_APPROVER = "MOBILE_INVOICE_PURCH_APRVR";

    // A Breeze user.
    public static final String ENTITY_TYPE_BREEZE = "Breeze";
    // A Corporate user.
    public static final String ENTITY_TYPE_CORPORATE = "Corporate";

    // A open booking user.
    public static final String MOBILE_OPEN_BOOKING_USER = "OpenBookingUser";

    // Show call travel agent functionality
    public static final String MOBILE_ENABLE_CALL_TRAVEL_AGENT = "EnableCallTravelAgent";

    // A Travel Points user.
    public static final String MOBILE_TRAVEL_POINTS_USER = "TravelPointsUser";

    // Travel Points User functionality
    public static final String PREF_TRAVEL_POINTS_USER = "pref_travel_points_user";

    // Show Price-to-Beat Menu
    public static final String PREF_SHOW_PRICE_TO_BEAT_MENU = "pref_show_price_to_beat_menu";

    // Show Price-to-Beat generator flag
    public static final String MOBILE_SHOW_P2B_GENERATOR = "ShowP2BGenerator";

    // Show Jarvis Hotel UI
    public static final String SHOW_JARVIS_HOTEL_UI = "ShowJarvisHotelUIOnAndroid";

    // User is loggedIn to ExpenseIt
    public static final String PREF_USER_LOGGED_IN_EXPENSE_IT = "pref_user_logged_in_expense_it";

    // The default ip/port for the MWS.
    public static final String BREEZE_WEB_ADDRESS = "https://www.concurbreeze.com/login";
    // public static final String DEFAULT_MWS_ADDRESS = "www.concursolutions.com";
    public static String DEFAULT_MWS_ADDRESS;

    // Web service action status codes
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_OKAY = "OKAY";
    public static final String STATUS_FAIL = "FAIL";

    // Travel search activity result codes
    public static final int RESULT_NEW_SEARCH = Activity.RESULT_FIRST_USER + 0;

    // Dialog IDs
    // See ConcurMobile.createDialog() for their usage
    public static final int DIALOG_LOGIN_WAIT = 0;
    public static final int DIALOG_RETRIEVE_ITINS = 1;
    public static final int DIALOG_EXPENSE_RETRIEVE_EXPENSE_APPROVALS = 2;
    public static final int DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL = 3;
    public static final int DIALOG_EXPENSE_APPROVE_REPORT = 4;
    public static final int DIALOG_EXPENSE_REJECT_COMMENT_PROMPT = 5;
    public static final int DIALOG_EXPENSE_RETRIEVE_RECEIPT = 6;
    public static final int DIALOG_EXPENSE_RETRIEVE_RECEIPT_UNAVAILABLE = 7;
    public static final int DIALOG_REGISTER_PIN = 8;
    public static final int DIALOG_EXPENSE_RETRIEVE_MOBILE_ENTRY = 9;
    public static final int DIALOG_RETRIEVE_CARDS = 10;
    public static final int DIALOG_OUT_OF_POCKET_EXPENSE_TRANSACTION_DATE = 12;
    public static final int DIALOG_OUT_OF_POCKET_EXPENSE_VENDOR_NAME = 13;
    public static final int DIALOG_OUT_OF_POCKET_EXPENSE_TYPE = 14;
    public static final int DIALOG_OUT_OF_POCKET_EXPENSE_CURRENCY = 15;
    public static final int DIALOG_OUT_OF_POCKET_EXPENSE_AMOUNT = 16;
    public static final int DIALOG_OUT_OF_POCKET_EXPENSE_LOCATION_NAME = 17;
    public static final int DIALOG_EXPENSE_RETRIEVE_ACTIVE_REPORTS = 18;
    public static final int DIALOG_EXPENSE_SUBMIT_REPORT = 19;
    public static final int DIALOG_EXPENSE_POLICY_ERROR_PROMPT = 20;
    public static final int DIALOG_EXPENSE_SAVE = 21;
    public static final int DIALOG_EXPENSE_DELETE = 22;
    public static final int DIALOG_EXPENSE_UNDEFINED_EXPENSE_TYPE = 24;
    public static final int DIALOG_EXPENSE_MISSING_RECEIPT = 25;
    public static final int DIALOG_EXPENSE_SUBMITTING_REPORT = 26;
    public static final int DIALOG_EXPENSE_SUBMIT_REPORT_FAILED = 27;
    public static final int DIALOG_EXPENSE_SET_REPORT_NAME = 28;
    public static final int DIALOG_EXPENSE_REPORT_NO_ENTRIES = 29;
    public static final int DIALOG_SYSTEM_UNAVAILABLE = 30;
    public static final int DIALOG_EXPENSE_REPORT_SEND_BACK = 31;
    public static final int DIALOG_EXPENSE_REPORT_SEND_BACK_FAILED = 32;
    public static final int DIALOG_EXPENSE_REPORT_APPROVE = 33;
    public static final int DIALOG_EXPENSE_APPROVE_REPORT_PROGRESS = 34;
    public static final int DIALOG_EXPENSE_APPROVE_REPORT_FAILED = 35;
    public static final int DIALOG_EXPENSE_ADD_TO_REPORT_FAILED = 36;
    public static final int DIALOG_EXPENSE_DELETE_FAILED = 37;
    public static final int DIALOG_EXPENSE_NO_EXPENSE_TYPE_CURRENCY = 39;
    public static final int DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED = 40;
    public static final int DIALOG_EXPENSE_SAVE_FAILED = 41;
    public static final int DIALOG_EXPENSE_DOWNLOAD_RECEIPT_FAILED = 42;
    public static final int DIALOG_EXPENSE_APPROVAL_RETRIEVE_FAILED = 43;
    public static final int DIALOG_EXPENSE_ACTIVE_REPORT_RETRIEVE_FAILED = 44;
    public static final int DIALOG_EXPENSE_REPORT_DETAIL_RETRIEVE_FAILED = 45;
    public static final int DIALOG_EXPENSE_EXPENSES_RETRIEVE_FAILED = 46;
    public static final int DIALOG_EXPENSE_REPORT_REMOVE_EXPENSE_ENTRY_CONFIRM = 47;
    public static final int DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_FAILED = 48;
    public static final int DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_PROGRESS = 49;
    public static final int DIALOG_EXPENSE_SUBMIT_REPORT_CONFIRM = 50;
    public static final int DIALOG_LOGIN_FAIL = 51;
    public static final int DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL = 52;
    public static final int DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL_FAILED = 53;
    public static final int DIALOG_TRAVEL_SEARCH_HOTEL_FAILED = 54;
    public static final int DIALOG_TRAVEL_HOTEL_VIEW_IMAGES = 55;
    public static final int DIALOG_NO_CONNECTIVITY = 56;
    public static final int DIALOG_EXPENSE_SAVE_RECEIPT = 57;
    public static final int DIALOG_EXPENSE_SAVE_RECEIPT_FAILED = 58;
    public static final int DIALOG_EXPENSE_SAVE_REPORT_ENTRY = 59;
    public static final int DIALOG_EXPENSE_SAVE_REPORT_ENTRY_FAILED = 60;
    public static final int DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL = 61;
    public static final int DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL_FAILED = 62;
    public static final int DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT = 63;
    public static final int DIALOG_EXPENSE_SAVE_REPORT_RECEIPT = 64;
    public static final int DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT = 65;
    public static final int DIALOG_EXPENSE_ADD_REPORT_RECEIPT_SUCCEEDED = 66;
    public static final int DIALOG_EXPENSE_ADD_REPORT_RECEIPT_FAILED = 67;
    public static final int DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_FAILED = 68;
    public static final int DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE = 69;
    public static final int DIALOG_EXPENSE_SAVE_REPORT = 70;
    public static final int DIALOG_EXPENSE_SAVE_REPORT_FAILED = 71;
    public static final int DIALOG_EXPENSE_ENTRY_FORM = 72;
    public static final int DIALOG_EXPENSE_CREATE_REPORT = 73;
    public static final int DIALOG_EXPENSE_CONFIRM_SAVE_REPORT = 74;
    public static final int DIALOG_EXPENSE_INVALID_FORM_FIELD_VALUES = 75;
    public static final int DIALOG_EXPENSE_MISSING_HARD_STOP_FORM_FIELD_VALUES = 76;
    public static final int DIALOG_EXPENSE_MISSING_SOFT_STOP_FORM_FIELD_VALUES = 77;
    public static final int DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE = 78;
    public static final int DIALOG_EXPENSE_RETRIEVE_EXCHANGE_RATE_FAILED = 79;
    public static final int DIALOG_RETRIEVE_EXPENSE_TYPES = 80;
    public static final int DIALOG_SELECT_EXPENSE_TYPE = 81;
    public static final int DIALOG_EXPENSE_ITEMIZE_HOTEL = 82;
    public static final int DIALOG_EXPENSE_ITEMIZE_HOTEL_FAILED = 83;
    public static final int DIALOG_EXPENSE_COPY_DOWN_FIELD_VALUES = 84;
    public static final int DIALOG_WHATS_NEW = 85;
    public static final int DIALOG_EXPENSE_NO_MILEAGE_FORM = 86;
    public static final int DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_PROGRESS = 87;
    public static final int DIALOG_EXPENSE_RETRIEVE_REPORT_ENTRY_DETAIL_FAILED = 88;
    public static final int DIALOG_EXPENSE_DELETE_RECEIPT = 89;
    public static final int DIALOG_EXPENSE_DELETE_RECEIPT_FAILED = 90;
    public static final int DIALOG_EXPENSE_DELETE_RECEIPT_CONFIRM = 91;
    public static final int DIALOG_EXPENSE_RETRIEVE_RECEIPT_URLS_FAILED = 92;
    public static final int DIALOG_EXPENSE_NO_PDF_VIEWER = 93;
    public static final int DIALOG_EXPENSE_RECEIPT_STORE_CONFIRM_ADD_TO_REPORT = 94;
    public static final int DIALOG_EXPENSE_RECEIPT_STORE_SELECT_FOR_EXPENSE = 95;
    public static final int DIALOG_EXPENSE_RECEIPT_STORE_SELECT_FOR_QUICK_EXPENSE = 96;
    public static final int DIALOG_EXPENSE_ADD_ATTENDEE = 97;
    public static final int DIALOG_EXPENSE_CONTACT_IMPORT_FAILED = 98;
    public static final int DIALOG_EXPENSE_ATTENDEE_CONFIRM_CONTACT_CHOICE = 99;
    public static final int DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_PROGRESS = 100;
    public static final int DIALOG_EXPENSE_RETRIEVE_ATTENDEE_FORM_FAILED = 101;
    public static final int DIALOG_EXPENSE_CONFIRM_SAVE_ATTENDEE = 102;
    public static final int DIALOG_EXPENSE_ATTENDEE_SAVE_PROGRESS = 103;
    public static final int DIALOG_EXPENSE_ATTENDEE_SAVE_FAILED = 104;
    public static final int DIALOG_EXPENSE_DEFAULT_ATTENDEE_PROGRESS = 105;
    public static final int DIALOG_EXPENSE_DEFAULT_ATTENDEE_FAILED = 106;

    public static final int DIALOG_EXPENSE_ATTENDEE_ADD_PROGRESS = 107;
    public static final int DIALOG_EXPENSE_ATTENDEE_ADD_FAILED = 108;
    public static final int DIALOG_EXPENSE_ATTENDEE_REMOVE_PROGRESS = 109;
    public static final int DIALOG_EXPENSE_ATTENDEE_REMOVE_FAILED = 110;
    public static final int DIALOG_EXPENSE_ATTENDEE_UPDATE_PROGRESS = 111;
    public static final int DIALOG_EXPENSE_ATTENDEE_UPDATE_FAILED = 112;
    public static final int DIALOG_EXPENSE_CONFIRM_ATTENDEE_REMOVE = 113;

    public static final int DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_PROGRESS = 114;
    public static final int DIALOG_EXPENSE_RETRIEVE_EXPENSE_TYPE_FAILED = 115;
    public static final int DIALOG_EXPENSE_ATTENDEE_NO_EDIT = 116;
    public static final int DIALOG_EXPENSE_ATTENDEE_VERSION_MISMATCH = 117;
    public static final int DIALOG_EXPENSE_ATTENDEE_TYPE_NO_EDIT = 118;

    public static final int DIALOG_EXPENSE_VIEW_COMMENT = 119;

    public static final int DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM = 120;

    public static final int DIALOG_PROMPT_TO_RATE = 121;

    public static final int DIALOG_TRAVEL_NO_AIR_PERMISSION = 122;
    public static final int DIALOG_TRAVEL_BOOKING_CUSTOM_REQUIRED_FIELDS = 123;
    public static final int DIALOG_TRAVEL_PROFILE_INCOMPLETE = 124;

    public static final int DIALOG_EXPENSE_GET_REPORT_FORM = 125;
    public static final int DIALOG_EXPENSE_GET_REPORT_FORM_FAILED = 126;

    public static final int DIALOG_TRAVEL_RETRIEVE_ITINERARY = 127;
    public static final int DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED = 128;

    public static final int DIALOG_TRAVEL_RETRIEVE_FLIGHT_STATS = 129;
    public static final int DIALOG_EXPENSE_DELETE_REPORT_CONFIRM = 130;
    public static final int DIALOG_EXPENSE_DELETE_REPORT_PROGRESS = 131;
    public static final int DIALOG_EXPENSE_DELETE_REPORT_FAILED = 133;

    public static final int DIALOG_COMPANY_SIGNON_REQUIRED = 134;

    public static final int DIALOG_EXPENSE_NO_ATTENDEE_TYPES = 135;

    public static final int DIALOG_TRAVEL_VIOLATION_REASON = 136;
    public static final int DIALOG_TRAVEL_VIOLATION_NO_REASONS = 137;
    public static final int DIALOG_TRAVEL_VIOLATION_JUSTIFICATION = 138;
    public static final int DIALOG_TRAVEL_VIOLATION_VIEW_MESSAGE = 139;

    public static final int DIALOG_EXPENSE_RECEIPT_STORE_RETRIEVE_RECEIPTS = 140;

    public static final int DIALOG_EXPENSE_SELECT_WORKFLOW_ACTION = 141;
    public static final int DIALOG_NO_IMAGING_CONFIGURATION = 142;
    public static final int DIALOG_TRAVEL_FLEX_FARE = 143;

    public static final int MISSING_ITEMIZATION_ALERT_DIALOG = 145;

    public static final int DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND = 146;

    public static final int DIALOG_EXPENSE_NO_EXPENSE_TYPES = 147;
    public static final int DIALOG_EXPENSE_CONFIRM_CLEAR_RECEIPT = 148;
    public static final int DIALOG_EXPENSE_CLEAR_RECEIPT_PROGRESS = 149;
    public static final int DIALOG_EXPENSE_CLEAR_RECEIPT_FAILED = 150;

    public static final int ALTERNATIVE_AIR_SEARCH_PROGRESS_DIALOG = 144;
    public static final int ALTERNATIVE_AIR_SEARCH_FAIL_DIALOG = 151;

    public static final int DIALOG_ALLOW_REPORTS = 152;
    public static final int DIALOG_EXPENSE_ORIGINAL_RECEIPT_REQUIRED = 153;

    // TAX Forms
    public static final int DIALOG_EXPENSE_TAX_FORM_PROGRESS = 154;
    public static final int DIALOG_EXPENSE_TAX_FORM_PROGRESS_FAILURE = 155;

    public static final int DIALOG_EXPENSE_REFRESH_EXPENSES = 156;

    // Conditional formField Update
    public static final int DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS = 157;
    public static final int DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS_FAILURE = 158;

    public static final int DIALOG_EXPENSE_RECEIPT_APPEND_FAIL = 159;

    public static final int DIALOG_EXPENSE_RETRIEVE_E_RECEIPT_UNAVAILABLE = 160;

    // Message what values for handlers
    // These don't have to be unique from an Android viewpoint (different
    // handlers can use the same ID for different things)
    // but we need them to be unique since these also serve as the IDs for the
    // RESPONSES table
    public static final int MSG_LOGIN_RESULT = 0;
    public static final int MSG_ITINERARY_LIST_REQUEST = 1;

    // Expense related message values.
    public static final int MSG_EXPENSE_REPORT_APPROVAL_LIST_REQUEST = 2;
    public static final int MSG_EXPENSE_REPORT_APPROVAL_LIST_REPLY = 3;
    public static final int MSG_EXPENSE_REPORT_DETAIL_REQUEST = 4;
    public static final int MSG_EXPENSE_REPORT_DETAIL_REPLY = 5;
    public static final int MSG_EXPENSE_REPORT_APPROVE_REQUEST = 6;
    public static final int MSG_EXPENSE_REPORT_REJECT_REQUEST = 7;
    // public static final int MSG_EXPENSE_MOBILE_ENTRY_REQUEST = 8;
    public static final int MSG_EXPENSE_MOBILE_ENTRY_SAVE_REQUEST = 9;
    public static final int MSG_EXPENSE_MOBILE_ENTRY_DELETE_REQUEST = 10;
    public static final int MSG_REGISTER_RESULT = 11;
    public static final int MSG_CARD_LIST_REQUEST = 12;
    public static final int MSG_PERSONAL_CARD_CHARGE_HIDE_REQUEST = 13;
    public static final int MSG_CORPORATE_CARD_CHARGE_HIDE_REQUEST = 14;
    public static final int MSG_SUMMARY_COUNT_REQUEST = 15;
    public static final int MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST = 16;
    public static final int MSG_EXPENSE_UPLOAD_RECEIPT_REQUEST = 17;
    public static final int MSG_EXPENSE_ACTIVE_REPORTS_REQUEST = 18;
    public static final int MSG_EXPENSE_REPORT_SUBMIT_REQUEST = 19;
    public static final int MSG_EXPENSE_ADD_TO_REPORT_REQUEST = 20;
    public static final int MSG_EXPENSE_ALL_EXPENSE_REQUEST = 21;
    public static final int MSG_EXPENSE_REMOVE_REPORT_EXPENSE_REQUEST = 22;

    public static final int MSG_TRAVEL_CAR_SEARCH_REQUEST = 23;
    public static final int MSG_SYSTEM_CONFIG_REQUEST = 24;
    public static final int MSG_TRAVEL_LOCATION_SEARCH_REQUEST = 25;
    public static final int MSG_TRAVEL_HOTEL_SEARCH_REQUEST = 26;
    public static final int MSG_TRAVEL_HOTEL_DETAIL_REQUEST = 27;
    public static final int MSG_USER_CONFIG_REQUEST = 28;
    public static final int MSG_TRAVEL_RAIL_STATION_LIST_REQUEST = 29;
    public static final int MSG_TRAVEL_RAIL_SEARCH_REQUEST = 30;
    public static final int MSG_TRAVEL_HOTEL_CONFIRM_REQUEST = 31;
    public static final int MSG_TRAVEL_CAR_SELL_REQUEST = 32;

    public static final int MSG_LOGOUT_REQUEST = 33;
    public static final int MSG_CLEAR_LOCAL_DATA = 34;
    public static final int MSG_CANCEL_HOTEL_REQUEST = 35;

    public static final int MSG_MARK_RECEIPTS_VIEWED_REQUEST = 36;

    public static final int MSG_TRAVEL_RAIL_TICKET_DELIVERY_OPTION_REQUEST = 37;
    public static final int MSG_TRAVEL_RAIL_SELL_REQUEST = 38;

    public static final int MSG_EXPENSE_SAVE_RECEIPT_REQUEST = 39;
    public static final int MSG_EXPENSE_SAVE_REPORT_ENTRY_REQUEST = 40;
    public static final int MSG_EXPENSE_GET_RECEIPT_IMAGE_URL = 41;
    public static final int MSG_EXPENSE_ADD_REPORT_RECEIPT_REQUEST = 42;
    public static final int MSG_EXPENSE_GET_RECEIPT_IMAGE_URLS = 43;
    public static final int MSG_EXPENSE_GET_EXPENSE_TYPES = 44;
    public static final int MSG_EXPENSE_SEARCH_LIST_REQUEST = 45;
    public static final int MSG_EXPENSE_SAVE_REPORT_REQUEST = 46;

    public static final int MSG_EXPENSE_REPORT_ENTRY_FORM_REQUEST = 47;
    public static final int MSG_EXPENSE_CAR_CONFIGS_REQUEST = 48;
    public static final int MSG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_REQUEST = 49;

    public static final int MSG_EXPENSE_EXCHANGE_RATE_REQUEST = 50;
    public static final int MSG_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_REQUEST = 51;
    public static final int MSG_EXPENSE_ITEMIZE_HOTEL_REQUEST = 52;

    public static final int MSG_EXPENSE_REPORT_HEADER_DETAIL_REQUEST = 53;
    public static final int MSG_EXPENSE_ATTENDEE_SEARCH_REQUEST = 54;
    public static final int MSG_EXPENSE_REPORT_ENTRY_DETAIL_REQUEST = 55;

    public static final int MSG_CANCEL_CAR_REQUEST = 56;
    public static final int MSG_EXPENSE_DELETE_RECEIPT_REQUEST = 57;
    public static final int MSG_EXPENSE_RETRIEVE_URL_REQUEST = 58;

    public static final int MSG_EXPENSE_DISTANCE_TO_DATE_REQUEST = 59;

    public static final int MSG_EXPENSE_ATTENDEE_SAVE_REQUEST = 60;
    public static final int MSG_EXPENSE_GET_ATTENDEE_TYPES_REQUEST = 61;
    public static final int MSG_EXPENSE_ATTENDEE_FORM_REQUEST = 62;
    public static final int MSG_EXPENSE_DEFAULT_ATTENDEE_REQUEST = 63;

    public static final int MSG_TRAVEL_AIR_SEARCH_REQUEST = 65;
    public static final int MSG_TRAVEL_AIR_FILTER_REQUEST = 66;
    public static final int MSG_TRAVEL_AIR_SELL_REQUEST = 67;
    public static final int MSG_TRAVEL_AIR_CANCEL_REQUEST = 68;

    public static final int MSG_MARK_ENTRY_RECEIPT_VIEWED_REQUEST = 69;

    public static final int MSG_LOCATION_CHECK_IN_REQUEST = 70;

    public static final int MSG_TRAVEL_HOTEL_IMAGES_REQUEST = 71;

    public static final int MSG_EXPENSE_REPORT_FORM_REQUEST = 72;

    public static final int MSG_EXPENSE_APPROVER_SEARCH_REQUEST = 73;

    public static final int MSG_ITINERARY_SUMMARY_LIST_REQUEST = 74;
    public static final int MSG_ITINERARY_REQUEST = 75;
    public static final int MSG_EXPENSE_DELETE_REPORT_REQUEST = 76;

    public static final int MSG_SSO_QUERY_REQUEST = 77;

    public static final int MSG_EXPENSE_EXTENDED_ATTENDEE_SEARCH_REQUEST = 78;
    public static final int MSG_EXPENSE_ATTENDEE_SEARCH_FIELDS_REQUEST = 79;

    public static final int MSG_TRAVEL_CUSTOM_FIELDS_REQUEST = 80;
    public static final int MSG_FILLED_OUT_TRAVEL_CUSTOM_FIELD_INFO = 81;

    public static final int MSG_TRAVEL_REASON_CODE_REQUEST = 82;

    public static final int MSG_TRAVEL_CUSTOM_FIELDS_UPDATE_REQUEST = 83;

    public static final int MSG_ALTERNATIVE_FLIGHT_SEARCH_REQUEST = 84;

    public static final int MSG_POST_CRASH_LOG_REQUEST = 85;

    public static final int MSG_EXPENSE_APPEND_RECEIPT_REQUEST = 86;

    public static final int MSG_EXPENSE_ADD_REPORT_RECEIPT_V2_REQUEST = 87;

    public static final int MSG_EXPENSE_CLEAR_REPORT_ENTRY_RECEIPT_REQUEST = 88;

    public static final int MSG_CANCEL_RAIL_REQUEST = 89;

    public static final int MSG_EXPENSE_CURRENCY_SEARCH_REQUEST = 90;

    public static final int MSG_NOTIFICATION_REGISTER = 91;

    public static final int MSG_TRAVEL_CUSTOM_FIELD_SEARCH_REQUEST = 92;

    public static final int MSG_GET_TAX_FORM_REQUEST = 93;

    public static final int MSG_EXPENSE_GET_CONDITIONAL_FIELDS = 94;

    // Login response map constants
    public static final String LR_SERVER_URL = "server_url";
    public static final String LR_ABTEST_ID = "abtest_id";
    public static final String LR_ABTEST_EXP = "abtest_exp";
    public static final String LR_SESSION_ID = "session_id";
    public static final String LR_ACCESS_TOKEN = "oauth_access_token";
    public static final String LR_ACCESS_TOKEN_SECRET = "oauth_access_token_secret";
    public static final String LR_AUTHENTICATION_TYPE = "login_authentication_type";
    public static final String LR_STATUS = "status";
    public static final String LR_STATUS_DISABLED = "MobileDisabled";
    public static final String LR_STATUS_EXPIRED = "Pin Expired";
    public static final String LR_WIPED = "wiped";
    public static final String LR_SESSION_DURATION = "session_duration";
    public static final String LR_SESSION_EXPIRATION = "session_expiration";
    public static final String LR_USER_ID = "user_id";
    public static final String LR_ROLES = "roles";
    public static final String LR_ERROR_MESSAGE = "error.message";
    public static final String NEED_SAFE_HARBOR_AGREEMENT = "NeedSafeHarborAgreement";

    public static final String LR_USER_CRN_CODE = "user_crn_code";
    public static final String LR_ENTITY_TYPE = "entity_type";
    public static final String LR_PRODUCT_OFFERING = "product_offering";
    public static final String LR_ACCOUNT_EXPIRATION_DATE = "account_expiration_date";
    public static final String LR_CONTACT_COMPANY_NAME = "contact_company_name";
    public static final String LR_CONTACT_FIRST_NAME = "contact_first_name";
    public static final String LR_CONTACT_LAST_NAME = "contact_last_name";
    public static final String LR_CONTACT_EMAIL = "contact_email";
    public static final String LR_CONTACT_MIDDLE_INITIAL = "contact_middle_initial";
    public static final String LR_REQUIRED_CUSTOM_FIELDS = "required_custom_fields";
    public static final String LR_DISABLE_AUTO_LOGIN = "disable_auto_login";
    public static final String LR_TRAVEL_PROFILE_STATUS = "travel.profile.status";
    public static final String LR_SITE_SETTINGS_LOCACTION_CHECK_IN = "site_settings_location_check_in";
    public static final String LR_SITE_SETTINGS_CARD_TRANS_DATE_EDITABLE = "site.settings.card.trans.date.editable";
    public static final String LR_SITE_SETTINGS_CARD_ALLOW_TRANS_DELETE = "site.settings.card.allow.trans.delete";
    public static final String LR_SITE_SETTINGS_MOBILE_PERSONAL_CAR_MILEAGE_ON_HOME = "site.settings.mobile.personal.car.mileage.on.home";
    public static final String LR_SITE_SETTINGS_MOBILE_HAS_FIXED_TA = "site.settings.mobile.has.fixed.ta";
    public static final String LR_SITE_SETTINGS_MOBILE_HAS_TRAVEL_ALLOWANCE_FIXED = "site.settings.mobile.has.travel.allowance.fixed";
    public static final String LR_SITE_SETTINGS_HIDE_RECEIPT_STORE = "site.settings.hide.receipt.store";
    public static final String LR_SITE_SETTINGS_SHOW_NONREFUNDABLE_MESSAGE = "site.settings.show.nonrefundable.message";
    public static final String LR_SITE_SETTINGS_SHOW_LIST_CODES = "site.settings.show.list.codes";
    public static final String LR_SITE_SETTINGS_ALLOW_APPROVALS = "site.settings.allow.approvals";
    public static final String LR_SITE_SETTINGS_ALLOW_REPORTS = "site.settings.allow.reports";
    public static final String LR_SITE_SETTINGS_ALLOW_TRAVEL_BOOKING = "site.settings.allow.travel.booking";
    public static final String LR_SITE_SETTINGS_ALLOW_VOICE_BOOKING = "site.settings.allow.voice.booking";
    public static final String LR_SITE_SETTINGS_ALLOW_HOTEL_SEARCH_STREAMING = "site.settings.allow.hotel.search.streaming";
    public static final String LR_SITE_SETTINGS_ENABLE_CONDITIONAL_FIELD_EVALUATION = "site.settings.allow.conditional.field.evaluation";
    public static final String LR_SITE_SETTINGS_ENABLE_SPDY = "site.settings.enable.spdy";
    public static final String LR_PERMISSIONS_TR = "permissions.tr";
    public static final String LR_PERMISSIONS_TR_USER = "permissions.tr.user";
    public static final String LR_PERMISSIONS_TR_APPROVER = "permissions.tr.approver";
    public static final String LR_SITE_SETTINGS_SHOW_JARVIS_HOTEL_UI = "site.settings.showhoteljarvisui";

    // Register response map constants
    public static final String RR_STATUS = "status";
    public static final String RR_STATUS_MESSAGE = "status_message";
    public static final String RR_REG_STATUS = "reg_status";
    public static final String RR_RCODE = "rcode";

    // SystemConfig response constants.
    public static final String SC_RESPONSE_UPDATED_ID = "UPDATED";
    public static final String SC_RESPONSE_NO_CHANGE_ID = "NO_CHANGE";

    // Intent related constants
    public static final String ACTION_SUMMARY_UPDATED = "com.concur.mobile.action.SUMMARY_UPDATED";

    public static final String ACTION_TRIPS_UPDATED = "com.concur.mobile.action.TRIPS_UPDATED";
    public static final String ACTION_SUMMARY_TRIPS_UPDATED = "com.concur.mobile.action.SUMMARY_TRIPS_UPDATED";
    public static final String ACTION_TRIP_UPDATED = "com.concur.mobile.action.TRIP_UPDATED";
    public static final String EXTRA_ITIN_LOCATOR = "itin_locator";
    public static final String EXTRA_SEGMENT_KEY = "seg_key";
    public static final String EXTRA_OFFER_ID = "offer_id";
    public static final String EXTRA_COMBO_BOX_ACTION = "combo_box_action";
    public static final String EXTRA_COMBO_BOX_INLINE_TEXT = "combo_box_inline_text";
    public static final int COMBO_BOX_INLINE_TEXT = 0;
    public static final int COMBO_BOX_LIST_SELECTION = 1;

    // Activity result codes
    public static final int CREATE_MOBILE_ENTRY = 1;
    public static final int EDIT_MOBILE_ENTRY = 2;
    public static final int REQUEST_CODE_BOOK_HOTEL = 3;
    public static final int REQUEST_CODE_BOOK_CAR = 4;
    public static final int CREATE_MILEAGE_EXPENSE = 5;
    public static final int REQUEST_CODE_BOOK_RAIL = 6;
    public static final int REQUEST_CODE_LOCATION = 7;
    public static final int CREATE_NEW_REPORT = 8;
    public static final int SEARCH_APPROVER = 9;
    public static final int REQUEST_CODE_VIEW_SEGMENT_DETAIL = 10;
    public static final int REQUEST_CODE_SSO_LOGIN = 11;
    public static final int REQUEST_CODE_ADD_EXPENSES = 12;
    public static final int REQUEST_CODE_TRAVEL_POINTS_APPROVAL_CHOICE = 13;
    public static final int REQUEST_CODE_USE_TRAVEL_POINTS = 14;
    public static final int REQUEST_CODE_USE_MANAGER_APPROVAL = 15;

    // Login related extra intent key values.
    public static final String EXTRA_COMPANY_SIGN_ON_SESSION_ID = "login.company.sign.on.session.id";
    public static final String EXTRA_SSO_COMPANY_CODE = "sso.company.code";
    public static final String EXTRA_LOGIN_HELP_TOPIC_TITLE = "login.help.topic.title";
    public static final String EXTRA_LOGIN_HELP_TOPIC_SUBHEADER = "login.help.topic.subheader";
    public static final String EXTRA_LOGIN_HELP_TOPIC_MESSAGE = "login.help.topic.message";
    public static final String EXTRA_LOGIN_LAUNCHED_FROM_PRE_LOGIN = "login.launched.from.pre.login";
    public static final String EXTRA_LOGIN_LAUNCHED_FROM_TEST_DRIVE_REGISTRATION = "login.launched.from.test.drive.registration";

    // Itinerary and Travel Allowance related extra intent key values.
    public static final String EXTRA_ITINERARY_KEY = "itinerary.key";

    // Travel related extra intent key values.
    public static final String EXTRA_TRAVEL_VOICE_BOOK_INITIATED = "travel.voice.book.initiated";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION = "travel.hotel.search.location";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_AMOUNT = "travel.hotel.search.distance.amount";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID = "travel.hotel.search.distance.id";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_NAME = "travel.hotel.search.distance.unit.name";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID = "travel.hotel.search.distance.unit.id";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_SHOW_GDS_NAME = "travel.hotel.search.show.gds.name";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING = "travel.hotel.search.names.containing";
    public static final String EXTRA_HOTEL_SEARCH_FILTER_LESS_THAN_AMOUNT = "travel.hotel.search.filter.less.than.amount";
    public static final String EXTRA_HOTEL_SEARCH_FILTER_LESS_THAN_AMOUNT_CURRENCY = "travel.hotel.search.filter.less.than.amount.currency";

    public static final String EXTRA_TRAVEL_LATITUDE = "travel.latitude";
    public static final String EXTRA_TRAVEL_LONGITUDE = "travel.longitude";

    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN = "travel.hotel.search.check.in";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR = "travel.hotel.search.check.in.calendar";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT = "travel.hotel.search.check.out";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR = "travel.hotel.search.check.out.calendar";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID = "travel.hotel.search.property.id";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_FROM_CACHE = "travel.hotel.search.property.detail.from.cache";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_ROOM_ID = "travel.hotel.search.room.id";
    public static final String EXTRA_TRAVEL_ITINERARY_LOCATOR = "travel.itinerary.locator";
    public static final String EXTRA_TRAVEL_CLIQBOOK_TRIP_ID = "travel.cliqbook.trip.id";
    public static final String EXTRA_TRAVEL_CLIENT_LOCATOR = "travel.client.locator";
    public static final String EXTRA_TRAVEL_RECORD_LOCATOR = "travel.record.locator";
    public static final String EXTRA_TRAVEL_CAR_SEARCH_PICK_UP = "travel.car.search.pick.up";
    public static final String EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR = "travel.car.search.pick.up.calendar";
    public static final String EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF = "travel.car.search.drop.off";
    public static final String EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR = "travel.car.search.drop.off.calendar";
    public static final String EXTRA_TRAVEL_CAR_TYPE = "travel.car.type";
    public static final String EXTRA_TRAVEL_OFF_AIRPORT = "travel.off.airport";
    public static final String EXTRA_TRAVEL_LOCATION = "travel.location";
    public static final String EXTRA_TRAVEL_LAUNCHED_WITH_CLIQBOOK_TRIP_ID = "travel.launched.with.cliqbook.trip.id";
    public static final String EXTRA_TRAVEL_AIR_CHOICE_FARE_ID = "travel.air.choice.fare.id";
    public static final String EXTRA_TRAVEL_CREDIT_CARD_ID = "travel.credit.card.id";
    public static final String EXTRA_TRAVEL_HOTEL_POLLING_ID = "travel.hotel.polling.id";
    public static final String EXTRA_TRAVEL_HOTEL_SHOW_READ_ONLY_LIST = "travel.hotel.show.read.only.list";
    public static final String EXTRA_TRAVEL_HOTEL_SEARCH_WORKFLOW_START_TIME = "travel.hotel.show.search.workflow.start.time";

    // offline
    public static final String EXTRA_ENABLE_OFFLINE_MODE_NOTIFICATION = "enable_offline_mode_notification";
    public static final String EXTRA_QUEUE_ITEMS_AVAIL_NOTIFICATION = "offline_queue_items_available";
    public static final String ACTION_QUEUE_ITEMS_AVAILABLE = "com.concur.mobile.action.data.offline.items.available";
    public static final String ACTION_QUEUE_ITEMS_UNAVAILABLE = "com.concur.mobile.action.data.offline.items.unavailable";

    public static final String EXTRA_TRAVEL_HOTEL_IMAGES = "travel.hotel.images";

    public static final String EXTRA_CAR_DETAIL_ID = "car_id";

    public static final String EXTRA_LOCATION_SEARCH_ALLOWED_MODES = "loc_search_modes_allowed";
    public static final String EXTRA_LOCATION_SEARCH_MODE_USED = "loc_search_mode_used";
    public static final String EXTRA_IMAGE_URL = "image.url";
    public static final String EXTRA_IMAGE_TITLE = "image.title";

    public static final String EXTRA_PCA_KEY = "pca_key";

    public static final String ACTION_AIR_SEARCH_RESULTS = "com.concur.mobile.action.FLIGHTS_FOUND";
    public static final String ACTION_AIR_FILTER_RESULTS = "com.concur.mobile.action.FLIGHTS_FILTERED";
    public static final String ACTION_AIR_BOOK_RESULTS = "com.concur.mobile.action.FLIGHT_BOOK_RESULT";
    public static final String ACTION_AIR_CANCEL_RESULTS = "com.concur.mobile.action.FLIGHT_CANCEL_RESULT";
    public static final String ACTION_CAR_SEARCH_RESULTS = "com.concur.mobile.action.CARS_FOUND";
    public static final String ACTION_CAR_SELL_RESULTS = "com.concur.mobile.action.CAR_BOOK_RESULT";
    public static final String ACTION_HOTEL_SEARCH_RESULTS = "com.concur.mobile.action.HOTELS_FOUND";
    public static final String ACTION_HOTEL_DETAIL_RESULTS = "com.concur.mobile.action.HOTEL_DETAIL_FOUND";
    public static final String ACTION_HOTEL_CONFIRM_RESULTS = "com.concur.mobile.action.HOTEL_CONFIRM_RESULT";
    public static final String ACTION_HOTEL_CANCEL_RESULT = "com.concur.mobile.action.HOTEL_CANCEL_RESULT";
    public static final String ACTION_CAR_CANCEL_RESULT = "com.concur.mobile.action.CAR_CANCEL_RESULT";
    public static final String ACTION_RAIL_CANCEL_RESULT = "com.concur.mobile.action.RAIL_CANCEL_RESULT";
    public static final String ACTION_RAIL_STATION_LIST_RESULTS = "com.concur.mobile.action.RAIL_STATION_LIST_UPDATED";
    public static final String ACTION_RAIL_SEARCH_RESULTS = "com.concur.mobile.action.TRAINS_FOUND";
    public static final String ACTION_RAIL_TDO_RESULTS = "com.concur.mobile.action.DELIVERY_OPTIONS_RETRIEVED";
    public static final String ACTION_RAIL_SELL_RESULTS = "com.concur.mobile.action.RAIL_RESERVED";
    public static final String ACTION_ALTERNATIVE_AIR_SEARCH_RESULTS = "com.concur.mobile.action.ALTERNATIVE_FLIGHTS_FOUND";
    public static final String ACTION_HOTEL_IMAGES_RESULTS = "com.concur.mobile.action.HOTEL_IMAGES_FOUND";
    public static final String ACTION_LOCATION_SEARCH_RESULTS = "com.concur.mobile.action.LOCATIONS_FOUND";
    public static final String ACTION_CARDS_UPDATED = "com.concur.mobile.action.CARDS_UPDATED";
    public static final String ACTION_LOGOUT = "com.concur.mobile.action.LOGOUT";
    public static final String ACTION_LOCATION_CHECK_IN = "com.concur.mobile.action.LOCATION_CHECK_IN";
    public static final String ACTION_LOC_PROVIDER_DISABLED = "com.concur.mobile.action.ACTION_LOC_PROVIDER_DISABLED";
    public static final String ACTION_DELETE_REPORT = "com.concur.mobile.action.DELETE_REPORT";
    public static final String ACTION_CORP_SSO_QUERY = "com.concur.mobile.action.CORP_SSO_QUERY";
    public static final String ACTION_CORP_SSO_LOGIN = "com.concur.mobile.aciton.CORP_SSO_LOGIN";
    public static final String ACTION_TRAVEL_CUSTOM_FIELDS = "com.concur.mobile.action.TRAVEL_CUSTOM_FIELDS";
    public static final String ACTION_TRAVEL_CUSTOM_FIELDS_UPDATED = "com.concur.mobile.action.TRAVEL_CUSTOM_FIELDS_UPDATED";
    public static final String ACTION_TRAVEL_REASON_CODES_UPDATED = "com.concur.mobile.action.REASON_CODES_UPDATED";
    public static final String ACTION_EXPENSE_TAX_FORM_FILTER = "com.concur.mobile.action.GET_TAX_FORM";

    // These constants identify the "source" of an expense report, i.e., either
    // "approval", "active", or "new".
    // The key below 'Const.EXPENSE_REPORT_SOURCE_KEY' is the key within an
    // intent that identifies the
    // source value.
    public static final int EXPENSE_REPORT_SOURCE_NEW = 0;
    public static final int EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL = 1;
    public static final int EXPENSE_REPORT_SOURCE_ACTIVE = 2;

    public static final String EXTRA_EXPENSE_REPORT_KEY = "expense.report.key";
    // This key determines the "source" of the active report, i.e., "approval"
    // or "active".
    public static final String EXTRA_EXPENSE_REPORT_SOURCE = "expense.report.source";
    // This key can be used to provide the action for displaying a mobile entry (Quick Expense).
    public static final String EXTRA_EXPENSE_MOBILE_ENTRY_ACTION = "expense.mobile.entry.action";
    // This key is used to force the updating of a detailed expense report
    // object.
    public static final String EXTRA_EXPENSE_REPORT_DETAIL_UPDATE = "expense.report.detail.update";
    public static final String EXTRA_EXPENSE_REPORT_ENTRY_KEY = "expense.report.entry.key";
    public static final String EXTRA_EXPENSE_REPORT_FORM_FIELDS = "expense.report.detail.form.fields";
    public static final String EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY = "expense.parent.report.entry.key";
    public static final String EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING = "expense.report.to.approve.list.pending";
    public static final String EXTRA_EXPENSE_ACTIVE_REPORT_LIST_PENDING = "expense.active.report.list.pending";
    public static final String EXTRA_EXPENSE_LOCAL_KEY = "expense.local.key";
    public static final String EXTRA_EXPENSE_ENTRY_TYPE_KEY = "expense.entry.type.key";
    public static final String EXTRA_EXPENSE_MOBILE_ENTRY_KEY = "expense.mobile.entry.key";
    public static final String EXTRA_EXPENSE_MOBILE_ENTRY_PERSONAL_CARD_ACCOUNT_KEY = "expense.pca.entry.key";
    public static final String EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY = "expense.pct.entry.key";
    public static final String EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY = "expense.cct.entry.key";
    public static final String EXTRA_EXPENSE_RECEIPT_CAPTURE_KEY = "expense.rc.entry.key";
    public static final String EXTRA_EXPENSE_E_RECEIPT_KEY = "expense.ereceipt.entry.key";
    public static final String EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_FILTER_KEY = "expense.cct.trans.filter";
    public static final String EXTRA_EXPENSE_RECEIPT_URL_KEY = "expense.receipt.url";
    public static final String EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY = "expense.receipt.image.id.key";
    public static final String EXTRA_EXPENSE_FROM_RECEIPT_IMAGE_ID_KEY = "expense.from.receipt.image.id.key";
    public static final String EXTRA_EXPENSE_TO_RECEIPT_IMAGE_ID_KEY = "expense.to.receipt.image.id.key";
    public static final String EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY = "expense.receipt.image.url.key";
    public static final String EXTRA_EXPENSE_EXCHANGE_RATE_KEY = "expense.exchange.rate.key";
    public static final String EXTRA_EXPENSE_CURRENCY_SEARCH_RESULTS = "expense.currency.search.results";
    public static final String EXTRA_EXPENSE_SCREEN_TITLE_KEY = "expense.screen.title";
    public static final String EXTRA_EXPENSE_SELECT_REPORT_RECEIPT_KEY = "expense.select.report.receipt";
    public static final String EXTRA_EXPENSE_SELECT_ENTRY_RECEIPT_KEY = "expense.select.entry.receipt";
    public static final String EXTRA_EXPENSE_SELECT_QUICK_EXPENSE_RECEIPT_KEY = "expense.select.expense.receipt";
    public static final String EXTRA_EXPENSE_FILE_PATH = "expense.file.path";
    public static final String EXTRA_EXPENSE_REPORT_NAME = "expense.report.name";
    public static final String EXTRA_EXPENSE_EXPENSE_NAME = "expense.name";
    public static final String EXTRA_EXPENSE_EXPENSE_AMOUNT = "expense.amount";
    public static final String EXTRA_EXPENSE_TYPE_HAS_TAX_FORM = "expense.type.has.tax.form";
    public static final String EXTRA_EXPENSE_EXPENSE_ID_KEY = "expense.id.key";
    public static final String EXTRA_E_RECEIPT_EXPENSE = "e_receipt_expense";
    public static final String EXTRA_EXPENSE_TRANSACTION_DATE_KEY = "expense.receipt.transaction.date.key";

    public static final String EXTRA_EXPENSE_ATTENDEE_KEY = "expense.attendee.key";
    public static final String EXTRA_EXPENSE_ATTENDEE_TYPE_KEY = "expense.attendee.type.key";
    public static final String EXTRA_EXPENSE_ATTENDEE_FIRST_NAME = "expense.attendee.first.name";
    public static final String EXTRA_EXPENSE_ATTENDEE_LAST_NAME = "expense.attendee.last.name";
    public static final String EXTRA_EXPENSE_ATTENDEE_DISPLAY_NAME = "expense.attendee.display.name";
    public static final String EXTRA_EXPENSE_ATTENDEE_COMPANY = "expense.attendee.company";
    public static final String EXTRA_EXPENSE_ATTENDEE_TITLE = "expense.attendee.title";
    public static final String EXTRA_EXPENSE_ATTENDEE_AMOUNT = "expense.attendee.amount";
    public static final String EXTRA_EXPENSE_ATTENDEE_COUNT = "expense.attendee.count";
    public static final String EXTRA_EXPENSE_TRANSACTION_CURRENCY = "expense.transaction.currency";
    public static final String EXTRA_EXPENSE_TRANSACTION_AMOUNT = "expense.transaction.amount";
    public static final String EXTRA_EXPENSE_DISTANCE_TO_DATE = "expense.distance.to.date";
    public static final String EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE = "expense.delete.external.receipt.file";
    public static final String EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER = "expense.report.default.approver";
    public static final String EXTRA_EXPENSE_REPORT_SELECTED_APPROVER = "expense.report.selected.approver";
    public static final String EXTRA_EXPENSE_ATTENDEE_TYPE_OLD_KEY_FROM_DUP = "expense.attendee.type.key.from.duplicate";

    public static final String EXTRA_CHECK_PROMPT_TO_RATE = "check.prompt.to.rate";

    public static final String EXTRA_PROMPT_FOR_ADD = "prompt.for.add";

    public static final String EXTRA_APP_RESTART = "app.restart";
    public static final String EXTRA_EXPENSE_REFRESH_HEADER = "expense.refresh.header";
    public static final String EXTRA_RECEIPT_ONLY_FRAGMENT = "receipt.only.fragment ";
    public static final String EXTRA_SHOW_MENU = "show.menu.on.view.image";
    public static final String EXTRA_PICK_RECEIPT_FROM_EXPENSE = "from.expense.on.view.image";

    public static final String REPLY_STATUS = "reply.status";
    public static final String REPLY_BODY = "reply.body";
    public static final String REPLY_ERROR_MESSAGE = "reply.error";
    public static final String REPLY_STATUS_SUCCESS = "success";
    public static final String REPLY_STATUS_SUCCESS_SMARTEXP = "success_smartexp";
    public static final String REPLY_STATUS_OK = "ok";
    public static final String REPLY_STATUS_NOT_FOUND = "not_found";
    public static final String REPLY_STATUS_FAIL = "fail";
    public static final String REPLY_STATUS_FAIL_CONNECTOR_UNAUTHORIZED = "app.connector.unauthorized";
    public static final String REPLY_STATUS_FAILURE = "failure";
    public static final String REPLY_STATUS_COUNT = "reply.status.count";
    public static final String REPLY_STATUS_ENTRY_KEY = "reply.status.entry.key";
    public static final String REPLY_STATUS_REVIEW_APPROVAL_FLOW_APPROVER = "review_approval_flow_approver";
    public static final String REPLY_STATUS_NO_APPROVER = "no_approver";
    public static final String REPLY_HTTP_STATUS_CODE = "reply.http.status.code";
    public static final String REPLY_HTTP_STATUS_TEXT = "reply.http.status.text";
    public static final String REPLY_MOBILE_ENTRY_LIST = "reply.me.list";
    public static final String REPLY_PERSONAL_CARD_TRANSACTION_LIST = "reply.pct.list";
    public static final String REPLY_CORPORATE_CARD_TRANSACTION_LIST = "reply.cct.list";
    public static final String REPLY_RECEIPT_CAPTURE_LIST = "reply.rc.list";
    public static final String SERVICE_REQUEST_STATUS = "service.request.status";
    public static final String SERVICE_REQUEST_STATUS_TEXT = "service.request.status.text";
    public static final String SERVICE_REQUEST_MESSAGE_ID = "service.request.message.id";
    public static final String REPLY_STATUS_GC_RESPONSE_ID_KEY = "reply.status.gc.response.id.key";
    public static final String REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE = "Imaging Configuration Not Available.";
    public static final int SERVICE_REQUEST_STATUS_OKAY = 1;
    public static final int SERVICE_REQUEST_STATUS_INVALID_REQUEST = 2;
    public static final int SERVICE_REQUEST_STATUS_IO_ERROR = 3;
    public static final int REPLY_HTTP_DEFAULT_STATUS_CODE = -1;

    // Intent related Expense related constants
    public static final String ACTION_EXPENSE_APPROVAL_REPORTS_UPDATED = "com.concur.mobile.action.EXPENSE_APPROVAL_REPORTS_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_DETAIL_UPDATED = "com.concur.mobile.action.EXPENSE_REPORT_DETAIL_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_ENTRY_DETAIL_UPDATED = "com.concur.mobile.action.EXPENSE_REPORT_ENTRY_DETAIL_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_HEADER_DETAIL_UPDATED = "com.concur.mobile.action.EXPENSE_REPORT_HEADER_DETAIL_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_ENTRY_FORM_UPDATED = "com.concur.mobile.action.EXPENSE_REPORT_ENTRY_FORM_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_UPDATED = "com.concur.mobile.action.EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_UPDATED";
    public static final String ACTION_EXPENSE_TYPE_UPDATED = "com.concur.mobile.action.EXPENSE_TYPE_UPDATED";
    public static final String ACTION_EXPENSE_MOBILE_ENTRY_UPDATED = "com.concur.mobile.action.EXPENSE_MOBILE_ENTRY_UPDATED";
    public static final String ACTION_EXPENSE_MOBILE_ENTRY_SAVED = "com.concur.mobile.action.EXPENSE_MOBILE_ENTRY_SAVED";
    public static final String ACTION_EXPENSE_MOBILE_ENTRIES_DELETED = "com.concur.mobile.action.EXPENSE_MOBILE_ENTRIES_DELETED";
    public static final String ACTION_CURRENCY_TYPE_UPDATED = "com.concur.mobile.action.CURRENCY_TYPE_UPDATED";
    public static final String ACTION_EXPENSE_RECEIPT_DOWNLOADED = "com.concur.mobile.action.RECEIPT_DOWNLOADED";
    public static final String ACTION_EXPENSE_RECEIPT_UPLOADED = "com.concur.mobile.action.RECEIPT_UPLOADED";
    public static final String ACTION_EXPENSE_ACTIVE_REPORTS_UPDATED = "com.concur.mobile.action.ACTIVE_REPORTS_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_SUBMIT_UPDATE = "com.concur.mobile.mobile.action.EXPENSE_REPORT_SUBMIT_UPDATED";
    public static final String ACTION_EXPENSE_ADDED_TO_REPORT = "com.concur.mobile.action.ADDED_TO_REPORT";
    public static final String ACTION_EXPENSE_ALL_EXPENSE_UPDATED = "com.concur.mobile.action.ALL_EXPENSE_UPDATED";
    public static final String ACTION_EXPENSE_REPORT_SEND_BACK = "com.concur.mobile.action.EXPENSE_REPORT_SEND_BACK";
    public static final String ACTION_EXPENSE_REPORT_APPROVE = "com.concur.mobile.action.EXPENSE_REPORT_APPROVE";
    public static final String ACTION_EXPENSE_REMOVE_REPORT_EXPENSE = "com.concur.mobile.action.EXPENSE_REMOVE_REPORT_EXPENSE";
    public static final String ACTION_EXPENSE_RECEIPT_SAVE = "com.concur.mobile.action.EXPENSE_RECEIPT_SAVED";
    public static final String ACTION_EXPENSE_REPORT_ENTRY_SAVE = "com.concur.mobile.action.EXPENSE_REPORT_ENTRY_SAVED";
    public static final String ACTION_EXPENSE_REPORT_ENTRY_RECEIPT_SAVE = "com.concur.mobile.action.EXPENSE_REPORT_ENTRY_RECEIPT_SAVED";
    public static final String ACTION_EXPENSE_REPORT_ENTRY_RECEIPT_CLEAR = "com.concur.mobile.action.EXPENSE_REPORT_ENTRY_RECEIPT_CLEARED";
    public static final String ACTION_EXPENSE_REPORT_SAVE = "com.concur.mobile.action.EXPENSE_REPORT_SAVED";
    public static final String ACTION_EXPENSE_REPORT_FORM_DOWNLOADED = "com.concur.mobile.action.EXPENSE_REPORT_FORM_DOWNLOADED";
    public static final String ACTION_EXPENSE_RECEIPT_IMAGE_URL_DOWNLOADED = "com.concur.mobile.action.EXPENSE_RECEIPT_IMAGE_URL_DOWNLOAD";
    public static final String ACTION_EXPENSE_ADD_REPORT_RECEIPT = "com.concur.mobile.action.EXPENSE_ADD_REPORT_RECEIPT";
    public static final String ACTION_EXPENSE_RECEIPT_IMAGE_URLS_DOWNLOADED = "com.concur.mobile.action.EXPENSE_RECEIPT_IMAGE_URLS_DOWNLOAD";
    public static final String ACTION_EXPENSE_RECEIPT_DELETED = "com.concur.mobile.action.EXPENSE_RECEIPT_DELETED";
    public static final String ACTION_EXPENSE_RECEIPT_APPENDED = "com.concur.mobile.action.EXPENSE_RECEIPT_APPENDED";
    public static final String ACTION_EXPENSE_EXPENSE_TYPES_DOWNLOADED = "com.concur.mobile.action.EXPENSE_EXPENSE_TYPES_DOWNLOADED";
    public static final String ACTION_EXPENSE_SEARCH_LIST_UPDATED = "com.concur.mobile.action.EXPENSE_SEARCH_LIST_UPDATED";
    public static final String ACTION_EXPENSE_ATTENDEE_SEARCH_UPDATED = "com.concur.mobile.action.EXPENSE_ATTENDEE_SEARCH_UPDATED";
    public static final String ACTION_EXPENSE_EXTENDED_ATTENDEE_SEARCH_UPDATED = "com.concur.mobile.action.EXPENSE_EXTENDED_ATTENDEE_SEARCH_UPDATED";
    public static final String ACTION_EXPENSE_APPROVER_SEARCH_UPDATED = "com.concur.mobile.action.EXPENSE_APPROVER_SEARCH_UPDATED";
    public static final String ACTION_EXPENSE_ATTENDEE_SAVE = "com.concur.mobile.action.EXPENSE_ATTENDEE_SAVE";
    public static final String ACTION_EXPENSE_CAR_CONFIGS_UPDATED = "com.concur.mobile.action.EXPENSE_CAR_CONFIGS_UPDATED";
    public static final String ACTION_EXPENSE_EXCHANGE_RATE_UPDATED = "com.concur.mobile.action.EXPENSE_EXCHANGE_RATE_UPDATED";
    public static final String ACTION_EXPENSE_CURRENCY_SEARCH_UPDATED = "com.concur.mobile.action.ACTION_EXPENSE_CURRENCY_SEARCH_UPDATED";
    public static final String ACTION_EXPENSE_HOTEL_ITEMIZED = "com.concur.mobile.action.EXPENSE_HOTEL_ITEMIZED";
    public static final String ACTION_EXPENSE_RETRIEVE_URL = "com.concur.mobile.action.EXPENSE_RETRIEVE_URL";
    public static final String ACTION_EXPENSE_DISTANCE_TO_DATE_RETRIEVED = "com.concur.mobile.action.EXPENSE_DISTANCE_TO_DATE_RETRIEVED";
    public static final String ACTION_EXPENSE_ATTENDEE_TYPES_DOWNLOADED = "com.concur.mobile.action.EXPENSE_ATTENDEE_TYPES_DOWNLOADED";
    public static final String ACTION_EXPENSE_ATTENDEE_SEARCH_FIELDS_DOWNLOADED = "com.concur.mobile.action.EXPENSE_ATTENDEE_SEARCH_FIELDS_DOWNLOADED";
    public static final String ACTION_EXPENSE_ATTENDEE_FORM_DOWNLOADED = "com.concur.mobile.action.EXPENSE_ATTENDEE_FORM_DOWNLOADED";
    public static final String ACTION_EXPENSE_DEFAULT_ATTENDEE_DOWNLOADED = "com.concur.mobile.action.EXPENSE_DEFAULT_ATTENDEE_DOWNLOADED";
    public static final String ACTION_EXPENSE_CONDITIONAL_FIELDS_DOWNLOADED = "com.concur.mobile.action.EXPENSE_CONDITIONAL_FIELDS_DOWNLOADED";

    // Configuration information related constants.
    public static final String ACTION_SYSTEM_CONFIG_UPDATE = "com.concur.mobile.action.SYSTEM_CONFIG_UPDATE";
    public static final String ACTION_USER_CONFIG_UPDATE = "com.concur.mobile.action.USER_CONFIG_UPDATE";

    // Broadcast action/extra related to on-going network activity.
    public static final String ACTION_NETWORK_ACTIVITY_START = "com.concur.mobile.action.network.activity.start";
    public static final String ACTION_NETWORK_ACTIVITY_STOP = "com.concur.mobile.action.network.activity.stop";
    public static final String ACTION_NETWORK_ACTIVITY_TYPE = "com.concur.mobile.action.network.activity.type";
    public static final String ACTION_NETWORK_ACTIVITY_TEXT = "com.concur.mobile.action.network.activity.text";

    /**
     * Sticky broadcast intent action indicating the (server) system is unavailable for some reason.
     */
    public static final String ACTION_NETWORK_SYSTEM_UNAVAILABLE = "com.concur.mobile.action.network.system.unavailable";

    // Sticky broadcast intent action indicating service has been bound.
    // This is used for when the application is re-entered after the system has
    // shut it down.
    public static final String ACTION_CONCUR_SERVICE_BOUND = "com.concur.mobile.action.service.bound";
    public static final String ACTION_CONCUR_SERVICE_UNBOUND = "com.concur.mobile.action.service.unbound";

    public static final String ACTION_CONCUR_USER_CONFIG_AVAIL = "com.concur.mobile.action.user.config.available";
    public static final String ACTION_CONCUR_USER_CONFIG_UNAVAIL = "com.concur.mobile.action.user.config.unavailable";

    public static final String ACTION_CONCUR_SYS_CONFIG_AVAIL = "com.concur.mobile.action.sys.config.available";
    public static final String ACTION_CONCUR_SYS_CONFIG_UNAVAIL = "com.concur.mobile.action.sys.config.unavailable";

    // Indicates the database has been reset.
    public static final String ACTION_DATABASE_RESET = "com.concur.mobile.action.database.reset";

    // Simple constant containing the directory name to contain receipts.
    public static final String RECEIPT_DIRECTORY = "receipts";
    public static final String DEFAULT_RECEIPT_IMAGE_FILE_NAME = "receipt";

    // Extra Intent Values Affecting Expense List Search
    public static final String EXTRA_EXPENSE_LIST_SEARCH_IS_MRU = "expense.list.search.is.mru";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID = "expense.list.search.field.id";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_FT_CODE = "expense.list.search.ft.code";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY = "expense.list.search.list.key";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY = "expense.list.search.parent.li.key";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY = "expense.list.search.report.key";
    public static final String EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY = "expense.list.search.selected.list.item.key";
    public static final String EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE = "expense.list.search.selected.list.item.code";
    public static final String EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_TEXT = "expense.list.search.selected.list.item.text";
    public static final String EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CRN_CODE = "expense.list.search.selected.list.item.crn.code";
    public static final String EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CRN_KEY = "expense.list.search.selected.list.item.crn.key";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_TITLE = "expense.list.search.title";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS = "expense.list.search.exclude.keys";
    public static final String EXTRA_EXPENSE_LIST_SHOW_CODES = "expense.list.search.show.codes";
    public static final String EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST = "expense.list.search.static.list";

    public static final String EXTRA_EXPENSE_ATTENDEE_SEARCH_TITLE = "expense.attendee.search.title";
    public static final String EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_KEYS = "expense.attendee.search.exclude.keys";
    public static final String EXTRA_EXPENSE_ATTENDEE_SEARCH_EXCLUDE_EXTERNAL_IDS = "expense.attendee.search.exclude.external.ids";
    public static final String EXTRA_EXPENSE_IMAGE_FILE_PATH = "expense.image.file.path";
    public static final String EXTRA_EXPENSE_IT_RECEIPT_ID = "expense.receipt.image.id";

    public static final String EXTRA_SEARCH_MODE = "search.mode";
    public static final String EXTRA_SEARCH_LOC_FROM = "search.loc.from";
    public static final String EXTRA_SEARCH_LOC_TO = "search.loc.to";
    public static final String EXTRA_SEARCH_DT_DEPART = "search.dt.depart";
    public static final String EXTRA_SEARCH_DT_RETURN = "search.dt.return";
    public static final String EXTRA_SEARCH_CABIN_CLASS = "search.cabin.class";
    public static final String EXTRA_SEARCH_REFUNDABLE_ONLY = "search.refundable.only";

    public static final String EXTRA_SEARCH_SELECTED_ITEM = "search.selected.item";

    // Default cabin class.
    public static final String AIR_SEARCH_DEFAULT_CABIN_CLASS = "Y";

    // Vendor codes
    public static final String VENDOR_AMTRAK = "2V";

    // Booking Source
    public static final String RAIL_BOOKING_SOURCE_AMTRAK = "Amtrak";

    // Carrier codes
    public static final String VENDOR_SOUTHWEST = "WN";

    // Expense codes
    public static final String EXPENSE_CODE_PERSONAL_MILEAGE = "PCARMILE";
    public static final String EXPENSE_CODE_COMPANY_MILEAGE = "COCARMILE";

    // Expense types
    public static final String EXPENSE_TYPE_MILEAGE = "MILEG";
    public static final String EXPENSE_TYPE_COMPANY_CAR_MILEAGE = "CARMI";
    public static final String EXPENSE_TYPE_COMPANY_MILEAGE = "CARMI";
    public static final String EXPENSE_TYPE_ROOM_RATE = "LODNG";

    // Expense type itemization values.
    public static final String ITEMIZATION_TYPE_NOT_ALLOWED = "NALW";
    public static final String ITEMIZATION_TYPE_OPTIONAL = "NREQ";
    public static final String ITEMIZATION_TYPE_REQUIRED = "REQD";

    // Report Editing URI options
    public static final String COPY_DOWN_TO_CHILD_FORMS = "CopyDownToChildForms";

    // Contains the expiration time in minutes when cache data will be refreshed
    // from
    // the server.
    public static final long CACHE_DATA_EXPIRATION_DURATION_MILLISECONDS = (5 * 60 * 1000L);

    // Contains the report entry threshold for either retrieving a report detail
    // summary (detail header + summary entries)
    // versus a full detailed report.
    public static final int REPORT_DETAIL_SUMMARY_ENTRY_COUNT_THRESHOLD = 10;

    // Contains the expiration time in milliseconds for a Receipt Store receipt
    // image URL.
    public static final long RECEIPT_STORE_RECEIPT_IMAGE_URL_EXPIRATION_MILLISECONDS = (25 * 60 * 1000L);

    // Contains the default attendee type code used to retrieve an attendee
    // editing form for a new attendee.
    public static final String DEFAULT_ATTENDEE_TYPE_CODE = "BUSGUEST";

    // Contains the default attendee type code used to represent the current
    // mobile user.
    public static final String SYSTEM_EMPLOYEE_ATTENDEE_TYPE_CODE = "SYSEMP";

    // Credit card uses
    public static final String CC_USE_AIR = "Air";
    public static final String CC_USE_CAR = "Car";
    public static final String CC_USE_HOTEL = "Hotel";
    public static final String CC_USE_RAIL = "Rail";

    // Airline seat classes.
    public static final String AIR_SEAT_CLASS_ECONOMY = "Y";
    public static final String AIR_SEAT_CLASS_PREMIUM_ECONOMY = "W";
    public static final String AIR_SEAT_CLASS_BUSINESS = "C";
    public static final String AIR_SEAT_CLASS_FIRST = "F";
    public static final String AIR_SEAT_CLASS_ANY = "";

    // Airline seat classes Value.
    public static final String AIR_SEAT_CLASS_VALUE_ECONOMY = "Economy";
    public static final String AIR_SEAT_CLASS_VALUE_PREMIUM_ECONOMY = "Premium economy";
    public static final String AIR_SEAT_CLASS_VALUE_BUSINESS = "Business";
    public static final String AIR_SEAT_CLASS_VALUE_FIRST = "First";
    public static final String AIR_SEAT_CLASS_VALUE_ANY = "Any";

    // Last Location fields.
    public static final int LOC_UPDATE_MIN_DISTANCE = 10000; // minimum distance
                                                             // in meters
    public static final long LOC_UPDATE_MIN_TIME = 30 * 60000; // minimum time
                                                               // in
                                                               // milliseconds

    // Profile status fields.
    public static final int TRAVEL_PROFILE_ALL_REQUIRED_DATA_PLUS_TSA = 0;
    public static final int TRAVEL_PROFILE_ALL_REQUIRED_DATA_MISSING_TSA = 1;
    public static final int TRAVEL_PROFILE_INCOMPLETE = 2;

    // Contains the expiration in milliseconds for using the last saved
    // transaction date and location.
    public static final long LAST_TRANS_DATE_LOCATION_EXPIRATION_MILLISECONDS = (8 * 60 * 60 * 1000L);

    // Trip states
    public static final int TRAVEL_TRIP_STATE_AWAITING_APPROVAL = 101;

    // Contains the number of hotels to retrieve each time from a cached result
    // set on the server.
    public static final int HOTEL_RETRIEVE_COUNT = 30;

    // Contains the symbolic user id identifying responses to SSO query
    // requests.
    public static final String SSO_USER_ID = "SSO";

    // Contains the value for 'ftcode' in a list search to indicate attendee
    // type retrieval is for search.
    public static final String ATTENDEE_SEARCH_LIST_FT_CODE = "ATNSEARCH";

    /**
     * MOB-11669 A 'flagged' phone is one that has a keyboard layout that doesn't always show the proper symbols for currency
     * input. <br>
     */
    public static final List<String> FLAGGED_LOCALE_KEYBOARD_LAYOUT = Arrays.asList(new String[] { "SAMSUNG" });

    // Push related consts
    public static final String PUSH_CONCUR_NOTIF_TYPE_FIELD = "type";
    public static final String PUSH_CONCUR_NOTIF_MESSAGE_FIELD = "message";
    public static final String PUSH_CONCUR_NOTIF_SUBJECT_FIELD = "subject";
    public static final String PUSH_CONCUR_NOTIF_TYPE_REPORT_APPR = "EXP_RPT_APPR";
    public static final String PUSH_CONCUR_NOTIF_TYPE_CREDIT_CARD = "EXP_CCT_TRXN";
    public static final String PUSH_CONCUR_NOTIF_TYPE_TRIP_APPR = "TRV_TRP_APPR";

    public static final String EXTRA_IS_FOR_TRIP_APPROVAL = "is_for_trip_approval";
    public static final String EXTRA_TRAVELLER_NAME = "traveller_name";
    public static final String EXTRA_TRAVELLER_USER_ID = "traveller_user_id";
    public static final String EXTRA_TRAVELLER_COMPANY_ID = "traveller_company_id";
    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_TRIP_NAME = "trip_name";
    public static final String EXTRA_TOTAL_TRIP_COST = "total_trip_cost";
    public static final String EXTRA_TRIP_APPROVAL_MESSAGE = "trip_approval_message";

    public static final String TRIP_APPROVAL_ACTION_REJECT = "reject";
    public static final String TRIP_APPROVAL_ACTION_APPROVE = "approve";

    public static final String EXTRA_SHOW_TRAVEL_AGENCY_BUTTON = "show.travel.agency.button";

    public static final String EXTRA_LOCATION_IATA = "location.iata";

    // Common Activity state names.
    public static final String ACTIVITY_STATE_UPTIME = "ACTIVITY_STATE_UPTIME";

    public static final String EXTRA_SEARCH_ROUND_TRIP = "search.round.trip";

    public static final String MWS_ERROR_MESSAGE = "mws.error.message";

    // Price to Beat
    public static final String EXTRA_HOTEL_PRICE_TO_BEAT_MONTH_OF_STAY = "hotel.price.to.beat.month.of.stay";
    public static final String EXTRA_HOTEL_PRICE_TO_BEAT_DIST_WITH_UNITS = "hotel.price.to.beat.dist.with.units";
    public static final String EXTRA_PRICE_TO_BEAT_HEADER_TXT = "price.to.beat.header.text";
    public static final String EXTRA_AIR_PRICE_TO_BEAT_ROUND_TRIP = "air.price.to.beat.round.trip";

    // File Search Activity
    public static final String EXTRA_FILEPATH = "file.search.activity.file.path";

}
