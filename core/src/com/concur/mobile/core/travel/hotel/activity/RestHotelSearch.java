/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.travel.activity.LocationSearchV1;
import com.concur.mobile.core.travel.data.CompanyLocation;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.BookingDateUtil;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.ui.common.view.SearchListFormFieldView;
import com.concur.mobile.platform.ui.common.widget.CalendarPicker;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialogV1;
import com.concur.mobile.platform.ui.travel.hotel.activity.HotelSearchAndResultActivity;
import com.concur.mobile.platform.ui.travel.hotel.activity.HotelVoiceSearchActivity;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ConcurActivity</code> for the purposes of providing Rest API hotel search services.
 * 
 * @author Tejoa
 */
public class RestHotelSearch extends Activity {

    private static final String CLS_TAG = RestHotelSearch.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = HotelSearch.class.getSimpleName()
            + ".calendar.dialog.fragment";

    private CalendarPickerDialogV1 calendarDialog;

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

    protected static final int CHECK_IN_DATE_DIALOG = 0;
    protected static final int CHECK_OUT_DATE_DIALOG = 1;
    // protected static final int DISTANCE_DIALOG = DIALOG_ID_BASE + 2;
    // protected static final int DISTANCE_UNIT_DIALOG = DIALOG_ID_BASE + 3;

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

    protected EditText withNamesContaining;

    protected final IntentFilter hotelResultsFilter = new IntentFilter(Const.ACTION_HOTEL_SEARCH_RESULTS);

    protected String actionStatusErrorMessage;

    protected String lastHttpErrorMessage;
    protected boolean fromLocationSearchIntent;
    protected boolean searchNearMe;

    private int DialogId = -1;
    private boolean isCheckin = false;
    private boolean checkForSearchCriteraChanged;
    private boolean searchCriteriaChanged;

    // /////////////////////////////////////////////////////////////////

    protected void initializeFieldState(Bundle savedInstanceState) {

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
                getCurrentLocation();
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

        } else {
            // Restore location.
            if (savedInstanceState.getBundle(LOCATION) != null) {
                int searchMode = savedInstanceState.getInt(LOCATION_TYPE);
                Bundle locBundle = savedInstanceState.getBundle(LOCATION);
                switch (searchMode) {
                case LocationSearchV1.SEARCH_COMPANY_LOCATIONS: {
                    currentLocation = new CompanyLocation(locBundle);
                    break;
                }
                case LocationSearchV1.SEARCH_CUSTOM: {
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
            // currentDistanceUnit = SpinnerItem.findById(distanceUnits, savedInstanceState.getString(DISTANCE_UNIT));
            // Restore distance value.
            // currentDistanceAmount = SpinnerItem.findById(distanceItems, savedInstanceState.getString(DISTANCE_VALUE));

            // Restore with names containing field.
            if (withNamesContaining != null) {
                String savedStr = savedInstanceState.getString(NAMES_CONTAINING);
                if (savedStr != null) {
                    withNamesContaining.setText(savedStr);
                }
            }

            // book near me
            searchNearMe = savedInstanceState.getBoolean(BOOK_NEAR_ME);

            if (checkForSearchCriteraChanged) {
                searchCriteriaChanged = savedInstanceState.getBoolean("searchCriteriaChanged");
            }
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
                outState.putInt(LOCATION_TYPE, LocationSearchV1.SEARCH_COMPANY_LOCATIONS);
            } else {
                outState.putInt(LOCATION_TYPE, LocationSearchV1.SEARCH_CUSTOM);
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
        // if (currentDistanceUnit != null) {
        // outState.putString(DISTANCE_UNIT, currentDistanceUnit.id);
        // } else {
        // outState.putString(DISTANCE_UNIT, null);
        // }

        // Save distance value.
        // if (currentDistanceAmount != null) {
        // outState.putString(DISTANCE_VALUE, currentDistanceAmount.id);
        // } else {
        // outState.putString(DISTANCE_VALUE, null);
        // }

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
        if (checkForSearchCriteraChanged) {
            outState.putBoolean("searchCriteriaChanged", searchCriteriaChanged);
        }
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
        // if (Preferences.shouldAllowVoiceBooking()) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_voice_v1, menu);
        // }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuVoice) {
            if (!ConcurCore.isConnected()) {
                // showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                ConcurCore core = (ConcurCore) ConcurCore.getContext();
                Intent i = new Intent(RestHotelSearch.this, HotelVoiceSearchActivity.class);
                i.putExtra("currentLocation", core.getCurrentLocation());
                i.putExtra("currentAddress", core.getCurrentAddress());
                UserConfig userConfig = core.getUserConfig();
                String distanceUnit = userConfig != null ? userConfig.distanceUnitPreference : null;
                if (distanceUnit == null) {
                    distanceUnit = "M";
                } else {
                    distanceUnit = (distanceUnit.equalsIgnoreCase("Miles") ? "M" : "K");
                }
                i.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnit);

                RestHotelSearch.this.startActivity(i);
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

        setContentView(R.layout.rest_hotel_search);
        setActionBar();
        initializeFieldState(savedInstanceState);

        searchButton = (Button) findViewById(R.id.full_button);
        if (searchButton != null) {
            searchButton.setText(getText(R.string.general_search));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'footer_button_one' button!");
        }

        configureLocationButton();
        configureDateButtons();

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Check for connectivity, if none, then display dialog and
                // return.
                if (!ConcurCore.isConnected()) {
                    // showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    return;
                }
                // ValidateTravelCustomFields will display a dialog and return
                // 'false' if any displayed
                // fields have missing or invalid values.

                doSearch();

            }
        });

        checkSearchButton();

        // restoreReceivers();

        // Fetch booking information fields if created on a non-orientation
        // change
        // and no passed in trip
        // if (!orientationChange) {
        // if (cliqbookTripId == null) {
        // // Init or request the travel custom fields.
        // if (!hasTravelCustomFieldsView()) {
        // if (ConcurCore.isConnected()) {
        // sendTravelCustomFieldsRequest();
        // } else {
        // // TODO: Let end-user that connectivity is required for
        // // search.
        // }
        // }
        // }
        // }

        calendarDialog = (CalendarPickerDialogV1) getFragmentManager().findFragmentByTag(TAG_CALENDAR_DIALOG_FRAGMENT);
        if (calendarDialog != null) {
            if (savedInstanceState.containsKey(KEY_DIALOG_ID) && savedInstanceState.containsKey(KEY_IS_CHECKIN)) {
                calendarDialog.setOnDateSetListener(new HotelDateSetListener(savedInstanceState.getInt(KEY_DIALOG_ID),
                        savedInstanceState.getBoolean(KEY_IS_CHECKIN)));
            }
        }
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();

        // Set the header.
        // actionBar.setTitle(Html.fromHtml("<font color='#0078c8'>Search </font>"));
        actionBar.setTitle(R.string.general_search);

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    // @Override
    // protected void onPrepareDialog(int id, Dialog dialog) {
    // super.onPrepareDialog(id, dialog);
    // switch (id) {
    // case Const.DIALOG_TRAVEL_SEARCH_HOTEL_FAILED: {
    // AlertDialog alertDlg = (AlertDialog) dialog;
    // alertDlg.setMessage(actionStatusErrorMessage);
    // break;
    // }
    // }
    // }

    // @Override
    // protected boolean getDisplayAtStart() {
    // return true;
    // }

    // ///////////////////////////////////////////////////////////////////////////
    // Location methods - start
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Update the button with the current location selection
     */
    protected void updateLocationButton() {
        if (currentLocation == null) {
            SpannableString s = new SpannableString(getText(R.string.search_current_location));
            s.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView txtView = (TextView) location.findViewById(R.id.field_value);
            if (txtView != null) {
                if (cliqbookTripId == null) {
                    // a new itinerary, hence default to current location
                    configureBookNearMe();
                    searchNearMe = getIntent().getBooleanExtra(BOOK_NEAR_ME, false);
                    if (searchNearMe) {

                        getCurrentLocation();
                        if (currentLocation == null) {
                            txtView.setText(s);
                            // Display a toast message indicating current
                            // location cannot be determined.
                            String toastText = getText(R.string.dlg_no_current_location).toString();
                            Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
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

    private void getCurrentLocation() {
        Address currAdd = ((ConcurCore) ConcurCore.getContext()).getCurrentAddress();
        if (currAdd == null) {
            // txtView.setText(s);
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
        }
        // txtView.setText(R.string.general_current_location);

    }

    private void showCalendarDialog(int id) {
        Bundle bundle;

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

        DialogId = id;
        calendarDialog.setArguments(bundle);
        calendarDialog.setOnDateSetListener(new HotelDateSetListener(id, isCheckin));
        calendarDialog.show(getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);
    }

    /**
     * Handle the initial setup of the location button
     */
    protected void configureLocationButton() {

        // Grab our control
        location = findViewById(R.id.hotel_location);
        if (location != null) {
            TextView txtView = (TextView) location.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.search_destination_location);
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
                    // showDialog(Const.DIALOG_NO_CONNECTIVITY);
                } else {
                    // Determine if there are any company locations, if so, then
                    // pass that flag into
                    // the location search activity.
                    ConcurCore ConcurCore = (ConcurCore) getApplication();
                    com.concur.mobile.core.data.SystemConfig sysConfig = ConcurCore.getSystemConfig();
                    int locationSearchMode = LocationSearchV1.SEARCH_CUSTOM;
                    if (sysConfig != null && sysConfig.getCompanyLocations() != null
                            && sysConfig.getCompanyLocations().size() > 0) {
                        locationSearchMode |= LocationSearchV1.SEARCH_COMPANY_LOCATIONS;
                    }
                    if (checkForSearchCriteraChanged) {
                        searchCriteriaChanged = true;
                    }
                    Intent intent = new Intent(RestHotelSearch.this, LocationSearchV1.class);
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
                    case LocationSearchV1.SEARCH_COMPANY_LOCATIONS: {
                        currentLocation = new CompanyLocation(locBundle);
                        break;
                    }
                    case LocationSearchV1.SEARCH_CUSTOM: {
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
            } else if (resultCode == RESULT_CANCELED) {
                // Hotel Search cancelled or did not go further with the booking
                checkForSearchCriteraChanged = true;
                searchCriteriaChanged = false;
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
    class HotelDateSetListener implements CalendarPickerDialogV1.OnDateSetListener {

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
                if (checkForSearchCriteraChanged) {
                    searchCriteriaChanged = true;
                }

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
                if (checkForSearchCriteraChanged) {
                    searchCriteriaChanged = true;
                }

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
        checkInDateView = findViewById(R.id.hotel_search_label_checkin);
        if (checkInDateView != null) {
            // Set the field name.
            TextView txtView = (TextView) checkInDateView.findViewById(R.id.field_name);
            if (txtView != null) {
                txtView.setText(R.string.hotel_search_label_checkin_date);
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
                txtView.setText(R.string.hotel_search_label_checkout_date);
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

    protected void doSearch() {
        // Get the values
        String latitude = currentLocation.latitude;
        String longitude = currentLocation.longitude;

        // String distanceUnit = currentDistanceUnit.id;
        // String distanceValue = currentDistanceAmount.id;
        String namesContaining = null;

        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        core.setViewedPriceToBeatList(false);

        if (withNamesContaining != null) {
            namesContaining = withNamesContaining.getText().toString().trim().toLowerCase();
        }

        Intent intent = new Intent(this, HotelSearchAndResultActivity.class);

        if (currentLocation instanceof CompanyLocation) {
            String formattedName = null;
            if (currentLocation.state == null || currentLocation.state.length() == 0) {
                formattedName = Format.localizeText(ConcurCore.getContext(), R.string.general_citycountry,
                        new Object[] { currentLocation.city, ((CompanyLocation) currentLocation).country });
            } else {
                formattedName = Format.localizeText(ConcurCore.getContext(), R.string.general_citystatecountry,
                        new Object[] { currentLocation.city, currentLocation.state,
                                ((CompanyLocation) currentLocation).country });
            }
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, formattedName);
        } else {
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, currentLocation.getName());
        }

        intent.putExtra(Const.EXTRA_LOCATION_SEARCH_MODE_USED, fromLocationSearchIntent);

        // intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_AMOUNT, currentDistanceAmount.name);
        // intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID, distanceValue);
        // intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_NAME, currentDistanceUnit.name);
        UserConfig userConfig = core.getUserConfig();
        String distanceUnit = userConfig != null ? userConfig.distanceUnitPreference : null;
        if (distanceUnit == null) {
            distanceUnit = "M";
        } else {
            distanceUnit = (distanceUnit.equalsIgnoreCase("Miles") ? "M" : "K");
        }
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnit);

        intent.putExtra(Const.EXTRA_TRAVEL_LATITUDE, latitude);
        intent.putExtra(Const.EXTRA_TRAVEL_LONGITUDE, longitude);

        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING, namesContaining);

        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN,
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, checkInDate));
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR, checkInDate);
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT,
                Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY, checkOutDate));
        intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR, checkOutDate);

        intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, cliqbookTripId);

        // get the hotel violation reasons from the SystemConfig and pass it on to the activities.
        // since the platform systemconfig request is not being invoked by the application, we cannot use the getHotelReasons from
        // the platform. Hence passing in to the next activities.
        SystemConfig sysConfig = ((ConcurCore) getApplication()).getSystemConfig();
        ArrayList<com.concur.mobile.core.travel.data.ReasonCode> reasonCodesCore = sysConfig.getHotelReasons();
        if (reasonCodesCore != null) {
            ArrayList<String[]> violationReasons = new ArrayList<String[]>(reasonCodesCore.size());
            for (com.concur.mobile.core.travel.data.ReasonCode reasonCode : reasonCodesCore) {
                violationReasons.add(new String[] { reasonCode.id, reasonCode.description });
            }
            intent.putExtra("violationReasons", violationReasons);
        }
        if (checkForSearchCriteraChanged) {
            intent.putExtra("searchCriteriaChanged", searchCriteriaChanged);
        }

        // Intent launchIntent = getIntent();
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
