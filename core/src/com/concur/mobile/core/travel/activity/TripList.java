package com.concur.mobile.core.travel.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.travel.service.ItineraryRequest;
import com.concur.mobile.core.travel.service.ItinerarySummaryListRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FeedbackManager;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;

// TODO: This class needs to be enhanced to deal with the:
// 1. UI should show "restoring application state" message when building of the view
// is delayed.
// 2. Handle orientation change w/no re-fetching of data.
// 3. Show a no trips message or other messages when fetching data.
public class TripList extends BaseActivity {

    private static final String CLS_TAG = TripList.class.getSimpleName();
    private static final String ITINERARY_LIST_RECEIVER_KEY = "itinerary.list.receiver";
    private static final String ITINERARY_RECEIVER_KEY = "itinerary.receiver";
    protected final IntentFilter filter = new IntentFilter(Const.ACTION_TRIPS_UPDATED);
    // Contains a reference to the trip list adapter.
    protected TripListAdapter tripListAdapter;
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
    // Contains the last action status error message from the server.
    protected String actionStatusErrorMessage;
    // Contains the last http error message.
    protected String lastHttpErrorMessage;
    // Contains whether the building of the view is currently delayed.
    protected boolean buildViewDelayed = false;
    protected boolean resumeRefreshDelayed = false;
    private static int VIEW_SEGMENT_LIST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Construct and populate map from view state to child index.
        viewStateFlipChild = new HashMap<ViewState, Integer>();
        viewStateFlipChild.put(ViewState.LOCAL_DATA, 0);
        viewStateFlipChild.put(ViewState.NO_DATA, 2);
        viewStateFlipChild.put(ViewState.RESTORE_APP_STATE, 1);
        // The last two states here map to the same view.
        viewStateFlipChild.put(ViewState.NO_LOCAL_DATA_REFRESH, 3);
        viewStateFlipChild.put(ViewState.LOCAL_DATA_REFRESH, 3);
        // Init to local data.
        viewState = ViewState.LOCAL_DATA;
        setContentView(R.layout.triplist);
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        if (viewFlipper != null) {
            // Animation anim = AnimationUtils.loadAnimation(this,
            // R.anim.fade_out);
            // anim.setDuration(400L);
            // viewFlipper.setOutAnimation(anim);
        }

        // Set the title header.
        getSupportActionBar().setTitle(R.string.triplist_title);

        if (isServiceAvailable()) {
            buildUI();
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: service unavailable, delayed building view.");
            buildViewDelayed = true;
            viewState = ViewState.RESTORE_APP_STATE;
            flipViewForViewState();
        }
        // Restore any receivers.
        restoreReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
        if (buildViewDelayed || !isServiceAvailable()) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onResume: service unavailable, delayed refresh.");
            resumeRefreshDelayed = true;
        } else {
            if (!checkForRefetchData(false)) {
                checkForRefreshData();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == VIEW_SEGMENT_LIST) {
                FeedbackManager.with(this).showRatingsPrompt();
            }
        }
        super.onActivityResult(requestCode, requestCode, data);
    }

    protected int getDataLoadingTextResourceId() {
        int retVal = getRetrievingDataTextResourceId();
        IItineraryCache itinCache = getConcurCore().getItinCache();
        if (itinCache != null) {
            Calendar itinSummaryListUpdateTime = getConcurCore().getItinCache().getItinerarySummaryListUpdateTime();
            if (itinSummaryListUpdateTime != null) {
                retVal = getUpdatingDataTextResourceId();
            }
        }
        return retVal;
    }

    protected int getRetrievingDataTextResourceId() {
        return R.string.retrieving_trips;
    }

    protected int getUpdatingDataTextResourceId() {
        return R.string.updating_trips;
    }

    protected void restoreReceivers() {
        // Restore any retained data
        if (retainer.contains(ITINERARY_LIST_RECEIVER_KEY)) {
            itinerarySummaryListReceiver = (ItineraryListReceiver) retainer.get(ITINERARY_LIST_RECEIVER_KEY);
            itinerarySummaryListReceiver.setActivity(this);
        }
        if (retainer.contains(ITINERARY_RECEIVER_KEY)) {
            itineraryReceiver = (ItineraryReceiver) retainer.get(ITINERARY_RECEIVER_KEY);
            itineraryReceiver.setActivity(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the itinerary list receiver
        if (itinerarySummaryListReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            itinerarySummaryListReceiver.setActivity(null);
            // Store it in the retainer
            retainer.put(ITINERARY_LIST_RECEIVER_KEY, itinerarySummaryListReceiver);
        }
        // Save the itinerary receiver.
        if (itineraryReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            itineraryReceiver.setActivity(null);
            // Store it in the retainer
            retainer.put(ITINERARY_RECEIVER_KEY, itineraryReceiver);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and stop any outstanding
        // request
        // for a trip list update.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (itinerarySummaryListRequest != null) {
                itinerarySummaryListRequest.cancel();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_add, menu);

        // Enable the Add button only if Travel Booking is allowed and user has
        // TravelUser role.
        MenuItem menuItem = menu.findItem(R.id.menuAdd);
        boolean isTravelUser = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.PREF_CAN_TRAVEL,
                false);
        if (isTravelUser && Preferences.shouldAllowTravelBooking()) {
            menuItem.setEnabled(true);
            menuItem.setVisible(true);
        } else {
            menuItem.setEnabled(false);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int itemId = item.getItemId();
        if (R.id.menuAdd == itemId) {
            // MOB-11304
            if (!ConcurCore.isConnected()) {
                showDialog(Const.DIALOG_NO_CONNECTIVITY);
            } else {
                View v = findViewById(R.id.menuAdd);
                registerForContextMenu(v);
                openContextMenu(v);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = super.onCreateDialog(id);
        switch (id) {
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY: {
            dialog.setOnCancelListener(new OnCancelListener() {

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
        default:
            dialog = super.onCreateDialog(id);
            break;
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        default: {
            super.onPrepareDialog(id, dialog);
            break;
        }
        }
    }

    private void initFooter() {
        View v = findViewById(R.id.footer);
        Button agencyInfo = (Button) v.findViewById(R.id.left_button);
        agencyInfo.setText(R.string.travel_agency_info_title);
        agencyInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ConcurCore.isConnected()) {
                    Intent agencyActivity = new Intent(TripList.this, AgencyInformation.class);
                    startActivity(agencyActivity);
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            }
        });
    }

    private void removeFooter() {
        View v = findViewById(R.id.footer);
        v.setVisibility(View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        final int id = v.getId();
        if (id == R.id.menuAdd) {

            BookTravelDialogFragment dialogFragment = new BookTravelDialogFragment();
            (dialogFragment).show(getSupportFragmentManager(), null);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onServiceAvailable()
     */
    @Override
    protected void onServiceAvailable() {
        if (buildViewDelayed) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: building of view was delayed, building now.");
            buildUI();
            buildViewDelayed = false;
        }
        if (resumeRefreshDelayed) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: resume refresh was delayed, refreshing now.");
            if (!checkForRefetchData(false)) {
                checkForRefreshData();
            }
            resumeRefreshDelayed = false;
        }
    }

    /**
     * Will determine whether the current UI should be refreshed.
     */
    private boolean checkForRefreshData() {
        boolean retVal = false;
        IItineraryCache itinCache = getConcurCore().getItinCache();
        if (itinCache != null) {
            if (itinCache.shouldRefreshSummaryList()) {
                updateTripUI();
                retVal = true;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".checkForRefreshData: itin cache is null!");
        }
        return retVal;
    }

    /**
     * Will initiate refreshing the data if needed.
     * 
     * @param showNoConnectivityDialog
     *            whether to show the "no connectivity" dialog if the client is not connected.
     */
    private boolean checkForRefetchData(boolean showNoConnectivityDialog) {
        boolean retVal = false;
        IItineraryCache itinCache = getConcurCore().getItinCache();
        if (itinCache != null) {
            Calendar itinSummaryListUpdateTime = getConcurCore().getItinCache().getItinerarySummaryListUpdateTime();
            if (itinSummaryListUpdateTime == null || itinCache.shouldRefetchSummaryList()) {
                if (ConcurCore.isConnected()) {
                    if (itinerarySummaryListRequest == null) {
                        sendItinerarySummaryListRequest();
                    }
                    retVal = true;
                } else {
                    // If there is no cached data, then present a dialog
                    // indicating the
                    // client
                    // is offline.
                    if (itinSummaryListUpdateTime == null && showNoConnectivityDialog) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".checkForRefetchData: itin cache is null!");
        }
        return retVal;
    }

    private void buildUI() {
        
        // Set Trip List Adapter
        setTripListAdapter();

        // Update the trip UI.
        updateTripUI();
        // Check for an orientation change.
        if (!orientationChange) {
            checkForRefetchData(true);
        } else {
            // Clear the orientation change flag.
            orientationChange = false;
        }
        // Check if we need to prompt to rate
        boolean shouldCheck = getIntent().getBooleanExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, false);
        if (shouldCheck) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext());
            if (Preferences.shouldPromptToRate(prefs, false)) {
                showDialog(Const.DIALOG_PROMPT_TO_RATE);
            }
        }

        // check if Travel Agency button need to be shown
        if (getIntent().getBooleanExtra(Const.EXTRA_SHOW_TRAVEL_AGENCY_BUTTON, true)) {
            // set footer.
            initFooter();
        } else {
            removeFooter();
        }
    }

    /**
     * set trip list adapter
     * */
    protected void setTripListAdapter() {
        tripListAdapter = new TripListAdapter(this, null);
        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            listView.setAdapter(tripListAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Don't do anything for headers
                    ListItem item = tripListAdapter.getItem(position);
                    String itinLocator = ((TripListItem) item).trip.itinLocator;
                    if (itinLocator != null) {
                        IItineraryCache itinCache = getConcurCore().getItinCache();
                        if (itinCache != null) {
                            Trip itin = itinCache.getItinerary(itinLocator);
                            if (itin != null) {
                                Intent i = new Intent(TripList.this, SegmentList.class);
                                i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                                startActivityForResult(i, VIEW_SEGMENT_LIST);
                            } else {
                                sendItineraryRequest(itinLocator);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".buildUI.onItemClick: itin cache is null!");
                        }
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildUI: unable to locate ListView view in layout!");
        }
        return;
    }

    private void updateTripUI() {
        IItineraryCache itinCache = getConcurCore().getItinCache();
        if (itinCache != null) {
            List<Trip> trips = itinCache.getItinerarySummaryList();
            if (trips != null && trips.size() > 0) {
                tripListAdapter.setTrips(trips);
                tripListAdapter.notifyDataSetChanged();
                getConcurCore().updateLastUpdateText(this, itinCache.getItinerarySummaryListUpdateTime());
                // Clear the refresh trip list flag.
                itinCache.setShouldRefreshSummaryList(false);
                viewState = ViewState.LOCAL_DATA;
            } else {
                viewState = ViewState.NO_DATA;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateTripUI: itin cache is null!");
        }
        flipViewForViewState();
    }

    @Override
    protected int getNoDataTextResourceId() {
        return R.string.triplist_no_data;
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
            ViewUtil.setTextViewText(this, R.id.loading_data, R.id.data_loading_text,
                    getText(getDataLoadingTextResourceId()).toString(), true);
            // Set the view state to indicate data being loaded.
            viewState = ViewState.LOCAL_DATA_REFRESH;
            flipViewForViewState();
            // Set the request object on the receiver.
            itinerarySummaryListReceiver.setServiceRequest(itinerarySummaryListRequest);
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
     * Will send a request to obtain an itinerary.
     */
    protected void sendItineraryRequest(String itinLocator) {
        // @REF: MOB-11304
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerItineraryReceiver();
            itineraryRequest = concurService.sendItineraryRequest(itinLocator);
            if (itineraryRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendItineraryRequest: unable to create itinerary request.");
                unregisterItineraryReceiver();
            } else {
                // Set the request object on the receiver.
                itineraryReceiver.setServiceRequest(itineraryRequest);
                // Show the dialog.
                showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY);
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
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

    protected class TripListAdapter extends BaseAdapter {

        private static final int HEADER_VIEW_TYPE = 0;
        private static final int TRIP_VIEW_TYPE = 1;
        private final Context context;
        private final List<ListItem> tripItems = new ArrayList<ListItem>();

        public TripListAdapter(Context context, List<Trip> trips) {
            this.context = context;
            setTrips(trips);
        }

        @Override
        public int getCount() {
            return tripItems.size();
        }

        @Override
        public ListItem getItem(int position) {
            return tripItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
            // ListItem item = tripItems.get(position);
            // if (HEADER_VIEW_TYPE == item.getListItemViewType()) {
            // return null;
            // } else {
            // return ((TripListItem) item).trip.itinLocator;
            // }
        }

        @Override
        public boolean isEnabled(int position) {
            return tripItems.get(position).isEnabled();
        }

        void setTrips(List<Trip> trips) {
            tripItems.clear();
            // Build the trips list into a new list with trips organized
            if (trips != null && trips.size() > 0) {
                ArrayList<ListItem> activeTrips = new ArrayList<ListItem>();
                ArrayList<ListItem> upcomingTrips = new ArrayList<ListItem>();
                ArrayList<ListItem> pastTrips = new ArrayList<ListItem>();
                ArrayList<ListItem> awaitingApprovalTrips = new ArrayList<ListItem>();
                ArrayList<ListItem> rejectedTrips = new ArrayList<ListItem>();
                // Sort the trips into past, active and upcoming
                Calendar curTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                Iterator<Trip> tripIter = trips.iterator();
                while (tripIter.hasNext()) {
                    Trip trip = tripIter.next();
                    if (trip.approvalStatusEnum == Trip.ApprovalStatusEnum.AwaitingApproval) {
                        awaitingApprovalTrips.add(new TripListItem(trip, TRIP_VIEW_TYPE));
                    } else if (trip.approvalStatusEnum == Trip.ApprovalStatusEnum.RejectedCantOverride
                            || trip.approvalStatusEnum == Trip.ApprovalStatusEnum.RejectedOverridable
                            || trip.approvalStatusEnum == Trip.ApprovalStatusEnum.RejectedAndClosed
                            || trip.approvalStatusEnum == Trip.ApprovalStatusEnum.Withdrawn) {
                        rejectedTrips.add(new TripListItem(trip, TRIP_VIEW_TYPE));
                    } else {
                        // See if the trip ended in the past
                        if (trip.endUtc != null && trip.endUtc.before(curTime)) {
                            pastTrips.add(new TripListItem(trip, TRIP_VIEW_TYPE));
                        } else {
                            // The trip ends in the future...
                            if (trip.startUtc != null && trip.startUtc.before(curTime)) {
                                // If the start was in the past that makes it
                                // active.
                                // Active trip
                                activeTrips.add(new TripListItem(trip, TRIP_VIEW_TYPE));
                            } else {
                                // Must be in the future. Add it to upcoming.
                                upcomingTrips.add(new TripListItem(trip, TRIP_VIEW_TYPE));
                            }
                        }
                    }
                }
                // Glue all the lists back together in our display order
                if (activeTrips.size() > 0) {
                    tripItems.add(new HeaderListItem(getText(R.string.triplist_section_active).toString(),
                            HEADER_VIEW_TYPE));
                    tripItems.addAll(activeTrips);
                }
                if (rejectedTrips.size() > 0) {
                    tripItems.add(new HeaderListItem(getText(R.string.triplist_section_rejected).toString(),
                            HEADER_VIEW_TYPE));
                    tripItems.addAll(rejectedTrips);
                }
                if (awaitingApprovalTrips.size() > 0) {
                    tripItems.add(new HeaderListItem(getText(R.string.triplist_section_awaiting_approval).toString(),
                            HEADER_VIEW_TYPE));
                    tripItems.addAll(awaitingApprovalTrips);
                }
                if (upcomingTrips.size() > 0) {
                    tripItems.add(new HeaderListItem(getText(R.string.triplist_section_upcoming).toString(),
                            HEADER_VIEW_TYPE));
                    tripItems.addAll(upcomingTrips);
                }
                if (pastTrips.size() > 0) {
                    tripItems.add(new HeaderListItem(getText(R.string.triplist_section_past).toString(),
                            HEADER_VIEW_TYPE));
                    tripItems.addAll(pastTrips);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItem item = tripItems.get(position);
            return item.buildView(context, convertView, parent);
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    protected class ItineraryReceiver extends BaseBroadcastReceiver<TripList, ItineraryRequest> {

        /**
         * Constructs an instance of <code>ItineraryReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryReceiver(TripList activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(TripList activity) {
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
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            onHandleSuccessItinerary(intent, activity);
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
     * onHandleSuccessItinerary
     * 
     * @param activity
     * @param intent
     * */
    protected void onHandleSuccessItinerary(Intent intent, TripList activity) {
        if (intent.hasExtra(Const.EXTRA_ITIN_LOCATOR)) {
            String itinLocator = intent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
            if (itinLocator != null) {
                Intent i = new Intent(activity, SegmentList.class);
                i.putExtra(Const.EXTRA_ITIN_LOCATOR, itinLocator);
                activity.startActivityForResult(i, VIEW_SEGMENT_LIST);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator has invalid value!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: itin locator missing!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of an itinerary list request.
     */
    static class ItineraryListReceiver extends BaseBroadcastReceiver<TripList, ItinerarySummaryListRequest> {

        /**
         * Constructs an instance of <code>ItineraryListReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ItineraryListReceiver(TripList activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(TripList activity) {
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
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            activity.updateTripUI();
            return super.handleHttpError(context, intent, httpStatus);
        }

        @Override
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            activity.updateTripUI();
            super.handleRequestFailure(context, intent, requestStatus);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            // TODO: display an error dialog.
            activity.updateTripUI();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess (android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Set the re-fetch flag to false.
            activity.getConcurCore().getItinCache().setShouldRefetchSummaryList(false);
            activity.updateTripUI();
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
}
// ///////////////////////////////////////////
// This was just some fun code to test out shaking and maybe grab a random itin.
// Leaving here for the moment so I don't have to
// recreate it if we ever do something with shake.
// ///////////////////////////////////////////
// public SensorEventListener shakeListener = new SensorEventListener() {
//
// private double FORCE_THRESHOLD = 1.6;
// private double prevForce = 0.0;
//
// public void onAccuracyChanged(Sensor sensor, int accuracy) {
// }
//
// public void onSensorChanged(SensorEvent event) {
// if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { // Just to be
// paranoid
// double force;
//
// force = Math.pow(event.values[0]/SensorManager.GRAVITY_EARTH, 2.0);
// force += Math.pow(event.values[1]/SensorManager.GRAVITY_EARTH, 2.0);
// force += Math.pow(event.values[2]/SensorManager.GRAVITY_EARTH, 2.0);
// force = Math.sqrt(force);
//
// if (force < FORCE_THRESHOLD && prevForce > FORCE_THRESHOLD) {
// Log.d(Const.LOG_TAG, "shaken\nthresh [" + FORCE_THRESHOLD + "]   force [" +
// force + "]   prev [" + prevForce + "]");
// Toast.makeText(TripList.this, "Shaken not stirred",
// Toast.LENGTH_SHORT).show();
// }
//
// prevForce = force;
// }
// }
//
// };
//
// @Override
// protected void onResume() {
// super.onResume();
//
// SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
// sm.registerListener(shakeListener,
// sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
// SensorManager.SENSOR_DELAY_UI);
// }
//
// @Override
// protected void onPause() {
// super.onPause();
//
// SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
// sm.unregisterListener(shakeListener);
// }
