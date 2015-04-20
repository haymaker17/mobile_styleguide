package com.concur.mobile.platform.travel.search.hotel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.platform.common.formfield.FormField;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.List;

/**
 * An extension of <code>PlatformAsyncTaskLoader</code> for the purpose of performing a hotel booking. <br>
 * <br>
 * Performs Hotel Booking task
 */
public class HotelBookingLoader extends PlatformAsyncTaskLoader<HotelBookingRESTResult> {

    private static final String CLS_TAG = "HotelBookingLoader";
    //  public String bicCode;

    public String ccId;

    public String chainCode;

    public String propertyId;

    //   public String propertyName;

    //   public String sellSource;

    public String tripId;

    public String travelProgramId;

    // public String hotelReason;

    //public String hotelReasonCode;

    public boolean redeemTravelPoints;

    public List<ViolationReason> violationReasons;

    public String bookingURL;

    public List<FormField> customFields;

    protected HotelBookingRESTResult bookingResult;

    /**
     * Contains the parsed MWS response
     */
    private MWSResponse<HotelBookingRESTResult> mwsResp;

    public HotelBookingLoader(Context context, String ccId, String tripId, List<ViolationReason> violationReasons,
            List<FormField> customFields, String travelProgramId, boolean redeemTravelPoints, String bookingURL) {

        super(context);

        this.ccId = ccId;
        this.travelProgramId = travelProgramId;
        this.tripId = tripId;
        this.redeemTravelPoints = redeemTravelPoints;
        this.violationReasons = violationReasons;
        this.customFields = customFields;
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
        // Set the access token.
        String accessToken = PlatformProperties.getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            connection.addRequestProperty(PlatformAsyncTaskLoader.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
        }
        connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);

        try {
            connection.setRequestMethod(REQUEST_METHOD_POST);
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

        if (customFields != null && customFields.size() > 0) {
            {
                JsonArray fieldsArray = new JsonArray();
                for (FormField tcf : customFields) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", tcf.getId());
                    obj.addProperty("value", tcf.getValue());
                    fieldsArray.add(obj);
                }
                if (fieldsArray.size() > 0) {
                    requestBody.add("customFields", gson.toJsonTree(fieldsArray));
                }
            }

        }

        postBody = requestBody.toString();

        return postBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.io.InputStream)
     */
    @Override
    protected HotelBookingRESTResult parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        bookingResult = null;
        try {
            // prepare the object Type expected in MWS response 'data' element
            Type type = new TypeToken<MWSResponse<HotelBookingRESTResult>>() {

            }.getType();

            mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), type);

            if (mwsResp != null) {
                if (mwsResp.getData() != null) {
                    bookingResult = ((HotelBookingRESTResult) mwsResp.getData());

                    if (bookingResult != null && bookingResult.recordLocator != null
                            && bookingResult.itineraryLocator != null) {
                        Log.d(Const.LOG_TAG, "\n\n\n ****** Hotel Booking successful with recordLocator : "
                                + bookingResult.recordLocator + " and with itineraryLocator : "
                                + bookingResult.itineraryLocator);

                        // log the error message

                    } else {
                        Log.d(Const.LOG_TAG, "\n\n\n ****** Hotel Booking successful but no data");
                    }
                } else {
                    Log.d(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
                    Log.d(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
                    bookingResult.error = mwsResp.getErrors().size() > 0 ? mwsResp.getErrors().get(0) : null;
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
        return bookingResult;
    }

}
