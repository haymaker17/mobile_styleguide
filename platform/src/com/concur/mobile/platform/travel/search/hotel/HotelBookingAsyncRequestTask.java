package com.concur.mobile.platform.travel.search.hotel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of performing a hotel search. <br>
 * <br>
 * Performs Hotel Booking task
 * 
 */
public class HotelBookingAsyncRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "HotelBookingAsyncRequestTask";

    /**
     * Contains the key to retrieve the current result set from the data bundle.
     */
    public static final String HOTEL_BOOKING_RESULT_EXTRA_KEY = "HotelBookingResult";

    public String bicCode;

    public String ccId;

    public String chainCode;

    public String propertyId;

    public String propertyName;

    public String sellSource;

    public String tripId;

    public String travelProgramId;

    public String hotelReason;

    public String hotelReasonCode;

    // public List<TravelCustomField> fields;

    public boolean redeemTravelPoints;

    public List<ViolationReason> violationReasons;

    public String bookingURL;

    /**
     * Contains the parsed MWS response
     */
    private MWSResponse<HotelBookingRESTResult> mwsResp;

    public HotelBookingAsyncRequestTask(Context context, int id, BaseAsyncResultReceiver receiver, String ccId,
            String tripId, List<ViolationReason> violationReasons, String travelProgramId, boolean redeemTravelPoints,
            String bookingURL) {

        super(context, id, receiver);

        this.ccId = ccId;
        this.travelProgramId = travelProgramId;
        this.tripId = tripId;
        this.redeemTravelPoints = redeemTravelPoints;
        this.violationReasons = violationReasons;

        this.bookingURL = bookingURL;
    }

    @Override
    protected String getServiceEndPoint() {
        return bookingURL;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        connection.setReadTimeout(90000);
        connection.setRequestProperty(PlatformAsyncRequestTask.HEADER_CONTENT_TYPE,
                PlatformAsyncRequestTask.CONTENT_TYPE_JSON);
        try {
            connection.setRequestMethod(PlatformAsyncRequestTask.REQUEST_METHOD_POST);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#getPostBody()
     */
    @Override
    protected String getPostBody() {
        String postBody = null;
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(violationReasons);
        JsonObject requestBody = new JsonObject();
        if (ccId != null) {
            requestBody.addProperty("creditCardId", ccId);
        }
        if (tripId != null) {
            requestBody.addProperty("tripId", tripId);
        }
        if (travelProgramId != null) {
            requestBody.addProperty("travelProgramId", travelProgramId);
        }
        if (redeemTravelPoints) {
            requestBody.addProperty("redeemPoints", redeemTravelPoints);
        }
        if (jsonElement != null) {
            requestBody.add("violations", jsonElement);
        }

        // TODO - custom fields

        postBody = requestBody.toString();

        return postBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.io.InputStream)
     */
    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        HotelBookingRESTResult bookingResult = null;
        try {
            // prepare the object Type expected in MWS response 'data' element
            Type type = new TypeToken<MWSResponse<HotelBookingRESTResult>>() {}.getType();

            mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), type);

            if (mwsResp != null) {
                if (mwsResp.getData() != null) {
                    bookingResult = ((HotelBookingRESTResult) mwsResp.getData());

                    if (bookingResult != null && bookingResult.recordLocator != null
                            && bookingResult.itineraryLocator != null) {
                        Log.i(Const.LOG_TAG, "\n\n\n ******Hotel Booking successfull with recordLocator : "
                                + bookingResult.recordLocator);
                        resultData.putSerializable(HotelBookingAsyncRequestTask.HOTEL_BOOKING_RESULT_EXTRA_KEY,
                                (Serializable) bookingResult);
                        // log the error message

                    } else {
                        Log.i(Const.LOG_TAG, "\n\n\n ****** Hotel Booking successfull but no data");
                    }
                } else {
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
                    String errorMessage = mwsResp.getErrors().get(0).getSystemMessage();
                    Log.e(Const.LOG_TAG, errorMessage);
                    resultData.putString("mwsErrorMessage", errorMessage);
                }
            }

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                }
            }
        }
        return result;
    }

    @Override
    protected int onPostParse() {
        int resultcode = RESULT_ERROR;
        setMWSResponseStatusIntoResultBundle(mwsResp);
        if (mwsResp.getData() != null) {
            resultcode = RESULT_OK;

        }
        return resultcode;
    }

}
