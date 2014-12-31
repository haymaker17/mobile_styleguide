/**
 * 
 */
package com.concur.mobile.core.travel.rail.activity;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.util.BookingDateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.platform.util.Format;

public class RailSearch extends TravelBaseActivity implements View.OnClickListener {

    private static final String CLS_TAG = RailSearch.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = RailSearch.class.getSimpleName()
            + ".calendar.dialog.fragment";

    protected static final int DEP_LOCATION_SEARCH_ACTIVITY_CODE = 0;
    protected static final int ARR_LOCATION_SEARCH_ACTIVITY_CODE = 1;

    // TODO: legacy intent keys, need to re-factor this and use consistent set of intent
    // keys to related rail booking activities.
    public static final String DEP_LOCATION = "dep_loc";
    public static final String ARR_LOCATION = "arr_loc";
    protected static final String ROUNDTRIP = "roundtrip";
    protected static final String DEP_DATETIME = "dep_datetime";
    protected static final String RET_DATETIME = "ret_datetime";

    private static final String MODE_KEY = "mode";
    private static final String STATE_DEPART_LOC_KEY = "depart_loc";
    private static final String STATE_ARRIVE_LOC_KEY = "arrive_loc";
    private static final String STATE_DEPART_DT_KEY = "depart_datetime";
    private static final String STATE_RETURN_DT_KEY = "return_datetime";
    private static final String KEY_DIALOG_ID = "key.dialog.id";
    private static final String KEY_IS_DEPARTURE = "key.is.departure";

    protected static final int DEP_DATE_DIALOG = DIALOG_ID_BASE + 0;
    protected static final int DEP_TIME_DIALOG = DIALOG_ID_BASE + 1;
    protected static final int RET_DATE_DIALOG = DIALOG_ID_BASE + 2;
    protected static final int RET_TIME_DIALOG = DIALOG_ID_BASE + 3;
    protected static final int SEARCH_DIALOG = DIALOG_ID_BASE + 4;
    protected static final int NO_RESULTS_DIALOG = DIALOG_ID_BASE + 5;
    protected static final int STATION_LIST_DIALOG = DIALOG_ID_BASE + 6;
    protected static final int STATION_LIST_ERROR_DIALOG = DIALOG_ID_BASE + 7;

    private int DialogId = -1;
    private boolean isDeparture = false;

    public static enum SearchMode {
        None, OneWay, RoundTrip, MultiSegment
    };

    protected SearchMode searchMode;

    protected Button modeOneWay;
    protected Button modeRoundTrip;
    protected Button modeMultiSeg;

    protected View depLocationButton;
    protected View arrLocationButton;

    protected Button depDateButton;
    protected Button depTimeButton;
    protected Button retDateButton;
    protected Button retTimeButton;

    protected View retDateTimeLabel;
    protected View retDateTimeControls;

    protected Button searchButton;

    protected RailStation departLocation;
    protected RailStation arriveLocation;

    protected boolean currentRoundtrip;

    protected Calendar departDateTime;
    protected Calendar returnDateTime;

    private CalendarPickerDialog calendarDialog;

    // protected final static SpinnerItem[] numPassengers = new SpinnerItem[] {
    // new SpinnerItem("1", "1"),
    // new SpinnerItem("2", "2"),
    // new SpinnerItem("3", "3"),
    // new SpinnerItem("4", "4"),
    // new SpinnerItem("5", "5"),
    // new SpinnerItem("6", "6"),
    // new SpinnerItem("7", "7"),
    // new SpinnerItem("8", "8"),
    // new SpinnerItem("9", "9")
    // };
    //
    // protected SpinnerItem currentNumPassengers;

    // /////////////////////////////////////////////////////////////////

    @Override
    protected void initValues(Bundle sis) {

        if (sis == null) {
            searchMode = SearchMode.None;

            // We're specifically getting 'now' in the device local timezone because then
            // we just pull the date values out and use them to populate our UTC-standard
            // calendar.
            Calendar now = Calendar.getInstance();
            departDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            departDateTime.clear();
            departDateTime
                    .set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 9, 0, 0);
            departDateTime.add(Calendar.DAY_OF_MONTH, 1);

            returnDateTime = (Calendar) departDateTime.clone();
            returnDateTime.set(Calendar.HOUR, 17);
            returnDateTime.add(Calendar.DAY_OF_MONTH, 1);

        } else {

            String mode = sis.getString(MODE_KEY);
            if (mode != null) {
                searchMode = SearchMode.valueOf(mode);
            }

            Bundle locBundle = sis.getBundle(STATE_DEPART_LOC_KEY);
            if (locBundle != null) {
                departLocation = new RailStation(locBundle);
            }

            locBundle = sis.getBundle(STATE_ARRIVE_LOC_KEY);
            if (locBundle != null) {
                arriveLocation = new RailStation(locBundle);
            }

            departDateTime = (Calendar) sis.getSerializable(STATE_DEPART_DT_KEY);
            returnDateTime = (Calendar) sis.getSerializable(STATE_RETURN_DT_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(MODE_KEY, searchMode.name());

        if (departLocation != null) {
            outState.putBundle(STATE_DEPART_LOC_KEY, departLocation.getBundle());
        }
        if (arriveLocation != null) {
            outState.putBundle(STATE_ARRIVE_LOC_KEY, arriveLocation.getBundle());
        }
        outState.putSerializable(STATE_DEPART_DT_KEY, departDateTime);
        outState.putSerializable(STATE_RETURN_DT_KEY, returnDateTime);
        outState.putInt(KEY_DIALOG_ID, DialogId);
        outState.putBoolean(KEY_IS_DEPARTURE, isDeparture);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rail_search);

        initValues(savedInstanceState);
        initUI();

        // Make sure we have rail stations
        ConcurCore app = (ConcurCore) getApplication();
        if (app.getRailStationList() == null) {
            ConcurService svc = app.getService();
            if (svc.getRailStationList() == null) {
                if (ConcurCore.isConnected()) {
                    // Go fetch the stations and block until it comes back.
                    showDialog(STATION_LIST_DIALOG);
                    registerReceiver(stationListReceiver, stationListFilter);
                    svc.sendRailStationListRequest(Const.VENDOR_AMTRAK);
                }
            }
        }

        restoreReceivers();

        // Fetch booking information fields if created on a non-orientation change
        if (!orientationChange) {
            if (ConcurCore.isConnected()) {
                // Init or request the travel custom fields.
                if (!hasTravelCustomFieldsView()) {
                    if (ConcurCore.isConnected()) {
                        sendTravelCustomFieldsRequest();
                    } else {
                        // TODO: Let end-user that connectivity is required for search.
                    }
                }
            }
        }

        calendarDialog = (CalendarPickerDialog) getSupportFragmentManager().findFragmentByTag(
                TAG_CALENDAR_DIALOG_FRAGMENT);
        if (calendarDialog != null) {
            if (savedInstanceState.containsKey(KEY_DIALOG_ID) && savedInstanceState.containsKey(KEY_IS_DEPARTURE)) {
                calendarDialog.setOnDateSetListener(new RailDateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID),
                        savedInstanceState.getBoolean(KEY_IS_DEPARTURE)));
            }
        }

    }

    protected final IntentFilter stationListFilter = new IntentFilter(Const.ACTION_RAIL_STATION_LIST_RESULTS);

    protected final BroadcastReceiver stationListReceiver = new BroadcastReceiver() {

        private final String CLS_TAG = RailSearch.CLS_TAG + ".StationListReceiver.onReceive: ";

        @Override
        public void onReceive(Context context, Intent i) {

            unregisterReceiver(this);

            String status = i.getStringExtra(Const.REPLY_STATUS);

            if (status != null && status.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                try {
                    dismissDialog(STATION_LIST_DIALOG);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }
            } else {
                // Something went awry, bail
            }
        }
    };

    private void showCalendarDialog(int id) {
        Bundle bundle;

        calendarDialog = new CalendarPickerDialog();
        bundle = new Bundle();
        switch (id) {
        case DEP_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, departDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, departDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, departDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            isDeparture = true;
            break;
        case RET_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, returnDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, returnDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, returnDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            isDeparture = false;
            break;
        }

        DialogId = id;
        calendarDialog.setOnDateSetListener(new RailDateSetListener(DialogId, isDeparture));
        calendarDialog.setArguments(bundle);
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        dlg = super.onCreateDialog(id);
        if (dlg == null) {
            switch (id) {
            case DEP_TIME_DIALOG:
                dlg = new TimePickerDialog(this, new RailTimeSetListener(id, departDateTime),
                        departDateTime.get(Calendar.HOUR_OF_DAY), departDateTime.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this));
                break;
            case RET_TIME_DIALOG:
                dlg = new TimePickerDialog(this, new RailTimeSetListener(id, returnDateTime),
                        returnDateTime.get(Calendar.HOUR_OF_DAY), returnDateTime.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this));
                break;
            case SEARCH_DIALOG: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.searching_for_trains));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dlg = dialog;
                break;
            }
            case NO_RESULTS_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.search_no_result_dialog_title);
                dlgBldr.setMessage(R.string.rail_search_no_result_dialog_text);
                dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case STATION_LIST_DIALOG: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.retrieve_rail_station_list));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // If they cancel this dialog then we need to bail out of the entire activity
                        finish();
                    }
                });
                dlg = dialog;
                break;
            }
            case STATION_LIST_ERROR_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.rail_station_error_dialog_title);
                dlgBldr.setMessage(R.string.rail_station_error_dialog_text);
                dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // We can't do anything, bail.
                        dialog.dismiss();
                        finish();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            default: {
                ConcurCore ConcurCore = (ConcurCore) getApplication();
                dlg = ConcurCore.createDialog(this, id);
                break;
            }
            }
        }
        return dlg;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - start
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE:
            // MOB-14331
            super.onActivityResult(requestCode, resultCode, data);
            break;
        case DEP_LOCATION_SEARCH_ACTIVITY_CODE:
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                departLocation = new RailStation(locBundle);
                updateLocationViews();
            }
            break;
        case ARR_LOCATION_SEARCH_ACTIVITY_CODE:
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                arriveLocation = new RailStation(locBundle);
                updateLocationViews();
            }
            break;
        case Const.REQUEST_CODE_BOOK_RAIL:
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
            break;
        }

        searchButton.setEnabled(departLocation != null && arriveLocation != null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Depart and Arrive field methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Handle the date selection event Adjust the depart/arrive time as necessary to prevent bad times.
     */
    class RailDateSetListener implements CalendarPickerDialog.OnDateSetListener {

        private final int dialogId;

        // We need to know which date we are setting so we can adjust, therefore a
        // boolean instead of a reference to the calendar
        private final boolean isDepart;

        RailDateSetListener(int dialogId, boolean isDepart) {
            this.dialogId = dialogId;
            this.isDepart = isDepart;
        }

        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            BookingDateUtil dateUtil = new BookingDateUtil();
            if (isDepart) {
                departDateTime.set(year, monthOfYear, dayOfMonth);

                // Adjust the arrive time if needed
                if (departDateTime.after(returnDateTime)) {
                    returnDateTime.set(departDateTime.get(Calendar.YEAR), departDateTime.get(Calendar.MONTH),
                            departDateTime.get(Calendar.DAY_OF_MONTH));
                }

                if (dateUtil.isDateInValid(departDateTime, null, true)) {
                    departDateTime = dateUtil.setDepartToCurrent(departDateTime, returnDateTime,
                            dateUtil.getCurrentTime());
                    // Adjust the return time if needed
                    if (departDateTime.after(returnDateTime)) {
                        returnDateTime = dateUtil.setReturnToCurrent(returnDateTime, departDateTime);
                    }
                }

            } else {
                returnDateTime.set(year, monthOfYear, dayOfMonth);

                if (dateUtil.isDateInValid(departDateTime, returnDateTime, false)) {
                    returnDateTime = dateUtil.setReturnToCurrent(returnDateTime, departDateTime);
                }

                // Adjust the depart time if needed
                if (returnDateTime.before(returnDateTime)) {
                    departDateTime.set(returnDateTime.get(Calendar.YEAR), returnDateTime.get(Calendar.MONTH),
                            returnDateTime.get(Calendar.DAY_OF_MONTH));
                }
            }

            updateDateTimeViews();

            // Remove the dialog. This will force recreation and a reset of the displayed values.
            // If we do not do this then the dialog may be shown with it's old values (from the first
            // time around) instead of the newest values (if the underlying Calendar value changes).
            calendarDialog.dismiss();
        }

    }

    /**
     * Handle the time selection event
     */
    class RailTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        private final int dialogId;
        private final Calendar cal;

        protected RailTimeSetListener(int dialogId, Calendar cal) {
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

    // ///////////////////////////////////////////////////////////////////////////
    // Pickup and Dropoff field methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Passenger count spinner methods - start
    // ///////////////////////////////////////////////////////////////////////////

    // protected void configureNumPassengers() {
    // numPassengerSpinner = (Spinner) findViewById(R.id.railSearchNumPassengers);
    // ArrayAdapter<SpinnerItem> smokingAdapter = new ArrayAdapter<SpinnerItem>(
    // this, android.R.layout.simple_spinner_item, numPassengers);
    // smokingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // numPassengerSpinner.setAdapter(smokingAdapter);
    //
    // numPassengerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    //
    // public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    // currentNumPassengers = (SpinnerItem) numPassengerSpinner.getItemAtPosition(pos);
    // }
    //
    // public void onNothingSelected(AdapterView<?> parent) {
    // }
    //
    // });
    //
    // if (currentNumPassengers == null) {
    // currentNumPassengers = numPassengers[0];
    // numPassengerSpinner.setSelection(0);
    // } else {
    // numPassengerSpinner.setSelection(Integer.parseInt(currentNumPassengers.id) - 1);
    // }
    //
    // }

    // ///////////////////////////////////////////////////////////////////////////
    // Passenger count spinner methods - end
    // ///////////////////////////////////////////////////////////////////////////

    protected void doSearch() {

        // Launch the activity that will actually kick-off the search and
        // display a progress message.
        Intent intent = new Intent(this, RailSearchProgress.class);
        intent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocation.getBundle());
        intent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocation.getBundle());
        intent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);
        if (searchMode == SearchMode.RoundTrip) {
            intent.putExtra(RET_DATETIME, returnDateTime);
        }
        intent.putExtra(DEP_LOCATION, departLocation.stationCode);
        intent.putExtra(ARR_LOCATION, arriveLocation.stationCode);
        intent.putExtra(DEP_DATETIME, departDateTime);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        startActivityForResult(intent, Const.REQUEST_CODE_BOOK_RAIL);

    }

    @Override
    protected void initUI() {

        // Grab some references
        modeOneWay = (Button) findViewById(R.id.rail_search_oneway);
        modeRoundTrip = (Button) findViewById(R.id.rail_search_roundtrip);
        modeMultiSeg = (Button) findViewById(R.id.rail_search_multi);

        // Init footer
        searchButton = (Button) findViewById(R.id.footer_button_one);
        searchButton.setText(R.string.general_search);

        // Set static labels
        getSupportActionBar().setTitle(R.string.rail_search_title);

        setFieldName(R.id.rail_search_depart_loc, R.string.rail_search_label_depart_station);
        setFieldName(R.id.rail_search_arrive_loc, R.string.rail_search_label_arrive_station);
        setFieldName(R.id.rail_search_depart_date, R.string.rail_search_label_depart_date);
        setFieldName(R.id.rail_search_depart_time, R.string.rail_search_label_depart_time);
        setFieldName(R.id.rail_search_return_date, R.string.rail_search_label_return_date);
        setFieldName(R.id.rail_search_return_time, R.string.rail_search_label_return_time);

        // Set display values
        updateLocationViews();
        updateDateTimeViews();

        // Set up the handlers
        findViewById(R.id.rail_search_depart_loc).setOnClickListener(this);
        findViewById(R.id.rail_search_arrive_loc).setOnClickListener(this);
        findViewById(R.id.rail_search_depart_date).setOnClickListener(this);
        findViewById(R.id.rail_search_depart_time).setOnClickListener(this);
        findViewById(R.id.rail_search_return_date).setOnClickListener(this);
        findViewById(R.id.rail_search_return_time).setOnClickListener(this);

        // Set us up the default
        switch (searchMode) {
        case None:
        case RoundTrip:
            selectModeButton(modeRoundTrip);
            break;
        case OneWay:
            selectModeButton(modeOneWay);
            break;
        case MultiSegment:
            selectModeButton(modeMultiSeg);
            break;
        }

    }

    protected void updateLocationViews() {
        if (departLocation != null) {
            setFieldValue(R.id.rail_search_depart_loc, departLocation.getName());
        }
        if (arriveLocation != null) {
            setFieldValue(R.id.rail_search_arrive_loc, arriveLocation.getName());
        }
        if (departLocation != null && arriveLocation != null) {
            searchButton.setEnabled(true);
        } else {
            searchButton.setEnabled(false);
        }
    }

    protected void updateDateTimeViews() {
        // Force our timezone for formatting to UTC since that's what we use everywhere internally.
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        setFieldValue(R.id.rail_search_depart_date,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, departDateTime));
        setFieldValue(R.id.rail_search_depart_time, Format.safeFormatCalendar(timeFormat, departDateTime));

        setFieldValue(R.id.rail_search_return_date,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, returnDateTime));
        setFieldValue(R.id.rail_search_return_time, Format.safeFormatCalendar(timeFormat, returnDateTime));
    }

    protected void setFieldName(int parentView, int textId) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_name));
        tv.setText(textId);
    }

    protected void setFieldValue(int parentView, CharSequence text) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_value));
        tv.setText(text);
    }

    protected void selectModeButton(View v) {
        if (!v.isSelected()) {
            // Only do something if we are clicking a different button
            final int id = v.getId();
            if (id == R.id.rail_search_oneway) {
                modeOneWay.setSelected(true);
                modeRoundTrip.setSelected(false);
                modeMultiSeg.setSelected(false);
                searchMode = SearchMode.OneWay;
            } else if (id == R.id.rail_search_multi) {
                modeMultiSeg.setSelected(true);
                modeOneWay.setSelected(false);
                modeRoundTrip.setSelected(false);
                searchMode = SearchMode.MultiSegment;
            } else {
                modeRoundTrip.setSelected(true);
                modeMultiSeg.setSelected(false);
                modeOneWay.setSelected(false);
                searchMode = SearchMode.RoundTrip;
            }
        }

        updateUIForMode();
    }

    protected void updateUIForMode() {
        switch (searchMode) {
        case OneWay:
            findViewById(R.id.rail_search_return).setVisibility(View.GONE);
            break;
        case MultiSegment:
            break;
        case RoundTrip:
        default:
            findViewById(R.id.rail_search_return).setVisibility(View.VISIBLE);
            break;
        }
    }

    @Override
    protected boolean getDisplayAtStart() {
        return true;
    }

    @Override
    public void onClick(View v) {

        Intent i;

        final int id = v.getId();
        if (id == R.id.rail_search_depart_loc) {
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                i = new Intent(this, LocationSearch.class);
                i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_RAIL_STATIONS);
                startActivityForResult(i, DEP_LOCATION_SEARCH_ACTIVITY_CODE);
            }
        } else if (id == R.id.rail_search_arrive_loc) {
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                i = new Intent(this, LocationSearch.class);
                i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_RAIL_STATIONS);
                startActivityForResult(i, ARR_LOCATION_SEARCH_ACTIVITY_CODE);
            }
        } else if (id == R.id.rail_search_depart_date) {
            showCalendarDialog(DEP_DATE_DIALOG);
        } else if (id == R.id.rail_search_return_date) {
            showCalendarDialog(RET_DATE_DIALOG);
        } else if (id == R.id.rail_search_depart_time) {
            showDialog(DEP_TIME_DIALOG);
        } else if (id == R.id.rail_search_return_time) {
            showDialog(RET_TIME_DIALOG);
        } else if (id == R.id.footer_button_one) {
            if (ConcurCore.isConnected()) {
                // ValidateTravelCustomFields will display a dialog and return 'false' if any displayed
                // fields have missing or invalid values.
                if (validateTravelCustomFields()) {
                    // Commit the travel custom fields.
                    commitTravelCustomFields();
                    doSearch();
                }
            } else {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        } else if (id == R.id.rail_search_oneway || id == R.id.rail_search_roundtrip || id == R.id.rail_search_multi) {
            selectModeButton(v);
        }
    }

}
