package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.service.parser.GetTAItinerariesResponseParser;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetTAItinerariesRequest extends CoreAsyncRequestTask {

	public static final String CLASS_TAG = GetTAItinerariesRequest.class
			.getSimpleName();
	private String rptKey;

	private GetTAItinerariesResponseParser parser;

	private List<Itinerary> itineraryList;

    private boolean isManager;

    private long startMillis;

    private long parserStartMillis;

	public GetTAItinerariesRequest(Context context,
			BaseAsyncResultReceiver receiver, String rptKey, boolean isManager) {
		super(context, 0, receiver);
		this.rptKey = rptKey;
        this.isManager = isManager;
	}


    @Override
    protected void onPreExecute() {
        startMillis = System.currentTimeMillis();
        super.onPreExecute();
    }

    @Override
	protected String getServiceEndpoint() {
        String endPoint = "/Mobile/TravelAllowance/GetTAItineraries/" + rptKey;
        if(isManager) {
            return endPoint + "/MANAGER";
        } else {
            return endPoint;
        }
	}

	@Override
	protected int parse(CommonParser parser) {
        parserStartMillis = System.currentTimeMillis();
		int result = RESULT_OK;

		// register the parser of interest
		this.parser = new GetTAItinerariesResponseParser();
		parser.registerParser(this.parser, "Itinerary");

		try {
			Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "parse", "Start parsing itineraries..."));
			parser.parse();
		} catch (XmlPullParserException e) {
			result = RESULT_ERROR;
			e.printStackTrace();
		} catch (IOException e) {
			result = RESULT_ERROR;
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected int onPostParse() {
        long currentMillisParser = System.currentTimeMillis();
		Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPostParse",
				"Parsing time = " + (currentMillisParser - parserStartMillis) + "ms"));

        resultData.putBoolean(IS_SUCCESS, true);
		this.itineraryList = parser.getItineraryList();
		ArrayList<Itinerary> itinArrayList = new ArrayList<Itinerary>(this.itineraryList);
		resultData.putSerializable(BundleId.ITINERARY_LIST, itinArrayList);

        long currentMillis = System.currentTimeMillis();
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPostParse",
				"Request total = " + (currentMillis - startMillis) + "ms"));

        return RESULT_OK;
	}

	public List<Itinerary> getItineraryList() {
		return itineraryList;
	}

}
