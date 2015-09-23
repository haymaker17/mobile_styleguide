package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.adapter.ViewPagerAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ControllerAction;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.IController;
import com.concur.mobile.core.expense.travelallowance.controller.IControllerListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.MessageDialogFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.ServiceRequestListenerFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.SimpleTAItineraryListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItineraryListFragment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D049515 on 15.06.2015.
 */
@EventTracker.EventTrackerClassName(getClassName = TravelAllowanceActivity.SCREEN_NAME_TRAVEL_ALLOWANCE_MAIN)
public class TravelAllowanceActivity extends TravelAllowanceBaseActivity
        implements FixedTravelAllowanceListFragment.IFixedTravelAllowanceSelectedListener, IControllerListener, IFragmentCallback, PopupMenu.OnMenuItemClickListener {

    public static final String SCREEN_NAME_TRAVEL_ALLOWANCE_MAIN = "Tab-View: Expense-Report-TravelAllowances";

    private static final String CLASS_TAG = TravelAllowanceActivity.class.getSimpleName();

    private static final String TAG_DELETE_DIALOG_FRAGMENT = "delete.dialog.fragment";
    private static final String TAG_UNASSIGN_DIALOG_FRAGMENT = "unassign.dialog.fragment";

    private static final String MSG_DIALOG_REMOVE_POSITIVE = "dialog.remove.positive";
    private static final String MSG_DIALOG_REMOVE_NEUTRAL = "dialog.remove.neutral";
    private static final String MSG_DIALOG_DELETE_POSITIVE = "dialog.delete.positive";
    private static final String MSG_DIALOG_DELETE_NEUTRAL = "dialog.delete.neutral";

    private String expenseReportKey;

    private ViewPagerAdapter viewPagerAdapter;

    private boolean isInApproval;

    private boolean itinRefreshDone = false;
    private boolean fixedTaRefreshDone = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFixedTravelAllowanceSelected(FixedTravelAllowance allowance) {

        Intent intent = new Intent(this, FixedTravelAllowanceDetailsActivity.class);

        if (getIntent().hasExtra(BundleId.IS_EDIT_MODE)) {
            intent.putExtra(BundleId.IS_EDIT_MODE, getIntent().getExtras().getBoolean(BundleId.IS_EDIT_MODE));
        }

        if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED)) {
            intent.putExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, getIntent().getExtras().getBoolean(BundleId.EXPENSE_REPORT_IS_SUBMITTED));
        }

        if(!StringUtilities.isNullOrEmpty(expenseReportKey) ){
            intent.putExtra(BundleId.EXPENSE_REPORT_KEY, expenseReportKey);
        }

        if (allowance != null) {
            intent.putExtra(FixedTravelAllowanceDetailsActivity.INTENT_EXTRA_KEY_FIXED_TRAVEL_ALLOWANCE, allowance);
            startActivityForResult(intent, REQUEST_CODE_FIXED_TRAVEL_ALLOWANCE_DETAILS);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.ta_travel_allowance_activity);

        ConcurCore app = (ConcurCore) getApplication();

        this.itineraryController = app.getTaController().getTaItineraryController();
        this.fixedTaController = app.getTaController().getFixedTravelAllowanceController();

        registerControllerActionListener();

        if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY);
        }

        initializeToolbar(R.string.ta_travel_allowances);

        isInApproval = getIntent().getBooleanExtra(BundleId.IS_IN_APPROVAL, false);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        List<ViewPagerAdapter.ViewPagerItem> pagerItemList = new ArrayList<ViewPagerAdapter.ViewPagerItem>();
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getApplicationContext(), getViewPagerItemList());

        pager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_INSTANCE_STATE_ID_FIXED_TA_REFRESH_DONE, fixedTaRefreshDone);
        outState.putBoolean(SAVED_INSTANCE_STATE_ID_ITIN_REFRESH_DONE, itinRefreshDone);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fixedTaRefreshDone = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_ID_FIXED_TA_REFRESH_DONE, false);
        itinRefreshDone = savedInstanceState.getBoolean(SAVED_INSTANCE_STATE_ID_ITIN_REFRESH_DONE, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_UPDATE_ITINERARY
                || requestCode == REQUEST_CODE_FIXED_TRAVEL_ALLOWANCE_DETAILS) {//bypass
            if (data != null && data.getBooleanExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, false)) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
                setResult(RESULT_OK, resultIntent);
            }

            if (data != null && data.getBooleanExtra(BundleId.REFRESH_FIXED_TA, false)) {
                this.fixedTaController.refreshFixedTravelAllowances(expenseReportKey, null);
                FixedTravelAllowanceListFragment fixedTaListFrag = (FixedTravelAllowanceListFragment) getFragmentByClass(
                        FixedTravelAllowanceListFragment.class);
                if (fixedTaListFrag != null) {
                    fixedTaListFrag.showRefreshIndicator();
                }
            }
        }
    }

    private List<ViewPagerAdapter.ViewPagerItem> getViewPagerItemList() {

        List<ViewPagerAdapter.ViewPagerItem> list = new ArrayList<>();

        ViewPagerAdapter.ViewPagerItem adjustmentFrag = new ViewPagerAdapter.ViewPagerItem(
                getString(R.string.ta_adjustments), FixedTravelAllowanceListFragment.class, null);
        list.add(adjustmentFrag);

        boolean isTraveller = getIntent().getExtras().getBoolean(BundleId.IS_EDIT_MODE, true);
        if (isTraveller && !getIntent().getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false)) {
            Bundle arguments = new Bundle();
            ArrayList<Itinerary> itinList = new ArrayList<>(itineraryController.getItineraryList());
            arguments.putSerializable(BundleId.ITINERARY_LIST, itinList);
            arguments.putString(BundleId.EXPENSE_REPORT_KEY, getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY));
            arguments.putString(BundleId.EXPENSE_REPORT_NAME, getIntent().getStringExtra(BundleId.EXPENSE_REPORT_NAME));
            arguments.putBoolean(BundleId.EXPENSE_REPORT_IS_SUBMITTED,
                    getIntent().getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false));
            arguments.putSerializable(BundleId.EXPENSE_REPORT_DATE, getIntent().getSerializableExtra(BundleId.EXPENSE_REPORT_DATE));
            ViewPagerAdapter.ViewPagerItem itinFrag = new ViewPagerAdapter.ViewPagerItem(
                    getString(R.string.itin_itineraries), SimpleTAItineraryListFragment.class, arguments);
            list.add(itinFrag);
        } else {
            Bundle arguments = new Bundle();
            arguments.putBoolean(BundleId.IS_EDIT_MODE, isTraveller);
            ViewPagerAdapter.ViewPagerItem itinFrag = new ViewPagerAdapter.ViewPagerItem(
                    getString(R.string.itin_itineraries), TravelAllowanceItineraryListFragment.class, arguments);
            list.add(itinFrag);
        }
        return list;
    }


    @Override
    protected void onResume() {
        super.onResume();
        SimpleTAItineraryListFragment simpleTaListFrag = (SimpleTAItineraryListFragment) getFragmentByClass(SimpleTAItineraryListFragment.class);
        if (simpleTaListFrag != null) {
            Bundle bundle = new Bundle();
            ArrayList<Itinerary> arrayList = new ArrayList<Itinerary>(itineraryController.getItineraryList());
            bundle.putSerializable(BundleId.ITINERARY_LIST, arrayList);
            simpleTaListFrag.onRefreshFinished(bundle);
        }
        FixedTravelAllowanceListFragment fixedTaListFrag = (FixedTravelAllowanceListFragment) getFragmentByClass(
                FixedTravelAllowanceListFragment.class);
        if (fixedTaListFrag != null) {
            // fixedTaListFrag.onRefreshFinished();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerControllerActionListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterControllerActionListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterControllerActionListener();
    }

    private void registerControllerActionListener() {
        if (itineraryController != null) {
            this.itineraryController.registerListener(this);
        }
        if (this.fixedTaController != null) {
            this.fixedTaController.registerListener(this);
        }
    }

    private void unregisterControllerActionListener() {
        if (itineraryController != null) {
            this.itineraryController.unregisterListener(this);
        }
        if (this.fixedTaController != null) {
            this.fixedTaController.unregisterListener(this);
        }
    }

    @Override
    public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleFragmentMessage", "message = " + fragmentMessage));
        if (TravelAllowanceItineraryListFragment.ON_REFRESH_MSG.equals(fragmentMessage)) {
            this.itineraryController.refreshItineraries(expenseReportKey, isInApproval, null);
        }
        if (FixedTravelAllowanceListFragment.ON_REFRESH_MSG.equals(fragmentMessage)) {
            fixedTaController.refreshFixedTravelAllowances(expenseReportKey, null);
        }
        if (SimpleTAItineraryListFragment.ON_REFRESH_MSG_ITIN.equals(fragmentMessage)) {
            itineraryController.refreshItineraries(expenseReportKey, isInApproval, null);
        }
        if (SimpleTAItineraryListFragment.ON_REFRESH_MSG_TA.equals(fragmentMessage)) {
            fixedTaController.refreshFixedTravelAllowances(expenseReportKey, null);
        }

        if (MSG_DELETE_ITIN_SUCCESS.equals(fragmentMessage)) {
            refreshFixedTravelAllowances(expenseReportKey);
            refreshItineraries(expenseReportKey, isInApproval);
            Toast.makeText(this, R.string.general_delete_success, Toast.LENGTH_SHORT).show();
        }

        if (MSG_DELETE_ITIN_FAILED.equals(fragmentMessage)) {
            dismissProgressDialog();
            Toast.makeText(this, R.string.general_delete_fail, Toast.LENGTH_SHORT).show();
        }

        if (MSG_UNASSIGN_ITIN_FAILED.equals(fragmentMessage)) {
            dismissProgressDialog();
        }

        if (MSG_UNASSIGN_ITIN_SUCCESS.equals(fragmentMessage)) {
            refreshAssignableItineraries(expenseReportKey);
            refreshFixedTravelAllowances(expenseReportKey);
            refreshItineraries(expenseReportKey, isInApproval);
        }

        if (MSG_REFRESH_ITIN_FINISHED.equals(fragmentMessage)) {
            itinRefreshDone = true;
            onRefreshFixedTAandItin();
        }

        if (MSG_REFRESH_TA_FINISHED.equals(fragmentMessage)) {
            fixedTaRefreshDone = true;
            onRefreshFixedTAandItin();
        }
        if (MSG_DIALOG_DELETE_POSITIVE.equals(fragmentMessage) && extras != null) {
            Itinerary itinerary = (Itinerary) extras.getSerializable(BundleId.ITINERARY);
            if (StringUtilities.isNullOrEmpty(itinerary.getItineraryID())) {
                SimpleTAItineraryListFragment listFrag = (SimpleTAItineraryListFragment) getFragmentByClass(
                        SimpleTAItineraryListFragment.class);
                if (listFrag != null) {
                    listFrag.deleteItinerary(itinerary);
                }
            } else {
                ServiceRequestListenerFragment f = getServiceRequestListenerFragment(TAG_DELETE_ITIN_LISTENER,
                        MSG_DELETE_ITIN_SUCCESS, MSG_DELETE_ITIN_FAILED);
                showProgressDialog();
                itineraryController.executeDeleteItinerary(itinerary, f);
            }
        }
        if (MSG_DIALOG_REMOVE_POSITIVE.equals(fragmentMessage) && extras != null) {
            Itinerary itinerary = (Itinerary) extras.getSerializable(BundleId.ITINERARY);
            if (StringUtilities.isNullOrEmpty(itinerary.getItineraryID())) {
                SimpleTAItineraryListFragment listFrag = (SimpleTAItineraryListFragment) getFragmentByClass(
                        SimpleTAItineraryListFragment.class);
                if (listFrag != null) {
                    listFrag.deleteItinerary(itinerary);
                }
            } else {
                ServiceRequestListenerFragment f = getServiceRequestListenerFragment(TAG_UNASSIGN_REQUEST_LISTENER,
                        MSG_UNASSIGN_ITIN_SUCCESS, MSG_UNASSIGN_ITIN_FAILED);
                showProgressDialog();
                itineraryController.unassignItinerary(expenseReportKey, itinerary.getItineraryID(), f);
            }
        }
    }

    private void onRefreshFixedTAandItin() {
        if (itinRefreshDone && fixedTaRefreshDone) {
            itinRefreshDone = false;
            fixedTaRefreshDone = false;
            dismissProgressDialog();
        }
    }

    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {

        if (isSuccess) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished", "controller = " + controller.getClass().getSimpleName() +
                    ", action = " + action + ", isSuccess = " + isSuccess));
            if (controller instanceof TravelAllowanceItineraryController) {
                TravelAllowanceItineraryListFragment itinListFrag = (TravelAllowanceItineraryListFragment) getFragmentByClass(
                        TravelAllowanceItineraryListFragment.class);
                if (itinListFrag != null) {
                    itinListFrag.onRefreshFinished();
                }

                SimpleTAItineraryListFragment simpleList = (SimpleTAItineraryListFragment) getFragmentByClass(SimpleTAItineraryListFragment.class);
                if (simpleList != null) {
                    simpleList.onRefreshFinished(result);
                }
            }

            if (controller instanceof FixedTravelAllowanceController) {
                FixedTravelAllowanceListFragment allowanceFrag = (FixedTravelAllowanceListFragment) getFragmentByClass(
                        FixedTravelAllowanceListFragment.class);
                if (allowanceFrag != null) {
                    allowanceFrag.onRefreshFinished();
                }
            }
        } else {//TODO TA: Handle error situation
            Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "actionFinished", "controller = " + controller.getClass().getSimpleName() +
                    ", action = " + action + ", isSuccess = " + isSuccess));
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (R.id.delete == item.getItemId()) {
            Intent intent = item.getIntent();
            Itinerary itinerary = (Itinerary) intent.getSerializableExtra(BundleId.ITINERARY);
            showDeleteItineraryDialog(itinerary);

            // needs to be always true because the intent is used for passing additional similar to menuInfo.
            return true;
        }

        if (R.id.remove == item.getItemId()) {
            Intent intent = item.getIntent();
            Itinerary itinerary = (Itinerary) intent.getSerializableExtra(BundleId.ITINERARY);
            showRemoveItineraryDialog(itinerary);

            // needs to be always true because the intent is used for passing additional similar to menuInfo.
            return true;
        }

        return false;
    }

    private void showDeleteItineraryDialog(final Itinerary itinerary) {
        Bundle arguments = new Bundle();
        Bundle extras = new Bundle();
        String msgText = getResources().getString(R.string.ta_confirm_delete_itinerary, itinerary.getName());
        extras.putSerializable(BundleId.ITINERARY, itinerary);
        arguments.putString(MessageDialogFragment.MESSAGE_TEXT, msgText);
        arguments.putString(MessageDialogFragment.POSITIVE_BUTTON, MSG_DIALOG_DELETE_POSITIVE);
        arguments.putString(MessageDialogFragment.NEUTRAL_BUTTON, MSG_DIALOG_DELETE_NEUTRAL);
        arguments.putBundle(BundleId.FRAGMENT_MESSAGE_EXTRAS, extras);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(arguments);
        messageDialog.show(getSupportFragmentManager(), TAG_DELETE_DIALOG_FRAGMENT);
    }

    private void showRemoveItineraryDialog(final Itinerary itinerary) {//unassign
        Bundle arguments = new Bundle();
        Bundle extras = new Bundle();
        String msgText = getResources().getString(R.string.ta_confirm_remove, itinerary.getName());
        extras.putSerializable(BundleId.ITINERARY, itinerary);
        arguments.putString(MessageDialogFragment.MESSAGE_TEXT, msgText);
        arguments.putString(MessageDialogFragment.POSITIVE_BUTTON, MSG_DIALOG_REMOVE_POSITIVE);
        arguments.putString(MessageDialogFragment.NEUTRAL_BUTTON, MSG_DIALOG_REMOVE_NEUTRAL);
        arguments.putBundle(BundleId.FRAGMENT_MESSAGE_EXTRAS, extras);
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(arguments);
        messageDialog.show(getSupportFragmentManager(), TAG_UNASSIGN_DIALOG_FRAGMENT);
    }

}