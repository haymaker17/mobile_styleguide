package com.concur.mobile.core.travel.car.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.car.data.CarType;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.BookingDateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.view.SearchListFormFieldView;
import com.concur.mobile.platform.ui.common.widget.CalendarPicker;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialog;
import com.concur.mobile.platform.util.Format;

public class CarSearch extends TravelBaseActivity {

    private static final String CLS_TAG = CarSearch.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = CarSearch.class.getSimpleName()
            + ".calendar.dialog.fragment";
    private static final String TAG_INVALID_DROP_OFF_DIALOG_FRAGMENT = CarSearch.class.getSimpleName()
            + ".invalid.dropoff.time.dialog.fragment";

    protected static final String LOCATION = "location";
    protected static final String PICKUP_DATETIME = "pickup_datetime";
    protected static final String DROPOFF_DATETIME = "dropoff_datetime";
    protected static final String CAR_TYPE = "car_type";
    private static final String KEY_DIALOG_ID = "key.dialog.id";
    private static final String KEY_IS_PICKUP = "key.is.pickup";

    protected static final int PICKUP_DATE_DIALOG = DIALOG_ID_BASE + 0;
    protected static final int PICKUP_TIME_DIALOG = DIALOG_ID_BASE + 1;
    protected static final int DROPOFF_DATE_DIALOG = DIALOG_ID_BASE + 2;
    protected static final int DROPOFF_TIME_DIALOG = DIALOG_ID_BASE + 3;
    protected static final int SEARCH_DIALOG = DIALOG_ID_BASE + 4;
    protected static final int NO_RESULTS_DIALOG = DIALOG_ID_BASE + 5;
    protected static final int CAR_TYPE_DIALOG = DIALOG_ID_BASE + 6;

    // Contains a Cliqbook trip id.
    protected String cliqbookTripId;
    // Contains a client locator.
    protected String clientLocator;
    // Contains a trip record locator
    protected String recordLocator;

    protected View locationView;

    protected View pickupDateView;
    protected View pickupTimeView;
    protected View dropoffDateView;
    protected View dropoffTimeView;

    protected View carTypeView;

    protected Button searchButton;

    protected Calendar pickupDateTime;
    protected Calendar dropoffDateTime;

    protected LocationChoice currentLocation;

    protected SpinnerItem[] carTypes;

    protected int defaultCarType;
    protected SpinnerItem currentCarType;

    private CalendarPickerDialog calendarDialog;
    private int DialogId = -1;
    private boolean isPickup = false;

    // /////////////////////////////////////////////////////////////////

    protected void initializeFieldState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // Initialize some values

            Intent intent = getIntent();

            // Check for a passed in location object.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_LOCATION)) {
                Bundle locationBundle = intent.getBundleExtra(Const.EXTRA_TRAVEL_LOCATION);
                if (locationBundle != null) {
                    currentLocation = new LocationChoice(locationBundle);
                } else {
                    currentLocation = null;
                }
            } else {
                currentLocation = null;
            }
            // Check for a passed in itinerary locator.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID)) {
                cliqbookTripId = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            }
            // Check for passed in client locator.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR)) {
                clientLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR);
            }
            // Check for passed in record locator.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR)) {
                recordLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
            }
            // Check for passed in pick-up date.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR)) {
                pickupDateTime = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR);
            } else {
                // We set the pickup/dropoff to be one day apart initially.
                // Later they just have
                // to be before/after (even by 1 minute)

                // We're specifically getting 'now' in the device local timezone
                // because then
                // we just pull the date values out and use them to populate our
                // UTC-standard
                // calendar.
                Calendar now = Calendar.getInstance();
                pickupDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                pickupDateTime.clear();
                pickupDateTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 9,
                        0, 0);
            }
            // Check for passed in drop-off date.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR)) {
                dropoffDateTime = (Calendar) intent
                        .getSerializableExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR);
            } else {
                dropoffDateTime = (Calendar) pickupDateTime.clone();
                dropoffDateTime.set(Calendar.HOUR, 17);
                dropoffDateTime.add(Calendar.DAY_OF_MONTH, 1);
            }

            currentCarType = null;

        } else {

            Bundle savedLoc = savedInstanceState.getBundle(LOCATION);
            if (savedLoc != null) {
                currentLocation = new LocationChoice(savedLoc);
            }

            pickupDateTime = (Calendar) savedInstanceState.getSerializable(PICKUP_DATETIME);
            dropoffDateTime = (Calendar) savedInstanceState.getSerializable(DROPOFF_DATETIME);

            currentCarType = SpinnerItem.findById(carTypes, savedInstanceState.getString(CAR_TYPE));

            cliqbookTripId = savedInstanceState.getString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            clientLocator = savedInstanceState.getString(Const.EXTRA_TRAVEL_CLIENT_LOCATOR);
            recordLocator = savedInstanceState.getString(Const.EXTRA_TRAVEL_RECORD_LOCATOR);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the location
        if (currentLocation != null) {
            outState.putBundle(LOCATION, currentLocation.getBundle());
        } else {
            outState.putBundle(LOCATION, null);
        }

        // Grab the date and time fields and save them
        outState.putSerializable(PICKUP_DATETIME, pickupDateTime);
        outState.putSerializable(DROPOFF_DATETIME, dropoffDateTime);

        // Save the itinerary locator field.
        outState.putString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        // Save the client locator.
        outState.putString(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
        // Save the client locator.
        outState.putString(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);

        // Save car type
        if (currentCarType != null) {
            outState.putString(CAR_TYPE, currentCarType.id);
        } else {
            outState.putString(CAR_TYPE, null);
        }

        outState.putInt(KEY_DIALOG_ID, DialogId);
        outState.putBoolean(KEY_IS_PICKUP, isPickup);
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
                Intent i = new Intent(CarSearch.this, VoiceCarSearchActivity.class);
                CarSearch.this.startActivity(i);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_search);

        // Set the header.
        getSupportActionBar().setTitle(R.string.car_search_title);

        loadCarTypes();

        initializeFieldState(savedInstanceState);

        searchButton = (Button) findViewById(R.id.footer_button_one);
        if (searchButton != null) {
            searchButton.setText(R.string.general_search);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'footer_button_one' button!");
        }

        configureLocation();
        configureDateButtons();
        configureCarType();

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Check for connectivity, if none, then display dialog and
                // return.
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    return;
                }
                // ValidateTravelCustomFields will display a dialog and return
                // 'false' if any displayed
                // fields have missing or invalid values.
                if (validateTravelCustomFields()) {
                    // Commit the travel custom fields.
                    commitTravelCustomFields();
                    if (validateDateTimeFields()) {
                        doSearch();
                    }
                }
            }
        });

        restoreReceivers();

        // Fetch booking information fields if created on a non-orientation
        // change
        // and no passed in trip.
        if (!orientationChange) {
            if (cliqbookTripId == null) {
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
        }

        calendarDialog = (CalendarPickerDialog) getSupportFragmentManager().findFragmentByTag(
                TAG_CALENDAR_DIALOG_FRAGMENT);
        if (calendarDialog != null) {
            if (savedInstanceState.containsKey(KEY_DIALOG_ID) && savedInstanceState.containsKey(KEY_IS_PICKUP)) {
                calendarDialog.setOnDateSetListener(new CarDateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID),
                        savedInstanceState.getBoolean(KEY_IS_PICKUP)));
            }
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        dlg = super.onCreateDialog(id);
        if (dlg == null) {
            switch (id) {
            case PICKUP_TIME_DIALOG:
                dlg = new TimePickerDialog(this, new CarTimeSetListener(id, pickupDateTime),
                        pickupDateTime.get(Calendar.HOUR_OF_DAY), pickupDateTime.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this));
                break;
            case DROPOFF_TIME_DIALOG:
                dlg = new TimePickerDialog(this, new CarTimeSetListener(id, dropoffDateTime),
                        dropoffDateTime.get(Calendar.HOUR_OF_DAY), dropoffDateTime.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this));
                break;
            case SEARCH_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.searching_for_cars));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dlg = dialog;
                break;
            case NO_RESULTS_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.search_no_result_dialog_title);
                dlgBldr.setMessage(R.string.car_search_no_result_dialog_text);
                dlgBldr.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case CAR_TYPE_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, carTypes) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (currentCarType != null) {
                    for (int i = 0; i < carTypes.length; i++) {
                        if (currentCarType.id.equals(carTypes[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }

                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentCarType = carTypes[which];
                        updateCarTypeView();
                        removeDialog(CAR_TYPE_DIALOG);
                    }
                });

                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeDialog(CAR_TYPE_DIALOG);
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
    protected boolean getDisplayAtStart() {
        return true;
    }

    private void showCalendarDialog(int id) {
        Bundle bundle;

        calendarDialog = new CalendarPickerDialog();
        bundle = new Bundle();
        switch (id) {
        case PICKUP_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, pickupDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, pickupDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, pickupDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            isPickup = true;
            break;
        case DROPOFF_DATE_DIALOG:
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, dropoffDateTime.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, dropoffDateTime.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, dropoffDateTime.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            isPickup = false;
            break;
        }

        DialogId = id;
        calendarDialog.setArguments(bundle);
        calendarDialog.setOnDateSetListener(new CarDateSetListener(id, isPickup));
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Update the button with the current location selection
     */
    protected void updateLocationView() {
        if (currentLocation == null) {
            SpannableString s = new SpannableString(getText(R.string.search_location_prompt));
            s.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (locationView != null) {
                TextView txtView = (TextView) locationView.findViewById(R.id.field_value);
                if (txtView != null) {
                    txtView.setText(s);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".updateLocationView: unable to locate 'field_value' in location view!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateLocationView: location view is null!");
            }
            searchButton.setEnabled(false);
        } else {
            if (locationView != null) {
                TextView txtView = (TextView) locationView.findViewById(R.id.field_value);
                if (txtView != null) {
                    txtView.setText(currentLocation.getName());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".updateLocationView: unable to locate 'field_value' in location view!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateLocationView: location view is null!");
            }
            searchButton.setEnabled(true);
        }
    }

    /**
     * Handle the initial setup of the location button
     */
    protected void configureLocation() {

        // Grab our control
        locationView = findViewById(R.id.car_search_loc);
        if (locationView != null) {
            // Set the field name.
            TextView txtView = (TextView) locationView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.car_search_label_location);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureLocation: unable to locate 'field_name' in locationView!");
            }
            // Hook up the handler
            locationView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!ConcurCore.isConnected()) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    } else {
                        Intent i = new Intent(CarSearch.this, LocationSearch.class);
                        i.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, LocationSearch.SEARCH_AIRPORTS);
                        startActivityForResult(i, Const.REQUEST_CODE_LOCATION);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureLocation: unable to locate 'car_search_loc' view!");
        }

        // Initialize the display
        updateLocationView();

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Pickup and Dropoff field methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Handle the date selection event Adjust the pickup/dropoff time as necessary to prevent bad times.
     */
    class CarDateSetListener implements CalendarPickerDialog.OnDateSetListener {

        private final int dialogId;

        // We need to know which date we are setting so we can adjust, therefore
        // a
        // boolean instead of a reference to the calendar
        private final boolean isPickup;

        CarDateSetListener(int dialogId, boolean isPickup) {
            this.dialogId = dialogId;
            this.isPickup = isPickup;
        }

        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            BookingDateUtil dateUtil = new BookingDateUtil();
            if (isPickup) {
                pickupDateTime.set(year, monthOfYear, dayOfMonth);

                // Adjust the dropoff time if needed
                if (pickupDateTime.after(dropoffDateTime)) {
                    dropoffDateTime.set(pickupDateTime.get(Calendar.YEAR), pickupDateTime.get(Calendar.MONTH),
                            pickupDateTime.get(Calendar.DAY_OF_MONTH));
                }

                if (dateUtil.isDateInValid(pickupDateTime, null, true)) {
                    pickupDateTime = dateUtil.setDepartToCurrent(pickupDateTime, dropoffDateTime,
                            dateUtil.getCurrentTime());
                    // Adjust the return time if needed
                    if (pickupDateTime.after(dropoffDateTime)) {
                        dropoffDateTime = dateUtil.setReturnToCurrent(dropoffDateTime, pickupDateTime);
                    }
                }

            } else {
                dropoffDateTime.set(year, monthOfYear, dayOfMonth);

                if (dateUtil.isDateInValid(pickupDateTime, dropoffDateTime, false)) {
                    dropoffDateTime = dateUtil.setReturnToCurrent(dropoffDateTime, pickupDateTime);
                }

                // Adjust the pickup time if needed
                if (dropoffDateTime.before(pickupDateTime)) {
                    pickupDateTime.set(dropoffDateTime.get(Calendar.YEAR), dropoffDateTime.get(Calendar.MONTH),
                            dropoffDateTime.get(Calendar.DAY_OF_MONTH));
                }
            }

            updateDateTimeViews();

            // Remove the dialog. This will force recreation and a reset of the
            // displayed values.
            // If we do not do this then the dialog may be shown with it's old
            // values (from the first
            // time around) instead of the newest values (if the underlying
            // Calendar value changes).
            calendarDialog.dismiss();
        }

    }

    /**
     * Handle the time selection event
     */
    class CarTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        private final int dialogId;
        private final Calendar cal;

        protected CarTimeSetListener(int dialogId, Calendar cal) {
            this.dialogId = dialogId;
            this.cal = cal;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

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

    /**
     * Update the view with current pick-up/drop-off date/time information.
     */
    protected void updateDateTimeViews() {

        // Force our timezone for formatting to UTC since that's what we use
        // everywhere internally.
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (pickupDateView != null) {
            TextView txtView = (TextView) pickupDateView.findViewById(R.id.field_value);
            if (txtView != null) {
                txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY,
                        pickupDateTime));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".updateDateTimeView: unable to locate 'field_value' for pickup date view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDateTimeViews: pickupDateView is null!");
        }

        if (pickupTimeView != null) {
            TextView txtView = (TextView) pickupTimeView.findViewById(R.id.field_value);
            if (txtView != null) {
                txtView.setText(Format.safeFormatCalendar(timeFormat, pickupDateTime));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".updateDateTimeView: unable to locate 'field_value' for pickup time view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDateTimeViews: pickupTimeView is null!");
        }

        if (dropoffDateView != null) {
            TextView txtView = (TextView) dropoffDateView.findViewById(R.id.field_value);
            if (txtView != null) {
                txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY,
                        dropoffDateTime));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".updateDateTimeView: unable to locate 'field_value' for dropoff date view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDateTimeViews: dropoffDateView is null!");
        }

        if (dropoffTimeView != null) {
            TextView txtView = (TextView) dropoffTimeView.findViewById(R.id.field_value);
            if (txtView != null) {
                txtView.setText(Format.safeFormatCalendar(timeFormat, dropoffDateTime));
                validateDateTimeFields();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".updateDateTimeView: unable to locate 'field_value' for dropoff time view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDateTimeViews: dropoffTimeView is null!");
        }
    }

    /**
     * Initial configuration of the pick-up/drop-off date/time views.
     */
    protected void configureDateButtons() {

        // Configure the pick-up date view.
        pickupDateView = findViewById(R.id.car_search_pickup_date);
        if (pickupDateView != null) {
            // Set the field label.
            TextView txtView = (TextView) pickupDateView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.car_search_label_pickup_date);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".configureDateButtons: unable to locate 'field_name' view for pickup date view!");
            }
            // Set up the handler.
            pickupDateView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showCalendarDialog(PICKUP_DATE_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate 'car_search_pickup_date' view!");
        }
        // Configure the pick-up time view.
        pickupTimeView = findViewById(R.id.car_search_pickup_time);
        if (pickupTimeView != null) {
            // Set the field label.
            TextView txtView = (TextView) pickupTimeView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.car_search_label_pickup_time);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".configureDateButtons: unable to locate 'field_name' view for pickup time view!");
            }
            // Set up the handler.
            pickupTimeView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(PICKUP_TIME_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate 'car_search_pickup_time' view!");
        }
        // Configure the drop-off date view.
        dropoffDateView = findViewById(R.id.car_search_dropoff_date);
        if (dropoffDateView != null) {
            // Set the field label.
            TextView txtView = (TextView) dropoffDateView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.car_search_label_dropoff_date);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".configureDateButtons: unable to locate 'field_name' view for drop-off date view!");
            }
            // Set up the handler.
            dropoffDateView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showCalendarDialog(DROPOFF_DATE_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate 'car_search_dropoff_date' view!");
        }

        // Configure the drop-off time view.
        dropoffTimeView = findViewById(R.id.car_search_dropoff_time);
        if (dropoffTimeView != null) {
            // Set the field label.
            TextView txtView = (TextView) dropoffTimeView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.car_search_label_dropoff_time);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".configureDateButtons: unable to locate 'field_name' view for drop-off time view!");
            }
            // Set up the handler.
            dropoffTimeView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(DROPOFF_TIME_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate 'car_search_pickup_time' view!");
        }

        // Initialize their display
        updateDateTimeViews();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Pickup and Dropoff field methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Car type spinner methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Loads the car-type selection list.
     */
    protected void loadCarTypes() {

        // Load up our car types from user config
        ArrayList<SpinnerItem> carTypeList = new ArrayList<SpinnerItem>();
        UserConfig uc = ((ConcurCore) getApplication()).getUserConfig();
        if (uc != null) {
            ArrayList<CarType> allowedCars = uc.allowedCarTypes;

            // According to Market dumps, some folks are getting here with a
            // null car types list.
            // Deal with it even if we have no idea how it happens.
            if (allowedCars == null) {
                carTypeList.add(new SpinnerItem("", ""));
            } else {
                int size = allowedCars.size();
                for (int i = 0; i < size; i++) {
                    CarType ct = allowedCars.get(i);
                    if (ct.isDefault) {
                        defaultCarType = i;
                    }

                    SpinnerItem si = new SpinnerItem(ct.code, ct.description);
                    carTypeList.add(si);
                }
            }
        }
        carTypes = carTypeList.toArray(new SpinnerItem[carTypeList.size()]);

    }

    /**
     * Configures the car-type selection control.
     */
    protected void configureCarType() {
        carTypeView = findViewById(R.id.car_type);
        if (carTypeView != null) {
            // Set the field name.
            TextView txtView = (TextView) carTypeView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.car_search_label_car_type);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureCarType: unable to locate 'field_name' within carTypeView!");
            }
            // Set up the handler.
            carTypeView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(CAR_TYPE_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureCarType: unable to locate 'car_type' view!");
        }

        // Default our saved selection
        if (currentCarType == null && carTypes.length > 0) {
            currentCarType = carTypes[defaultCarType];
        }

        // Update the car type view.
        updateCarTypeView();

    }

    /**
     * Updates the car-type selection control.
     */
    protected void updateCarTypeView() {

        if (carTypeView != null) {
            TextView txtView = (TextView) carTypeView.findViewById(R.id.field_value);
            if (txtView != null) {
                String currentCarTypeStr = "";
                if (currentCarType != null) {
                    currentCarTypeStr = currentCarType.name;
                }
                txtView.setText(currentCarTypeStr);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateCarTypeView: unable to locate 'field_value' in carTypeView!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateCarTypeView: carTypeView is null!");
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Car type spinner methods - end
    // ///////////////////////////////////////////////////////////////////////////

    protected void doSearch() {

        // Launch the activity that will actually kick-off the search andS
        // display a progress message.
        Intent intent = new Intent(this, CarSearchProgress.class);
        intent.putExtra(Const.EXTRA_TRAVEL_LOCATION, currentLocation.getName());
        intent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, currentLocation.latitude);
        intent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, currentLocation.longitude);
        intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, pickupDateTime));
        intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR, pickupDateTime);
        intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, dropoffDateTime));
        intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR, dropoffDateTime);
        String carType;
        if (currentCarType != null) {
            carType = currentCarType.id;
        } else {
            // Somehow we got here without something being selected. Not sure
            // how this happens
            // because there is always at least one car type value (Any) coming
            // back from the server
            // and that should be selected when the spinner is created.
            // Set it to a blank string which equates to Any.
            carType = "";
        }
        intent.putExtra(Const.EXTRA_TRAVEL_CAR_TYPE, carType);
        intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        intent.putExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, clientLocator);
        intent.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, recordLocator);
        intent.putExtra(PICKUP_DATETIME, pickupDateTime);
        intent.putExtra(DROPOFF_DATETIME, dropoffDateTime);
        intent.putExtra(Const.EXTRA_LOCATION_IATA, currentLocation.iata);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        startActivityForResult(intent, Const.REQUEST_CODE_BOOK_CAR);
    }

    /**
     * Will validate the Date and Time fields. If the fields are found to be invalid, then this method will display a dialog.
     * 
     * @return returns whether the custom fields had invalid values.
     */
    protected boolean validateDateTimeFields() {
        boolean retVal = true;

        if (dropoffTimeView != null && pickupTimeView != null) {
            if (dropoffDateTime.compareTo(pickupDateTime) == -1) {
                DialogFragmentFactory.getAlertOkayInstance(getString(R.string.car_search_invalid_drop_off_time_title),
                        getString(R.string.car_search_invalid_drop_off_time)).show(getSupportFragmentManager(),
                        TAG_INVALID_DROP_OFF_DIALOG_FRAGMENT);
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        case Const.REQUEST_CODE_LOCATION:
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                currentLocation = new LocationChoice(locBundle);
                updateLocationView();
            }
            break;
        case Const.REQUEST_CODE_BOOK_CAR:
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
            break;
        }
    }

}
