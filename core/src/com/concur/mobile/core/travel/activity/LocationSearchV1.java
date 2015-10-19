package com.concur.mobile.core.travel.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
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
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author tejoa Location Search with Search View widget.
 */
@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_TRAVEL_HOTEL_SEARCH_CRITERIA)
public class LocationSearchV1 extends Activity implements INetworkActivityListener, SearchView.OnQueryTextListener {

    public static final int SEARCH_CUSTOM = 0x01;
    public static final int SEARCH_COMPANY_LOCATIONS = SEARCH_CUSTOM << 1;
    public static final int SEARCH_RAIL_STATIONS = SEARCH_CUSTOM << 2;
    public static final int SEARCH_AIRPORTS = SEARCH_CUSTOM << 3;
    public static final int SEARCH_CURRENT_LOCATION = SEARCH_CUSTOM << 4;
    protected static final String RESULT_LIST = "result_list";

    protected ListView searchResultsList;
    protected Button searchOfficeLocations;
    protected Button currentLocationBtn;

    protected int allowedSearchModes;
    protected int currentSearchMode;

    protected String currentSearchText;

    protected int minSearchLength = 3;
    protected SearchView searchView;
    private View backButton;
    private View searchIcon;

    // Contains a reference to a receiver to start/stop search progress indicator.
    private NetworkActivityReceiver networkActivityReceiver;
    // Contains the filter used to register the above receiver.
    private IntentFilter networkActivityFilter;

    protected Handler searchDelayHandler;
    protected Runnable searchDelayRunnable;

    private String currentLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.location_search_v1);

        allowedSearchModes = getIntent().getIntExtra(Const.EXTRA_LOCATION_SEARCH_ALLOWED_MODES, SEARCH_CUSTOM);
        currentLocationName = getIntent().getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);

        configureControls(savedInstanceState);

        searchDelayHandler = new Handler();
        searchDelayRunnable = new DelayedSearch();

        Log.d(com.concur.mobile.platform.ui.travel.util.Const.LOG_TAG, "LocationSearchV1*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_TRAVEL_DESTINATION_TAPPED);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL,
                Flurry.EVENT_ACTION_TRAVEL_DESTINATION_TAPPED);
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

        EventTracker.INSTANCE.activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(networkActivityReceiver);

        unregisterReceiver(locationReceiver);

        EventTracker.INSTANCE.activityStop(this);
    }

    protected void configureControls(Bundle savedInstanceState) {

        // Register the network activity receiver.
        networkActivityReceiver = new NetworkActivityReceiver(this, this);
        networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);

        searchResultsList = (ListView) findViewById(R.id.locSearchResults);
        searchOfficeLocations = (Button) findViewById(R.id.officeLocation);
        searchOfficeLocations.setVisibility(View.VISIBLE);

        currentLocationBtn = (Button) findViewById(R.id.currentLocation);

        backButton = (ImageView) findViewById(R.id.search_back_button);
        searchIcon = (ImageView) findViewById(R.id.search_icon);
        searchIcon.setVisibility(View.VISIBLE);
        searchResultsList.setVisibility(View.GONE);
        setupSearchView();
        currentSearchMode = SEARCH_CUSTOM;

        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (searchResultsList != null && searchResultsList.getVisibility() == View.VISIBLE) {
                    searchResultsList.setVisibility(View.GONE);
                    searchOfficeLocations.setVisibility(View.VISIBLE);
                    currentLocationBtn.setVisibility(View.VISIBLE);
                    SearchResultsAdapter adapter = searchResultsList.getAdapter() != null ? (SearchResultsAdapter) searchResultsList.getAdapter() : null;
                    if (adapter != null) adapter.clearLocations();
                    currentSearchMode = SEARCH_CUSTOM;
                    currentSearchText = null;
                    if (currentLocationName != null) {
                        searchView.setQueryHint(currentLocationName);
                    } else {
                        searchView.setQueryHint(getText(R.string.loc_search_hint_v1));
                    }

                } else {
                    onBackPressed();
                }
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

        searchOfficeLocations.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Check for connectivity, if none, then display dialog and
                // return.
                if (!ConcurCore.isConnected()) {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    return;
                }
                // currentSearchMode = Integer.parseInt(selMode.id);
                // previousSearchMode = previousSearchMode == 0 ? currentSearchMode : previousSearchMode;
                SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();

                currentSearchMode = SEARCH_COMPANY_LOCATIONS;
                ConcurCore app = (ConcurCore) getApplication();
                ArrayList<CompanyLocation> full = app.getSystemConfig().getCompanyLocations();
                adapter.clearLocations();
                searchView.setQueryHint(getText(R.string.loc_search_office_hint));
                searchOfficeLocations.setVisibility(View.GONE);
                currentLocationBtn.setVisibility(View.GONE);
                searchResultsList.setVisibility(View.VISIBLE);
                adapter.updateLocations(full);
            }

        });

        currentLocationBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra(Const.EXTRA_LOCATION_SEARCH_MODE_USED, SEARCH_CURRENT_LOCATION);
                setResult(RESULT_OK, i);
                finish();
            }
        });

    }

    /**
     * set the Search Manager
     *
     * @return
     */
    private SearchView setupSearchView() {

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        if (currentLocationName != null) {
            searchView.setQueryHint(currentLocationName);
        } else {
            searchView.setQueryHint(getText(R.string.loc_search_hint_v1));
        }
        searchView.setOnQueryTextListener((OnQueryTextListener) this);
        setSearchIcons();
        setSearchTextColour();
        return searchView;
    }

    /**
     * custom icons for search widget
     */
    private void setSearchIcons() {
        try {
            int searchbutton = searchView.getContext().getResources()
                    .getIdentifier("android:id/search_mag_icon", null, null);
            ImageView searchButtonImage = (ImageView) searchView.findViewById(searchbutton);
            searchButtonImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * custom colors to search Widget
     */
    private void setSearchTextColour() {

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.WHITE);
            int searchTextId = searchPlate.getContext().getResources()
                    .getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.BLACK);
                searchText.setHintTextColor(Color.parseColor("#9f9f9f"));
            }
        }
    }

    /**
     * on textChange listener
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        handleSearch(newText);
        return false;
    }

    /**
     * on text submit listener
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return true;
    }

    protected void handleSearch(String text) {
        searchIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        searchResultsList.setVisibility(View.VISIBLE);
        searchOfficeLocations.setVisibility(View.GONE);
        currentLocationBtn.setVisibility(View.GONE);

        if (text.length() >= minSearchLength && currentSearchMode == SEARCH_CUSTOM) {
            doSearch(text);
        } else if (text.length() == 0) {
            searchIcon.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            // hide the list view and show the buttons again
            searchResultsList.setVisibility(View.GONE);
            searchOfficeLocations.setVisibility(View.VISIBLE);
            currentLocationBtn.setVisibility(View.VISIBLE);
        } else if (text.length() >= 1 && currentSearchMode != SEARCH_CUSTOM) {
            doSearch(text);
        }
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
            // hideSearchGlass();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#networkActivityStopped(int)
     */
    public void networkActivityStopped(int networkMsgType) {
        if (networkMsgType == Const.MSG_TRAVEL_LOCATION_SEARCH_REQUEST) {
            // showSearchGlass();
        }
    }

    // protected void hideSearchGlass() {
    // TextView txtView = (TextView) findViewById(R.id.locSearchEdit);
    // if (txtView != null) {
    // txtView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    // }
    // }
    //
    // protected void showSearchGlass() {
    // TextView txtView = (TextView) findViewById(R.id.locSearchEdit);
    // if (txtView != null) {
    // txtView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_btn_search, 0);
    // }
    // }

    // ///////////////////////////////////////////////////////////////////////////
    // Search text methods - start
    // ///////////////////////////////////////////////////////////////////////////

    class DelayedSearch implements Runnable {

        public void run() {
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();

            switch (currentSearchMode) {

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
                default: {
                    currentSearchMode = SEARCH_CUSTOM;
                    if (ConcurCore.isConnected()) {
                        ConcurCore app = (ConcurCore) getApplication();
                        app.getService().searchLocations(currentSearchText);
                    }
                    break;
                }
            }
        }

    }

    protected void doSearch(String search) {
        currentSearchText = search;

        SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
        adapter.clearLocations();
        searchDelayHandler.removeCallbacks(searchDelayRunnable);
        searchDelayHandler.postDelayed(searchDelayRunnable, 800);
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
            String name = cl.getName().toLowerCase();
            if (name != null && name.contains(search)) {
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
                row = (RelativeLayout) inflater.inflate(R.layout.location_search_row_v1, null);
            }

            LocationChoice loc = getItem(position);
            TextView tv = (TextView) row.findViewById(R.id.locName);
            ImageView officeIcon = (ImageView) row.findViewById(R.id.office_icon);
            TextView city = (TextView) row.findViewById(R.id.locCity);

            // int padding = ViewUtil.dpToPx(context, 20);
            int textPadding = ViewUtil.dpToPx(context, 16);
            //
            if (currentSearchMode == SEARCH_COMPANY_LOCATIONS) {

                CompanyLocation companyLocation = (CompanyLocation) loc;

                int leftPadding = ViewUtil.dpToPx(context, 5);
                row.setPadding(leftPadding, 0, 0, 0);

                if (officeIcon != null) {
                    officeIcon.setVisibility(View.VISIBLE);
                }
                if (city != null) {
                    city.setVisibility(View.VISIBLE);
                    Bundle address = loc.getBundle();
                    city.setText(companyLocation.getProvince());
                }
//                int textLeftPadding = ViewUtil.dpToPx(context, 8);
                tv.setPadding(0, textPadding, 0, 0);
                tv.setText(companyLocation.getAddress());

            } else {
                int leftPadding = ViewUtil.dpToPx(context, 56);

                row.setPadding(leftPadding, 0, 0, 0);
                if (officeIcon != null) {
                    officeIcon.setVisibility(View.GONE);
                }
                if (city != null) {
                    city.setVisibility(View.GONE);
                }
                tv.setPadding(0, textPadding, 0, textPadding);
                tv.setText(loc.getName());
            }

            row.requestLayout();
            return row;
        }

    }

    // ///////////////////////////////////////////////////////////////////////////
    // Search result methods - end
    // ///////////////////////////////////////////////////////////////////////////

}
