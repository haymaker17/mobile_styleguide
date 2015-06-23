package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.adapter.ViewPagerAdapter;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceControllerFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItineraryListFragment;
import com.concur.mobile.core.util.Const;

/**
 * Created by D049515 on 15.06.2015.
 */
public class TravelAllowanceActivity extends AppCompatActivity
        implements FixedTravelAllowanceListFragment.IFixedTravelAllowanceSelectedListener, TravelAllowanceControllerFragment.IRequestCallback, IFragmentCallback{

    private static final int REQUEST_CODE_FIXED_TRAVEL_ALLOWANCE_DETAILS = 0x01;
    
    private static final String CONTROLLER_FRAGMENT_TAG = "controllerFragment";

    private String expenseReportKey;

    private TravelAllowanceControllerFragment controller;

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

        controller = (TravelAllowanceControllerFragment) getSupportFragmentManager().findFragmentByTag(
                CONTROLLER_FRAGMENT_TAG);

        if (controller == null) {
            controller = new TravelAllowanceControllerFragment();
            getSupportFragmentManager().beginTransaction().add(controller, CONTROLLER_FRAGMENT_TAG).commit();
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.itin_travel_allowances);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

    }


    @Override
    public void onRequestSuccess(Bundle resultData) {
        TravelAllowanceItineraryListFragment itinListFrag = viewPagerAdapter.getTravelAllowanceItineraryFragment();
        if (itinListFrag != null) {
            itinListFrag.onRefreshFinished();
        }
    }

    @Override
    public void onRequestFail(Bundle resultData) {
        TravelAllowanceItineraryListFragment itinListFrag = viewPagerAdapter.getTravelAllowanceItineraryFragment();
        if (itinListFrag != null) {
            itinListFrag.onRefreshFinished();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (message.equals(TravelAllowanceItineraryListFragment.ON_REFRESH_MSG)) {
            controller.refreshItineraries(expenseReportKey, true);
        }
    }
}
