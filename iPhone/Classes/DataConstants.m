//
//  DataConstants.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/17/09.
//  Copyright 2009 Concur. All rights reserved.
//
//  Change log
//      Updated on 4/22 by Pavan for MOB-12986

#import "DataConstants.h"
#import "Config.h"

@implementation DataConstants

NSString * const WEATHER = @"WEATHER";
NSString * const TRIPS_DATA = @"TRIPS_DATA";
NSString * const REPORT_APPROVAL_LIST_DATA = @"REPORT_APPROVAL_LIST_DATA";
NSString * const TRIP_APPROVAL_LIST_DATA = @"TRIP_APPROVAL_LIST_DATA";
NSString * const APPROVE_REPORTS_DATA = @"APPROVE_REPORTS_DATA";
NSString * const APPROVE_TRIPS_DATA = @"APPROVE_TRIPS_DATA";
NSString * const APPROVE_REPORTS_DATA_APPROVE = @"APPROVE_REPORTS_DATA_APPROVE";
NSString * const APPROVE_REPORTS_DATA_REJECT = @"APPROVE_REPORTS_DATA_REJECT";
NSString * const APPROVE_REPORT_DETAIL_DATA = @"APPROVE_REPORT_DETAIL_DATA";
NSString * const VENDOR_IMAGE = @"VENDOR_IMAGE";
NSString * const IMAGE = @"IMAGE";
NSString * const CAR_IMAGE = @"CAR_IMAGE";
NSString * const OOPES_DATA = @"OOPES_DATA";
NSString * const OOPE_DATA = @"OOPE_DATA";
NSString * const EXPENSE_TYPES_DATA = @"EXPENSE_TYPES_DATA"; 
NSString * const SAVE_OOP_DATA = @"SAVE_OOP_DATA"; 
NSString * const DELETE_OOP_DATA = @"DELETE_OOP_DATA_OOP_DATA";
// MOB 12986 - Add new msgid's for MobileEntry data to replace OOPEntry
// ME == Mobile Expense
NSString * ME_LIST_DATA() {
    // Check for eReceipts
    if ([Config isEreceiptsEnabled]) {
       return  @"MOBILE_SMART_EXPENSE_LIST_DATA";
    }
    else
    {
        return @"ME_LIST_DATA";
    }
    //
    //NSString * const ME_LIST_DATA = @"ME_LIST_DATA";
    //NSString * const MOBILE_SMART_EXPENSE_LIST_DATA = @"MOBILE_SMART_EXPENSE_LIST_DATA";
}

NSString * const ME_DETAIL_DATA = @"ME_DETAIL_DATA";
NSString * const ME_SAVE_DATA = @"ME_SAVE_DATA";
NSString * const ME_DELETE_DATA = @"ME_DELETE_DATA";

NSString * const OOP_TYPE = @"OOP_TYPE";
NSString * const PCT_TYPE = @"PCT_TYPE";
NSString * const CCT_TYPE = @"CCT_TYPE";
NSString * const RC_TYPE = @"RC_TYPE";
NSString * const E_RECEIPT_TYPE = @"E_RECEIPT_TYPE";
NSString * const SMART_EXPENSE_ID = @"SMART_EXPENSE_ID";

NSString * const AUTHENTICATION_DATA = @"AUTHENTICATION_DATA"; 
NSString * const RESET_PASSWORD_DATA = @"RESET_PASSWORD_DATA";
NSString * const FLIGHT_SCHEDULE_DATA = @"FLIGHT_SCHEDULE_DATA";
NSString * const TRAVEL_AGENCY_ASSISTANCE_INFO = @"TRAVEL_AGENCY_ASSISTANCE_INFO";
NSString * const PRE_SELL_OPTIONS = @"PRE_SELL_OPTIONS";
NSString * const AIR_BENCHMARK_DATA = @"AIR_BENCHMARK_DATA";
NSString * const HOTEL_BENCHMARK_DATA = @"HOTEL_BENCHMARK_DATA";

NSString * const CARDS_PERSONAL_TRAN_DATA = @"CARDS_PERSONAL_TRAN_DATA";
NSString * const SUMMARY_DATA = @"SUMMARY_DATA";
NSString * const REGISTER_DATA = @"REGISTER_DATA";
NSString * const PWD_LOGIN_DATA = @"PWD_LOGIN_DATA";
NSString * const ACTIVE_REPORTS_DATA = @"ACTIVE_REPORTS_DATA";
NSString * const ACTIVE_REPORT_DETAIL_DATA = @"ACTIVE_REPORT_DETAIL_DATA";
NSString * const ADD_TO_REPORT_DATA = @"ADD_TO_REPORT_DATA";
NSString * const SUBMIT_REPORT_DATA = @"SUBMIT_REPORT_DATA";
NSString * const SAVE_REPORT_DATA = @"SAVE_REPORT_DATA";
NSString * const SAVE_REPORT_ENTRY_DATA = @"SAVE_REPORT_ENTRY_DATA";
NSString * const SAVE_REPORT_ENTRY_RECEIPT = @"SAVE_REPORT_ENTRY_RECEIPT";
NSString * const SAVE_REPORT_RECEIPT = @"SAVE_REPORT_RECEIPT";
NSString * const SAVE_REPORT_RECEIPT2 = @"SAVE_REPORT_RECEIPT2";
NSString * const APPEND_RECEIPT = @"APPEND_RECEIPT";
NSString * const DELETE_RECEIPT = @"DELETE_RECEIPT";
NSString * const REPORT_ENTRY_FORM_DATA = @"REPORT_ENTRY_FORM_DATA";
NSString * const REPORT_FORM_DATA = @"REPORT_FORM_DATA";
NSString * const FORM_DATA = @"FORM_DATA";
NSString * const ITEMIZE_HOTEL_DATA = @"ITEMIZE_HOTEL_DATA";
NSString * const CAR_RATES_DATA = @"CAR_RATES_DATA";

NSString * const REPORT_ENTRY_DETAIL_DATA = @"REPORT_ENTRY_DETAIL_DATA";
NSString * const REPORT_HEADER_DETAIL_DATA = @"REPORT_HEADER_DETAIL_DATA";

NSString * const ERROR_DATA = @"ERROR_DATA";
NSString * const CURRENCY_DATA = @"CURRENCY_DATA";
NSString * const FORCE_FETCH = @"FORCE_FETCH";

NSString * const UPLOAD_IMAGE_DATA = @"UPLOAD_IMAGE_DATA";
NSString * const GET_RECEIPT_URL = @"GET_RECEIPT_URL";

NSString * const DELETE_REPORT_ENTRY_DATA = @"DELETE_REPORT_ENTRY_DATA";
NSString * const DELETE_REPORT_DATA = @"DELETE_REPORT_DATA";
NSString * const RECALL_REPORT_DATA = @"RECALL_REPORT_DATA";

NSString * const FIND_LOCATION = @"FIND_LOCATION";
NSString * const GOV_FIND_DUTY_LOCATION = @"GOV_FIND_DUTY_LOCATION";
NSString * const FIND_HOTELS = @"FIND_HOTELS";
NSString * const FIND_HOTEL_ROOMS = @"FIND_HOTEL_ROOMS";
NSString * const RESERVE_HOTEL = @"RESERVE_HOTEL";

NSString * const FIND_CARS = @"FIND_CARS";
NSString * const RESERVE_CAR = @"RESERVE_CAR";

NSString * const DOWNLOAD_USER_CONFIG = @"DOWNLOAD_USER_CONFIG";
NSString * const DOWNLOAD_SYSTEM_CONFIG = @"DOWNLOAD_SYSTEM_CONFIG";
NSString * const DOWNLOAD_TRAVEL_CUSTOMFIELDS = @"DOWNLOAD_TRAVEL_CUSTOMFIELDS";
NSString * const DOWNLOAD_TRAVEL_VIOLATIONREASONS = @"DOWNLOAD_TRAVEL_VIOLATIONREASONS";
NSString * const TWITTER_STATUSES = @"TWITTER_STATUSES";

NSString * const APPROVE_INVOICES_DATA=@"APPROVE_INVOICES_DATA";
NSString * const INVOICE_APPROVE_DATA=@"INVOICE_APPROVE_DATA";
NSString * const INVOICE_REJECT_DATA=@"INVOICE_REJECT_DATA";
NSString * const INVOICE_DETAIL_DATA=@"INVOICE_DETAIL_DATA";

NSString * const LIST_FIELD_SEARCH_DATA=@"LIST_FIELD_SEARCH_DATA";

NSString * const SAVE_ATTENDEE_DATA = @"SAVE_ATTENDEE_DATA";
NSString * const ATTENDEE_SEARCH_DATA = @"ATTENDEE_SEARCH_DATA";
NSString * const DEFAULT_ATTENDEE_DATA = @"DEFAULT_ATTENDEE_DATA";
NSString * const LOAD_ATTENDEE_FORM = @"LOAD_ATTENDEE_FORM";
NSString * const EXCHANGE_RATE_DATA = @"EXCHANGE_RATE_DATA";
NSString * const CAR_DISTANCE_TO_DATE_DATA = @"CAR_DISTANCE_TO_DATE_DATA";

NSString * const SAFETY_CHECK_IN_DATA = @"SAFETY_CHECK_IN_DATA";
NSString * const SEARCH_APPROVER_DATA = @"SEARCH_APPROVER_DATA";
NSString * const ATTENDEE_SEARCH_FIELDS_DATA = @"ATTENDEE_SEARCH_FIELDS_DATA";
NSString * const ATTENDEE_FULL_SEARCH_DATA = @"ATTENDEE_FULL_SEARCH_DATA";
NSString * const ATTENDEES_IN_GROUP_DATA = @"ATTENDEES_IN_GROUP_DATA";

NSString * const SEARCH_YODLEE_CARD_DATA = @"SEARCH_YODLEE_CARD_DATA";
NSString * const ADD_YODLEE_CARD_DATA = @"ADD_YODLEE_CARD_DATA";
NSString * const YODLEE_CARD_LOGIN_FORM_DATA = @"YODLEE_CARD_LOGIN_FORM_DATA";

NSString * const GET_DEFAULT_APPROVER_DATA = @"GET_DEFAULT_APPROVER_DATA";

NSString * const GET_TRIPIT_CACHE_DATA = @"GET_TRIPIT_CACHE_DATA";
NSString * const OBTAIN_TRIPIT_REQUEST_TOKEN = @"OBTAIN_TRIPIT_REQUEST_TOKEN";
NSString * const OBTAIN_TRIPIT_ACCESS_TOKEN = @"OBTAIN_TRIPIT_ACCESS_TOKEN";
NSString * const VALIDATE_TRIPIT_ACCESS_TOKEN = @"VALIDATE_TRIPIT_ACCESS_TOKEN";
NSString * const EXPENSE_TRIPIT_TRIP = @"EXPENSE_TRIPIT_TRIP";
NSString * const UNLINK_FROM_TRIPIT  = @"UNLINK_FROM_TRIPIT";

#pragma mark Government
NSString * const GET_GOV_EXPENSE_TYPES = @"GET_GOV_EXPENSE_TYPES";
NSString * const GET_GOV_EXPENSE_FORM = @"GET_GOV_EXPENSE_FORM";
NSString * const SAVE_GOV_EXPENSE = @"SAVE_GOV_EXPENSE";
NSString * const GOV_EXPENSE_FORM_DATA = @"GOV_EXPENSE_FORM_DATA";
NSString * const GOV_EXP_LIST_FIELD_SEARCH_DATA = @"GOV_EXP_LIST_FIELD_SEARCH_DATA";

NSString * const GOV_DOCUMENTS = @"GOV_DOCUMENTS";
NSString * const GOV_DOCUMENTS_AUTH_FOR_VCH = @"GOV_DOCUMENTS_AUTH_FOR_VCH";
NSString * const GOV_DOCUMENTS_TO_STAMP = @"GOV_DOCUMENTS_TO_STAMP";
NSString * const GOV_STAMP_TM_DOCUMENTS = @"GOV_STAMP_TM_DOCUMENTS";
NSString * const GOV_RETURN_TM_DOCUMENTS = @"GOV_RETURN_TM_DOCUMENTS";
NSString * const GOV_DOCUMENT_DETAIL = @"GOV_DOCUMENT_DETAIL";
NSString * const GOV_CREATE_VOUCHER_FROM_AUTH = @"GOV_CREATE_VOUCHER_FROM_AUTH";
NSString * const GOV_DOC_AVAIL_STAMPS = @"GOV_DOC_AVAIL_STAMPS";
NSString * const GOV_STAMP_REQ_INFO = @"GOV_STAMP_REQ_INFO";
NSString * const GOV_ATTACH_RECEIPT = @"GOV_ATTACH_RECEIPT";
NSString * const GOV_UNAPPLIED_EXPENSES = @"GOV_UNAPPLIED_EXPENSES";
NSString * const GOV_DELETE_UNAPPLIED_EXPENSE = @"GOV_DELETE_UNAPPLIED_EXPENSE";
NSString * const GOV_ATTACH_EXP_TO_DOC = @"GOV_ATTACH_EXP_TO_DOC";
NSString * const GOV_DELETE_EXP_FROM_DOC = @"GOV_DELETE_EXP_FROM_DOC";
NSString * const GOV_TA_NUMBERS = @"GOV_TA_NUMBERS";
NSString * const GOV_LOCATIONS = @"GOV_LOCATIONS";
NSString * const GOV_PER_DIEM_RATE = @"GOV_PER_DIEM_RATE";
NSString * const GOV_WARNING_MSG = @"GOV_WARNING_MSG";
NSString * const GOV_AGREE_TO_SAFEHARBOR = @"GOV_AGREE_TO_SAFEHARBOR";
NSString * const GOV_DOC_INFO_FROM_TRIP_LOCATOR = @"GOV_DOC_INFO_FROM_TRIP_LOCATOR";

#pragma mark Product Line
// Constants for product line identification
NSString * const PROD_CORP = @"Corporate";
NSString * const PROD_BREEZE = @"Breeze";
NSString * const PROD_GOVERNMENT = @"Government";

NSString * const PROD_OFFER_BRONX = @"BRONX";
NSString * const ROLE_TRIPITAD_USER = @"TripItAd_User";

#pragma mark User Roles
// User roles
NSString * const ROLE_TRAVEL_REQUEST_APPROVER = @"Travel_Request_Approver";
NSString * const ROLE_TRIP_APPROVER = @"TravelApprover";
NSString * const ROLE_TRAVEL_USER = @"TravelUser";
NSString * const ROLE_ITINVIEWER_USER = @"ItineraryViewer";
NSString * const ROLE_OPEN_BOOKING_USER = @"OpenBookingUser";
NSString * const ROLE_AMTRAK_USER = @"Amtrak_User";
NSString * const ROLE_EXPENSE_ONLY_USER = @"EXPENSE_ONLY_USER";
NSString * const ROLE_EXPENSE_TRAVELER = @"MOBILE_EXPENSE_TRAVELER";
NSString * const ROLE_EXPENSE_MANAGER = @"MOBILE_EXPENSE_MANAGER";
NSString * const ROLE_INVOICE_APPROVER = @"MOBILE_INVOICE_PAYMENT_APRVR";
NSString * const MOBILE_INVOICE_PAYMENT_USER = @"MOBILE_INVOICE_PAYMENT_USER";
NSString * const ROLE_AIR_BOOKING_ENABLED = @"Air_Booking_Enabled";
NSString * const ROLE_LNA_USER = @"LNA_User";
NSString * const ROLE_FLEX_FARING = @"Flex_Faring";
NSString * const ROLE_GOVERNMENT_USER = @"GovTravelManager";
NSString * const ROLE_GOVERNMENT_TRAVELER = @"TravelManagerTraveler";
NSString * const ROLE_CALL_AGENT_ENABLED = @"EnableCallTravelAgent";
NSString * const ROLE_MOBILE_INVOICE_PURCH_APRVR = @"MOBILE_INVOICE_PURCH_APRVR";


NSString * const TRAIN_STATIONS = @"TRAIN_STATIONS";
NSString * const TRAIN_SHOP = @"TRAIN_SHOP";
NSString * const AIR_SHOP = @"AIR_SHOP";
NSString * const AIR_FILTER = @"AIR_FILTER";
NSString * const TRAIN_DELIVERY = @"TRAIN_DELIVERY";
NSString * const AMTRAK_SELL = @"AMTRAK_SELL";
NSString * const AIR_SELL = @"AIR_SELL";
NSString *const TRIPIT_LINK = @"TRIPIT_LINK";

NSString * const HOTEL_IMAGES = @"HOTEL_IMAGES";
NSString * const HOTEL_CANCEL = @"HOTEL_CANCEL";
NSString * const AIR_CANCEL = @"AIR_CANCEL";
NSString * const CAR_CANCEL = @"CAR_CANCEL";
NSString * const AMTRAK_CANCEL = @"AMTRAK_CANCEL";
NSString * const RECEIPT_STORE_RECEIPTS = @"RECEIPT_STORE_RECEIPTS";

NSString* const USER_LAST_CORRUPTED_CACHE_KEY = @"lastCorruptedCacheKey";
NSString * const CORP_SSO_AUTHENTICATION_DATA = @"CORP_SSO_AUTHENTICATION_DATA";
NSString * const CORP_SSO_QUERY_DATA = @"CORP_SSO_QUERY_DATA";
NSString * const VALIDATE_SESSION = @"VALIDATE_SESSION";

NSString * const REGISTER_PUSH = @"REGISTER_PUSH";
NSString * const MRUKEY = @"Most Recently Used";

// Corporate does not use all the Ignite Salesforce stuff, but it does use some and we're gradually adding more features.
//#ifdef IGNITE
NSString * const CHATTER_FEED_DATA = @"CHATTER_FEED_DATA";
NSString * const CHATTER_COMMENTS_DATA = @"CHATTER_COMMENTS_DATA";
NSString * const CHATTER_POST_DATA = @"CHATTER_POST_DATA";
NSString * const CHATTER_NEWS_FEED_LABEL = @"news";
NSString * const CHATTER_OPPORTUNITY_FEED_PREFIX = @"opp_";

NSString * const SALES_OPPORTUNITIES_DATA = @"SALES_OPPORTUNITIES_DATA";

NSString * const IGNITE_USER_INFO_DATA = @"IGNITE_USER_INFO_DATA";
NSString * const IGNITE_SEARCH_USERS = @"IGNITE_SEARCH_USERS";

NSString * const STATUS_SEGMENT_UNSCHEDULED = @"SEGMENT_UNSCHEDULED";

NSString * const SALESFORCE_TRIP_DATA = @"SALESFORCE_TRIP_DATA";
NSString * const SALESFORCE_SHARE_TRIP_DATA = @"SALESFORCE_SHARE_TRIP_DATA";
//#endif

NSString * const INVOICE_COUNT = @"INVOICE_COUNT";

NSString * const PDF = @"PDF";
NSString * const JPG = @"JPG";
NSString * const MIME_TYPE_PDF = @"application/pdf";
NSString * const MIME_TYPE_JPG = @"image/jpg";

NSString * const PUSH_NOTIFICATION_TYPE_EXP_RPT_APPR = @"EXP_RPT_APPR";
NSString * const PUSH_NOTIFICATION_TYPE_EXP_CCT_TRXN = @"EXP_CCT_TRXN";
NSString * const PUSH_NOTIFICATION_TYPE_EXP_CCT_AUTH = @"EXP_CCT_AUTH";  // AMEX authorization
NSString * const PUSH_NOTIFICATION_TYPE_LNA = @"LNA";
NSString * const PUSH_NOTIFICATION_TYPE_TRV_TRP_APPR = @"TRV_TRP_APPR";
NSString * const PUSH_NOTIFICATION_TYPE_IPM_GOGO = @"IPM_GOGO";

#pragma mark corporate card cct types
NSString * const CCT_TYPE_RPE = @"RPE"; // Regular cct for entry
NSString * const CCT_TYPE_AUTH = @"ATH"; // Authorization
NSString * const CCT_TYPE_PRE_AUTH = @"APA"; // PreAuthorization
NSString * const CCT_TYPE_CANCEL = @"ACN"; // Cancellation

// Tax forms
NSString * const GET_TAX_FORMS_DATA = @"GET_TAX_FORMS_DATA";

//Conditional Fields
NSString * const GET_DYNAMIC_ACTIONS = @"GET_DYNAMIC_ACTIONS";

// Evature
NSString * const EVA_API_KEY = @"0585a2f5-9d6c-41a4-981a-842fc791b5dc";
NSString * const EVA_SITE_CODE = @"concur_m" ;
NSString * const EVA_API_DEV_KEY = @"1ee20d8e-7423-41d3-88e1-2034db5d96bb";
NSString * const EVA_SITE_DEV_CODE = @"concur_dev";
// ‘Answer’, ‘Question’, ‘Greeting’, ‘Statement’,
NSString * const EVA_FLOW_HOTELS = @"Hotel";
NSString * const EVA_FLOW_CARS = @"Car";
NSString * const EVA_FLOW_FLIGHTS = @"Flight";
NSString * const EVA_FLOW_TRAINS = @"Train";

NSString * const EVA_FLOW_ANSWER = @"Answer";
NSString * const EVA_FLOW_QUESTION = @"Question";
NSString * const EVA_FLOW_STATEMENT = @"Statement";
NSString * const EVA_FLOW_GREETING = @"Greeting";

//"EVA_WS_URL" = http://concur.evaws.com
//"EVA_WS_DEV_URL = http://dev.evaws.com

NSString * const EVA_SUCCESSFUL_PARSE = @"Successful Parse";
NSString *const EVA_NOTACCEPTABLE_PARSE = @"Not Acceptable";
NSString * const EVA_PARTIAL_PARSE =  @"Partial Parse";

#pragma mark login / reset pin
NSString * const RESET_PIN_USER_EMAIL_DATA = @"RESET_PIN_USER_EMAIL_DATA";
NSString * const RESET_PIN_RESET_USER_PIN = @"RESET_PIN_RESET_USER_PIN";
NSString * const MOB_PIN_RSET = @"MOB_PIN_RSET";
NSString * const MOB_PWD_RSET = @"MOB_PWD_RSET";
NSString * const MOB_SSO_LGIN = @"MOB_SSO_LGIN";

#pragma mark flurry keys
NSString * const FLURRY_VOICE_BOOK_USAGE_SUCCESS = @"Voice Book: Usage Success";
NSString * const FLURRY_VOICE_BOOK_USAGE_CANCELLED = @"Voice Book: Usage Cancelled";
NSString * const FLURRY_VOICE_BOOK_ERROR = @"Voice Book: Error";
NSString * const FLURRY_VOICE_BOOK_COMPLETED = @"Voice Book: Completed";

#pragma mark - Testdrive
NSString * const RegTestDriveUserExistError = @"RegTestDriveUserExistError";
NSString * const AccountAlreadyExistsMsg = @"Account Already Exists";

NSString * const NotificationOnLoginSuccess = @"NotificationOnLoginSuccess";
NSString * const NotificationOnFirstTimeLogin = @"NotificationOnFirstTimeLogin";
NSString * const NotificationHasCarRatesData = @"NotificationHasCarRatesData";
NSString * const NotificationOnAccountExpired = @"NotificationOnAccountExpired";


@end
