package com.concur.mobile.core.expense.travelallowance;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

public class GetItinerariesRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetItinerariesRequest.class.getSimpleName();
    private String rptKey;

    private GetTAItinerariesResultParser itinParser;

    public GetItinerariesRequest(Context context, int id, BaseAsyncResultReceiver receiver, String rptKey) {
        super(context, id, receiver);
        this.rptKey = rptKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetTAItineraries/" + rptKey;
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        itinParser = new GetTAItinerariesResultParser();
        parser.registerParser(itinParser, "Itinerary");

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
        resultData.putBoolean(IS_SUCCESS, true);
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        core.setTAItinerary(itinParser.getItinerary());

        return RESULT_OK;
    }

}
