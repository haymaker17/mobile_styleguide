/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.travel.activity.LocationSearch;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.data.CompanyLocation;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.BookingDateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ConcurActivity</code> for the purposes of providing hotel search services.
 * 
 * @author AndrewK
 */
public class HotelSearch extends TravelBaseActivity {

    private static final String CLS_TAG = HotelSearch.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = HotelSearch.class.getSimpleName()
            + ".calendar.dialog.fragment";

    private CalendarPickerDialog calendarDialog;

    protected static final int LOCATION_ACTIVITY_CODE = 0;

    protected static final String LOCATION_TYPE = "location_type";
    protected static final String LOCATION = "location";
    protected static final String CHECK_IN_DATE = "check_in_date";
    protected static final String CHECK_OUT_DATE = "check_out_date";
    protected static final String DISTANCE_UNIT = "distance_unit";
    protected static final String DISTANCE_VALUE = "distance_value";
    protected static final String NAMES_CONTAINING = "names_containing";
    protected static final String FROM_LOCATION_SEARCH_INTENT = "from_location_search_intent";
    protected static final String BOOK_NEAR_ME = "book_near_me";
    private static final String KEY_DIALOG_ID = "key.dialog.id";
    private static final String KEY_IS_CHECKIN = "key.is.checkin";

    protected static final int CHECK_IN_DATE_DIALOG = DIALOG_ID_BASE + 0;
    protected static final int CHECK_OUT_DATE_DIALOG = DIALOG_ID_BASE + 1;
    protected static final int DISTANCE_DIALOG = DIALOG_ID_BASE + 2;
    protected static final int DISTANCE_UNIT_DIALOG = DIALOG_ID_BASE + 3;

    protected View location;

    protected View checkInDateView;
    protected View checkOutDateView;

    protected View distance;
    protected View distanceUnit;

    protected Calendar checkInDate;
    protected Calendar checkOutDate;

    // Contains the Cliqbook trip id if a hotel search is being performed in the
    // context
    // of a trip.
    protected String cliqbookTripId;

    protected Button searchButton;

    protected LocationChoice currentLocation;

    // NOTE: The data contained in the SpinnerItem objects below may need to
    // come from
    // company configuration information.

    // Distance drop-down.
    protected final static SpinnerItem[] distanceItems = new SpinnerItem[] { new SpinnerItem("1", "1"),
            new SpinnerItem("2", "2"), new SpinnerItem("5", "5"), new SpinnerItem("10", "10"),
            new SpinnerItem("15", "15"), new SpinnerItem("25", "25"), new SpinnerItem("100", "> 25") };

    // Distance value.
    protected SpinnerItem currentDistanceAmount;

    // Distance unit options.
    protected final static SpinnerItem[] distanceUnits = new SpinnerItem[] {
            new SpinnerItem("M", R.string.search_distance_unit_miles),
            new SpinnerItem("K", R.string.search_distance_unit_km), };

    protected SpinnerItem currentDistanceUnit;

    protected EditText withNamesContaining;

    protected final IntentFilter hotelResultsFilter = new IntentFilter(Const.ACTION_HOTEL_SEARCH_RESULTS);

    protected String actionStatusErrorMessage;

    protected String lastHttpErrorMessage;
    protected boolean fromLocationSearchIntent;
    protected boolean searchNearMe;

    private int DialogId = -1;
    private boolean isCheckin = false;

    // /////////////////////////////////////////////////////////////////

    protected void initializeFieldState(Bundle savedInstanceState) {

        // Grab a reference to the "with names containing" edit text.
        View nameScopeView = findViewById(R.id.hotel_search_name_scope);
        if (nameScopeView != null) {
            // Set the field name.
            TextView txtView = (TextView) nameScopeView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_label_names_containing);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initializeFieldState: unable to locate field_name!");
            }
            withNamesContaining = (EditText) nameScopeView.findViewById(R.id.field_value);
            if (withNamesContaining != null) {
                withNamesContaining.setText("");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".initializeFieldState: unable to locate names containing edit text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initializeFieldState: unable to locate hotel_search_name_scope view!");
        }

        if (savedInstanceState == null) {
            // Grab the calling intent and default to any passed in trip/date
            // information.
            Intent intent = getIntent();
            // Check for passed in location.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_LOCATION)) {
                Bundle locationBundle = intent.getBundleExtra(Const.EXTRA_TRAVEL_LOCATION);
                if (locationBundle != null) {
                    currentLocation = new LocationChoice(locationBundle);
                    fromLocationSearchIntent = true;
                } else {
                    currentLocation = null;
                }
            } else {
                currentLocation = null;
            }
            // Check for Cliqbook trip id.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID)) {
                cliqbookTripId = intent.getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            }
            // Check for check-in date.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN)) {
                checkInDate = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN);
            } else {
                // Initialize the check-in date to today.
                // We're specifically getting 'now' in the device local timezone
                // because then
                // we just pull the date values out and use them to populate our
                // UTC-standard
                // calendar.
                Calendar now = Calendar.getInstance();

                checkInDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                checkInDate.clear();
                checkInDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0,
                        0);
            }
            // Check for check-out date.
            if (intent.hasExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT)) {
                checkOutDate = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);
            } else {
                // Make check-out one day beyond check-in.
                checkOutDate = (Calendar) checkInDate.clone();
                checkOutDate.add(Calendar.DAY_OF_MONTH, 1);
            }

            distance = findViewById(R.id.hotel_search_distance);
            if (distance == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initializeFieldState: can't locate distance view!");
            }
        } else {
            // Restore location.
            if (savedInstanceState.getBundle(LOCATION) != null) {
                int searchMode = savedInstanceState.getInt(LOCATION_TYPE);
                Bundle locBundle = savedInstanceState.getBundle(LOCATION);
                switch (searchMode) {
                case LocationSearch.SEARCH_COMPANY_LOCATIONS: {
                    currentLocation = new CompanyLocation(locBundle);
                    break;
                }
                case LocationSearch.SEARCH_CUSTOM: {
                    currentLocation = new LocationChoice(locBundle);
                    break;
                }
                default: {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initializeFieldState: invalid search mode of '" + searchMode
                            + "' on result intent.");
                }
                }

                fromLocationSearchIntent = savedInstanceState.getBoolean(FROM_LOCATION_SEARCH_INTENT);
            }
            // Restore check in/out dates.
            checkInDate = (Calendar) savedInstanceState.getSerializable(CHECK_IN_DATE);
            checkOutDate = (Calendar) savedInstanceState.getSerializable(CHECK_OUT_DATE);
            // Restore Cliqbook trip id.
            cliqbookTripId = savedInstanceState.getString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID);
            // Restore distance unit.
            currentDistanceUnit = SpinnerItem.findById(distanceUnits, savedInstanceState.getString(DISTANCE_UNIT));
            // Restore distance value.
            currentDistanceAmount = SpinnerItem.findById(distanceItems, savedInstanceState.getString(DISTANCE_VALUE));

            // Restore with names containing field.
            if (withNamesContaining != null) {
                String savedStr = savedInstanceState.getString(NAMES_CONTAINING);
                if (savedStr != null) {
                    withNamesContaining.setText(savedStr);
                }
            }

            // book near me
            searchNearMe = savedInstanceState.getBoolean(BOOK_NEAR_ME);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the location
        if (currentLocation != null) {
            // Store the type of location object so that upon unpacking the
            // correct location object
            // can unpack the bundle.
            if (currentLocation instanceof CompanyLocation) {
                outState.putInt(LOCATION_TYPE, LocationSearch.SEARCH_COMPANY_LOCATIONS);
            } else {
                outState.putInt(LOCATION_TYPE, LocationSearch.SEARCH_CUSTOM);
            }
            outState.putBundle(LOCATION, currentLocation.getBundle());
            outState.putBoolean(FROM_LOCATION_SEARCH_INTENT, fromLocationSearchIntent);
        } else {
            outState.putBundle(LOCATION, null);
        }

        // Grab the date fields and save them
        outState.putSerializable(CHECK_IN_DATE, checkInDate);
        outState.putSerializable(CHECK_OUT_DATE, checkOutDate);

        // Save the trip record locator.
        outState.putString(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);

        // Save distance unit.
        if (currentDistanceUnit != null) {
            outState.putString(DISTANCE_UNIT, currentDistanceUnit.id);
        } else {
            outState.putString(DISTANCE_UNIT, null);
        }

        // Save distance value.
        if (currentDistanceAmount != null) {
            outState.putString(DISTANCE_VALUE, currentDistanceAmount.id);
        } else {
            outState.putString(DISTANCE_VALUE, null);
        }

        // Save out the "with names containing" information.
        if (withNamesContaining != null) {
            outState.putString(NAMES_CONTAINING, withNamesContaining.getText().toString());
        } else {
            outState.putString(NAMES_CONTAINING, null);
        }

        // save the search near me
        outState.putBoolean(BOOK_NEAR_ME, searchNearMe);

        outState.putBoolean(KEY_IS_CHECKIN, isCheckin);
        outState.putInt(KEY_DIALOG_ID, DialogId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // No-op. In the 'onCreate' method below the values for the fields
        // are being reset. The default implementation appears to restore the
        // UI values to blank values so this no-op override blocks our reset
        // values from being trounced.
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
                Intent i = new Intent(HotelSearch.this, VoiceHotelSearchActivity.class);
                HotelSearch.this.startActivity(i);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.hotel_search);

        // Set the header.
        getSupportActionBar().setTitle(R.string.hotel_search_title);

        initializeFieldState(savedInstanceState);

        searchButton = (Button) findViewById(R.id.footer_button_one);
        if (searchButton != null) {
            searchButton.setText(getText(R.string.general_search));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'footer_button_one' button!");
        }

        configureLocationButton();
        configureDateButtons();
        configureDistanceValue();
        configureDistanceUnit();

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
                    doSearch();
                }
            }
        });

        checkSearchButton();

        restoreReceivers();

        // Fetch booking information fields if created on a non-orientation
        // change
        // and no passed in trip
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
            if (savedInstanceState.containsKey(KEY_DIALOG_ID) && savedInstanceState.containsKey(KEY_IS_CHECKIN)) {
                calendarDialog.setOnDateSetListener(new HotelDateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID),
                        savedInstanceState.getBoolean(KEY_IS_CHECKIN)));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        dlg = super.onCreateDialog(id);
        if (dlg == null) {
            switch (id) {
            case DISTANCE_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, distanceItems) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (currentDistanceAmount != null) {
                    for (int i = 0; i < distanceItems.length; i++) {
                        if (currentDistanceAmount.id.equals(distanceItems[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }

                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentDistanceAmount = distanceItems[which];
                        updateDistance();
                        removeDialog(DISTANCE_DIALOG);
                    }
                });

                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeDialog(DISTANCE_DIALOG);
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case DISTANCE_UNIT_DIALOG: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, distanceUnits) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };

                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int selectedItem = -1;
                if (currentDistanceUnit != null) {
                    for (int i = 0; i < distanceUnits.length; i++) {
                        if (currentDistanceUnit.id.equals(distanceUnits[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }

                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentDistanceUnit = distanceUnits[which];
                        updateDistanceUnit();
                        removeDialog(DISTANCE_UNIT_DIALOG);
                    }
                });

                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeDialog(DISTANCE_UNIT_DIALOG);
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            default: {
                ConcurCore ConcurCore = (ConcurCore) getApplication();
                dlg = ConcurCore.createDialog(this, id);
            }
            }
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
        case Const.DIALOG_TRAVEL_SEARCH_HOTEL_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        }
    }

    @Override
    protected boolean getDisplayAtStart() {
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Update the button with the current location selection
     */
    protected void updateLocationButton() {
        if (currentLocation == null) {
            SpannableString s = new SpannableString(getText(R.string.search_location_prompt));
            s.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView txtView = (TextView) location.findViewById(R.id.field_value);
            if (txtView != null) {
                if (cliqbookTripId == null) {
                    // a new itinerary, hence default to current location
                    configureBookNearMe();
                    searchNearMe = getIntent().getBooleanExtra(BOOK_NEAR_ME, false);
                    if (searchNearMe) {
                        Address currAdd = ((ConcurCore) ConcurCore.getContext()).getCurrentAddress();
                        if (currAdd == null) {
                            txtView.setText(s);
                            // Display a toast message indicating current
                            // location cannot be determined.
                            String toastText = getText(R.string.dlg_no_current_location).toString();
                            Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            currentLocation = new LocationChoice();
                            StringBuilder strBldr = new StringBuilder();
                            strBldr.append(currAdd.getLocality());
                            strBldr.append(", ");
                            strBldr.append(currAdd.getCountryCode());
                            currentLocation.setName(strBldr.toString());// used
                                                                        // for
                                                                        // displaying
                                                                        // in
                                                                        // the
                                                                        // search
                                                                        // progress
                                                                        // screen
                            currentLocation.latitude = Double.toString(currAdd.getLatitude());
                            currentLocation.longitude = Double.toString(currAdd.getLongitude());
                            txtView.setText(R.string.general_current_location);
                        }
                    } else {
                        txtView.setText(s);
                    }
                } else {
                    txtView.setText(s);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateLocationButton: unable to locate location field_value!");
            }
        } else {
            if (cliqbookTripId == null) {
                configureBookNearMe();
            }

            TextView txtView = (TextView) location.findViewById(R.id.field_value);
            if (txtView != null) {
                if (fromLocationSearchIntent) {
                    txtView.setText(currentLocation.getName());
                } else {
                    txtView.setText(R.string.general_current_location);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateLocationButton: unable to locate location field_value!");
            }
        }
    }

    private void showCalendarDialog(int id) {
        Bundle bundle;

        calendarDialog = new CalendarPickerDialog();
        bundle = new Bundle();
        switch (id) {
        case CHECK_IN_DATE_DIALOG: {
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, checkInDate.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, checkInDate.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, checkInDate.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            isCheckin = true;
            break;
        }
        case CHECK_OUT_DATE_DIALOG: {
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, checkOutDate.get(Calendar.YEAR));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, checkOutDate.get(Calendar.MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, checkOutDate.get(Calendar.DAY_OF_MONTH));
            bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);
            isCheckin = false;
            break;
        }
        }

        DialogId = id;
        calendarDialog.setArguments(bundle);
        calendarDialog.setOnDateSetListener(new HotelDateSetListener(id, isCheckin));
        calendarDialog.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    /**
     * Will update the distance view based on the current spinner item value.
     */
    protected void updateDistance() {
        TextView txtView = (TextView) distance.findViewById(R.id.field_value);
        if (txtView != null) {
            if (currentDistanceAmount != null) {
                txtView.setText(currentDistanceAmount.name);
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDistance: unable to locate distance field_value!");
        }
    }

    /**
     * Will update the distance unit view based on the current spinner value.
     */
    protected void updateDistanceUnit() {
        TextView txtView = (TextView) distanceUnit.findViewById(R.id.field_value);
        if (txtView != null) {
            if (currentDistanceUnit != null) {
                txtView.setText(currentDistanceUnit.name);
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDistanceUnit: unable to locate distance_unit field_value!");
        }
    }

    /**
     * Handle the initial setup of the location button
     */
    protected void configureLocationButton() {

        // Grab our control
        location = findViewById(R.id.hotel_search_loc);
        if (location != null) {
            TextView txtView = (TextView) location.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_label_location);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureLocationButton: unable to locate field_name!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureLocationButton: unable to locate hotel_search_loc!");
        }

        // Initialize the display
        updateLocationButton();

        // Hook up the handler
        location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                } else {
                    // Determine if there are any company locations, if so, then
                    // pass that flag into
                    // the location search activity.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    SystemConfig sysConfig = ConcurCore.getSystemConfig();
                    int locationSearchMode = LocationSearch.SEARCH_CUSTOM;
                    if (sysConfig != null && sysConfig.getCompanyLocations() != null
                            && sysConfig.getCompanyLocations().size() > 0) {
                        locationSearchMode |= LocationSearch.SEARCH_COMPANY_LOCATIONS;
                    }

                    Intent intent = new Intent(HotelSearch.this, LocationSearch.class);
                    intent.putExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, locationSearchMode);
                    startActivityForResult(intent, Const.REQUEST_CODE_LOCATION);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE: {
            // MOB-14331
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
        case Const.REQUEST_CODE_LOCATION: {
            if (resultCode == RESULT_OK) {
                Bundle locBundle = data.getBundleExtra(LocationChoice.LOCATION_BUNDLE);
                // obtain the search mode that provided the choice.
                int searchMode = data.getIntExtra(Const.EXTRA_LOCATION_SEARCH_MODE_USED, -1);
                if (searchMode != -1) {
                    switch (searchMode) {
                    case LocationSearch.SEARCH_COMPANY_LOCATIONS: {
                        currentLocation = new CompanyLocation(locBundle);
                        break;
                    }
                    case LocationSearch.SEARCH_CUSTOM: {
                        currentLocation = new LocationChoice(locBundle);
                        break;
                    }
                    default: {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: invalid search mode of '" + searchMode
                                + "' on result intent.");
                    }
                    }
                    // flag to be saved in the instance state for showing the
                    // text in the location field
                    fromLocationSearchIntent = true;
                    updateLocationButton();
                    checkSearchButton();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: no search mode used flag set on result intent.");
                }
            }
            break;
        }
        case Const.REQUEST_CODE_BOOK_HOTEL: {
            if (resultCode == RESULT_OK) {
                // Hotel was booked, set the result code to okay.
                setResult(Activity.RESULT_OK, data);
                finish();
            }
            break;
        }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Check in/out field methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Handle the date selection event Adjust the check in/out time as necessary to prevent invalid ordering.
     */
    class HotelDateSetListener implements CalendarPickerDialog.OnDateSetListener {

        private final int dialogId;

        // We need to know which date we are setting so we can adjust, therefore
        // a
        // boolean instead of a reference to the calendar
        private final boolean isCheckIn;

        HotelDateSetListener(int dialogId, boolean isCheckIn) {
            this.dialogId = dialogId;
            this.isCheckIn = isCheckIn;
        }

        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            BookingDateUtil dateUtil = new BookingDateUtil();
            if (isCheckIn) {
                checkInDate.set(year, monthOfYear, dayOfMonth);

                // Adjust the check-out time if needed
                if (checkInDate.after(checkOutDate) || checkInDate.equals(checkOutDate)) {
                    checkOutDate.set(checkInDate.get(Calendar.YEAR), checkInDate.get(Calendar.MONTH),
                            checkInDate.get(Calendar.DAY_OF_MONTH));
                    checkOutDate.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (dateUtil.isDateInValid(checkInDate, null, true)) {
                    checkInDate = dateUtil.setDepartToCurrent(checkInDate, checkOutDate, dateUtil.getCurrentTime());
                    // Adjust the return time if needed
                    if (checkInDate.after(checkOutDate)) {
                        checkOutDate = dateUtil.setReturnToCurrent(checkOutDate, checkInDate);
                    }
                }

            } else {
                checkOutDate.set(year, monthOfYear, dayOfMonth);

                if (dateUtil.isDateInValid(checkInDate, checkOutDate, false)) {
                    checkOutDate = dateUtil.setReturnToCurrent(checkOutDate, checkInDate);
                }

                // Adjust the check-in time if needed
                if (checkOutDate.before(checkInDate) || checkOutDate.equals(checkInDate)) {
                    checkInDate.set(checkOutDate.get(Calendar.YEAR), checkOutDate.get(Calendar.MONTH),
                            checkOutDate.get(Calendar.DAY_OF_MONTH));
                    checkInDate.add(Calendar.DAY_OF_MONTH, -1);
                }
            }

            updateDateTimeButtons();

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
     * Update the buttons with the most recent values
     */
    protected void updateDateTimeButtons() {
        // Set the check-in date.
        TextView txtView = (TextView) checkInDateView.findViewById(R.id.field_value);
        if (txtView != null) {
            txtView.setText(Format
                    .safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkInDate));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDateTimeButtons: unable to locate check-in field value!");
        }
        // Set the check-out date.
        txtView = (TextView) checkOutDateView.findViewById(R.id.field_value);
        if (txtView != null) {
            txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY,
                    checkOutDate));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDateTimeButtons: unable to locate check-out field value!");
        }
    }

    /**
     * Handle the initial setup of the date and time buttons
     */
    protected void configureDateButtons() {

        // Check-in controls.
        checkInDateView = findViewById(R.id.hotel_search_check_in);
        if (checkInDateView != null) {
            // Set the field name.
            TextView txtView = (TextView) checkInDateView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_label_checkin);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate check_in field_name!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate hotel_search_check_in!");
        }
        // Check-out controls.
        checkOutDateView = findViewById(R.id.hotel_search_check_out);
        if (checkOutDateView != null) {
            // Set the field name.
            TextView txtView = (TextView) checkOutDateView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_label_checkout);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate check_out field_name!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDateButtons: unable to locate hotel_search_check_out!");
        }

        // Initialize their display
        updateDateTimeButtons();

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

    // ///////////////////////////////////////////////////////////////////////////
    // Check in/out field methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Distance value spinner methods - start
    // ///////////////////////////////////////////////////////////////////////////

    protected void configureDistanceValue() {

        distance = findViewById(R.id.hotel_search_distance);
        if (distance != null) {
            // Set the field name.
            TextView txtView = (TextView) distance.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_distance_label);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceValue: unable to locate field name text view!");
            }
            if (currentDistanceAmount == null) {
                currentDistanceAmount = distanceItems[2];
            }
            updateDistance();
            distance.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(DISTANCE_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceValue: unable to locate distance search distance!");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Distance value spinner methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Distance unit spinner methods - start
    // ///////////////////////////////////////////////////////////////////////////

    protected void configureDistanceUnit() {

        distanceUnit = findViewById(R.id.hotel_search_distance_units);
        if (distanceUnit != null) {
            // Set the field name.
            TextView txtView = (TextView) distanceUnit.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText("");
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceUnit: unable to locate field name text view!");
            }
            if (currentDistanceUnit == null) {
                currentDistanceUnit = distanceUnits[0];
            }
            updateDistanceUnit();
            distanceUnit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(DISTANCE_UNIT_DIALOG);
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureDistanceValue: unable to locate distance search distance unit!");
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Distance unit spinner methods - end
    // ///////////////////////////////////////////////////////////////////////////

    protected void doSearch() {
        // Get the values
        String latitude = currentLocation.latitude;
        String longitude = currentLocation.longitude;

        String distanceUnit = currentDistanceUnit.id;
        String distanceValue = currentDistanceAmount.id;
        String namesContaining = null;

        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        core.setViewedPriceToBeatList(false);

        if (withNamesContaining != null) {
            namesContaining = withNamesContaining.getText().toString().trim().toLowerCase();
        }

        Intent intent = new Intent(this, HotelSearchProgress.class);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, currentLocation.getName());
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_AMOUNT, currentDistanceAmount.name);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID, distanceValue);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_NAME, currentDistanceUnit.name);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnit);

        intent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, latitude);
        intent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, longitude);

        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING, namesContaining);

        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkInDate));
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDate);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT,
                Format.safeFormatCalendar(FormatUtil.SHORT_WEEKDAY_MONTH_DAY_FULL_YEAR_DISPLAY, checkOutDate));
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDate);

        intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);
        Intent launchIntent = getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        startActivityForResult(intent, Const.REQUEST_CODE_BOOK_HOTEL);
    }

    /**
     * Will check fields and enable/disable the search button.
     */
    protected void checkSearchButton() {
        if (currentLocation != null) {
            searchButton.setEnabled(true);
        } else {
            searchButton.setEnabled(false);
        }
    }

    /**
     * configure the book near me UI
     */
    protected void configureBookNearMe() {
        // disabling the feature for 9.3 as a change in spec

        // findViewById(R.id.book_near_me).setVisibility(View.VISIBLE);
        // findViewById(R.id.book_near_me).setOnClickListener(new
        // View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // finish();
        // Intent i = getIntent();
        // i.putExtra(BOOK_NEAR_ME, true);
        // startActivity(i);
        // }
        // });
    }

}
