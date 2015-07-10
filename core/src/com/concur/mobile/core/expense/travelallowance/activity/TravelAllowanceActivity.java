package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.adapter.ViewPagerAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.controller.IServiceRequestListener;
import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItineraryListFragment;
import com.concur.mobile.core.util.Const;

/**
 * Created by D049515 on 15.06.2015.
 */
public class TravelAllowanceActivity extends AppCompatActivity
        implements FixedTravelAllowanceListFragment.IFixedTravelAllowanceSelectedListener, IServiceRequestListener, IFragmentCallback{

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
        this.setContentView(R.layout.travel_allowance_activity);

        ConcurCore app = (ConcurCore) getApplication();

        this.itineraryController = app.getTaItineraryController();
        this.itineraryController.registerListener(this);

        this.allowanceController = app.getFixedTravelAllowanceController();
        this.allowanceController.registerListener(this);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.ta_travel_allowances);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getApplicationContext());

        pager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

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
    protected void onDestroy() {
        super.onDestroy();
        this.itineraryController.unregisterListener(this);
        this.allowanceController.unregisterListener(this);
    }

    @Override
    public void sendMessage(String message) {
        if (message.equals(TravelAllowanceItineraryListFragment.ON_REFRESH_MSG)) {
            this.itineraryController.refreshItineraries(expenseReportKey, true);
        }
        if (message.equals(FixedTravelAllowanceListFragment.ON_REFRESH_MSG)) {
            this.allowanceController.refreshFixedTravelAllowances(expenseReportKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestSuccess(final String controllerTag) {
        Log.d(Const.LOG_TAG, CLASS_TAG + ".onRequestSuccess: " + controllerTag);
        if (TravelAllowanceItineraryController.CONTROLLER_TAG.equals(controllerTag)) {
            TravelAllowanceItineraryListFragment itinListFrag = viewPagerAdapter.getTravelAllowanceItineraryFragment();
            if (itinListFrag != null) {
                itinListFrag.onRefreshFinished();
            }
        }
        if (FixedTravelAllowanceController.CONTROLLER_TAG.equals(controllerTag)) {
            FixedTravelAllowanceListFragment allowanceFrag = viewPagerAdapter.getFixedTravelAllowanceFragment();
            if (allowanceFrag != null) {
                allowanceFrag.onRefreshFinished();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestFail(final String controllerTag) {
        if (TravelAllowanceItineraryController.CONTROLLER_TAG.equals(controllerTag)) {
            TravelAllowanceItineraryListFragment itinListFrag = viewPagerAdapter.getTravelAllowanceItineraryFragment();
            if (itinListFrag != null) {
                itinListFrag.onRefreshFinished();
            }
        }
        if (FixedTravelAllowanceController.CONTROLLER_TAG.equals(controllerTag)) {
            FixedTravelAllowanceListFragment allowanceFrag = viewPagerAdapter.getFixedTravelAllowanceFragment();
            if (allowanceFrag != null) {
                allowanceFrag.onRefreshFinished();
            }
        }
    }
}
