/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.air.activity.AirSearch.SearchMode;
import com.concur.mobile.core.travel.air.data.AirBookingSegment;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.core.travel.air.data.AirDictionaries;
import com.concur.mobile.core.travel.air.data.Flight;
import com.concur.mobile.core.travel.air.service.AirFilterReply;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.RuleEnforcementLevel;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;

/**
 * An extension of <code>BaseActivity</code> for the purpose of displaying a list of air search results. <br>
 * <b>NOTE:</b><br>
 * This activity uses the latest instance of <code>AirFilterResults</code> associated with the application object.
 */
public class AirResultsList extends TravelBaseActivity {

    private static final String CLS_TAG = AirResultsList.class.getSimpleName();

    private static final int DIALOG_SORT_OPTION = 0;

    private static final int FLIGHT_DETAIL_REQUEST_CODE = 0;

    private static final String EXTRA_SORT_CRITERIA = "sort.criteria";

    protected SearchMode searchMode;

    protected LocationChoice departLocation;
    protected LocationChoice arriveLocation;

    protected Calendar departDateTime;
    protected Calendar returnDateTime;

    protected Intent flightDetailIntent;

    protected String formattedBenchmarkPrice;
    protected String travelPointsInBank;

    protected enum SortCriteria {
        PREFERENCE, PRICE, TOTAL_TRAVEL_TIME, EARLIEST_DEPARTURE
    };

    protected ListView listView;
    protected ListItemAdapter<AirChoiceListItem> airChoiceAdapter;

    protected SpinnerItem curCriteriaItem = null;
    protected SpinnerItem[] criteriaItems = {
            new SpinnerItem(SortCriteria.PREFERENCE.name(), R.string.sort_criteria_preference),
            new SpinnerItem(SortCriteria.PRICE.name(), R.string.sort_criteria_price),
            new SpinnerItem(SortCriteria.TOTAL_TRAVEL_TIME.name(), R.string.sort_criteria_total_travel_time),
            new SpinnerItem(SortCriteria.EARLIEST_DEPARTURE.name(), R.string.sort_criteria_earliest_departure) };

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_search_results);

        initValues(savedInstanceState);
        initUI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os .Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store the current sort criteria.
        if (curCriteriaItem != null) {
            outState.putString(EXTRA_SORT_CRITERIA, curCriteriaItem.id);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
        case DIALOG_SORT_OPTION:
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.air_search_filter_sort_by_title);
            dlgBldr.setCancelable(true);
            ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(this,
                    android.R.layout.simple_spinner_item, criteriaItems) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    return super.getDropDownView(position, convertView, parent);
                }
            };

            listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            int selectedItem = -1;
            if (curCriteriaItem != null) {
                for (int i = 0; i < criteriaItems.length; i++) {
                    if (curCriteriaItem.id.equals(criteriaItems[i].id)) {
                        selectedItem = i;
                        break;
                    }
                }
            }
            dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    curCriteriaItem = criteriaItems[which];
                    sortByCurrentCriteria();
                    listView.setSelection(0);
                    removeDialog(DIALOG_SORT_OPTION);
                }
            });
            dlgBldr.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    removeDialog(DIALOG_SORT_OPTION);
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.air_search_filter, menu);
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
        if (itemId == R.id.air_filter_results_sort_menu) {
            showDialog(DIALOG_SORT_OPTION);
            retVal = true;
        }
        return retVal;
    }

    // Sort by preference first, then by price.
    protected void sortByPreference() {
        // Switch the natural order so that higher ranking carriers are at the
        // top of the list.
        Comparator<Integer> prefComparator = new Comparator<Integer>() {

            @Override
            public int compare(Integer object1, Integer object2) {
                int retVal = 0;
                if (object1 < object2) {
                    retVal = 1;
                } else if (object1 > object2) {
                    retVal = -1;
                }
                return retVal;
            }
        };
        TreeMap<Integer, List<AirChoiceListItem>> rankAirChoiceMap = new TreeMap<Integer, List<AirChoiceListItem>>(
                prefComparator);
        List<AirChoiceListItem> acListItems = airChoiceAdapter.getItems();
        if (acListItems != null) {
            // First, place into separate lists based on ranking.
            for (AirChoiceListItem acListItem : acListItems) {
                Integer airChoiceRank = getAirChoicePreference(acListItem.getAirChoice());
                List<AirChoiceListItem> subList = rankAirChoiceMap.get(airChoiceRank);
                if (subList == null) {
                    subList = new ArrayList<AirChoiceListItem>();
                    rankAirChoiceMap.put(airChoiceRank, subList);
                }
                subList.add(acListItem);
            }
            // Second, iterate over the list of air choices, sort them by Price,
            // then add to one big list.
            List<AirChoiceListItem> listItems = new ArrayList<AirChoiceListItem>();
            AirChoiceListItemComparator airChListComp = new AirChoiceListItemComparator(SortCriteria.PRICE);
            Iterator<Integer> airChListIter = rankAirChoiceMap.keySet().iterator();
            while (airChListIter.hasNext()) {
                List<AirChoiceListItem> chList = rankAirChoiceMap.get(airChListIter.next());
                Collections.sort(chList, airChListComp);
                listItems.addAll(chList);
            }
            // Finally, set the list of air choice items on the adapter.
            airChoiceAdapter.setItems(listItems);
            airChoiceAdapter.notifyDataSetChanged();
        }
    }

    protected void sortByPrice() {

        TreeMap<Double, List<AirChoiceListItem>> fareAirChoiceMap = new TreeMap<Double, List<AirChoiceListItem>>();
        List<AirChoiceListItem> acListItems = airChoiceAdapter.getItems();
        if (acListItems != null) {
            // First, place into separate lists based on price.
            for (AirChoiceListItem acListItem : acListItems) {
                Double airChoiceFare = acListItem.getAirChoice().fare;
                List<AirChoiceListItem> subList = fareAirChoiceMap.get(airChoiceFare);
                if (subList == null) {
                    subList = new ArrayList<AirChoiceListItem>();
                    fareAirChoiceMap.put(airChoiceFare, subList);
                }
                subList.add(acListItem);
            }
            // Second, iterate over the list of air choices, sort them by
            // Preference, then add to one big list.
            List<AirChoiceListItem> listItems = new ArrayList<AirChoiceListItem>();
            AirChoiceListItemComparator airChListComp = new AirChoiceListItemComparator(SortCriteria.PREFERENCE);
            Iterator<Double> airChListIter = fareAirChoiceMap.keySet().iterator();
            while (airChListIter.hasNext()) {
                List<AirChoiceListItem> chList = fareAirChoiceMap.get(airChListIter.next());
                Collections.sort(chList, airChListComp);
                listItems.addAll(chList);
            }
            // Finally, set the list of air choice items on the adapter.
            airChoiceAdapter.setItems(listItems);
            airChoiceAdapter.notifyDataSetChanged();
        }
    }

    protected void sortByDuration() {
        TreeMap<Integer, Map<Double, List<AirChoiceListItem>>> durationAirChoiceMap = new TreeMap<Integer, Map<Double, List<AirChoiceListItem>>>();
        List<AirChoiceListItem> acListItems = airChoiceAdapter.getItems();
        if (acListItems != null) {
            // First, filter 'acListItem' into the correct list based on first
            // looking at duration, then fare.
            for (AirChoiceListItem acListItem : acListItems) {
                Integer airChoiceDuration = getAirChoiceDuration(acListItem.getAirChoice());
                Map<Double, List<AirChoiceListItem>> fareListMap = durationAirChoiceMap.get(airChoiceDuration);
                if (fareListMap == null) {
                    fareListMap = new TreeMap<Double, List<AirChoiceListItem>>();
                    durationAirChoiceMap.put(airChoiceDuration, fareListMap);
                }
                List<AirChoiceListItem> acList = fareListMap.get(acListItem.getAirChoice().fare);
                if (acList == null) {
                    acList = new ArrayList<AirChoiceListItem>();
                    fareListMap.put(acListItem.getAirChoice().fare, acList);
                }
                acList.add(acListItem);
            }
            // Second, iterate over key sets and then sort the leaf lists
            // containing AirChoiceList items.
            List<AirChoiceListItem> listItems = new ArrayList<AirChoiceListItem>();
            Iterator<Integer> durationIterator = durationAirChoiceMap.keySet().iterator();
            AirChoiceListItemComparator airChListComp = new AirChoiceListItemComparator(SortCriteria.PREFERENCE);
            while (durationIterator.hasNext()) {
                Integer durationKey = durationIterator.next();
                Map<Double, List<AirChoiceListItem>> fareListMap = durationAirChoiceMap.get(durationKey);
                Iterator<Double> fareIterator = fareListMap.keySet().iterator();
                while (fareIterator.hasNext()) {
                    Double fareKey = fareIterator.next();
                    List<AirChoiceListItem> fareList = fareListMap.get(fareKey);
                    Collections.sort(fareList, airChListComp);
                    listItems.addAll(fareList);
                }
            }
            // Finally, set the list of air choice items on the adapter.
            airChoiceAdapter.setItems(listItems);
            airChoiceAdapter.notifyDataSetChanged();
        }
    }

    protected void sortByEarliestDeparture() {
        TreeMap<Calendar, Map<Double, List<AirChoiceListItem>>> earlDepAirChoiceMap = new TreeMap<Calendar, Map<Double, List<AirChoiceListItem>>>();
        List<AirChoiceListItem> acListItems = airChoiceAdapter.getItems();
        if (acListItems != null) {
            // First, filter 'acListItem' into the correct list based on first
            // looking at duration, then fare.
            for (AirChoiceListItem acListItem : acListItems) {
                Calendar airChoiceEarliestDeparture = getAirChoiceEarliestDeparture(acListItem.getAirChoice());
                Map<Double, List<AirChoiceListItem>> fareListMap = earlDepAirChoiceMap.get(airChoiceEarliestDeparture);
                if (fareListMap == null) {
                    fareListMap = new TreeMap<Double, List<AirChoiceListItem>>();
                    earlDepAirChoiceMap.put(airChoiceEarliestDeparture, fareListMap);
                }
                List<AirChoiceListItem> acList = fareListMap.get(acListItem.getAirChoice().fare);
                if (acList == null) {
                    acList = new ArrayList<AirChoiceListItem>();
                    fareListMap.put(acListItem.getAirChoice().fare, acList);
                }
                acList.add(acListItem);
            }
            // Second, iterate over key sets and then sort the leaf lists
            // containing AirChoiceList items.
            List<AirChoiceListItem> listItems = new ArrayList<AirChoiceListItem>();
            Iterator<Calendar> earlDepIterator = earlDepAirChoiceMap.keySet().iterator();
            AirChoiceListItemComparator airChListComp = new AirChoiceListItemComparator(SortCriteria.PREFERENCE);
            while (earlDepIterator.hasNext()) {
                Calendar earlDepKey = earlDepIterator.next();
                Map<Double, List<AirChoiceListItem>> fareListMap = earlDepAirChoiceMap.get(earlDepKey);
                Iterator<Double> fareIterator = fareListMap.keySet().iterator();
                while (fareIterator.hasNext()) {
                    Double fareKey = fareIterator.next();
                    List<AirChoiceListItem> fareList = fareListMap.get(fareKey);
                    Collections.sort(fareList, airChListComp);
                    listItems.addAll(fareList);
                }
            }
            // Finally, set the list of air choice items on the adapter.
            airChoiceAdapter.setItems(listItems);
            airChoiceAdapter.notifyDataSetChanged();
        }
    }

    public static Calendar getAirChoiceEarliestDeparture(AirChoice airChoice) {
        Calendar earliestDeparture = null;
        if (airChoice.segments != null) {
            if (airChoice.segments.get(0).flights != null) {
                earliestDeparture = airChoice.segments.get(0).flights.get(0).departureDateTime;
            }
        }
        return earliestDeparture;
    }

    public static int getAirChoiceDuration(AirChoice airChoice) {
        int acDuration = 0;
        if (airChoice.segments != null) {
            for (AirBookingSegment airBkSeg : airChoice.segments) {
                acDuration += airBkSeg.elapsedTime;
            }
        }
        return acDuration;
    }

    // Gets the ranking for an airchoice based on carrier preference.
    // If not all the carriers in the various flights are not the same as the
    // the carrier of the first flight, then the ranking is 0; otherwise, it's
    // the ranking of the carrier for the first flight.
    public static int getAirChoicePreference(AirChoice airChoice) {
        int airChoiceRank = 0;

        String firstFlightCarrier = null;
        boolean rankSet = false;
        if (airChoice.segments != null) {
            for (AirBookingSegment airBkSeg : airChoice.segments) {
                if (airBkSeg.flights != null) {
                    for (Flight flight : airBkSeg.flights) {
                        if (firstFlightCarrier != null) {
                            if (!firstFlightCarrier.equalsIgnoreCase(flight.carrier)) {
                                airChoiceRank = 0;
                                rankSet = true;
                                break;
                            }
                        } else {
                            firstFlightCarrier = flight.carrier;
                        }
                    }
                }
                if (rankSet) {
                    break;
                }
            }
        }
        if (!rankSet && firstFlightCarrier != null) {
            String rankStr = AirDictionaries.preferenceRankMap.get(firstFlightCarrier);
            if (rankStr != null && rankStr.trim().length() > 0) {
                try {
                    airChoiceRank = Integer.decode(rankStr);
                } catch (NumberFormatException nfe) {
                    Log.d(Const.LOG_TAG, "Failed to convert preference rank to integer // " + rankStr);
                }
            }
        }
        return airChoiceRank;
    }

    protected void initValues(Bundle savedInstanceState) {
        Intent i = getIntent();

        flightDetailIntent = getFlightDetailIntent();

        if (i.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            flightDetailIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, i.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        flightDetailIntent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED,
                getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false));

        searchMode = SearchMode.None;
        String mode = i.getStringExtra(Const.EXTRA_SEARCH_MODE);
        if (mode != null) {
            searchMode = SearchMode.valueOf(mode);
        }
        flightDetailIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchMode.name());
        final Bundle departLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        final Bundle arriveLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);

        departLocation = new LocationChoice(departLocBundle);
        arriveLocation = new LocationChoice(arriveLocBundle);
        departDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);

        flightDetailIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocBundle);
        flightDetailIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocBundle);
        flightDetailIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);

        boolean refundableOnly = i.getBooleanExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, false);
        flightDetailIntent.putExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, refundableOnly);

        if (searchMode != SearchMode.OneWay) {
            returnDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_RETURN);
            flightDetailIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateTime);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_SORT_CRITERIA)) {
                curCriteriaItem = SpinnerItem
                        .findById(criteriaItems, savedInstanceState.getString(EXTRA_SORT_CRITERIA));
            }
        }

        formattedBenchmarkPrice = i.getStringExtra(EXTRA_FORMATTED_PRICE_TO_BEAT_KEY);
        travelPointsInBank = i.getStringExtra(EXTRA_TRAVEL_POINTS_IN_BANK_KEY);

        flightDetailIntent.putExtra(EXTRA_FORMATTED_PRICE_TO_BEAT_KEY, formattedBenchmarkPrice);
        flightDetailIntent.putExtra(EXTRA_TRAVEL_POINTS_IN_BANK_KEY, travelPointsInBank);
    }

    protected Intent getFlightDetailIntent() {
        return new Intent(this, AirFlightDetail.class);
    }

    protected void initUI() {

        AirFilterReply filterReply = getConcurCore().getAirFilterResults();
        if (filterReply != null) {

            // Init the screen header.
            initScreenHeader(filterReply);

            // Init the travel header.
            initTravelHeader(filterReply);

            // set Travel Points header
            int headerResId = R.string.travel_points_air_booking_workflow_p2b_header;
            TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;
            if (travelPointsConfig != null && travelPointsConfig.isAirTravelPointsEnabled()) {
                initAirTravelPointsHeader(formattedBenchmarkPrice, headerResId, travelPointsInBank,
                        R.string.travel_points_air_booking_workflow_points_header);
            } else {
                initAirTravelPointsHeader(formattedBenchmarkPrice, headerResId, null, -1);
            }

            // Init the list.
            initResultsList(filterReply);

        } else {
            // Finish the activity since there are no filter results!
            setResult(Activity.RESULT_CANCELED);
        }
    }

    /**
     * Will initialize the results list.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initResultsList(AirFilterReply filterReply) {

        listView = (ListView) findViewById(R.id.list_view);
        if (listView != null) {
            List<AirChoiceListItem> airChoiceListItems = null;
            if (filterReply.choices != null) {
                airChoiceListItems = new ArrayList<AirChoiceListItem>(filterReply.choices.size());
                for (AirChoice airChoice : filterReply.choices) {
                    // Check for whether the flight should be hidden.
                    if (ViewUtil.getRuleEnforcementLevel(ViewUtil.getMaxRuleEnforcementLevel(airChoice.violations)) != RuleEnforcementLevel.HIDE) {
                        airChoiceListItems.add(createListItem(airChoice));
                    }
                }
            }
            airChoiceAdapter = new ListItemAdapter<AirChoiceListItem>(this, airChoiceListItems);
            // Prior to setting the adapter on the view, init the image cache
            // receiver to handle
            // updating the list based on images downloaded asychronously.
            imageCacheReceiver = new ImageCacheReceiver<AirChoiceListItem>(airChoiceAdapter, listView);
            registerImageCacheReceiver();
            listView.setAdapter(airChoiceAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AirChoiceListItem airChoiceListItem = (AirChoiceListItem) listView.getItemAtPosition(position);
                    // MOB-14778 - if max enforcement level is 40 then show
                    // message and do not allow for reserve
                    Violation maxEnforcementViolation = ViewUtil.getShowButNoBookingViolation(
                            airChoiceListItem.getAirChoice().violations,
                            airChoiceListItem.getAirChoice().maxEnforcementLevel);
                    if (maxEnforcementViolation != null) {
                        showReserveNotAllowed(maxEnforcementViolation.message);
                    } else {
                        if (ConcurCore.isConnected()) {
                            flightDetailIntent.putExtra(Const.EXTRA_TRAVEL_AIR_CHOICE_FARE_ID,
                                    airChoiceListItem.getAirChoice().fareId);
                            startActivityForResult(flightDetailIntent, FLIGHT_DETAIL_REQUEST_CODE);
                        } else {
                            new NoConnectivityDialogFragment().show(getSupportFragmentManager(), null);
                        }
                    }
                }
            });
            // If the current sort criteria is not set, then set it to
            // preference.
            if (curCriteriaItem == null) {
                if (airChoiceListItems != null) {
                    for (SpinnerItem spinnerItem : criteriaItems) {
                        if (SortCriteria.valueOf(spinnerItem.id) == SortCriteria.PREFERENCE) {
                            curCriteriaItem = spinnerItem;
                            break;
                        }
                    }
                }
            }
            sortByCurrentCriteria();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initResultsList: unable to locate list view!");
        }
    }

    protected AirChoiceListItem createListItem(AirChoice airChoice) {
        return new AirChoiceListItem(airChoice);
    }

    protected void sortByCurrentCriteria() {
        if (curCriteriaItem != null) {
            switch (SortCriteria.valueOf(curCriteriaItem.id)) {
            case EARLIEST_DEPARTURE: {
                sortByEarliestDeparture();
                break;
            }
            case PREFERENCE: {
                sortByPreference();
                break;
            }
            case PRICE: {
                sortByPrice();
                break;
            }
            case TOTAL_TRAVEL_TIME: {
                sortByDuration();
                break;
            }
            }
        }
    }

    /**
     * Will initialize the travel header with from/to information.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initTravelHeader(AirFilterReply filterReply) {
        final String departIATACode = departLocation.getIATACode();
        final String arriveIATACode = arriveLocation.getIATACode();

        // The travel header
        TextView tv = (TextView) findViewById(R.id.travel_name);
        tv.setText(Format.localizeText(this, R.string.segmentlist_air_fromto, new Object[] { departIATACode,
                arriveIATACode }));

        StringBuilder sb = new StringBuilder();
        // MOB-22200 - choose local time zone
        sb.append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL.format(departDateTime.getTime()));
        if (searchMode != SearchMode.OneWay) {
            sb.append(" - ")
                    .append(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL.format(returnDateTime.getTime()));
        }
        tv = (TextView) findViewById(R.id.date_span);
        tv.setText(sb.toString());
    }

    /**
     * Will initialize the screen header.
     * 
     * @param filterReply
     *            the filter reply.
     */
    protected void initScreenHeader(AirFilterReply filterReply) {
        getSupportActionBar().setTitle(R.string.air_search_filter_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case FLIGHT_DETAIL_REQUEST_CODE:
            setResult(resultCode, data);
            // If the result was 'OK', then finish the activity.
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
            break;
        }
    }
}
