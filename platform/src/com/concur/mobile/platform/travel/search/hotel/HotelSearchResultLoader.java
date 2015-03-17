package com.concur.mobile.platform.travel.search.hotel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.travel.provider.TravelUtilHotel;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;
import com.concur.platform.PlatformProperties;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * Async task loader class for the hotel search
 * 
 * @author RatanK
 * 
 */
public class HotelSearchResultLoader extends PlatformAsyncTaskLoader<HotelSearchRESTResult> {

    private static final String CLS_TAG = "HotelSearchRESTResultLoader";

    private static final String SERVICE_END_POINT = "/mobile/travel/v1.0/Hotels";

    /**
     * Contains the key to retrieve the current result set from the data bundle.
     */
    public static final String HOTEL_SEARCH_RESULT_EXTRA_KEY = "HotelSearchResult";

    /**
     * Contains the check-in date.
     */
    protected Calendar checkInDate;

    /**
     * Contains the check-out date.
     */
    protected Calendar checkOutDate;

    /**
     * Contains the latitude.
     */
    protected Double lat;

    /**
     * Contains the longitude.
     */
    protected Double lon;

    /**
     * Contains the perdiem rate.
     */
    protected Double perdiemRate;

    /**
     * Contains the radius.
     */
    protected Integer radius;

    /**
     * Contains the distanceUnit, i.e., 'M', 'K', etc.
     */
    protected String distanceUnit;

    protected String pollingURL;

    /**
     * Contains the parsed MWS response
     */
    private MWSResponse<HotelSearchRESTResult> mwsResp;

    protected Bundle resultData;

    /**
     * Contains search url
     */
    public static String searchUrl;

    /**
     * Contains results object
     */
    protected HotelSearchRESTResult searchResult;

    public HotelSearchResultLoader(Context context, Calendar checkInDate, Calendar checkOutDate, Double lat,
            Double lon, Integer radius, String distanceUnit) {

        super(context);

        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.distanceUnit = distanceUnit;
    }

    /**
     * Configure connection properties. The default implementation sets the user agent, content type to type/xml, connect timeout
     * to 10 seconds, and read timeout to 30 seconds.
     * 
     * @param connection
     *            The open but not yet connected {@link HttpURLConnection} to the server
     */
    @Override
    protected void configureConnection(HttpURLConnection connection) {
        // Set the access token.
        String accessToken = PlatformProperties.getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            connection.addRequestProperty(PlatformAsyncTaskLoader.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
        }
    }

    @Override
    protected void releaseResources(HotelSearchRESTResult data) {
        // TODO Auto-generated method stub

    }

    @Override
    protected HotelSearchRESTResult parseStream(InputStream is) {
        try {
            // prepare the object Type expected in MWS response 'data' element
            Type type = new TypeToken<MWSResponse<HotelSearchRESTResult>>() {}.getType();

            mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), type);

            if (mwsResp != null) {
                if (mwsResp.getData() != null) {
                    searchResult = ((HotelSearchRESTResult) mwsResp.getData());

                    if (searchResult != null && searchResult.searchDone && searchResult.hotels != null
                            && searchResult.hotels.size() > 0) {
                        Log.i(Const.LOG_TAG,
                                "\n\n\n ****** going to insert into travel provider with searchResult.hotels.size() : "
                                        + searchResult.hotels.size());
                        searchResult.searchUrl = searchUrl;
                        // TODO - does this need to be fired in a separate thread?
                        new Thread(new Runnable() {

                            public void run() {
                                TravelUtilHotel.insertHotelDetails(getContext().getContentResolver(), searchResult);
                            }
                        }).start();

                    } else {
                        Log.i(Const.LOG_TAG,
                                "\n\n\n ****** searchResult is null or searchdone false or searchResult.hotels is null");
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

        return searchResult;
    }

    @Override
    protected String getServiceEndPoint() {
        searchUrl = prepareEndPointUrl(lat, lon, distanceUnit, checkInDate, checkOutDate);
        return searchUrl;
    }

    /**
     * prepare end point url for Hotel search
     * 
     * @param end_point
     * @param lat
     * @param lon
     * @param distanceUnit
     * @param checkInDate
     * @param checkOutDate
     * @return
     */
    public static String prepareEndPointUrl(Double lat, Double lon, String distanceUnit, Calendar checkInDate,
            Calendar checkOutDate) {

        StringBuilder endPointUrlBldr = new StringBuilder(SERVICE_END_POINT);
        endPointUrlBldr.append("?latitude=");
        endPointUrlBldr.append(lat);
        endPointUrlBldr.append("&longitude=");
        endPointUrlBldr.append(lon);
        endPointUrlBldr.append("&distanceUnit=");
        endPointUrlBldr.append(distanceUnit);
        endPointUrlBldr.append("&checkin=");
        endPointUrlBldr.append(Format.safeFormatCalendar(Parse.LONG_YEAR_MONTH_DAY, checkInDate));
        endPointUrlBldr.append("&checkout=");
        endPointUrlBldr.append(Format.safeFormatCalendar(Parse.LONG_YEAR_MONTH_DAY, checkOutDate));
        // endPointUrlBldr.append("&radius=25");
        return endPointUrlBldr.toString();
    }

}
