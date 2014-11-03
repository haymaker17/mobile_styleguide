//
//  MessageRegistrationCenter.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MessageRegistrationCenter.h"
#import "MsgControl.h"


#import "Weather.h"
#import "TripsData.h"
#import "OutOfPocketData.h"
#import "ExpenseTypesData.h"

#import "ApproveReportsData.h"
#import "RejectReportsData.h"

#import "OutOfPocketSaveData.h"
#import "OutOfPocketDeleteData.h"
#import "Authenticate.h"
#import "ReportApprovalListData.h"
#import "TripApprovalListData.h"
#import "TripApproveOrReject.h"
#import "ReportApprovalDetailData.h"
#import "OutOfPocketGetEntry.h"
#import "MobileExpenseList.h"
#import "MobileExpenseDetail.h"
#import "MobileExpenseSave.h"
#import "MobileExpenseDelete.h"
#import "MobileSmartExpenseList.h"
#import "CardsGetPersonalAndTransactions.h"
#import "SummaryData.h"
#import "PasswordLogin.h"
#import "RegisterData.h"
#import "ActiveReportListData.h"
#import "AddToReportData.h"
#import "ActiveReportDetailData.h"
#import "SubmitReportData.h"
#import "CurrencyData.h"
#import "UploadReceiptData.h"
#import "DeleteReportEntryData.h"
#import "FindLocation.h"
#import "GovFindDutyLocation.h"
#import "FindHotels.h"
#import "FindRooms.h"
#import	"ReserveHotel.h"
#import "FindCars.h"
#import "ReserveCar.h"
#import "DownloadUserConfig.h"
#import "DownloadSystemConfig.h"
#import "DownloadViolationReasons.h"
#import "TrainStationsData.h"
#import "TrainTimeTablesFetchData.h"
#import "HotelImagesData.h"
#import "ListFieldSearchData.h"
#import "SaveReportData.h"
#import "SaveReportEntryData.h"
#import "SaveReportEntryReceipt.h"
#import "TrainDeliveryData.h"
#import "AmtrakSell.h"
#import "AttendeeSearchData.h"
#import "AgencyAssistanceData.h"
#import "DefaultAttendeeData.h"
#import "SaveAttendeeData.h"
#import "ExchangeRateData.h"
#import "ReportEntryFormData.h"
#import "ReportFormData.h"
#import "SaveReportReceipt.h"
#import "SaveReportReceipt2.h"
#import "DeleteReceipt.h"
#import "LoadAttendeeForm.h"
#import "LoadFormData.h"
#import "HotelCancel.h"
#import "GetReceiptUrl.h"
#import "ItemizeHotelData.h"
#import "CarRatesData.h"
#import "DeleteReportData.h"
#import "RecallReportData.h"
#import "ReportHeaderDetailData.h"
#import "ReportEntryDetailData.h"
#import "ExCarDistanceToDateData.h"
#import "AirSell.h"
#import "AirCancel.h"
#import "CarCancel.h"
#import "AmtrakCancel.h"
#import "PreSellOptions.h"
#import "AirBenchmarkData.h"
#import "HotelBenchmarkData.h"

#import "ReceiptStoreListData.h"
#import "MobileViewController.h"
#import "CarImageDH.h"
#import "AirShop.h"
#import "AirFilter.h"

#import "SafetyCheckInData.h"
#import "SearchApproverData.h"
#import "AttendeeSearchFieldsData.h"
#import "AttendeeFullSearchData.h"
#import "AttendeesInGroupData.h"

#import "SearchYodleeCardData.h"
#import "AddYodleeCardData.h"
#import "YodleeCardLoginFormData.h"
#import "GetDefaultApproverData.h"

#import "TripItLink.h"
#import "DownloadTravelCustomFields.h"
#import "CorpSSOAuthenticate.h"
#import "LoginOptionsViewController.h"
#import "CorpSSOQueryData.h"

#import "TripItCacheData.h"
#import "TripItRequestTokenData.h"
#import "TripItAccessTokenData.h"
#import "TripItExpenseTripData.h"
#import "TripItUnlink.h"
#import "TripItValidateAccessTokenData.h"

#import "ValidateSessionData.h"

#import "RegisterPush.h"
#import "FlightScheduleData.h"
#import "InvoiceCountData.h"
#import "AppendReceiptData.h"

// test salesforce chatter on corp
#import "IgniteUserInfoData.h"
#import "IgniteChatterPostData.h"
#import "ChatterCommentsMsg.h"
#import "IgniteChatterFeedData.h"

#import "GovExpListFieldSearchData.h"
#import "GovExpenseTypesData.h"
#import "GovExpenseSaveData.h"
#import "GovExpenseFormData.h"
#import "GovDocumentsData.h"
#import "GovDocumentsAuthForVCHData.h"
#import "GovDocumentsToStampData.h"
#import "GovDocumentDetailData.h"
#import "GovCreateVoucherFromAuthData.h"
#import "GovDocAvailableStampsData.h"
#import "GovStampRequirementInfoData.h"
#import "GovAttachReceiptData.h"
#import "GovUnappliedExpensesData.h"
#import "GovDeleteUnappliedExpenseData.h"
#import "GovStampTMDocumentData.h"
#import "GovAttachExpToDocData.h"
#import "GovDeleteExpFromDocData.h"
#import "GovTANumbersData.h"
#import "GovLocationsData.h"
#import "GovPerDiemRateData.h"
#import "GovWarningMessagesData.h"
#import "GovSafeHarborAgreementData.h"
#import "GovDocInfoFromTripLocatorData.h"
#import "ConditionalFieldsList.h"

#import "TaxForms.h"
// login reset pin
#import "ResetUserPin.h"
#import "ResetPinUserEmailData.h"

@implementation MessageRegistrationCenter

+ (void) registerAllMessages
{
    [MsgControl registerMsgClass:CAR_IMAGE withClass:[CarImageDH class]];
    [MsgControl registerMsgClass:RECALL_REPORT_DATA withClass:[RecallReportData class]];
    [MsgControl registerMsgClass:DELETE_REPORT_DATA withClass:[DeleteReportData class]];
    [MsgControl registerMsgClass:DELETE_REPORT_ENTRY_DATA withClass:[DeleteReportEntryData class]];
    [MsgControl registerMsgClass:ACTIVE_REPORT_DETAIL_DATA withClass:[ActiveReportDetailData class]];
    [MsgControl registerMsgClass:AUTHENTICATION_DATA withClass:[Authenticate class]];
    [MsgControl registerMsgClass:CORP_SSO_AUTHENTICATION_DATA withClass:[CorpSSOAuthenticate class]];
    [MsgControl registerMsgClass:CORP_SSO_QUERY_DATA withClass:[CorpSSOQueryData class]];
    [MsgControl registerMsgClass:DELETE_OOP_DATA withClass:[OutOfPocketDeleteData class]];
    [MsgControl registerMsgClass:SAVE_OOP_DATA withClass:[OutOfPocketSaveData class]];
    [MsgControl registerMsgClass:UPLOAD_IMAGE_DATA withClass:[UploadReceiptData class]];
   
    [MsgControl registerMsgClass:GET_RECEIPT_URL withClass:[GetReceiptUrl class]];
    [MsgControl registerMsgClass:SUMMARY_DATA withClass:[SummaryData class]];
    [MsgControl registerMsgClass:APPROVE_REPORT_DETAIL_DATA withClass:[ReportApprovalDetailData class]];
    [MsgControl registerMsgClass:ACTIVE_REPORTS_DATA withClass:[ActiveReportListData class]];
    [MsgControl registerMsgClass:ADD_TO_REPORT_DATA withClass:[AddToReportData class]];
    [MsgControl registerMsgClass:SUBMIT_REPORT_DATA withClass:[SubmitReportData class]];
    [MsgControl registerMsgClass:REPORT_APPROVAL_LIST_DATA withClass:[ReportApprovalListData class]];
    [MsgControl registerMsgClass:TRIP_APPROVAL_LIST_DATA withClass:[TripApprovalListData class]];
    [MsgControl registerMsgClass:APPROVE_TRIPS_DATA withClass:[TripApproveOrReject class]];
    [MsgControl registerMsgClass:EXPENSE_TYPES_DATA withClass:[ExpenseTypesData class]];
    [MsgControl registerMsgClass:OOPE_DATA withClass:[OutOfPocketGetEntry class]];
    [MsgControl registerMsgClass:CARDS_PERSONAL_TRAN_DATA withClass:[CardsGetPersonalAndTransactions class]];
    [MsgControl registerMsgClass:PWD_LOGIN_DATA withClass:[PasswordLogin class]];
    [MsgControl registerMsgClass:REGISTER_DATA withClass:[RegisterData class]];
    [MsgControl registerMsgClass:CURRENCY_DATA withClass:[CurrencyData class]];
    [MsgControl registerMsgClass:FIND_LOCATION withClass:[FindLocation class]];
    [MsgControl registerMsgClass:GOV_FIND_DUTY_LOCATION withClass:[GovFindDutyLocation class]];
    [MsgControl registerMsgClass:FIND_HOTELS withClass:[FindHotels class]];
    [MsgControl registerMsgClass:FIND_HOTEL_ROOMS withClass:[FindRooms class]];
    [MsgControl registerMsgClass:RESERVE_HOTEL withClass:[ReserveHotel class]];
    [MsgControl registerMsgClass:FIND_CARS withClass:[FindCars class]];
    [MsgControl registerMsgClass:RESERVE_CAR withClass:[ReserveCar class]];
    [MsgControl registerMsgClass:DOWNLOAD_USER_CONFIG withClass:[DownloadUserConfig class]];
    [MsgControl registerMsgClass:DOWNLOAD_SYSTEM_CONFIG withClass:[DownloadSystemConfig class]];
    [MsgControl registerMsgClass:DOWNLOAD_TRAVEL_CUSTOMFIELDS withClass:[DownloadTravelCustomFields class]];
    [MsgControl registerMsgClass:DOWNLOAD_TRAVEL_VIOLATIONREASONS withClass:[DownloadViolationReasons class]];
    [MsgControl registerMsgClass:TRAIN_STATIONS withClass:[TrainStationsData class]];
    [MsgControl registerMsgClass:TRAIN_SHOP withClass:[TrainTimeTablesFetchData class]];
    [MsgControl registerMsgClass:AIR_SHOP withClass:[AirShop class]];
    [MsgControl registerMsgClass:AIR_FILTER withClass:[AirFilter class]];
    [MsgControl registerMsgClass:HOTEL_IMAGES withClass:[HotelImagesData class]];
    [MsgControl registerMsgClass:TRAVEL_AGENCY_ASSISTANCE_INFO withClass:[AgencyAssistanceData class]];
    [MsgControl registerMsgClass:PRE_SELL_OPTIONS withClass:[PreSellOptions class]];
    [MsgControl registerMsgClass:AIR_BENCHMARK_DATA withClass:[AirBenchmarkData class]];
    [MsgControl registerMsgClass:HOTEL_BENCHMARK_DATA withClass:[HotelBenchmarkData class]];
    [MsgControl registerMsgClass:OOPES_DATA withClass:[OutOfPocketData class]];
    // MOB 12986 - Add new msgid's for MobileEntry data to replace OOPEntry
    // ME == Mobile Expense
    // Hard coded until ereceipts is final.
    [MsgControl registerMsgClass:@"ME_LIST_DATA" withClass:[MobileExpenseList class]];
    // call new class for getting all expenses.
    [MsgControl registerMsgClass:@"MOBILE_SMART_EXPENSE_LIST_DATA" withClass:[MobileSmartExpenseList class]];
    [MsgControl registerMsgClass:ME_DETAIL_DATA withClass:[MobileExpenseDetail class]];
    [MsgControl registerMsgClass:ME_SAVE_DATA withClass:[MobileExpenseSave class]];
    [MsgControl registerMsgClass:ME_DELETE_DATA withClass:[MobileExpenseDelete class]];
    
    [MsgControl registerMsgClass:TRIPS_DATA withClass:[TripsData class]];
    [MsgControl registerMsgClass:APPROVE_REPORTS_DATA_APPROVE withClass:[ApproveReportsData class]];
    [MsgControl registerMsgClass:APPROVE_REPORTS_DATA_REJECT withClass:[RejectReportsData class]];
    [MsgControl registerMsgClass:LIST_FIELD_SEARCH_DATA withClass:[ListFieldSearchData class]];
    [MsgControl registerMsgClass:ATTENDEE_SEARCH_DATA withClass:[AttendeeSearchData class]];
    [MsgControl registerMsgClass:DEFAULT_ATTENDEE_DATA withClass:[DefaultAttendeeData class]];
    [MsgControl registerMsgClass:SAVE_ATTENDEE_DATA withClass:[SaveAttendeeData class]];
    [MsgControl registerMsgClass:LOAD_ATTENDEE_FORM withClass:[LoadAttendeeForm class]];
    [MsgControl registerMsgClass:SAVE_REPORT_DATA withClass:[SaveReportData class]];
    [MsgControl registerMsgClass:SAVE_REPORT_ENTRY_DATA withClass:[SaveReportEntryData class]];
    [MsgControl registerMsgClass:SAVE_REPORT_ENTRY_RECEIPT withClass:[SaveReportEntryReceipt class]];
    [MsgControl registerMsgClass:SAVE_REPORT_RECEIPT withClass:[SaveReportReceipt class]];
    [MsgControl registerMsgClass:SAVE_REPORT_RECEIPT2 withClass:[SaveReportReceipt2 class]];
    [MsgControl registerMsgClass:DELETE_RECEIPT withClass:[DeleteReceipt class]];
    [MsgControl registerMsgClass:EXCHANGE_RATE_DATA withClass:[ExchangeRateData class]];
    [MsgControl registerMsgClass:TRAIN_DELIVERY withClass:[TrainDeliveryData class]];
    [MsgControl registerMsgClass:AMTRAK_SELL withClass:[AmtrakSell class]];
    [MsgControl registerMsgClass:AIR_SELL withClass:[AirSell class]];
    [MsgControl registerMsgClass:TRIPIT_LINK withClass:[TripItLink class]];
    [MsgControl registerMsgClass:REPORT_ENTRY_FORM_DATA withClass:[ReportEntryFormData class]];
    [MsgControl registerMsgClass:REPORT_FORM_DATA withClass:[ReportFormData class]];
    [MsgControl registerMsgClass:FORM_DATA withClass:[LoadFormData class]];
    [MsgControl registerMsgClass:HOTEL_CANCEL withClass:[HotelCancel class]];
    [MsgControl registerMsgClass:AIR_CANCEL withClass:[AirCancel class]];
    [MsgControl registerMsgClass:CAR_CANCEL withClass:[CarCancel class]];
    [MsgControl registerMsgClass:AMTRAK_CANCEL withClass:[AmtrakCancel class]];
    [MsgControl registerMsgClass:ITEMIZE_HOTEL_DATA withClass:[ItemizeHotelData class]];
    [MsgControl registerMsgClass:CAR_RATES_DATA withClass:[CarRatesData class]];
    [MsgControl registerMsgClass:RECEIPT_STORE_RECEIPTS withClass:[ReceiptStoreListData class]];
    [MsgControl registerMsgClass:REPORT_HEADER_DETAIL_DATA withClass:[ReportHeaderDetailData class]];
    [MsgControl registerMsgClass:REPORT_ENTRY_DETAIL_DATA withClass:[ReportEntryDetailData class]];
    [MsgControl registerMsgClass:GET_TAX_FORMS_DATA withClass:[TaxForms class]];
    [MsgControl registerMsgClass:GET_DYNAMIC_ACTIONS withClass:[ConditionalFieldsList class]];
    [MsgControl registerMsgClass:CAR_DISTANCE_TO_DATE_DATA withClass:[ExCarDistanceToDateData class]];
    [MsgControl registerMsgClass:SAFETY_CHECK_IN_DATA withClass:[SafetyCheckInData class]];
    [MsgControl registerMsgClass:SEARCH_APPROVER_DATA withClass:[SearchApproverData class]];
    [MsgControl registerMsgClass:ATTENDEE_SEARCH_FIELDS_DATA withClass:[AttendeeSearchFieldsData class]];
    [MsgControl registerMsgClass:ATTENDEE_FULL_SEARCH_DATA withClass:[AttendeeFullSearchData class]];
    [MsgControl registerMsgClass:SEARCH_YODLEE_CARD_DATA withClass:[SearchYodleeCardData class]];
    [MsgControl registerMsgClass:ADD_YODLEE_CARD_DATA withClass:[AddYodleeCardData class]];
    [MsgControl registerMsgClass:YODLEE_CARD_LOGIN_FORM_DATA withClass:[YodleeCardLoginFormData class]];
    [MsgControl registerMsgClass:GET_DEFAULT_APPROVER_DATA withClass:[GetDefaultApproverData class]];
    [MsgControl registerMsgClass:GET_TRIPIT_CACHE_DATA withClass:[TripItCacheData class]];
    [MsgControl registerMsgClass:OBTAIN_TRIPIT_REQUEST_TOKEN withClass:[TripItRequestTokenData class]];
    [MsgControl registerMsgClass:OBTAIN_TRIPIT_ACCESS_TOKEN withClass:[TripItAccessTokenData class]];
    [MsgControl registerMsgClass:UNLINK_FROM_TRIPIT withClass:[TripItUnlink class]];
    [MsgControl registerMsgClass:ATTENDEES_IN_GROUP_DATA withClass:[AttendeesInGroupData class]];
    [MsgControl registerMsgClass:VALIDATE_SESSION withClass:[ValidateSessionData class]];
    [MsgControl registerMsgClass:EXPENSE_TRIPIT_TRIP withClass:[TripItExpenseTripData class]];
    [MsgControl registerMsgClass:VALIDATE_TRIPIT_ACCESS_TOKEN withClass:[TripItValidateAccessTokenData class]];
    [MsgControl registerMsgClass:REGISTER_PUSH withClass:[RegisterPush class]];
    [MsgControl registerMsgClass:FLIGHT_SCHEDULE_DATA withClass:[FlightScheduleData class]];
    [MsgControl registerMsgClass:INVOICE_COUNT withClass:[InvoiceCountData class]];
    [MsgControl registerMsgClass:APPEND_RECEIPT withClass:[AppendReceiptData class]];

    // salesforce chatter
    [MsgControl registerMsgClass:IGNITE_USER_INFO_DATA withClass:[IgniteUserInfoData class]];
    [MsgControl registerMsgClass:CHATTER_FEED_DATA withClass:[IgniteChatterFeedData class]];
    [MsgControl registerMsgClass:CHATTER_COMMENTS_DATA withClass:[ChatterCommentsMsg class]];
    [MsgControl registerMsgClass:CHATTER_POST_DATA withClass:[IgniteChatterPostData class]];
    
    [MsgControl registerMsgClass:GET_GOV_EXPENSE_TYPES withClass:[GovExpenseTypesData class]];
    [MsgControl registerMsgClass:GET_GOV_EXPENSE_FORM withClass:[GovExpenseFormData class]];
    [MsgControl registerMsgClass:GOV_EXP_LIST_FIELD_SEARCH_DATA withClass:[GovExpListFieldSearchData class]];
    [MsgControl registerMsgClass:GOV_EXPENSE_FORM_DATA withClass:[GovExpenseFormData class]];
    [MsgControl registerMsgClass:SAVE_GOV_EXPENSE withClass:[GovExpenseSaveData class]];
    [MsgControl registerMsgClass:GOV_DOCUMENTS withClass:[GovDocumentsData class]];
    [MsgControl registerMsgClass:GOV_DOCUMENTS_AUTH_FOR_VCH withClass:[GovDocumentsAuthForVCHData class]];
    [MsgControl registerMsgClass:GOV_DOCUMENTS_TO_STAMP withClass:[GovDocumentsToStampData class]];
    [MsgControl registerMsgClass:GOV_DOCUMENT_DETAIL withClass:[GovDocumentDetailData class]];
    [MsgControl registerMsgClass:GOV_CREATE_VOUCHER_FROM_AUTH withClass:[GovCreateVoucherFromAuthData class]];
    [MsgControl registerMsgClass:GOV_DOC_AVAIL_STAMPS withClass:[GovDocAvailableStampsData class]];
    [MsgControl registerMsgClass:GOV_STAMP_REQ_INFO withClass:[GovStampRequirementInfoData class]];
    [MsgControl registerMsgClass:GOV_ATTACH_RECEIPT withClass:[GovAttachReceiptData class]];
    [MsgControl registerMsgClass:GOV_UNAPPLIED_EXPENSES withClass:[GovUnappliedExpensesData class]];
    [MsgControl registerMsgClass:GOV_DELETE_UNAPPLIED_EXPENSE withClass:[GovDeleteUnappliedExpenseData class]];
    [MsgControl registerMsgClass:GOV_STAMP_TM_DOCUMENTS withClass:[GovStampTMDocumentData class]];
    [MsgControl registerMsgClass:GOV_ATTACH_EXP_TO_DOC withClass:[GovAttachExpToDocData class]];
    [MsgControl registerMsgClass:GOV_DELETE_EXP_FROM_DOC withClass:[GovDeleteExpFromDocData class]];
    [MsgControl registerMsgClass:GOV_TA_NUMBERS withClass:[GovTANumbersData class]];
    [MsgControl registerMsgClass:GOV_LOCATIONS withClass:[GovLocationsData class]];
    [MsgControl registerMsgClass:GOV_PER_DIEM_RATE withClass:[GovPerDiemRateData class]];
    [MsgControl registerMsgClass:GOV_WARNING_MSG withClass:[GovWarningMessagesData class]];
    [MsgControl registerMsgClass:GOV_AGREE_TO_SAFEHARBOR withClass:[GovSafeHarborAgreementData class]];
    [MsgControl registerMsgClass:GOV_DOC_INFO_FROM_TRIP_LOCATOR withClass:[GovDocInfoFromTripLocatorData class]];

    // Pin create/reset
    [MsgControl registerMsgClass:RESET_PIN_RESET_USER_PIN withClass:[ResetUserPin class]];
    [MsgControl registerMsgClass:RESET_PIN_USER_EMAIL_DATA withClass:[ResetPinUserEmailData class]];
}

@end
