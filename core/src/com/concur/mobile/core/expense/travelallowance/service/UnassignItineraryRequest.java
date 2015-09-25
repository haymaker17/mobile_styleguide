package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.service.parser.StatusParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by D049515 on 09.09.2015.
 */
public class UnassignItineraryRequest extends CoreAsyncRequestTask {

    private String rptKey;
    private String itinKey;

    private StatusParser statusParser;

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
        int result = RESULT_OK;

        // register the parser of interest
        statusParser = new StatusParser();
        parser.registerParser(statusParser, "Response");

        try {
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
        if (statusParser != null) {
            resultData = statusParser.getResultData();
        }
        return RESULT_OK;
    }
}
