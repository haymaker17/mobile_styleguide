package com.concur.mobile.platform.ui.travel.hotel.activity;

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

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.travel.provider.TravelUtilHotel;
import com.concur.mobile.platform.travel.search.hotel.Hotel;
import com.concur.mobile.platform.travel.search.hotel.HotelComparator;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchPollResultLoader;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchRESTResult;
import com.concur.mobile.platform.travel.search.hotel.HotelSearchResultLoader;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFilterFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFilterFragment.HotelSearchResultsFilterListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFragment;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultFragment.HotelSearchResultsFragmentListener;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelSearchResultListItem;
import com.concur.mobile.platform.ui.travel.hotel.maps.ShowMaps;
import com.concur.mobile.platform.ui.travel.util.Const;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Activity to launch the Jarvis Hotel Search
 * 
 * @author RatanK
 * 
 */
public class HotelSearchAndResultActivity extends Activity implements OnMenuItemClickListener,
        HotelSearchResultsFilterListener, HotelSearchResultsFragmentListener,
        LoaderManager.LoaderCallbacks<HotelSearchRESTResult> {

    public static final String FRAGMENT_SEARCH_RESULT = "FRAGMENT_SEARCH_RESULT";
    private static final String FRAGMENT_SEARCH_RESULT_FILTER = "FRAGMENT_SEARCH_RESULT_FILTER";

    private static final int HOTEL_SEARCH_LIST_LOADER_ID = 0;
    private static final int HOTEL_POLL_LIST_LOADER_ID = 1;

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

    // set of hotel list items to sort
    private List<HotelSearchResultListItem> hotelListItemsToSort;

    private Double longitude;
    private Double latitude;
    private Calendar checkInDate;
    private Calendar checkOutDate;
    private String location;
    private boolean fromLocationSearch;
    private boolean searchCriteriaChanged;
    private String distanceUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_search_and_result);

        Intent intent = getIntent();

        location = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);

        // searchCriteriaChanged will be false if it just came back from search criteria screen without any changes to the
        // previous searched criteria. in such
        // case, we can get the results from db and show instead of doing the server call again
        searchCriteriaChanged = intent.getBooleanExtra("searchCriteriaChanged", true);

        if (searchCriteriaChanged) {

            checkInDate = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
            checkOutDate = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);
            latitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE));
            longitude = Double.valueOf(intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE));
            fromLocationSearch = intent.getBooleanExtra(Const.EXTRA_LOCATION_SEARCH_MODE_USED, false);
            distanceUnit = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);

            // Initialize the loader.
            lm = getLoaderManager();
            searchWorkflowStartTime = System.currentTimeMillis();
            lm.initLoader(HOTEL_SEARCH_LIST_LOADER_ID, null, this);
        }

        hotelSearchRESTResultFrag = (HotelSearchResultFragment) getFragmentManager().findFragmentByTag(
                FRAGMENT_SEARCH_RESULT);

        if (hotelSearchRESTResultFrag == null) {
            hotelSearchRESTResultFrag = new HotelSearchResultFragment();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.container, hotelSearchRESTResultFrag, FRAGMENT_SEARCH_RESULT);
            ft.commit();
        }

        hotelSearchRESTResultFrag.location = location;
        hotelSearchRESTResultFrag.durationOfStayForDisplayInHeader = getIntent().getStringExtra(
                Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN)
                + " - " + intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT);

        listItemAdapater = new ListItemAdapter<HotelSearchResultListItem>(this, hotelListItems);

    }

    @Override
    public void fragmentReady() {
        // signal from fragment that it is loaded
        if (!searchCriteriaChanged) {
            populateHotelListItemsFromDB();
        }
    }

    // get hotels from database and populate the list items
    private void populateHotelListItemsFromDB() {
        // get from db
        List<Hotel> hotels = TravelUtilHotel.getHotels(this);

        Log.d(Const.LOG_TAG, " ***** retrieved hotels from TravelUtilHotel.getHotels *****  "
                + (hotels == null ? null : hotels.size()));

        // populate list items
        if (hotels != null && hotels.size() > 0) {
            hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(hotels.size());
            for (Hotel hotel : hotels) {
                HotelSearchResultListItem item = new HotelSearchResultListItem(hotel);
                hotelListItemsToSort.add(item);
            }
            showResults();
            hotelSearchRESTResultFrag.hideProgressBar();
        } else {
            // hotelSearchRESTResultFrag.showNegativeView();
            Toast.makeText(this, "Hotel Search Failed. Please try again.", Toast.LENGTH_SHORT).show();
        }

    }

    // invoked by the layout xml file
    public void showSortOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.hotel_search_results_sort_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        String toastMessage = null;
        if (item.getItemId() == R.id.item_distance) {
            sortByDistance(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_distance).toString();
        } else if (item.getItemId() == R.id.item_preferred) {
            sortByPreference(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_preference).toString();
        } else if (item.getItemId() == R.id.item_price) {
            sortByCheapestRoomPrice(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_price).toString();
        } else if (item.getItemId() == R.id.item_rating) {
            sortByStarRating(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_rating).toString();
        } else {
            sortBySuggestion(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_suggestion).toString();
        }

        // refresh the UI with the updated hotelListItemsTemp
        updateResultsFragmentUI(hotelListItemsToSort, toastMessage);

        return false;
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
    }

    /**
     * Sorts the list of hotels primarily by preference, secondarily by price.
     */
    protected void sortByPreference(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.PREFERENCE,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
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
    }

    /**
     * Sort the list of hotels primarily by star rating, secondarily by distance.
     */
    protected void sortBySuggestion(List<HotelSearchResultListItem> hotelListItemsToSort) {
        // Construct the primary/secondary sorts.
        Comparator<Hotel> primarySort = new HotelComparator(HotelComparator.CompareField.RECOMMENDATION,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.PREFERENCE,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort, hotelListItemsToSort);
    }

    /**
     * Will perform first a primary sort, then secondary sort over the current hotel list.
     * 
     * @param primarySort
     *            the primary sort.
     * @param secondarySort
     *            the secondary sort.
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

    @Override
    public void filterResults(String starRating, Double distance, String nameContaining) {
        Log.d(Const.LOG_TAG, "**** HotelSearchAndResultActivity.filterResults ****** starRating : " + starRating
                + " **** distance : " + distance + " **** nameContaining : " + nameContaining
                + " *** hotelListItems : " + hotelListItems);

        boolean starRatingFilterEnabled = (starRating == null ? false : true); // assuming ALL is null
        boolean distanceFilterEnabled = (distance == null ? false : true); // assuming ALL is null
        boolean nameContainingFilterEnabled = (nameContaining == null ? false : true);

        if ((starRatingFilterEnabled || distanceFilterEnabled || nameContainingFilterEnabled)) {

            // list to store the filtered items
            List<HotelSearchResultListItem> hotelListItemsTemp = new ArrayList<HotelSearchResultListItem>();

            // list to store items to be filtered out from the full list
            List<HotelSearchResultListItem> itemsToBeRemoved = new ArrayList<HotelSearchResultListItem>();

            // get the full set of hotel list items
            hotelListItemsTemp.addAll(hotelListItems);

            for (HotelSearchResultListItem listItem : hotelListItemsTemp) {

                // star rating
                if (starRatingFilterEnabled) {
                    if (listItem.getHotel().preferences != null
                            && !starRating.equals(listItem.getHotel().preferences.starRating)) {
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
                if (nameContainingFilterEnabled
                        && (listItem.getHotel().name == null || !listItem.getHotel().name.toLowerCase().contains(
                                nameContaining.toLowerCase()))) {
                    itemsToBeRemoved.add(listItem);
                    continue;
                }
            }

            // now remove the items that are not part of the filter criteria
            hotelListItemsTemp.removeAll(itemsToBeRemoved);

            if (hotelListItemsTemp.size() == 0) {
                // hotelSearchRESTResultFrag.showNegativeView();
                Toast.makeText(this, "No hotels found", Toast.LENGTH_SHORT).show();
            } else {
                // update the fragment with new list items
                updateResultsFragmentUI(hotelListItemsTemp, null);
            }

            // set hotel list items for sort - this is the set to be sorted when sort option chosen and not the original full set
            hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>(hotelListItemsTemp.size());
            hotelListItemsToSort.addAll(hotelListItemsTemp);

            Log.d(Const.LOG_TAG,
                    " ***** invoking the hotelSearchRESTResultFrag.updateUI with filtered list of hotelListItems = *****  "
                            + (hotelListItemsTemp == null ? null : hotelListItemsTemp.size()));
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

    }

    @Override
    public void onFilterClicked() {
        // show the filter options fragment
        filterFrag = (HotelSearchResultFilterFragment) getFragmentManager().findFragmentByTag(
                FRAGMENT_SEARCH_RESULT_FILTER);
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

    @Override
    public void onMapsClicked() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        Intent i = new Intent(this, ShowMaps.class);
        if (resultCode == ConnectionResult.SUCCESS && listItemAdapater != null && listItemAdapater.getItems() != null) {
            List<HotelSearchResultListItem> hotelList = listItemAdapater.getItems();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Const.EXTRA_HOTELS_LIST, (Serializable) hotelList);
            i.putExtras(bundle);

            startActivity(i);

        } else {
            Toast.makeText(this, "unavialable to show map", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onHeaderClicked() {
        finish();
    }

    public void hotelListItemClicked(HotelSearchResultListItem itemClicked) {

        if (itemClicked != null) {
            Hotel hotelSelected = itemClicked.getHotel();
            if (hotelSelected != null) {
                // Determine if the hotel details are already in our in-memory
                // cache, if so, then
                // re-use them. A request to update will be made in the
                // background.
                if (hotelSelected.rates == null || hotelSelected.rates.size() == 0) {
                    // no rooms
                    Toast.makeText(this, "No Rooms avialable", Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(this, HotelChoiceDetailsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Const.EXTRA_HOTELS_DETAILS, (Serializable) itemClicked);
                    i.putExtras(bundle);

                    startActivity(i);
                }

            }
        }

    }

    // Will sort the results depending on the type of search and invoked the fragment to display the sorted results
    public void showResults() {
        String toastMessage = null;
        if (fromLocationSearch) {
            sortByDistance(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_distance).toString();
        } else {
            sortBySuggestion(hotelListItemsToSort);
            toastMessage = getText(R.string.hotel_search_results_sorted_by_suggestion).toString();
        }

        if (hotelListItems == null) {
            hotelListItems = new ArrayList<HotelSearchResultListItem>(hotelListItemsToSort.size());
        } else {
            hotelListItems.clear();
        }

        hotelListItems.addAll(hotelListItemsToSort);

        // refresh the UI with the updated hotelListItems
        updateResultsFragmentUI(hotelListItemsToSort, toastMessage);

        hotelSearchRESTResultFrag.getHotelListView().setAlpha(1);
        hotelSearchRESTResultFrag.showSortAndFilterIconsInFooter();

        if (hotelSearchRESTResultFrag.getHotelListView() != null) {
            hotelSearchRESTResultFrag.getHotelListView().setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    hotelListItemClicked((HotelSearchResultListItem) parent.getItemAtPosition(position));
                }
            });
        }

    }

    // invokes the updateUI call on the fragment
    private void updateResultsFragmentUI(List<HotelSearchResultListItem> hotelListItemsToUpdate, String toastMessage) {
        listItemAdapater.setItems(hotelListItemsToUpdate);
        hotelSearchRESTResultFrag.updateUI(listItemAdapater, hotelListItemsToUpdate.size(), toastMessage);
    }

    @Override
    public Loader<HotelSearchRESTResult> onCreateLoader(int id, Bundle bundle) {
        PlatformAsyncTaskLoader<HotelSearchRESTResult> hotelSearchAsyncTaskLoader;
        if (!searchDone && pollingURL != null) {
            // request polling
            fromPolling = true;
            Log.d(Const.LOG_TAG, " ***** creating poll search loader *****  ");
            hotelSearchAsyncTaskLoader = new HotelSearchPollResultLoader(this, checkInDate, checkOutDate, latitude,
                    longitude, 25, distanceUnit, 0, 10, pollingURL);
        } else {
            fromPolling = false;
            // request initial search
            Log.d(Const.LOG_TAG, " ***** creating search loader *****  ");

            // TODO - does this need to be fired in a separate thread?
            // TravelUtilHotel.deleteAllHotelDetails(this);

            hotelSearchAsyncTaskLoader = new HotelSearchResultLoader(this, checkInDate, checkOutDate, latitude,
                    longitude, 25, distanceUnit, 0, 10);
        }
        return hotelSearchAsyncTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<HotelSearchRESTResult> loader, HotelSearchRESTResult hotelSearchResult) {

        List<HotelSearchResultListItem> hotelListItemsFromLoader = new ArrayList<HotelSearchResultListItem>();

        Log.d(Const.LOG_TAG, " ***** searchdone *****  " + searchDone);

        if (searchDone) {
            // skip everything as this could be due to on orientation change or pop back from stack
            Log.d(Const.LOG_TAG, " ***** searchDone true,  hotelListItems = "
                    + (hotelListItems != null ? hotelListItems.size() : 0));
            hotelSearchRESTResultFrag.hideProgressBar();
        } else {
            if (hotelSearchResult == null) {
                hotelSearchRESTResultFrag.hideProgressBar();
                // hotelSearchRESTResultFrag.showNegativeView();
                // TODO handle the search errors
                Toast.makeText(this, "Hotel Search Failed. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                searchDone = hotelSearchResult.searchDone;
                hotelSearchRESTResultFrag.getHotelListView().setAlpha(0.5f);

                if (hotelSearchResult.hotels != null && hotelSearchResult.hotels.size() > 0) {
                    for (Hotel hotel : hotelSearchResult.hotels) {
                        hotel.distanceUnit = hotelSearchResult.distanceUnit;
                        hotel.currencyCode = hotelSearchResult.currency;
                        HotelSearchResultListItem item = new HotelSearchResultListItem(hotel);
                        hotelListItemsFromLoader.add(item);
                    }
                }

                Log.d(Const.LOG_TAG, " ***** hotelSearchResult.searchDone " + searchDone + ",  hotelListItems = "
                        + (hotelListItems != null ? hotelListItems.size() : 0) + ",  hotelListItemsFromLoader = "
                        + (hotelListItemsFromLoader != null ? hotelListItemsFromLoader.size() : 0));

                if (searchDone) {
                    if (hotelListItems == null) {
                        hotelListItems = new ArrayList<HotelSearchResultListItem>();
                    }
                    hotelListItems = hotelListItemsFromLoader;
                    if (hotelListItems != null && hotelListItems.size() > 0) {

                        // set hotel list items that can be sorted
                        hotelListItemsToSort = new ArrayList<HotelSearchResultListItem>();
                        hotelListItemsToSort.addAll(hotelListItems);

                        showResults();
                    }

                    hotelSearchRESTResultFrag.hideProgressBar();

                } else {

                    if (fromPolling) {
                        if (listItemAdapater == null) {
                            Log.e(Const.LOG_TAG, " cannot call refreshUIListItem: listItemAdapater is null!");
                        } else {
                            hotelSearchRESTResultFrag.refreshUIListItems(hotelListItemsFromLoader, listItemAdapater);
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

                            Log.d(Const.LOG_TAG, " in retrieveHotelPricing, time now from start of search " + timeNow
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

    private void callPollLoader() {
        // will invoke the createloader
        lm.restartLoader(HOTEL_POLL_LIST_LOADER_ID, null, this);
    }

    @Override
    public void onLoaderReset(Loader<HotelSearchRESTResult> data) {
        Log.d(Const.LOG_TAG, " ***** loader reset *****  ");
        if (listItemAdapater != null) {
            listItemAdapater.setItems(null);
        }
    }

}
