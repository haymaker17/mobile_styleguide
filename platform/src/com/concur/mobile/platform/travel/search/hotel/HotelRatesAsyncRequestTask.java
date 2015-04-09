package com.concur.mobile.platform.travel.search.hotel;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.travel.provider.TravelUtilHotel;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;

/**
 * Performs Get Hotel Rates task
 *
 * @author tejoa
 */
public class HotelRatesAsyncRequestTask extends PlatformAsyncRequestTask {

    /**
     * Contains the key to retrieve the current result set from the data bundle.
     */
    public static final String HOTEL_RATES_RESULT_EXTRA_KEY = "HotelRatesResult";
    private static final String CLS_TAG = "HotelRatesAsyncRequestTask";
    public String ratesURL;

    public long hotelId;

    public long hotelSearchId;

    public HotelRatesRESTResult hotelRateResult;

    public Hotel hotel;

    /**
     * Contains the parsed MWS response
     */
    private MWSResponse<HotelRatesRESTResult> mwsResp;

    public HotelRatesAsyncRequestTask(Context context, int id, BaseAsyncResultReceiver receiver, String ratesURL,
            long hotelId, long hotelSearchId) {

        super(context, id, receiver);

        this.ratesURL = ratesURL;
        this.hotelId = hotelId;
        this.hotelSearchId = hotelSearchId;
    }

    @Override
    protected String getServiceEndPoint() {
        return ratesURL;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        connection.setReadTimeout(60000);
        connection.setRequestProperty(PlatformAsyncRequestTask.HEADER_CONTENT_TYPE,
                PlatformAsyncRequestTask.CONTENT_TYPE_JSON);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.io.InputStream)
     */
    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        hotelRateResult = null;
        try {
            // prepare the object Type expected in MWS response 'data' element
            Type type = new TypeToken<MWSResponse<HotelRatesRESTResult>>() {

            }.getType();

            mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), type);

            if (mwsResp != null) {
                if (mwsResp.getData() != null) {
                    hotelRateResult = ((HotelRatesRESTResult) mwsResp.getData());

                    hotel = hotelRateResult.hotel;

                    if (hotel != null && hotel.rates != null && hotel.rates.size() > 0) {
                        Log.i(Const.LOG_TAG,
                                "\n\n\n ******Get Hotel Rates successfull with count : " + hotel.rates.size());
                        resultData.putSerializable(HotelRatesAsyncRequestTask.HOTEL_RATES_RESULT_EXTRA_KEY,
                                (Serializable) hotelRateResult);
                        if (hotelId != 0 && hotelSearchId != 0) {

                            // insert violations
                            TravelUtilHotel
                                    .insertHotelViolations(getContext().getContentResolver(), (int) hotelSearchId,
                                            hotelRateResult.violations, true);

                            // insert rates
                            TravelUtilHotel.bulkInsertHotelRateDetail(getContext().getContentResolver(), (int) hotelId,
                                    hotel.rates);

                        }

                    } else {
                        // log the error message
                        Log.i(Const.LOG_TAG, "\n\n\n ****** Hotel Booking successfull but no data with rates");
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
