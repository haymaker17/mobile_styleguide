/**
 * 
 */
package com.concur.mobile.core.travel.hotel.activity;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.dialog.ProgressDialogFragment;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.ServiceRequest;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.travel.hotel.data.HotelChoiceDetail;
import com.concur.mobile.core.travel.hotel.data.HotelComparator;
import com.concur.mobile.core.travel.hotel.service.GetHotels;
import com.concur.mobile.core.travel.hotel.service.GetHotelsPricing;
import com.concur.mobile.core.travel.hotel.service.HotelDetailRequest;
import com.concur.mobile.core.travel.hotel.service.HotelSearchReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListActivity</code> permitting a choice of hotel.
 * 
 * @author AndrewK
 */
public class HotelSearchResults extends TravelBaseActivity implements OnItemClickListener {

    private static final String CLS_TAG = HotelSearchResults.class.getSimpleName();

    private static final String HOTEL_DETAIL_RECEIVER_KEY = "hotel.detail.receiver";
    private static final String HOTEL_SEARCH_RECEIVER_KEY = "hotel.search.receiver";
    private static final String HOTEL_SORT_OPTION_KEY = "hotel.sort.option";
    // private static final String GET_HOTELS_PRICING_RECEIVER = "hotels.pricing.receiver";
    // private static final String GET_LOAD_MORE_HOTELS_RECEIVER = "hotels.load.more.receiver";

    private static final int DIALOG_SORT_OPTION = 1;
    private static final int DIALOG_RETRIEVE_HOTELS = 2;
    private static final int DIALOG_RETRIEVE_HOTELS_FAILED = 3;

    // Reference to the receiver to be notified of the outcome of the hotel
    // detail
    // request.
    private HotelDetailReceiver hotelDetailReceiver;
    // Intent filter used to register the hotel details receiver.
    private IntentFilter hotelDetailFilter;
    // Contains whether the receiver is currently registered.
    boolean hotelDetailReceiverRegistered;

    // Contains a reference to a receiver to handle the result of fetching
    // additional hotels.
    protected HotelSearchReceiver hotelSearchReceiver;
    // Contains a reference to a filter to register the results receiver.
    protected IntentFilter hotelSearchFilter;
    // Contains a reference to an outstanding request to retrieve additional
    // hotels.
    protected ServiceRequest hotelSearchRequest;

    // Contains the last "action status" message from the server.
    private String actionStatusErrorMessage;

    // Contains a reference to the current outstanding hotel details request.
    private HotelDetailRequest hotelDetailRequest;

    // Contains the adapter used to populate the hotel list.
    private ListItemAdapter<HotelChoiceListItem> hotelChoiceListAdapter;

    // Contains a reference to the hotel list.
    private ListView hotelList;

    // Contains a reference to a view object used as a list footer.
    private View moreHotelsFooter;

    // A reference to the list adapter used to provide sort options.
    private HotelSortOptionListAdapter sortOptionListAdapter;

    // Contains the currently selected sort option.
    private SortCriteria currentSortOption;

    // An enum defining the hotel choice list sort options.
    protected enum SortCriteria {
        PRICE, // price ascending.
        DISTANCE, // distance ascending.
        STAR_RATING, // star rating ascending.
        VENDOR_NAME, // name.
        PREFERRED_VENDOR, // preferred vendor.
        RECOMMENDATION // recommendations
    };

    // start of hotel streaming
    private Calendar checkInDateCal;
    private Calendar checkOutDateCal;
    private String latitude;
    private String longitude;
    private String distanceId;
    private String distanceUnitId;
    private String namesContaining;
    private String pollingId;

    // flag to be used in the UI
    private boolean showReadOnlyList;

    private ArrayList<HotelChoice> hotelChoicesToBeUpdated;
    // private ArrayList<HotelChoice> choices;
    private ProgressDialogFragment searchingRatesProgressFrag;
    private AlertDialogFragment searchingRatesFailedFrag;

    private BaseAsyncResultReceiver hotelsPricingReceiver;
    private BaseAsyncResultReceiver hotelResultsReceiver;
    private ProgressDialog progDlgRetrieveMoreHotels;

    private GetHotelsPricing getHotelsPricing;
    private long searchWorkflowStartTime;
    private MenuItem sortItem;

    // end of hotel streaming

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.travel_hotel_search_results);

        // Restore any saved sorting option.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(HOTEL_SORT_OPTION_KEY)) {
                String sortOptionStr = savedInstanceState.getString(HOTEL_SORT_OPTION_KEY);
                if (sortOptionStr != null) {
                    sortOptionStr = sortOptionStr.trim();
                    currentSortOption = SortCriteria.valueOf(sortOptionStr);
                }
            }
        }

        configureSearchCriteriaText();

        hotelList = (ListView) findViewById(android.R.id.list);

        if (hotelList == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate list view!");
        } else {
            if (orientationChange) {
                showReadOnlyList = (Boolean) retainer.get(Const.EXTRA_TRAVEL_HOTEL_SHOW_READ_ONLY_LIST);
                searchWorkflowStartTime = (Long) retainer.get(Const.EXTRA_TRAVEL_HOTEL_SEARCH_WORKFLOW_START_TIME);
            } else {
                showReadOnlyList = getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_HOTEL_SHOW_READ_ONLY_LIST, false);
                searchWorkflowStartTime = getIntent().getLongExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_WORKFLOW_START_TIME,
                        System.currentTimeMillis());
            }
            if (orientationChange && searchingRatesProgressFrag != null
                    && searchingRatesProgressFrag.getDialog() != null) {
                // in this case, orientation changed and pricing retrieval is still happening. so, do not refresh the view as the
                // Pricing listener will invoke the refresh
            } else {
                initUI(!showReadOnlyList);
                if (showReadOnlyList && !orientationChange) {
                    getPricingReqParams();
                    retrieveHotelPricing();
                }
            }
        }

        // Restore any retained receivers.
        restoreReceivers();
    }

    /**
     * 
     * @param showClickableList
     *            - true - if user can use the UI i.e. can do sort, scroll, view rates, go into hotel details
     */
    private void initUI(boolean showClickableList) {
        HotelSearchReply results = ((ConcurCore) getApplication()).getHotelSearchResults();

        if (results != null) {
            // Construct a footer view to be used with the results list if not
            // all the results have been downloaded.
            // do not need this footer if pricing is still being retrieved
            if (showClickableList && (results.length < results.totalCount)) {
                // Create the main row container and static elements
                if (hotelList.getFooterViewsCount() == 0) {
                    // load more hotels view is not added, hence add
                    LayoutInflater inflater = LayoutInflater.from(this);
                    moreHotelsFooter = inflater.inflate(R.layout.hotel_search_load_more_hotels, null);
                    hotelList.addFooterView(moreHotelsFooter, null, true);
                    moreHotelsFooter.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (!ConcurCore.isConnected()) {
                                showDialog(Const.DIALOG_NO_CONNECTIVITY);
                            } else {
                                sendHotelSearchRequest();
                            }
                        }
                    });
                }
            }

            List<HotelChoiceListItem> hotelListItems = new ArrayList<HotelChoiceListItem>(results.length);
            if (results.hotelChoices != null) {
                for (HotelChoice hotelChoice : results.hotelChoices) {
                    hotelListItems.add(new HotelChoiceListItem(hotelChoice));
                }
            }

            hotelChoiceListAdapter = new ListItemAdapter<HotelChoiceListItem>(this, hotelListItems);

            if (imageCacheReceiver == null) {
                // Prior to setting the adapter on the view, init the image cache
                // receiver to handle
                // updating the list based on images downloaded asychronously.
                imageCacheReceiver = new ImageCacheReceiver<HotelChoiceListItem>(hotelChoiceListAdapter, hotelList);
                registerImageCacheReceiver();
            }

            hotelList.setAdapter(hotelChoiceListAdapter);

            // not needed during pricing retrieval
            if (showClickableList) {
                // Set the screen title
                getSupportActionBar().setTitle(R.string.hotel_results_title);

                initHotelTravelPointsHeader();

                // MOB-16531 - Position the first item from the result set at the start
                hotelList.setSelection(results.startIndex);

                hotelList.setOnItemClickListener(this);

                hotelDetailFilter = new IntentFilter(Const.ACTION_HOTEL_DETAIL_RESULTS);

                // if (results.hasRecommendation && currentSortOption == null) {
                // sortByRecommendation();
                // }

                sortResults(results.hasRecommendation);

                updateResultsCountHideFooterIfNecessary();
            }
            enableSortItem(showClickableList);
        } else {
            ((TextView) findViewById(R.id.footer_navigation_bar_status))
                    .setText(R.string.search_no_result_dialog_title);
        }
    }

    // MOB-15696 - enable or disable sort option item
    private void enableSortItem(boolean enable) {
        if (sortItem != null) {
            sortItem.setEnabled(enable);
            sortItem.setVisible(enable);
        }
    }

    // called by the pricing and load more hotels listeners
    private void updateUI(boolean hasRecommendation) {
        // show user clickable view
        initUI(true);

        // Display a toast message indicating results have been updated and
        // sorted.
        String toastText = com.concur.mobile.base.util.Format.localizeText(this,
                R.string.general_loaded_more_results_toast, getCurrentSortOptionText(hasRecommendation));
        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    // will be called by the Hotels pricing listener when the reply is final i.e. full datat loaded with pricing
    private void sortResults(boolean hasRecommendation) {
        // Ensure the new set of results are sorted by the current sort
        // option.
        if (currentSortOption != null) {
            switch (currentSortOption) {
            case DISTANCE: {
                sortByDistance();
                break;
            }
            case PREFERRED_VENDOR: {
                sortByPreference();
                break;
            }
            case PRICE: {
                sortByCheapestRoomPrice();
                break;
            }
            case STAR_RATING: {
                sortByStarRating();
                break;
            }
            case VENDOR_NAME: {
                sortByChainName();
                break;
            }
            case RECOMMENDATION: {
                sortByRecommendation();
                break;
            }
            }
        } else {
            if (hasRecommendation) {
                sortByRecommendation();
            } else {
                sortByPreference();
            }
        }
    }

    /**
     * This method will examine the information in the latest results and update the count display and manage the footer.
     */
    protected void updateResultsCountHideFooterIfNecessary() {
        HotelSearchReply results = getConcurCore().getHotelSearchResults();
        if (results != null) {
            // Hide the footer is all results have been retrieved.
            if (moreHotelsFooter != null) {
                if (results.length == results.totalCount) {
                    hotelList.removeFooterView(moreHotelsFooter);
                }
            }
            // Set the text indicating the how many results have been retrieved
            // locally
            // out of the total and the current sort criteria.
            TextView txtView = (TextView) findViewById(R.id.footer_navigation_bar_status);
            if (txtView != null) {

                String resultsText = com.concur.mobile.base.util.Format.localizeText(this,
                        R.string.general_results_n_of_m_by_sort, results.length, results.totalCount,
                        getCurrentSortOptionText(results.hasRecommendation));
                txtView.setText(resultsText);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".updateResultsCount: unable to locate footer navigation bar status text view!");
            }
        }
    }

    /**
     * Gets the text string indicating the current sort option.
     * 
     * @return the current sorting option text.
     */
    protected String getCurrentSortOptionText(boolean hasRecommendations) {
        String sortOptionStr = null;
        if (currentSortOption != null) {
            switch (currentSortOption) {
            case DISTANCE: {
                sortOptionStr = getText(R.string.general_distance).toString();
                break;
            }
            case PREFERRED_VENDOR: {
                sortOptionStr = getText(R.string.general_preferred_vendor).toString();
                break;
            }
            case PRICE: {
                sortOptionStr = getText(R.string.general_price).toString();
                break;
            }
            case STAR_RATING: {
                sortOptionStr = getText(R.string.general_rating).toString();
                break;
            }
            case VENDOR_NAME: {
                sortOptionStr = getText(R.string.general_vendor_name).toString();
                break;
            }
            case RECOMMENDATION: {
                sortOptionStr = getText(R.string.hotel_recommendation).toString();
            }
            }
        } else {
            if (hasRecommendations) {
                sortOptionStr = getText(R.string.hotel_recommendation).toString();
            } else {
                sortOptionStr = getText(R.string.hotel_search_preferred_vendor).toString();
            }
        }
        return sortOptionStr;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Retain any hotel search receiver.
        if (hotelSearchReceiver != null) {
            // Clear the activity reference, it will be set in the new
            // HotelSearch instance.
            hotelSearchReceiver.setActivity(null);
            retainer.put(HOTEL_SEARCH_RECEIVER_KEY, hotelSearchReceiver);
        }
        // Retain any hotel detail receiver.
        if (hotelDetailReceiver != null) {
            hotelDetailReceiver.setActivity(null);
            retainer.put(HOTEL_DETAIL_RECEIVER_KEY, hotelDetailReceiver);
        }

        retainer.put(Const.EXTRA_TRAVEL_HOTEL_SHOW_READ_ONLY_LIST, showReadOnlyList);
        retainer.put(Const.EXTRA_TRAVEL_HOTEL_SEARCH_WORKFLOW_START_TIME, searchWorkflowStartTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void restoreReceivers() {
        // Restore any hotel detail receiver.
        if (retainer.contains(HOTEL_SEARCH_RECEIVER_KEY)) {
            hotelSearchReceiver = (HotelSearchReceiver) retainer.get(HOTEL_SEARCH_RECEIVER_KEY);
            // Reset the activity reference.
            hotelSearchReceiver.setActivity(this);
        }
        // Restore any retained hotel detail receiver.
        if (retainer.contains(HOTEL_DETAIL_RECEIVER_KEY)) {
            hotelDetailReceiver = (HotelDetailReceiver) retainer.get(HOTEL_DETAIL_RECEIVER_KEY);
            hotelDetailReceiver.setActivity(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os .Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentSortOption != null) {
            outState.putString(HOTEL_SORT_OPTION_KEY, currentSortOption.toString());
        }
    }

    /**
     * Fills in the search criteria based on passed in "extra" information in the launching intent.
     */
    private void configureSearchCriteriaText() {
        Intent intent = getIntent();
        View criteriaView = findViewById(R.id.hotel_search_results_criteria);

        if (criteriaView != null) {
            String criteriaLocation = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_LOCATION);
            String criteriaStr = com.concur.mobile.base.util.Format.localizeText(HotelSearchResults.this,
                    R.string.hotel_search_travel_header_name, criteriaLocation);
            ((TextView) criteriaView.findViewById(R.id.travel_name)).setText(criteriaStr);

            // Set the checkin/checkout dates.
            Calendar checkin = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
            Calendar checkout = (Calendar) intent
                    .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);
            if (checkin != null && checkout != null) {
                StringBuilder dates = new StringBuilder(Format.safeFormatCalendar(
                        FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, checkin));
                dates.append(" - ");
                dates.append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_YEAR_DISPLAY_NO_COMMA, checkout));

                // Set departure/arrival dates
                ((TextView) criteriaView.findViewById(R.id.date_span)).setText(dates.toString());
            }

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate search criteria text view!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_CODE_BOOK_HOTEL) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL: {
            dialog = ((ConcurCore) getApplication()).createDialog(this, id);
            // Add a listener for when the dialog gets canceled.
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (hotelDetailRequest != null) {
                        hotelDetailRequest.cancel();
                    }
                }
            });
            break;
        }
        case DIALOG_SORT_OPTION: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.hotel_search_sort_options));
            ArrayList<SortCriteria> sortOptions = new ArrayList<SortCriteria>();
            sortOptions.add(SortCriteria.RECOMMENDATION);
            sortOptions.add(SortCriteria.PREFERRED_VENDOR);
            sortOptions.add(SortCriteria.VENDOR_NAME);
            sortOptions.add(SortCriteria.PRICE);
            sortOptions.add(SortCriteria.DISTANCE);
            sortOptions.add(SortCriteria.STAR_RATING);
            sortOptionListAdapter = new HotelSortOptionListAdapter(sortOptions);
            builder.setSingleChoiceItems(sortOptionListAdapter, -1, new HotelSortDialogListener());
            dialog = builder.create();
            break;
        }
        case DIALOG_RETRIEVE_HOTELS: {
            progDlgRetrieveMoreHotels = new ProgressDialog(this);
            progDlgRetrieveMoreHotels.setMessage(this.getText(R.string.hotel_search_loading_more_hotels));
            progDlgRetrieveMoreHotels.setIndeterminate(true);
            progDlgRetrieveMoreHotels.setCancelable(true);
            progDlgRetrieveMoreHotels.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (hotelSearchRequest != null) {
                        hotelSearchRequest.cancel();
                    }
                }
            });
            dialog = progDlgRetrieveMoreHotels;
            break;
        }
        case DIALOG_RETRIEVE_HOTELS_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_hotel_search_load_more_hotels_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            break;
        }
        default:
            dialog = ((ConcurCore) getApplication()).createDialog(this, id);
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
        super.onPrepareDialog(id, dialog);
        switch (id) {
        case Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case DIALOG_RETRIEVE_HOTELS_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hotel_search_results, menu);
        sortItem = menu.findItem(R.id.hotel_search_results_sort_menu);
        enableSortItem(!showReadOnlyList);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean retVal = false;
        final int itemId = menuItem.getItemId();
        if (itemId == R.id.hotel_search_results_sort_menu) {
            showDialog(DIALOG_SORT_OPTION);
            retVal = true;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget .AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id) {

        HotelChoiceListItem hotelChoiceListItem = (HotelChoiceListItem) list.getItemAtPosition(position);
        if (hotelChoiceListItem != null) {
            HotelChoice hotelChoice = hotelChoiceListItem.getHotelChoice();
            if (hotelChoice != null) {
                // Determine if the hotel details are already in our in-memory
                // cache, if so, then
                // re-use them. A request to update will be made in the
                // background.
                ConcurCore app = (ConcurCore) getApplication();
                HotelChoiceDetail hotelChoiceDetail = app.getHotelDetail(hotelChoice.propertyId);
                if (hotelChoiceDetail != null) {
                    startHotelChoiceDetailActivity(hotelChoice.propertyId, true);
                } else {
                    if (ConcurCore.isConnected()) {
                        sendHotelDetailRequest(hotelChoice.propertyId);
                    } else {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }

                ConcurCore core = (ConcurCore) ConcurCore.getContext();
                if (core.hasViewedPriceToBeatList()) {

                    if (hotelChoice.cheapestRoom != null || hotelChoice.cheapestRoomWithViolation != null) {
                        Integer travelPoints = hotelChoice.cheapestRoom != null ? hotelChoice.cheapestRoom.travelPoints
                                : hotelChoice.cheapestRoomWithViolation.travelPoints;

                        int travelPts = (travelPoints == null ? 0 : travelPoints);
                        Map<String, String> travelPointsParams = new HashMap<String, String>();
                        if (hotelChoice.cheapestRoomWithViolation != null
                                && hotelChoice.cheapestRoomWithViolation.travelPoints != null) {
                            travelPointsParams.put(Flurry.PARAM_NAME_HOTEL_VIEWED, Flurry.PARAM_VALUE_USE);
                        } else if (travelPts > 0) {
                            travelPointsParams.put(Flurry.PARAM_NAME_HOTEL_VIEWED, Flurry.PARAM_VALUE_EARN);
                        } else {
                            travelPointsParams.put(Flurry.PARAM_NAME_HOTEL_VIEWED, Flurry.PARAM_VALUE_NA);
                        }
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_PRICE_TO_BEAT,
                                Flurry.EVENT_NAME_VIEWED_PRICE_TO_BEAT_RANGE, travelPointsParams);

                    }
                }

            }
        }
    }

    /**
     * Will launch the hotel choice detail activity for a specific property id.
     * 
     * @param propertyId
     *            the property id.
     * @param fromCache
     *            contains whether the hotel details is available from the cache.
     */
    protected void startHotelChoiceDetailActivity(String propertyId, boolean fromCache) {
        ConcurCore concurCore = (ConcurCore) getApplication();
        // Locate the HotelDetail object.
        HotelChoiceDetail hotelDetail = concurCore.getHotelDetail(propertyId);
        if (hotelDetail.rooms == null || hotelDetail.rooms.size() == 0) {
            // no rooms
            DialogFragmentFactory.getAlertOkayInstance(R.string.hotel_search_room_title,
                    R.string.hotel_details_no_rooms_available).show(getSupportFragmentManager(), null);
        } else {

            Intent intent = getHotelsearchRoomIntent();
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID, propertyId);
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_FROM_CACHE, fromCache);
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN,
                    getIntent().getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN));
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR,
                    getIntent().getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR));
            intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT,
                    getIntent().getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT));
            intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID,
                    getIntent().getStringExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID));
            intent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED,
                    getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false));
            Intent launchIntent = getIntent();
            if (launchIntent.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
                intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM,
                        launchIntent.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
            }
            startActivityForResult(intent, Const.REQUEST_CODE_BOOK_HOTEL);
        }
    }

    /**
     * get the hotelsearch room intent
     * */
    protected Intent getHotelsearchRoomIntent() {
        return new Intent(this, HotelSearchRooms.class);
    }

    /**
     * An extension of <code>BaseAdapter</code> for selecting a hotel sort option.
     * 
     * @author AndrewK
     */
    class HotelSortOptionListAdapter extends BaseAdapter {

        /**
         * Contains a list of available options.
         */
        ArrayList<SortCriteria> options = new ArrayList<SortCriteria>();

        /**
         * Constructs an instance of <code>HotelSortOptionListAdapter</code> with the various sort options.
         * 
         * @param options
         *            the list of sort options.
         */
        HotelSortOptionListAdapter(ArrayList<SortCriteria> options) {
            this.options = options;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return options.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return options.get(position);
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            LayoutInflater inflater = LayoutInflater.from(HotelSearchResults.this);

            int textResId = 0;
            switch (options.get(position)) {
            case PREFERRED_VENDOR:
                textResId = R.string.hotel_search_preferred_vendor;
                break;
            case DISTANCE:
                textResId = R.string.hotel_search_distance;
                break;
            case VENDOR_NAME:
                textResId = R.string.hotel_search_vendor_name;
                break;
            case PRICE:
                textResId = R.string.hotel_search_price;
                break;
            case STAR_RATING:
                textResId = R.string.hotel_search_rating;
                break;
            case RECOMMENDATION:
                textResId = R.string.hotel_recommendation;
                break;
            }
            view = inflater.inflate(R.layout.travel_hotel_search_result_sort_option, null);
            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                if (txtView != null) {
                    txtView.setText(getText(textResId));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate text view!");
                }
            }
            return view;
        }
    }

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> for handling user selection receipt option.
     * 
     * @author AndrewK
     */
    class HotelSortDialogListener implements DialogInterface.OnClickListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content .DialogInterface, int)
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {

            // Dismiss the dialog.
            dismissDialog(DIALOG_SORT_OPTION);
            // Perform the sort option.
            currentSortOption = (SortCriteria) sortOptionListAdapter.getItem(which);
            switch (currentSortOption) {
            case PREFERRED_VENDOR:
                sortByPreference();
                break;
            case DISTANCE:
                sortByDistance();
                break;
            case VENDOR_NAME:
                sortByChainName();
                break;
            case PRICE:
                sortByCheapestRoomPrice();
                break;
            case STAR_RATING:
                sortByStarRating();
                break;
            case RECOMMENDATION:
                sortByRecommendation();
                break;
            }
            if (hotelList != null) {
                hotelList.setSelection(0);
            }
            updateResultsCountHideFooterIfNecessary();
        }
    }

    /**
     * Sort the list of hotels primarily by distance, secondarily by price.
     */
    protected void sortByDistance() {
        // Construct the primary/secondary sorts.
        Comparator<HotelChoice> primarySort = new HotelComparator(HotelComparator.CompareField.DISTANCE,
                HotelComparator.CompareOrder.ASCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort);
    }

    /**
     * Sort the list of hotels primarily by star rating, secondarily by price.
     */
    protected void sortByStarRating() {
        // Construct the primary/secondary sorts.
        Comparator<HotelChoice> primarySort = new HotelComparator(HotelComparator.CompareField.STAR_RATING,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort);
    }

    /**
     * Sort the list of hotels primarily by price, secondarily by name.
     */
    protected void sortByCheapestRoomPrice() {
        // Construct the primary/secondary sorts.
        Comparator<HotelChoice> primarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.NAME,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort);
    }

    /**
     * Sorts the list of hotels primarily by chain name, secondarily by price.
     */
    protected void sortByChainName() {
        // Construct the primary/secondary sorts.
        Comparator<HotelChoice> primarySort = new HotelComparator(HotelComparator.CompareField.NAME,
                HotelComparator.CompareOrder.ASCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort);
    }

    /**
     * Sorts the list of hotels primarily by preference, secondarily by price.
     */
    protected void sortByPreference() {
        // Construct the primary/secondary sorts.
        Comparator<HotelChoice> primarySort = new HotelComparator(HotelComparator.CompareField.PREFERENCE,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort);
    }

    /**
     * Sort the list of hotels primarily by recommendation, secondarily by price.
     */
    protected void sortByRecommendation() {
        // Construct the primary/secondary sorts.
        // compare order is not used for the recommendation comparison so just
        // passing DESCENDING as the constrcutor needs
        Comparator<HotelChoice> primarySort = new HotelComparator(HotelComparator.CompareField.RECOMMENDATION,
                HotelComparator.CompareOrder.DESCENDING);
        HotelComparator secondarySort = new HotelComparator(HotelComparator.CompareField.CHEAPEST_ROOM,
                HotelComparator.CompareOrder.ASCENDING);
        // Perform the actual sort.
        sortByComparator(primarySort, secondarySort);
    }

    /**
     * Will perform first a primary sort, then secondary sort over the current hotel list.
     * 
     * @param primarySort
     *            the primary sort.
     * @param secondarySort
     *            the secondary sort.
     */
    protected void sortByComparator(Comparator<HotelChoice> primarySort, Comparator<HotelChoice> secondarySort) {
        List<HotelChoice> hotels = null;
        List<HotelChoiceListItem> hotelListItems = hotelChoiceListAdapter.getItems();
        Map<HotelChoice, HotelChoiceListItem> hotelSortMap = new HashMap<HotelChoice, HotelChoiceListItem>();
        if (hotelListItems != null) {
            // Create a list of 'HotelChoice' objects for sorting by
            // 'primarySort' and 'secondarySort'.
            hotels = new ArrayList<HotelChoice>(hotelListItems.size());
            for (HotelChoiceListItem hotelListItem : hotelListItems) {
                hotels.add(hotelListItem.getHotelChoice());
                // Map is populated so that after 'HotelChoice' items are
                // sorted, that a new list of
                // 'HotelChoiceListItem' can be quickly produced from the sorted
                // list of 'HotelChoice' objects.
                hotelSortMap.put(hotelListItem.getHotelChoice(), hotelListItem);
            }
        }
        if (hotels != null && hotels.size() > 0) {
            // First, place into separate lists based on 'primarySort'.
            TreeMap<HotelChoice, List<HotelChoice>> hotelChoiceListMap = new TreeMap<HotelChoice, List<HotelChoice>>(
                    primarySort);
            for (HotelChoice hotelChoice : hotels) {
                List<HotelChoice> subList = hotelChoiceListMap.get(hotelChoice);
                if (subList == null) {
                    subList = new ArrayList<HotelChoice>();
                    hotelChoiceListMap.put(hotelChoice, subList);
                }
                subList.add(hotelChoice);
            }
            // Second, iterate over the lists of hotel choices, sort each list
            // based on secondarySort.
            List<HotelChoice> listItems = new ArrayList<HotelChoice>();
            Iterator<HotelChoice> hotelChListIter = hotelChoiceListMap.keySet().iterator();
            while (hotelChListIter.hasNext()) {
                HotelChoice key = hotelChListIter.next();
                if (key != null) {
                    List<HotelChoice> chList = hotelChoiceListMap.get(key);
                    if (chList != null) {
                        Collections.sort(chList, secondarySort);
                        listItems.addAll(chList);
                    }
                }
            }
            // Produce a new list of 'HotelChoiceListItem' objects based on
            // iterating through 'listItems'
            // and pulling 'HotelChoiceListItem' objects from 'hotelSortMap'.
            if (hotelListItems != null) {
                hotelListItems.clear();
            } else {
                hotelListItems = new ArrayList<HotelChoiceListItem>(listItems.size());
            }
            for (HotelChoice hotelChoice : listItems) {
                hotelListItems.add(hotelSortMap.get(hotelChoice));
            }

            // Finally, set the list of hotel choice items on the adapter.
            hotelChoiceListAdapter.setItems(hotelListItems);
            hotelChoiceListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Will send a request to obtain additional hotels.
     */
    protected void sendHotelDetailRequest(String propertyId) {
        ConcurService concurService = getConcurService();
        registerHotelDetailReceiver();
        hotelDetailRequest = concurService.getHotelDetails(propertyId);
        if (hotelDetailRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendHotelDetailRequest: unable to create hotel detail request.");
            unregisterHotelDetailReceiver();
        } else {
            // Set the request object on the receiver.
            hotelDetailReceiver.setServiceRequest(hotelDetailRequest);
            // Show the dialog.
            showDialog(Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL);
        }
    }

    /**
     * Will register a hotel detail receiver.
     */
    private void registerHotelDetailReceiver() {
        if (hotelDetailReceiver == null) {
            hotelDetailReceiver = new HotelDetailReceiver(this);
            if (hotelDetailFilter == null) {
                hotelDetailFilter = new IntentFilter(Const.ACTION_HOTEL_DETAIL_RESULTS);
            }
            getApplicationContext().registerReceiver(hotelDetailReceiver, hotelDetailFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerHotelDetailReceiver: hotelDetailReceiver is *not* null!");
        }
    }

    /**
     * Will unregister a hotel search receiver.
     */
    private void unregisterHotelDetailReceiver() {
        if (hotelDetailReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(hotelDetailReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterHotelDetailReceiver: illegal argument", ilaExc);
            }
            hotelDetailReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterHotelDetailReceiver: hotelDetailReceiver is null!");
        }
    }

    static class HotelDetailReceiver extends BaseBroadcastReceiver<HotelSearchResults, HotelDetailRequest> {

        /**
         * Constructs an instance of <code>HotelDetailReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        HotelDetailReceiver(HotelSearchResults activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(HotelSearchResults activity) {
            activity.hotelDetailRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                String propertyId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_PROPERTY_ID);
                if (propertyId != null) {
                    activity.startHotelChoiceDetailActivity(propertyId, false);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: intent missing hotel property id!");
                }
            } else {
                activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                        + activity.actionStatusErrorMessage + ".");
                activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_HOTEL_DETAIL_FAILED);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(HotelDetailRequest request) {
            activity.hotelDetailRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterHotelDetailReceiver();
        }

    }

    /**
     * Will send a request to obtain additional hotels.
     */
    protected void sendHotelSearchRequest() {
        Intent intent = getIntent();
        Calendar checkInDateCal = (Calendar) intent
                .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        Calendar checkOutDateCal = (Calendar) intent
                .getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);

        String latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        String longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);

        String distanceId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID);
        String distanceUnitId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);
        String namesContaining = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING);

        HotelSearchReply results = getConcurCore().getHotelSearchResults();
        int startIndex = results.length;
        int count = results.totalCount - results.length;
        count = Math.min(Const.HOTEL_RETRIEVE_COUNT, count);

        // Show the dialog.
        showDialog(DIALOG_RETRIEVE_HOTELS);

        // TODO - can we reuse the HotelSearchReply Parser instead ?
        // TODO - move this functionality into BaseActivity then remove the code from HotelSearchProgress
        hotelResultsReceiver = new BaseAsyncResultReceiver(new Handler());
        hotelResultsReceiver.setListener(new HotelResultsListener());
        new GetHotels(getApplicationContext(), 1, hotelResultsReceiver, checkOutDateCal, checkInDateCal,
                namesContaining, latitude, longitude, distanceId, distanceUnitId, startIndex, count).execute();

    }

    /**
     * Will register a hotel search receiver.
     */
    private void registerHotelSearchReceiver() {
        if (hotelSearchReceiver == null) {
            hotelSearchReceiver = new HotelSearchReceiver(this);
            if (hotelSearchFilter == null) {
                hotelSearchFilter = new IntentFilter(Const.ACTION_HOTEL_SEARCH_RESULTS);
            }
            getApplicationContext().registerReceiver(hotelSearchReceiver, hotelSearchFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerHotelSearchReceiver: hotelSearchReceiver is *not* null!");
        }
    }

    /**
     * Will unregister a hotel search receiver.
     */
    private void unregisterHotelSearchReceiver() {
        if (hotelSearchReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(hotelSearchReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterHotelSearchReceiver: illegal argument", ilaExc);
            }
            hotelSearchReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterHotelSearchReceiver: hotelSearchReceiver is null!");
        }
    }

    protected class HotelSearchReceiver extends BaseBroadcastReceiver<HotelSearchResults, ServiceRequest> {

        /**
         * Constructs an instance of <code>HotelSearchReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        public HotelSearchReceiver(HotelSearchResults activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(HotelSearchResults activity) {
            activity.hotelSearchRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_RETRIEVE_HOTELS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_RETRIEVE_HOTELS_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Update the list adapter with the new set of results.
            HotelSearchReply reply = activity.getConcurCore().getHotelSearchResults();
            List<HotelChoiceListItem> hotelListItems = new ArrayList<HotelChoiceListItem>();
            if (reply.hotelChoices != null) {
                for (HotelChoice hotelChoice : reply.hotelChoices) {
                    hotelListItems.add(new HotelChoiceListItem(hotelChoice));
                }
            }
            activity.hotelChoiceListAdapter.setItems(hotelListItems);
            // Ensure the new set of results are sorted by the current sort
            // option.
            if (activity.currentSortOption != null) {
                switch (activity.currentSortOption) {
                case DISTANCE: {
                    activity.sortByDistance();
                    break;
                }
                case PREFERRED_VENDOR: {
                    activity.sortByPreference();
                    break;
                }
                case PRICE: {
                    activity.sortByCheapestRoomPrice();
                    break;
                }
                case STAR_RATING: {
                    activity.sortByStarRating();
                    break;
                }
                case VENDOR_NAME: {
                    activity.sortByChainName();
                    break;
                }
                case RECOMMENDATION: {
                    activity.sortByRecommendation();
                    break;
                }
                }
            } else {
                if (reply.hasRecommendation) {
                    activity.sortByRecommendation();
                } else {
                    activity.sortByPreference();
                }
            }
            // Update the results count display.
            activity.updateResultsCountHideFooterIfNecessary();
            // Send the end-user back to the beginning of the list.
            activity.hotelList.setSelection(0);
            // Display a toast message indicating results have been updated and
            // sorted.
            String toastText = com.concur.mobile.base.util.Format.localizeText(activity,
                    R.string.general_loaded_more_results_toast,
                    activity.getCurrentSortOptionText(reply.hasRecommendation));
            Toast toast = Toast.makeText(activity, toastText, Toast.LENGTH_SHORT);
            toast.show();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ServiceRequest request) {
            activity.hotelSearchRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterHotelSearchReceiver();
        }
    }

    private void getPricingReqParams() {

        Intent intent = getIntent();

        checkInDateCal = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN_CALENDAR);
        checkOutDateCal = (Calendar) intent.getSerializableExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT_CALENDAR);
        latitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LATITUDE);
        longitude = intent.getStringExtra(Const.EXTRA_TRAVEL_LONGITUDE);
        distanceId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_ID);
        distanceUnitId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID);
        namesContaining = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_NAMES_CONTAINING);
        pollingId = intent.getStringExtra(Const.EXTRA_TRAVEL_HOTEL_POLLING_ID);
    }

    private void retrieveHotelPricing() {
        if (ConcurCore.isConnected()) {

            showSearchingRatesProgressDialog();

            // create a delayed handler for the aysnc task
            Handler asyncHandler = new Handler();

            hotelsPricingReceiver = new BaseAsyncResultReceiver(asyncHandler);
            hotelsPricingReceiver.setListener(new HotelPricingListener());

            asyncHandler.postDelayed(new Runnable() {

                public void run() {
                    // if the time taken for the search workflow (i.e. start of search to end of pricing) > 90 seconds then stop
                    // the
                    // functionality
                    long timeNow = (System.currentTimeMillis() - searchWorkflowStartTime) / 1000;

                    Log.d(Const.LOG_TAG, " in retrieveHotelPricing, time now from start of search " + timeNow
                            + " seconds. If this is < 90 then will invoke GetHotelsPricing again.");
                    if (timeNow < 90) {
                        // call the pricing end point
                        getHotelsPricing = new GetHotelsPricing(getApplicationContext(), 1, hotelsPricingReceiver,
                                checkOutDateCal, checkInDateCal, namesContaining, latitude, longitude, distanceId,
                                distanceUnitId, 0, Const.HOTEL_RETRIEVE_COUNT, pollingId);

                        getHotelsPricing.execute();
                    } else {
                        if (searchingRatesProgressFrag.getDialog() != null) {
                            searchingRatesProgressFrag.dismiss();
                            showSearchingRatesFailedDialog();
                        }
                    }
                }

            }, 2000);
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    private void refreshUIListItem() {
        if (hotelChoiceListAdapter == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".refreshUIListItem: hotelChoiceListAdapter is null!");
        } else {
            for (HotelChoice hotelChoice : hotelChoicesToBeUpdated) {
                refreshView(hotelChoice);
            }
        }
    }

    /**
     * Will refresh each visible view contained in a list view that match on <code>HotelChoice.propertyId</code>.
     * 
     * @param propertyId
     *            contains the propertyId that should be refreshed.
     */
    public void refreshView(HotelChoice updatedHotelChoice) {
        int start = hotelList.getFirstVisiblePosition();
        for (int i = start, j = hotelList.getLastVisiblePosition(); i <= j; i++) {
            HotelChoiceListItem listItem = hotelChoiceListAdapter.getItem(i);
            // NOTE: Need to check for 'listItem' not being null as the last visible position within the list
            // could be a list footer, which accounts for a visible position, but not reflecting any data
            // within the adapter.
            if (listItem != null && listItem.getHotelChoice() != null
                    && listItem.getHotelChoice().propertyId.equals(updatedHotelChoice.propertyId)) {

                listItem.getHotelChoice().cheapestRoom = updatedHotelChoice.cheapestRoom;
                listItem.getHotelChoice().cheapestRoomWithViolation = updatedHotelChoice.cheapestRoomWithViolation;
                listItem.getHotelChoice().isAdditional = updatedHotelChoice.isAdditional;
                listItem.getHotelChoice().isSoldOut = updatedHotelChoice.isSoldOut;
                listItem.getHotelChoice().isNoRates = updatedHotelChoice.isNoRates;

                View view = hotelList.getChildAt(i - start);
                hotelChoiceListAdapter.getView(i, view, hotelList);

                // TODO - remove this log statement
                Log.d(Const.LOG_TAG, " *** trying to refresh " + listItem.getHotelChoice().hotel);

                break;
            }
        }
    }

    private void showSearchingRatesProgressDialog() {
        // show the dialog if it is not being displayed
        if (searchingRatesProgressFrag == null) {
            searchingRatesProgressFrag = new ProgressDialogFragment();
            searchingRatesProgressFrag.setMessage(R.string.hotel_search_for_rates_progress);
            searchingRatesProgressFrag.setIndeterminate(true);

            // MOB-18224
            searchingRatesProgressFrag.setCanceledOnTouchOutside(false);

            searchingRatesProgressFrag.setCancelListener(new ProgressDialogFragment.OnCancelListener() {

                @Override
                public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                    if (getHotelsPricing != null) {
                        getHotelsPricing.cancel(true);
                    }
                    searchingRatesProgressFrag.dismiss();
                }
            });
            searchingRatesProgressFrag.show(getSupportFragmentManager(), null);
        }
    }

    private void showSearchingRatesFailedDialog() {
        showSearchingRatesFailedDialog(null);
    }

    private void showSearchingRatesFailedDialog(String message) {
        searchingRatesFailedFrag = new AlertDialogFragment();
        if (message == null) {
            searchingRatesFailedFrag.setMessage(R.string.hotel_search_for_rates_failed);
        } else {
            searchingRatesFailedFrag.setMessage(message);
        }
        searchingRatesFailedFrag.setPositiveButtonText(R.string.okay);
        searchingRatesFailedFrag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                hotelsPricingReceiver = null;
                searchingRatesFailedFrag.dismiss();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }
        });
        if (getSupportFragmentManager() != null && ConcurCore.isConnected()) {
            searchingRatesFailedFrag.show(getSupportFragmentManager(), null);
        }
    }

    /**
     * Listener used for retrieving the hotel pricing results
     */
    private class HotelPricingListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestSuccess in HotelPricingListener...");

            if (searchingRatesProgressFrag.getDialog() != null) {

                HotelSearchReply reply = (HotelSearchReply) resultData.getSerializable("HotelSearchReply");

                if (reply.isFinal) {
                    try {
                        searchingRatesProgressFrag.dismiss();
                        // TODO Move searchingRatesProgressFrag to async task and handle new flag to dismiss fragment in
                        // onActivityResult of ProgressDialogFragment
                    } catch (IllegalStateException ex) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".searchingRatesProgressFrag: dismiss", ex);
                    }

                    // Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    long totalSearchDuration = (System.currentTimeMillis() - searchWorkflowStartTime) / 1000;
                    params.put(Flurry.PARAM_NAME_TOTAL_HOTEL_SEARCH_DURATION, Long.toString(totalSearchDuration));
                    if (reply.hotelChoices == null || reply.hotelChoices.size() == 0) {
                        params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_HOTEL_SEARCH_STREAMING_FAILURE);
                        showSearchingRatesFailedDialog();
                    } else {
                        // refresh the list view with clickable and sort operations true
                        params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
                        showReadOnlyList = false;
                        updateUI(reply.hasRecommendation);
                    }
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_BOOK, Flurry.EVENT_NAME_HOTEL_SEARCH_STREAMING_ON,
                            params);
                } else {
                    if (resultData.containsKey("HotelChoicesToBeUpdated")) {
                        hotelChoicesToBeUpdated = (ArrayList<HotelChoice>) resultData
                                .getSerializable("HotelChoicesToBeUpdated");
                        if (!hotelChoicesToBeUpdated.isEmpty()) {
                            // refresh the rates of the appropriate properties
                            refreshUIListItem();
                        }
                    }
                    // send another request to retrieve the pricing
                    Log.d(Const.LOG_TAG,
                            " onRequestSuccess in HotelPricingListener... going to invoke GetHotelsPricing again...");
                    retrieveHotelPricing();
                }
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            if (searchingRatesProgressFrag != null && searchingRatesProgressFrag.getDialog() != null) {
                // fix for app crash after losing connection
                searchingRatesProgressFrag.dismissAllowingStateLoss();
                showSearchingRatesFailedDialog(resultData.getString(Const.MWS_ERROR_MESSAGE));
            }
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            // Log.d(Const.LOG_TAG, " onRequestCancel in HotelPricingListener...");
        }

        @Override
        public void cleanup() {
            // work around for - receiver not found, dropping results issue
            // hotelsPricingReceiver = null;
        }
    }

    /**
     * Listener used for displaying the hotel results for load more hotels feature
     */
    private class HotelResultsListener implements AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestSuccess in HotelResultsListener for load more hotels ...");

            if (progDlgRetrieveMoreHotels != null && progDlgRetrieveMoreHotels.isShowing()) {

                // get the reply from the app object
                HotelSearchReply reply = getConcurCore().getHotelSearchResults();

                // get the new set of load more hotels data
                HotelSearchReply newReply = (HotelSearchReply) resultData.getSerializable("HotelSearchReply");

                // update the reply in app with the length and the new set of data
                reply.hotelChoices.addAll(newReply.hotelChoices);
                reply.length = newReply.length + reply.length;
                reply.isFinal = newReply.isFinal;

                // MOB-16531 - update the start index
                reply.startIndex = newReply.startIndex;

                getConcurCore().setHotelSearchResults(reply);

                showReadOnlyList = !reply.isFinal;

                dismissDialog(DIALOG_RETRIEVE_HOTELS);

                // refresh the full list
                updateUI(reply.hasRecommendation);
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestFail in HotelResultsListener for load more hotels ...");
            if (progDlgRetrieveMoreHotels != null && progDlgRetrieveMoreHotels.isShowing()) {
                dismissDialog(DIALOG_RETRIEVE_HOTELS);
                showDialog(DIALOG_RETRIEVE_HOTELS_FAILED);
            }
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, " onRequestCancel in HotelResultsListener for load more hotels ...");
        }

        @Override
        public void cleanup() {
            hotelResultsReceiver = null;
        }
    }

}
