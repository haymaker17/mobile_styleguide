package com.concur.mobile.core.travel.rail.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.activity.TripList;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.TravelCustomField;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.travel.rail.data.RailChoice;
import com.concur.mobile.core.travel.rail.data.RailChoiceLeg;
import com.concur.mobile.core.travel.rail.data.RailChoiceSegment;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.rail.data.RailTicketDeliveryOption;
import com.concur.mobile.core.travel.rail.service.RailSellRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.util.Format;

public class RailSearchDetail extends TravelBaseActivity {

    public static final String CLS_TAG = RailSearchDetail.class.getSimpleName();
    // Keys used to save/retrieve state during screen orientation change.
    private static final String RESERVE_RAIL_RECEIVER_KEY = "reserve.rail.receiver";

    private static final String DELIVERY_OPTION_KEY = "DELIVERY_OPTION_KEY";
    protected static final int DIALOG_DELIVERY_OPTION = DIALOG_ID_BASE + 1;

    protected String groupId;
    protected String bucket;
    protected RailStation depLocation;
    protected RailStation arrLocation;
    protected Calendar depDateTime;
    protected Calendar retDateTime;
    protected RailChoice railChoice;
    protected SpinnerItem[] spinnerDeliveryOptions;
    protected SpinnerItem currentDeliveryOption;
    protected TextView deliveryOptionField;
    /**
     * Contains a reference to the receiver to handle the result of booking rail.
     */
    protected ReserveRailReceiver reserveRailReceiver;
    /**
     * Contains the intent filter used to register the reserve rail receiver.
     */
    protected IntentFilter reserveRailFilter;
    /**
     * Contains a reference to an outstanding request to reserve rail.
     */
    protected RailSellRequest reserveRailRequest;

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        // Restore any receivers.
        restoreReceivers();

        initValues(inState);
        initUI();

        // Fetch booking information fields if created on a non-orientation change
        if (!orientationChange) {
            if (ConcurCore.isConnected()) {
                // Check whether a travel custom fields view fragment already exists.
                // This can
                // can be the case if a device gets rotated while this activity is on
                // the stack
                // and not directly visible. Example, is on HotelReserveRoom screen, go
                // to hotel room details
                // and rotate the device, then press HW back button.
                if (!hasTravelCustomFieldsView()) {
                    if (shouldRequestTravelCustomFields()) {
                        sendTravelCustomFieldsRequest();
                    } else {
                        initTravelCustomFieldsView();
                    }
                }

            }
        }
    }

    protected void initValues(Bundle inState) {
        Intent intent = getIntent();
        groupId = intent.getStringExtra(RailSearchResultsFares.KEY_GROUP_ID);
        bucket = intent.getStringExtra(RailSearchResultsFares.KEY_BUCKET);
        // Grab our stations from the app cache
        HashMap<String, RailStation> stationMap = ((ConcurCore) getApplication()).getCodeRailStationMap();
        String stationCode = intent.getStringExtra(RailSearch.DEP_LOCATION);
        depLocation = stationMap.get(stationCode);
        stationCode = intent.getStringExtra(RailSearch.ARR_LOCATION);
        arrLocation = stationMap.get(stationCode);
        depDateTime = (Calendar) intent.getSerializableExtra(RailSearch.DEP_DATETIME);
        if (intent.hasExtra(RailSearch.RET_DATETIME)) {
            retDateTime = (Calendar) intent.getSerializableExtra(RailSearch.RET_DATETIME);
        }
        ConcurCore app = (ConcurCore) getApplication();
        ArrayList<RailChoice> choices = app.getRailSearchResults().choiceMap.get(groupId);
        final int size = choices.size();
        for (int i = 0; i < size; i++) {
            RailChoice c = choices.get(i);
            if (c.bucket.equals(bucket)) {
                railChoice = c;
                break;
            }
        }
        if (inState != null) {
            // Restore selected Delivery Option.
            currentDeliveryOption = (SpinnerItem) inState.getSerializable(DELIVERY_OPTION_KEY);
        }

        // Modified code for MOB-14317
        if (!orientationChange) {
            showDialog(RETRIEVE_PRE_SELL_OPTIONS_DIALOG);
            getPreSellOptions(railChoice.choiceId);
        }

        super.initValues(inState);
    }

    protected void initUI() {
        setContentView(R.layout.rail_search_detail);
        // Pull the intent values for the header and populate the UI
        Intent intent = getIntent();
        // Set the screen title
        getSupportActionBar().setTitle(R.string.rail_detail_title);

        // Set the depart/location and date(s).
        populateCriteria();
        if (railChoice != null) {
            populateLegs();
            populateCost();

            initRailPreSellOptions();

        }
        super.initUI();
    }

    @Override
    protected boolean getDisplayAtStart() {
        return false;
    }

    @Override
    protected void cancelBookingRequest() {
        if (reserveRailRequest != null) {
            reserveRailRequest.cancel();
        }
    }

    @Override
    protected CharSequence getBookingConfirmDialogMessage() {
        return getText(R.string.dlg_travel_rail_confirm_message);
    }

    @Override
    protected CharSequence getBookingConfirmDialogTitle() {
        return getText(R.string.confirm);
    }

    @Override
    protected CharSequence getBookingFailedDialogTitle() {
        return getText(R.string.rail_detail_book_failure_title);
    }

    @Override
    protected CharSequence getBookingProgressDialogMessage() {
        return getText(R.string.booking_train);
    }

    @Override
    protected CharSequence getBookingSucceededDialogMessage() {
        return getText(R.string.rail_detail_book_success_message);
    }

    @Override
    protected CharSequence getBookingSucceededDialogTitle() {
        return getText(R.string.rail_detail_book_success_title);
    }

    @Override
    protected CharSequence getBookingType() {
        return getText(R.string.general_rail);
    }

    @Override
    protected List<Violation> getViolations() {
        List<Violation> violations = null;
        if (railChoice != null) {
            violations = railChoice.getViolations();
        }
        return violations;
    }

    @Override
    protected void onBookingSucceeded() {
        if (!launchedWithCliqbookTripId) {
            // Set the flag that the trip list should be refetched.
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
            }
            // Retrieve an updated trip summary list, then retrieve the detailed itinerary.
            sendItinerarySummaryListRequest();
        } else {
            // Just finish the activity.
            finish();
        }
    }

    /**
     * get trip list intent.
     * */
    protected Intent getTripListIntent() {
        return new Intent(RailSearchDetail.this, TripList.class);
    }

    @Override
    protected void sendBookingRequest() {
        sendReserveRailRequest();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
        case DIALOG_DELIVERY_OPTION: {
            if (spinnerDeliveryOptions != null) {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.rail_detail_select_ticket_delivery);
                dlgBldr.setCancelable(true);
                ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                        android.R.layout.simple_spinner_item, spinnerDeliveryOptions) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return super.getDropDownView(position, convertView, parent);
                    }
                };
                listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Get the currently selected item.
                int selectedItem = -1;
                if (currentDeliveryOption != null) {
                    for (int i = 0; i < spinnerDeliveryOptions.length; i++) {
                        if (currentDeliveryOption.id.equals(spinnerDeliveryOptions[i].id)) {
                            selectedItem = i;
                            break;
                        }
                    }
                }
                dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        currentDeliveryOption = spinnerDeliveryOptions[which];
                        deliveryOptionField.setText(currentDeliveryOption.name);
                        removeDialog(DIALOG_DELIVERY_OPTION);
                    }
                });
                dlgBldr.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        removeDialog(DIALOG_DELIVERY_OPTION);
                    }
                });
                dlg = dlgBldr.create();
            }
            break;
        }
        default: {
            dlg = super.onCreateDialog(id);
            break;
        }
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os. Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save selected Delivery Option.
        outState.putSerializable(DELIVERY_OPTION_KEY, currentDeliveryOption);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            // Save ReserveRailReceiver
            if (reserveRailReceiver != null) {
                // Clear the activity reference, it will be set in the 'onCreate'
                // method.
                reserveRailReceiver.setActivity(null);
                // Add to the retainer
                retainer.put(RESERVE_RAIL_RECEIVER_KEY, reserveRailReceiver);
            }
        }
    }

    @Override
    protected void restoreReceivers() {
        super.restoreReceivers();
        // Restore any receivers.
        if (retainer != null) {
            // Restore 'ReserveRailReceiver'.
            if (retainer.contains(RESERVE_RAIL_RECEIVER_KEY)) {
                reserveRailReceiver = (ReserveRailReceiver) retainer.get(RESERVE_RAIL_RECEIVER_KEY);
                if (reserveRailReceiver != null) {
                    // Set the activity on the receiver.
                    reserveRailReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for reserve rail receiver!");
                }
            }
        }
    }

    protected void populateCriteria() {
        // Set departure/arrival header title.
        String loc = com.concur.mobile.base.util.Format.localizeText(this, R.string.rail_search_label_dep_to_arr_loc,
                new Object[] { depLocation.getName(), arrLocation.getName() });
        ((TextView) findViewById(R.id.travel_name)).setText(loc);
        // Set departure/arrival dates
        StringBuilder dates = new StringBuilder(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA,
                depDateTime));
        if (retDateTime != null) {
            dates.append(" - ");
            dates.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, retDateTime));
        }
        ((TextView) findViewById(R.id.date_span)).setText(dates.toString());
    }

    protected void populateLegs() {
        // Get the layout to hold all the legs.
        LayoutInflater inflater = LayoutInflater.from(this);
        // Get the outbound segment and populate it
        LinearLayout departureLegLayout = (LinearLayout) findViewById(R.id.railDetailDepartureLegsLayout);
        RailChoiceSegment outboundSeg = railChoice.getOutboundSegment();
        for (RailChoiceLeg leg : outboundSeg.legs) {
            // Inflate the outbound leg.
            View outboundLegView = inflater.inflate(R.layout.rail_search_detail_header, null);
            departureLegLayout.addView(outboundLegView);
            if (leg.isBus()) {
                // Change the label
                ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderEquipmentLabel))
                        .setText(R.string.rail_general_bus);
            }
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderTrain)).setText(leg.trainNum);
            // Set Departure date.
            String depDate = Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_DISPLAY, leg.depDateTime);
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderDate)).setText(depDate);
            // Force our timezone for formatting to UTC since that's what we use
            // everywhere internally.
            java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderDepStation)).setText(leg.depStationCode);
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderDepTime)).setText(Format.safeFormatCalendar(
                    timeFormat, leg.depDateTime));
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderArrStation)).setText(leg.arrStationCode);
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderArrTime)).setText(Format.safeFormatCalendar(
                    timeFormat, leg.arrDateTime));
            StringBuilder elapsed = new StringBuilder("(");
            elapsed.append(leg.getElapsedTime(this)).append(')');
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderTotalTime)).setText(elapsed.toString());
            // Set Departure seat class.
            ((TextView) outboundLegView.findViewById(R.id.railDetailHeaderClass)).setText(leg.seatClassName);
        }
        // Get the return segment and populate it
        RailChoiceSegment ret = railChoice.getReturnSegment();
        if (ret != null) {
            LinearLayout returnLegLayout = (LinearLayout) findViewById(R.id.railDetailReturnLegsLayout);
            // Get the outbound segment and populate it
            for (RailChoiceLeg leg : ret.legs) {
                // Inflate the return leg.
                View returnLegView = inflater.inflate(R.layout.rail_search_detail_header, null);
                returnLegLayout.addView(returnLegView);
                if (leg.isBus()) {
                    // Change the label
                    ((TextView) returnLegView.findViewById(R.id.railDetailHeaderEquipmentLabel))
                            .setText(R.string.rail_general_bus);
                }
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderTrain)).setText(leg.trainNum);
                // Set Return date.
                String depDate = Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_DISPLAY, leg.depDateTime);
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderDate)).setText(depDate);
                // Force our timezone for formatting to UTC since that's what we use
                // everywhere internally.
                java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
                timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderDepStation)).setText(leg.depStationCode);
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderDepTime)).setText(Format
                        .safeFormatCalendar(timeFormat, leg.depDateTime));
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderArrStation)).setText(leg.arrStationCode);
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderArrTime)).setText(Format
                        .safeFormatCalendar(timeFormat, leg.arrDateTime));
                StringBuilder elapsed = new StringBuilder("(");
                elapsed.append(leg.getElapsedTime(this)).append(')');
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderTotalTime)).setText(elapsed.toString());
                // Set Departure seat class.
                ((TextView) returnLegView.findViewById(R.id.railDetailHeaderClass)).setText(leg.seatClassName);
            }
        } else {
            findViewById(R.id.railDetailReturnMainLayout).setVisibility(View.GONE);
            findViewById(R.id.railDetailReturnMainLayoutShadow).setVisibility(View.GONE);
        }
    }

    protected void populateCost() {
        // Get the view and hide the cheveron image.
        View total = findViewById(R.id.railDetailTotal);
        total.findViewById(R.id.field_image).setVisibility(View.GONE);
        // Set the total label and value.
        Locale loc = RailSearchDetail.this.getResources().getConfiguration().locale;
        double cost = railChoice.cost == null ? 0.0 : railChoice.cost;
        TextView txtView = (TextView) total.findViewById(R.id.field_value);
        (txtView).setText(FormatUtil.formatAmount(cost, loc, railChoice.currency, true));
        int textAppearanceResourceId = ViewUtil.getFormFieldValueStyle(railChoice.violations);
        txtView.setTextAppearance(this, textAppearanceResourceId);
        ((TextView) total.findViewById(R.id.field_name)).setText(R.string.rail_detail_total_label);
    }

    /**
     * Load the delivery options from the intent
     */
    protected void loadDeliveryOptions() {
        // Initialize the Delivery Option field.
        ((TextView) findViewById(R.id.railDetailDeliveryOption).findViewById(R.id.field_name))
                .setText(R.string.rail_detail_ticket_delivery);
        deliveryOptionField = (TextView) findViewById(R.id.railDetailDeliveryOption).findViewById(R.id.field_value);
        findViewById(R.id.railDetailDeliveryOption).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(DIALOG_DELIVERY_OPTION);
            }
        });
        if (currentDeliveryOption != null) {
            deliveryOptionField.setText(currentDeliveryOption.name);
        }
    }

    /**
     * Will send a request off to reserve the car.
     */
    protected void sendReserveRailRequest() {
        ConcurService concurService = getConcurService();
        if (concurService != null) {
            registerReserveRailReceiver();
            String creditCardId = null;
            if (curCardChoice != null) {
                creditCardId = curCardChoice.id;
            }
            String reasonCodeId = "";
            if (reasonCode != null) {
                reasonCodeId = reasonCode.id;
            }
            String violationText = (justificationText != null) ? justificationText : "";
            String tdoCode = null;
            if (currentDeliveryOption != null) {
                tdoCode = currentDeliveryOption.id;
            }
            List<TravelCustomField> tcfs = getTravelCustomFields();
            reserveRailRequest = concurService.reserveTrain(groupId, bucket, creditCardId, tdoCode, reasonCodeId,
                    violationText, tcfs);
            if (reserveRailRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendRailRequest: unable to create 'RailSellRequest' request!");
                unregisterReserveRailReceiver();
            } else {
                // Set the request object on the receiver.
                reserveRailReceiver.setServiceRequest(reserveRailRequest);
                // Show the delete receipt progress dialog.
                showDialog(BOOKING_PROGRESS_DIALOG);
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".sendRailRequest: service is unavailable.");
        }
    }

    /**
     * Will register an instance of <code>ReserveRailReceiver</code> with the application context and set the
     * <code>reserveRailReceiver</code> attribute.
     */
    public void registerReserveRailReceiver() {
        if (reserveRailReceiver == null) {
            reserveRailReceiver = new ReserveRailReceiver(this);
            if (reserveRailFilter == null) {
                reserveRailFilter = new IntentFilter(Const.ACTION_RAIL_SELL_RESULTS);
            }
            getApplicationContext().registerReceiver(reserveRailReceiver, reserveRailFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerReserveRailReceiver: reserveRailFilter is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ReserveRailReceiver</code> with the application context and set the
     * <code>reserveRailReceiver</code> to <code>null</code>.
     */
    public void unregisterReserveRailReceiver() {
        if (reserveRailReceiver != null) {
            getApplicationContext().unregisterReceiver(reserveRailReceiver);
            reserveRailReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReserveRailReceiver: reserveRailReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result reserving a trail.
     */
    public class ReserveRailReceiver extends BaseBroadcastReceiver<RailSearchDetail, RailSellRequest> {

        /**
         * Constructs an instance of <code>ReserveRailReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        public ReserveRailReceiver(RailSearchDetail activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(RailSearchDetail activity) {
            activity.reserveRailRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(BOOKING_PROGRESS_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(BOOKING_FAILED_DIALOG);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            onHandleSuccessReservation(activity, intent);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(RailSellRequest request) {
            activity.reserveRailRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterReserveRailReceiver();
        }
    }

    /**
     * Handle Successful reservation event.
     * 
     * @param intent
     * @param activity
     * */
    protected void onHandleSuccessReservation(RailSearchDetail activity, Intent intent) {
        locateTripId(activity, intent);
        flurryEvents(activity);
        activity.showDialog(BOOKING_SUCCEEDED_DIALOG);
    }

    protected void locateTripId(RailSearchDetail activity, Intent intent) {
        Intent result = new Intent();
        activity.itinLocator = intent.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR);
        result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, activity.itinLocator);
        if (activity.cliqbookTripId != null) {
            IItineraryCache itinCache = activity.getConcurCore().getItinCache();
            Trip trip = itinCache.getItinerarySummaryByCliqbookTripId(activity.cliqbookTripId);
            if (trip != null) {
                result.putExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR, trip.itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to locate trip based on cliqbook trip id!");
            }
        }
        activity.setResult(Activity.RESULT_OK, result);
    }

    protected void flurryEvents(RailSearchDetail activity) {
        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAIN);
        Intent launchIntent = activity.getIntent();
        if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            params.put(Flurry.PARAM_NAME_BOOKED_FROM, launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_RESERVE, params);
    }

    @Override
    protected void updatePreSellOptions() {
        initRailPreSellOptions();
    }

    // Pre-sell options
    private void initRailPreSellOptions() {
        if (!orientationChange) {
            // update credit cards
            initCreditCards();
        }

        // update ticket delivery options
        initTicketDeliveryOptions();
    }

    private void initCreditCards() {
        initCardChoices();
        initCardChoiceView();
    }

    private void initTicketDeliveryOptions() {
        if (preSellOption != null) {
            int c = preSellOption.getTicketDeliveryOptions().size();
            spinnerDeliveryOptions = new SpinnerItem[c];
            for (int j = 0; j < c; j++) {
                RailTicketDeliveryOption tdo = new RailTicketDeliveryOption(preSellOption.getTicketDeliveryOptions()
                        .get(j).getBundle());
                StringBuilder display = new StringBuilder(tdo.name);
                if (tdo.fee > 0.0) {
                    display.append(" (")
                            .append(FormatUtil.formatAmount(tdo.fee, getResources().getConfiguration().locale, "USD",
                                    true, true)).append(")");
                }
                spinnerDeliveryOptions[j] = new SpinnerItem(tdo.type, display.toString());
            }
            if (currentDeliveryOption == null && spinnerDeliveryOptions.length > 0) {
                // Initially set the value to the fist delivery option.
                currentDeliveryOption = spinnerDeliveryOptions[0];
            }

            loadDeliveryOptions();
        }
    }

}
