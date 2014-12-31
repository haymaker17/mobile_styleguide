/**
 * 
 */
package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>HotelSearchAsyncRequestTask</code> for the purpose of polling the latest results from a hotel search
 * result set given a polling id.
 */
public class HotelSearchPollAsyncRequestTask extends HotelSearchAsyncRequestTask {

    private static final String CLS_TAG = "HotelSearchPollAsyncRequestTask";

    private static final String SERVICE_END_POINT = "/Mobile/Hotel/PollSearchResults";

    /**
     * Contains the key value to retrieve the list of hotel choices to be updated.
     */
    public static final String HOTEL_CHOICES_TO_BE_UPDATED_EXTRA_KEY = "HotelChoicesToBeUpdated";
    public static final String HOTEL_IDS_PRICED_EXTRA_KEY = "HotelIdsAlreadyPriced";

    /**
     * Contains the hotel search polling id.
     */
    protected String pollingId;

    /**
     * Contains the list of property ids already containing pricing information.
     */
    protected List<String> propertyIdsAlreadyPriced;

    /**
     * Constructs an instance of <code>PollHotelSearchAsyncRequestTask</code> to perform a streaming hotel search.
     * 
     * @param context
     *            contains an application context.
     * @param id
     *            contains the request id.
     * @param receiver
     *            contains the results receiver.
     * @param checkInDate
     *            contains the check-in date.
     * @param checkOutDate
     *            contains the check-out date.
     * @param includeBenchmarks
     *            contains whether to include benchmark data.
     * @param hotelChain
     *            contains the name of a hotel chain.
     * @param includeDepositRequired
     *            contains whether to include "deposit required" hotels.
     * @param lat
     *            contains the latitude.
     * @param lon
     *            contains the longitude.
     * @param perdiemRate
     *            contains a perdiem rate.
     * @param radius
     *            contains the search radius.
     * @param radiusUnits
     *            contains the search radius units.
     * @param start
     *            contains the start index.
     * @param count
     *            contains the count of hotels to retrieve.
     * @param pollingId
     *            contains the search polling id.
     * @param propertyIdsAlreadyPriced
     *            contains the list of property id's that already contain pricing information.
     */
    public HotelSearchPollAsyncRequestTask(Context context, int id, BaseAsyncResultReceiver receiver,
            Calendar checkInDate, Calendar checkOutDate, Boolean includeBenchmarks, String hotelChain,
            Boolean includeDepositRequired, Double lat, Double lon, Double perdiemRate, Integer radius,
            String radiusUnits, Integer start, Integer count, String pollingId, List<String> propertyIdsAlreadyPriced) {

        super(context, id, receiver, checkInDate, checkOutDate, includeBenchmarks, hotelChain, includeDepositRequired,
                lat, lon, perdiemRate, radius, radiusUnits, start, count);

        this.pollingId = pollingId;
        this.propertyIdsAlreadyPriced = propertyIdsAlreadyPriced;
    }

    @Override
    protected String getServiceEndPoint() {
        StringBuilder strBldr = new StringBuilder(SERVICE_END_POINT);
        if (pollingId != null) {
            strBldr.append('/');
            strBldr.append(pollingId);
        }
        return strBldr.toString();
    }

    @Override
    protected int onPostParse() {
        super.onPostParse();

        int resultcode = RESULT_ERROR;

        // Set any MWS response data.
        setMWSResponseStatusIntoResultBundle(reqStatus);

        if (reqStatus.isSuccess()) {

            resultcode = RESULT_OK;

            HotelSearchResult result = null;

            // should always contain the key when this end point invoked, so this case should not happen, so remove after
            // testing
            if (resultData.containsKey(HotelSearchAsyncRequestTask.HOTEL_SEARCH_RESULT_EXTRA_KEY)) {
                result = (HotelSearchResult) resultData
                        .getSerializable(HotelSearchAsyncRequestTask.HOTEL_SEARCH_RESULT_EXTRA_KEY);
                if (!result.isFinal) {
                    // collect the new set of hotel choices to be updated in the UI
                    ArrayList<String> propertyIdsPriced = new ArrayList<String>();
                    ArrayList<HotelChoice> hotelChoicesToBeUpdated = new ArrayList<HotelChoice>();

                    // collect the new set of priced properties which needs to be sent to UI
                    boolean newPricedPropertyId = true;
                    for (HotelChoice choice : result.hotelChoices) {
                        if (propertyIdsAlreadyPriced != null) {
                            newPricedPropertyId = propertyIdsAlreadyPriced.contains(choice.propertyId) ? false : true;
                        }
                        if (newPricedPropertyId) {
                            propertyIdsPriced.add(choice.propertyId);
                            hotelChoicesToBeUpdated.add(choice);
                        }
                    }

                    // pass the new set of hotel choices for the UI to refresh
                    if (propertyIdsPriced.size() > 0) {
                        // Add into the passed in list of property ids already priced.
                        if (propertyIdsAlreadyPriced != null) {
                            propertyIdsAlreadyPriced.addAll(propertyIdsPriced);
                        } else {
                            propertyIdsAlreadyPriced = propertyIdsPriced;
                        }
                        resultData.putSerializable(
                                HotelSearchPollAsyncRequestTask.HOTEL_CHOICES_TO_BE_UPDATED_EXTRA_KEY,
                                hotelChoicesToBeUpdated);
                        // TODO: Making an assumption here that the passed in implementation of <code>java.util.List</code>
                        // implements the 'java.io.Serializable' interface!
                        // TODO: Should ensure that the passed in type implements serializable.
                        resultData.putSerializable(HotelSearchPollAsyncRequestTask.HOTEL_IDS_PRICED_EXTRA_KEY,
                                (Serializable) propertyIdsAlreadyPriced);
                    }
                }
            }
        } else {
            // log the error message
            String errorMessage = "could not perform hotel pricing";
            if (reqStatus.getErrors().isEmpty()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: " + errorMessage);
            } else {
                errorMessage = reqStatus.getErrors().get(0).getSystemMessage();
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: " + errorMessage);
            }
        }
        return resultcode;

    }

}
