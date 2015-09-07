package com.concur.mobile.core.util;

/**
 * A class providing public constants for use with the Flurry event reporting system.
 *
 * @author andy
 */
public class Flurry {

    // Contains constants used to pass flurry category and an action param value through an intent.
    public static final String EXTRA_FLURRY_CATEGORY = "flurry.category";
    public static final String EXTRA_FLURRY_ACTION_PARAM_VALUE = "flurry.action.param.value";

    // Contains the Flurry category/event separator string.
    public static final String CATEGORY_EVENT_NAME_SEPARATOR = ": ";

    // Flurry event category names.
    public static final String CATEGORY_ANDROID = "Android";
    public static final String CATEGORY_ATTENDEE = "Attendee";
    public static final String CATEGORY_BOOK = "Book";
    public static final String CATEGORY_CAR_MILEAGE = "Car Mileage";
    public static final String CATEGORY_DELETE = "Delete";
    public static final String CATEGORY_EXTERNAL_APP = "External App";
    public static final String CATEGORY_HOME = "Home";
    public static final String CATEGORY_ITIN = "Itin";
    public static final String CATEGORY_LNA = "LnA";
    public static final String CATEGORY_MOBILE_ENTRY = "Mobile Entry";
    public static final String CATEGORY_RECEIPTS = "Receipts";
    public static final String CATEGORY_REPORT_ENTRY = "Report Entry";
    public static final String CATEGORY_REPORTS = "Reports";
    public static final String CATEGORY_SETTINGS = "Settings";
    public static final String CATEGORY_TRIP_APPROVAL = "Trip Approval";
    public static final String CATEGORY_USER = "User";
    public static final String CATEGORY_VOICE_BOOK = "Voice Book";
    public static final String CATEGORY_HOME_MORE = "Home More";
    public static final String CATEGORY_APPROVALS = "Approvals";
    public static final String CATEGORY_TRAVEL_AGENCY = "Travel Agency";
    public static final String CATEGORY_TRAVEL_REQUEST = "Travel Request";
    public static final String CATEGORY_PUSH_NOTIFICATION = "Push Notification";
    public static final String CATEGORY_EMAIL_NOTIFICATION = "Email Notification";
    public static final String CATEGORY_QUICK_EXPENSE = "QuickExpense";
    public static final String CATEGORY_MESSAGE_CENTER = "Message Center";
    public static final String CATEGORY_HOTEL_RECOMMENDATIONS = "Hotel Recommendations";
    public static final String CATEGORY_START_UP = "Start Up";
    public static final String CATEGORY_TEST_DRIVE_REGISTRATION = "Test Drive Registration";
    public static final String CATEGORY_TEST_DRIVE = "Test Drive";
    public static final String CATEGORY_OVERLAYS = "Overlays";
    public static final String CATEGORY_PRICE_TO_BEAT = "Price-to-Beat";
    public static final String CATEGORY_RECEIPT_CHOICE = "Receipt choice";
    public static final String CATEGORY_ALL_MOBILE_EXPENSES = "All Mobile Expenses";
    public static final String CATEGORY_MAIN_MENU = "Main Menu";

    // ################### NEW GOOGLE ANALTYICS LABELS ################# //

    public static final String LABEL_ALL_EXPENSES = "All Expenses";
    public static final String LABEL_SMARTMATCHED_EXPENSE = "SmartMatched Expense";
    public static final String LABEL_SMARTMATCHED_EXPENSE_ERECEIPT = "SmartMatched Expense E-Receipt";
    public static final String LABEL_CORPORATE_CARD_EXPENSE = "Corporate Card Expense";
    public static final String LABEL_QUICK_EXPENSE_DETAIL = "Quick Expense Detail";
    public static final String LABEL_REPORT_ENTRY_DETAIL = "Report Entry Detail";

    // ####################### END GA LABELS ########################## //

    // ##################### NEW GOOLGE ANALTYIC ACTIONS ############## //

    public static final String ACTION_EXPENSE_LIST = "Expense List";
    public static final String ACTION_RECEIPT_DETAILS = "View Receipt details";
    public static final String ACTION_E_RECEIPT_IMAGE_ERROR = "E-Receipt Image Error";
    public static final String ACTION_APP_CENTER = "App Center";

    // ####################### END GA ACTIONS ###################### //

    // ///////////////////////////////////////////////
    // BEGIN New event names
    // ///////////////////////////////////////////////
    public static final String EVENT_NAME_FAILURE = "Failed Attempt";
    public static final String EVENT_NAME_SUCCESS = "Successful Attempt";
    public static final String EVENT_NAME_OVERALL = "Overall";
    public static final String EVENT_NAME_REQUEST_PIN_RESET = "Request Reset";
    public static final String EVENT_NAME_RESET_PIN_ATTEMPT = "Reset Pin Attempt";
    public static final String EVENT_NAME_RESET_PIN_SUCCESS = "Reset Attempt Success";
    public static final String EVENT_NAME_RESET_PIN_FAILURE = "Reset Attempt Failure";
    public static final String EVENT_NAME_BACK_BUTTON_CLICK = "Back Button Click";
    public static final String EVENT_NAME_SUBMIT_REGISTRATION = "Submit Registration";
    public static final String EVENT_NAME_SUBMIT_REGISTRATION_SUCCESS = "Submit Registration Success";
    public static final String EVENT_NAME_SUBMIT_REGISTRATION_FAILURE = "Submit Registration Failure";
    public static final String EVENT_ADD_EXPENSE = "Add Expense";
    public static final String EVENT_OFFLINE = "Offline";
    public static final String EVENT_FORMAT_ISSUE = "Format Issue";
    public static final String EVENT_OTHER_ERROR = "Other Error";
    public static final String EVENT_EMAIL_LOOKUP_FAILURE = "Email Lookup Failure";
    public static final String EVENT_REMOTE_WIPE = "Remote Wipe";
    public static final String EVENT_FORBIDDEN = "Forbidden";
    public static final String EVENT_SERVER_ERROR = "Server Error";
    public static final String EVENT_BAD_CREDENTIALS = "Bad Credentials";

    // ///////////////////////////////////////////////
    // END New event names
    // ///////////////////////////////////////////////

    // Flurry event names.
    public static final String EVENT_NAME_ADD = "Add";
    public static final String EVENT_NAME_AIR = "Air";
    public static final String EVENT_NAME_CANCEL = "Cancel";
    public static final String EVENT_NAME_CAR = "Car";
    public static final String EVENT_NAME_HOTEL = "Hotel";
    public static final String EVENT_NAME_TRAIN = "Train";
    public static final String EVENT_NAME_CREATE = "Create";
    public static final String EVENT_NAME_CONVERT_FAILED_OCR = "Convert Failed OCR";
    public static final String EVENT_NAME_ACTION = "Action";
    public static final String EVENT_NAME_VIEW_ITIN_SEGMENTS = "View Itin Segments";
    public static final String EVENT_NAME_VIEW_SEGMENT = "View Segment";
    public static final String EVENT_NAME_CHECK_IN = "Check In";
    public static final String EVENT_NAME_ADD_TO_REPORT = "Add to Report";
    public static final String EVENT_NAME_LIST = "List";
    public static final String EVENT_NAME_SAVED = "Saved";
    public static final String EVENT_NAME_AGE = "Age";
    public static final String EVENT_NAME_DELETE = "Delete";
    public static final String EVENT_NAME_QUEUE_VIEW = "Queue View";
    public static final String EVENT_NAME_UPLOAD = "Upload";
    public static final String EVENT_NAME_UPLOAD_CANCEL = "Upload Cancel";
    public static final String EVENT_NAME_USAGE = "Usage";
    public static final String EVENT_NAME_VIEWED = "Viewed";
    public static final String EVENT_NAME_VIEWED_FROM_QUEUE = "Viewed from Queue";
    public static final String EVENT_NAME_RECEIPT_STORE_VIEWED = "Receipt Store: Viewed";
    public static final String EVENT_NAME_ADD_RECEIPT_TO_REPORT_ENTRY = "Add Receipt To Report Entry";
    public static final String EVENT_NAME_ADD_TO_MOBILE_ENTRY = "Add To Mobile Entry";
    public static final String EVENT_NAME_APPEND = "Append";
    public static final String EVENT_NAME_RECEIPT_STORE_CREATE = "Receipt Store: Create";
    public static final String EVENT_NAME_ITEMIZE_ENTRY = "Itemize Entry";
    public static final String EVENT_NAME_ITEMIZE_HOTEL_ENTRY = "Itemize Hotel Entry";
    public static final String EVENT_NAME_ITEMIZED_ENTRY_LIST = "Itemized Entry List";
    public static final String EVENT_NAME_APPROVE_REPORT = "Approve Report";
    public static final String EVENT_NAME_SUBMIT = "Submit";
    public static final String EVENT_NAME_AUTHENTICATION = "Credential Type";
    public static final String EVENT_NAME_AUTHENTICATION_STATISTICS = "Authentication Statistics";
    public static final String EVENT_NAME_RESET_PASSWORD = "Reset Password";
    public static final String EVENT_NAME_APPROVE_TRIP = "Approve Trip";
    public static final String EVENT_NAME_COMPLETED_HOTEL = "Completed Hotel";
    public static final String EVENT_NAME_COMPLETED_AIR = "Completed Air";
    public static final String EVENT_NAME_COMPLETED_CAR = "Completed Car";
    public static final String EVENT_NAME_ERROR_HOTEL = "Error Hotel";
    public static final String EVENT_NAME_USAGE_SUCCESS = "Usage Success";
    public static final String EVENT_NAME_USAGE_CANCELLED = "Usage Cancelled";
    public static final String EVENT_NAME_LAUNCH = "Launch";
    public static final String EVENT_NAME_RESERVE = "Reserve";
    public static final String EVENT_NAME_SHARE = "Share";
    public static final String EVENT_NAME_REJECT_REPORT = "Reject Report";
    public static final String EVENT_NAME_TYPE = "Type";
    public static final String EVENT_NAME_PHONED_TRAVEL_AGENT = "Phoned Travel Agent";
    public static final String EVENT_NAME_VIEWED_TRIP_RULE_VIOLATION_SUMMARY = "Viewed Trip Rule Violation Summary";
    public static final String EVENT_INITIAL_SELECTION_MATCH_ON = "Initial Selection Match On";
    public static final String EVENT_INITIAL_SELECTION_MATCH_OFF = "Initial Selection Match Off";
    public static final String EVENT_NAME_HOTEL_SEARCH_STREAMING_ON = "Book: Hotel Streaming On";
    public static final String EVENT_NAME_HOTEL_SEARCH_STREAMING_OFF = "Book: Hotel Streaming Off";
    public static final String EVENT_NAME_HOTEL_RECOMMENDATIONS_RATES_VIEWED = "Hotel Recommendations: Hotel Rates Viewed";
    public static final String EVENT_NAME_HOTEL_RECOMMENDATIONS_HOTEL_RESERVED = "Hotel Recommendations: Hotel Reserved";
    public static final String EVENT_NAME_VIEWED_PRICE_TO_BEAT_MENU = "Price-to-Beat Menu Viewed";
    public static final String EVENT_NAME_VIEWED_AIR_PRICE_TO_BEAT_SEARCH = "Air Price-to-Beat Search Viewed";
    public static final String EVENT_NAME_VIEWED_AIR_PRICE_TO_BEAT_RESULTS = "Air Price-to-Beat Results Viewed";
    public static final String EVENT_NAME_VIEWED_AIR_PRICE_TO_BEAT_RESULTS_NOT_FOUND = "Air Price-to-Beat Results Not Found";
    public static final String EVENT_NAME_VIEWED_HOTEL_PRICE_TO_BEAT_SEARCH = "Hotel Price-to-Beat Search Viewed";
    public static final String EVENT_NAME_VIEWED_HOTEL_PRICE_TO_BEAT_RESULTS = "Hotel Price-to-Beat Results Viewed";
    public static final String EVENT_NAME_VIEWED_HOTEL_PRICE_TO_BEAT_RESULTS_NOT_FOUND = "Hotel Price-to-Beat Results Not Found";
    public static final String EVENT_NAME_VIEWED_PRICE_TO_BEAT_RANGE = "Price-to-Beat Range Viewed";
    public static final String EVENT_NAME_VIEWED_MANAGE_VIOLATIONS = "Manage Violations Viewed";
    public static final String EVENT_NAME_HOTEL_RESERVE = "Hotel Reserve";
    public static final String EVENT_NAME_AIR_RESERVE = "Air Reserve";

    // Flurry parameter names.
    public static final String PARAM_NAME_VIA = "Via";
    public static final String PARAM_NAME_ATTENDEE_COUNT = "Attendee Count";
    public static final String PARAM_NAME_FARE_PRICE = "Fare Price";
    public static final String PARAM_NAME_CURRENCY = "Currency";
    public static final String PARAM_NAME_FROM = "From";
    public static final String PARAM_NAME_TO = "To";
    public static final String PARAM_NAME_NUM_LEGS = "Num Legs";
    public static final String PARAM_NAME_IN_POLICY = "In Policy";
    public static final String PARAM_NAME_TYPE = "Type";
    public static final String PARAM_NAME_ITEMS_LEFT_IN_ITIN = "Items Left In Itin";
    public static final String PARAM_NAME_VENDOR = "Vendor";
    public static final String PARAM_NAME_CAR_TYPE = "Car Type";
    public static final String PARAM_NAME_LOCATION = "Location";
    public static final String PARAM_NAME_VIOLATION_COUNT = "Violation Count";
    public static final String PARAM_NAME_ROOM_TYPE = "Room Type";
    public static final String PARAM_NAME_SORT_ORDER = "Sort Order";
    public static final String PARAM_NAME_NEAREST_SEGMENT_TO_NOW_IN_HOURS = "Nearest Segment to now in hours";
    public static final String PARAM_NAME_CAME_FROM = "Came From";
    public static final String PARAM_NAME_HOW_MANY_ADDED = "How Many Added";
    public static final String PARAM_NAME_HAS_CREDIT_CARD = "Has Credit Card";
    public static final String PARAM_NAME_HAS_RECEIPT = "Has Receipt";
    public static final String PARAM_NAME_CARD_COUNT = "Card Count";
    public static final String PARAM_NAME_MOBILE_ENTRY_COUNT = "Mobile Entry Count";
    public static final String PARAM_NAME_RECEIPT_COUNT = "Receipt Count";
    public static final String PARAM_NAME_EDIT_NEW = "Edit New";
    public static final String PARAM_NAME_CONTAINS_RECEIPT = "Contains Receipt";
    public static final String PARAM_NAME_AGE = "Age";
    public static final String PARAM_NAME_WAS_OFFLINE = "Was Offline";
    public static final String PARAM_NAME_QUEUE_COUNT = "Queue Count";
    public static final String PARAM_NAME_QUEUED_RECEIPTS = "Queued Receipts";
    public static final String PARAM_NAME_QUEUED_MOBILE_ENTRIES = "Queued Mobile Entries";
    public static final String PARAM_NAME_QUEUED_MOBILE_ENTRIES_WITH_RECEIPTS = "Queued Mobile Entries with Receipts";
    public static final String PARAM_NAME_TIME = "Time";
    public static final String PARAM_NAME_UPLOADED_COUNT = "Uploaded Count";
    public static final String PARAM_NAME_UPLOADED_IMAGE_COUNT = "Uploaded Image Count";
    public static final String PARAM_NAME_HOW_MANY_UPLOADED = "How many uploaded";
    public static final String PARAM_NAME_TIME_SPENT_UPLOADING = "Time spent uploading";
    public static final String PARAM_NAME_TIME_OFFLINE = "Time Offline";
    public static final String PARAM_NAME_VIEWED_FROM = "Viewed From";
    public static final String PARAM_NAME_ADDED_USING = "Added Using";
    public static final String PARAM_NAME_FAILURE = "Failure";
    public static final String PARAM_NAME_UNSUBMITTED_COUNT = "Unsubmitted Count";
    public static final String PARAM_NAME_SUBMITTED_PENDING_COUNT = "Submitted Pending Count";
    public static final String PARAM_NAME_SUCCESS = "Success";
    public static final String PARAM_NAME_NEW_VALUE = "New Value";
    public static final String PARAM_NAME_ERROR_TYPE = "Error Type";
    public static final String PARAM_NAME_WORKED = "Worked";
    public static final String PARAM_NAME_ACTION = "Action";
    public static final String PARAM_NAME_AUTHENTICATION_ENDPOINT = "Authentication Endpoint";
    public static final String PARAM_NAME_AUTHENTICATION_MILLISECONDS = "Authentication Milliseconds";
    public static final String PARAM_NAME_PARSING_MILLISECONDS = "Parsing Milliseconds";
    public static final String PARAM_NAME_CONFIGURATION_MILLISECONDS = "Configuration Milliseconds";
    public static final String PARAM_NAME_BOOKED_FROM = "Booked From";
    public static final String PARAM_NAME_SHARE_COUNT = "Share Count";
    public static final String PARAM_NAME_OFFLINE_CREATE = "Offline Create";
    public static final String PARAM_NAME_COUNT = "Count";
    public static final String PARAM_NAME_RESULT = "Result";
    public static final String PARAM_NAME_UPLOADED_USING = "Uploaded Using";
    public static final String PARAM_NAME_ADDED_TO = "Added To";
    public static final String PARAM_NAME_VIEW_EXPENSE = "QuickExpense: View Expense";
    public static final String PARAM_NAME_TOTAL_HOTEL_SEARCH_DURATION = "Book: Total Hotel Search Duration In Seconds";
    public static final String PARAM_NAME_RECOMMENDED = "Recommended";
    public static final String PARAM_NAME_SEARCH_HAD_RECOMMENDATIONS = "Search had recommendations";
    public static final String PARAM_NAME_PROPERTY_ID = "Property ID";
    public static final String PARAM_NAME_SECONDS_ON_OVERLAY = "Seconds on Overlay:";
    public static final String PARAM_NAME_TRAVEL_DEPARTURE_LOC = "Departure Location:";
    public static final String PARAM_NAME_TRAVEL_ARRIVAL_LOC = "Arrival Location:";
    public static final String PARAM_NAME_TRAVEL_DEPARTURE_DATE = "Departure Date:";
    public static final String PARAM_NAME_RADIUS = "Radius:";
    public static final String PARAM_NAME_SCALE = "Scale:";
    public static final String PARAM_NAME_MONTH = "Month of Stay:";
    public static final String PARAM_NAME_HOTEL_VIEWED = "Viewed Hotel To";
    public static final String PARAM_NAME_CHANGED_OPTIONS = "Changed Options To";
    public static final String PARAM_NAME_TRAVELLER_COMPANY = "User Company Name";
    public static final String PARAM_NAME_SELECTED_OPTION = "Selected Positive Points";
    public static final String PARAM_NAME_POINTS_USED = "Travel Points Used";
    public static final String PARAM_NAME_POINTS_EARNED = "Travel Points Earned";
    public static final String PARAM_NAME_TRAVEL_POINTS_TO_USE = "Travel Points To Use";
    public static final String PARAM_NAME_TRAVEL_POINTS_IN_BANK = "Travel Points In Bank";
    public static final String PARAM_NAME_USE_TRAVEL_POINTS = "Use Travel Points Selected";

    // Flurry parameter values.
    public static final String PARAM_VALUE_CONTACTS = "Contacts";
    public static final String PARAM_VALUE_QUICK_SEARCH = "Quick Search";
    public static final String PARAM_VALUE_ADVANCED_SEARCH = "Advanced Search";
    public static final String PARAM_VALUE_MANUAL = "Manual";
    public static final String PARAM_VALUE_AIR = "Air";
    public static final String PARAM_VALUE_HOTEL = "Hotel";
    public static final String PARAM_VALUE_CAR = "Car";
    public static final String PARAM_VALUE_TRAIN = "Train";
    public static final String PARAM_VALUE_DINING = "Dining";
    public static final String PARAM_VALUE_EVENT = "Event";
    public static final String PARAM_VALUE_PARKING = "Parking";
    public static final String PARAM_VALUE_RIDE = "Ride";
    public static final String PARAM_VALUE_PERSONAL = "Personal";
    public static final String PARAM_VALUE_COMPANY = "Company";
    public static final String PARAM_VALUE_REPORT = "Report";
    public static final String PARAM_VALUE_REPORT_ENTRY = "Report Entry";
    public static final String PARAM_VALUE_MOBILE_ENTRY = "Mobile Entry";
    public static final String PARAM_VALUE_CREDIT_CARD = "Credit Card";
    public static final String PARAM_VALUE_RECEIPT_STORE_RECEIPT = "Receipt Store Receipt";
    public static final String PARAM_VALUE_ITEMIZATION = "Itemization";
    public static final String PARAM_VALUE_TRIP_IT = "TripIt";
    public static final String PARAM_VALUE_TRAVEL_TEXT = "TravelText";
    public static final String PARAM_VALUE_BROWSER = "Browser";
    public static final String PARAM_VALUE_ABUKAI = "Abukai";
    public static final String PARAM_VALUE_CURB = "Curb";
    public static final String PARAM_VALUE_EXPENSE_IT = "ExpenseIt";
    public static final String PARAM_VALUE_ADD_CARD_FROM_HOME = "Add Card From Home";
    public static final String PARAM_VALUE_ADD_QUICK_RECEIPT = "Add Quick Receipt";
    public static final String PARAM_VALUE_BOOK_TRAVEL = "Book Travel";
    public static final String PARAM_VALUE_LINK_TO_TRIP_IT = "Link To TripIt";
    public static final String PARAM_VALUE_PERSONAL_CAR_MILEAGE = "Personal Car Mileage";
    public static final String PARAM_VALUE_QUICK_EXPENSE = "Quick Expense";
    public static final String PARAM_VALUE_OCR_EXPENSE = "OCR Expense";
    public static final String PARAM_VALUE_RECEIPT_STORE = "Receipt Store";
    public static final String PARAM_VALUE_REFRESH_DATA = "Refresh Data";
    public static final String PARAM_VALUE_LOCATION_CHECK_IN = "Location Checkin";
    public static final String PARAM_VALUE_TRAVEL_REQUEST = "Travel Request";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_CLICK = "Travel Request Click";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_LIST = "Travel Request List";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_LIST_RECALL_ACTION = "Travel Request List Recall Action";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_LIST_ROW_TAP_ACTION = "Travel Request List Row Tap Action";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_SUMMARY = "Travel Request Summary";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_LIST_PLUS_BUTTON = "Travel Request List Plus Button";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_HEADER = "Travel Request Header";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_ENTRY = "Travel Request Entry";
    public static final String PARAM_VALUE_TRAVEL_REQUEST_LOCATION = "Travel Request Location";
    public static final String PARAM_VALUE_VIEW_EXPENSE_LIST = "View Expense List";
    public static final String PARAM_VALUE_VIEW_CURRENT_TRIP = "View Current Trip";
    public static final String PARAM_VALUE_VIEW_INVOICES = "View Invoices";
    public static final String PARAM_VALUE_VIEW_REPORT_APPROVAL = "View Report Approval";
    public static final String PARAM_VALUE_VIEW_REPORTS = "View Reports";
    public static final String PARAM_VALUE_VIEW_TRIPS = "View Trips";
    public static final String PARAM_VALUE_PUSH = "Push";
    public static final String PARAM_VALUE_EMAIL = "Email";
    public static final String PARAM_VALUE_URL = "URL";
    public static final String PARAM_VALUE_LNA_VIEW = "LnA View";
    public static final String PARAM_VALUE_EXPENSE_LIST = "Expense List";
    public static final String PARAM_VALUE_REPORT_HEADER = "Report Header";
    public static final String PARAM_VALUE_YES = "Yes";
    public static final String PARAM_VALUE_NO = "No";
    public static final String PARAM_VALUE_NA = "NA";
    public static final String PARAM_VALUE_HOME = "Home";
    public static final String PARAM_VALUE_EDIT = "Edit";
    public static final String PARAM_VALUE_NEW = "New";
    public static final String PARAM_VALUE_RECEIPT = "Receipt";
    public static final String PARAM_VALUE_QUICK_EXPENSE_WITH_RECEIPT = "Quick Expense with Receipt";
    public static final String PARAM_VALUE_AIRPLANE_MODE = "Airplane Mode";
    public static final String PARAM_VALUE_NO_NETWORK = "No Network";
    public static final String PARAM_VALUE_INTERMITTENT = "Intermittent";
    public static final String PARAM_VALUE_REPORT_TO_APPROVE = "Report To Approve";
    public static final String PARAM_VALUE_TRIP_TO_APPROVE = "Trip To Approve";
    public static final String PARAM_VALUE_SETTINGS = "Settings";
    public static final String PARAM_VALUE_ITINERARIES = "Itineraries";
    public static final String PARAM_VALUE_SEGMENTS = "Segments";
    public static final String PARAM_VALUE_SEGMENT = "Segment";
    public static final String PARAM_VALUE_QUEUED_RECEIPT = "Queued Receipt";
    public static final String PARAM_VALUE_SELECT_RECEIPT = "Select Receipt";
    public static final String PARAM_VALUE_CAMERA = "Camera";
    public static final String PARAM_VALUE_ALBUM = "Album";
    public static final String PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE = "Failed to capture or reduce resolution for receipt image";
    public static final String PARAM_VALUE_FAILED_TO_SAVE_RECEIPT_IMAGE_ID_FOR_ENTRY = "Failed to save receipt image id for entry";
    public static final String PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE = "Failed to upload receipt image";
    public static final String PARAM_VALUE_CAR_MILEAGE = "Car Mileage";
    public static final String PARAM_VALUE_AUTO_LOGIN = "Auto Login";
    public static final String PARAM_VALUE_SAVE_USER_NAME = "Save User Name";
    public static final String PARAM_VALUE_PUSH_ALLOW = "Allow Notifications";
    public static final String PARAM_VALUE_PUSH_ALLOW_VIBRATE = "Allow Vibration on Push Notifications";
    public static final String PARAM_VALUE_SEND_LOG = "Send Log";
    public static final String PARAM_VALUE_CLEAR_CACHE = "Clear Cache";
    public static final String PARAM_VALUE_RESET = "Reset";
    public static final String PARAM_VALUE_LOGOUT = "Logout";
    public static final String PARAM_VALUE_CONNECTION = "Connection";
    public static final String PARAM_VALUE_OFFER_VALIDITY = "Offer Validity";
    public static final String PARAM_VALUE_SHOW_OFFERS = "Show Offers";
    public static final String PARAM_VALUE_VOICE_SEARCH_LANGUAGE = "Voice Search Language";
    public static final String PARAM_VALUE_SSO = "SSO";
    public static final String PARAM_VALUE_PASSWORD = "Password";
    public static final String PARAM_VALUE_PIN = "Pin";
    public static final String PARAM_VALUE_PIN_OR_PASSWORD = "Pin or Password";
    public static final String PARAM_VALUE_LOGIN_USING_PASSWORD = "Password";
    public static final String PARAM_VALUE_LOGIN_USING_MOBILE_PASSWORD = "Mobile Password";
    public static final String PARAM_VALUE_LOGIN_USING_SSO = "SSO";
    public static final String PARAM_VALUE_BREEZE = "Breeze";
    public static final String PARAM_VALUE_SMART_EXPENSE = "Smart Expense";
    public static final String PARAM_VALUE_EXPENSE_ONLY = "Expense Only";
    public static final String PARAM_VALUE_TRAVEL_ONLY = "Travel Only";
    public static final String PARAM_VALUE_CTE = "CTE";
    public static final String PARAM_VALUE_GOV = "Gov";
    public static final String PARAM_VALUE_SPEECH_RECOGNIZER = "Speech Recognizer";
    public static final String PARAM_VALUE_OTHER = "Other";
    public static final String PARAM_VALUE_TRIPS = "Trips";
    public static final String PARAM_VALUE_TRIP = "Trip";
    public static final String PARAM_VALUE_PROFILE = "Profile";

    public static final String PARAM_VALUE_PRICE = "Price";
    public static final String PARAM_VALUE_DISTANCE = "Distance";
    public static final String PARAM_VALUE_STAR_RATING = "Star Rating";
    public static final String PARAM_VALUE_VENDOR_NAME = "Vendor Name";
    public static final String PARAM_VALUE_PREFERRED_VENDOR = "Preferred Vendor";

    public static final String PARAM_VALUE_AD_LINK_TAPPED = "Ad Link Tapped";
    public static final String PARAM_VALUE_RECEIPT_SHARE_SERVICE = "Receipt Share Service";
    public static final String PARAM_VALUE_RECEIPT_SHARE = "Receipt Share";
    public static final String PARAM_VALUE_SHARE = "Share";

    public static final String PARAM_VALUE_MESSAGE_CENTER = "Message Center";
    public static final String PARAM_VALUE_VIEW_APPROVALS = "View Approvals";
    public static final String PARAM_VALUE_NOTIFICATION_APPROVALS = "EXP_RPT_APPR";
    public static final String PARAM_VALUE_OKAY = "Okay";
    public static final String PARAM_VALUE_CANCEL = "Cancel";
    public static final String PARAM_VALUE_VIEW_TRAVEL_REQUESTS = "View Travel Requests";
    public static final String PARAM_VALUE_VIEW_PURCHASE_REQUESTS = "View Purchase Requests";
    public static final String PARAM_VALUE_MWS = "MWS";
    public static final String PARAM_VALUE_CONNECT = "Connect";
    public static final String PARAM_VALUE_HOME_MORE = "Home More";
    public static final String PARAM_VALUE_TOUR = "Tour";

    public static final String PARAM_VALUE_PERSONAL_CARD = "Personal Card";
    public static final String PARAM_VALUE_CORPORATE_CARD = "Corporate Card";
    public static final String PARAM_VALUE_RECEIPT_CAPTURE = "Receipt Capture";
    public static final String PARAM_VALUE_E_RECEIPT = "E-Receipt";
    public static final String PARAM_VALUE_CASH = "Cash";

    public static final String PARAM_VALUE_HOTEL_SEARCH_STREAMING_FAILURE = "No Hotel Choices available in the final polling for pricing.";
    public static final String PARAM_VALUE_USE = "Use Points";
    public static final String PARAM_VALUE_EARN = "Earn Points";
    public static final String PARAM_VALUE_MANAGER_APPROVAL = "Manager Approval";
    public static final String PARAM_VALUE_UBER = "Uber";

    // SCREEN NAME FOR SIGN IN
    public static final String SCREEN_NAME_EMAIL_LOOKUP = "Sign In-Enter Email";
    public static final String SCREEN_NAME_LOGIN_PASSWORD = "Sign In-Enter Password";
    public static final String SCREEN_NAME_SSO = "Sign In-SSO Company Code";
    public static final String SCREEN_NAME_FORGOT_PASSWORD_REQUEST = "Sign In-Forgot Password-Enter Email";
    public static final String SCREEN_NAME_FORGOT_PASSWORD_RESET = "Sign In-Forgot Password-New Password";
    public static final String SCREEN_NAME_FORGOT_PIN_RESET = "Sign In-Forgot PIN-New PIN";
    public static final String SCREEN_NAME_EMAIL_PASSWORD = "Sign In-Enter Email Password";
    public static final String SCREEN_NAME_HOME = "Home";



    //Login Category, event action and param values
    public static final String CATEGORY_SIGN_IN = "Sign In";

    public static final String ACTION_SIGN_IN_SUCCESS_METHOD = "Success-Method";
    public static final String ACTION_SIGN_IN_FAIL_METHOD = "Fail-Method";
    public static final String ACTION_SUCCESS_CREDENTIAL_TYPE = "Success-Credential Type";
    public static final String ACTION_FAIL_CREDENTIAL_TYPE = "Fail-Credential Type";
    public static final String ACTION_FAIL_REASON = "Fail-Reason";

    public static final String LABEL_AUTO_LOGIN = "Auto Login";
    public static final String LABEL_MANUAL = "Manual";
    public static final String LABEL_LOGIN_USING_PASSWORD = "Password";
    public static final String LABEL_LOGIN_USING_MOBILE_PASSWORD = "Mobile Password";
    public static final String LABEL_LOGIN_USING_SSO = "SSO";
    public static final String LABEL_REMOTE_WIPE = "Remote Wipe";
    public static final String LABEL_FORBIDDEN = "Forbidden";
    public static final String LABEL_SERVER_ERROR = "Server Error";
    public static final String LABEL_BAD_CREDENTIALS = "Bad Credentials";
    public static final String LABEL_OFFLINE = "Offline";

    public static final String EVENT_CATEGORY_TRAVEL_HOTEL = "TRAVEL-HOTEL";
    public static final String EVENT_ACTION_TRAVEL_DESTINATION_TAPPED = "Destination Tapped";
    public static final String EVENT_ACTION_TRAVEL_SEARCH_INITIATED = "Search initiated";
    public static final String EVENT_LABEL_TRAVEL_SEARCH_CURRENT_LOCATION = "Current Location";
    public static final String EVENT_LABEL_TRAVEL_SEARCH_OFFICE_LOCATION = "Office Location";
    public static final String EVENT_LABEL_TRAVEL_SEARCH_OTHER = "Other";

    // Screen name for Travel Allowance
    public static final String SCREEN_NAME_TRAVEL_ALLOWANCE_MAIN = "Tab-View: Expense-Report-TravelAllowances";
    public static final String SCREEN_NAME_TRAVEL_ALLOWANCE_FIXED_DETAIL = "Allowance Details: Expense-Report-TravelAllowances-DailyAllowance";
    public static final String SCREEN_NAME_TRAVEL_ALLOWANCE_ITIN_UPDATE = "Itin-View (Create/Edit) Expense-Report-TravelAllowances-Itinerary";

    /**
     * Formats a Flurry event name based on a category and name.
     *
     * @param categoryName
     *            contains the Flurry event category name.
     * @param eventName
     *            contains the Flurry event name.
     * @return returns the formatted Flurry event name.
     */
    public static String formatFlurryEvent(String categoryName, String eventName) {
        StringBuilder strBldr = new StringBuilder();
        if (categoryName != null && categoryName.length() > 0) {
            strBldr.append(categoryName);
            strBldr.append(CATEGORY_EVENT_NAME_SEPARATOR);
        }
        strBldr.append(eventName);
        return strBldr.toString();
    }

    /**
     * Returns the duration time as a range between <code>0-3</code>, <code>3-10</code>, and <code>10+</code>.
     *
     * @param seconds
     *            the duration time in seconds
     * @return A string representing a time range.
     */
    public static String formatDurationEventParam(long seconds) {
        String duration;
        if (seconds <= 3.0) {
            duration = "0-3";
        } else if (seconds > 3.0 && seconds <= 10.0) {
            duration = "3-10";
        } else {
            duration = "10+";
        }

        return duration;
    }
}
