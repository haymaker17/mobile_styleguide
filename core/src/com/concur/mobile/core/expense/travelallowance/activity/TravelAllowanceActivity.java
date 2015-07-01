package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import com.concur.mobile.core.travel.hotel.activity.HotelSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialogV1;

import java.util.Calendar;

/**
 * Created by D049515 on 15.06.2015.
 */
public class TravelAllowanceActivity extends AppCompatActivity
        implements FixedTravelAllowanceListFragment.IFixedTravelAllowanceSelectedListener, IServiceRequestListener, IFragmentCallback{

    private static final String CLASS_TAG = TravelAllowanceActivity.class.getSimpleName();

    private static final int REQUEST_CODE_FIXED_TRAVEL_ALLOWANCE_DETAILS = 0x01;

    private static final int CHECK_IN_DATE_DIALOG = 0;
    private static final int CHECK_OUT_DATE_DIALOG = 1;
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT =
            HotelSearch.class.getSimpleName() + ".calendar.dialog.fragment";
    private CalendarPickerDialogV1 calendarDialog;

    /**
     * Contains the key identifying the source of this report key. Should be one of
     * <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code>, <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code> or
     * <code>Const.EXPENSE_REPORT_SOURCE_NEW</code>
     */
    private int expenseReportKeySource;
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

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE)) {
            expenseReportKeySource = getIntent().getIntExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, -1);
        }

        if (expenseReportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
            this.setContentView(R.layout.travel_allowance_activity);
        } else {
            this.setContentView(R.layout.ta_itinerary_create);
        }

        ConcurCore app = (ConcurCore) getApplication();

        this.itineraryController = app.getTaItineraryController();
        this.itineraryController.registerListener(this);

        this.allowanceController = app.getFixedTravelAllowanceController();
        this.allowanceController.registerListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.itin_travel_allowances);
        }

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        if (pager != null) {
            viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getApplicationContext());
            pager.setAdapter(viewPagerAdapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(pager);
        }

        if (expenseReportKeySource != Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
            View checkOutDateView = findViewById(R.id.hotel_search_check_out);
            View checkInDateView = findViewById(R.id.hotel_search_label_checkin);

            // Hook up the handlers
            checkInDateView.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    showCalendarDialog(CHECK_IN_DATE_DIALOG);
                }
            });
            checkOutDateView.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    showCalendarDialog(CHECK_OUT_DATE_DIALOG);
                }
            });
        }

    }

    private void showCalendarDialog(int id) {
        Bundle bundle;

        //Temporary for testing
        //TODO: Replace
        Calendar checkInDate = Calendar.getInstance();
        Calendar checkOutDate = Calendar.getInstance();
        boolean isCheckin;

        calendarDialog = new CalendarPickerDialogV1();
        bundle = new Bundle();
        switch (id) {
            case CHECK_IN_DATE_DIALOG: {
                bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_YEAR, checkInDate.get(Calendar.YEAR));
                bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_MONTH, checkInDate.get(Calendar.MONTH));
                bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_DAY, checkInDate.get(Calendar.DAY_OF_MONTH));
                bundle.putInt(CalendarPickerDialogV1.KEY_TEXT_COLOR, Color.parseColor("#a5a5a5"));
                isCheckin = true;
                break;
            }
            case CHECK_OUT_DATE_DIALOG: {
                bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_YEAR, checkOutDate.get(Calendar.YEAR));
                bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_MONTH, checkOutDate.get(Calendar.MONTH));
                bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_DAY, checkOutDate.get(Calendar.DAY_OF_MONTH));
                bundle.putInt(CalendarPickerDialogV1.KEY_TEXT_COLOR, Color.parseColor("#a5a5a5"));

                isCheckin = false;
                break;
            }
        }

        //DialogId = id;
        calendarDialog.setArguments(bundle);
        //calendarDialog.setOnDateSetListener(new HotelDateSetListener(id, isCheckin));
        calendarDialog.show(getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
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
