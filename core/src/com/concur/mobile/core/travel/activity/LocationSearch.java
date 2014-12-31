package com.concur.mobile.core.travel.activity;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.travel.data.CompanyLocation;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.rail.data.RailStation;
import com.concur.mobile.core.travel.service.LocationSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.SpinnerItem;

public class LocationSearch extends Activity implements INetworkActivityListener {

    public static final int SEARCH_CUSTOM = 0x01;
    public static final int SEARCH_COMPANY_LOCATIONS = SEARCH_CUSTOM << 1;
    public static final int SEARCH_RAIL_STATIONS = SEARCH_CUSTOM << 2;
    public static final int SEARCH_AIRPORTS = SEARCH_CUSTOM << 3;
    protected static final String RESULT_LIST = "result_list";

    protected int colorWhiteStripe;
    protected int colorBlueStripe;

    protected EditText searchText;
    protected ListView searchResultsList;
    protected Spinner searchModeSpinner;

    protected int allowedSearchModes;
    protected int currentSearchMode;
    protected int previousSearchMode;
    protected String preSearchText;

    protected String currentSearchText;

    protected int minSearchLength = 3;

    // Contains a reference to a receiver to start/stop search progress indicator.
    private NetworkActivityReceiver networkActivityReceiver;
    // Contains the filter used to register the above receiver.
    private IntentFilter networkActivityFilter;

    protected Handler searchDelayHandler;
    protected Runnable searchDelayRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location_search);

        colorWhiteStripe = getResources().getColor(R.color.ListStripeWhite);
        colorBlueStripe = getResources().getColor(R.color.ListStripeBlue);

        allowedSearchModes = getIntent().getIntExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, SEARCH_CUSTOM);

        configureControls(savedInstanceState);

        searchDelayHandler = new Handler();
        searchDelayRunnable = new DelayedSearch();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<? extends LocationChoice> extracted(Serializable locBundle) {
        return (ArrayList<? extends LocationChoice>) locBundle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
        // Save the location list
        if (adapter != null && adapter.getCount() > 0) {
            // Store the locations
            outState.putSerializable(RESULT_LIST, adapter.locations);
        } else {
            outState.putSerializable(RESULT_LIST, null);
        }

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
            // MOB-17636 - Hide the soft keyboard.
            IBinder windowToken = searchText.getWindowToken();
            if (windowToken != null) {
                com.concur.mobile.platform.ui.common.util.ViewUtil.hideSoftKeyboard(this, windowToken);
            }

            doSearch(searchText.getText().toString());
        } else {
            searchDelayHandler.removeCallbacks(searchDelayRunnable);
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            adapter.clearLocations();
        }
    }

    protected void configureControls(Bundle savedInstanceState) {

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
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || ((event != null) && ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {

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
                searchText.setCursorVisible(true);
                // Is the search glass showing?
                Drawable searchGlass = searchText.getCompoundDrawables()[2];
                if (searchGlass == null)
                    return false;
                // Start search only for up touches.
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                // Detect whether the touch event
                if (event.getX() > searchText.getWidth() - searchText.getPaddingRight()
                        - searchGlass.getIntrinsicWidth()) {
                    // Kick-off the search.
                    handleSearchAction();
                    searchText.setCursorVisible(false);
                    return true;
                }
                return false;
            }

        });

        // Get the list ready
        searchResultsList.setAdapter(new SearchResultsAdapter(this));
        searchResultsList.setOnItemClickListener(new SearchResultClickListener());

        // load existing location list after screen rotation
        if (savedInstanceState != null && savedInstanceState.getSerializable(RESULT_LIST) != null) {
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            Serializable locBundle = savedInstanceState.getSerializable(RESULT_LIST);
            if (locBundle != null)
                adapter.updateLocations(extracted(locBundle));
        }
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
                previousSearchMode = previousSearchMode == 0 ? currentSearchMode : previousSearchMode;
                setMinSearchLength();
                SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();

                // load previous custom search locations
                if (adapter.getCount() > 0 && previousSearchMode == SEARCH_CUSTOM) {
                    preSearchText = searchText.getText().toString();
                    if (preSearchText != null && preSearchText.length() > 0) {
                        adapter.updatePreLocations(adapter.locations);
                    }
                    adapter.clearLocations();
                }
                // show user company locations
                if (SEARCH_COMPANY_LOCATIONS == currentSearchMode) {
                    ConcurCore app = (ConcurCore) getApplication();
                    ArrayList<CompanyLocation> full = app.getSystemConfig().getCompanyLocations();
                    adapter.clearLocations();
                    searchText.setText(null);
                    adapter.updateLocations(full);
                } else {
                    if (adapter != null && adapter.preLocations.size() > 0) {
                        adapter.updateLocations(adapter.preLocations);
                        searchText.setText(preSearchText);
                    }
                    if (searchText.getText().toString().length() == 0) {
                        adapter.clearLocations();
                    }

                }
                previousSearchMode = currentSearchMode;
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
                if (ConcurCore.isConnected()) {
                    ConcurCore app = (ConcurCore) getApplication();
                    app.getService().searchLocations(currentSearchText);
                }
                break;
            }
            case SEARCH_AIRPORTS: {
                if (ConcurCore.isConnected()) {
                    ConcurCore app = (ConcurCore) getApplication();
                    app.getService().searchLocations(currentSearchText, true);
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
        SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
        adapter.clearLocations();
        searchDelayHandler.removeCallbacks(searchDelayRunnable);
        searchDelayHandler.postDelayed(searchDelayRunnable, 300);
    }

    protected ArrayList<? extends LocationChoice> filterCompanyLocations(String search) {
        ConcurCore app = (ConcurCore) getApplication();

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

        ConcurCore app = (ConcurCore) getApplication();

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
         * Receive notification that something has been updated. This method may be called any number of times while the Activity
         * is running.
         */
        public void onReceive(Context context, Intent intent) {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
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
            setResult(RESULT_OK, i);
            finish();
        }

    }

    class SearchResultsAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<? extends LocationChoice> locations;
        private final ArrayList<? extends LocationChoice> emptyLocations = new ArrayList<LocationChoice>(1);
        private ArrayList<? extends LocationChoice> preLocations = new ArrayList<LocationChoice>();

        public SearchResultsAdapter(Context context) {
            this.context = context;
            locations = emptyLocations;
            preLocations = emptyLocations;
        }

        public void updateLocations(ArrayList<? extends LocationChoice> locations) {
            this.locations = locations;
            notifyDataSetChanged();
        }

        public void updatePreLocations(ArrayList<? extends LocationChoice> locations) {
            this.preLocations = locations;
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

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - end
    // ///////////////////////////////////////////////////////////////////////////

}
