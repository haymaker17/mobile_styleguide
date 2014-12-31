package com.concur.mobile.core.travel.hotel.service;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.travel.hotel.data.HotelChoice;
import com.concur.mobile.core.util.Const;

/**
 * 
 * @author RatanK
 * 
 */
public class GetHotelsPricing extends GetHotels {

    private static final String CLS_TAG = GetHotelsPricing.class.getSimpleName();

    private static final String SERVICE_END_POINT = "/Mobile/Hotel/PollSearchResults";

    private String pollingId;

    public GetHotelsPricing(Context context, int id, BaseAsyncResultReceiver receiver, Calendar dateEnd,
            Calendar dateStart, String hotelChain, String lat, String lon, String radius, String scale,
            Integer startIndex, Integer count, String pollingId) {

        super(context, id, receiver, dateEnd, dateStart, hotelChain, lat, lon, radius, scale, startIndex, count);

        this.pollingId = pollingId;
    }

    @Override
    protected String getServiceEndpoint() {
        StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
        if (pollingId != null) {
            strBldr.append('/');
            strBldr.append(pollingId);
        }
        return strBldr.toString();
    }

    @Override
    protected String getPostBody() {
        return super.getPostBody();
    }

    @Override
    protected int onPostParse() {

        super.onPostParse();

        int resultcode = RESULT_ERROR;

        if (reqStatus.isSuccess()) {

            resultcode = RESULT_OK;

            ConcurCore core = (ConcurCore) ConcurCore.getContext();
            HotelSearchReply reply = null;

            // should always contain the key when this end point invoked, so this case should not happen, so remove after
            // testing
            if (resultData.containsKey("HotelSearchReply")) {
                reply = (HotelSearchReply) resultData.getSerializable("HotelSearchReply");
                if (reply.isFinal) {
                    // overwrite the reply in the app object as this contains full set of data
                    core.setHotelSearchResults(reply);
                } else {
                    // collect the new set of hotel choices to be updated in the UI
                    ArrayList<String> propertyIdsPriced = new ArrayList<String>();
                    ArrayList<HotelChoice> hotelChoicesToBeUpdated = new ArrayList<HotelChoice>();

                    // collect the new set of priced properties which needs to be sent to UI
                    boolean newPricedPropertyId = true;
                    for (HotelChoice choice : reply.hotelChoices) {
                        newPricedPropertyId = (core.getPropertyIdsAlreadyPriced().contains(choice.propertyId) ? false
                                : true);
                        if (newPricedPropertyId) {
                            propertyIdsPriced.add(choice.propertyId);
                            hotelChoicesToBeUpdated.add(choice);
                        }
                    }

                    // pass the new set of hotel choices for the UI to refresh
                    if (propertyIdsPriced.size() > 0) {
                        // update the app object for future reference
                        core.getPropertyIdsAlreadyPriced().addAll(propertyIdsPriced);

                        resultData.putSerializable("HotelChoicesToBeUpdated", hotelChoicesToBeUpdated);
                    }
                }
            } else {
                reply = core.getHotelSearchResults();
            }

        } else {
            // log the error message
            String errorMessage = "could not perform hotel pricing";
            if (reqStatus.getErrors().isEmpty()) {
                Log.e(Const.LOG_TAG, errorMessage);
            } else {
                errorMessage = reqStatus.getErrors().get(0).getSystemMessage();
                Log.e(Const.LOG_TAG, errorMessage);
                resultData.putString(Const.MWS_ERROR_MESSAGE, errorMessage);
            }
        }
        return resultcode;
    }
}
