package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.air.activity.AirSegmentListItem;
import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.travel.air.service.AirCancelRequest;
import com.concur.mobile.core.travel.approval.activity.RuleViolationSummary;
import com.concur.mobile.core.travel.car.activity.CarSearch;
import com.concur.mobile.core.travel.car.activity.CarSegmentListItem;
import com.concur.mobile.core.travel.car.data.CarSegment;
import com.concur.mobile.core.travel.car.service.CancelCarRequest;
import com.concur.mobile.core.travel.data.CategorySearchSuggestion;
import com.concur.mobile.core.travel.data.CitySearchSuggestion;
import com.concur.mobile.core.travel.data.CustomSearchSuggestion;
import com.concur.mobile.core.travel.data.DiningSegment;
import com.concur.mobile.core.travel.data.EventSegment;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.ITripAnalyzer;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.LodgeSearchSuggestion;
import com.concur.mobile.core.travel.data.Offer;
import com.concur.mobile.core.travel.data.OfferSegmentLink;
import com.concur.mobile.core.travel.data.ParkingSegment;
import com.concur.mobile.core.travel.data.RideSegment;
import com.concur.mobile.core.travel.data.SearchSuggestion;
import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.core.travel.data.Segment.SegmentType;
import com.concur.mobile.core.travel.data.TransportSearchSuggestion;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.data.Trip.ApprovalStatusEnum;
import com.concur.mobile.core.travel.hotel.activity.HotelCancelSegmentListerner;
import com.concur.mobile.core.travel.hotel.activity.HotelSearch;
import com.concur.mobile.core.travel.hotel.activity.HotelSegmentListItem;
import com.concur.mobile.core.travel.hotel.data.HotelSegment;
import com.concur.mobile.core.travel.rail.activity.RailSegmentListItem;
import com.concur.mobile.core.travel.rail.data.RailSegment;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.rail.service.CancelRailRequest;
import com.concur.mobile.core.travel.rail.service.RailStationListRequest;
import com.concur.mobile.core.travel.service.CancelSegment;
import com.concur.mobile.core.travel.service.GetAgencyDetails;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.ItinerarySummaryListRequest;
import com.concur.mobile.core.travel.service.TripApproval;
import com.concur.mobile.core.travel.service.TripApprovalReqObject;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.FeedbackManager;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.util.Format;


public class SegmentList extends BaseActivity {

    public static final String CLS_TAG = SegmentList.class.getSimpleName();

    protected String itinLocator;
    protected Trip trip;

    protected String NA_TEXT;
    protected static final int DIALOG_BASE_NUMBER = 1000;// to create unique ids
                                                         // for the dialogs

    protected static final int HEADER_VIEW_TYPE = 0;
    protected static final int SEGMENT_VIEW_TYPE = 1;
    protected static final int OFFER_VIEW_TYPE = 2;

    protected static final int DIALOG_ADD_HOTEL = DIALOG_BASE_NUMBER + 0;
    protected static final int DIALOG_ADD_CAR = DIALOG_BASE_NUMBER + 1;
    protected static final int DIALOG_CANCEL_HOTEL_CONFIRM = DIALOG_BASE_NUMBER + 2;

    protected static final int DIALOG_ITINERARY_REFRESH_FAILED = DIALOG_BASE_NUMBER + 6;
    protected static final int DIALOG_ITINERARY_GONE = DIALOG_BASE_NUMBER + 7;
    protected static final int DIALOG_RAIL_STATION_PROGRESS = DIALOG_BASE_NUMBER + 8;
    protected static final int DIALOG_CANCEL_CAR_CONFIRM = DIALOG_BASE_NUMBER + 9;
    protected static final int DIALOG_CANCEL_CAR_PROGRESS = DIALOG_BASE_NUMBER + 10;
    protected static final int DIALOG_CANCEL_CAR_SUCCESS = DIALOG_BASE_NUMBER + 11;
    protected static final int DIALOG_CANCEL_CAR_FAIL = DIALOG_BASE_NUMBER + 12;

    protected static final int DIALOG_CANCEL_AIR_CONFIRM = DIALOG_BASE_NUMBER + 13;
    protected static final int DIALOG_CANCEL_AIR_PROGRESS = DIALOG_BASE_NUMBER + 14;
    protected static final int DIALOG_CANCEL_AIR_SUCCESS = DIALOG_BASE_NUMBER + 15;
    protected static final int DIALOG_CANCEL_AIR_FAIL = DIALOG_BASE_NUMBER + 16;
    protected static final int DIALOG_ITINERARY_LIST_REFRESH = DIALOG_BASE_NUMBER + 17;
    protected static final int DIALOG_ITINERARY_LIST_REFRESH_FAILED = DIALOG_BASE_NUMBER + 18;

    protected static final int DIALOG_CANCEL_RAIL_CONFIRM = DIALOG_BASE_NUMBER + 19;
    protected static final int DIALOG_CANCEL_RAIL_PROGRESS = DIALOG_BASE_NUMBER + 20;
    protected static final int DIALOG_CANCEL_RAIL_SUCCESS = DIALOG_BASE_NUMBER + 21;
    protected static final int DIALOG_CANCEL_RAIL_FAIL = DIALOG_BASE_NUMBER + 22;
    protected static final int DIALOG_TRIP_REJECT_COMMENTS = DIALOG_BASE_NUMBER + 23;
    protected static final int DIALOG_TRIP_REJECT_COMMENT_PROMPT = DIALOG_BASE_NUMBER + 24;
    protected static final int DIALOG_PROMPT_TO_APPROVE_TRIP = DIALOG_BASE_NUMBER + 25;
    protected static final int DIALOG_TRIP_APPROVAL_PROGRESS = DIALOG_BASE_NUMBER + 26;
    protected static final int DIALOG_TRIP_APPROVAL_ACTION_FAILURE = DIALOG_BASE_NUMBER + 27;
    protected static final int DIALOG_TRIP_APPROVAL_ACTION_SUCCESS = DIALOG_BASE_NUMBER + 28;
    protected static final int DIALOG_PROMPT_TO_CALL_AGENT = DIALOG_BASE_NUMBER + 29;
    protected static final int DIALOG_AGENCY_ASSISTANCE_FAIL = DIALOG_BASE_NUMBER + 30;
    protected static final int DIALOG_GET_AGENCY_ASSISTANCE_PROGRESS = DIALOG_BASE_NUMBER + 31;
    protected static final int DIALOG_AGENT_CHANGED_ITINERARY = DIALOG_BASE_NUMBER + 32;

    private static final String EXTRA_HOTEL_CANCEL_RECEIVER_KEY = "hotel.cancel.receiver";
    private static final String EXTRA_HOTEL_CANCEL_SEGMENT_KEY = "hotel.cancel.segment";
    private static final String EXTRA_CAR_CANCEL_RECEIVER_KEY = "car.cancel.receiver";
    private static final String EXTRA_RAIL_CANCEL_RECEIVER_KEY = "car.cancel.receiver";
    private static final String EXTRA_AIR_CANCEL_RECEIVER_KEY = "air.cancel.receiver";
    private static final String EXTRA_RAIL_STATION_RECEIVER_KEY = "rail.station.receiver";
    private static final String EXTRA_LONG_PRESS_SEGMENT_KEY = "long.press.segment";
    private static final String EXTRA_CAR_SUGGESTION_ADAPTER = "car.suggestion.adapter";
    private static final String EXTRA_HOTEL_SUGGESTION_ADAPTER = "hotel.suggestion.adapter";
    private static final String EXTRA_BOOKING_RECORD_LOCATOR_KEY = "booking.record.locator";
    private static final String EXTRA_BOOKING_ITINERARY_LOCATOR_KEY = "booking.itinerary.locator";
    private static final String EXTRA_AGENT_PREFERRED_PHONE_NUMBER = "agent.preferred.phone.number";
    private static final String EXTRA_TRIP_RECORD_LOCATOR_FOR_AGENT = "agent.trip.record.locator";
    private static final String EXTRA_AGENT_ERROR_MESSAGE = "agent.error.message";
    private static final String EXTRA_AFTER_CALLING_AGENT = "after.calling.agent";
    private static final String EXTRA_TRIP_TOTAL_TRAVEL_POINTS = "trip.total.points.booked";

    private static final String ITINERARY_LIST_RECEIVER_KEY = "itinerary.list.receiver";
    private static final String ITINERARY_RECEIVER_KEY = "itinerary.receiver";

    // Contains the receiver used to handle the results of an itinerary summary
    // list request.
    private ItineraryListReceiver itinerarySummaryListReceiver;

    // Contains the filter used to register the itinerary summary list receiver.
    private IntentFilter itinerarySummaryListFilter;

    // Contains a reference to the currently outstanding itinerary summary list
    // request.
    private ItinerarySummaryListRequest itinerarySummaryListRequest;

    // Contains the receiver used to handle the results of an itinerary request.
    private ItineraryReceiver itineraryReceiver;

    // Contains the filter used to register the itinerary receiver.
    private IntentFilter itineraryFilter;

    // Contains a reference to the currently outstanding itinerary request.
    private ItineraryRequest itineraryRequest;

    // Contains the adapter for hotel suggestions.
    private SuggestionAdapter hotelSuggestionAdapter;

    // Contains the adapter for car rental suggestions.
    private SuggestionAdapter carSuggestionAdapter;

    // Reference to an outstanding request to cancel a car.
    private CancelCarRequest cancelCarRequest;

    // Reference to an outstanding request to cancel a rail reservation.
    private CancelRailRequest cancelRailRequest;

    // Reference to an outstanding request to cancel an air booking.
    private AirCancelRequest cancelAirRequest;

    // Reference to an outstanding request to retrieve a rail station list.
    private RailStationListRequest railStationListRequest;

    // Contains the hotel segment of the hotel room the end-user has chosen
    // to cancel.
    // TODO: Persist this via 'onSaveInstanceState' so that end-user changes
    // orientation
    // with dialog to confirm up, that we don't lose which hotel segment is
    // being cancelled.
    protected Segment longPressSegment;

    private CancelSegment cancelSegment;

    private BaseAsyncResultReceiver hotelCancelReceiver;

    protected final IntentFilter hotelCancelFilter = new IntentFilter(Const.ACTION_HOTEL_CANCEL_RESULT);

    private CarCancelReceiver carCancelReceiver;

    protected final IntentFilter carCancelFilter = new IntentFilter(Const.ACTION_CAR_CANCEL_RESULT);

    private RailCancelReceiver railCancelReceiver;

    protected final IntentFilter railCancelFilter = new IntentFilter(Const.ACTION_RAIL_CANCEL_RESULT);

    private AirCancelReceiver airCancelReceiver;

    private final IntentFilter airCancelFilter = new IntentFilter(Const.ACTION_AIR_CANCEL_RESULTS);

    private RailStationReceiver railStationReceiver;

    private final IntentFilter railStationFilter = new IntentFilter(Const.ACTION_RAIL_STATION_LIST_RESULTS);

    // Contains the last record locator returned as a result of performing
    // a booking originating from within this activity.
    private String bookingRecordLocator;

    // Contains the itinerary locator referencing an itinerary containing
    // 'bookingRecordLocator'.
    // This value may be 'null' if a booking was added to a new itinerary.
    private String bookingItineraryLocator;

    // Contains whether or not the activity should prompt to add a hotel/car.
    private boolean promptForAdd;

    // Contains the set of options for a context menu on segments.
    protected final static int MENU_ITEM_VIEW_DETAILS = 1;
    protected final static int MENU_ITEM_CANCEL = 2;

    // Contains one of the segment type constants from the
    // <code>Flurry.PARAM_VALUE_[<AIR>|<CAR>|<HOTEL>|<TRAIN>]</code>.
    private String lastCanceledFlurrySegmentParamValue;

    // flag to switch the screen contents for trip approver
    private boolean isForTripApproval;

    private TripApprovalReqObject tripApprovalReqObj;

    protected EditText tripRejectCommentsText;

    private static final String GET_TRIP_TO_APPROVE_RECEIVER = "trip.to.approve.receiver.token";

    BaseAsyncResultReceiver tripToApproveReceiver;

    private static final String GET_AGENCY_DETAILS_RECEIVER = "agency.details.receiver.token";

    BaseAsyncResultReceiver agencyDetailsReceiver;

    private String agentPreferredPhoneNumber;
    private String tripRecordLocatorForAgent;
    private String callAgentErrorMessage;

    private String totalTravelPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segmentlist);

        Intent intent = getIntent();

        isForTripApproval = intent.getBooleanExtra(Const.EXTRA_IS_FOR_TRIP_APPROVAL, false);

        // Restore any receivers.
        restoreReceivers();

        // Restore any non-configuration data.
        if (retainer != null) {
            // Restore any booking record locator.
            if (retainer.contains(EXTRA_BOOKING_RECORD_LOCATOR_KEY)) {
                bookingRecordLocator = (String) retainer.get(EXTRA_BOOKING_RECORD_LOCATOR_KEY);
            }
            // Restore any booking itinerary locator.
            if (retainer.contains(EXTRA_BOOKING_ITINERARY_LOCATOR_KEY)) {
                bookingItineraryLocator = (String) retainer.get(EXTRA_BOOKING_ITINERARY_LOCATOR_KEY);
            }

            if (!isForTripApproval) {
                // Restore any current long-press segment.
                if (retainer.contains(EXTRA_LONG_PRESS_SEGMENT_KEY)) {
                    longPressSegment = (Segment) retainer.get(EXTRA_LONG_PRESS_SEGMENT_KEY);
                }

                // Restore the car suggestion adapter.
                if (retainer.contains(EXTRA_CAR_SUGGESTION_ADAPTER)) {
                    carSuggestionAdapter = (SuggestionAdapter) retainer.get(EXTRA_CAR_SUGGESTION_ADAPTER);
                }

                // Restore the hotel suggestion adapter.
                if (retainer.contains(EXTRA_HOTEL_SUGGESTION_ADAPTER)) {
                    hotelSuggestionAdapter = (SuggestionAdapter) retainer.get(EXTRA_HOTEL_SUGGESTION_ADAPTER);
                }

                // Restore the call agent dialog information
                if (retainer.contains(EXTRA_AGENT_PREFERRED_PHONE_NUMBER)) {
                    agentPreferredPhoneNumber = (String) retainer.get(EXTRA_AGENT_PREFERRED_PHONE_NUMBER);
                    tripRecordLocatorForAgent = (String) retainer.get(EXTRA_TRIP_RECORD_LOCATOR_FOR_AGENT);
                    callAgentErrorMessage = (String) retainer.get(EXTRA_AGENT_ERROR_MESSAGE);
                }

                if (retainer.contains(EXTRA_TRIP_TOTAL_TRAVEL_POINTS)) {
                    totalTravelPoints = (String) retainer.get(EXTRA_TRIP_TOTAL_TRAVEL_POINTS);
                }
            }
        }

        if (carSuggestionAdapter == null) {
            // Initialize the suggestion adapter as it is needed in the
            // 'onOptionItemSelected' method.
            carSuggestionAdapter = new SuggestionAdapter();
        }

        if (hotelSuggestionAdapter == null) {
            // Initialize the suggestion adapter as it is needed in the
            // 'onOptionItemSelected' method.
            hotelSuggestionAdapter = new SuggestionAdapter();
        }

        if (savedInstanceState != null) {
            promptForAdd = savedInstanceState.getBoolean(Const.EXTRA_PROMPT_FOR_ADD);
        } else {
            promptForAdd = intent.getBooleanExtra(Const.EXTRA_PROMPT_FOR_ADD, false);
        }

        initScreenHeader();

        if (isServiceAvailable()) {
            buildView();
        } else {
            buildViewDelay = true;
        }

        boolean shouldCheck = this.getIntent().getBooleanExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, false);

        if (shouldCheck) {
            // Prompt for rating
            FeedbackManager.with(this).showRatingsPrompt();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the "prompt for add" state.
        outState.putBoolean(Const.EXTRA_PROMPT_FOR_ADD, promptForAdd);

    }

    @Override
    protected void onPostCreate(Bundle inState) {
        super.onPostCreate(inState);
        // Only display "prompt for add" if the activity wasn't created due
        // to an orientation change.
        // NOTE: At the moment this functionality only happens on pre-4.0
        // devices.
        // NOTE: PM is aware of this and we will address it in the future
        if (!orientationChange && promptForAdd) {
            final View addButton = findViewById(R.id.menuAdd);
            if (addButton != null) {
                addButton.post(new Runnable() {

                    @Override
                    public void run() {
                        registerForContextMenu(addButton);
                        openContextMenu(addButton);
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostCreate: unable to locate action button!");
            }
        }
    }

    @Override
    protected void onServiceAvailable() {
        if (buildViewDelay) {
            buildView();
            buildViewDelay = false;
        }
    }

    protected void buildView() {

        initScreenHeader();

        // Grab our trip out of the itinerary cache.
        Intent intent = getIntent();
        itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
        trip = null;
        if (itinLocator != null) {
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                trip = itinCache.getItinerary(itinLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: itin cache is null!");
            }
        }

        if (trip == null) {
            // Something is way not right. Get out and let the trips list reload
            // things properly.
            // MOB-10690
            finish();
            return;
        } else {
            int closestSegmentHours = trip.getClosestSegmentToNow();
            if (closestSegmentHours != -1) {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_NEAREST_SEGMENT_TO_NOW_IN_HOURS, Integer.toString(closestSegmentHours));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ITIN, Flurry.EVENT_NAME_VIEW_ITIN_SEGMENTS, params);
            }
        }

        // check if the current view is for trip approval
        if (isForTripApproval) {
            // get the trip approval related
            String travellerName = intent.getStringExtra(Const.EXTRA_TRAVELLER_NAME);
            String tripName = intent.getStringExtra(Const.EXTRA_TRIP_NAME);
            String totalTripCost = intent.getStringExtra(Const.EXTRA_TOTAL_TRIP_COST);
            String message = intent.getStringExtra(Const.EXTRA_TRIP_APPROVAL_MESSAGE);

            // initialize trip approve related header
            initTripApprovalHeader(travellerName, tripName, totalTripCost, message);

            // hide the regular itinerary related header
            hideTravelHeader();

            // Configure the screen footer with approve and reject buttons
            configureScreenFooterForApproval();

            // data to be available in the reject or approve dialog
            tripApprovalReqObj = new TripApprovalReqObject();
            tripApprovalReqObj.setTravellerCompanyId(intent.getStringExtra(Const.EXTRA_TRAVELLER_COMPANY_ID));
            tripApprovalReqObj.setTravellerUserId(intent.getStringExtra(Const.EXTRA_TRAVELLER_USER_ID));
            tripApprovalReqObj.setTripIdOfTripForApproval(intent.getStringExtra(Const.EXTRA_TRIP_ID));

        } else {
            hideTripApprovalHeader();
            hideTripApprovalScreenFooter();
            if (showCallTravelAgent()) {
                // show call travel agent button
                configureScreenFooter();
            }
            initTravelHeader();

            tripRecordLocatorForAgent = trip.recordLocatorForAgent;
            initAgencyRecordLocator();

            initTotalTravelPoints();
        }

        configureSegmentList();

        if (!promptForAdd) {
            // Prompt for rating if the client should not "prompt to add" a
            // car/hotel.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            if (Preferences.shouldPromptToRate(prefs, true)) {
                showDialog(Const.DIALOG_PROMPT_TO_RATE);
            }
        }
    }

    /**
     * Will initialize the travel header with from/to information.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initTravelHeader() {

        // Set the trip name.
        TextView tv = (TextView) findViewById(R.id.travel_name);
        // Just use the trip name.
        tv.setText(trip.name);

        // Set the date span.
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtil.SHORT_DAY_DISPLAY_NO_COMMA.format(trip.startLocal.getTime()));
        sb.append(" - ");
        sb.append(FormatUtil.SHORT_DAY_DISPLAY_NO_COMMA.format(trip.endLocal.getTime()));
        tv = (TextView) findViewById(R.id.date_span);
        tv.setText(sb.toString());
    }

    /**
     * Will show the trip record locator in the travel title header after the date span
     */
    protected void initAgencyRecordLocator() {
        if (tripRecordLocatorForAgent != null) {
            TextView tv = (TextView) findViewById(R.id.trip_record_locator);
            tv.setText(com.concur.mobile.base.util.Format.localizeText(ConcurCore.getContext(),
                    R.string.trip_record_locator_for_agent, new Object[] { tripRecordLocatorForAgent }));
            tv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Will initialize the travel header with from/to information.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void hideTravelHeader() {
        findViewById(R.id.travel_header).setVisibility(View.GONE);
    }

    protected void hideTripApprovalHeader() {
        findViewById(R.id.trip_approver_header).setVisibility(View.GONE);
    }

    /**
     * Will initialize the travel approval header.
     * 
     * @param travellerName
     * @param tripName
     * @param totalTripCost
     * @param message
     */
    protected void initTripApprovalHeader(final String travellerName, final String tripName,
            final String totalTripCost, final String message) {
        View tripHeaderView = findViewById(R.id.trip_approver_header);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_employee_name)).setText(travellerName);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_cost_amount)).setText(totalTripCost);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_trip_name)).setText(tripName);
        ((TextView) tripHeaderView.findViewById(R.id.trip_app_row_message)).setText(message);

        // initiate the Violation Summary header
        if (trip.getRuleViolations() != null && trip.getRuleViolations().size() > 0) {
            View violationSummaryView = tripHeaderView.findViewById(R.id.header_violation_summary);
            violationSummaryView.setVisibility(View.VISIBLE);
            violationSummaryView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ConcurCore.isConnected()) {
                        // show the list of trip rule violations
                        Intent i = new Intent(SegmentList.this, RuleViolationSummary.class);
                        i.putExtra(Const.EXTRA_TRAVELLER_NAME, travellerName);
                        i.putExtra(Const.EXTRA_TRIP_NAME, tripName);
                        i.putExtra(Const.EXTRA_TOTAL_TRIP_COST, totalTripCost);
                        i.putExtra(Const.EXTRA_TRIP_APPROVAL_MESSAGE, message);
                        // set the rule violations in the app object for
                        // retrieving in the intent
                        ConcurCore core = (ConcurCore) ConcurCore.getContext();
                        core.setTripRuleViolations(trip.getRuleViolations());
                        core.setTripRuleViolationsLastRetrieved(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                        startActivity(i);
                    }
                }
            });
        }
    }

    /**
     * Will initialize the screen header.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initScreenHeader() {
        if (isForTripApproval) {
            getSupportActionBar().setTitle(R.string.trip_approvals_title);
        } else {
            getSupportActionBar().setTitle(R.string.segmentlist_title);
        }
    }

    protected void hideTripApprovalScreenFooter() {
        findViewById(R.id.trip_approver_footer).setVisibility(View.GONE);
    }

    protected void configureScreenFooterForApproval() {
        final View tripFooterView = findViewById(R.id.trip_approver_footer);

        // reject functionality
        Button rejectButton = (Button) tripFooterView.findViewById(R.id.reject_button);
        if (rejectButton != null) {
            rejectButton.setText(getText(R.string.general_reject));
            rejectButton.setVisibility(View.VISIBLE);
            rejectButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ConcurCore.isConnected()) {
                        // show the dialog for reject comments
                        showDialog(DIALOG_TRIP_REJECT_COMMENTS);
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".trip approval footer : can't find 'reject' button!");
        }

        // approve functionality
        Button approveButton = (Button) tripFooterView.findViewById(R.id.approve_button);
        if (approveButton != null) {
            approveButton.setText(getText(R.string.general_approve));
            approveButton.setVisibility(View.VISIBLE);
            approveButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ConcurCore.isConnected()) {
                        showDialog(DIALOG_PROMPT_TO_APPROVE_TRIP);
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            });
        }

    }

    // call travel agent functionality
    protected void configureScreenFooter() {
        View callAgentView = findViewById(R.id.call_travel_agent);
        callAgentView.setVisibility(View.VISIBLE);

        Button leftButton = (Button) callAgentView.findViewById(R.id.left_button);
        if (leftButton != null) {
            leftButton.setText(getText(R.string.call_travel_agent));
            leftButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ConcurCore.isConnected()) {
                        agencyDetailsReceiver = new BaseAsyncResultReceiver(new Handler());
                        agencyDetailsReceiver.setListener(new AgencyDetailsListener());
                        new GetAgencyDetails(getApplicationContext(), 1, agencyDetailsReceiver, itinLocator).execute();
                        showDialog(DIALOG_GET_AGENCY_ASSISTANCE_PROGRESS);

                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".itinerary footer : can't find 'call travel agent' button!");
        }
    }

    // The 'Call Travel Agent' button should be available only if the 'Enable
    // Agency Assistance' module property is enabled
    private boolean showCallTravelAgent() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_SHOW_CALL_TRAVEL_AGENT, false);
    }

    // check to verify the device has phone capability
    private boolean deviceHasPhone() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".deviceHasPhone, device has no phone, so call option will not be available");
            return false;
        }
        return true;
    }

    /**
     * Will show the trip total travel points
     */
    protected void initTotalTravelPoints() {
        if (trip.itinTravelPoint != null) {
            totalTravelPoints = trip.itinTravelPoint.getTotalPoints();

            if (totalTravelPoints != null && totalTravelPoints.trim().length() > 0) {
                TextView tv = (TextView) findViewById(R.id.trip_total_travel_points_booked);
                tv.setText(com.concur.mobile.base.util.Format.localizeText(ConcurCore.getContext(),
                        R.string.trip_total_travel_points, new Object[] { totalTravelPoints }));
                tv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void restoreReceivers() {
        if (retainer != null) {
            if (isForTripApproval) {
                if (retainer.contains(GET_TRIP_TO_APPROVE_RECEIVER)) {
                    tripToApproveReceiver = (BaseAsyncResultReceiver) retainer.get(GET_TRIP_TO_APPROVE_RECEIVER);
                    tripToApproveReceiver.setListener(new ApproveTripListener());
                }
            } else {
                // Check if due an orientation change, there's a saved hotel
                // cancel receiver.

                if (retainer.contains(EXTRA_HOTEL_CANCEL_RECEIVER_KEY)) {
                    hotelCancelReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_HOTEL_CANCEL_RECEIVER_KEY);
                    // Reset the activity reference.
                    HotelCancelSegmentListerner hotelCancelSegmentListerner = new HotelCancelSegmentListerner();
                    hotelCancelReceiver.setListener(hotelCancelSegmentListerner);
                    hotelCancelSegmentListerner.setFm(getSupportFragmentManager());
                    hotelCancelSegmentListerner.setItineraryLocator(itinLocator);
                }
                if (retainer.contains(EXTRA_HOTEL_CANCEL_SEGMENT_KEY)) {
                    cancelSegment = (CancelSegment) retainer.get(EXTRA_HOTEL_CANCEL_SEGMENT_KEY);
                }

                // Check if due an orientation change, there's a saved car
                // cancel receiver.
                if (retainer.contains(EXTRA_CAR_CANCEL_RECEIVER_KEY)) {
                    carCancelReceiver = (CarCancelReceiver) retainer.get(EXTRA_CAR_CANCEL_RECEIVER_KEY);
                    // Reset the activity reference.
                    carCancelReceiver.setActivity(this);
                }
                // Check if due an orientation change, there's a saved rail
                // cancel receiver.
                if (retainer.contains(EXTRA_RAIL_CANCEL_RECEIVER_KEY)) {
                    railCancelReceiver = (RailCancelReceiver) retainer.get(EXTRA_RAIL_CANCEL_RECEIVER_KEY);
                    // Reset the activity reference.
                    railCancelReceiver.setActivity(this);
                }
                // Check if due an orientation change, there's a saved air
                // cancel receiver.
                if (retainer.contains(EXTRA_AIR_CANCEL_RECEIVER_KEY)) {
                    airCancelReceiver = (AirCancelReceiver) retainer.get(EXTRA_AIR_CANCEL_RECEIVER_KEY);
                    // Reset the activity reference.
                    airCancelReceiver.setActivity(this);
                }
                // Check if due to an orientation change, there's a saved rail
                // station list receiver.
                if (retainer.contains(EXTRA_RAIL_STATION_RECEIVER_KEY)) {
                    railStationReceiver = (RailStationReceiver) retainer.get(EXTRA_RAIL_STATION_RECEIVER_KEY);
                    // Reset the activity reference.
                    railStationReceiver.setActivity(this);
                }
                // Check if due to an orientation change, there's a saved agency
                // details receiver.
                if (retainer.contains(GET_AGENCY_DETAILS_RECEIVER)) {
                    agencyDetailsReceiver = (BaseAsyncResultReceiver) retainer.get(GET_AGENCY_DETAILS_RECEIVER);
                    agencyDetailsReceiver.setListener(new AgencyDetailsListener());
                }

                if (totalTravelPoints != null) {
                    retainer.put(EXTRA_TRIP_TOTAL_TRAVEL_POINTS, totalTravelPoints);
                }
            }
            // Restore itinerary summary list receiver.
            if (retainer.contains(ITINERARY_LIST_RECEIVER_KEY)) {
                itinerarySummaryListReceiver = (ItineraryListReceiver) retainer.get(ITINERARY_LIST_RECEIVER_KEY);
                itinerarySummaryListReceiver.setActivity(this);
            }
            // Restore itinerary receiver.
            if (retainer.contains(ITINERARY_RECEIVER_KEY)) {
                itineraryReceiver = (ItineraryReceiver) retainer.get(ITINERARY_RECEIVER_KEY);
                itineraryReceiver.setActivity(this);
            }
        }
    }

    /**
     * Whether the currently logged in end-user is a traveler.
     * 
     * @return whether the currently logged in end-user is a traveler.
     */
    protected boolean isTraveler() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Const.PREF_CAN_TRAVEL, false);
    }

    /**
     * Will configure the trip segment adapter and title.
     */
    private void configureSegmentList() {

        // Set the display values for the header information and set the list
        // adaptor
        if (trip != null) {

            boolean showOffers = (!isForTripApproval && Preferences.shouldShowOffers());

            // Construct a list of ListItems to be set on the list item adapter.
            List<ListItem> listItems = new ArrayList<ListItem>();

            // Grab both maps for start and end days
            final TreeMap<Calendar, List<Segment>> segmentStartDayMap = trip.getSegmentStartDayMap();
            final TreeMap<Calendar, List<Segment>> segmentEndDayMap = trip.getSegmentEndDayMap();

            // Combine all the keys into one sorted, non-duplicate set
            TreeSet<Calendar> allSegmentDays = new TreeSet<Calendar>(segmentStartDayMap.keySet());
            allSegmentDays.addAll(segmentEndDayMap.keySet());

            if (allSegmentDays != null) {
                long curTimeMillis = System.currentTimeMillis();
                boolean registerForContextMenu = false;
                boolean registeredForContextMenu = false;

                for (Calendar segDay : allSegmentDays) {
                    List<Segment> startDaySegs = segmentStartDayMap.get(segDay);
                    if (startDaySegs != null) {
                        for (Segment seg : startDaySegs) {
                            int segInd = startDaySegs.indexOf(seg);
                            // The first segment within a day list sets the date
                            // header.
                            if (segInd == 0) {
                                // Use this date as the date for the segment
                                // date section header
                                final String timeText = Format.safeFormatCalendar(FormatUtil.SHORT_DAY_DISPLAY,
                                        seg.getStartDateLocal());
                                HeaderListItem hdrListItem = new HeaderListItem(timeText, HEADER_VIEW_TYPE);
                                listItems.add(hdrListItem);
                            }

                            // Add offers if needed
                            if (showOffers) {
                                List<Offer> os = trip.getValidSegmentOffers(seg.segmentKey, OfferSegmentLink.SIDE_START
                                        | OfferSegmentLink.SIDE_DURATION);
                                if (os != null) {
                                    for (Offer o : os) {
                                        listItems.add(new OfferListItem(o, OFFER_VIEW_TYPE));
                                    }
                                }
                            }

                            // Construct the appropriate sub-class of ListItem.
                            registerForContextMenu = false;
                            switch (seg.getType()) {
                            case AIR: {
                                // For an air segment, use the trip date.
                                if (trip.startUtc != null) {
                                    if (curTimeMillis < trip.startUtc.getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                }
                                ListItem listItem = new AirSegmentListItem((AirSegment) seg, registerForContextMenu,
                                        SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case CAR: {
                                // Determine whether to show the context
                                // sensitive menu based on first checking for a
                                // car
                                // segment start date UTC being before the
                                // current time. If car doesn't have a start
                                // date
                                // UTC, then try the trip start date in UTC.
                                if (seg.getStartDateUtc() != null) {
                                    if (curTimeMillis < seg.getStartDateUtc().getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                } else if (trip.startUtc != null) {
                                    // No car segment start date UTC, try trip
                                    // start utc.
                                    if (curTimeMillis < trip.startUtc.getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                }
                                ListItem listItem = new CarSegmentListItem((CarSegment) seg, registerForContextMenu,
                                        SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case DINING: {
                                ListItem listItem = new DiningSegmentListItem((DiningSegment) seg,
                                        registerForContextMenu, SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case EVENT: {
                                ListItem listItem = new EventSegmentListItem((EventSegment) seg,
                                        registerForContextMenu, SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case HOTEL: {
                                // Determine whether to show the context
                                // sensitive menu based on first checking for a
                                // hotel
                                // segment start date UTC being before the
                                // current time. If hotel doesn't have a start
                                // date
                                // UTC, then try the trip start date in UTC.
                                if (seg.getStartDateUtc() != null) {
                                    if (curTimeMillis < seg.getStartDateUtc().getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                } else if (trip.startUtc != null) {
                                    // No hotel segment start date UTC, try trip
                                    // start utc.
                                    if (curTimeMillis < trip.startUtc.getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                }
                                ListItem listItem = new HotelSegmentListItem((HotelSegment) seg,
                                        registerForContextMenu, SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case PARKING: {
                                ListItem listItem = new ParkingSegmentListItem((ParkingSegment) seg,
                                        registerForContextMenu, SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case RAIL: {
                                // Determine whether to show the context
                                // sensitive menu based on first checking for a
                                // rail
                                // segment start date UTC being before the
                                // current time. If hotel doesn't have a start
                                // date
                                // UTC, then try the trip start date in UTC.
                                if (seg.getStartDateUtc() != null) {
                                    if (curTimeMillis < seg.getStartDateUtc().getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                } else if (trip.startUtc != null) {
                                    // No rail segment start date UTC, try trip
                                    // start utc.
                                    if (curTimeMillis < trip.startUtc.getTimeInMillis()) {
                                        registerForContextMenu = true;
                                    }
                                }
                                ListItem listItem = new RailSegmentListItem((RailSegment) seg, registerForContextMenu,
                                        SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            case RIDE: {
                                ListItem listItem = new RideSegmentListItem((RideSegment) seg, registerForContextMenu,
                                        SEGMENT_VIEW_TYPE);
                                listItems.add(listItem);
                                break;
                            }
                            default: {
                                // no-op.
                                break;
                            }
                            }

                            if (!registeredForContextMenu && registerForContextMenu) {
                                registerForContextMenu(findViewById(R.id.seg_list));
                                registeredForContextMenu = true;
                            }
                        }
                    }

                    if (showOffers) {
                        // Check for segments ending on this day and render
                        // offers
                        List<Segment> endDaySegs = segmentEndDayMap.get(segDay);
                        if (endDaySegs != null) {
                            for (Segment seg : endDaySegs) {
                                // Add offers if needed
                                List<Offer> os = trip.getValidSegmentOffers(seg.segmentKey, OfferSegmentLink.SIDE_END);
                                if (os != null) {
                                    for (Offer o : os) {
                                        listItems.add(new OfferListItem(o, OFFER_VIEW_TYPE));
                                    }
                                }

                            }
                        }
                    }
                }

            }

            // Set the adapter on the list.
            ListItemAdapter<ListItem> listItemAdapter = new ListItemAdapter<ListItem>(this, listItems);
            ListView listView = (ListView) findViewById(R.id.seg_list);
            if (listView != null) {
                listView.setAdapter(listItemAdapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ListItem item = (ListItem) parent.getItemAtPosition(position);

                        switch (item.getListItemViewType()) {
                        case SEGMENT_VIEW_TYPE: {
                            Segment segmentClicked = (Segment) view.getTag();
                            if (segmentClicked != null) {
                                Intent intent = getClickedSegIntent(segmentClicked);
                                intent.putExtra(Const.EXTRA_ITIN_LOCATOR, trip.itinLocator);
                                intent.putExtra(Const.EXTRA_SEGMENT_KEY, segmentClicked.segmentKey);
                                intent.putExtra(Const.EXTRA_IS_FOR_TRIP_APPROVAL, isForTripApproval);
                                startActivityForResult(intent, Const.REQUEST_CODE_VIEW_SEGMENT_DETAIL);
                                // Flurry notification.
                                sendFlurryEventForClickedSegment(segmentClicked);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onItemClick: unable to locate selected segment in view tag!");
                            }
                            break;
                        }
                        case OFFER_VIEW_TYPE: {
                            Offer o = (Offer) view.getTag();
                            if (o != null) {
                                Intent i = o.getOfferLaunchIntent(SegmentList.this, itinLocator);
                                if (i != null) {
                                    startActivity(i);
                                }
                            }
                            break;
                        }
                        }
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureSegmentList: unable to locate seg_list list view!");
            }
        }
    }

    /**
     * get the segment clicked intent. it will return AirSegmentDetail, CarSegmentDetail,HotelSegmentDetail,RailSegmentDetail
     * respectively based on condition
     * */
    protected Intent getClickedSegIntent(Segment segmentClicked) {
        return new Intent(SegmentList.this, segmentClicked.getType().activity);
    }

    /**
     * Will send a Flurry notification for the clicked segment.
     * 
     * @param segment
     *            contains the segment that was clicked.
     */
    protected void sendFlurryEventForClickedSegment(Segment segment) {
        if (segment != null && segment.getType() != null) {
            String paramValue = null;
            switch (segment.getType()) {
            case AIR: {
                paramValue = Flurry.PARAM_VALUE_AIR;
                break;
            }
            case CAR: {
                paramValue = Flurry.PARAM_VALUE_CAR;
                break;
            }
            case HOTEL: {
                paramValue = Flurry.PARAM_VALUE_HOTEL;
                break;
            }
            case RAIL: {
                paramValue = Flurry.PARAM_VALUE_TRAIN;
                break;
            }
            case DINING: {
                paramValue = Flurry.PARAM_VALUE_DINING;
                break;
            }
            case EVENT: {
                paramValue = Flurry.PARAM_VALUE_EVENT;
                break;
            }
            case PARKING: {
                paramValue = Flurry.PARAM_VALUE_PARKING;
                break;
            }
            case RIDE: {
                paramValue = Flurry.PARAM_VALUE_RIDE;
                break;
            }
            default:
                break;
            }
            if (paramValue != null) {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_TYPE, paramValue);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ITIN, Flurry.EVENT_NAME_VIEW_SEGMENT,
                        params);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget .AdapterView, android.view.View, int, long)
     */

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean handled = super.onContextItemSelected(item);
        if (!handled) {
            final int itemId = item.getItemId();
            if (itemId == MENU_ITEM_VIEW_DETAILS) {
                handled = false;
            } else if (itemId == MENU_ITEM_CANCEL) {
                if (longPressSegment instanceof HotelSegment) {
                    showDialog(DIALOG_CANCEL_HOTEL_CONFIRM);
                    handled = true;
                } else if (longPressSegment instanceof CarSegment) {
                    showDialog(DIALOG_CANCEL_CAR_CONFIRM);
                    handled = true;
                } else if (longPressSegment instanceof AirSegment) {
                    showDialog(DIALOG_CANCEL_AIR_CONFIRM);
                    handled = true;
                } else if (longPressSegment instanceof RailSegment) {
                    showDialog(DIALOG_CANCEL_RAIL_CONFIRM);
                    handled = true;
                }
            } else if (itemId == R.id.menuTripBookCar) {
                addCar();
                handled = true;
            } else if (itemId == R.id.menuTripBookHotel) {
                addHotel();
                handled = true;
            }
        }
        return handled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        final int id = view.getId();
        if (id == R.id.menuAdd) {
            android.view.MenuInflater infl = getMenuInflater();
            infl.inflate(R.menu.segmentlist_book, menu);
            // MOB-13402
            if (!trip.allowAddCar || isRejected(trip)) {
                menu.findItem(R.id.menuTripBookCar).setVisible(false);
            }
            // MOB-13402
            if (!trip.allowAddHotel || isRejected(trip)) {
                menu.findItem(R.id.menuTripBookHotel).setVisible(false);
            }

            menu.setHeaderTitle(R.string.home_action_title);
        } else if (id == R.id.seg_list) {
            longPressSegment = null;
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Object tag = info.targetView.getTag();
            if (tag instanceof Segment) {
                longPressSegment = (Segment) tag;
            }
            if (longPressSegment != null) {
                if (isTraveler()) {
                    if (longPressSegment instanceof HotelSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_hotel_long_press_hotel_title);
                        if (trip.allowCancel) {
                            menu.add(0, MENU_ITEM_CANCEL, 0, R.string.general_cancel_hotel);
                        }
                    } else if (longPressSegment instanceof CarSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_car_long_press_car_title);
                        if (trip.allowCancel) {
                            menu.add(0, MENU_ITEM_CANCEL, 0, R.string.general_cancel_car);
                        }
                    } else if (longPressSegment instanceof AirSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_air_long_press_air_title);
                        // Hide air cancel op.
                        // if(trip.allowCancel) {
                        // menu.add(0, MENU_ITEM_CANCEL, 0,
                        // R.string.general_cancel_air);
                        // }
                    } else if (longPressSegment instanceof RailSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_rail_long_press_rail_title);
                        // Check for "allow cancel" *and* that the booking
                        // source is 'Amtrak'.
                        // Disable rail cancel for now MOB-11748.
                        // if(trip.allowCancel && longPressSegment.bookingSource
                        // != null &&
                        // longPressSegment.bookingSource.equalsIgnoreCase(Const.RAIL_BOOKING_SOURCE_AMTRAK))
                        // {
                        // menu.add(0, MENU_ITEM_CANCEL, 0,
                        // R.string.general_cancel_rail);
                        // }
                    }
                }
                android.view.MenuItem menuItem = menu.add(0, MENU_ITEM_VIEW_DETAILS, 0, R.string.view_details);
                Intent intent = new Intent(this, longPressSegment.getType().activity);
                intent.putExtra(Const.EXTRA_ITIN_LOCATOR, trip.itinLocator);
                intent.putExtra(Const.EXTRA_SEGMENT_KEY, longPressSegment.segmentKey);
                menuItem.setIntent(intent);
            }
        }
    }

    private boolean isRejected(Trip trip) {
        if (trip.approvalStatusEnum == ApprovalStatusEnum.RejectedAndClosed
                || trip.approvalStatusEnum == ApprovalStatusEnum.RejectedCantOverride
                || trip.approvalStatusEnum == ApprovalStatusEnum.RejectedOverridable
                || trip.approvalStatusEnum == ApprovalStatusEnum.Withdrawn) {
            return true;
        } else {
            return false;
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

        switch (id) {
        case DIALOG_ADD_CAR: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.car_search_suggestions);
            builder.setSingleChoiceItems(carSuggestionAdapter, -1, new DialogInterface.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    searchCar((SearchSuggestion) carSuggestionAdapter.getItem(which));
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = builder.create();
            break;
        }
        case DIALOG_ADD_HOTEL: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.hotel_search_suggestions);
            builder.setSingleChoiceItems(hotelSuggestionAdapter, -1, new DialogInterface.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    searchHotel((SearchSuggestion) hotelSuggestionAdapter.getItem(which));
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = builder.create();
            break;
        }
        case DIALOG_CANCEL_HOTEL_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_hotel_confirm_cancel_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                    if (longPressSegment != null) {
                        hotelCancelReceiver = new BaseAsyncResultReceiver(new Handler());
                        HotelCancelSegmentListerner hotelCancelSegmentListerner = new HotelCancelSegmentListerner();
                        hotelCancelReceiver.setListener(hotelCancelSegmentListerner);
                        hotelCancelSegmentListerner.setFm(getSupportFragmentManager());
                        hotelCancelSegmentListerner.setItineraryLocator(itinLocator);

                        String bookingSource = longPressSegment.bookingSource;
                        // TODO: Future: provide a text view inside the
                        // confirm permitting the end-user
                        // to provide a justification.
                        String reason = null;
                        String recordLocator = longPressSegment.locator;
                        String segmentKey = longPressSegment.segmentKey;
                        String tripId = trip.cliqbookTripId;

                        if (ConcurCore.isConnected()) {
                            cancelSegment = new CancelSegment(SegmentList.this, getApplicationContext(), 1,
                                    hotelCancelReceiver, SegmentType.HOTEL, bookingSource, null, recordLocator,
                                    segmentKey, tripId);

                            // Make the call
                            cancelSegment.execute();
                        } else {
                            showDialog(Const.DIALOG_NO_CONNECTIVITY);
                        }

                        longPressSegment = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: hotel segment to be canceled is null!");
                    }
                }
            });
            dlgBldr.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Clear the segment long press reference.
                    longPressSegment = null;
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_CAR_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_car_confirm_cancel_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    if (longPressSegment != null) {
                        String bookingSource = longPressSegment.bookingSource;
                        String reason = null;
                        String recordLocator = longPressSegment.locator;
                        String segmentKey = longPressSegment.segmentKey;
                        String tripId = trip.cliqbookTripId;

                        // Make the call
                        ConcurService svc = ((ConcurCore) getApplication()).getService();
                        if (svc != null) {
                            if (carCancelReceiver == null) {
                                carCancelReceiver = new CarCancelReceiver(SegmentList.this);
                            }
                            getApplicationContext().registerReceiver(carCancelReceiver, carCancelFilter);
                            cancelCarRequest = svc.sendCancelCarRequest(bookingSource, reason, recordLocator,
                                    segmentKey, tripId);
                            carCancelReceiver.setRequest(cancelCarRequest);
                            showDialog(DIALOG_CANCEL_CAR_PROGRESS);
                        }

                        // Clear the segment long press reference.
                        longPressSegment = null;

                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: car segment to be canceled is null!");
                    }
                }
            });
            dlgBldr.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Clear the segment long press reference.
                    longPressSegment = null;
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_RAIL_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_rail_confirm_cancel_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    if (longPressSegment != null) {
                        String bookingSource = longPressSegment.bookingSource;
                        String reason = null;
                        String recordLocator = longPressSegment.locator;
                        String segmentKey = longPressSegment.segmentKey;
                        String tripId = trip.itinLocator;

                        // Make the call
                        ConcurService svc = ((ConcurCore) getApplication()).getService();
                        if (svc != null) {
                            if (railCancelReceiver == null) {
                                railCancelReceiver = new RailCancelReceiver(SegmentList.this);
                            }
                            getApplicationContext().registerReceiver(railCancelReceiver, railCancelFilter);
                            cancelRailRequest = svc.sendCancelRailRequest(bookingSource, reason, recordLocator,
                                    segmentKey, tripId);
                            railCancelReceiver.setRequest(cancelRailRequest);
                            showDialog(DIALOG_CANCEL_RAIL_PROGRESS);
                        }

                        // Clear the segment long press reference.
                        longPressSegment = null;

                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: rail segment to be canceled is null!");
                    }
                }
            });
            dlgBldr.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Clear the segment long press reference.
                    longPressSegment = null;
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_AIR_CONFIRM: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_air_confirm_cancel_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    if (longPressSegment != null) {
                        String reason = null;
                        String recordLocator = longPressSegment.locator;

                        // Make the call
                        ConcurService svc = ((ConcurCore) getApplication()).getService();
                        if (svc != null) {
                            if (airCancelReceiver == null) {
                                airCancelReceiver = new AirCancelReceiver(SegmentList.this);
                            }
                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext());
                            String userId = prefs.getString(Const.PREF_USER_ID, null);
                            getApplicationContext().registerReceiver(airCancelReceiver, airCancelFilter);
                            cancelAirRequest = svc.sendAirCancelRequest(userId, recordLocator, reason);
                            airCancelReceiver.setRequest(cancelAirRequest);
                            showDialog(DIALOG_CANCEL_AIR_PROGRESS);
                        }

                        // Clear the segment long press reference.
                        longPressSegment = null;

                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: air segment to be canceled is null!");
                    }
                }
            });
            dlgBldr.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Clear the segment long press reference.
                    longPressSegment = null;
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_CAR_PROGRESS: {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(this.getText(R.string.dlg_car_cancel_progress_message));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel the request.
                    if (cancelCarRequest != null) {
                        cancelCarRequest.cancel();
                    }
                    // Unregister the receiver and clear the reference to the
                    // receiver.
                    if (carCancelReceiver != null) {
                        getApplicationContext().unregisterReceiver(carCancelReceiver);
                        carCancelReceiver = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onCreateDialog.CarProgressDialog.onCancel: null car cancel receiver!");
                    }
                }
            });
            dlg = dialog;
            break;
        }
        case DIALOG_CANCEL_RAIL_PROGRESS: {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(this.getText(R.string.dlg_rail_cancel_progress_message));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel the request.
                    if (cancelRailRequest != null) {
                        cancelRailRequest.cancel();
                    }
                    // Unregister the receiver and clear the reference to the
                    // receiver.
                    if (railCancelReceiver != null) {
                        getApplicationContext().unregisterReceiver(railCancelReceiver);
                        railCancelReceiver = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onCreateDialog.RailProgressDialog.onCancel: null rail cancel receiver!");
                    }
                }
            });
            dlg = dialog;
            break;
        }
        case DIALOG_CANCEL_AIR_PROGRESS: {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(this.getText(R.string.dlg_air_cancel_progress_message));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel the request.
                    if (cancelAirRequest != null) {
                        cancelAirRequest.cancel();
                    }
                    // Unregister the receiver and clear the reference to the
                    // receiver.
                    if (airCancelReceiver != null) {
                        getApplicationContext().unregisterReceiver(airCancelReceiver);
                        airCancelReceiver = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onCreateDialog.CarProgressDialog.onCancel: null air cancel receiver!");
                    }
                }
            });
            dlg = dialog;
            break;
        }
        case DIALOG_CANCEL_CAR_SUCCESS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_car_cancel_succeeded_title);
            dlgBldr.setMessage(R.string.dlg_car_cancel_succeeded_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendItineraryRequest(itinLocator);
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_RAIL_SUCCESS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_rail_cancel_succeeded_title);
            dlgBldr.setMessage(R.string.dlg_rail_cancel_succeeded_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendItineraryRequest(itinLocator);
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_AIR_SUCCESS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_air_cancel_succeeded_title);
            dlgBldr.setMessage(R.string.dlg_air_cancel_succeeded_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendItineraryRequest(itinLocator);
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_ITINERARY_REFRESH_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_itinerary_refresh_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_ITINERARY_GONE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_itinerary_gone_title);
            dlgBldr.setMessage(R.string.dlg_itinerary_gone_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnClickListener#onClick (android.content.DialogInterface, int)
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // dismiss the dialog and finish the activity.
                    dialog.dismiss();
                    finish();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_CAR_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_car_cancel_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_RAIL_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_rail_cancel_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CANCEL_AIR_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_air_cancel_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_RAIL_STATION_PROGRESS: {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(this.getText(R.string.retrieve_rail_station_list));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.content.DialogInterface.OnCancelListener#onCancel (android.content.DialogInterface)
                 */
                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel the request.
                    if (railStationListRequest != null) {
                        railStationListRequest.cancel();
                    }
                    // Unregister the receiver and clear the reference to the
                    // receiver.
                    if (railStationReceiver != null) {
                        getApplicationContext().unregisterReceiver(railStationReceiver);
                        railStationReceiver = null;
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".onCreateDialog.RailStationListProgressDialog.onCancel: null rail station list receiver!");
                    }
                }
            });
            dlg = dialog;
            break;
        }
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY: {
            dlg = super.onCreateDialog(id);
            dlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancel any outstanding request.
                    if (itineraryRequest != null) {
                        itineraryRequest.cancel();
                    }
                }
            });
            break;
        }
        case DIALOG_ITINERARY_LIST_REFRESH_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_itinerary_list_refresh_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_ITINERARY_LIST_REFRESH: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_retrieving_itin));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            dlg = progDlg;
            break;
        }
        case DIALOG_TRIP_REJECT_COMMENTS: {
            tripApprovalReqObj.setTripApproveAction(Const.TRIP_APPROVAL_ACTION_REJECT);

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.comment);
            dlgBldr.setCancelable(true);
            dlgBldr.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });

            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Editable commentText = tripRejectCommentsText.getText();
                    if (commentText == null) {
                        showDialog(DIALOG_TRIP_REJECT_COMMENT_PROMPT);
                    } else {
                        String comment = commentText.toString().trim();
                        if (comment.length() == 0) {
                            showDialog(DIALOG_TRIP_REJECT_COMMENT_PROMPT);
                        } else {
                            // invoke async task
                            executeTripApproval(Const.TRIP_APPROVAL_ACTION_REJECT, comment,
                                    tripApprovalReqObj.getTravellerCompanyId(),
                                    tripApprovalReqObj.getTravellerUserId(),
                                    tripApprovalReqObj.getTripIdOfTripForApproval());

                            showDialog(DIALOG_TRIP_APPROVAL_PROGRESS);
                        }
                    }
                }
            });

            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            tripRejectCommentsText = new EditText(this);
            tripRejectCommentsText.setMinLines(5);
            tripRejectCommentsText.setMaxLines(5);
            tripRejectCommentsText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            dlgBldr.setView(tripRejectCommentsText);
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_TRIP_REJECT_COMMENT_PROMPT: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setMessage(getText(R.string.send_back_comment_prompt));
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (ConcurCore.isConnected()) {
                        showDialog(DIALOG_TRIP_REJECT_COMMENTS);
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_PROMPT_TO_APPROVE_TRIP: {
            tripApprovalReqObj.setTripApproveAction(Const.TRIP_APPROVAL_ACTION_APPROVE);

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.please_confirm);
            dlgBldr.setMessage(R.string.trip_confirm_approve_msg);
            dlgBldr.setPositiveButton(getText(R.string.general_yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // invoke async task
                    executeTripApproval(Const.TRIP_APPROVAL_ACTION_APPROVE, null,
                            tripApprovalReqObj.getTravellerCompanyId(), tripApprovalReqObj.getTravellerUserId(),
                            tripApprovalReqObj.getTripIdOfTripForApproval());

                    showDialog(DIALOG_TRIP_APPROVAL_PROGRESS);
                }

            });
            dlgBldr.setNegativeButton(getText(R.string.general_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlgBldr.setCancelable(false);
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_TRIP_APPROVAL_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);

            String progressMsg = "";
            if (tripApprovalReqObj.getTripApproveAction().equals(Const.TRIP_APPROVAL_ACTION_APPROVE)) {
                progressMsg = getText(R.string.approve_trip).toString();
            } else {
                progressMsg = getText(R.string.reject_trip).toString();
            }
            progDlg.setMessage(progressMsg);

            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            dlg = progDlg;
            break;
        }
        case DIALOG_TRIP_APPROVAL_ACTION_FAILURE: {
            // for both approve and reject request failures
            String msg = "";
            if (tripApprovalReqObj.getTripApproveAction().equals(Const.TRIP_APPROVAL_ACTION_APPROVE)) {
                msg = getText(R.string.trip_approval_failed_msg).toString();
            } else {
                msg = getText(R.string.trip_reject_failed_msg).toString();
            }

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setMessage(msg);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_TRIP_APPROVAL_ACTION_SUCCESS: {
            // for both approve and reject request success
            String msg = "";
            if (tripApprovalReqObj.getTripApproveAction().equals(Const.TRIP_APPROVAL_ACTION_APPROVE)) {
                msg = getText(R.string.trip_approval_success_msg).toString();
            } else {
                msg = getText(R.string.trip_reject_success_msg).toString();
            }

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setMessage(msg);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // send back to the approvals list where it would be
                    // refreshed
                    finish();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_PROMPT_TO_CALL_AGENT: {
            // show the agent preferred phone number and the trip record locator
            // (if any)
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(agentPreferredPhoneNumber);

            boolean showCallDialog = true;
            StringBuilder msgBldr = new StringBuilder();

            if (FormatUtil.nullCheckForString(agentPreferredPhoneNumber).length() == 0) {
                showCallDialog = false;
                // show message that phone number is not configured for the
                // travel agent
                msgBldr.append(getText(R.string.call_agent_phone_number_not_configured));
            }
            if (!deviceHasPhone()) {
                showCallDialog = false;
                // show message that device is not capable of making a phone
                // call
                msgBldr.append(getText(R.string.device_cannot_make_phone_call));
            }

            if (showCallDialog) {
                dlgBldr.setPositiveButton(getText(R.string.general_call), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        callAgent();
                    }
                });
                dlgBldr.setNegativeButton(getText(R.string.general_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            } else {
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }

            if (tripRecordLocatorForAgent == null) {
                msgBldr.append(callAgentErrorMessage);
            } else {
                msgBldr.append(com.concur.mobile.base.util.Format.localizeText(this, R.string.dlg_trip_rec_locator,
                        new Object[] { tripRecordLocatorForAgent }));
            }

            dlgBldr.setMessage(msgBldr);
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_GET_AGENCY_ASSISTANCE_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_retrieving_agency_info));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            dlg = progDlg;
            break;
        }
        case DIALOG_AGENCY_ASSISTANCE_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setMessage(callAgentErrorMessage);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_AGENT_CHANGED_ITINERARY: {
            // show message for the changes made on itinerary by the travel
            // agent
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.general_attention));
            dlgBldr.setMessage(getText(R.string.call_agent_itin_msg));
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // refresh the itinerary list
                    refreshItinerary(itinLocator);
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
        return dlg;
    }

    /**
     * Invokes the TripApproval Async task
     * 
     * @param action
     * @param comments
     * @param travellerCompanyId
     * @param travellerUserId
     * @param tripIdForAction
     */
    private void executeTripApproval(String action, String comments, String travellerCompanyId, String travellerUserId,
            String tripIdForAction) {
        // register the receiver
        tripToApproveReceiver = new BaseAsyncResultReceiver(new Handler());
        tripToApproveReceiver.setListener(new ApproveTripListener());

        // start the async task
        new TripApproval(getApplicationContext(), 1, tripToApproveReceiver, action, comments, travellerCompanyId,
                travellerUserId, tripIdForAction).execute();
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
        case DIALOG_CANCEL_HOTEL_CONFIRM: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            // NOTE: Managed dialogs across configuration changes have their
            // 'onPrepareDailog' methods invokved
            // even though the dialogs aren't currently displayed!
            if (longPressSegment != null) {
                alertDlg.setMessage(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.dlg_hotel_confirm_cancel_message, longPressSegment.startCity, Format
                                .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY,
                                        longPressSegment.getStartDayLocal()), Format.safeFormatCalendar(
                                FormatUtil.SHORT_MONTH_DAY_DISPLAY, longPressSegment.getEndDayLocal())));
            }
            break;
        }
        case DIALOG_CANCEL_CAR_CONFIRM: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            // NOTE: Managed dialogs across configuration changes have their
            // 'onPrepareDailog' methods invokved
            // even though the dialogs aren't currently displayed!
            if (longPressSegment != null) {
                alertDlg.setMessage(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.dlg_car_confirm_cancel_message, ((CarSegment) longPressSegment).startAirportCity,
                        Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY,
                                longPressSegment.getStartDayLocal()), Format.safeFormatCalendar(
                                FormatUtil.SHORT_MONTH_DAY_DISPLAY, longPressSegment.getEndDayLocal())));
            }
            break;
        }
        case DIALOG_CANCEL_RAIL_CONFIRM: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            // NOTE: Managed dialogs across configuration changes have their
            // 'onPrepareDailog' methods invokved
            // even though the dialogs aren't currently displayed!
            if (longPressSegment != null) {
                alertDlg.setMessage(com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.dlg_rail_confirm_cancel_message, ((RailSegment) longPressSegment).startRailStation,
                        Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY,
                                longPressSegment.getStartDayLocal()), Format.safeFormatCalendar(
                                FormatUtil.SHORT_MONTH_DAY_DISPLAY, longPressSegment.getEndDayLocal())));
            }
            break;
        }
        case DIALOG_CANCEL_AIR_CONFIRM: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(getText(R.string.dlg_air_confirm_cancel_message));
            break;
        }
        case DIALOG_CANCEL_CAR_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case DIALOG_CANCEL_RAIL_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case DIALOG_CANCEL_AIR_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case DIALOG_ITINERARY_REFRESH_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        case DIALOG_ADD_CAR: {
            carSuggestionAdapter.resolveDuplicateCitySearchSuggestions();
            break;
        }
        case DIALOG_ADD_HOTEL: {
            hotelSuggestionAdapter.resolveDuplicateCitySearchSuggestions();
            break;
        }
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case DIALOG_ITINERARY_LIST_REFRESH_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case DIALOG_PROMPT_TO_APPROVE_TRIP: {
            tripApprovalReqObj.setTripApproveAction(Const.TRIP_APPROVAL_ACTION_APPROVE);
            break;
        }
        case DIALOG_TRIP_REJECT_COMMENTS: {
            tripApprovalReqObj.setTripApproveAction(Const.TRIP_APPROVAL_ACTION_REJECT);
            break;
        }
        case DIALOG_TRIP_APPROVAL_PROGRESS: {
            String progressMsg = "";
            if (tripApprovalReqObj.getTripApproveAction().equals(Const.TRIP_APPROVAL_ACTION_APPROVE)) {
                progressMsg = getText(R.string.approve_trip).toString();
            } else {
                progressMsg = getText(R.string.reject_trip).toString();
            }

            ProgressDialog progDlg = (ProgressDialog) dialog;
            progDlg.setMessage(progressMsg);
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            break;
        }
        case DIALOG_TRIP_APPROVAL_ACTION_FAILURE: {
            String msg = "";
            if (tripApprovalReqObj.getTripApproveAction().equals(Const.TRIP_APPROVAL_ACTION_APPROVE)) {
                msg = getText(R.string.trip_approval_failed_msg).toString();
            } else {
                msg = getText(R.string.trip_reject_failed_msg).toString();
            }

            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(msg);
            break;
        }
        case DIALOG_TRIP_APPROVAL_ACTION_SUCCESS: {
            String msg = "";
            if (tripApprovalReqObj.getTripApproveAction().equals(Const.TRIP_APPROVAL_ACTION_APPROVE)) {
                msg = getText(R.string.trip_approval_success_msg).toString();
            } else {
                msg = getText(R.string.trip_reject_success_msg).toString();
            }

            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(msg);
            break;
        }
        case DIALOG_PROMPT_TO_CALL_AGENT: {
            // show the agent preferred phone number and the trip record locator
            // (if any)
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setTitle(agentPreferredPhoneNumber);

            StringBuilder msgBldr = new StringBuilder();

            if (FormatUtil.nullCheckForString(agentPreferredPhoneNumber).length() == 0) {
                // show message that phone number is not configured for the
                // travel agent
                msgBldr.append(getText(R.string.call_agent_phone_number_not_configured));
            }

            if (!deviceHasPhone()) {
                // show message that device is not capable of making a phone
                // call
                msgBldr.append(getText(R.string.device_cannot_make_phone_call));
            }

            if (tripRecordLocatorForAgent == null) {
                msgBldr.append(callAgentErrorMessage);
            } else {
                msgBldr.append(com.concur.mobile.base.util.Format.localizeText(this, R.string.dlg_trip_rec_locator,
                        new Object[] { tripRecordLocatorForAgent }));
            }
            alertDlg.setMessage(msgBldr);
            break;
        }
        case DIALOG_AGENCY_ASSISTANCE_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(callAgentErrorMessage);
            break;
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
        boolean retVal = false;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.segment_list, menu);

        if (isForTripApproval) {
            MenuItem offersMenuItem = menu.findItem(R.id.toggle_offers);
            offersMenuItem.setEnabled(false);
            offersMenuItem.setVisible(false);

            MenuItem offerValidityMenuItem = menu.findItem(R.id.toggle_offer_validity);
            offerValidityMenuItem.setEnabled(false);
            offerValidityMenuItem.setVisible(false);

            MenuItem addCarMenuItem = menu.findItem(R.id.add_car);
            // not available in older version layout menu/segment_list -
            // MOB-14411
            if (addCarMenuItem != null) {
                addCarMenuItem.setEnabled(false);
                addCarMenuItem.setVisible(false);
            }

            MenuItem addHotelMenuItem = menu.findItem(R.id.add_hotel);
            // not available in older version layout menu/segment_list -
            // MOB-14411
            if (addHotelMenuItem != null) {
                addHotelMenuItem.setEnabled(false);
                addHotelMenuItem.setVisible(false);
            }

            retVal = true;
        } else if ((trip != null) && isTraveler()) {
            long curTimeMillis = System.currentTimeMillis();
            // Only show the menu if the trip is still in the present/future.
            if (trip.endUtc != null && curTimeMillis < trip.endUtc.getTimeInMillis()) {
                if (!trip.hasOffers()) {
                    menu.removeItem(R.id.toggle_offers);
                    menu.removeItem(R.id.toggle_offer_validity);
                }
                // Travel booking allowed?
                // MOB-13402
                if (Preferences.shouldAllowTravelBooking()) {
                    if (!trip.allowAddCar || isRejected(trip)) {
                        menu.removeItem(R.id.add_car);
                    }
                } else {
                    menu.removeItem(R.id.add_car);
                }

                // Travel booking allowed?
                // MOB-13402
                if (Preferences.shouldAllowTravelBooking()) {
                    if (!trip.allowAddHotel || isRejected(trip)) {
                        menu.removeItem(R.id.add_hotel);
                    }
                } else {
                    menu.removeItem(R.id.add_hotel);
                }
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!isForTripApproval) {

            final MenuItem addButton = menu.findItem(R.id.menuAdd);
            if (addButton != null) {
                // MOB-13402
                if (Preferences.shouldAllowTravelBooking()) {
                    if (trip != null && (!trip.allowAddCar && !trip.allowAddHotel) && (isRejected(trip))) {
                        addButton.setVisible(false);
                        addButton.setEnabled(false);
                    }
                } else {
                    addButton.setVisible(false);
                    addButton.setEnabled(false);
                }
            }

            if (trip != null && trip.hasOffers()) {
                MenuItem item = menu.findItem(R.id.toggle_offers);
                if (item != null) {
                    if (Preferences.shouldShowOffers()) {
                        // Set the item to be hide
                        item.setTitle(R.string.segmentlist_offer_hide);
                    } else {
                        // Set the item to be show
                        item.setTitle(R.string.segmentlist_offer_show);
                    }
                }

                ConcurCore app = (ConcurCore) ConcurCore.getContext();
                String loginId = Preferences.getLogin(PreferenceManager.getDefaultSharedPreferences(app), "")
                        .toLowerCase();
                if (loginId.endsWith("@democoncur.com") || loginId.endsWith("@concur.com")
                        || loginId.endsWith("@snwjune.com")) {
                    item = menu.findItem(R.id.toggle_offer_validity);
                    if (item != null) {
                        if (Preferences.shouldCheckOfferValidity()) {
                            // Set the item to be disable
                            item.setTitle(R.string.segmentlist_offer_disable_validity);
                        } else {
                            // Set the item to be enable
                            item.setTitle(R.string.segmentlist_offer_enable_validity);
                        }
                    }
                }

            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean retVal = false;
        final int itemId = menuItem.getItemId();
        if (itemId == R.id.menuAdd) {
            registerForContextMenu(findViewById(itemId));
            openContextMenu(findViewById(itemId));
        } else if (itemId == R.id.add_car) {
            addCar();
            retVal = true;
        } else if (itemId == R.id.add_hotel) {
            addHotel();
            retVal = true;
        } else if (itemId == R.id.toggle_offers) {
            if (Preferences.shouldShowOffers()) {
                Preferences.disableShowOffers();
            } else {
                Preferences.enableShowOffers();
            }
            configureSegmentList();
            retVal = true;
        } else if (itemId == R.id.toggle_offer_validity) {
            if (Preferences.shouldCheckOfferValidity()) {
                Preferences.disableOfferValidityCheck();
            } else {
                Preferences.enableOfferValidityCheck();
            }
            configureSegmentList();
            retVal = true;
        } else if (itemId == R.id.refresh) {
            if (isForTripApproval) {
                refreshItinerary(itinLocator, tripApprovalReqObj.getTravellerCompanyId(),
                        tripApprovalReqObj.getTravellerUserId(), true);
            } else {
                refreshItinerary(itinLocator);
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {
            if (isForTripApproval) {
                if (tripToApproveReceiver != null) {
                    tripToApproveReceiver.setListener(null);
                    retainer.put(GET_TRIP_TO_APPROVE_RECEIVER, tripToApproveReceiver);
                }
            } else {
                if (hotelCancelReceiver != null) {
                    // Clear the activity reference, it will be set in the new
                    // SegmentList instance.
                    hotelCancelReceiver.setListener(null);
                    retainer.put(EXTRA_HOTEL_CANCEL_RECEIVER_KEY, hotelCancelReceiver);
                    if (cancelSegment != null && cancelSegment.progressDialog != null) {
                        if (!cancelSegment.progressDialog.isShowing()) {
                            cancelSegment.progressDialog.dismiss();
                        }
                        retainer.put(EXTRA_HOTEL_CANCEL_SEGMENT_KEY, cancelSegment);
                    }
                }
                if (carCancelReceiver != null) {
                    // Clear the activity reference, it will be set in the new
                    // SegmentList instance.
                    carCancelReceiver.setActivity(null);
                    retainer.put(EXTRA_CAR_CANCEL_RECEIVER_KEY, carCancelReceiver);
                }
                if (railCancelReceiver != null) {
                    // Clear the activity reference, it will be set in the new
                    // SegmentList instance.
                    railCancelReceiver.setActivity(null);
                    retainer.put(EXTRA_RAIL_CANCEL_RECEIVER_KEY, railCancelReceiver);
                }
                if (airCancelReceiver != null) {
                    // Clear the activity reference, it will be set in the new
                    // SegmentList instance.
                    airCancelReceiver.setActivity(null);
                    retainer.put(EXTRA_AIR_CANCEL_RECEIVER_KEY, airCancelReceiver);
                }
                if (railStationReceiver != null) {
                    // Clear the activity reference, it will be set in the new
                    // SegmentList instance.
                    railStationReceiver.setActivity(null);
                    retainer.put(EXTRA_RAIL_STATION_RECEIVER_KEY, railStationReceiver);
                }
                if (longPressSegment != null) {
                    retainer.put(EXTRA_LONG_PRESS_SEGMENT_KEY, longPressSegment);
                }
                if (carSuggestionAdapter != null) {
                    retainer.put(EXTRA_CAR_SUGGESTION_ADAPTER, carSuggestionAdapter);
                }
                if (hotelSuggestionAdapter != null) {
                    retainer.put(EXTRA_HOTEL_SUGGESTION_ADAPTER, hotelSuggestionAdapter);
                }
                if (agencyDetailsReceiver != null) {
                    agencyDetailsReceiver.setListener(null);
                    retainer.put(GET_AGENCY_DETAILS_RECEIVER, agencyDetailsReceiver);
                }
                if (agentPreferredPhoneNumber != null) {
                    retainer.put(EXTRA_AGENT_PREFERRED_PHONE_NUMBER, agentPreferredPhoneNumber);
                    retainer.put(EXTRA_TRIP_RECORD_LOCATOR_FOR_AGENT, tripRecordLocatorForAgent);
                    retainer.put(EXTRA_AGENT_ERROR_MESSAGE, callAgentErrorMessage);
                }
                if (retainer.contains(EXTRA_AFTER_CALLING_AGENT)) {
                    Boolean returnedFromPhoneApp = (Boolean) retainer.get(EXTRA_AFTER_CALLING_AGENT);
                    if (returnedFromPhoneApp) {
                        retainer.put(EXTRA_AFTER_CALLING_AGENT, false);
                        showDialog(DIALOG_AGENT_CHANGED_ITINERARY);
                    }
                }
            }
            if (bookingRecordLocator != null) {
                retainer.put(EXTRA_BOOKING_RECORD_LOCATOR_KEY, bookingRecordLocator);
            }
            if (bookingItineraryLocator != null) {
                retainer.put(EXTRA_BOOKING_ITINERARY_LOCATOR_KEY, bookingItineraryLocator);
            }

            // Save the itinerary list receiver
            if (itinerarySummaryListReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                itinerarySummaryListReceiver.setActivity(null);
                // Store it in the retainer
                retainer.put(ITINERARY_LIST_RECEIVER_KEY, itinerarySummaryListReceiver);
            }
            // Save the itinerary receiver.
            if (itineraryReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                itineraryReceiver.setActivity(null);
                // Store it in the retainer
                retainer.put(ITINERARY_RECEIVER_KEY, itineraryReceiver);
            }
        }
    }

    /**
     * Will launch the car rental search activity passing in arguments based on values in <code>suggestion</code>.
     * 
     * @param suggestion
     *            the suggestion selected by the end-user.
     */
    protected void searchCar(SearchSuggestion suggestion) {
        Intent intent = new Intent(this, CarSearch.class);
        // Add the Cliqbook trip id.
        intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, trip.cliqbookTripId);
        // Add the client locator.
        intent.putExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, trip.clientLocator);
        // Add the record locator.
        intent.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, trip.recordLocator);
        // Add the suggestion location.

        LocationChoice locChoice = suggestion.getStartLocationChoice(getApplicationContext());
        if (locChoice != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_LOCATION, locChoice.getBundle());
        }
        // Add the suggestion pick-up date.
        if (suggestion.getStartDate() != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR, suggestion.getStartDate());
        }
        // Add the suggestion drop-off date.
        if (suggestion.getStopDate() != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR, suggestion.getStopDate());
        }
        intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIP);
        startActivityForResult(intent, Const.REQUEST_CODE_BOOK_CAR);
    }

    /**
     * Will launch the hotel search activity passing in arguments based on values in <code>suggestion</code>.
     * 
     * @param suggestion
     *            the suggestion selected by the end-user.
     */
    protected void searchHotel(SearchSuggestion suggestion) {
        Intent intent = new Intent(this, HotelSearch.class);
        intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
        // Add the Cliqbook trip id.
        intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, trip.cliqbookTripId);
        // Add the suggestion location.
        LocationChoice locChoice = suggestion.getStartLocationChoice(getApplicationContext());
        if (locChoice != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_LOCATION, locChoice.getBundle());
        }
        // Add the suggestion pick-up date.
        if (suggestion.getStartDate() != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN, suggestion.getStartDate());
        }
        // Add the suggestion drop-off date.
        if (suggestion.getStopDate() != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT, suggestion.getStopDate());
        }
        intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIP);
        startActivityForResult(intent, Const.REQUEST_CODE_BOOK_HOTEL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Const.REQUEST_CODE_BOOK_HOTEL: {
            if (resultCode == RESULT_OK) {
                handleBookingResult(data.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR));
            }
            break;
        }
        case Const.REQUEST_CODE_BOOK_CAR: {
            if (resultCode == RESULT_OK) {
                handleBookingResult(data.getStringExtra(Const.EXTRA_TRAVEL_ITINERARY_LOCATOR));
            }
        }
        case Const.REQUEST_CODE_VIEW_SEGMENT_DETAIL: {
            if (resultCode == RESULT_OK) {
                // A result of 'OK' indicates a segment has been canceled. The
                // intent
                // data contains Flurry constants used to generate a Flurry
                // event.
                if (data != null && data.hasExtra(Flurry.PARAM_NAME_TYPE)) {
                    lastCanceledFlurrySegmentParamValue = data.getStringExtra(Flurry.PARAM_NAME_TYPE);
                }
                refreshItinerary(itinLocator);
            }
            break;
        }
        }
    }

    private void handleBookingResult(String itineraryLocator) {
        bookingItineraryLocator = itineraryLocator;

        if (bookingItineraryLocator != null) {
            IItineraryCache itinCache = getConcurCore().getItinCache();
            // NOTE: Even though the existing trip was already local, adding a
            // hotel/car to it
            // can affect the start/end times of the trip. So, the flag to
            // refetch the
            // trip list will be set since the itinerary summary objects may not
            // reflect
            // the same start/end as the detail summary object.
            if (itinCache != null) {
                itinCache.setShouldRefetchSummaryList(true);
                refreshItinerary(bookingItineraryLocator);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleBookingResult: itin cache is null!");
            }
        } else {
            // The booking was added to a new itinerary that has not yet been
            // downloaded.
            // Refetch the itinerary summary list, then fetch the itinerary
            // detail object.
            sendItinerarySummaryListRequest();
        }
    }

    /**
     * Will refresh the itinerary list and receive the response within the itinerary list refresh receiver.
     */
    private void refreshItinerary(String itinLocator) {
        sendItineraryRequest(itinLocator);
    }

    /**
     * Will refresh the itinerary list and receive the response within the itinerary list refresh receiver.
     */
    private void refreshItinerary(String itinLocator, String travellerCompanyId, String travellerUserId,
            boolean isForApprover) {
        sendItineraryRequest(itinLocator, travellerCompanyId, travellerUserId, isForApprover);
    }

    protected void addHotel() {
        ITripAnalyzer tripAnalyzer = Trip.getTripAnalyzer();

        // MOB-13546 - Add hotel to my flight - check for defaults if Hotel is
        // being added to an existing trip with Flight
        // segments and do not show the suggestions pop up
        // TODO - need to fine tune this function when 'Add Hotel' to other
        // segments is addressed similar to MOB-13546
        LodgeSearchSuggestion hotelSearchSuggestionForFlight = tripAnalyzer.findHotelSearchSuggestionForFlight(trip);
        if (hotelSearchSuggestionForFlight != null) {
            // show the search UI with out suggestions pop up
            searchHotel(hotelSearchSuggestionForFlight);
        } else {

            List<SearchSuggestion> searchSuggestions = new ArrayList<SearchSuggestion>();

            // Obtain an initial list of suggestions based on an analysis of the
            // itinerary segments.
            // ITripAnalyzer tripAnalyzer = Trip.getTripAnalyzer();
            List<LodgeSearchSuggestion> lodgeSearchSuggestions = tripAnalyzer.findHotelSuggestions(trip);
            if (lodgeSearchSuggestions != null && lodgeSearchSuggestions.size() > 0) {
                CategorySearchSuggestion cityDateSearchSuggestionCategory = new CategorySearchSuggestion(getText(
                        R.string.city_date_suggestions).toString());
                hotelSuggestionAdapter.setCityDateCategorySuggestion(cityDateSearchSuggestionCategory);
                searchSuggestions.add(cityDateSearchSuggestionCategory);
                searchSuggestions.addAll(lodgeSearchSuggestions);
            } else {
                hotelSuggestionAdapter.setCityDateCategorySuggestion(null);
            }

            // Obtain the list of cities with non-specific dates.
            List<CitySearchSuggestion> citySearchSuggestions = tripAnalyzer.findTripCities(trip, false);
            if (citySearchSuggestions != null && citySearchSuggestions.size() > 0) {
                CategorySearchSuggestion citySearchSuggestionCategory = new CategorySearchSuggestion(getText(
                        R.string.city_suggestions).toString());
                hotelSuggestionAdapter.setCityCategorySuggestion(citySearchSuggestionCategory);
                searchSuggestions.add(citySearchSuggestionCategory);
                searchSuggestions.addAll(citySearchSuggestions);
            } else {
                hotelSuggestionAdapter.setCityCategorySuggestion(null);
            }

            // Add a custom option not based on any analysis of the itinerary.
            searchSuggestions.add(new CategorySearchSuggestion(getText(R.string.general).toString()));
            searchSuggestions.add(new CustomSearchSuggestion(getText(R.string.custom_hotel_search).toString()));

            // Set the new list on the adapter and notify of change.
            hotelSuggestionAdapter.setSuggestions(searchSuggestions);
            hotelSuggestionAdapter.notifyDataSetChanged();

            // Complete the handling of the suggestions.
            handleSearchSuggestions(DIALOG_ADD_HOTEL, searchSuggestions);
        }
    }

    protected void addCar() {
        ITripAnalyzer tripAnalyzer = Trip.getTripAnalyzer();

        // MOB-13547 - Add car to my flight - check for defaults if Car is being
        // added to an existing trip with Flight
        // segments and do not show the suggestions pop up
        // TODO - need to fine tune this function when 'Add Car' to other
        // segments is addressed similar to MOB-13547
        SearchSuggestion carSearchSuggestionForFlight = tripAnalyzer.findCarSearchSuggestionForFlight(trip);
        if (carSearchSuggestionForFlight != null) {
            // show the search UI with out suggestions pop up
            searchCar(carSearchSuggestionForFlight);
        } else {

            List<SearchSuggestion> searchSuggestions = new ArrayList<SearchSuggestion>();

            // Obtain an initial list of suggestions based on an analysis of the
            // itinerary segments.
            List<TransportSearchSuggestion> transportSearchSuggestions = tripAnalyzer.findCarSuggestions(trip);
            if (transportSearchSuggestions != null && transportSearchSuggestions.size() > 0) {
                CategorySearchSuggestion cityDateSearchSuggestionCategory = new CategorySearchSuggestion(getText(
                        R.string.city_date_suggestions).toString());
                carSuggestionAdapter.setCityDateCategorySuggestion(cityDateSearchSuggestionCategory);
                searchSuggestions.add(cityDateSearchSuggestionCategory);
                searchSuggestions.addAll(transportSearchSuggestions);
            } else {
                carSuggestionAdapter.setCityDateCategorySuggestion(null);
            }

            // Obtain the list of cities with non-specific dates.
            List<CitySearchSuggestion> citySearchSuggestions = tripAnalyzer.findTripCities(trip, true);
            if (citySearchSuggestions != null && citySearchSuggestions.size() > 0) {
                CategorySearchSuggestion citySearchSuggestionCategory = new CategorySearchSuggestion(getText(
                        R.string.city_suggestions).toString());
                carSuggestionAdapter.setCityCategorySuggestion(citySearchSuggestionCategory);
                searchSuggestions.add(citySearchSuggestionCategory);
                searchSuggestions.addAll(citySearchSuggestions);
            } else {
                carSuggestionAdapter.setCityCategorySuggestion(null);
            }

            // Add a custom option not based on any analysis of the itinerary.
            searchSuggestions.add(new CategorySearchSuggestion(getText(R.string.general).toString()));
            searchSuggestions.add(new CustomSearchSuggestion(getText(R.string.custom_car_search).toString()));

            // Set the new list on the adapter and notify of change.
            carSuggestionAdapter.setSuggestions(searchSuggestions);
            carSuggestionAdapter.notifyDataSetChanged();

            // Complete the handling of the suggestions.
            handleSearchSuggestions(DIALOG_ADD_CAR, searchSuggestions);
        }
    }

    /**
     * Will handle a list of search suggestions and a dialog that should be displayed.
     * 
     * @param dialog
     *            the dialog to be displayed.
     * @param searchSuggestions
     *            the list of search suggestions.
     */
    private void handleSearchSuggestions(int dialog, List<SearchSuggestion> searchSuggestions) {
        // Iterate through the suggestions and determine whether any of them
        // require rail station information. If so,
        // check locally for rail station information and download from the
        // server if needbe.
        boolean requireRailStationInfo = false;
        for (SearchSuggestion suggestion : searchSuggestions) {
            if (suggestion.requiresRailStations()) {
                requireRailStationInfo = true;
                break;
            }
        }
        // Is rail station info required?
        if (requireRailStationInfo) {
            // Is rail station local?
            ConcurCore app = (ConcurCore) getApplication();
            List<RailStation> railStations = app.getRailStationList();
            if (railStations == null || railStations.size() == 0) {
                railStations = app.getService().getRailStationList();
                if (railStations == null || railStations.size() == 0) {
                    if (ConcurCore.isConnected()) {
                        // No local rail station information, we'll request it
                        // from the server
                        // and present a progress dialog.
                        // Create a receiver and register it to receive result
                        // of rail station request.
                        if (railStationReceiver == null) {
                            railStationReceiver = new RailStationReceiver(this, dialog);
                        }
                        getApplicationContext().registerReceiver(railStationReceiver, railStationFilter);
                        railStationListRequest = app.getService().sendRailStationListRequest(Const.VENDOR_AMTRAK);
                        railStationReceiver.setRequest(railStationListRequest);
                        showDialog(DIALOG_RAIL_STATION_PROGRESS);
                    } else {
                        // Not connected and no local rail station information,
                        // just show the dialog.
                        showDialog(dialog);
                    }
                } else {
                    // Rail station loaded from persistence, just show the
                    // dialog.
                    showDialog(dialog);
                }
            } else {
                // Rail station information is local and in-memory, just show
                // the dialog.
                showDialog(dialog);
            }
        } else {
            // No rail station information required, just show the dialog.
            showDialog(dialog);
        }
    }

    private void callAgent() {
        // remove all special characters and brackets from the
        // agentPreferredPhoneNumber
        String phoneNumberCleaned = PhoneNumberUtils.formatNumber(PhoneNumberUtils
                .stripSeparators(agentPreferredPhoneNumber));

        retainer.put(EXTRA_AFTER_CALLING_AGENT, true);

        // Flurry Notification.
        EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_AGENCY,
                Flurry.EVENT_NAME_PHONED_TRAVEL_AGENT);

        // start the phone dialer
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumberCleaned));

        startActivity(callIntent);
    }

    /**
     * An adapter used to provide a list of search suggestions based on a trip analysis.
     * 
     * @author AndrewK
     */
    private class SuggestionAdapter extends BaseAdapter {

        // Contains the list of search suggestions.
        private List<SearchSuggestion> suggestions;

        private CategorySearchSuggestion cityDateCategorySuggestion;

        private CategorySearchSuggestion cityCategorySuggestion;

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            int count = 0;
            if (suggestions != null) {
                count = suggestions.size();
            }
            return count;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            SearchSuggestion suggestion = null;
            if (suggestions != null && position >= 0) {
                suggestion = suggestions.get(position);
            }
            return suggestion;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.BaseAdapter#isEnabled(int)
         */
        @Override
        public boolean isEnabled(int position) {
            boolean enabled = true;
            if (position >= 0) {
                enabled = !(suggestions.get(position) instanceof CategorySearchSuggestion);
            }
            return enabled;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            LayoutInflater inflater = LayoutInflater.from(SegmentList.this);
            if (!(suggestions.get(position) instanceof CategorySearchSuggestion)) {
                view = inflater.inflate(R.layout.suggestion_row, null);
            } else {
                view = inflater.inflate(R.layout.suggestion_category_row, null);
            }
            TextView txtView = (TextView) view.findViewById(R.id.suggestion);
            SearchSuggestion suggestion = suggestions.get(position);
            txtView.setText(suggestion.getDisplayText((ConcurCore) getApplication()));
            return view;
        }

        /**
         * Sets the list of
         * 
         * @param suggestions
         */
        void setSuggestions(List<SearchSuggestion> suggestions) {
            this.suggestions = suggestions;
        }

        /**
         * Sets the city/date category search suggestion.
         * 
         * @param categorySearchSuggestion
         *            the city/date category search suggestion.
         */
        void setCityDateCategorySuggestion(CategorySearchSuggestion categorySearchSuggestion) {
            cityDateCategorySuggestion = categorySearchSuggestion;
        }

        /**
         * Sets the city category search suggestion.
         * 
         * @param categorySearchSuggestion
         *            the city category search suggestion.
         */
        void setCityCategorySuggestion(CategorySearchSuggestion categorySearchSuggestion) {
            cityCategorySuggestion = categorySearchSuggestion;
        }

        /**
         * Will examine the current set of suggestions and for CitySearchSuggestion instances ensure duplicates don't exist.
         * 
         * NOTE: The need for this method is kind of a hack as the current design first analyzes a trip, then for each city
         * suggestion that requires city, state and country information from a rail station, performs a late resolution of that
         * information. The late resolution could then result in duplicate CitySearchSuggestion objects. So, this method will will
         * determine if these duplicates exists and will resolve them by removing any duplicates.
         */
        void resolveDuplicateCitySearchSuggestions() {
            if (suggestions != null && suggestions.size() > 0) {
                ConcurCore ConcurCore = (ConcurCore) getApplication();
                HashSet<String> citySet = new HashSet<String>();
                ArrayList<SearchSuggestion> resolvedSuggestions = new ArrayList<SearchSuggestion>();
                int citySearchSuggestionCount = 0;
                int cityDateSearchSuggestionCount = 0;
                for (SearchSuggestion suggestion : suggestions) {
                    // We want to limit to instances of CitySearchSuggestion and
                    // not objects in sub-classes of
                    // CitySearchSuggestion.
                    if (suggestion.getClass() == CitySearchSuggestion.class) {
                        CitySearchSuggestion citySearchSuggestion = (CitySearchSuggestion) suggestion;
                        if (citySearchSuggestion.requiresRailStations()) {
                            citySearchSuggestion.getDisplayText(ConcurCore);
                            // Don't add blank city search names.
                            if (citySearchSuggestion.city != null && citySearchSuggestion.city.length() > 0) {
                                if (citySet.add(citySearchSuggestion.city)) {
                                    resolvedSuggestions.add(citySearchSuggestion);
                                    ++citySearchSuggestionCount;
                                }
                            }
                        } else {
                            resolvedSuggestions.add(citySearchSuggestion);
                            ++citySearchSuggestionCount;
                        }
                    } else if (suggestion.getClass() == TransportSearchSuggestion.class) {
                        TransportSearchSuggestion transportSearchSuggestion = (TransportSearchSuggestion) suggestion;
                        if (transportSearchSuggestion.requiresRailStations()) {
                            transportSearchSuggestion.getDisplayText(ConcurCore);
                            if (transportSearchSuggestion.departureCity.city != null
                                    && transportSearchSuggestion.departureCity.city.length() > 0) {
                                resolvedSuggestions.add(transportSearchSuggestion);
                                ++cityDateSearchSuggestionCount;
                            }
                        } else {
                            resolvedSuggestions.add(transportSearchSuggestion);
                            ++cityDateSearchSuggestionCount;
                        }
                    } else if (suggestion.getClass() == LodgeSearchSuggestion.class) {
                        LodgeSearchSuggestion lodgeSearchSuggestion = (LodgeSearchSuggestion) suggestion;
                        if (lodgeSearchSuggestion.requiresRailStations()) {
                            lodgeSearchSuggestion.getDisplayText(ConcurCore);
                            if (lodgeSearchSuggestion.city != null && lodgeSearchSuggestion.city.length() > 0) {
                                resolvedSuggestions.add(lodgeSearchSuggestion);
                                ++cityDateSearchSuggestionCount;
                            }
                        } else {
                            resolvedSuggestions.add(lodgeSearchSuggestion);
                            ++cityDateSearchSuggestionCount;
                        }
                    } else {
                        resolvedSuggestions.add(suggestion);
                    }
                }
                // If city/date or city suggestion counts are zero, then remove
                // their
                // category headings.
                if (cityDateSearchSuggestionCount == 0 && cityDateCategorySuggestion != null) {
                    resolvedSuggestions.remove(cityDateCategorySuggestion);
                }
                if (citySearchSuggestionCount == 0 && cityCategorySuggestion != null) {
                    resolvedSuggestions.remove(cityCategorySuggestion);
                }
                // Reset the list of suggestions and notify any listeners.
                suggestions = resolvedSuggestions;
                notifyDataSetChanged();
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of canceling a car reservation.
     */
    static class CarCancelReceiver extends BroadcastReceiver {

        // A reference to the segment list activity.
        private SegmentList activity;

        // A reference to the car cancel request.
        private CancelCarRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>CarCancelReceiver</code> with an associated activity.
         * 
         * @param activity
         *            the associated activity.
         */
        CarCancelReceiver(SegmentList activity) {
            this.activity = activity;
        }

        /**
         * Sets the segment list activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the segment list activity associated with this broadcast receiver.
         */
        void setActivity(SegmentList activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.cancelCarRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the car cancel request object associated with this broadcast receiver.
         * 
         * @param request
         *            the car cancel request object associated with this broadcast receiver.
         */
        void setRequest(CancelCarRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister this receiver.
                activity.getApplicationContext().unregisterReceiver(this);
                // Sever the reference to this receiver.
                activity.carCancelReceiver = null;
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    try {
                                        activity.dismissDialog(DIALOG_CANCEL_CAR_PROGRESS);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                    activity.lastCanceledFlurrySegmentParamValue = Flurry.PARAM_VALUE_CAR;
                                    activity.showDialog(DIALOG_CANCEL_CAR_SUCCESS);
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");
                                    activity.showDialog(DIALOG_CANCEL_CAR_FAIL);
                                    try {
                                        activity.dismissDialog(DIALOG_CANCEL_CAR_PROGRESS);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                try {
                                    activity.dismissDialog(DIALOG_CANCEL_CAR_PROGRESS);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            try {
                                activity.dismissDialog(DIALOG_CANCEL_CAR_PROGRESS);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    try {
                        activity.dismissDialog(DIALOG_CANCEL_CAR_PROGRESS);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Reset the cancel request object.
                activity.cancelCarRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of canceling a rail reservation.
     */
    static class RailCancelReceiver extends BroadcastReceiver {

        // A reference to the segment list activity.
        private SegmentList activity;

        // A reference to the rail cancel request.
        private CancelRailRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>RailCancelReceiver</code> with an associated activity.
         * 
         * @param activity
         *            the associated activity.
         */
        RailCancelReceiver(SegmentList activity) {
            this.activity = activity;
        }

        /**
         * Sets the segment list activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the segment list activity associated with this broadcast receiver.
         */
        void setActivity(SegmentList activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.cancelRailRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the rail cancel request object associated with this broadcast receiver.
         * 
         * @param request
         *            the rail cancel request object associated with this broadcast receiver.
         */
        void setRequest(CancelRailRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister this receiver.
                activity.getApplicationContext().unregisterReceiver(this);
                // Sever the reference to this receiver.
                activity.railCancelReceiver = null;
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    try {
                                        activity.dismissDialog(DIALOG_CANCEL_RAIL_PROGRESS);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                    activity.showDialog(DIALOG_CANCEL_RAIL_SUCCESS);
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");
                                    activity.showDialog(DIALOG_CANCEL_RAIL_FAIL);
                                    try {
                                        activity.dismissDialog(DIALOG_CANCEL_RAIL_PROGRESS);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                try {
                                    activity.dismissDialog(DIALOG_CANCEL_RAIL_PROGRESS);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            try {
                                activity.dismissDialog(DIALOG_CANCEL_RAIL_PROGRESS);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    try {
                        activity.dismissDialog(DIALOG_CANCEL_RAIL_PROGRESS);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Reset the cancel request object.
                activity.cancelRailRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of canceling an air reservation.
     */
    static class AirCancelReceiver extends BroadcastReceiver {

        // A reference to the segment list activity.
        private SegmentList activity;

        // A reference to the air cancel request.
        private AirCancelRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>AirCancelReceiver</code> with an associated activity.
         * 
         * @param activity
         *            the associated activity.
         */
        AirCancelReceiver(SegmentList activity) {
            this.activity = activity;
        }

        /**
         * Sets the segment list activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the segment list activity associated with this broadcast receiver.
         */
        void setActivity(SegmentList activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.cancelAirRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the car cancel request object associated with this broadcast receiver.
         * 
         * @param request
         *            the car cancel request object associated with this broadcast receiver.
         */
        void setRequest(AirCancelRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister this receiver.
                activity.getApplicationContext().unregisterReceiver(this);
                // Sever the reference to this receiver.
                activity.airCancelReceiver = null;
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)
                                        || i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_OK)) {
                                    try {
                                        activity.dismissDialog(DIALOG_CANCEL_AIR_PROGRESS);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                    activity.lastCanceledFlurrySegmentParamValue = Flurry.PARAM_VALUE_AIR;
                                    activity.showDialog(DIALOG_CANCEL_AIR_SUCCESS);
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");
                                    activity.showDialog(DIALOG_CANCEL_AIR_FAIL);
                                    try {
                                        activity.dismissDialog(DIALOG_CANCEL_AIR_PROGRESS);
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                try {
                                    activity.dismissDialog(DIALOG_CANCEL_AIR_PROGRESS);
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            try {
                                activity.dismissDialog(DIALOG_CANCEL_AIR_PROGRESS);
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    try {
                        activity.dismissDialog(DIALOG_CANCEL_AIR_PROGRESS);
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Reset the cancel request object.
                activity.cancelAirRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }

    }

    /**
     * Will send a request to obtain an itinerary list.
     */
    private void sendItinerarySummaryListRequest() {
        ConcurService concurService = getConcurService();
        registerItinerarySummaryListReceiver();
        itinerarySummaryListRequest = concurService.sendItinerarySummaryListRequest(false);
        if (itinerarySummaryListRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".sendItinerarySummaryListRequest: unable to create summary itinerary list request.");
            unregisterItinerarySummaryListReceiver();
        } else {
            // Set the request object on the receiver.
            itinerarySummaryListReceiver.setServiceRequest(itinerarySummaryListRequest);
            // Show the retrieving itineraries dialog.
            showDialog(DIALOG_ITINERARY_LIST_REFRESH);
        }
    }

    /**
     * Will register an itinerary summary list receiver.
     */
    private void registerItinerarySummaryListReceiver() {
        if (itinerarySummaryListReceiver == null) {
            itinerarySummaryListReceiver = new ItineraryListReceiver(this);
            if (itinerarySummaryListFilter == null) {
                itinerarySummaryListFilter = new IntentFilter(Const.ACTION_SUMMARY_TRIPS_UPDATED);
            }
            getApplicationContext().registerReceiver(itinerarySummaryListReceiver, itinerarySummaryListFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerItinerarySummaryListReceiver: itinerarySummaryListReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an itinerary summary list receiver.
     */
    private void unregisterItinerarySummaryListReceiver() {
        if (itinerarySummaryListReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(itinerarySummaryListReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItinerarySummaryListReceiver: illegal argument", ilaExc);
            }
            itinerarySummaryListReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterItinerarySummaryListReceiver: itinerarySummaryListReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    static class ItineraryListReceiver extends BaseBroadcastReceiver<SegmentList, ItinerarySummaryListRequest> {

        /**
         * Constructs an instance of <code>ItineraryListReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryListReceiver(SegmentList activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(SegmentList activity) {
            activity.itinerarySummaryListRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_ITINERARY_LIST_REFRESH);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_ITINERARY_LIST_REFRESH_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (activity.bookingRecordLocator != null) {
                // Locate the itinerary locator based on the record locator for
                // 'bookingRecordLocator', then
                // fetch the detailed itinerary.
                IItineraryCache itinCache = activity.getConcurCore().getItinCache();
                if (itinCache != null) {
                    // Set the flag indicating the trip list should be
                    // refreshed.
                    itinCache.setShouldRefreshSummaryList(true);
                    Trip bookTrip = itinCache.getItinerarySummaryByBookingRecordLocator(activity.bookingRecordLocator);
                    if (bookTrip != null) {
                        activity.sendItineraryRequest(bookTrip.itinLocator);
                        // get latest total trip travel points
                        activity.totalTravelPoints = (bookTrip.itinTravelPoint == null ? "" : bookTrip.itinTravelPoint
                                .getPointsPosted());
                    } else {
                        Log.i(Const.LOG_TAG,
                                CLS_TAG
                                        + ".ItineraryListReceiver.handleSuccess: unable to locate new itinerary by booking record locator "
                                        + "in itinerary summary list! Goign to trip list!");
                        // Unable to locate the itinerary summary object based
                        // on the booking record locator, so
                        // let's just send the end-user back to the trip list.
                        // NOTE: 1/18/2012: Once the 'GetItinerariesV2'
                        // end-point has been updated to return a list
                        // of booking record locators, the above call
                        // 'getItinerarySummaryByBookingRecordLocator'
                        // should be able to locate the itinerary that includes
                        // 'activity.bookingRecordLocator'.
                        activity.finish();
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".ItineraryListReceiver.handleSuccess: itin cache is null!");
                }
                // Reset booking record locator.
                activity.bookingRecordLocator = null;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItinerarySummaryListRequest request) {
            activity.itinerarySummaryListRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItinerarySummaryListReceiver();
        }

    }

    /**
     * Will send a request to obtain an itinerary.
     */
    public void sendItineraryRequest(String itinLocator) {
        sendItineraryRequest(itinLocator, null,
                PreferenceManager.getDefaultSharedPreferences(this).getString(Const.PREF_USER_ID, null), false);
    }

    /**
     * Will send a request to obtain an itinerary of the specific traveler.
     */
    private void sendItineraryRequest(String itinLocator, String travellerCompanyId, String travellerUserId,
            boolean isForApprover) {
        ConcurService concurService = getConcurService();
        registerItineraryReceiver();
        itineraryRequest = concurService.sendItineraryRequest(itinLocator, travellerCompanyId, travellerUserId,
                isForApprover);
        if (itineraryRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendItineraryRequest: unable to create itinerary request.");
            unregisterItineraryReceiver();
        } else {
            // Set the request object on the receiver.
            itineraryReceiver.setServiceRequest(itineraryRequest);
            // Show the dialog.
            showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }
    }

    /**
     * Will register an itinerary receiver.
     */
    private void registerItineraryReceiver() {
        if (itineraryReceiver == null) {
            itineraryReceiver = new ItineraryReceiver(this);
            if (itineraryFilter == null) {
                itineraryFilter = new IntentFilter(Const.ACTION_TRIP_UPDATED);
            }
            getApplicationContext().registerReceiver(itineraryReceiver, itineraryFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerItineraryReceiver: itineraryReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an itinerary receiver.
     */
    private void unregisterItineraryReceiver() {
        if (itineraryReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(itineraryReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItinerarySReceiver: illegal argument", ilaExc);
            }
            itineraryReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterItineraryReceiver: itineraryReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of refreshing an itinerary.
     */
    static class ItineraryReceiver extends BaseBroadcastReceiver<SegmentList, ItineraryRequest> {

        /**
         * Constructs an instance of <code>ItineraryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryReceiver(SegmentList activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(SegmentList activity) {
            activity.itineraryRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (intent.hasExtra(Const.EXTRA_ITIN_LOCATOR)) {
                String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
                if (itinLocator != null) {
                    IItineraryCache itinCache = activity.getConcurCore().getItinCache();
                    if (itinCache != null) {
                        // Reset both the trip and itinLocator objects, then
                        // refresh the display.
                        activity.trip = itinCache.getItinerary(itinLocator);
                        activity.itinLocator = itinLocator;
                        activity.configureSegmentList();
                        activity.bookingRecordLocator = null;
                        activity.bookingItineraryLocator = null;
                        int itinSegmentCount = 0;
                        if (activity.trip != null) {
                            itinSegmentCount = activity.trip.getSegmentCount();
                        }
                        // Flurry Notification.
                        if (activity.lastCanceledFlurrySegmentParamValue != null) {
                            // Generate the notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_TYPE, activity.lastCanceledFlurrySegmentParamValue);
                            params.put(Flurry.PARAM_NAME_ITEMS_LEFT_IN_ITIN, Integer.toString(itinSegmentCount));
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_CANCEL, params);
                            // Clear the last canceled Flurry segment param
                            // value.
                            activity.lastCanceledFlurrySegmentParamValue = null;
                        }
                        // If 'itinSegmentCount' has dropped to zero, then set
                        // the flag to refresh the trip summary
                        // list.
                        if (itinSegmentCount == 0) {
                            itinCache.setShouldRefetchSummaryList(true);
                        }
                        activity.initTotalTravelPoints();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".ItineraryReceiver.handleSuccess: itin cache is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator has invalid value!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator missing!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ItineraryRequest request) {
            activity.itineraryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterItineraryReceiver();
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of retrieving a station list.
     * 
     * @author AndrewK
     */
    static class RailStationReceiver extends BroadcastReceiver {

        // A reference to the hotel search activity.
        private SegmentList activity;

        // Contains the request for which this receiver is waiting on a
        // response.
        RailStationListRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        // Contains an integer code for a dialog to be displayed once this
        // receiver
        // has received a response from the server.
        private final int dialog;

        /**
         * Constructs an instance of <code>RailStationReceiver</code> with an associated activity.
         * 
         * @param activity
         *            the associated activity.
         * @param dialog
         *            the dialog that should be displayed once the result is processed.
         */
        RailStationReceiver(SegmentList activity, int dialog) {
            this.activity = activity;
            this.dialog = dialog;
        }

        /**
         * Sets the segment list activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the segment list activity associated with this broadcast receiver.
         */
        void setActivity(SegmentList activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.railStationListRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the rail station list request object associated with this broadcast receiver.
         * 
         * @param request
         *            the rail station list request object associated with this broadcast receiver.
         */
        void setRequest(RailStationListRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent i) {

            // Does this receiver have a current activity?
            if (activity != null) {
                // Unregister this receiver.
                activity.getApplicationContext().unregisterReceiver(this);
                // Sever the reference to this receiver.
                activity.railStationReceiver = null;
                int serviceRequestStatus = i.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = i.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (i.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                    activity.dismissDialog(DIALOG_RAIL_STATION_PROGRESS);
                                    activity.showDialog(dialog);
                                } else {
                                    activity.actionStatusErrorMessage = i.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage + ".");
                                    activity.showDialog(dialog);
                                    activity.dismissDialog(DIALOG_RAIL_STATION_PROGRESS);
                                }
                            } else {
                                activity.lastHttpErrorMessage = i.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage + ".");
                                activity.showDialog(dialog);
                                activity.dismissDialog(DIALOG_RAIL_STATION_PROGRESS);
                            }
                        } else {
                            activity.showDialog(dialog);
                            activity.dismissDialog(DIALOG_RAIL_STATION_PROGRESS);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.showDialog(dialog);
                            activity.dismissDialog(DIALOG_RAIL_STATION_PROGRESS);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + i.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    activity.showDialog(dialog);
                    activity.dismissDialog(DIALOG_RAIL_STATION_PROGRESS);
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
                // Reset the search request object.
                activity.railStationListRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = i;
            }
        }
    }

    private class ApproveTripListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            dismissDialog(DIALOG_TRIP_APPROVAL_PROGRESS);
            // check if MWS end point returned error
            boolean isSuccess = resultData.getBoolean(TripApproval.IS_SUCCESS);
            if (!isSuccess) {
                Log.e(Const.LOG_TAG,
                        "ApproveTripListener.onRequestSuccess - " + resultData.getString(TripApproval.ERROR_MESSAGE));
                // show the retry message
                showDialog(DIALOG_TRIP_APPROVAL_ACTION_FAILURE);
            } else {
                showDialog(DIALOG_TRIP_APPROVAL_ACTION_SUCCESS);
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, "ApproveTripListener.onRequestFail");
            dismissDialog(DIALOG_TRIP_APPROVAL_PROGRESS);

            // show the retry message
            showDialog(DIALOG_TRIP_APPROVAL_ACTION_FAILURE);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            dismissDialog(DIALOG_TRIP_APPROVAL_PROGRESS);
        }

        @Override
        public void cleanup() {
            tripToApproveReceiver = null;
        }

    }

    /**
     * Listener used for displaying the agent phone number dialog
     */
    private class AgencyDetailsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            boolean isSuccess = resultData.getBoolean(GetAgencyDetails.GET_AGENCY_DETAILS_IS_SUCCESS);
            callAgentErrorMessage = resultData.getString(GetAgencyDetails.GET_AGENCY_DETAILS_RESPONSE_MESSAGE);

            Log.e(Const.LOG_TAG, "AgencyDetailsListener.onRequestSuccess - " + callAgentErrorMessage);

            if (isSuccess) {

                // MOB-14280 - ItinLocator changes when passive segments are
                // created,so get the new ItinLocator and use that
                String itinLocatorFromAgencyCall = resultData.getString(Const.EXTRA_ITIN_LOCATOR);
                if (itinLocatorFromAgencyCall != null && itinLocatorFromAgencyCall.trim().length() > 0) {
                    itinLocator = itinLocatorFromAgencyCall;
                } else {
                    Log.d(Const.LOG_TAG, "AgencyDetailsListener.onRequestSuccess - itinLocatorFromAgencyCall is null");
                }

                agentPreferredPhoneNumber = resultData.getString(GetAgencyDetails.AGENT_PREFERRED_PHONE_NUMBER);
                String tripRecLocForAgent = resultData.getString(GetAgencyDetails.TRIP_RECORD_LOCATOR_FOR_AGENT);
                dismissDialog(DIALOG_GET_AGENCY_ASSISTANCE_PROGRESS);

                // MOB-MOB-14267 - show the tip record locator if it is
                // different form previous one or previous is null (will be
                // null if it is
                // open booking)
                if (tripRecordLocatorForAgent == null
                        || (!tripRecordLocatorForAgent.equalsIgnoreCase(tripRecLocForAgent))) {
                    tripRecordLocatorForAgent = tripRecLocForAgent;
                    initAgencyRecordLocator();
                }

                // show dialog with preferred phone number and trip record
                // locator
                showDialog(DIALOG_PROMPT_TO_CALL_AGENT);
            } else {
                // show the error message while retrieving the agency details
                showDialog(DIALOG_AGENCY_ASSISTANCE_FAIL);
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestFail in AgencyDetailsListener...");
            dismissDialog(DIALOG_GET_AGENCY_ASSISTANCE_PROGRESS);
            showDialog(DIALOG_AGENCY_ASSISTANCE_FAIL);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestCancel in AgencyDetailsListener...");
            dismissDialog(DIALOG_GET_AGENCY_ASSISTANCE_PROGRESS);
        }

        @Override
        public void cleanup() {
            agencyDetailsReceiver = null;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Notify the Application that this activity is recreated so attach this new activity reference to all Async Tasks that
        // are running (started) by this activity
        ConcurCore concurCoreApp = (ConcurCore) getApplication();
        concurCoreApp.attach(this);
    }

}
