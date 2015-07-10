package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.Activity;
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

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.travelallowance.adapter.ItineraryUpdateListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.ItineraryUpdateController;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.ui.model.PositionInfoTag;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.ui.common.widget.CalendarPicker;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialogV1;

import java.util.Calendar;
import java.util.Date;

public class ItineraryUpdateActivity extends BaseActivity {

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = ItineraryUpdateActivity.class
            .getSimpleName();


    private static final int DIALOG_ID_DATE_PICKER = 0;

    private static final String TAG_CALENDAR_DIALOG_FRAGMENT =
            CLASS_TAG + ".calendar.dialog.fragment";

    private String expenseReportKey;
    private ItineraryUpdateController updateController;
    private ItineraryUpdateListAdapter adapter;
    private PositionInfoTag currentPosition;
    private View.OnClickListener onTimeClickListener;
    private View.OnClickListener onDateClickListener;
    private View.OnClickListener onLocationClickListener;
    private CalendarPickerDialogV1.OnDateSetListener onDateSetListener;

    private CalendarPickerDialogV1 calendarDialog;


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConcurCore app = (ConcurCore) getApplication();
        app.getTAConfigController().refreshConfiguration();

        this.updateController = app.getItineraryUpdateController();
        String expenseReportName = StringUtilities.EMPTY_STRING;

        this.setContentView(R.layout.itin_update_activity);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            this.expenseReportKey = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        }

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            expenseReportName = getIntent().getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
        }

        updateController.refreshCompactItinerary(expenseReportName);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("@New Itinerary@");

        }

        onDateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                showCalendarDialog(DIALOG_ID_DATE_PICKER);
            }
        };

        onTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                //showCalendarDialog(DIALOG_ID_DATE_PICKER);
            }
        };

        onLocationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionInfoTag tagValue = (PositionInfoTag) v.getTag(R.id.tag_key_position);
                if (tagValue != null) {
                    ItineraryUpdateActivity.this.currentPosition = tagValue;
                }
                Intent locationIntent = new Intent(ItineraryUpdateActivity.this, ListSearch.class);
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_IS_MRU, true);
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, "LocName");
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE, "RPTINFO");
                locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, getText(R.string.location)
                        .toString());
                startActivityForResult(locationIntent, Const.REQUEST_CODE_LOCATION);
            }
        };

        onDateSetListener = new CalendarPickerDialogV1.OnDateSetListener() {
            @Override
            public void onDateSet(CalendarPicker view, int year, int month, int day) {
                if (currentPosition != null) {
                    Date date;
                    Calendar cal = Calendar.getInstance();
                    CompactItinerarySegment segment = updateController.getCompactItinerarySegment(currentPosition.getPosition());
                    if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                        date = segment.getDepartureDateTime();
                    } else {
                        date = segment.getArrivalDateTime();
                    }
                    cal.setTime(date);
                    cal.set(year, month, day);
                    date = cal.getTime();
                    if (currentPosition.getInfo() == PositionInfoTag.INFO_OUTBOUND) {
                        segment.setDepartureDateTime(date);
                    } else {
                        segment.setArrivalDateTime(date);
                    }
                    adapter.notifyDataSetChanged();
                    calendarDialog.dismiss();
                }
            }
        };

        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            adapter = new ItineraryUpdateListAdapter(this, onLocationClickListener,
                    onDateClickListener, onTimeClickListener);
            listView.setAdapter(adapter);
        }
        renderDefaultValues();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Const.REQUEST_CODE_LOCATION:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedListItemKey = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY);
                    //String selectedListItemCode = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE);
                    String selectedListItemText = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_TEXT);
                    if (this.currentPosition != null) {
                        CompactItinerarySegment segment = this.updateController.getCompactItinerarySegment(currentPosition.getPosition());
                        ItineraryLocation itinLocation = new ItineraryLocation();
                        itinLocation.setName(selectedListItemText);
                        itinLocation.setCode(selectedListItemKey);
                        segment.setLocation(itinLocation);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;

            default: break;
        }
    }

    private void renderDefaultValues() {
        EditText etItinerary = (EditText) findViewById(R.id.et_itinerary);
        if (etItinerary != null && updateController.getCompactItinerary() != null) {
            etItinerary.setText(updateController.getCompactItinerary().getName());
        }
    }

    private void showCalendarDialog(int id) {
        Bundle bundle = new Bundle();

        Calendar date = Calendar.getInstance();

        calendarDialog = new CalendarPickerDialogV1();

        if (id == DIALOG_ID_DATE_PICKER) {
            bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_YEAR, date.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_MONTH, date.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialogV1.KEY_INITIAL_DAY, date.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialogV1.KEY_TEXT_COLOR, Color.parseColor("#a5a5a5"));
            calendarDialog.setArguments(bundle);
            calendarDialog.setOnDateSetListener(onDateSetListener);
            calendarDialog.show(getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
        }
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
            updateController.executeSave(this.expenseReportKey);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}