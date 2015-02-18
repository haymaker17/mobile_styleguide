package com.concur.mobile.core.request.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.apptentive.android.sdk.Log;
import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.adapter.AbstractGenericAdapter;
import com.concur.mobile.core.request.task.LocationListTask;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.request.location.Location;
import com.concur.mobile.platform.request.util.RequestParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationSearchActivity extends Activity {

    private static final String CLS_TAG = "LocationSearchActivity";
    private static final int MIN_SEARCH_LENGTH = 3;

    public static final String EXTRA_PARAM_IS_AIRPORT = "isAirport";
    public static final String EXTRA_PARAM_LOCATION_ID = "locationId";
    public static final String EXTRA_PARAM_LOCATION_NAME = "locationName";

    protected Handler searchDelayHandler;
    protected Runnable searchDelayRunnable;
    private BaseAsyncResultReceiver asyncReceiverSearch;

    protected EditText searchText;
    protected ListView searchResultsList;
    protected Spinner searchModeSpinner;

    protected int colorWhiteStripe;
    protected int colorBlueStripe;

    private boolean isAirport = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_search);

        final Boolean isAirport = getIntent().getExtras().getBoolean(EXTRA_PARAM_IS_AIRPORT);
        this.isAirport = isAirport;

        colorWhiteStripe = getResources().getColor(R.color.ListStripeWhite);
        colorBlueStripe = getResources().getColor(R.color.ListStripeBlue);

        configureControls(savedInstanceState);

        searchDelayHandler = new Handler();
        searchDelayRunnable = new DelayedSearch();
    }

    @Override protected void onResume() {
        super.onResume();

        if (asyncReceiverSearch == null) {
            // activity creation
            asyncReceiverSearch = new BaseAsyncResultReceiver(new Handler());
        }
    }

    protected void handleSearchAction() {
        if (searchText.getText().toString().length() >= MIN_SEARCH_LENGTH) {
            // MOB-17636 - Hide the soft keyboard.
            IBinder windowToken = searchText.getWindowToken();
            if (windowToken != null) {
                com.concur.mobile.platform.ui.common.util.ViewUtil.hideSoftKeyboard(this, windowToken);
            }

            doSearch(searchText.getText().toString());
        } else {
            searchDelayHandler.removeCallbacks(searchDelayRunnable);
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
            adapter.clearListItems();
        }
    }

    protected void doSearch(String search) {
        final SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();
        adapter.clearListItems();
        searchDelayHandler.removeCallbacks(searchDelayRunnable);
        searchDelayHandler.postDelayed(searchDelayRunnable, 300);
    }

    protected void configureControls(Bundle savedInstanceState) {

        // Grab all our controls
        searchText = (EditText) findViewById(R.id.locSearchEdit);
        searchResultsList = (ListView) findViewById(R.id.locSearchResults);
        searchModeSpinner = (Spinner) findViewById(R.id.locSearchMode);

        // Listen for typing in the search filter
        // MOB-8910 HTC keyboard doesnt not support IME option instead it support Enter.
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || ((event != null) && (
                        (event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode()
                                == KeyEvent.KEYCODE_ENTER)))) {

                    handleSearchAction();
                    return true;
                }
                return false;
            }
        });

        // Install a touch-listener that determines whether the right compound drawable has been
        // clicked on. This will kick-off a search.
        searchText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchText.setCursorVisible(true);
                // Is the search glass showing?
                Drawable searchGlass = searchText.getCompoundDrawables()[2];
                if (searchGlass == null) {
                    return false;
                }
                // Start search only for up touches.
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                // Detect whether the touch event
                if (event.getX() > searchText.getWidth() - searchText.getPaddingRight() - searchGlass
                        .getIntrinsicWidth()) {
                    // Kick-off the search.
                    handleSearchAction();
                    searchText.setCursorVisible(false);
                    return true;
                }
                return false;
            }

        });

        // Get the list ready
        searchResultsList.setAdapter(new SearchResultsAdapter(this, null));
        searchResultsList.setOnItemClickListener(new SearchResultClickListener());

        searchModeSpinner.setEnabled(false);
        searchModeSpinner.setVisibility(View.GONE);
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

    class DelayedSearch implements Runnable {

        public void run() {
            SearchResultsAdapter adapter = (SearchResultsAdapter) searchResultsList.getAdapter();

            asyncReceiverSearch.setListener(new SearchListener());
            new LocationListTask(getApplicationContext(), 1, asyncReceiverSearch, searchText.getText().toString(),
                    isAirport).execute();
        }
    }

    public class SearchListener implements BaseAsyncRequestTask.AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            ConnectHelper.displayMessage(getApplicationContext(), "SEARCH SUCCESSFULL");
            final List<Location> listLocation = RequestParser
                    .parseLocations(resultData.getString(BaseAsyncRequestTask.HTTP_RESPONSE));
            // metrics
            final Map<String, String> params = new HashMap<>();

            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_TRAVEL_REQUEST_LOCATION);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TRAVEL_REQUEST, Flurry.EVENT_NAME_LIST, params);

            ((SearchResultsAdapter) searchResultsList.getAdapter()).updateList(listLocation);
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            ConnectHelper.displayResponseMessage(getApplicationContext(), resultData,
                    getResources().getString(R.string.tr_error_save));

            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestFail");
            Log.d(Const.LOG_TAG, " onRequestFail in SaveListener...");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            ConnectHelper
                    .displayMessage(getApplicationContext(), getResources().getString(R.string.tr_operation_canceled));
            Log.d(Const.LOG_TAG, CLS_TAG + " calling decrement from onRequestCancel");
            Log.d(Const.LOG_TAG, " onRequestCancel in SaveListener...");
        }

        @Override
        public void cleanup() {
            asyncReceiverSearch.setListener(null);
        }

    }

    class SearchResultClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Intent i = new Intent();
            final Location loc = (Location) parent.getItemAtPosition(position);
            i.putExtra(EXTRA_PARAM_LOCATION_ID, loc.getId());
            i.putExtra(EXTRA_PARAM_LOCATION_NAME, loc.getName());
            setResult(RESULT_OK, i);
            finish();
        }

    }

    class SearchResultsAdapter extends AbstractGenericAdapter<Location> {

        public SearchResultsAdapter(Context context, List<Location> listT) {
            super(context, listT);
        }

        @Override public void updateList(List<Location> locations) {
            clearListItems();
            getList().addAll(locations);
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout row = (RelativeLayout) convertView;

            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                row = (RelativeLayout) inflater.inflate(R.layout.location_search_row, null);
            }

            final Location loc = getItem(position);
            final TextView tv = (TextView) row.findViewById(R.id.locName);
            tv.setText(loc.getName());

            if ((position % 2) == 0) {
                tv.setBackgroundColor(colorBlueStripe);
            } else {
                tv.setBackgroundColor(colorWhiteStripe);
            }

            return row;
        }

    }
}
