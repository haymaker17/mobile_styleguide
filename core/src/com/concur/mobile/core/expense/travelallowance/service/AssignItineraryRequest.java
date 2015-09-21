package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

/**
 * Created by D049515 on 09.09.2015.
 */
public class AssignItineraryRequest extends CoreAsyncRequestTask {

    private String rptKey;
    private String itinKey;

    private long startMillis;


    public AssignItineraryRequest(Context context, BaseAsyncResultReceiver receiver, String rptKey, String itinKey) {
        super(context, 0, receiver);
        this.rptKey = rptKey;
        this.itinKey = itinKey;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText("Request", "Pre Execute", "Call"));
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText("Request", "Post Execute", "Call"));
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/AssignItinerary/"+ itinKey + "/" + rptKey;
    }

    @Override
    protected int parse(CommonParser parser) {
        return 0;
    }
}
