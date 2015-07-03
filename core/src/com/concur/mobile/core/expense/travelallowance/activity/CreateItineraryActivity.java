package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.adapter.ViewPagerAdapter;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.travel.hotel.activity.HotelSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialogV1;

import java.util.Calendar;
import java.util.Locale;

public class CreateItineraryActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLS_TAG = CreateItineraryActivity.class
            .getSimpleName();

    private String expenseReportKey;

    private static final int CHECK_IN_DATE_DIALOG = 0;
    private static final int CHECK_OUT_DATE_DIALOG = 1;
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT =
            HotelSearch.class.getSimpleName() + ".calendar.dialog.fragment";
    private CalendarPickerDialogV1 calendarDialog;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        this.setContentView(R.layout.ta_itinerary_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("@New Itinerary@");

        }


        View checkOutDateView = findViewById(R.id.hotel_search_check_out);
        View checkInDateView = findViewById(R.id.hotel_search_label_checkin);

        // Hook up the handlers
        checkInDateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showCalendarDialog(CHECK_IN_DATE_DIALOG);
            }
        });
        checkOutDateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showCalendarDialog(CHECK_OUT_DATE_DIALOG);
            }
        });
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
    }
}