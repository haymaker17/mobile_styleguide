package com.concur.mobile.corp.test;

import android.app.Activity;
import android.util.Log;

import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.ApptentiveListActivity;
import com.apptentive.android.sdk.ViewActivity;
import com.concur.mobile.base.util.Const;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.LogView;
import com.concur.mobile.core.activity.MessageCenter;
import com.concur.mobile.core.activity.OffLineUploadList;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.activity.Tour;
import com.concur.mobile.core.activity.ViewImage;
import com.concur.mobile.core.expense.activity.ExpenseDetail;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.activity.ExpensesAndReceiptsActivity;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.activity.TAExpensesActivity;
import com.concur.mobile.core.expense.activity.TAItineraryActivity;
import com.concur.mobile.core.expense.activity.TAStopActivity;
import com.concur.mobile.core.expense.charge.activity.QuickExpense;
import com.concur.mobile.core.expense.receiptstore.activity.ExpenseReceiptCombined;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptShare;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptShareStatus;
import com.concur.mobile.core.expense.report.activity.AttendeeSearch;
import com.concur.mobile.core.expense.report.activity.ExpenseAttendeeEdit;
import com.concur.mobile.core.expense.report.activity.ExpenseAttendeePreview;
import com.concur.mobile.core.expense.report.activity.ExpenseDuplicateAttendee;
import com.concur.mobile.core.expense.report.activity.ExpenseEntries;
import com.concur.mobile.core.expense.report.activity.ExpenseEntry;
import com.concur.mobile.core.expense.report.activity.ExpenseEntryAttendee;
import com.concur.mobile.core.expense.report.activity.ExpenseHotelWizard;
import com.concur.mobile.core.expense.report.activity.ExpenseReceipt;
import com.concur.mobile.core.expense.report.activity.ExpenseReportHeader;
import com.concur.mobile.core.expense.report.activity.ExpenseSendBack;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.expense.report.approval.activity.ExpenseApproval;
import com.concur.mobile.core.invoice.activity.InvoicesWebView;
import com.concur.mobile.core.invoice.activity.PurchaseRequestsWebView;
import com.concur.mobile.core.request.activity.LocationSearchActivity;
import com.concur.mobile.core.request.activity.RequestEntryActivity;
import com.concur.mobile.core.request.activity.RequestHeaderActivity;
import com.concur.mobile.core.request.activity.RequestListActivity;
import com.concur.mobile.core.request.activity.RequestSummaryActivity;
import com.concur.mobile.core.travel.activity.AgencyInformation;
import com.concur.mobile.core.travel.activity.ImageActivity;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.LocationSearchV1;
import com.concur.mobile.core.travel.activity.OfferList;
import com.concur.mobile.core.travel.activity.OfferWebView;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.activity.SegmentList;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.activity.TravelCustomFieldSearch;
import com.concur.mobile.core.travel.activity.TravelPointsExplanation;
import com.concur.mobile.core.travel.activity.TravelViolationsApprovalChoice;
import com.concur.mobile.core.travel.activity.TravelViolationsForManagerApproval;
import com.concur.mobile.core.travel.activity.TripList;
import com.concur.mobile.core.travel.air.activity.AirFlightDetail;
import com.concur.mobile.core.travel.air.activity.AirPriceToBeatSearch;
import com.concur.mobile.core.travel.air.activity.AirPriceToBeatSearchResults;
import com.concur.mobile.core.travel.air.activity.AirResultsList;
import com.concur.mobile.core.travel.air.activity.AirSearch;
import com.concur.mobile.core.travel.air.activity.AirSearchNoResults;
import com.concur.mobile.core.travel.air.activity.AirSearchProgress;
import com.concur.mobile.core.travel.air.activity.AirSearchResultsSummary;
import com.concur.mobile.core.travel.air.activity.AlternativeAirScheduleList;
import com.concur.mobile.core.travel.air.activity.VoiceAirSearchActivity;
import com.concur.mobile.core.travel.approval.activity.RuleViolationSummary;
import com.concur.mobile.core.travel.car.activity.CarSearch;
import com.concur.mobile.core.travel.car.activity.CarSearchDetail;
import com.concur.mobile.core.travel.car.activity.CarSearchNoResults;
import com.concur.mobile.core.travel.car.activity.CarSearchProgress;
import com.concur.mobile.core.travel.car.activity.CarSearchResults;
import com.concur.mobile.core.travel.car.activity.VoiceCarSearchActivity;
import com.concur.mobile.core.travel.hotel.jarvis.activity.RestHotelSearch;
import com.concur.mobile.core.travel.hotel.activity.VoiceHotelSearchActivity;
import com.concur.mobile.core.travel.rail.activity.RailSearch;
import com.concur.mobile.core.travel.rail.activity.RailSearchDetail;
import com.concur.mobile.core.travel.rail.activity.RailSearchNoResults;
import com.concur.mobile.core.travel.rail.activity.RailSearchProgress;
import com.concur.mobile.core.travel.rail.activity.RailSearchResults;
import com.concur.mobile.core.travel.rail.activity.RailSearchResultsFares;
import com.concur.mobile.core.travel.request.activity.TravelRequestApprovalsWebView;
import com.concur.mobile.core.widget.FileSearchActivity;
import com.concur.mobile.corp.activity.CompanyCodeLoginActivity;
import com.concur.mobile.corp.activity.CompanySignOnActivity;
import com.concur.mobile.corp.activity.EmailLookupActivity;
import com.concur.mobile.corp.activity.ForegroundApp;
import com.concur.mobile.corp.activity.Home;
import com.concur.mobile.corp.activity.LocationCheckIn;
import com.concur.mobile.corp.activity.Login;
import com.concur.mobile.corp.activity.LoginHelp;
import com.concur.mobile.corp.activity.LoginHelpTopic;
import com.concur.mobile.corp.activity.LoginPasswordActivity;
import com.concur.mobile.corp.activity.OpenSourceLicenseDisplay;
import com.concur.mobile.corp.activity.OpenSourceLicenseInfo;
import com.concur.mobile.corp.activity.PreLogin;
import com.concur.mobile.corp.activity.ProfileInfo;
import com.concur.mobile.corp.activity.Register;
import com.concur.mobile.corp.activity.SimpleWebViewActivity;
import com.concur.mobile.corp.activity.Startup;
import com.concur.mobile.corp.activity.TestDriveRegistration;
import com.concur.mobile.corp.activity.TestDriveTour;
import com.concur.mobile.corp.activity.UniversalTour;
import com.concur.mobile.core.travel.hotel.jarvis.activity.ImageDetailActivity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
public class CorporateUITestSuite extends CorporateTestSuite {
    private static String CLS_TAG = CorporateUITestSuite.class.getSimpleName();

    @Test
    public void testAllActivitiesLifecycle() {
        if (!testActivities) {
            return;
        }
        List<Class<? extends Activity>> failedActivities = new ArrayList<>();
        List<Class<? extends Activity>> successfulLaunchActivities = new ArrayList<>();
        List<Class<? extends Activity>> activities = Arrays.asList(
            AgencyInformation.class
            , AirFlightDetail.class
            , AirPriceToBeatSearch.class
            , AirPriceToBeatSearchResults.class
            , AirResultsList.class
            , AirSearch.class
            , AirSearchNoResults.class
            , AirSearchProgress.class
            , AirSearchResultsSummary.class
            , AlternativeAirScheduleList.class
            , Approval.class
            , ApptentiveActivity.class
            , ApptentiveListActivity.class
            , AttendeeSearch.class
            , BaseActivity.class
            , BaseActivity.class
            , CarSearch.class
            , CarSearchDetail.class
            , CarSearchNoResults.class
            , CarSearchProgress.class
            , CarSearchResults.class
            , CompanyCodeLoginActivity.class
            , CompanySignOnActivity.class
            , EmailLookupActivity.class
            , ExpenseApproval.class
            , ExpenseAttendeeEdit.class
            , ExpenseAttendeePreview.class
            , ExpenseDetail.class
            , ExpenseDuplicateAttendee.class
            , ExpenseEntries.class
            , ExpenseEntry.class
            , ExpenseEntryAttendee.class
            , ExpenseHotelWizard.class
            , ExpenseReceipt.class
            , ExpenseReceiptCombined.class
            , ExpenseReportHeader.class
            , ExpensesAndReceipts.class
            , ExpensesAndReceiptsActivity.class
            , ExpenseSendBack.class
            , FileSearchActivity.class
            , ForegroundApp.class
            , Home.class
//            , HotelBookingActivity.class
//            , HotelChoiceDetailsActivity.class
//            , HotelPriceToBeatSearch.class
//            , HotelPriceToBeatSearchResults.class
//            , HotelReserveRoom.class
//            , HotelSearch.class
//            , HotelSearchAndResultActivity.class
//            , HotelSearchNoResults.class
//            , HotelSearchResults.class
//            , HotelSearchRoomDetails.class
//            , HotelSearchRooms.class
//            , HotelVoiceSearchActivity.class
            , ImageActivity.class
            , ImageDetailActivity.class
            , InvoicesWebView.class
            , ListSearch.class
            , LocationCheckIn.class
            , LocationSearch.class
            , LocationSearchActivity.class
            , LocationSearchV1.class
            , Login.class
            , LoginHelp.class
            , LoginHelpTopic.class
            , LoginPasswordActivity.class
            , LogView.class
            , MessageCenter.class
            , OfferList.class
            , OfferWebView.class
            , OffLineUploadList.class
            , OpenSourceLicenseDisplay.class
            , OpenSourceLicenseInfo.class
            , Preferences.class
            , PreLogin.class
            , ProfileInfo.class
            , PurchaseRequestsWebView.class
            , QuickExpense.class
            , RailSearch.class
            , RailSearchDetail.class
            , RailSearchNoResults.class
            , RailSearchProgress.class
            , RailSearchResults.class
            , RailSearchResultsFares.class
            , ReceiptShare.class
            , ReceiptShareStatus.class
            , Register.class
            , RequestEntryActivity.class
            , RequestHeaderActivity.class
            , RequestListActivity.class
            , RequestSummaryActivity.class
            , RestHotelSearch.class
            , RuleViolationSummary.class
            , SegmentDetail.class
            , SegmentList.class
//            ,ShowHotelItinerary.class
//            ,ShowHotelMap.class
            , SimpleWebViewActivity.class
            , Startup.class
            , TAExpensesActivity.class
            , TAItineraryActivity.class
            , TAStopActivity.class
            , TestDriveRegistration.class
            , TestDriveTour.class
            , Tour.class
            , TravelBaseActivity.class
            , TravelCustomFieldSearch.class
            , TravelPointsExplanation.class
            , TravelRequestApprovalsWebView.class
            , TravelViolationsApprovalChoice.class
            , TravelViolationsForManagerApproval.class
            , TripList.class
            , UniversalTour.class
            , ViewActivity.class
            , ViewImage.class
            , VoiceAirSearchActivity.class
            , VoiceCarSearchActivity.class
            , VoiceHotelSearchActivity.class
            /*
            ----Gov Activities----
            ,TaxFormReceiver.class
            ,AccAllocationListActivity.class
            ,AlternateExampleActivity.class
            ,AuthForVchListActivity.class
            ,AuthorizationListActivity.class
            ,BasicListActivity.class
            ,CommentsActivity.class
            ,CorpSsoQueryReceiver.class
            ,CompanyLogin.class
            ,CompanySignOn.class
            ,DevActivity.class
            ,DocInfoFromTripLocator.class
            ,DocumentDetail.class
            ,DocumentListActivity.class
            ,DocumentReceipt.class
            ,ExampleActivity.class
            ,ExceptionsListActivity.class
            ,Expense.class
            ,ExpenseListActivity.class
            ,DeleteExpenseReceiver.class
            ,GovListSearch.class
            ,GovLocationSearch.class
            ,GovRulesActivity.class
            ,OpenOrExistingAuthList.class
            ,PerDiemLocationListActivity.class
            ,ExampleActivity.class
            ,RequestEntryFragment<T.class
            ,RequestPagerAdapter<T.class
            ,StampDocumentActivity.class
            ,StampDocumentListActivity.class
            ,TDYPerDiemLocations.class
            ,TestsActivity.class
            ,TotalsAndTravelsActivity.class
            ,TravelAuthType.class
            ,ReasonCodeReceiver.class
            ,Approval.ItineraryReceiver.class
            ,UnAppliedList.class
            ,VouchersListActivity.class
            ,CreateVoucherReceiver.class
            */
        );


        for (Class<? extends Activity> item : activities) {
            try {
                testActivity(item);
                successfulLaunchActivities.add(item);
            } catch (Exception e) {
                failedActivities.add(item);
            }
        }
        Log.v(Const.LOG_TAG, CLS_TAG + "================================================================");
        Log.v(Const.LOG_TAG, CLS_TAG + " Successful count: [" + successfulLaunchActivities.size() + "]");

        for (Class<? extends Activity> item : successfulLaunchActivities) {
            Log.v(Const.LOG_TAG, CLS_TAG + " Successful Activity: ===" + item.getSimpleName() + "===");
        }

        Log.v(Const.LOG_TAG, CLS_TAG + " Failed count: [" + failedActivities.size() + "]");

        for (Class<? extends Activity> item : failedActivities) {
            Log.v(Const.LOG_TAG, CLS_TAG + " Failed Activity: ###" + item.getSimpleName() + "###");
        }
    }
}
