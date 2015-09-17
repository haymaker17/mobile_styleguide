package com.concur.mobile.core.travel.hotel.jarvis.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.travel.provider.TravelUtilHotel;
import com.concur.mobile.platform.travel.search.hotel.BenchmarksCollection;
import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.travel.search.hotel.HotelBenchmark;
import com.concur.mobile.platform.travel.search.hotel.HotelComparator;
import com.concur.mobile.platform.travel.search.hotel.HotelPropertyId;
import com.concur.mobile.platform.travel.search.hotel.HotelRatesLoader;
import com.concur.mobile.platform.travel.search.hotel.HotelRatesRESTResult;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchPollResultLoader;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchRESTResult;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchResultLoader;
import com.concur.mobile.platform.travel.search.hotel.HotelViolation;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.activity.TravelBaseActivity;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelBenchmarkListItem;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelBenchmarksFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFilterFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFilterFragment.HotelSearchResultsFilterListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFragment.HotelSearchResultsFragmentListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultListItem;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultMapFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.SpinnerDialogFragment;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomFieldsConfig;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.concur.mobile.platform.util.Format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Activity to launch the Jarvis Hotel Search
 *
 * @author RatanK
 */
@EventTracker.EventTrackerClassName(getClassName = Flurry.SCREEN_NAME_TRAVEL_HOTEL_SEARCH_RESULTS)
public class HotelSearchAndResultActivity extends TravelBaseActivity
        implements HotelSearchResultsFilterListener, HotelSearchResultsFragmentListener,
        HotelSearchResultMapFragment.HotelSearchMapsFragmentListener,
        SpinnerDialogFragment.SpinnerDialogFragmentCallbackListener {

    public static final String FRAGMENT_SEARCH_RESULT = "FRAGMENT_SEARCH_RESULT";
    private static final String FRAGMENT_SEARCH_RESULT_FILTER = "FRAGMENT_SEARCH_RESULT_FILTER";
    public static final String FRAGMENT_SEARCH_RESULT_MAP = "FRAGMENT_SEARCH_RESULT_MAP";
    public static final String FRAGMENT_SORT_ITEMS = "FRAGMENT_SORT_ITEMS";

    private static final int HOTEL_SEARCH_LIST_LOADER_ID = 0;
    private static final int HOTEL_POLL_LIST_LOADER_ID = 1;
    private static final int HOTEL_RATES_LOADER_ID = 2;

    //private static final String SERVICE_END_POINT = "/mobile/travel/v1.0/Hotels";

    private static final String PRICE_TO_BEAT_DETAILS_FRAGMENT = "price.to.beat.details.fragment";

    private HotelSearchResultFragment hotelSearchRESTResultFrag;
    private HotelSearchResultFilterFragment filterFrag;

    private LoaderManager lm;
    private long searchWorkflowStartTime;
    private boolean searchDone;
    private String pollingURL;
    private boolean fromPolling;

    private ListItemAdapter<HotelSearchResultListItem> listItemAdapater;

    // full set of hotel list items
    private List<HotelSearchResultListItem> hotelListItems;

    // hotel list items to cache the 'suggested' server sort, instead of retrieveing from database
    private List<HotelSearchResultListItem> serverSortedHotelListItemsCache;

    // set of hotel list items to sort
    private List<HotelSearchResultListItem> hotelListItemsToSort;

    private Double longitude;
    private Double latitude;
    private Calendar checkInDate;
    private Calendar checkOutDate;
    private String location;
    private String distanceUnit;
    private int numberOfNights;
    private ArrayList<String[]> violationReasons;
    private String cacheKey;
    private boolean retrieveFromDB;
    private HotelSearchResultListItem selectedHotelListItem;
    private boolean ruleViolationExplanationRequired;
    private String currentTripId;
    private boolean showGDSName;
    private String customTravelText;
    private List<HotelViolation> updatedViolations;
    private List<HotelViolation> violations;
    private BenchmarksCollection benchmarksCollection;
    private boolean searchNearMe;
    private HotelSearchResultMapFragment mapFragment;

    // MOB-24049
    private SpinnerItem sortItems[];
    private SpinnerItem curSortItem;
    private String starRating;
    private Double distance;
    private String nameContaining;
    private boolean callFromDB;
    private int listItemSelectedPosition = -1;

    // for GA tracking
    private boolean suggestedAvailable;

    // HotelSearchResults loader callback implementation
    private LoaderManager.LoaderCallbacks<HotelSearchRESTResult> hotelSearchRESTResultLoaderCallbacks = new LoaderManager.LoaderCallbacks<HotelSearchRESTResult>() {

        PlatformAsyncTaskLoader<HotelSearchRESTResult> hotelSearchAsyncTaskLoader;

        @Override
        public Loader<HotelSearchRESTResult> onCreateLoader(int id, Bundle bundle) {
            Context context = getApplicationContext();
            if (!searchDone && pollingURL != null) {
                // request polling
                fromPolling = true;
                Log.d(Const.LOG_TAG, " ***** creating poll search loader *****  ");
                hotelSearchAsyncTaskLoader = new HotelSearchPollResultLoader(context, checkInDate, checkOutDate,
                        latitude, longitude, 25, distanceUnit, pollingURL);

            } else {
                fromPolling = false;
                // request initial search
                Log.d(Const.LOG_TAG, " ***** creating search loader *****  ");

                hotelSearchAsyncTaskLoader = new HotelSearchResultLoader(context, checkInDate, checkOutDate, latitude,
                        longitude, 25, distanceUnit);

            }
            return hotelSearchAsyncTaskLoader;

        }

        @Override
        public void onLoaderReset(Loader<HotelSearchRESTResult> loader) {
            Log.d(Const.LOG_TAG, " ***** loader reset *****  ");
            if (listItemAdapater != null) {
                listItemAdapater.setItems(null);
            }

        }

        @Override
        public void onLoadFinished(Loader<HotelSearchRESTResult> loader, HotelSearchRESTResult hotelSearchResult) {

            if (hotelSearchAsyncTaskLoader.result == hotelSearchAsyncTaskLoader.SESSION_EXPIRED
                    || hotelSearchAsyncTaskLoader.result == hotelSearchAsyncTaskLoader.RE_AUTHENTICATED) {
                hotelSearchRESTResultFrag.hideProgressBar();
                sessionExpired(hotelSearchAsyncTaskLoader.result);
            } else {

                List<HotelSearchResultListItem> hotelListItemsFromLoader = new ArrayList<HotelSearchResultListItem>();

                Log.d(Const.LOG_TAG, " ***** searchdone *****  " + searchDone);

                if (searchDone) {
                    // skip everything as this could be due to on orientation change or pop back from stack
                    Log.d(Const.LOG_TAG, " ***** searchDone true,  hotelListItems = " + (hotelListItems != null ?
                            hotelListItems.size() :
                            0));
                    hotelSearchRESTResultFrag.hideProgressBar();
                } else {
                    if (hotelSearchResult == null) {
                        hotelSearchRESTResultFrag.hideProgressBar();
                        // hotelSearchRESTResultFrag.showNegativeView();
                        // TODO handle the search errors
                        Toast.makeText(getApplicationContext(), R.string.dlg_travel_hotel_search_failed_general_msg,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        searchDone = hotelSearchResult.searchDone;
                        hotelSearchRESTResultFrag.getHotelListView().setAlpha(0.50f);

                        if (hotelSearchResult.hotels != null && hotelSearchResult.hotels.size() > 0) {
                            for (Hotel hotel : hotelSearchResult.hotels) {
                                hotel.distanceUnit = hotelSearchResult.distanceUnit;
                                hotel.currencyCode = hotelSearchResult.currency;
                                HotelSearchResultListItem item = new HotelSearchResultListItem(hotel);
                                hotelListItemsFromLoader.add(item);
                            }

                            violations = hotelSearchResult.violations;
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.hotel_search_no_hotels_found,
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }

                        Log.d(Const.LOG_TAG,
                                " ***** hotelSearchResult.searchDone " + searchDone + ",  hotelListItems = " + (
                                        hotelListItems != null ?
                                                hotelListItems.size() :
                                                0) + ",  hotelListItemsFromLoader = " + (
                                        hotelListItemsFromLoader != null ? hotelListItemsFromLoader.size() : 0));

                        if (searchDone) {
                            if (hotelListItems == null) {
                                hotelListItems = new ArrayList<HotelSearchResultListItem>();
                            }
                            hotelListItems = hotelListItemsFromLoader;
                            if (hotelListItems != null && hotelListItems.size() > 0) {

                                // set hotel list items that can be sorted
                                hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>();
                                hotelListItemsToSort.addAll(hotelListItems);

                                benchmarksCollection = hotelSearchResult.benchmarksCollection;

                                showResults();
                            }

                            hotelSearchRESTResultFrag.hideProgressBar();

                        } else {

                            if (fromPolling) {
                                if (listItemAdapater == null) {
                                    Log.e(Const.LOG_TAG, " cannot call refreshUIListItem: listItemAdapater is null!");
                                } else {
                                    hotelSearchRESTResultFrag
                                            .refreshUIListItems(hotelListItemsFromLoader, listItemAdapater);
                                }
                            } else {
                                hotelListItems = hotelListItemsFromLoader;
                                updateResultsFragmentUI(hotelListItemsFromLoader, null);
                            }

                            if (hotelSearchResult.polling != null) {
                                pollingURL = hotelSearchResult.polling.href;
                            }

                            // create a delayed handler for the aysnc task
                            Handler asyncHandler = new Handler();
                            asyncHandler.postDelayed(new Runnable() {

                                public void run() {
                                    // if the time taken for the search workflow (i.e. start of search to end of pricing) > 90 seconds
                                    // then
                                    // stop
                                    // the
                                    // functionality
                                    long timeNow = (System.currentTimeMillis() - searchWorkflowStartTime) / 1000;

                                    Log.d(Const.LOG_TAG,
                                            " in retrieveHotelPricing, time now from start of search " + timeNow
                                                    + " seconds. If this is < 90 then will invoke GetHotelsPricing again.");
                                    if (timeNow < 90) {
                                        // call the hotel polling loader that invokes the polling end point
                                        callPollLoader();
                                    } else {

                                        hotelSearchRESTResultFrag.hideProgressBar();

                                    }
                                }

                            }, 2000);
                        }
                    }
                }
            }
        }
    };

    // HotelRates loader callback implementation
    private LoaderManager.LoaderCallbacks<HotelRatesRESTResult> hotelRatesRESTResultLoaderCallbacks = new LoaderManager.LoaderCallbacks<HotelRatesRESTResult>() {

        PlatformAsyncTaskLoader<HotelRatesRESTResult> hotelRatesAsyncTaskLoader;

        @Override
        public Loader<HotelRatesRESTResult> onCreateLoader(int id, Bundle args) {
            Context context = getApplicationContext();
            Hotel hotelSelected = selectedHotelListItem.getHotel();
            String rateUrl = hotelSelected.ratesURL.href;
            // get ids from db
            Hotel hotel = TravelUtilHotel.getHotelByRateUrl(context, rateUrl, cacheKey);
            if (hotel != null) {
                hotelSelected._id = hotel._id;
                hotelSelected.search_id = hotel.search_id;
            }

            hotelRatesAsyncTaskLoader = new HotelRatesLoader(context, rateUrl, hotelSelected._id,
                    hotelSelected.search_id);
            return hotelRatesAsyncTaskLoader;
        }

        @Override
        public void onLoadFinished(Loader<HotelRatesRESTResult> loader, HotelRatesRESTResult hotelRateResult) {

            hotelSearchRESTResultFrag.hideProgressBar();

            if (hotelRatesAsyncTaskLoader.result == hotelRatesAsyncTaskLoader.SESSION_EXPIRED
                    || hotelRatesAsyncTaskLoader.result == hotelRatesAsyncTaskLoader.RE_AUTHENTICATED) {

                sessionExpired(hotelRatesAsyncTaskLoader.result);
            } else {

                if (mapFragment != null) {
                    mapFragment.hideProgressBar();
                }
                // bar.setVisibility(View.GONE);
                Hotel hotel = hotelRateResult != null ? hotelRateResult.hotel : null;
                if (hotel != null && hotel.rates != null) {

                    if (selectedHotelListItem != null && selectedHotelListItem.getHotel() != null) {
                        selectedHotelListItem.getHotel().rates = hotel.rates;
                        selectedHotelListItem.getHotel().lowestRate = hotel.lowestRate;
                        selectedHotelListItem.getHotel().priceToBeat = hotel.priceToBeat;
                        selectedHotelListItem.getHotel().availabilityErrorCode = hotel.availabilityErrorCode;
                        selectedHotelListItem.getHotel().travelPointsForLowestRate = hotel.travelPointsForLowestRate;
                        updatedViolations = hotelRateResult.violations;
                        callFromDB = true;
                        viewHotelChoiceDetails();
                    }

                } else {
                    callFromDB = false;
                    Toast.makeText(getApplicationContext(), "No Rooms Available", Toast.LENGTH_LONG).show();
                }
            }
            // TODO add GA event for booking
        }

        @Override
        public void onLoaderReset(Loader<HotelRatesRESTResult> loader) {
            Log.d(Const.LOG_TAG, " ***** loader reset *****  ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_search_and_result);

        Intent intent = getIntent();

        location = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
        violationReasons = (ArrayList<String[]>) intent.getSerializableExtra("violationReasons");
        ruleViolationExplanationRequired = intent.getBooleanExtra("ruleViolationExplanationRequired", false);
        currentTripId = intent.getStringExtra("currentTripId");

        checkInDate = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        checkOutDate = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);
        latitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE));
        longitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE));
        distanceUnit = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);
        showGDSName = intent.getBooleanExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_SHOW_GDS_NAME, false);
        searchNearMe = intent.getBooleanExtra(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, false);

        if (intent.hasExtra("travelCustomFieldsConfig")) {
            travelCustomFieldsConfig = (TravelCustomFieldsConfig) intent
                    .getSerializableExtra("travelCustomFieldsConfig");
        }

        if (intent.hasExtra("customTravelText")) {
            customTravelText = intent.getStringExtra("customTravelText");
        }
        hotelSearchRESTResultFrag = (HotelSearchResultFragment) getFragmentManager()
                .findFragmentByTag(FRAGMENT_SEARCH_RESULT);

        if (hotelSearchRESTResultFrag == null) {
            hotelSearchRESTResultFrag = new HotelSearchResultFragment();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (hotelSearchRESTResultFrag != null && !hotelSearchRESTResultFrag.isAdded()) {
            ft.add(R.id.container, hotelSearchRESTResultFrag, FRAGMENT_SEARCH_RESULT);
            ft.commit();
        }

        hotelSearchRESTResultFrag.location = location;
        hotelSearchRESTResultFrag.durationOfStayForDisplayInHeader =
                getIntent().getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN) + " - " + intent
                        .getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);

        listItemAdapater = new ListItemAdapter<HotelSearchResultListItem>(this, hotelListItems);

        if (checkInDate != null && checkInDate.before(checkOutDate)) {
            numberOfNights = (int) ((checkOutDate.getTimeInMillis() - checkInDate.getTimeInMillis()) / (24 * 60 * 60
                    * 1000));
        } else {
            // safe assume to 1 night
            numberOfNights = 1;
        }

        initSortOptions();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (callFromDB) {
            hotelSearchRESTResultFrag.showProgressBar(false);
            populateHotelListItemsFromDB();
        }
    }

    @Override
    public void fragmentReady() {
        // signal from fragment that it is loaded
        cacheKey = HotelSearchResultLoader
                .prepareEndPointUrl(latitude, longitude, distanceUnit, checkInDate, checkOutDate);
        populateHotelListItemsFromDB();
    }

    private void initSortOptions() {
        sortItems = new SpinnerItem[6];
        sortItems[0] = new SpinnerItem("1", getString(R.string.hotel_search_results_sort_option_default));
        sortItems[1] = new SpinnerItem("2", getString(R.string.hotel_search_results_sort_option_distance));
        sortItems[2] = new SpinnerItem("3", getString(R.string.hotel_search_results_sort_option_preferred));
        sortItems[3] = new SpinnerItem("4", getString(R.string.hotel_search_results_sort_option_price));
        sortItems[4] = new SpinnerItem("5", getString(R.string.hotel_search_results_sort_option_rating));
        sortItems[5] = new SpinnerItem("6", getString(R.string.hotel_search_results_sort_option_suggested));

    }

    // get hotels from database and populate the list items
    private void populateHotelListItemsFromDB() {
        // get from db
        HotelSearchRESTResult hotelSearchRESTResult = TravelUtilHotel.getHotelSearchRESTResult(this, cacheKey);
        List<Hotel> hotels = hotelSearchRESTResult.hotels;
        Log.d(Const.LOG_TAG, " ***** retrieved hotels from TravelUtilHotel.getHotels *****  " + (hotels == null ?
                null :
                hotels.size()));

        // populate list items
        if (hotels != null && hotels.size() > 0) {
            // get the benchmarks from database
            benchmarksCollection = hotelSearchRESTResult.benchmarksCollection;

            hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(hotels.size());
            retrieveFromDB = true;
            for (Hotel hotel : hotels) {
                HotelSearchResultListItem item = new HotelSearchResultListItem(hotel);
                hotelListItemsToSort.add(item);
            }
            showResults();
            hotelSearchRESTResultFrag.hideProgressBar();
        } else {
            // Initialize the loader.
            lm = getLoaderManager();
            searchWorkflowStartTime = System.currentTimeMillis();
            lm.initLoader(HOTEL_SEARCH_LIST_LOADER_ID, null, hotelSearchRESTResultLoaderCallbacks);
        }
    }

    // invoked by the layout xml file
    public void showSortOptions(View v) {
        SpinnerDialogFragment dialogFragment = new SpinnerDialogFragment(R.string.general_sort, sortItems);
        if (curSortItem != null) {
            dialogFragment.curSpinnerItemId = curSortItem.id;
        } else {
            // default to 'default' i.e. server sort or 'distance'
            dialogFragment.curSpinnerItemId = sortItems[0].id;
        }
        dialogFragment.show(getFragmentManager(), FRAGMENT_SORT_ITEMS);
    }

    @Override
    public void onSpinnerItemSelected(SpinnerItem selectedSpinnerItem, String fragmentTagName) {
        if (fragmentTagName.equals(FRAGMENT_SORT_ITEMS)) {
            curSortItem = selectedSpinnerItem;
            sortResults(curSortItem.id);
        }
    }

    // sortOrder  - SpinnerItem Id
    private void sortResults(String sortOrder) {
        String toastMessage = null;
        switch (sortOrder) {
            case "2":
                sortByDistance(hotelListItemsToSort);
                toastMessage = getText(R.string.hotel_search_results_sorted_by_distance).toString();
                break;
            case "3":
                sortByPreference(hotelListItemsToSort);
                toastMessage = getText(R.string.hotel_search_results_sorted_by_preference).toString();
                break;
            case "4":
                sortByCheapestRoomPrice(hotelListItemsToSort);
                toastMessage = getText(R.string.hotel_search_results_sorted_by_price).toString();
                break;
            case "5":
                sortByStarRating(hotelListItemsToSort);
                toastMessage = getText(R.string.hotel_search_results_sorted_by_rating).toString();
                break;
            case "6":
                sortBySuggestion(hotelListItemsToSort);
                toastMessage = getText(R.string.hotel_search_results_sorted_by_suggestion).toString();
                break;
            default:
                // default sort that came from server
                defaultSort();
                toastMessage = getText(R.string.hotel_search_results_sorted_by_default).toString();
        }
        // refresh the UI with the updated hotelListItemsTemp
        updateResultsFragmentUI(hotelListItemsToSort, toastMessage);
    }

    // MOB-24049 - Retrieve the 'default' server sort order that was saved in the cache
    protected void defaultSort() {
        // apply the filter on the items if filter option already exists
        List<HotelSearchResultListItem> hotelListItemsTemp = filter(starRating, distance, nameContaining,
                serverSortedHotelListItemsCache);

        if (hotelListItemsTemp == null) {
            // no filter available, hence set to the cached list
            hotelListItemsTemp = serverSortedHotelListItemsCache;
        }

        // now set the filtered set
        if (hotelListItemsToSort != null) {
            hotelListItemsToSort.clear();
        } else {
            hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(hotelListItemsTemp.size());
        }
        hotelListItemsToSort.addAll(hotelListItemsTemp);

        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SORT + " - " + Flurry.EVENT_LABEL_DEFAULT);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SORT, Flurry.EVENT_LABEL_DEFAULT);
    }

    /**
     * Sort the list of hotels primarily by distance, secondarily by price
     */
    protected void sortByDistance(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.DISTANCE,
                HotelComparator.CompareOrder.ASCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SORT + " - " + Flurry.EVENT_LABEL_DISTANCE);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SORT, Flurry.EVENT_LABEL_DISTANCE);
    }

    /**
     * Sorts the list of hotels primarily by preference, secondarily by price.
     */
    protected void sortByPreference(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.PREFERENCE);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SORT + " - " + Flurry.EVENT_LABEL_PREFERRED);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SORT, Flurry.EVENT_LABEL_PREFERRED);
    }

    /**
     * Sort the list of hotels primarily by price, secondarily by distance.
     */
    protected void sortByCheapestRoomPrice(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.DISTANCE,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SORT + " - " + Flurry.EVENT_LABEL_PRICE);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SORT, Flurry.EVENT_LABEL_PRICE);
    }

    /**
     * Sort the list of hotels primarily by star rating, secondarily by price.
     */
    protected void sortByStarRating(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.STAR_RATING,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SORT + " - " + Flurry.EVENT_LABEL_RATING);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SORT, Flurry.EVENT_LABEL_RATING);
    }

    /**
     * Will perform first a primary sort, then secondary sort over the current hotel list.
     *
     * @param primarySort   the primary sort.
     * @param secondarySort the secondary sort.
     */
    protected void sortByComparator(Comparator<Hotel> primarySort, Comparator<Hotel> secondarySort,
                                    List<HotelSearchResultListItem> hotelListItemsToSort) {
        List<Hotel> hotels = null;

        // Populate list of hotels with availability error codes that need to be moved to the bottom of the list
        List<HotelSearchResultListItem> hotelListItemsToDisplayAtbottom = null;

        Map<Hotel, HotelSearchResultListItem> hotelSortMap = new HashMap<Hotel, HotelSearchResultListItem>();
        if (hotelListItemsToSort != null) {
            hotelListItemsToDisplayAtbottom = new ArrayList<HotelSearchResultListItem>();
            // Create a list of 'Hotel' objects for sorting by
            // 'primarySort' and 'secondarySort'.
            hotels = new ArrayList<Hotel>(hotelListItemsToSort.size());
            for (HotelSearchResultListItem hotelListItem : hotelListItemsToSort) {

                if (hotelListItem.getHotel().availabilityErrorCode != null) {
                    // this list item should be shown at the bottom
                    hotelListItemsToDisplayAtbottom.add(hotelListItem);
                    continue;
                }

                hotels.add(hotelListItem.getHotel());
                // Map is populated so that after 'Hotel' items are
                // sorted, that a new list of
                // 'HotelSearchResultListItem' can be quickly produced from the sorted
                // list of 'Hotel' objects.
                hotelSortMap.put(hotelListItem.getHotel(), hotelListItem);
            }
        }
        if (hotels != null && hotels.size() > 0) {
            // First, place into separate lists based on 'primarySort'.
            TreeMap<Hotel, List<Hotel>> hotelListMap = new TreeMap<Hotel, List<Hotel>>(primarySort);
            for (Hotel hotel : hotels) {
                List<Hotel> subList = hotelListMap.get(hotel);
                if (subList == null) {
                    subList = new ArrayList<Hotel>();
                    hotelListMap.put(hotel, subList);
                }
                subList.add(hotel);
            }
            // Second, iterate over the lists of hotels, sort each list
            // based on secondarySort.
            List<Hotel> listItems = new ArrayList<Hotel>();
            Iterator<Hotel> hotelChListIter = hotelListMap.keySet().iterator();
            while (hotelChListIter.hasNext()) {
                Hotel key = hotelChListIter.next();
                if (key != null) {
                    List<Hotel> chList = hotelListMap.get(key);
                    if (chList != null) {
                        Collections.sort(chList, secondarySort);
                        listItems.addAll(chList);
                    }
                }
            }
            // Produce a new list of 'HotelSearchResultListItem' objects based on
            // iterating through 'listItems'
            // and pulling 'HotelSearchResultListItem' objects from 'hotelSortMap'.
            if (hotelListItemsToSort != null) {
                hotelListItemsToSort.clear();
            } else {
                hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(listItems.size());
            }

            for (Hotel hotel : listItems) {
                hotelListItemsToSort.add(hotelSortMap.get(hotel));
            }

            // Add the list of hotels with availability error codes to the bottom of the list
            hotelListItemsToSort.addAll(hotelListItemsToDisplayAtbottom);
        }
    }

    /**
     * Sort the list of hotels primarily by suggestion, secondarily by preference.
     */
    protected void sortBySuggestion(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.RECOMMENDATION,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.PREFERENCE,
                HotelComparator.CompareOrder.DESCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SORT + " - " + Flurry.EVENT_LABEL_SUGGESTED);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SORT, Flurry.EVENT_LABEL_SUGGESTED);

    }

    @Override
    public void filterResults(String starRating, Double distance, String nameContaining) {
        Log.d(Const.LOG_TAG, "**** HotelSearchAndResultActivity.filterResults ****** starRating : " + starRating
                + " **** distance : " + distance + " **** nameContaining : " + nameContaining + " *** hotelListItems : "
                + hotelListItems);

        this.starRating = starRating;
        this.distance = distance;
        this.nameContaining = nameContaining;

        List<HotelSearchResultListItem> hotelListItemsTemp = filter(starRating, distance, nameContaining,
                hotelListItems);

        if (hotelListItemsTemp != null) {
            if (hotelListItemsTemp.size() == 0) {
                Toast.makeText(this, "No hotels found", Toast.LENGTH_SHORT).show();
            } else {
                // update the fragment with new list items
                updateResultsFragmentUI(hotelListItemsTemp, null);


                // set hotel list items for sort - this is the set to be sorted when sort option chosen and not the original full set
                hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(hotelListItemsTemp.size());
                hotelListItemsToSort.addAll(hotelListItemsTemp);

                Log.d(Const.LOG_TAG,
                        " ***** invoking the hotelSearchRESTResultFrag.updateUI with filtered list of hotelListItems = *****  "
                                + (hotelListItemsTemp == null ? null : hotelListItemsTemp.size()));
            }
        } else {
            // update the fragment with full set of list items
            updateResultsFragmentUI(hotelListItems, "No hotels found");

            // set hotel list items for sort - this is the full set to be sorted
            hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(hotelListItems.size());
            hotelListItemsToSort.addAll(hotelListItems);

            Log.d(Const.LOG_TAG,
                    " ***** invoking the hotelSearchRESTResultFrag.updateUI with full list of hotelListItems = *****  "
                            + (hotelListItems == null ? null : hotelListItems.size()));
        }

        // bring the results fragment to front
        getFragmentManager().popBackStack();

        // Log GA tracking
        trackViewedHotels(hotelListItemsToSort);
    }


    private List<HotelSearchResultListItem> filter(String starRating, Double distance, String nameContaining,
                                                   List<HotelSearchResultListItem> itemsToSort) {
        boolean starRatingFilterEnabled = (starRating == null ? false : true); // assuming ALL is null
        boolean distanceFilterEnabled = (distance == null ? false : true); // assuming ALL is null
        boolean nameContainingFilterEnabled = (nameContaining == null ? false : true);

        List<HotelSearchResultListItem> hotelListItemsTemp = null;

        if ((starRatingFilterEnabled || distanceFilterEnabled || nameContainingFilterEnabled)) {
            // list to store the filtered items
            hotelListItemsTemp = new ArrayList<HotelSearchResultListItem>();

            // list to store items to be filtered out from the full list
            List<HotelSearchResultListItem> itemsToBeRemoved = new ArrayList<HotelSearchResultListItem>();

            // get the full set of hotel list items
            hotelListItemsTemp.addAll(itemsToSort);

            for (HotelSearchResultListItem listItem : hotelListItemsTemp) {

                // star rating
                if (starRatingFilterEnabled) {
                    if (listItem.getHotel().preferences != null && listItem.getHotel().preferences.starRating != null
                            && listItem.getHotel().preferences.starRating.trim().length() > 0) {
                        if (Integer.parseInt(starRating) > Integer
                                .parseInt(listItem.getHotel().preferences.starRating)) {
                            itemsToBeRemoved.add(listItem);
                            continue;
                        }
                    } else {
                        itemsToBeRemoved.add(listItem);
                        continue;
                    }
                }

                // distance
                if (distanceFilterEnabled && listItem.getHotel().distance > distance) {
                    itemsToBeRemoved.add(listItem);
                    continue;
                }

                // name containing
                if (nameContainingFilterEnabled && (listItem.getHotel().name == null || !listItem.getHotel().name
                        .toLowerCase().contains(nameContaining.toLowerCase()))) {
                    itemsToBeRemoved.add(listItem);
                    continue;
                }
            }

            // now remove the items that are not part of the filter criteria
            hotelListItemsTemp.removeAll(itemsToBeRemoved);

            // start of GA tracking
            StringBuilder filterEventLabel = new StringBuilder();
            if (starRatingFilterEnabled) {
                filterEventLabel.append(Flurry.EVENT_LABEL_STAR_RATING);
            }
            if (distanceFilterEnabled) {
                if (filterEventLabel.length() > 0) {
                    filterEventLabel.append(", ");
                }
                filterEventLabel.append(Flurry.EVENT_LABEL_DISTANCE);
            }
            if (nameContainingFilterEnabled) {
                if (filterEventLabel.length() > 0) {
                    filterEventLabel.append(", ");
                }
                filterEventLabel.append(Flurry.EVENT_LABEL_WITH_NAMES_CONTAINING);
            }
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_FILTER + " - " + filterEventLabel.toString());
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_FILTER, filterEventLabel.toString());
            // end of GA tracking
        }
        return hotelListItemsTemp;
    }

    @Override
    public void onFilterClicked() {
        if (!hotelSearchRESTResultFrag.progressbarVisible) {
            // show the filter options fragment
            filterFrag = (HotelSearchResultFilterFragment) getFragmentManager()
                    .findFragmentByTag(FRAGMENT_SEARCH_RESULT_FILTER);
            if (filterFrag == null) {
                filterFrag = new HotelSearchResultFilterFragment();
            }

            if (distanceUnit.equalsIgnoreCase("K")) {
                filterFrag.setDistanceUnitInKm(true);
            }

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(hotelSearchRESTResultFrag);
            ft.add(R.id.container, filterFrag, FRAGMENT_SEARCH_RESULT_FILTER);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void onMapsClicked() {
        if (isOffline) {
            showOfflineDialog();
        } else if (!hotelSearchRESTResultFrag.progressbarVisible) {
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_MAP_NAVBAR_ALL_HOTELS);
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_MAP_NAVBAR_ALL_HOTELS);
            //int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            mapFragment = (HotelSearchResultMapFragment) getFragmentManager()
                    .findFragmentByTag(FRAGMENT_SEARCH_RESULT_MAP);
            //resultCode == ConnectionResult.SUCCESS &&
            if (listItemAdapater != null && listItemAdapater.getItems() != null) {
                List<HotelSearchResultListItem> hotelList = listItemAdapater.getItems();

                if (mapFragment == null) {
                    mapFragment = new HotelSearchResultMapFragment();
                }
                Bundle args = new Bundle();
                args.putSerializable(Const.EXTRA_HOTELS_LIST, (Serializable) hotelList);
                args.putString(Const.EXTRA_TRAVEL_LATITUDE, latitude.toString());
                args.putString(Const.EXTRA_TRAVEL_LONGITUDE, longitude.toString());
                args.putBoolean(Const.EXTRA_TRAVEL_SEARCH_NEAR_ME, searchNearMe);
                mapFragment.setArguments(args);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.hide(hotelSearchRESTResultFrag);

                // if (mapFragment != null && !mapFragment.isAdded()) {
                ft.add(R.id.container, mapFragment, FRAGMENT_SEARCH_RESULT_MAP);
                ft.addToBackStack(null);
                ft.commit();
                //   }

            } else {
                Toast.makeText(this, R.string.map_unavailable, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.map_loading, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onHeaderClicked() {
        Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_SEARCH_CRITERIA_HEADER_TAPPED);
        EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_SEARCH_CRITERIA_HEADER_TAPPED);

        finish();
    }

    @Override
    public void hotelListItemClicked(HotelSearchResultListItem itemClicked) {

        if (isOffline) {
            showOfflineDialog();
        } else if (itemClicked != null) {
            selectedHotelListItem = itemClicked;
            Hotel hotelSelected = selectedHotelListItem.getHotel();
            if (hotelSelected != null && hotelSelected.ratesURL != null
                    && hotelSelected.availabilityErrorCode == null) {

                // GA event logging
                String[] paramKeys = null;
                String[] paramValues = null;
                if (hotelSelected.recommended != null && hotelSelected.recommended.getSuggestedCategory() != null) {
                    paramKeys = new String[3];
                    paramValues = new String[3];
                    paramKeys[1] = Flurry.EVENT_LABEL_HOTEL_RECOMMENDED;
                    paramValues[1] = Flurry.PARAM_VALUE_YES.toUpperCase();
                    paramKeys[2] = Flurry.EVENT_LABEL_HOTEL_RECOMMENDED_TYPE;
                    paramValues[2] = hotelSelected.recommended.getSuggestedCategory(); //CompanyFavourite etc
                } else {
                    paramKeys = new String[2];
                    paramValues = new String[2];
                    paramKeys[1] = Flurry.EVENT_LABEL_HOTEL_RECOMMENDED;
                    paramValues[1] = Flurry.PARAM_VALUE_NO.toUpperCase();
                }
                paramKeys[0] = Flurry.EVENT_LABEL_HOTEL_PROPERTY_ID;
                paramValues[0] = getFormattedPropertyIdFromPropertyIds(hotelSelected.propertyIds);

                Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_HOTEL_SELECTED + " - " + paramKeys + " - " + paramValues);
                EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_HOTEL_SELECTED, paramKeys, paramValues);
                // end of GA logging

                hotelSelected.showNearMe = searchNearMe;
                // Determine if the hotel details are already in our in-memory
                // cache, if so, then
                // re-use them. A request to update will be made in the
                // background.

                if (retrieveFromDB) {

                    // DB call
                    long id = hotelSelected._id;

                    hotelSelected.rates = TravelUtilHotel.getHotelRateDetails(this, id);
                    hotelSelected.imagePairs = TravelUtilHotel.getHotelImagePairs(this, id);
                    violations = TravelUtilHotel
                            .getHotelViolations(getApplicationContext(), null, (int) hotelSelected.search_id);
                }
                if (hotelSelected.rates != null && hotelSelected.rates.size() > 0) {
                    viewHotelChoiceDetails();
                } else if (hotelSelected.lowestRate == null && hotelSelected.ratesURL.href != null) {

                    hotelSearchRESTResultFrag.showProgressBar(true);
                    lm = getLoaderManager();
                    lm.restartLoader(HOTEL_RATES_LOADER_ID, null, hotelRatesRESTResultLoaderCallbacks);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_rooms, Toast.LENGTH_SHORT).show();
                if (mapFragment != null) {
                    mapFragment.hideProgressBar();
                }

            }

        }

    }

    private void viewHotelChoiceDetails() {

        Hotel hotel = selectedHotelListItem.getHotel();
        String searchId = TravelUtilHotel.getHotelSearchResultId(this, cacheKey);
        if (searchId != null) {
            hotel.search_id = Long.valueOf(searchId);
        }
        Intent i = new Intent(this, HotelChoiceDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Const.EXTRA_HOTELS_DETAILS, (Serializable) selectedHotelListItem);
        i.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION, hotelSearchRESTResultFrag.location);
        i.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_OF_STAY,
                hotelSearchRESTResultFrag.durationOfStayForDisplayInHeader);
        i.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DURATION_NUM_OF_NIGHTS, numberOfNights);
        i.putExtra("violationReasons", violationReasons);
        i.putExtra("ruleViolationExplanationRequired", ruleViolationExplanationRequired);
        i.putExtra("currentTripId", currentTripId);
        i.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_SHOW_GDS_NAME, showGDSName);
        if (travelCustomFieldsConfig != null) {
            i.putExtra("travelCustomFieldsConfig", travelCustomFieldsConfig);
        }
        if (customTravelText != null) {
            i.putExtra("customTravelText", customTravelText);
        }
        i.putExtra("suggestedAvailable", suggestedAvailable);
        if (updatedViolations != null && updatedViolations.size() > 0) {
            bundle.putSerializable("updatedViolations", (Serializable) updatedViolations);
        }
        bundle.putSerializable("violations", (Serializable) violations);
        // i.putExtra("searchId", searchId);
        i.putExtras(bundle);
        // startActivity(i);
        selectedHotelListItem = null;
        if (mapFragment != null) {
            mapFragment.hideProgressBar();
        }
        startActivityForResult(i, Const.REQUEST_CODE_BOOK_HOTEL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Const.REQUEST_CODE_BOOK_HOTEL: {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    Log.d(com.concur.mobile.platform.util.Const.LOG_TAG,
                            "\n\n\n ****** HotelSearchAndResultActivity onActivityResult with result code : " + resultCode);
                    finish();
                }
                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    } // onActivityResult()

    // Will sort the results depending on the type of search and invoked the fragment to display the sorted results
    public void showResults() {

        if (hotelListItems == null) {
            hotelListItems = new ArrayList<HotelSearchResultListItem>(hotelListItemsToSort.size());
        } else {
            hotelListItems.clear();
        }

        //MOB-24365 - show the server sorted results as it is. cache this result set to be used to sort on 'default' option rather than retrieving again from database
        serverSortedHotelListItemsCache = new ArrayList<HotelSearchResultListItem>(hotelListItemsToSort.size());
        serverSortedHotelListItemsCache.addAll(hotelListItemsToSort);

        hotelListItems.addAll(hotelListItemsToSort);

        // init and show the price to beat
        initPriceToBeat();

        // refresh the UI with the updated hotelListItems
        updateResultsFragmentUI(hotelListItemsToSort, null);

        hotelSearchRESTResultFrag.getHotelListView().setAlpha(1);
        if (callFromDB && listItemSelectedPosition != -1) {
            // show the list item at the center of the list view
            int h1 = hotelSearchRESTResultFrag.getHotelListView().getHeight();
            hotelSearchRESTResultFrag.getHotelListView()
                    .setSelectionFromTop(listItemSelectedPosition, h1 / 2);// - h2/2);
        }
        hotelSearchRESTResultFrag.showSortAndFilterIconsInFooter();

        if (hotelSearchRESTResultFrag.getHotelListView() != null) {
            hotelSearchRESTResultFrag.getHotelListView().setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listItemSelectedPosition = position;
                    hotelListItemClicked((HotelSearchResultListItem) parent.getItemAtPosition(position));
                }
            });
        }

        // Log GA tracking
        trackViewedHotels(hotelListItemsToSort);

    }

    private void initPriceToBeat() {
        if (benchmarksCollection != null && benchmarksCollection.benchmarks != null) {
            ArrayList<Double> priceToBeatValues = new ArrayList<Double>(benchmarksCollection.benchmarks.size());
            Double priceToBeatValue = null;
            for (HotelBenchmark hotelBenchmark : benchmarksCollection.benchmarks) {
                priceToBeatValue = hotelBenchmark.price;
                if (priceToBeatValue != null) {
                    priceToBeatValues.add(priceToBeatValue);
                }
            }
            if (priceToBeatValues.size() > 0) {
                String currencyCode = benchmarksCollection.benchmarks.get(0).crnCode;
                if (currencyCode == null) {
                    currencyCode = hotelListItems.get(0).getHotel().currencyCode;
                }
                Double maxPriceToBeatValue = Collections.max(priceToBeatValues);
                priceToBeatValue = Collections.min(priceToBeatValues);
                String formattedMinBenchmarkPrice = null;
                View priceToBeatView = hotelSearchRESTResultFrag.mainView.findViewById(R.id.priceToBeatView);
                priceToBeatView.setVisibility(View.VISIBLE);
                final String priceToBeatRangeText;
                if (priceToBeatValue == maxPriceToBeatValue) {
                    // no price range
                    formattedMinBenchmarkPrice = FormatUtil
                            .formatAmountWithNoDecimals(priceToBeatValue, this.getResources().getConfiguration().locale,
                                    currencyCode, true, true);
                    priceToBeatRangeText = formattedMinBenchmarkPrice;
                } else {
                    // price range available
                    formattedMinBenchmarkPrice = FormatUtil
                            .formatAmountWithNoDecimals(priceToBeatValue, this.getResources().getConfiguration().locale,
                                    currencyCode, true, true);
                    String formattedMaxBenchmarkPrice = FormatUtil.formatAmountWithNoDecimals(maxPriceToBeatValue,
                            this.getResources().getConfiguration().locale, currencyCode, true, true);

                    priceToBeatRangeText = Format.localizeText(this, R.string.price_to_beat_range,
                            new Object[]{formattedMinBenchmarkPrice, formattedMaxBenchmarkPrice});
                }
                ((TextView) (priceToBeatView.findViewById(R.id.priceToBeatText)))
                        .setText(getString(R.string.price_to_beat_label).toUpperCase() + " : " + priceToBeatRangeText);
                // init the onclick event
                priceToBeatView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showPriceToBeatFragment(priceToBeatRangeText);
                    }
                });
            }
        }
    } //end of price to beat

    private void showPriceToBeatFragment(String priceToBeatRangeText) {
        List<HotelBenchmark> benchmarks = benchmarksCollection.benchmarks;
        if (benchmarks != null && benchmarks.size() > 0) {
            List<HotelBenchmarkListItem> benchmarkListItems = new ArrayList<HotelBenchmarkListItem>(benchmarks.size());
            for (HotelBenchmark bm : benchmarks) {
                benchmarkListItems.add(new HotelBenchmarkListItem(bm));
            }

            // prepare the adapter
            ListItemAdapter<HotelBenchmarkListItem> listItemAdapater = new ListItemAdapter<HotelBenchmarkListItem>(this,
                    benchmarkListItems);

            // initiate the fragment and show
            FragmentManager fm = getFragmentManager();
            HotelBenchmarksFragment detailsFragment = (HotelBenchmarksFragment) fm
                    .findFragmentByTag(PRICE_TO_BEAT_DETAILS_FRAGMENT);
            if (detailsFragment == null) {
                detailsFragment = new HotelBenchmarksFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.commit();
            }
            detailsFragment.priceToBeatRangeText = priceToBeatRangeText;
            detailsFragment.listItemAdapter = listItemAdapater;
            detailsFragment.show(fm, PRICE_TO_BEAT_DETAILS_FRAGMENT);
        }
    }

    // invokes the updateUI call on the fragment
    private void updateResultsFragmentUI(List<HotelSearchResultListItem> hotelListItemsToUpdate, String toastMessage) {
        listItemAdapater.setItems(hotelListItemsToUpdate);
        hotelSearchRESTResultFrag
                .updateUI(listItemAdapater, hotelListItems, hotelListItemsToUpdate.size(), toastMessage);
    }

    private void callPollLoader() {
        // will invoke the create loader
        lm.restartLoader(HOTEL_POLL_LIST_LOADER_ID, null, hotelSearchRESTResultLoaderCallbacks);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and stop any outstanding
        // request of async task
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishActivity(Const.REQUEST_CODE_BACK_BUTTON_PRESSED);
        }
        return super.onKeyDown(keyCode, event);
    }

    // track hotels viewed
    private void trackViewedHotels(List<HotelSearchResultListItem> itemsToSort) {
        for (HotelSearchResultListItem item : itemsToSort) {

            String[] paramKeys = new String[1];
            String[] paramValues = new String[1];
            if (item.getHotel().recommended != null) {
                paramKeys = new String[2];
                paramValues = new String[2];

                // recommendation score
                paramKeys[1] = Flurry.EVENT_LABEL_HOTEL_RECOMMENDATION_SCORE;
                paramValues[1] = Double.toString(item.getHotel().recommended.totalScore);

                if (!suggestedAvailable) {
                    suggestedAvailable = (item.getHotel().recommended.getSuggestedCategory() == null ? false : true);
                }
            }

            // property id
            paramKeys[0] = Flurry.EVENT_LABEL_HOTEL_PROPERTY_ID;
            paramValues[0] = getFormattedPropertyIdFromPropertyIds(item.getHotel().propertyIds);
            Log.d(Const.LOG_TAG, CLS_TAG + "*********************** EventTracker - " + Flurry.EVENT_CATEGORY_TRAVEL_HOTEL + " - " + Flurry.EVENT_ACTION_TRAVEL_VIEWED_HOTELS + " - " + paramKeys.toString() + " - " + paramValues.toString());
            EventTracker.INSTANCE.eventTrack(Flurry.EVENT_CATEGORY_TRAVEL_HOTEL, Flurry.EVENT_ACTION_TRAVEL_VIEWED_HOTELS,
                    paramKeys, paramValues);
        }
    }

    // returns property id, source and vendor id
    private String getFormattedPropertyIdFromPropertyIds(List<HotelPropertyId> hotelPropertyIds) {
        StringBuilder sbr = new StringBuilder();
        if (hotelPropertyIds != null && hotelPropertyIds.size() > 0) {
            // just need to return the first property id, no need to check for NorthStar or GDS sources
            sbr.append("{");
            sbr.append("propertyId=" + hotelPropertyIds.get(0).propertyId);
            sbr.append(", source=" + hotelPropertyIds.get(0).source);
            sbr.append(", vendorId=" + (hotelPropertyIds.get(0).vendorId == null ? "" : hotelPropertyIds.get(0).vendorId));
            sbr.append("}");
        }
        return sbr.toString();
    }

}
