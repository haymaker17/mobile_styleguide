package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

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
import com.concur.mobile.core.expense.travelallowance.fragment.SimpleTAItineraryListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItineraryListFragment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D049515 on 15.06.2015.
 */
public class TravelAllowanceActivity extends AppCompatActivity
        implements FixedTravelAllowanceListFragment.IFixedTravelAllowanceSelectedListener, IControllerListener, IFragmentCallback{

    private static final String CLASS_TAG = TravelAllowanceActivity.class.getSimpleName();

    private static final int REQUEST_CODE_FIXED_TRAVEL_ALLOWANCE_DETAILS = 0x01;

    private String expenseReportKey;

    private TravelAllowanceItineraryController itineraryController;
    private FixedTravelAllowanceController allowanceController;

    private ViewPagerAdapter viewPagerAdapter;

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

        this.itineraryController = app.getTaItineraryController();
        this.itineraryController.registerListener(this);

        this.allowanceController = app.getFixedTravelAllowanceController();
        this.allowanceController.registerListener(this);

        if (getIntent().hasExtra(BundleId.EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ta_travel_allowances);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        List<ViewPagerAdapter.ViewPagerItem> pagerItemList = new ArrayList<ViewPagerAdapter.ViewPagerItem>();
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getApplicationContext(), getViewPagerItemList());

        pager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

    }
    
    private List<ViewPagerAdapter.ViewPagerItem> getViewPagerItemList() {

        List<ViewPagerAdapter.ViewPagerItem> list = new ArrayList<>();

        ViewPagerAdapter.ViewPagerItem adjustmentFrag = new ViewPagerAdapter.ViewPagerItem(
                getString(R.string.ta_adjustments), FixedTravelAllowanceListFragment.class, null);
        list.add(adjustmentFrag);

        if (getIntent().getExtras().getBoolean(BundleId.IS_EDIT_MODE)
                && !getIntent().getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false)) {
            Bundle arguments = new Bundle();
            ArrayList<Itinerary> itinList = new ArrayList<>(itineraryController.getItineraryList());
            arguments.putSerializable(BundleId.ITINERARY_LIST, itinList);
            arguments.putString(BundleId.EXPENSE_REPORT_KEY, getIntent().getStringExtra(BundleId.EXPENSE_REPORT_KEY));
            arguments.putString(BundleId.EXPENSE_REPORT_NAME, getIntent().getStringExtra(BundleId.EXPENSE_REPORT_NAME));
            arguments.putBoolean(BundleId.EXPENSE_REPORT_IS_SUBMITTED,
                    getIntent().getBooleanExtra(BundleId.EXPENSE_REPORT_IS_SUBMITTED, false));
            ViewPagerAdapter.ViewPagerItem itinFrag = new ViewPagerAdapter.ViewPagerItem(
                    getString(R.string.itin_itineraries), SimpleTAItineraryListFragment.class, arguments);
            list.add(itinFrag);
        } else {
            ViewPagerAdapter.ViewPagerItem itinFrag = new ViewPagerAdapter.ViewPagerItem(
                    getString(R.string.itin_itineraries), TravelAllowanceItineraryListFragment.class, null);
            list.add(itinFrag);
        }
        return list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleTAItineraryListFragment simpleTaListFrag = getSimpleTAItineraryListFragment();
        if (simpleTaListFrag != null) {
            Bundle bundle = new Bundle();
            ArrayList<Itinerary> arrayList = new ArrayList<Itinerary>(itineraryController.getItineraryList());
            bundle.putSerializable(BundleId.ITINERARY_LIST, arrayList);
            simpleTaListFrag.onRefreshFinished(bundle);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (itineraryController != null) {
            this.itineraryController.unregisterListener(this);
        }

        if (this.allowanceController != null) {
            this.allowanceController.unregisterListener(this);
        }
    }

    @Override
    public void sendMessage(String message) {
        if (message.equals(TravelAllowanceItineraryListFragment.ON_REFRESH_MSG)) {
            this.itineraryController.refreshItineraries(expenseReportKey, true);
        }
        if (message.equals(FixedTravelAllowanceListFragment.ON_REFRESH_MSG)) {
            this.allowanceController.refreshFixedTravelAllowances(expenseReportKey);
        }
        if (message.equals(SimpleTAItineraryListFragment.ON_REFRESH_MSG_ITIN)) {
            this.itineraryController.refreshItineraries(expenseReportKey, false);
        }
        if (message.equals(SimpleTAItineraryListFragment.ON_REFRESH_MSG_TA)) {
            this.allowanceController.refreshFixedTravelAllowances(expenseReportKey);
        }
    }

    @Override
    public void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result) {
        if (controller instanceof TravelAllowanceItineraryController) {
            TravelAllowanceItineraryListFragment itinListFrag = viewPagerAdapter.getTravelAllowanceItineraryFragment();
            if (itinListFrag != null) {
                itinListFrag.onRefreshFinished();
            }

            SimpleTAItineraryListFragment simpleList = getSimpleTAItineraryListFragment();
            if (simpleList != null) {
                simpleList.onRefreshFinished(result);
            }
        }

        if (controller instanceof FixedTravelAllowanceController) {
            FixedTravelAllowanceListFragment allowanceFrag = viewPagerAdapter.getFixedTravelAllowanceFragment();
            if (allowanceFrag != null) {
                allowanceFrag.onRefreshFinished();
            }
        }
    }

    public SimpleTAItineraryListFragment getSimpleTAItineraryListFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) {
            return null;
        }
        for (Fragment fragment : fragments) {
            if (fragment instanceof SimpleTAItineraryListFragment) {
                return (SimpleTAItineraryListFragment) fragment;
            }
        }
        return null;
    }
}