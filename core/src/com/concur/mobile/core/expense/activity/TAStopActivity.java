package com.concur.mobile.core.expense.activity;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.data.SearchListResponse;
import com.concur.mobile.core.expense.ta.service.AsyncReplyAdapter;
import com.concur.mobile.core.expense.ta.service.Itinerary;
import com.concur.mobile.core.expense.ta.service.ItineraryRow;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.platform.util.Format;

public class TAStopActivity extends BaseActivity {

    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = TAStopActivity.class.getSimpleName()
            + ".calendar.dialog.fragment";

    private static final int SEARCH_LIST_REQUEST_CODE_DEPART = 0;
    private static final int SEARCH_LIST_REQUEST_CODE_ARRIVE = 1;

    private static final int DIALOG_ID_BASE = 10030;
    private static final int DEPART_DATE_DIALOG = DIALOG_ID_BASE + 0;
    private static final int DEPART_TIME_DIALOG = DIALOG_ID_BASE + 1;
    private static final int ARRIVE_DATE_DIALOG = DIALOG_ID_BASE + 2;
    private static final int ARRIVE_TIME_DIALOG = DIALOG_ID_BASE + 3;

    private static final String KEY_DIALOG_ID = "key.dialog.id";
    private static final String KEY_DEPARTURE_DATE = "key.departure.date";
    private static final String KEY_ARRIVAL_DATE = "key.arrival.date";

    private static final int DIALOG_SAVING = 0;

    private BaseAsyncResultReceiver asyncReceiver; // needs to be a member otherwise the weakreference sometimes lets go too
                                                   // quickly (?)
    private ItineraryRow itineraryRow;
    private final View.OnClickListener onClickListener = new OnClickListener();
    private final Calendar departDateTime = new GregorianCalendar();
    private final Calendar arriveDateTime = new GregorianCalendar();
    private int DialogId = -1;
    private CalendarPickerDialog calendarDialog;

    private ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ta_stop_activity);

        getSupportActionBar().setTitle(R.string.itin_edit_stop);

        // TODO: refresh from savedInstanceState

        ConcurCore core = (ConcurCore) getApplication();
        itineraryRow = core.getSelectedTAItineraryRow();

        initView();

        calendarDialog = (CalendarPickerDialog) getSupportFragmentManager().findFragmentByTag(
                TAG_CALENDAR_DIALOG_FRAGMENT);
        if (calendarDialog != null) {
            if (savedInstanceState.containsKey(KEY_DIALOG_ID)) {
                switch (savedInstanceState.getInt(KEY_DIALOG_ID)) {
                case DEPART_DATE_DIALOG:
                    calendarDialog.setOnDateSetListener(new DateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID),
                            (Calendar) savedInstanceState.getSerializable(KEY_DEPARTURE_DATE)));
                    break;
                case ARRIVE_DATE_DIALOG:
                    calendarDialog.setOnDateSetListener(new DateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID),
                            (Calendar) savedInstanceState.getSerializable(KEY_ARRIVAL_DATE)));
                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Blow up the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.itinerary_save_menu, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.menuSave) {
            save();
            return true;
        }
        return false;
    }

    private void initView() {
        if (itineraryRow.getDepartDateTime() != null) {
            departDateTime.setTime(itineraryRow.getDepartDateTime());
        }
        if (itineraryRow.getArrivalDateTime() != null) {
            arriveDateTime.setTime(itineraryRow.getArrivalDateTime());
        }

        setFieldName(R.id.editDepartLocation, R.string.itin_depart_from);
        setFieldName(R.id.editArriveLocation, R.string.itin_arrive_in);
        setFieldName(R.id.editDepartDate, R.string.date);
        setFieldName(R.id.editDepartTime, R.string.general_time);
        setFieldName(R.id.editArriveDate, R.string.date);
        setFieldName(R.id.editArriveTime, R.string.general_time);

        findViewById(R.id.editDepartDate).setOnClickListener(onClickListener);
        findViewById(R.id.editDepartTime).setOnClickListener(onClickListener);
        findViewById(R.id.editDepartLocation).setOnClickListener(onClickListener);
        findViewById(R.id.editArriveDate).setOnClickListener(onClickListener);
        findViewById(R.id.editArriveTime).setOnClickListener(onClickListener);
        findViewById(R.id.editArriveLocation).setOnClickListener(onClickListener);

        updateLocationViews();
        updateDateTimeViews();
    }

    private void updateLocationViews() {
        setFieldValue(R.id.editDepartLocation, itineraryRow.getDepartLocation());
        setFieldValue(R.id.editArriveLocation, itineraryRow.getArrivalLocation());
    }

    private void updateDateTimeViews() {
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);

        setFieldValue(R.id.editDepartDate,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, departDateTime));
        setFieldValue(R.id.editArriveDate,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, arriveDateTime));

        setFieldValue(R.id.editDepartTime, Format.safeFormatCalendar(timeFormat, departDateTime));
        setFieldValue(R.id.editArriveTime, Format.safeFormatCalendar(timeFormat, arriveDateTime));
    }

    // Copied from AirSeach.java. Do we need a common base class?
    protected void setFieldName(int parentView, int textId) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_name));
        tv.setText(textId);
    }

    protected void setFieldValue(int parentView, CharSequence text) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_value));
        tv.setText(text);
    }

    public void save() {
        asyncReceiver = new BaseAsyncResultReceiver(new Handler());
        asyncReceiver.setListener(new GetTAItinerariesListener());

        ConcurCore core = (ConcurCore) getApplication();
        Itinerary itin = core.getTAItinerary();
        itineraryRow.setDepartDateTime(departDateTime.getTime());
        itineraryRow.setArrivalDateTime(arriveDateTime.getTime());
        showDialog(DIALOG_SAVING);
       // new SaveItineraryRequest(getApplicationContext(), 1, asyncReceiver, itin, itineraryRow).execute();
    }

    protected class GetTAItinerariesListener extends AsyncReplyAdapter {

        public GetTAItinerariesListener() {
            super(TAStopActivity.this);
        }

        @Override
        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(CoreAsyncRequestTask.IS_SUCCESS, false);

            if (success) {
                ConcurCore core = (ConcurCore) getApplication();
                Itinerary itinerary = core.getTAItinerary();
                Log.e(Const.LOG_TAG, "Got Itin response: " + itinerary);
                for (ItineraryRow row : itinerary.getItineraryRows()) {
                    if (ItineraryRow.STATUS_FAILURE.equals(row.getStatus())) {
                        Toast t = Toast.makeText(TAStopActivity.this, row.getStatusText(), Toast.LENGTH_LONG);
                        t.show();
                        break;
                    } else if (row.getIrKey().equals(itineraryRow.getIrKey()) || row.getIrKey() != null
                            && itineraryRow.getIrKey() == null) {
                        itineraryRow = row;
                        core.setSelectedTAItineraryRow(row);
                        finish();
                        break;
                    } else {
                        // when would this happen?
                    }
                }
            }
        }

        @Override
        public void cleanup() {
            dismissDialog(DIALOG_SAVING);
        }
    }

    private void showCalendarDialog(int id) {
        Bundle bundle;

        calendarDialog = new CalendarPickerDialog();
        bundle = new Bundle();
        switch (id) {
        case DEPART_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, departDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, departDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, departDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            calendarDialog.setOnDateSetListener(new DateSetListener(id, departDateTime));
            break;
        case ARRIVE_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, arriveDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, arriveDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, arriveDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            calendarDialog.setOnDateSetListener(new DateSetListener(id, arriveDateTime));
            break;
        }

        DialogId = id;
        calendarDialog.setArguments(bundle);
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        switch (id) {
        case DEPART_TIME_DIALOG:
            dlg = new TimePickerDialog(this, new TimeSetListener(id, departDateTime),
                    departDateTime.get(Calendar.HOUR_OF_DAY), departDateTime.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this));
            break;
        case ARRIVE_TIME_DIALOG:
            dlg = new TimePickerDialog(this, new TimeSetListener(id, arriveDateTime),
                    arriveDateTime.get(Calendar.HOUR_OF_DAY), arriveDateTime.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this));
            break;
        case DIALOG_SAVING:
            dialog = new ProgressDialog(this);
            dialog.setMessage(getText(R.string.itin_saving_stop));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dlg = dialog;
            break;
        default:
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            dlg = ConcurCore.createDialog(this, id);
            break;
        }
        if (dlg == null) {
            return super.onCreateDialog(id);
        } else {
            return dlg;
        }
    }

    // Result from the City search
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String selectedListItemKey = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY);
            String selectedListItemCode = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE);
            if (selectedListItemKey != null || selectedListItemCode != null) {
                List<ListItem> items = null;
                ConcurCore app = (ConcurCore) TAStopActivity.this.getApplication();
                final SearchListResponse expenseSearchListResults = app.getExpenseSearchListResults();
                if (expenseSearchListResults != null) {
                    items = expenseSearchListResults.listItems;
                }

                if (items != null) {
                    for (ListItem listItem : items) {
                        if (listItem.key != null && listItem.key.equalsIgnoreCase(selectedListItemKey)
                                || listItem.code != null && listItem.code.equalsIgnoreCase(selectedListItemCode)) {
                            switch (requestCode) {
                            case SEARCH_LIST_REQUEST_CODE_DEPART:
                                itineraryRow.setDepartLocation(listItem.text);
                                itineraryRow.setDepartLnKey(listItem.key == null ? listItem.code : listItem.key);
                                break;
                            case SEARCH_LIST_REQUEST_CODE_ARRIVE:
                                itineraryRow.setArrivalLocation(listItem.text);
                                itineraryRow.setArrivalLnKey(listItem.key == null ? listItem.code : listItem.key);
                                break;
                            }
                            updateLocationViews();
                            break;
                        }
                    }
                }
            } else {
                // none is selected so update view with ""
                // listItemSelected("", "", "");
                // updateView();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_DIALOG_ID, DialogId);
        outState.putSerializable(KEY_DEPARTURE_DATE, departDateTime);
        outState.putSerializable(KEY_ARRIVAL_DATE, arriveDateTime);
    }

    class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i;

            final int id = v.getId();
            if (id == R.id.editDepartDate) {
                showCalendarDialog(DEPART_DATE_DIALOG);
            } else if (id == R.id.editArriveDate) {
                showCalendarDialog(ARRIVE_DATE_DIALOG);
            } else if (id == R.id.editDepartTime) {
                showDialog(DEPART_TIME_DIALOG);
            } else if (id == R.id.editArriveTime) {
                showDialog(ARRIVE_TIME_DIALOG);
            } else if (id == R.id.editDepartLocation) {
                Intent intent = new Intent(TAStopActivity.this, ListSearch.class);
                intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, "LocName");
                intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, getString(R.string.location));
                TAStopActivity.this.startActivityForResult(intent, SEARCH_LIST_REQUEST_CODE_DEPART);
            } else if (id == R.id.editArriveLocation) {
                Intent intent = new Intent(TAStopActivity.this, ListSearch.class);
                intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, "LocName");
                intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, getString(R.string.location));
                TAStopActivity.this.startActivityForResult(intent, SEARCH_LIST_REQUEST_CODE_ARRIVE);
            }
        }
    }

    // copied from AirSearch.java
    class DateSetListener implements CalendarPickerDialog.OnDateSetListener {

        private final int dialogId;
        private final Calendar calendar;

        DateSetListener(int dialogId, Calendar calendar) {
            this.dialogId = dialogId;
            this.calendar = calendar;
        }

        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(year, monthOfYear, dayOfMonth);

            updateDateTimeViews();

            // Remove the dialog. This will force recreation and a reset of the displayed values.
            // If we do not do this then the dialog may be shown with it's old values (from the first
            // time around) instead of the newest values (if the underlying Calendar value changes).
            calendarDialog.dismiss();
        }
    }

    // copied from AirSearch.java
    class TimeSetListener implements TimePickerDialog.OnTimeSetListener {

        private final int dialogId;
        private final Calendar cal;

        protected TimeSetListener(int dialogId, Calendar cal) {
            this.dialogId = dialogId;
            this.cal = cal;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO: Setting the time could cause a datetime overlap. Prevent that.

            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);

            updateDateTimeViews();

            // Remove the dialog. This will force recreation and a reset of the displayed values.
            // If we do not do this then the dialog may be shown with it's old values (from the first
            // time around) instead of the newest values (if the underlying Calendar value changes).
            removeDialog(dialogId);
        }
    }
}
