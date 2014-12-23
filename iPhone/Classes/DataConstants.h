//
//  DataConstants.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/17/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface DataConstants : NSObject {

}

extern NSString * const WEATHER;
extern NSString * const TRIPS_DATA;
extern NSString * const REPORT_APPROVAL_LIST_DATA;
extern NSString * const TRIP_APPROVAL_LIST_DATA;
extern NSString * const APPROVE_REPORTS_DATA;
extern NSString * const APPROVE_TRIPS_DATA;
extern NSString * const APPROVE_REPORTS_DATA_APPROVE;
extern NSString * const APPROVE_REPORTS_DATA_REJECT;
extern NSString * const APPROVE_REPORT_DETAIL_DATA;
extern NSString * const VENDOR_IMAGE;
extern NSString * const IMAGE;
extern NSString * const CAR_IMAGE;
extern NSString * const OOPES_DATA;
extern NSString * const OOPE_DATA;
extern NSString * const EXPENSE_TYPES_DATA;
extern NSString * const SAVE_OOP_DATA;
extern NSString * const DELETE_OOP_DATA;
//extern NSString * const ME_LIST_DATA;
NSString * ME_LIST_DATA();
// for new expense endpoint
//extern NSString * const MOBILE_SMART_EXPENSE_LIST_DATA;
extern NSString * const ME_DETAIL_DATA;
extern NSString * const ME_SAVE_DATA;
extern NSString * const ME_DELETE_DATA;
extern NSString * const OOP_TYPE;
extern NSString * const PCT_TYPE;
extern NSString * const CCT_TYPE;
extern NSString * const RC_TYPE;
extern NSString * const E_RECEIPT_TYPE;
extern NSString * const SMART_EXPENSE_ID;
extern NSString * const AUTHENTICATION_DATA;
extern NSString * const RESET_PASSWORD_DATA;
extern NSString * const FLIGHT_SCHEDULE_DATA;
extern NSString * const TRAVEL_AGENCY_ASSISTANCE_INFO;
extern NSString * const PRE_SELL_OPTIONS;
extern NSString * const AIR_BENCHMARK_DATA;
extern NSString * const HOTEL_BENCHMARK_DATA;

extern NSString * const CARDS_PERSONAL_TRAN_DATA;
extern NSString * const SUMMARY_DATA;
extern NSString * const REGISTER_DATA;
extern NSString * const PWD_LOGIN_DATA;
extern NSString * const ACTIVE_REPORTS_DATA;
extern NSString * const ACTIVE_REPORT_DETAIL_DATA;
extern NSString * const ADD_TO_REPORT_DATA;
extern NSString * const SUBMIT_REPORT_DATA;
extern NSString * const SAVE_REPORT_DATA;
extern NSString * const SAVE_REPORT_ENTRY_DATA;
extern NSString * const SAVE_REPORT_ENTRY_RECEIPT;
extern NSString * const SAVE_REPORT_RECEIPT;
extern NSString * const SAVE_REPORT_RECEIPT2;
extern NSString * const APPEND_RECEIPT;
extern NSString * const DELETE_RECEIPT;
extern NSString * const REPORT_ENTRY_FORM_DATA;
extern NSString * const REPORT_FORM_DATA;
extern NSString * const FORM_DATA;
extern NSString * const ITEMIZE_HOTEL_DATA;

extern NSString * const REPORT_ENTRY_DETAIL_DATA;
extern NSString * const REPORT_HEADER_DETAIL_DATA;

extern NSString * const ERROR_DATA;
extern NSString * const CURRENCY_DATA;
extern NSString * const FORCE_FETCH;

extern NSString * const UPLOAD_IMAGE_DATA;
extern NSString * const GET_RECEIPT_URL;
extern NSString * const DELETE_REPORT_ENTRY_DATA;
extern NSString * const DELETE_REPORT_DATA;
extern NSString * const RECALL_REPORT_DATA;

extern NSString * const FIND_LOCATION;
extern NSString * const GOV_FIND_DUTY_LOCATION;
extern NSString * const FIND_HOTELS;
extern NSString * const FIND_HOTEL_ROOMS;
extern NSString	* const RESERVE_HOTEL;

extern NSString * const FIND_CARS;
extern NSString * const RESERVE_CAR;

extern NSString * const DOWNLOAD_USER_CONFIG;
extern NSString * const DOWNLOAD_SYSTEM_CONFIG;
extern NSString * const DOWNLOAD_TRAVEL_CUSTOMFIELDS;
extern NSString * const DOWNLOAD_TRAVEL_VIOLATIONREASONS;

extern NSString * const TWITTER_STATUSES;
extern NSString * const APPROVE_INVOICES_DATA;
extern NSString * const INVOICE_APPROVE_DATA;
extern NSString * const INVOICE_REJECT_DATA;
extern NSString * const INVOICE_DETAIL_DATA;

extern NSString * const LIST_FIELD_SEARCH_DATA;

extern NSString * const SAVE_ATTENDEE_DATA;
extern NSString * const ATTENDEE_SEARCH_DATA;
extern NSString * const DEFAULT_ATTENDEE_DATA;
extern NSString * const LOAD_ATTENDEE_FORM;
extern NSString * const ATTENDEES_IN_GROUP_DATA;

extern NSString * const EXCHANGE_RATE_DATA;
extern NSString * const CAR_DISTANCE_TO_DATE_DATA;
extern NSString * const SAFETY_CHECK_IN_DATA;
extern NSString * const SEARCH_APPROVER_DATA;
extern NSString * const ATTENDEE_SEARCH_FIELDS_DATA;
extern NSString * const ATTENDEE_FULL_SEARCH_DATA;
extern NSString * const SEARCH_YODLEE_CARD_DATA;
extern NSString * const ADD_YODLEE_CARD_DATA;
extern NSString * const YODLEE_CARD_LOGIN_FORM_DATA;
extern NSString * const GET_DEFAULT_APPROVER_DATA;

extern NSString * const GET_TRIPIT_CACHE_DATA;
extern NSString * const OBTAIN_TRIPIT_REQUEST_TOKEN;
extern NSString * const OBTAIN_TRIPIT_ACCESS_TOKEN;
extern NSString * const VALIDATE_TRIPIT_ACCESS_TOKEN;
extern NSString * const EXPENSE_TRIPIT_TRIP;
extern NSString * const UNLINK_FROM_TRIPIT;
extern NSString * const MRUKEY;

#pragma mark Government

extern NSString * const GET_GOV_EXPENSE_TYPES;
extern NSString * const GET_GOV_EXPENSE_FORM;
extern NSString * const SAVE_GOV_EXPENSE;
extern NSString * const GOV_EXPENSE_FORM_DATA;
extern NSString * const GOV_EXP_LIST_FIELD_SEARCH_DATA;

extern NSString * const GOV_DOCUMENTS;
extern NSString * const GOV_DOCUMENTS_AUTH_FOR_VCH;
extern NSString * const GOV_STAMP_TM_DOCUMENTS;
extern NSString * const GOV_RETURN_TM_DOCUMENTS;
extern NSString * const GOV_DOCUMENTS_TO_STAMP;
extern NSString * const GOV_DOCUMENT_DETAIL;
extern NSString * const GOV_CREATE_VOUCHER_FROM_AUTH;
extern NSString * const GOV_DOC_AVAIL_STAMPS;
extern NSString * const GOV_STAMP_REQ_INFO;
extern NSString * const GOV_ATTACH_RECEIPT;
extern NSString * const GOV_UNAPPLIED_EXPENSES;
extern NSString * const GOV_DELETE_UNAPPLIED_EXPENSE;
extern NSString * const GOV_ATTACH_EXP_TO_DOC;
extern NSString * const GOV_DELETE_EXP_FROM_DOC;
extern NSString * const GOV_TA_NUMBERS;
extern NSString * const GOV_LOCATIONS;
extern NSString * const GOV_PER_DIEM_RATE;
extern NSString * const GOV_WARNING_MSG;
extern NSString * const GOV_AGREE_TO_SAFEHARBOR;
extern NSString * const GOV_DOC_INFO_FROM_TRIP_LOCATOR;

// product lines
extern NSString * const PROD_CORP;
extern NSString * const PROD_BREEZE;
extern NSString * const PROD_GOVERNMENT;

// product Offering
extern NSString * const PROD_OFFER_BRONX;

// User roles
extern NSString * const ROLE_TRAVEL_REQUEST_APPROVER;
extern NSString * const ROLE_TRIP_APPROVER;
extern NSString * const ROLE_TRAVEL_USER;
extern NSString * const ROLE_ITINVIEWER_USER;
extern NSString * const ROLE_OPEN_BOOKING_USER;
extern NSString * const ROLE_EXPENSE_ONLY_USER;
extern NSString * const ROLE_AMTRAK_USER;
extern NSString * const ROLE_EXPENSE_TRAVELER;
extern NSString * const ROLE_EXPENSE_MANAGER;
extern NSString * const ROLE_INVOICE_APPROVER;
extern NSString * const MOBILE_INVOICE_PAYMENT_USER;
extern NSString * const ROLE_AIR_BOOKING_ENABLED;
extern NSString * const ROLE_TRIPITAD_USER;
extern NSString * const ROLE_LNA_USER;
extern NSString * const ROLE_FLEX_FARING;
extern NSString * const ROLE_GOVERNMENT_USER;
extern NSString * const ROLE_GOVERNMENT_TRAVELER;
extern NSString * const ROLE_CALL_AGENT_ENABLED;
extern NSString * const ROLE_MOBILE_INVOICE_PURCH_APRVR;

extern NSString * const TRAIN_STATIONS;
extern NSString * const	TRAIN_DELIVERY;
extern NSString * const	AMTRAK_SELL;
extern NSString * const	AIR_SELL;
extern NSString * const TRIPIT_LINK;
extern NSString * const TRAIN_SHOP;
extern NSString * const AIR_SHOP;
extern NSString * const AIR_FILTER;
extern NSString * const HOTEL_IMAGES;
extern NSString * const HOTEL_CANCEL;
extern NSString * const AIR_CANCEL;
extern NSString * const CAR_CANCEL;
extern NSString * const AMTRAK_CANCEL;
extern NSString * const CAR_RATES_DATA;
extern NSString * const RECEIPT_STORE_RECEIPTS;

extern NSString* const USER_LAST_CORRUPTED_CACHE_KEY;
extern NSString * const CORP_SSO_AUTHENTICATION_DATA;
extern NSString * const CORP_SSO_QUERY_DATA;
extern NSString * const VALIDATE_SESSION;

extern NSString * const REGISTER_PUSH;

extern NSString * const INVOICE_COUNT;

#pragma mark receipt data type/ content-type conversion
extern NSString * const PDF;
extern NSString * const JPG;
extern NSString * const MIME_TYPE_PDF;
extern NSString * const MIME_TYPE_JPG;

#pragma mark push notification type
// All new expense events have "EXP_" as prefix
extern NSString * const PUSH_NOTIFICATION_TYPE_EXP_RPT_APPR;
extern NSString * const PUSH_NOTIFICATION_TYPE_EXP_CCT_TRXN;
extern NSString * const PUSH_NOTIFICATION_TYPE_EXP_CCT_AUTH;
extern NSString * const PUSH_NOTIFICATION_TYPE_LNA;
extern NSString * const PUSH_NOTIFICATION_TYPE_TRV_TRP_APPR;

#pragma mark - In-Product Messaging
extern NSString * const PUSH_NOTIFICATION_TYPE_IPM_GOGO;

#pragma mark - corporate card cct types
extern NSString * const CCT_TYPE_RPE; // Regular cct for entry
extern NSString * const CCT_TYPE_AUTH; // Authorization
extern NSString * const CCT_TYPE_PRE_AUTH; // PreAuthorization
extern NSString * const CCT_TYPE_CANCEL; // Cancellation

// Corporate does not use all the Ignite Salesforce stuff, but it does use some and we're gradually adding more features.
//#ifdef IGNITE
extern NSString * const CHATTER_COMMENTS_DATA;
extern NSString * const CHATTER_FEED_DATA;
extern NSString * const CHATTER_POST_DATA;
extern NSString * const CHATTER_NEWS_FEED_LABEL;
extern NSString * const CHATTER_OPPORTUNITY_FEED_PREFIX;

extern NSString * const SALES_OPPORTUNITIES_DATA;

extern NSString * const IGNITE_USER_INFO_DATA;
extern NSString * const IGNITE_SEARCH_USERS;

extern NSString * const STATUS_SEGMENT_UNSCHEDULED;

extern NSString * const SALESFORCE_TRIP_DATA;
extern NSString * const SALESFORCE_SHARE_TRIP_DATA;
//#endif

// Get tax forms
extern NSString * const GET_TAX_FORMS_DATA;

// Conditional fields
extern NSString * const GET_DYNAMIC_ACTIONS;

// For evature
extern NSString * const EVA_API_KEY;
extern NSString * const EVA_SITE_CODE;
extern NSString * const EVA_API_DEV_KEY;
extern NSString * const EVA_SITE_DEV_CODE;
extern NSString * const EVA_SUCCESSFUL_PARSE;
extern NSString * const EVA_PARTIAL_PARSE;
extern NSString *const EVA_NOTACCEPTABLE_PARSE;

extern NSString * const EVA_FLOW_HOTELS;
extern NSString * const EVA_FLOW_CARS;
extern NSString * const EVA_FLOW_FLIGHTS;
extern NSString * const EVA_FLOW_TRAINS;

extern NSString * const EVA_FLOW_ANSWER;
extern NSString * const EVA_FLOW_QUESTION;
extern NSString * const EVA_FLOW_STATEMENT;
extern NSString * const EVA_FLOW_GREETING;

// hold type of search
typedef NS_ENUM(NSUInteger, EvaSearchCategory) {
    EVA_UNKNOWN = 0,
    EVA_FLIGHTS,
    EVA_CARS,
    EVA_HOTELS,
    EVA_TRAINS,
    EVA_ANSWER,
    EVA_QUESTION,
    EVA_STATEMENT,
    EVA_GREETING,
};

typedef NS_ENUM(NSUInteger, AlertTag) {
    kInvalidPassword,
    kInvalidEmail,
    kInvalidUnknown,
    kInvalidAll
};

#pragma mark login / reset pin
extern NSString * const RESET_PIN_USER_EMAIL_DATA;
extern NSString * const RESET_PIN_RESET_USER_PIN;
extern NSString * const MOB_PIN_RSET;
extern NSString * const MOB_PWD_RSET;
extern NSString * const MOB_SSO_LGIN;
extern NSString * const MOB_SSO_LGIN_SAFARI;

#pragma mark flurry keys
extern NSString * const FLURRY_VOICE_BOOK_USAGE_SUCCESS;
extern NSString * const FLURRY_VOICE_BOOK_USAGE_CANCELLED;
extern NSString * const FLURRY_VOICE_BOOK_ERROR;
extern NSString * const FLURRY_VOICE_BOOK_COMPLETED;

#pragma mark - Testdrive
extern NSString * const RegTestDriveUserExistError;
extern NSString * const AccountAlreadyExistsMsg;

#pragma mark - TestDrive flurry

#define fCatStartup @"Start Up"
#define fCatSignIn @"Sign In"
#define fCatTestdriveRegistration @"Test Drive Registration"
#define fCatTestdrive @"Test Drive"

#define fNameLearnMoreClick @"Learn More Click"
#define fNameSignInClick @"Sign In Click"
#define fNameTestDriveClick @"Test Drive Click"
#define fNameBackButtonClick @"Back Button Click"
#define fNameSubmitRegistration @"Submit Registration"
#define fNameSubmitRegistrationSuccess @"Submit Registration Success"
#define fNameSubmitRegistrationFailure @"Submit Registration Failure"
#define fNameSubmitRegistrationAccountAlreadyExists @"Submit Registration Account Already Exists"

#pragma mark - Notification
/*
 * this notification is generated after a successfull login 
 */
extern NSString * const NotificationOnLoginSuccess;

/*
 * this notification is generated after a user has car rates data
 */
extern NSString * const NotificationHasCarRatesData;

extern NSString * const NotificationOnFirstTimeLogin;
/**
 * this notification is generated when xml parser finds that account is expired in login result
 */
extern NSString * const NotificationOnAccountExpired;

#pragma mark - Summary Data keys
#define purchaseRequestsToApproveCount @"PurchaseRequestsToApproveCount"

#pragma - mark home definitions
// These need not be here but for now just using this as a common place
// Book Travel Action sheet button IDs
#define BOOKINGS_BTN_AIR @"Book Air"
#define BOOKINGS_BTN_HOTEL @"Book Hotel"
#define BOOKINGS_BTN_CAR @"Book Car"
#define BOOKINGS_BTN_RAIL @"Book Rail"


#define kSECTION_TRAVEL @"TRIPS_BUTTON"
#define kSECTION_APPROVALS @"EXPENSE_APPROVALS"
#define kSECTION_EXPENSES @"kSECTION_EXPENSE_QUICK"
#define kSECTION_EXPENSE_REPORTS @"EXPENSE_REPORTS"
#define kSECTION_EXPENSE_QUICK @"kSECTION_EXPENSE_QUICK"
#define kSECTION_EXPENSE_CARDS @"EXPENSE_CARDS"
#define kSECTION_EXPENSE_APPROVALS @"EXPENSE_APPROVALS"
#define kSECTION_EXPENSE @"EXPENSE"
#define kSECTION_TRIPS_FIND_TRAVEL  @"TRIPS_FIND_TRAVEL"
#define kSECTION_TRIPS @"TRIPS"

// New positions
#define kSECTION_TRIPS_POS 0
#define kSECTION_EXPENSES_POS 1
#define kSECTION_EXPENSE_POS 2
#define kSECTION_EXPENSES_REPORTS_POS 2
#define kSECTION_APPROVALS_POS 3
#define kSECTION_APP_TOUR_POS 4


// Book Travel Action sheet button IDs
#define BOOKINGS_BTN_AIR @"Book Air"
#define BOOKINGS_BTN_HOTEL @"Book Hotel"
#define BOOKINGS_BTN_CAR @"Book Car"
#define BOOKINGS_BTN_RAIL @"Book Rail"
#define kSECTION_TRAVEL @"TRIPS_BUTTON"
#define kSECTION_APPROVALS @"EXPENSE_APPROVALS"
#define kSECTION_EXPENSES @"kSECTION_EXPENSE_QUICK"
#define kSECTION_EXPENSE_REPORTS @"EXPENSE_REPORTS"
#define kSECTION_CAR_MILEAGE @"kSECTION_CAR_MILEAGE"

#define kSECTION_TRIPS_TRIPS_BUTTON  @"TRIPS_BUTTON"
#define kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON @"TRAVEL_REQUEST_BUTTON"

#define kSECTION_EXPENSE_BREEZE_CARD @"kSECTION_EXPENSE_BREEZE_CARD"


@end
