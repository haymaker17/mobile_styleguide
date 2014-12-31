package com.concur.mobile.gov.travel.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.travel.data.CompanyLocation;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.service.LocationSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.service.PerDiemLocationListReply;
import com.concur.mobile.gov.travel.service.PerDiemLocationListRequest;
import com.concur.mobile.gov.util.GeneralBaseReceiver;
import com.concur.mobile.gov.util.GovFlurry;

public class GovLocationSearch extends BaseActivity implements INetworkActivityListener {

    private static final String CLS_TAG = GovLocationSearch.class.getSimpleName();

    public static final int SEARCH_CUSTOM = 0x01;
    public static final int SEARCH_COMPANY_LOCATIONS = SEARCH_CUSTOM << 1;
    public static final int SEARCH_RAIL_STATIONS = SEARCH_CUSTOM << 2;
    public static final int SEARCH_AIRPORTS = SEARCH_CUSTOM << 3;

    protected static final int SELECTED_LOCATION = SEARCH_CUSTOM << 4;

    protected int colorWhiteStripe;
    protected int colorBlueStripe;

    protected EditText searchText;
    protected ListView searchResultsList;
    protected Spinner searchModeSpinner;

    protected int allowedSearchModes;
    protected int currentSearchMode;

    protected String currentSearchText;

    protected int minSearchLength = 3;

    // Contains a reference to a receiver to start/stop search progress indicator.
    private NetworkActivityReceiver networkActivityReceiver;
    // Contains the filter used to register the above receiver.
    private IntentFilter networkActivityFilter;

    protected Handler searchDelayHandler;
    protected Runnable searchDelayRunnable;

    private PerDiemLocationListRequest perDiemLocationListRequest;
    private PerdiemLocationReceiver perDiemLocationListReceiver;
    private IntentFilter perDiemLocationFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gov_location_search);
        initScreenHeader();
        colorWhiteStripe = getResources().getColor(R.color.ListStripeWhite);
        colorBlueStripe = getResources().getColor(R.color.ListStripeBlue);

        allowedSearchModes = getIntent().getIntExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, SEARCH_CUSTOM);

        configureControls();

        searchDelayHandler = new Handler();
        searchDelayRunnable = new DelayedSearch();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(networkActivityReceiver, networkActivityFilter);

        registerReceiver(locationReceiver, filterLocationsUpdated);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(networkActivityReceiver);

        unregisterReceiver(locationReceiver);
    }

    /** set screen title */
    private void initScreenHeader() {
        getSupportActionBar().setTitle(getString(R.string.gov_travel_authorization_duty_location_title).toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#isNetworkRequestInteresting(int)
     */
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return (networkMsgType == Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#getNetworkActivityText(java.lang.String)
     */
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#networkActivityStarted(int)
     */
    public void networkActivityStarted(int networkMsgType) {
        if (networkMsgType == Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST) {
            hideSearchGlass();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#networkActivityStopped(int)
     */
    public void networkActivityStopped(int networkMsgType) {
        if (networkMsgType == Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST) {
            showSearchGlass();
        }
    }

    protected void hideSearchGlass() {
        TextView txtView = (TextView) findViewById(R.id.locSearchEdit);
        if (txtView != null) {
            txtView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    protected void showSearchGlass() {
        TextView txtView = (TextView) findViewById(R.id.locSearchEdit);
        if (txtView != null) {
            txtView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_btn_search, 0);
        }
    }

    protected void handleSearchAction() {
        if (searchText.getText().toString().length() >= minSearchLength) {
            doSearch(searchText.getText().toString());
        } else {
            searchDelayHandler
                .removeCallbacks(searchDelayRunnable);
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList
                .getAdapter();
            adapter.clearLocations();
        }
    }

    protected void configureControls() {

        // Register the network activity receiver.
        networkActivityReceiver = new NetworkActivityReceiver(this, this);
        networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);

        // Grab all our controls
        searchText = (EditText) findViewById(R.id.locSearchEdit);
        searchResultsList = (ListView) findViewById(R.id.locSearchResults);
        searchModeSpinner = (Spinner) findViewById(R.id.locSearchMode);

        // Listen for typing in the search filter
        // MOB-8910 HTC keyboard doesnt not support IME option instead it support Enter.
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || ((event != null) && ((event.getAction() == KeyEvent.ACTION_DOWN) && (event
                        .getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {

                    handleSearchAction();
                    return true;
                }
                return false;
            }
        });

        // Install a touch-listener that determines whether the right compound drawable has been
        // clicked on. This will kick-off a search.
        searchText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Is the search glass showing?
                Drawable searchGlass = searchText.getCompoundDrawables()[2];
                if (searchGlass == null) return false;
                // Start search only for up touches.
                if (event.getAction() != MotionEvent.ACTION_UP) return false;
                // Detect whether the touch event
                if (event.getX() > searchText.getWidth() - searchText.getPaddingRight()
                    - searchGlass.getIntrinsicWidth()) {
                    // Kick-off the search.
                    handleSearchAction();
                    return true;
                }
                return false;
            }

        });

        // Get the list ready
        searchResultsList.setAdapter(new SearchResultsAdapter(this));
        searchResultsList.setOnItemClickListener(new SearchResultClickListener());

        // Setup the mode spinner
        ArrayList<SpinnerItem> modeList = new ArrayList<SpinnerItem>(3);
        if ((allowedSearchModes & SEARCH_CUSTOM) == SEARCH_CUSTOM) {
            modeList.add(new SpinnerItem(Integer.toString(SEARCH_CUSTOM), getText(R.string.loc_search_mode_custom)));
        }
        if ((allowedSearchModes & SEARCH_COMPANY_LOCATIONS) == SEARCH_COMPANY_LOCATIONS) {
            modeList.add(new SpinnerItem(Integer.toString(SEARCH_COMPANY_LOCATIONS),
                getText(R.string.loc_search_mode_company)));
        }
        if ((allowedSearchModes & SEARCH_RAIL_STATIONS) == SEARCH_RAIL_STATIONS) {
            modeList.add(new SpinnerItem(Integer.toString(SEARCH_RAIL_STATIONS), getText(R.string.loc_search_mode_rail)));
        }
        if ((allowedSearchModes & SEARCH_AIRPORTS) == SEARCH_AIRPORTS) {
            modeList.add(new SpinnerItem(Integer.toString(SEARCH_AIRPORTS), getText(R.string.loc_search_mode_air)));
        }

        ArrayAdapter<SpinnerItem> modeAdapter = new ArrayAdapter<SpinnerItem>(this,
            android.R.layout.simple_spinner_item, modeList);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchModeSpinner.setAdapter(modeAdapter);

        searchModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                SpinnerItem selMode = ((SpinnerItem) searchModeSpinner.getItemAtPosition(pos));
                currentSearchMode = Integer.parseInt(selMode.id);
                setMinSearchLength();
                searchText.setText(searchText.getText());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        searchModeSpinner.setSelection(0);
        currentSearchMode = Integer.parseInt(modeList.get(0).id);
        setMinSearchLength();

        // Freeze the spinner if only one option
        if (modeList.size() == 1) {
            searchModeSpinner.setEnabled(false);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - start
    // ///////////////////////////////////////////////////////////////////////////

    protected void setMinSearchLength() {
        switch (currentSearchMode) {
        case SEARCH_CUSTOM:
        case SEARCH_AIRPORTS:
            minSearchLength = 3;
            break;
        case SEARCH_COMPANY_LOCATIONS:
        case SEARCH_RAIL_STATIONS:
            minSearchLength = 1;
            break;
        }
    }

    class DelayedSearch implements Runnable {

        public void run() {
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();

            switch (currentSearchMode) {
            case SEARCH_CUSTOM: {
                if (GovAppMobile.isConnected()) {
                    GovAppMobile app = (GovAppMobile) getApplication();
                    app.getService().govSearchLocations(currentSearchText);
                }
                break;
            }
            case SEARCH_AIRPORTS: {
                if (GovAppMobile.isConnected()) {
                    GovAppMobile app = (GovAppMobile) getApplication();
                    app.getService().govSearchLocations(currentSearchText, true);
                }
                break;
            }
            case SEARCH_COMPANY_LOCATIONS:
                adapter.updateLocations(filterCompanyLocations(currentSearchText));
                break;
            case SEARCH_RAIL_STATIONS:
                adapter.updateLocations(filterRailStations(currentSearchText));
                break;
            }
        }

    }

    protected void doSearch(String search) {
        currentSearchText = search;

        searchDelayHandler.removeCallbacks(searchDelayRunnable);
        searchDelayHandler.postDelayed(searchDelayRunnable, 300);
    }

    protected ArrayList<? extends LocationChoice> filterCompanyLocations(String search) {
        GovAppMobile app = (GovAppMobile) getApplication();

        ArrayList<CompanyLocation> filtered = new ArrayList<CompanyLocation>();
        ArrayList<CompanyLocation> full = app.getSystemConfig().getCompanyLocations();

        // TODO: This is a horrible brute-force search. It needs to be replaced with a properly
        // sorted list (by city) and then searched accordingly.
        search = search.toLowerCase();
        int fullSize = full.size();
        for (int i = 0; i < fullSize; i++) {
            CompanyLocation cl = full.get(i);
            String city = cl.city.toLowerCase();
            if (city != null && city.contains(search)) {
                filtered.add(cl);
            }
        }

        return filtered;
    }

    protected ArrayList<? extends LocationChoice> filterRailStations(String search) {

        GovAppMobile app = (GovAppMobile) getApplication();

        ArrayList<RailStation> filtered = new ArrayList<RailStation>();
        ArrayList<RailStation> full = app.getRailStationList();

        if (full != null) {
            // TODO: This is a horrible brute-force search. It needs to be replaced with a properly
            // sorted list and then searched accordingly.
            search = search.toLowerCase();
            int fullSize = full.size();
            for (int i = 0; i < fullSize; i++) {
                RailStation sta = full.get(i);
                String city = sta.city.toLowerCase();
                String code = sta.stationCode.toLowerCase();
                String name = sta.stationName.toLowerCase();
                if (code.startsWith(search) || name.contains(search) || city != null && city.contains(search)) {

                    if (code.equalsIgnoreCase(search)) {
                        // If it's an exact match on station code then put it first.
                        filtered.add(0, sta);
                    } else {
                        filtered.add(sta);
                    }

                    if (filtered.size() == 100)
                        break;
                }
            }
        }

        return filtered;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - end
    // ///////////////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - start
    // ///////////////////////////////////////////////////////////////////////////

    protected final IntentFilter filterLocationsUpdated = new IntentFilter(Const.ACTION_LOCATION_SEARCH_RESULTS);

    protected final BroadcastReceiver locationReceiver = new BroadcastReceiver() {

        /**
         * Receive notification that something has been updated. This method
         * may be called any number of times while the Activity is running.
         */
        public void onReceive(Context context, Intent intent) {
            GovAppMobile ConcurCore = (GovAppMobile) getApplication();
            String action = intent.getAction();

            if (Const.ACTION_LOCATION_SEARCH_RESULTS.equals(action)) {
                LocationSearchReply results = ConcurCore.getLocationSearchResults();
                SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
                if (results != null) {
                    adapter.updateLocations(results.locations);
                } else {
                    adapter.clearLocations();
                }
            }
        }
    };

    class SearchResultClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent();
            i.putExtra(LocationChoice.LOCATION_BUNDLE,
                ((LocationChoice) parent.getItemAtPosition(position)).getBundle());
            i.putExtra(Const.EXTRA_LOCATION_SEARCH_MODE_USED, currentSearchMode);
            // setResult(RESULT_OK, i);
            Bundle locBundle = ((LocationChoice) parent.getItemAtPosition(position)).getBundle();
            LocationChoice selectedLocation = new LocationChoice(locBundle);
            GovAppMobile app = (GovAppMobile) getApplication();
            app.trvlBookingCache.setSelectedLocation(selectedLocation);
            findPerdiemLocation(selectedLocation);
        }

    }

    class SearchResultsAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<? extends LocationChoice> locations;
        private final ArrayList<? extends LocationChoice> emptyLocations = new ArrayList<LocationChoice>(1);

        public SearchResultsAdapter(Context context) {
            this.context = context;
            locations = emptyLocations;
        }

        public void updateLocations(ArrayList<? extends LocationChoice> locations) {
            this.locations = locations;
            notifyDataSetChanged();
        }

        public void clearLocations() {
            locations = emptyLocations;
            notifyDataSetChanged();
        }

        public int getCount() {
            return locations.size();
        }

        public LocationChoice getItem(int position) {
            return locations.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout row = (RelativeLayout) convertView;

            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                row = (RelativeLayout) inflater.inflate(R.layout.location_search_row, null);
            }

            LocationChoice loc = getItem(position);
            TextView tv = (TextView) row.findViewById(R.id.locName);
            tv.setText(loc.getName());

            if ((position % 2) == 0) {
                tv.setBackgroundColor(colorBlueStripe);
            } else {
                tv.setBackgroundColor(colorWhiteStripe);
            }

            return row;
        }

    }

    protected void findPerdiemLocation(LocationChoice selectedLocation) {
        GovAppMobile app = (GovAppMobile) getApplication();
        if (GovAppMobile.isConnected()) {
            GovService govService = app.getService();
            if (govService != null) {
                registerPerdiemLocationReceiver();
                perDiemLocationListRequest = govService.getPerdiemLocationList(selectedLocation);
                if (perDiemLocationListRequest == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".findPerdiemLocation: unable to create request to find perdiem location!");
                    unregisterPerdiemLocationReceiver();
                } else {
                    // set service request.
                    perDiemLocationListReceiver.setServiceRequest(perDiemLocationListRequest);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_LOCATION);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - end
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * register perdiem location receiver
     * */
    protected void registerPerdiemLocationReceiver() {
        if (perDiemLocationListReceiver == null) {
            perDiemLocationListReceiver = new PerdiemLocationReceiver(this);
            if (perDiemLocationFilter == null) {
                perDiemLocationFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_GET_PERDIEM_LOCATIONS);
            }
            getApplicationContext().registerReceiver(perDiemLocationListReceiver, perDiemLocationFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerPerdiemLocationReceiver not null");
        }
    }

    /**
     * un-register perdiem location receiver
     * */
    protected void unregisterPerdiemLocationReceiver() {
        if (perDiemLocationListReceiver != null) {
            getApplicationContext().unregisterReceiver(perDiemLocationListReceiver);
            perDiemLocationListReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterPerdiemLocationReceiver is null!");
        }
    }

    /**
     * An extension of {@link GeneralBaseReceiver} for the purposes of handling
     * the response after getting new perdiem location for selected location
     */
    class PerdiemLocationReceiver extends
        GeneralBaseReceiver<GovLocationSearch, PerDiemLocationListRequest>
    {

        private final String CLS_TAG = GovLocationSearch.CLS_TAG + "."
            + PerdiemLocationReceiver.class.getSimpleName();

        protected PerdiemLocationReceiver(GovLocationSearch activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(GovLocationSearch activity) {
            activity.perDiemLocationListRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_LOCATION);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_LOCATION_FAIL);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getApplication();
            final PerDiemLocationListReply reply = app.trvlBookingCache.getPerDiemLocationListReply();
            if (reply != null) {
                onHandleSuccessPerDiemLocation(reply, app);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(PerDiemLocationListRequest request) {
            activity.perDiemLocationListRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterPerdiemLocationReceiver();
        }
    }

    /**
     * Handle the response after getting new perdiem locations
     * 
     * @param app
     *            : Reference of GovAppMobile
     * @param reply
     *            : reference of PerDiemLocationListReply
     * */
    protected void onHandleSuccessPerDiemLocation(PerDiemLocationListReply reply, GovAppMobile app) {
        List<PerDiemListRow> perDiemList = reply.perDiemListRows;

        // add default perdiem on the top of the list
        PerDiemListRow defaultLocation = reply.getDefaultPerDiemLocation();
        if (defaultLocation != null) {
            perDiemList.add(0, defaultLocation);
        }
        // set perdiem list to cache
        app.trvlBookingCache.setPerDiemList(perDiemList);

        // start perdiem location
        Intent it = new Intent(GovLocationSearch.this, TDYPerDiemLocations.class);
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ACTION, GovFlurry.PARAM_VALUE_PER_DIEM_LOC_SEARCH);
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_BOOK, Flurry.EVENT_NAME_ACTION, params);
        startActivityForResult(it, SELECTED_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SELECTED_LOCATION: {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
            break;
        }

        default:
            break;
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_LOCATION: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_get_per_diem_location));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    // Dismiss the dialog.
                    dialog.dismiss();
                    // Attempt to cancel the request.
                    if (perDiemLocationListRequest != null) {
                        perDiemLocationListRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: perDiemLocationListRequest is null!");
                    }

                }
            });
            dialog = pDialog;
            break;
        }

        case com.concur.mobile.gov.util.Const.DIALOG_GET_PERDIEM_LOCATION_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_get_per_diem_location_fail_title);
            dlgBldr.setMessage(R.string.gov_get_per_diem_location_fail_msg);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }

        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean retVal = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            if (perDiemLocationListRequest != null) {
                perDiemLocationListRequest.cancel();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onKeyDown: perDiemLocationListRequest is null!");
            }
            setResult(RESULT_CANCELED);
            finish();
            break;
        }
        default: {
            retVal = super.onKeyDown(keyCode, event);
            break;
        }
        }
        return retVal;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (perDiemLocationListReceiver == null) {
            registerPerdiemLocationReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (perDiemLocationListReceiver != null) {
            unregisterPerdiemLocationReceiver();
        }

    }

}
