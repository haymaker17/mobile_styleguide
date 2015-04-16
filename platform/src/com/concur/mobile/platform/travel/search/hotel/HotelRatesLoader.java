package com.concur.mobile.platform.travel.search.hotel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.travel.provider.TravelUtilHotel;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;

/**
 * Performs Get Hotel Rates task
 *
 * @author tejoa
 */
public class HotelRatesLoader extends PlatformAsyncTaskLoader<HotelRatesRESTResult> {

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

    public HotelRatesLoader(Context context, String ratesURL, long hotelId, long hotelSearchId) {

        super(context);
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
        // Set the access token.
        String accessToken = PlatformProperties.getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            connection.addRequestProperty(PlatformAsyncTaskLoader.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
        }
        connection.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);

    }

    /*
     * parse inputStream
     * 
     */
    @Override
    protected HotelRatesRESTResult parseStream(InputStream is) {
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
                        if (hotelId != 0 && hotelSearchId != 0) {

                            // insert violations
                            TravelUtilHotel
                                    .insertHotelViolations(getContext().getContentResolver(), (int) hotelSearchId,
                                            hotelRateResult.violations, true);

                            // insert rates
                            TravelUtilHotel.bulkInsertHotelRateDetail(getContext().getContentResolver(), (int) hotelId,
                                    hotel.rates);

                        } else {
                            Log.i(Const.LOG_TAG, "\n\n\n ****** hotelRateResult is null");
                        }
                    }
                } else {
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
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
        return hotelRateResult;
    }

}
