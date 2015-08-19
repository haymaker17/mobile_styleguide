package com.concur.mobile.corp.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.ApptentiveListActivity;
import com.apptentive.android.sdk.ViewActivity;
import com.concur.mobile.base.util.Const;
import com.concur.mobile.core.activity.*;
import com.concur.mobile.core.expense.activity.*;
import com.concur.mobile.core.expense.charge.activity.*;
import com.concur.mobile.core.expense.receiptstore.activity.*;
import com.concur.mobile.core.expense.report.activity.*;
import com.concur.mobile.core.expense.report.approval.activity.*;
import com.concur.mobile.core.invoice.activity.*;
import com.concur.mobile.core.request.activity.*;
import com.concur.mobile.core.travel.activity.*;
import com.concur.mobile.core.travel.air.activity.*;
import com.concur.mobile.core.travel.approval.activity.RuleViolationSummary;
import com.concur.mobile.core.travel.car.activity.*;
import com.concur.mobile.core.travel.hotel.activity.*;
import com.concur.mobile.core.travel.rail.activity.*;
import com.concur.mobile.core.travel.request.activity.TravelRequestApprovalsWebView;
import com.concur.mobile.core.widget.FileSearchActivity;
import com.concur.mobile.corp.activity.*;
import com.concur.mobile.platform.ui.travel.hotel.activity.*;
import com.concur.mobile.platform.ui.travel.hotel.maps.ShowHotelMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.ActivityController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Config(application = CorporateTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CorpTestRunner.class)
public class CorporateTestSuite {

    private static Boolean testActivities = true;
    private static String CLS_TAG = CorporateTestSuite.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testEmpty() {

    }

    //@Test
    public void testExpenseItListBackgroundRefresh() {
        if (!testActivities) {
            return;
        }
        List<Class<? extends Activity>> failedActivities = new ArrayList<>();
        List<Class<? extends Activity>> successfulLaunchActivities = new ArrayList<>();
        List<Class<? extends Activity>> activities = Arrays.asList(
            AgencyInformation.class
            ,AirFlightDetail.class
            ,AirPriceToBeatSearch.class
            ,AirPriceToBeatSearchResults.class
            ,AirResultsList.class
            ,AirSearch.class
            ,AirSearchNoResults.class
            ,AirSearchProgress.class
            ,AirSearchResultsSummary.class
            ,AlternativeAirScheduleList.class
            ,Approval.class
            ,ApptentiveActivity.class
            ,ApptentiveListActivity.class
            ,AttendeeSearch.class
            ,BaseActivity.class
            ,BaseActivity.class
            ,CarSearch.class
            ,CarSearchDetail.class
            ,CarSearchNoResults.class
            ,CarSearchProgress.class
            ,CarSearchResults.class
            ,CompanyCodeLoginActivity.class
            ,CompanySignOnActivity.class
            ,EmailLookupActivity.class
            ,ExpenseApproval.class
            ,ExpenseAttendeeEdit.class
            ,ExpenseAttendeePreview.class
            ,ExpenseDetail.class
            ,ExpenseDuplicateAttendee.class
            ,ExpenseEntries.class
            ,ExpenseEntry.class
            ,ExpenseEntryAttendee.class
            ,ExpenseHotelWizard.class
            ,ExpenseItDetailActivity.class
            ,ExpenseReceipt.class
            ,ExpenseReceiptCombined.class
            ,ExpenseReportHeader.class
            ,ExpensesAndReceipts.class
            ,ExpensesAndReceiptsActivity.class
            ,ExpenseSendBack.class
            ,FileSearchActivity.class
            ,ForegroundApp.class
            ,Home.class
            ,HotelBookingActivity.class
            ,HotelChoiceDetailsActivity.class
            ,HotelPriceToBeatSearch.class
            ,HotelPriceToBeatSearchResults.class
            ,HotelReserveRoom.class
            ,HotelSearch.class
            ,HotelSearchAndResultActivity.class
            ,HotelSearchNoResults.class
            ,HotelSearchResults.class
            ,HotelSearchRoomDetails.class
            ,HotelSearchRooms.class
            ,HotelVoiceSearchActivity.class
            ,ImageActivity.class
            ,ImageDetailActivity.class
            ,InvoicesWebView.class
            ,ListSearch.class
            ,LocationCheckIn.class
            ,LocationSearch.class
            ,LocationSearchActivity.class
            ,LocationSearchV1.class
            ,Login.class
            ,LoginHelp.class
            ,LoginHelpTopic.class
            ,LoginPasswordActivity.class
            ,LogView.class
            ,MessageCenter.class
            ,OfferList.class
            ,OfferWebView.class
            ,OffLineUploadList.class
            ,OpenSourceLicenseDisplay.class
            ,OpenSourceLicenseInfo.class
            ,Preferences.class
            ,PreLogin.class
            ,ProfileInfo.class
            ,PurchaseRequestsWebView.class
            ,QuickExpense.class
            ,RailSearch.class
            ,RailSearchDetail.class
            ,RailSearchNoResults.class
            ,RailSearchProgress.class
            ,RailSearchResults.class
            ,RailSearchResultsFares.class
            ,ReceiptShare.class
            ,ReceiptShareStatus.class
            ,ExpenseItReceiptView.class
            ,Register.class
            ,RequestEntryActivity.class
            ,RequestHeaderActivity.class
            ,RequestListActivity.class
            ,RequestSummaryActivity.class
            ,RestHotelSearch.class
            ,RuleViolationSummary.class
            ,SegmentDetail.class
            ,SegmentList.class
            ,ShowHotelItinerary.class
            ,ShowHotelMap.class
            ,SimpleWebViewActivity.class
            ,Startup.class
            ,TAExpensesActivity.class
            ,TAItineraryActivity.class
            ,TAStopActivity.class
            ,TestDriveRegistration.class
            ,TestDriveTour.class
            ,Tour.class
            ,TravelBaseActivity.class
            ,TravelCustomFieldSearch.class
            ,TravelPointsExplanation.class
            ,TravelRequestApprovalsWebView.class
            ,TravelViolationsApprovalChoice.class
            ,TravelViolationsForManagerApproval.class
            ,TripList.class
            ,UniversalTour.class
            ,ViewActivity.class
            ,ViewImage.class
            ,VoiceAirSearchActivity.class
            ,VoiceCarSearchActivity.class
            ,VoiceHotelSearchActivity.class
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

    private static <T extends Activity> void testActivity(Class<T> activityClass) {
        Log.v(Const.LOG_TAG, CLS_TAG + ".testActivity current Activity: ===" + activityClass.getSimpleName() + "===");
        Intent newIt = new Intent();
        Bundle bundle = new Bundle();
        ActivityController<T> activityController = Robolectric.buildActivity(activityClass)
            .withIntent(newIt)
            .create(bundle)
            .start()
            .visible()
            .restoreInstanceState(bundle)
            .resume();
        //Activity runningActivity = activityController.get();
        //runningActivity.finish();
        activityController.saveInstanceState(bundle).pause().stop().destroy();
    }
}
