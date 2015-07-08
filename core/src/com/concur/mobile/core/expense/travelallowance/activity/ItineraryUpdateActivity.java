package com.concur.mobile.core.expense.travelallowance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ItineraryUpdateController;
import com.concur.mobile.core.travel.hotel.activity.HotelSearch;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialogV1;

import java.util.Calendar;

public class ItineraryUpdateActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = ItineraryUpdateActivity.class
            .getSimpleName();



    private static final int CHECK_IN_DATE_DIALOG = 0;
    private static final int CHECK_OUT_DATE_DIALOG = 1;
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT =
            HotelSearch.class.getSimpleName() + ".calendar.dialog.fragment";

    private String expenseReportKey;
    private ItineraryUpdateController itineraryUpdateController;
    private View.OnClickListener onTimeClickListener;
    private View.OnClickListener onDateClickListener;
    private View.OnClickListener onLocationClickListener;

    private CalendarPickerDialogV1 calendarDialog;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConcurCore app = (ConcurCore) getApplication();
        this.itineraryUpdateController = app.getItineraryUpdateController();

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        onDateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        onTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        onLocationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ItineraryUpdateActivity.this, ListSearch.class);
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_IS_MRU, true);
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, "LocName");
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE, "RPTINFO");
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, getText(R.string.location)
                        .toString());
                startActivityForResult(locationIntent, Const.REQUEST_CODE_LOCATION);
            }
        };

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            String expenseReportName = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
            this.setContentView(R.layout.itin_update_activity);
            itineraryUpdateController.refreshCompactItinerary(expenseReportName);

            ListView listView = (ListView) findViewById(R.id.list_view);
            if (listView != null) {
                listView.setAdapter(new ItineraryUpdateListAdapter(this, onLocationClickListener,
                        onDateClickListener, onTimeClickListener));
            }
            renderDefaultValues();
        } else {//Temporary
            this.setContentView(R.layout.ta_itinerary_create);
            renderItineraryNameV1();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("@New Itinerary@");

        }


        View vDepartureDate = findViewById(R.id.departure_date);
        View vArrivalDate = findViewById(R.id.arrival_date);

        // Hook up the handlers
        if (vArrivalDate != null) {
            vArrivalDate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showCalendarDialog(CHECK_IN_DATE_DIALOG);
                }
            });
        }
        if (vDepartureDate != null) {
            vDepartureDate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showCalendarDialog(CHECK_OUT_DATE_DIALOG);
                }
            });
        }
    }


    private void onSave() {

    }

    private void renderDefaultValues() {
        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
        if (etItinerary != null) {
            etItinerary.setText(itineraryUpdateController.getCompactItinerary().getName());
        }
    }

    private void renderItineraryNameV1() {
        View vItineraryName = this.findViewById(R.id.itinerary_name);
        if (vItineraryName != null) {
            TextView tvLabel = (TextView) vItineraryName.findViewById(R.id.field_name);
            if (tvLabel != null) {
                tvLabel.setText("@Itinerary Name@");
            }
            EditText etName = (EditText) vItineraryName.findViewById(R.id.field_value);
            if (etName != null) {
                etName.setText("@Expense Report Default@");
            }
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
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itinerary_save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menuSave) {
            onSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}