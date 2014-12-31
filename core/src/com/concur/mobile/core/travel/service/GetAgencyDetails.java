package com.concur.mobile.core.travel.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.travel.service.parser.AgencyAssistanceAdditionalParser;
import com.concur.mobile.core.travel.service.parser.AgencyParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * Async task for getting the travel agent information
 * 
 * @author RatanK
 * 
 */
public class GetAgencyDetails extends CoreAsyncRequestTask {

    private MWSResponseParser mwsRespParser;
    private AgencyParser agencyParser;
    private AgencyAssistanceAdditionalParser agencyAdditionalParser;
    private String itinLocator;

    public final static String GET_AGENCY_DETAILS_IS_SUCCESS = "get_agency_details_is_success";
    public final static String GET_AGENCY_DETAILS_RESPONSE_MESSAGE = "get_agency_details_response_message";
    public final static String AGENT_PREFERRED_PHONE_NUMBER = "agent_preferred_phone_number";
    public final static String TRIP_RECORD_LOCATOR_FOR_AGENT = "trip_record_locator_for_agent";

    public GetAgencyDetails(Context context, int id, BaseAsyncResultReceiver receiver, String itinLocator) {
        super(context, id, receiver);
        this.itinLocator = itinLocator;
    }

    @Override
    protected String getServiceEndpoint() {
        StringBuilder strBldr = new StringBuilder("/Mobile/Agency/GetAgencyAssistance");
        if (itinLocator != null) {
            strBldr.append("?itinLocator=" + itinLocator);
        }
        return strBldr.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        mwsRespParser = new MWSResponseParser();
        agencyParser = new AgencyParser();

        // register the parsers of interest
        parser.registerParser(agencyParser, "Agency");

        if (itinLocator != null) {
            // to parse miscellaneous elements outside the Agency elements i.e. like TripRecordLocator etc. only when the
            // itinlocator is
            // passed.
            agencyAdditionalParser = new AgencyAssistanceAdditionalParser();
            parser.registerParser(agencyAdditionalParser, "Response");
        }

        parser.registerParser(mwsRespParser, "MWSResponse");

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
        int resultcode = RESULT_ERROR;

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();

        // response will have isSuccess false but still have response with valid data and errors for record locator
        if (agencyParser.agency != null) {
            // set the preferred phone number and trip record locator in the result bundle for the activity
            resultData.putString(AGENT_PREFERRED_PHONE_NUMBER, agencyParser.agency.getPreferredPhoneNumber());
            if (itinLocator != null) {
                resultData.putString(TRIP_RECORD_LOCATOR_FOR_AGENT, agencyAdditionalParser.tripRecordLocator);
            }

            // MOB-14280 - ItinLocator changes when passive segments are created,so get the new ItinLocator and use that
            resultData.putString(Const.EXTRA_ITIN_LOCATOR, agencyAdditionalParser.itinLocator);

            resultcode = RESULT_OK;
            resultData.putBoolean(GET_AGENCY_DETAILS_IS_SUCCESS, true);
        }

        // check if response is success
        if (!reqStatus.isSuccess()) {
            // log the error message
            String errorMessage = "could not retrieve agency details";
            if (reqStatus.getErrors().isEmpty()) {
                Log.e(Const.LOG_TAG, errorMessage);
            } else {
                // TODO loop thru all the errors, usermessage for showing to user and systemmessage is for logging
                errorMessage = reqStatus.getErrors().get(0).getSystemMessage();
                Log.e(Const.LOG_TAG, errorMessage);
            }
            resultData.putString(GET_AGENCY_DETAILS_RESPONSE_MESSAGE, errorMessage);
        }

        return resultcode;
    }

}
