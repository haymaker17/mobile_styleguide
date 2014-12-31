/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.concur.mobile.core.travel.air.activity.AirResultAllListItem;
import com.concur.mobile.core.travel.air.activity.AirSearchResultsSummary;
import com.concur.mobile.core.travel.air.data.AirlineEntry;
import com.concur.mobile.core.travel.air.service.AirFilterRequest;
import com.concur.mobile.core.travel.air.service.AirSearchReply;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.travel.service.GovAirSearchReply;
import com.concur.mobile.gov.util.Const;
import com.concur.mobile.gov.view.GovHeaderListItem;

/**
 * @author Chris N. Diaz
 * 
 */
public class GovAirSearchResultsSummary extends AirSearchResultsSummary {

    private static final String CLS_TAG = GovAirSearchResultsSummary.class.getSimpleName();

    /**
     * Default constructor.
     */
    public GovAirSearchResultsSummary() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.core.activity.travel.AirSearchResultsSummary#getAirSearchResultsAdapter(com.concur.core.service.AirSearchReply)
     */
    @Override
    protected BaseAdapter getAirSearchResultsAdapter(AirSearchReply airReply) {
        return new AirSearchResultsAdapter(this, (GovAirSearchReply) airReply);
    }

    /**
     * Will send a request to save filter air information.
     */
    @Override
    protected void sendAirFilterRequest(String airlineCode, String rateType) {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            registerAirFilterReceiver();
            airFilterRequest = govService.getFilteredFlights(airlineCode, rateType);
            if (airFilterRequest == null) {
                Log.e(com.concur.mobile.core.util.Const.LOG_TAG, CLS_TAG
                    + ".sendAirFilterRequest: unable to create request to filter air booking results!");
                unregisterAirFilterReceiver();
            } else {
                // Set the request object on the receiver.
                airFilterReceiver.setServiceRequest(airFilterRequest);
                // Show the attendee form progress dialog.
                showDialog(AIR_FILTER_RESULTS_PROGRESS_DIALOG);
            }
        } else {
            showDialog(com.concur.mobile.core.util.Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.activity.travel.AirSearchResultsSummary#getAirResultsListIntent()
     */
    @Override
    protected Intent getAirResultsListIntent() {
        Intent resultsIntent = new Intent(this, GovAirResultsList.class);

        Intent i = getIntent();
        String authNum = i.getStringExtra(Const.EXTRA_GOV_EXISTING_TA_NUMBER);
        String perdiemLocId = i.getStringExtra(Const.EXTRA_GOV_PER_DIEM_LOC_ID);
        if (i.hasExtra(Flurry.PARAM_NAME_BOOKED_FROM)) {
            resultsIntent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, i.getStringExtra(Flurry.PARAM_NAME_BOOKED_FROM));
        }
        // Pass the auth number and per diem loc id.
        resultsIntent.putExtra(Const.EXTRA_GOV_EXISTING_TA_NUMBER, authNum);
        resultsIntent.putExtra(Const.EXTRA_GOV_PER_DIEM_LOC_ID, perdiemLocId);

        return resultsIntent;
    }

    // ###################### INNER CLASSES ##################### //

    class AirSearchResultsAdapter extends BaseAdapter {

        /**
         * Inner class to handle clicks on the rows.
         */
        protected class ResultRowClickHandler implements View.OnClickListener {

            protected String airlineCode;
            protected String rateType;

            ResultRowClickHandler(String code, String rateType) {
                this.airlineCode = code;
                this.rateType = rateType;
            }

            public void onClick(View v) {
                sendAirFilterRequest(airlineCode, rateType);
            }

        }

        private static final int HEADER_VIEW_TYPE = 0;
        private static final int ALL_VIEW_TYPE = 1;
        private static final int AIRLINE_VIEW_TYPE = 2;

        private Context context;
        private ArrayList<ListItem> items = new ArrayList<ListItem>();
        private ArrayList<Double> lowestCost = new ArrayList<Double>();

        public AirSearchResultsAdapter(Context context, GovAirSearchReply reply) {
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

        void setItems(GovAirSearchReply reply) {
            items.clear();
            lowestCost.clear();
            if (reply != null) {

                // Build the stop groups into a new list with the groups organized
                // First, an everything all row
                GovAirResultAllListItem allItemHeader = new GovAirResultAllListItem(AirResultAllListItem.ALL_STOP_GROUPS, reply.getResultCount(),
                    ALL_VIEW_TYPE);
                ListItem li = allItemHeader;
                items.add(li);

                // Now iterate the groups
                Map<String, List<AirlineEntry>> govRateTypes = reply.govRateTypes;

                for (GovRateType rateType : GovRateType.values()) {
                    List<AirlineEntry> aes = govRateTypes.get(rateType.type);

                    if (aes == null) {
                        continue;
                    }

                    // Add the header for the group
                    final GovHeaderListItem govHeaderItem = new GovHeaderListItem(rateType.displayName, HEADER_VIEW_TYPE);
                    items.add(govHeaderItem);

                    // And then the rows
                    int choiceCount = 0;
                    for (AirlineEntry ae : aes) {
                        items.add(new GovAirResultSummaryListItem(rateType, ae, AIRLINE_VIEW_TYPE));
                        choiceCount += ae.numChoices;
                        lowestCost.add(ae.lowestCost);
                    }
                    govHeaderItem.setCount(choiceCount);
                    govHeaderItem.setRateType(rateType);
                    govHeaderItem.setOnClick(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            sendAirFilterRequest(AirFilterRequest.WILDCARD, govHeaderItem.getRateType().type);
                        }
                    });

                }
                Collections.sort(lowestCost);
                allItemHeader.setLowest(lowestCost.get(0));
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ListItem item = items.get(position);
            View v = item.buildView(context, convertView, parent);

            View.OnClickListener listener = null;

            switch (item.getListItemViewType()) {
            case ALL_VIEW_TYPE:
                int stops = ((AirResultAllListItem) item).stops;
                if (stops == AirResultAllListItem.ALL_STOP_GROUPS) {
                    listener = new ResultRowClickHandler(AirFilterRequest.WILDCARD, AirFilterRequest.WILDCARD);
                }
                break;
            case AIRLINE_VIEW_TYPE:
                String airlineCode = ((GovAirResultSummaryListItem) item).airline.airlineCode;
                listener = new ResultRowClickHandler(airlineCode, ((GovAirResultSummaryListItem) item).rateType.type);
                break;
            }

            v.setOnClickListener(listener);

            return v;
        }
    }
}