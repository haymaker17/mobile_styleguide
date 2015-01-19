package com.concur.mobile.core.travel.air.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.TravelBaseActivity;
import com.concur.mobile.core.travel.air.activity.AirSearch.SearchMode;
import com.concur.mobile.core.travel.air.data.AirlineEntry;
import com.concur.mobile.core.travel.air.service.AirFilterReply;
import com.concur.mobile.core.travel.air.service.AirFilterRequest;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;

public class AirSearchResultsSummary extends TravelBaseActivity implements View.OnClickListener {

    private static final String CLS_TAG = AirSearchResultsSummary.class.getSimpleName();

    protected static final int AIR_FILTER_RESULTS_PROGRESS_DIALOG = 0;

    protected static final String EXTRA_AIR_FILTER_RECEIVER_KEY = "air.filter.receiver";

    Intent filterIntent;

    protected SearchMode searchMode;

    protected LocationChoice departLocation;
    protected LocationChoice arriveLocation;

    protected Calendar departDateTime;
    protected Calendar returnDateTime;

    protected AirFilterReceiver airFilterReceiver;
    protected IntentFilter airFilterFilter;
    protected AirFilterRequest airFilterRequest;

    private boolean isDlgShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_search_results_summary);

        initValues();
        initUI();
    }

    public void onClick(View v) {
    }

    protected void initValues() {
        Intent i = getIntent();

        filterIntent = getAirResultsListIntent();

        if (i.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            filterIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, i.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        filterIntent.putExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED,
                getIntent().getBooleanExtra(Const.EXTRA_TRAVEL_VOICE_BOOK_INITIATED, false));

        searchMode = SearchMode.None;
        String mode = i.getStringExtra(Const.EXTRA_SEARCH_MODE);
        if (mode != null) {
            searchMode = SearchMode.valueOf(mode);
        }
        filterIntent.putExtra(Const.EXTRA_SEARCH_MODE, searchMode.name());
        final Bundle departLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_FROM);
        final Bundle arriveLocBundle = i.getBundleExtra(Const.EXTRA_SEARCH_LOC_TO);

        departLocation = new LocationChoice(departLocBundle);
        arriveLocation = new LocationChoice(arriveLocBundle);
        departDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_DEPART);

        filterIntent.putExtra(Const.EXTRA_SEARCH_LOC_FROM, departLocBundle);
        filterIntent.putExtra(Const.EXTRA_SEARCH_LOC_TO, arriveLocBundle);
        filterIntent.putExtra(Const.EXTRA_SEARCH_DT_DEPART, departDateTime);

        boolean refundableOnly = i.getBooleanExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, false);
        filterIntent.putExtra(Const.EXTRA_SEARCH_REFUNDABLE_ONLY, refundableOnly);

        if (searchMode != SearchMode.OneWay) {
            returnDateTime = (Calendar) i.getSerializableExtra(Const.EXTRA_SEARCH_DT_RETURN);
            filterIntent.putExtra(Const.EXTRA_SEARCH_DT_RETURN, returnDateTime);
        }

        // Restore any non-configuration data.
        restoreReceivers();
    }

    protected Intent getAirResultsListIntent() {
        return new Intent(this, AirResultsList.class);
    }

    protected void initUI() {
        // The header
        getSupportActionBar().setTitle(R.string.air_search_results_title);

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

        // Da List
        final ConcurCore app = (ConcurCore) getApplication();
        final AirSearchReply reply = app.getAirSearchResults();

        if (reply != null && reply.benchmark != null) {
            String formattedBenchmarkPrice = null;

            if (reply.benchmark.getPrice() != null) {
                formattedBenchmarkPrice = FormatUtil.formatAmount(reply.benchmark.getPrice(), this.getResources()
                        .getConfiguration().locale, reply.benchmark.getCrnCode(), true, true);
            }

            String travelPointsInBank = null;

            if (reply.travelPointsBank != null && reply.travelPointsBank.getPointsAvailableToSpend() != null) {
                travelPointsInBank = Integer.toString(reply.travelPointsBank.getPointsAvailableToSpend());
            }

            TravelPointsConfig travelPointsConfig = ((ConcurCore) getApplication()).getUserConfig().travelPointsConfig;
            if (travelPointsConfig != null && travelPointsConfig.isAirTravelPointsEnabled()) {
                initAirTravelPointsHeader(formattedBenchmarkPrice,
                        R.string.travel_points_air_booking_workflow_p2b_header, travelPointsInBank,
                        R.string.travel_points_air_booking_workflow_points_header);
            } else {
                initAirTravelPointsHeader(formattedBenchmarkPrice,
                        R.string.travel_points_air_booking_workflow_p2b_header, null, -1);
            }

            filterIntent.putExtra(EXTRA_FORMATTED_PRICE_TO_BEAT_KEY, formattedBenchmarkPrice);
            filterIntent.putExtra(EXTRA_TRAVEL_POINTS_IN_BANK_KEY, travelPointsInBank);

        }
        // end of Travel Points

        BaseAdapter resultAdapter = getAirSearchResultsAdapter(reply);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(resultAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Don't do anything for headers
                if (id != -1) {
                    // Intent i = new Intent(TripList.this, SegmentList.class);
                    // i.putExtra(Const.EXTRA_ITIN_LOCATOR, id);
                    // startActivity(i);
                }

            }
        });

    }

    /**
     * Creates a new AirSearchResultsAdapter used for displaying the air search results.
     */
    protected BaseAdapter getAirSearchResultsAdapter(AirSearchReply airReply) {
        return new AirSearchResultsAdapter(this, airReply);
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
        case AIR_FILTER_RESULTS_PROGRESS_DIALOG: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.air_search_filter_progress_message));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    if (airFilterRequest != null) {
                        airFilterRequest.cancel();
                    }
                    dialog.dismiss();
                }
            });
            dlg = progDlg;
            isDlgShown = true;
            break;
        }
        default: {
            dlg = super.onCreateDialog(id);
            break;
        }
        }
        return dlg;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case AirSearch.RESULTS_ACTIVITY_CODE:
            setResult(resultCode, data);
            // If the result was 'OK', then finish the activity.
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
            break;
        }
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
            // Save a reference to the air filter receiver.
            if (airFilterReceiver != null) {
                // Clear the activity reference, it will be reset in the 'initValues' method.
                airFilterReceiver.setActivity(null);
                // Store the reference in the retainer.
                retainer.put(EXTRA_AIR_FILTER_RECEIVER_KEY, airFilterReceiver);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore any receiver waiting on an air filter response.
            if (retainer.contains(EXTRA_AIR_FILTER_RECEIVER_KEY)) {
                airFilterReceiver = (AirFilterReceiver) retainer.get(EXTRA_AIR_FILTER_RECEIVER_KEY);
                if (airFilterReceiver != null) {
                    airFilterReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer has null value for air filter receiver!");
                }
            }
        }
    }

    /**
     * Will send a request to save filter air information.
     */
    protected void sendAirFilterRequest(String airlineCode, String numStops) {
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerAirFilterReceiver();
            airFilterRequest = concurService.getFilteredFlights(airlineCode, numStops);
            if (airFilterRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendAirFilterRequest: unable to create request to filter air booking results!");
                unregisterAirFilterReceiver();
            } else {
                // Set the request object on the receiver.
                airFilterReceiver.setServiceRequest(airFilterRequest);
                // Show the attendee form progress dialog.
                showDialog(AIR_FILTER_RESULTS_PROGRESS_DIALOG);
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    @Override
    protected void updateOfflineHeaderBar(boolean available) {
        super.updateOfflineHeaderBar(available);
        if (!available) {
            if (isDlgShown) {
                dismissDialog(AIR_FILTER_RESULTS_PROGRESS_DIALOG);
            }
            // cancel task;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateOfflineHeaderBar: offline mode detect!");
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * Will register an instance of <code>AirFilterReceiver</code> with the application context and set the
     * <code>airFilterReceiver</code> attribute.
     */
    protected void registerAirFilterReceiver() {
        if (airFilterReceiver == null) {
            airFilterReceiver = new AirFilterReceiver(this);
            if (airFilterFilter == null) {
                airFilterFilter = new IntentFilter(Const.ACTION_AIR_FILTER_RESULTS);
            }
            getApplicationContext().registerReceiver(airFilterReceiver, airFilterFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAirFilterReceiver: airFilterReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>AirFilterReceiver</code> with the application context and set the
     * <code>airFilterReceiver</code> to <code>null</code>.
     */
    protected void unregisterAirFilterReceiver() {
        if (airFilterReceiver != null) {
            getApplicationContext().unregisterReceiver(airFilterReceiver);
            airFilterReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAirFilterReceiver: airFilterReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to filter air results.
     */
    protected static class AirFilterReceiver extends BaseBroadcastReceiver<AirSearchResultsSummary, AirFilterRequest> {

        private static final String CLS_TAG = AirSearchResultsSummary.CLS_TAG + "."
                + AirFilterReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>AirFilterReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected AirFilterReceiver(AirSearchResultsSummary activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(AirSearchResultsSummary activity) {
            activity.airFilterRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(AIR_FILTER_RESULTS_PROGRESS_DIALOG);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            // TODO Auto-generated method stub
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore ConcurCore = activity.getConcurCore();
            final AirFilterReply reply = ConcurCore.getAirFilterResults();
            if (reply != null) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess: filtered results // " + reply.choices.size());
                activity.startActivityForResult(activity.filterIntent, AirSearch.RESULTS_ACTIVITY_CODE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(AirFilterRequest request) {
            activity.airFilterRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAirFilterReceiver();
        }

    }

    class AirSearchResultsAdapter extends BaseAdapter {

        /**
         * Inner class to handle clicks on the rows.
         */
        protected class ResultRowClickHandler implements View.OnClickListener {

            protected String airlineCode;
            protected String numStops;

            ResultRowClickHandler(String code, String stops) {
                this.airlineCode = code;
                this.numStops = stops;
            }

            public void onClick(View v) {
                sendAirFilterRequest(airlineCode, numStops);
            }

        }

        private static final int HEADER_VIEW_TYPE = 0;
        private static final int ALL_VIEW_TYPE = 1;
        private static final int AIRLINE_VIEW_TYPE = 2;

        private Context context;
        private ArrayList<ListItem> items = new ArrayList<ListItem>();

        public AirSearchResultsAdapter(Context context, AirSearchReply reply) {
            this.context = context;
            setItems(reply);
        }

        public int getCount() {
            return items.size();
        }

        public ListItem getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return items.get(position).isEnabled();
        }

        void setItems(AirSearchReply reply) {
            items.clear();
            if (reply != null) {

                // Build the stop groups into a new list with the groups organized
                // First, an everything all row
                ListItem li = new AirResultAllListItem(AirResultAllListItem.ALL_STOP_GROUPS, reply.getResultCount(),
                        ALL_VIEW_TYPE);
                items.add(li);

                // Now iterate the groups
                Map<Integer, List<AirlineEntry>> stopGroups = reply.stopGroups;
                Set<Integer> keys = stopGroups.keySet();
                Integer[] sortedKeys = new Integer[keys.size()];
                Arrays.sort(keys.toArray(sortedKeys));

                for (Integer stops : sortedKeys) {
                    List<AirlineEntry> aes = stopGroups.get(stops);

                    // Add the header for the group
                    String stopGroupName;
                    if (stops == 0) {
                        stopGroupName = context.getText(R.string.air_search_stopgroup_nonstop).toString();
                    } else if (stops == 1) {
                        stopGroupName = context.getText(R.string.air_search_stopgroup_singular).toString();
                    } else {
                        stopGroupName = Format.localizeText(context, R.string.air_search_stopgroup_plural, stops);
                    }
                    items.add(new HeaderListItem(stopGroupName, HEADER_VIEW_TYPE));

                    // And then the rows
                    int choiceCount = 0;
                    for (AirlineEntry ae : aes) {
                        items.add(new AirResultSummaryListItem(stops, ae, AIRLINE_VIEW_TYPE));
                        choiceCount += ae.numChoices;
                    }

                    // Then the all row for the group
                    li = new AirResultAllListItem(stops, choiceCount, ALL_VIEW_TYPE);
                    items.add(li);
                }
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ListItem item = items.get(position);
            View v = item.buildView(context, convertView, parent);

            View.OnClickListener listener = null;

            switch (item.getListItemViewType()) {
            case ALL_VIEW_TYPE:
                String stopStr = AirFilterRequest.WILDCARD;
                int stops = ((AirResultAllListItem) item).stops;
                if (stops != AirResultAllListItem.ALL_STOP_GROUPS) {
                    stopStr = Integer.toString(stops);
                }
                listener = new ResultRowClickHandler(AirFilterRequest.WILDCARD, stopStr);
                break;
            case AIRLINE_VIEW_TYPE:
                stops = ((AirResultSummaryListItem) item).stopGroup;
                String airlineCode = ((AirResultSummaryListItem) item).airline.airlineCode;
                listener = new ResultRowClickHandler(airlineCode, Integer.toString(stops));
                break;
            }

            v.setOnClickListener(listener);

            return v;
        }
    }
}
