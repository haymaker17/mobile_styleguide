package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patricius Komarnicki on 07.09.2015.
 */
public class GetAssignableItinerariesRequest extends CoreAsyncRequestTask {

    /**
     * Parser
     */
    private static class Parser extends BaseParser {

        private static final String ITINERARY_TAG = "Itinerary";

        private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm";

        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);

        private List<AssignableItinerary> itineraryList;

        private AssignableItinerary currentItinerary;


        public List<AssignableItinerary> getItineraryList() {
            if (itineraryList == null) {
                itineraryList = new ArrayList<AssignableItinerary>();
            }
            return itineraryList;
        }

        @Override
        public void startTag(String tag) {
            super.startTag(tag);
            if (ITINERARY_TAG.equals(tag)) {
                currentItinerary = new AssignableItinerary();
            }
        }

        @Override
        public void endTag(String tag) {
            super.endTag(tag);
            if (ITINERARY_TAG.equals(tag)) {
                if (itineraryList == null) {
                    itineraryList = new ArrayList<AssignableItinerary>();
                }
                itineraryList.add(currentItinerary);
            }
        }

        @Override
        public void handleText(String tag, String text) {
            if (text.trim().equals("")) {
                return;
            }
            try {
                switch (tag) {
                case "ItinKey":
                    currentItinerary.setItineraryID(text);
                    break;
                case "Name":
                    currentItinerary.setName(text);
                    break;
                case "StartDateTime":
                    currentItinerary.setStartDateTime(format.parse(text));
                    break;
                case "EndDateTime":
                    currentItinerary.setEndDateTime(format.parse(text));
                    break;
                case "LocationName":
                    currentItinerary.addArrivalLocation(text);
                    break;

                default:
                    break;
                }
            } catch (ParseException e) {
                Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG,
                        "parser.handleText(" + tag + ", " + text + ")", "Date time parser expects: " + DATE_PATTERN));
            }
        }
    }
    /***************/


    private static final String CLASS_TAG = GetAssignableItinerariesRequest.class.getSimpleName();

    private String rptKey;

    private long startMillis;

    private Parser parser;

    public GetAssignableItinerariesRequest(Context context, BaseAsyncResultReceiver receiver, String rptKey) {
        super(context, 0, receiver);
        this.rptKey = rptKey;
    }

    @Override
    protected void onPreExecute() {
        startMillis = System.currentTimeMillis();
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPreExecute",
                "Starting request. Endpoint: " + getServiceEndpoint()));
        super.onPreExecute();
    }

    @Override
    protected String getServiceEndpoint() {
        if (rptKey != null) {
            return "/Mobile/TravelAllowance/GetAssignableItineraries/"+ rptKey;
        } else {
            return "/Mobile/TravelAllowance/GetAssignableItineraries/";
        }
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;
        this.parser = new Parser();
        parser.registerParser(this.parser, "AssignableItineraries");
        
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "parse", "Start parsing..."));
        try {
            parser.parse();
        } catch (XmlPullParserException e) {
            result = RESULT_ERROR;
            Log.e(DebugUtils.LOG_TAG_TA,
                    DebugUtils.buildLogText(CLASS_TAG, "parse", "XmlPullParserException: " + e.getMessage()));
        } catch (IOException e) {
            Log.e(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "parse", "IOException: " + e.getMessage()));
            result = RESULT_ERROR;
        }

        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "parse",
                "Parsing finished. Assignable Itinerary List size: " + this.parser.getItineraryList().size()));
        return result;
    }

    @Override
    protected int onPostParse() {
        resultData.putBoolean(IS_SUCCESS, true);
        resultData.putSerializable(BundleId.ASSIGNABLE_ITINERARIES,
                new ArrayList<AssignableItinerary>(this.parser.getItineraryList()));

        long currentMillis = System.currentTimeMillis();
        Log.d(DebugUtils.LOG_TAG_TA,
                DebugUtils.buildLogText(CLASS_TAG, "", "Request total time: " + (currentMillis - startMillis) + "ms"));
        Log.i(DebugUtils.LOG_TAG_TA,
                DebugUtils.buildLogText(CLASS_TAG, "onPostParse", "Request finished. Result code OK"));

        return RESULT_OK;
    }
}
