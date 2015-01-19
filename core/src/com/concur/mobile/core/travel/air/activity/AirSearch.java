package com.concur.mobile.core.travel.air.activity;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.RefundableInfo;
import com.concur.mobile.core.util.BookingDateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.platform.util.Format;

public class AirSearch extends TravelBaseActivity implements View.OnClickListener {

    private static final String CLS_TAG = AirSearch.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = AirSearch.class.getSimpleName()
            + ".calendar.dialog.fragment";

    private static final String MODE_KEY = "mode";

    private static final String STATE_DEPART_LOC_KEY = "depart_loc";
    private static final String STATE_ARRIVE_LOC_KEY = "arrive_loc";
    private static final String STATE_DEPART_DT_KEY = "depart_datetime";
    private static final String STATE_RETURN_DT_KEY = "return_datetime";
    private static final String STATE_CABIN_CLASS = "service_class";
    private static final String STATE_REFUNDABLE_ONLY = "refundable_only";
    private static final String KEY_DIALOG_ID = "key.dialog.id";

    private static final int DEPART_DATE_DIALOG = DIALOG_ID_BASE + 0;
    private static final int RETURN_DATE_DIALOG = DIALOG_ID_BASE + 1;
    private static final int DEPART_TIME_DIALOG = DIALOG_ID_BASE + 2;
    private static final int RETURN_TIME_DIALOG = DIALOG_ID_BASE + 3;
    private static final int CABIN_CLASS_DIALOG = DIALOG_ID_BASE + 4;

    protected static final int DEPART_LOCATION_ACTIVITY_CODE = 0;
    protected static final int ARRIVE_LOCATION_ACTIVITY_CODE = 1;
    protected static final int RESULTS_ACTIVITY_CODE = 2;

    private int DialogId = -1;

    public static enum SearchMode {
        None, OneWay, RoundTrip, MultiSegment
    };

    private CalendarPickerDialog calendarDialog;
    private AlertDialogFragment invalidDatesFrag;

    protected SearchMode searchMode;

    protected Button modeOneWay;
    protected Button modeRoundTrip;
    protected Button modeMultiSeg;

    protected LocationChoice departLocation;
    protected LocationChoice arriveLocation;

    protected Calendar departDateTime;
    protected Calendar returnDateTime;

    protected Boolean refundableOnly;

    protected SpinnerItem curCabinClass;
    protected SpinnerItem[] cabinClassItems;

    protected Button searchButton;

    // Contains country codes for which intra-travel as a Flex Fare user should
    // be blocked from searching.
    public final static String[] FLEX_FARING_INTRA_COUNTRY_TRAVEL = { "AU", "NZ", "CA", "IN", "SE", "NO", "DK", "FI" };

    // ------------------------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.air_search);
        initValues(savedInstanceState);
        initUI();

        // If not an orientation change, then send a booking fields information
        // request.
        if (!orientationChange) {
            // Init or request the travel custom fields.
            if (!hasTravelCustomFieldsView()) {
                if (ConcurCore.isConnected()) {
                    sendTravelCustomFieldsRequest();
                } else {
                    // TODO: Let end-user that connectivity is required for
                    // search.
                }
            }
        }

        calendarDialog = (CalendarPickerDialog) getSupportFragmentManager().findFragmentByTag(
                TAG_CALENDAR_DIALOG_FRAGMENT);
        if (calendarDialog != null) {
            if (savedInstanceState.containsKey(KEY_DIALOG_ID)) {
                calendarDialog.setOnDateSetListener(new AirDateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID)));
            }
        }
    }

    @Override
    public void onClick(View v) {

        Intent i;

        final int id = v.getId();
        if (id == R.id.air_search_depart_loc) {
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                i = new Intent(this, LocationSearch.class);
                i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_AIRPORTS);
                startActivityForResult(i, DEPART_LOCATION_ACTIVITY_CODE);
            }
        } else if (id == R.id.air_search_arrive_loc) {
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                i = new Intent(this, LocationSearch.class);
                i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_AIRPORTS);
                startActivityForResult(i, ARRIVE_LOCATION_ACTIVITY_CODE);
            }
        } else if (id == R.id.air_search_depart_date) {
            showCalendarDialog(DEPART_DATE_DIALOG);
        } else if (id == R.id.air_search_return_date) {
            showCalendarDialog(RETURN_DATE_DIALOG);
        } else if (id == R.id.air_search_depart_time) {
            showDialog(DEPART_TIME_DIALOG);
        } else if (id == R.id.air_search_return_time) {
            showDialog(RETURN_TIME_DIALOG);
        } else if (id == R.id.air_search_cabin_class_selector) {
            showDialog(CABIN_CLASS_DIALOG);
        } else if (id == R.id.air_search_refundable_only) {
            refundableOnly = !ViewUtil.getCheckedTextViewState(v, R.id.field_name);
            ViewUtil.setCheckedTextViewState(v, R.id.field_name, refundableOnly);
        } else if (id == R.id.footer_button_one) {
            // Check for connectivity, if none, then display dialog and return.
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
                return;
            }
            // ValidateTravelCustomFields will display a dialog and return
            // 'false' if any displayed
            // fields have missing or invalid values.
            if (validateTravelCustomFields()) {
                // Check for a flex fare traveler and the start/end airport
                // locations.
                if (showTravelProfileInCompleteWarningDialog()) {
                    showDialog(Const.DIALOG_TRAVEL_PROFILE_INCOMPLETE);
                    return;
                } else if (showFlexFareWarningDialog()) {
                    showDialog(Const.DIALOG_TRAVEL_FLEX_FARE);
                    return;
                }
                // Commit the travel custom fields.
                commitTravelCustomFields();
                BookingDateUtil dateUtil = new BookingDateUtil();
                boolean isCurrentDate = searchMode == SearchMode.OneWay ? true : false;
                Calendar returnDate = searchMode == SearchMode.OneWay ? null : returnDateTime;
                if (dateUtil.isDateInValidForDefaultTimeZone(departDateTime, returnDate, isCurrentDate)) {
                    showInvalidDatesFrag();
                } else {
                    doSearch();
                }

            }
        } else if (id == R.id.air_search_oneway || id == R.id.air_search_roundtrip || id == R.id.air_search_multi) {
            selectModeButton(v);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // MOB-11596 MOB-13636
        if (Preferences.shouldAllowVoiceBooking()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.option_voice, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuVoice) {
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                Intent i = new Intent(AirSearch.this, VoiceAirSearchActivity.class);
                AirSearch.this.startActivity(i);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = super.onCreateDialog(id);

        if (dlg == null) {
            switch (id) {
            case DEPART_TIME_DIALOG:
                dlg = new TimePickerDialog(this, new AirTimeSetListener(id, departDateTime),
                        departDateTime.get(Calendar.HOUR_OF_DAY), departDateTime.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this));
                break;
            case RETURN_TIME_DIALOG:
                dlg = new TimePickerDialog(this, new AirTimeSetListener(id, returnDateTime),
                        returnDateTime.get(Calendar.HOUR_OF_DAY), returnDateTime.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this));
                break;
            case CABIN_CLASS_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.air_search_label_class);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, cabinClassItems) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (curCabinClass != null) {
                    for (int i = 0; i < cabinClassItems.length; i++) {
                        if (curCabinClass.id.equals(cabinClassItems[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }
                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        curCabinClass = cabinClassItems[which];
                        updateCabinClassView();
                        removeDialog(CABIN_CLASS_DIALOG);
                    }
                });
                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeDialog(CABIN_CLASS_DIALOG);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE:
            // MOB-14331
            super.onActivityResult(requestCode, resultCode, data);
            break;
        case DEPART_LOCATION_ACTIVITY_CODE:
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                departLocation = new LocationChoice(locBundle);
                updateLocationViews();
            }
            break;
        case ARRIVE_LOCATION_ACTIVITY_CODE:
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                arriveLocation = new LocationChoice(locBundle);
                updateLocationViews();
            }
            break;
        case RESULTS_ACTIVITY_CODE:
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
            break;
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
        if (curCabinClass != null) {
            outState.putString(STATE_CABIN_CLASS, curCabinClass.id);
        }
        outState.putBoolean(STATE_REFUNDABLE_ONLY, refundableOnly);

        outState.putSerializable(STATE_DEPART_DT_KEY, departDateTime);
        outState.putSerializable(STATE_RETURN_DT_KEY, returnDateTime);
        outState.putInt(KEY_DIALOG_ID, DialogId);
    }

    @Override
    protected boolean getDisplayAtStart() {
        return true;
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
            break;
        case RETURN_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, returnDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, returnDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, returnDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            break;
        }

        DialogId = id;
        calendarDialog.setOnDateSetListener(new AirDateSetListener(DialogId));
        calendarDialog.setArguments(bundle);
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    /**
     * Will determine whether or not an end-user that is searching for flights where the to/from location is within the US has
     * missing TSA information.
     * 
     * @return returns whether or not an end-user that is searching for flights where the to/from location is within the US has
     *         missing TSA information.
     */
    protected boolean showTravelProfileInCompleteWarningDialog() {
        boolean retVal = false;
        if ((departLocation.countryAbbrev != null && departLocation.countryAbbrev.equalsIgnoreCase("US"))
                || (arriveLocation.countryAbbrev != null && arriveLocation.countryAbbrev.equalsIgnoreCase("US"))) {
            retVal = ViewUtil.isTravelProfileCompleteMissingTSA(this);
        }
        return retVal;
    }

    /**
     * Will determine whether the current user is a Flex Fare user and if so whether the current depart/arrive locations require
     * the end-user to not book via mobile.
     * 
     * @return returns <code>true</code> if the flex fare warning dialog should be displayed; <code>false</code> otherwise.
     */
    protected boolean showFlexFareWarningDialog() {
        boolean retVal = false;
        if (ViewUtil.isFlexFareUser(this)) {
            if (departLocation.countryAbbrev != null && arriveLocation.countryAbbrev != null) {
                // First check for intra-country travel.
                for (int ffInd = 0; ffInd < FLEX_FARING_INTRA_COUNTRY_TRAVEL.length; ++ffInd) {
                    if (departLocation.countryAbbrev.equalsIgnoreCase(FLEX_FARING_INTRA_COUNTRY_TRAVEL[ffInd])
                            && arriveLocation.countryAbbrev.equalsIgnoreCase(FLEX_FARING_INTRA_COUNTRY_TRAVEL[ffInd])) {
                        retVal = true;
                        break;
                    }
                }
                if (!retVal) {
                    // Second check for inter-country travel between specific
                    // countries.
                    if ((departLocation.countryAbbrev.equalsIgnoreCase("AU") && arriveLocation.countryAbbrev
                            .equalsIgnoreCase("NZ"))
                            || (departLocation.countryAbbrev.equalsIgnoreCase("NZ") && arriveLocation.countryAbbrev
                                    .equalsIgnoreCase("AU"))) {
                        retVal = true;
                    }
                }
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG
                        + ".showFlexFareWarningDialog: depart/arrive country abbreviation missing!");
            }
        }
        return retVal;
    }

    @Override
    protected void initValues(Bundle sis) {

        cabinClassItems = getAirClassList();

        if (sis == null) {
            searchMode = SearchMode.None;

            // MOB-21681 - set user's preferred time zone
            departDateTime = Calendar.getInstance();
            departDateTime.set(Calendar.HOUR_OF_DAY, 9);
            departDateTime.set(Calendar.MINUTE, 0);
            departDateTime.set(Calendar.SECOND, 0);

            departDateTime.add(Calendar.DAY_OF_MONTH, 1);

            returnDateTime = (Calendar) departDateTime.clone();
            returnDateTime.add(Calendar.DAY_OF_MONTH, 3);

            // Select default cabin class.
            curCabinClass = SpinnerItem.findById(cabinClassItems, Const.AIR_SEARCH_DEFAULT_CABIN_CLASS);

            // Explicitly initialize refundableOnly to 'false'.
            refundableOnly = false;

        } else {

            String mode = sis.getString(MODE_KEY);
            if (mode != null) {
                searchMode = SearchMode.valueOf(mode);
            }

            Bundle locBundle = sis.getBundle(STATE_DEPART_LOC_KEY);
            if (locBundle != null) {
                departLocation = new LocationChoice(locBundle);
            }

            locBundle = sis.getBundle(STATE_ARRIVE_LOC_KEY);
            if (locBundle != null) {
                arriveLocation = new LocationChoice(locBundle);
            }

            departDateTime = (Calendar) sis.getSerializable(STATE_DEPART_DT_KEY);
            returnDateTime = (Calendar) sis.getSerializable(STATE_RETURN_DT_KEY);

            if (sis.containsKey(STATE_CABIN_CLASS)) {
                String cabinClassStr = sis.getString(STATE_CABIN_CLASS);
                curCabinClass = SpinnerItem.findById(cabinClassItems, cabinClassStr);
                if (curCabinClass == null) {
                    // Select default cabin class.
                    curCabinClass = SpinnerItem.findById(cabinClassItems, Const.AIR_SEARCH_DEFAULT_CABIN_CLASS);
                }
            } else {
                // Select default cabin class.
                curCabinClass = SpinnerItem.findById(cabinClassItems, Const.AIR_SEARCH_DEFAULT_CABIN_CLASS);
            }

            // Restore refundableOnly value.
            if (sis.containsKey(STATE_REFUNDABLE_ONLY)) {
                refundableOnly = sis.getBoolean(STATE_REFUNDABLE_ONLY);
            }
        }

        restoreReceivers();
    }

    protected void setFieldName(int parentView, int textId) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_name));
        tv.setText(textId);
    }

    protected void setFieldValue(int parentView, CharSequence text) {
        TextView tv = (TextView) (findViewById(parentView).findViewById(R.id.field_value));
        tv.setText(text);
    }

    @Override
    protected void initUI() {

        // Grab some references
        modeOneWay = (Button) findViewById(R.id.air_search_oneway);
        modeRoundTrip = (Button) findViewById(R.id.air_search_roundtrip);
        modeMultiSeg = (Button) findViewById(R.id.air_search_multi);

        // Init footer
        searchButton = (Button) findViewById(R.id.footer_button_one);
        searchButton.setText(R.string.general_search);

        // Set static labels
        getSupportActionBar().setTitle(R.string.air_search_title);

        setFieldName(R.id.air_search_depart_loc, R.string.air_search_label_departcity);
        setFieldName(R.id.air_search_arrive_loc, R.string.air_search_label_arrivecity);
        setFieldName(R.id.air_search_depart_date, R.string.air_search_label_departdate);
        setFieldName(R.id.air_search_depart_time, R.string.air_search_label_departtime);
        setFieldName(R.id.air_search_return_date, R.string.air_search_label_returndate);
        setFieldName(R.id.air_search_return_time, R.string.air_search_label_returntime);
        setFieldName(R.id.air_search_cabin_class_selector, R.string.air_search_label_class);
        setFieldName(R.id.air_search_refundable_only, R.string.air_search_label_refundable_only);
        ConcurCore concurCore = getConcurCore();
        SystemConfig sysConfig = concurCore.getSystemConfig();
        if (sysConfig != null && sysConfig.getRefundableInfo() != null) {
            RefundableInfo refInfo = sysConfig.getRefundableInfo();
            if (refInfo != null) {
                View view = findViewById(R.id.air_search_refundable_only);
                if (view != null) {
                    if (refInfo.showCheckBox != null && refInfo.showCheckBox) {
                        // Set the default state of the checkbox.
                        if (refInfo.checkBoxDefault != null) {
                            if (refundableOnly.equals(null)) {
                                refundableOnly = refInfo.checkBoxDefault;
                            }
                        }
                    } else {
                        // Hide the "refundable only" field.
                        view.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initUI: unable to locate 'air_search_refundable_only' field!");
                }
            }
        }

        // Set display values
        updateLocationViews();
        updateDateTimeViews();
        updateCabinClassView();
        updateRefundableOnlyView();

        // Set up the handlers
        findViewById(R.id.air_search_depart_loc).setOnClickListener(this);
        findViewById(R.id.air_search_arrive_loc).setOnClickListener(this);
        findViewById(R.id.air_search_depart_date).setOnClickListener(this);
        findViewById(R.id.air_search_depart_time).setOnClickListener(this);
        findViewById(R.id.air_search_return_date).setOnClickListener(this);
        findViewById(R.id.air_search_return_time).setOnClickListener(this);
        findViewById(R.id.air_search_cabin_class_selector).setOnClickListener(this);
        findViewById(R.id.air_search_refundable_only).setOnClickListener(this);

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
            setFieldValue(R.id.air_search_depart_loc, departLocation.getName());
        }

        if (arriveLocation != null) {
            setFieldValue(R.id.air_search_arrive_loc, arriveLocation.getName());
        }

        if (departLocation != null && arriveLocation != null) {
            searchButton.setEnabled(true);
        } else {
            searchButton.setEnabled(false);
        }
    }

    protected void updateDateTimeViews() {
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
        // MOB-21681 - set user's preferred time zone
        // timeFormat.setTimeZone(TimeZone.getDefault());

        // MOB-22200 - choose local time zone
        setFieldValue(R.id.air_search_depart_date,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL, departDateTime));
        setFieldValue(R.id.air_search_depart_time, Format.safeFormatCalendar(timeFormat, departDateTime));
        // MOB-22200 - choose local time zone
        setFieldValue(R.id.air_search_return_date,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL, returnDateTime));
        setFieldValue(R.id.air_search_return_time, Format.safeFormatCalendar(timeFormat, returnDateTime));
    }

    protected void updateCabinClassView() {
        if (curCabinClass != null) {
            setFieldValue(R.id.air_search_cabin_class_selector, curCabinClass.name);
        }
    }

    protected void updateRefundableOnlyView() {
        View refundableOnlyView = findViewById(R.id.air_search_refundable_only);
        if (refundableOnlyView != null) {
            View fieldName = refundableOnlyView.findViewById(R.id.field_name);
            if (fieldName instanceof CheckedTextView) {
                CheckedTextView ctv = (CheckedTextView) fieldName;
                ctv.setChecked(refundableOnly);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateRefundableOnlyView: expecting CheckedTextView object!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".updateRefundableOnlyView: unable to locate 'air_search_refundable_only' view!");
        }
    }

    protected void selectModeButton(View v) {
        if (!v.isSelected()) {
            // Only do something if we are clicking a different button
            final int id = v.getId();
            if (id == R.id.air_search_oneway) {
                modeOneWay.setSelected(true);
                modeRoundTrip.setSelected(false);
                modeMultiSeg.setSelected(false);
                searchMode = SearchMode.OneWay;
            } else if (id == R.id.air_search_multi) {
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
            findViewById(R.id.air_search_return).setVisibility(View.GONE);
            break;
        case MultiSegment:
            break;
        case RoundTrip:
        default:
            findViewById(R.id.air_search_return).setVisibility(View.VISIBLE);
            break;
        }
    }

    /**
     * Handle the date selection event Adjust the depart/return time as necessary to prevent bad times.
     */
    class AirDateSetListener implements CalendarPickerDialog.OnDateSetListener {

        private final int dialogId;

        AirDateSetListener(int dialogId) {
            this.dialogId = dialogId;
        }

        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            BookingDateUtil dateUtil = new BookingDateUtil();
            if (dialogId == DEPART_DATE_DIALOG) {

                departDateTime.set(year, monthOfYear, dayOfMonth);

                // Adjust the return time if needed
                if ((searchMode != SearchMode.OneWay) && departDateTime.after(returnDateTime)) {
                    returnDateTime.set(departDateTime.get(Calendar.YEAR), departDateTime.get(Calendar.MONTH),
                            departDateTime.get(Calendar.DAY_OF_MONTH));
                    returnDateTime.add(Calendar.DAY_OF_MONTH, 3);
                }

                if (dateUtil.isDateInValidForDefaultTimeZone(departDateTime, null, true)) {
                    departDateTime = dateUtil
                            .setDepartToCurrent(departDateTime, returnDateTime, Calendar.getInstance());
                    // Adjust the return time if needed
                    if (departDateTime.after(returnDateTime)) {
                        returnDateTime = dateUtil.setReturnToCurrent(returnDateTime, departDateTime);
                    }

                }
            } else {
                returnDateTime.set(year, monthOfYear, dayOfMonth);

                if (dateUtil.isDateInValidForDefaultTimeZone(departDateTime, returnDateTime, false)) {
                    returnDateTime = dateUtil.setReturnToCurrent(returnDateTime, departDateTime);
                }

                // Adjust the depart time if needed
                if ((searchMode != SearchMode.OneWay) && departDateTime.before(returnDateTime)) {
                    returnDateTime.set(returnDateTime.get(Calendar.YEAR), returnDateTime.get(Calendar.MONTH),
                            returnDateTime.get(Calendar.DAY_OF_MONTH));

                }

            }

            updateDateTimeViews();

            calendarDialog.dismiss();
        }

    }

    /**
     * Handle the time selection event
     */
    class AirTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        private final int dialogId;
        private final Calendar cal;

        protected AirTimeSetListener(int dialogId, Calendar cal) {
            this.dialogId = dialogId;
            this.cal = cal;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            // TODO: Setting the time could cause a datetime overlap. Prevent
            // that.

            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);

            updateDateTimeViews();

            // Remove the dialog. This will force recreation and a reset of the
            // displayed values.
            // If we do not do this then the dialog may be shown with it's old
            // values (from the first
            // time around) instead of the newest values (if the underlying
            // Calendar value changes).
            removeDialog(dialogId);
        }

    }

    protected void doSearch() {
        Intent i = getSearchProgressIntent();

        i.putExtra(Const.EXTRA_SEARCH_MODE, searchMode.name());
        i.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocation.getBundle());
        i.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocation.getBundle());
        i.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);
        i.putExtra(Const.EXTRA_SEARCH_CABIN_CLASS, curCabinClass.id);
        i.putExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, refundableOnly);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        if (searchMode != SearchMode.OneWay) {
            i.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateTime);
        }

        startActivityForResult(i, RESULTS_ACTIVITY_CODE);
    }

    protected Intent getSearchProgressIntent() {
        return new Intent(this, AirSearchProgress.class);
    }

    /**
     * Get List of Air Classes
     * 
     * @return Spinner Items for user selection
     */
    private SpinnerItem[] getAirClassList() {
        ArrayList<SpinnerItem> cabinClassItems = new ArrayList<SpinnerItem>();
        UserConfig userConfig = ((ConcurCore) getApplication()).getUserConfig();
        if (userConfig != null) {
            ArrayList<String> airClass = userConfig.allowedAirClassService;
            if (airClass == null || airClass.size() == 0) {
                cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_ECONOMY,
                        R.string.air_search_class_value_economy));
            } else {
                cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_ECONOMY,
                        R.string.air_search_class_value_economy));
                for (int i = 0; i < airClass.size(); i++) {
                    if (airClass.get(i).equalsIgnoreCase(Const.AIR_SEAT_CLASS_VALUE_BUSINESS)) {
                        cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_BUSINESS,
                                R.string.air_search_class_value_business));
                    }
                    if (airClass.get(i).equalsIgnoreCase(Const.AIR_SEAT_CLASS_VALUE_FIRST)) {
                        cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_FIRST,
                                R.string.air_search_class_value_first));
                    }
                    if (airClass.get(i).equalsIgnoreCase(Const.AIR_SEAT_CLASS_VALUE_PREMIUM_ECONOMY)) {
                        cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_PREMIUM_ECONOMY,
                                R.string.air_search_class_value_premium_economy));
                    }
                    if (airClass.get(i).equalsIgnoreCase(Const.AIR_SEAT_CLASS_VALUE_ANY)) {
                        cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_ANY,
                                R.string.air_search_class_value_any));
                    }
                }
            }
        } else {
            // toast or dialog user doesnt have classes.
            Log.e(Const.LOG_TAG, CLS_TAG + ".initState: userConfig is null!");
            // Temporary hack: until we implement wait for system/user config in
            // 9.0, permit
            // an Economy choice.
            cabinClassItems.add(new SpinnerItem(Const.AIR_SEAT_CLASS_ECONOMY, R.string.air_search_class_value_economy));
        }
        SpinnerItem[] result = cabinClassItems.toArray(new SpinnerItem[cabinClassItems.size()]);
        return result;
    }

    private void showInvalidDatesFrag() {
        invalidDatesFrag = new AlertDialogFragment();
        invalidDatesFrag.setMessage(R.string.air_search_invalid_dates);
        invalidDatesFrag.setPositiveButtonText(R.string.okay);
        invalidDatesFrag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                invalidDatesFrag.dismiss();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }
        });
        invalidDatesFrag.show(getSupportFragmentManager(), null);
    }
}
