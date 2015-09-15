package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

/**
 * Created by D049515 on 09.09.2015.
 */
public class UnassignItineraryRequest extends CoreAsyncRequestTask {

    private String rptKey;
    private String itinKey;

    private long startMillis;


    public UnassignItineraryRequest(Context context, BaseAsyncResultReceiver receiver, String rptKey, String itinKey) {
        super(context, 0, receiver);
        this.rptKey = rptKey;
        this.itinKey = itinKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/UnAssignItinerary/"+ itinKey + "/" + rptKey;
    }

    @Override
    protected int parse(CommonParser parser) {
        return 0;
    }
}
