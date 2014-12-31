/**
 * 
 */
package com.concur.mobile.platform.travel.search.hotel;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;
import com.concur.mobile.platform.util.XmlUtil;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of performing a hotel search. <br>
 * <br>
 * NOTE: This hotel search supports the hotel streaming.
 */
public class HotelSearchAsyncRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "HotelSearchAsyncRequestTask";

    private static final String SERVICE_END_POINT = "/Mobile/Hotel/Search3";

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
     * Contains whether benchmarks should be included in search results.
     */
    protected boolean includeBenchmarks;

    /**
     * Contains the hotel chain name.
     */
    protected String hotelChain;

    /**
     * Contains wehther "deposit required" hotels should be included in search results.
     */
    protected boolean includeDepositRequired;

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
     * Contains the radius units, i.e., 'M', 'K', etc.
     */
    protected String radiusUnits;

    /**
     * Contains the index into the result set to start collecting <code>count</code> results.
     */
    protected Integer start;

    /**
     * Contains the count of hotel choices to return.
     */
    protected Integer count;

    /**
     * Contains the MWS response parser.
     */
    private MWSResponseParser mwsResponseParser;

    /**
     * Contains the MWS response status parser.
     */
    protected MWSResponseStatus reqStatus;

    /**
     * Contains the hotel choices parser.
     */
    protected HotelChoicesParser choicesParser;

    /**
     * Constructs an instance of <code>HotelSearchAsyncRequestTask</code> to perform a streaming hotel search.
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
     */
    public HotelSearchAsyncRequestTask(Context context, int id, BaseAsyncResultReceiver receiver, Calendar checkInDate,
            Calendar checkOutDate, Boolean includeBenchmarks, String hotelChain, Boolean includeDepositRequired,
            Double lat, Double lon, Double perdiemRate, Integer radius, String radiusUnits, Integer start, Integer count) {

        super(context, id, receiver);

        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.includeBenchmarks = includeBenchmarks;
        this.hotelChain = hotelChain;
        this.includeDepositRequired = includeDepositRequired;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.radiusUnits = radiusUnits;
        this.start = start;
        this.count = count;
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#getPostBody()
     */
    @Override
    protected String getPostBody() {
        String postBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<HotelCriteria>");
        XmlUtil.addXmlElement(strBldr, "Count", count);
        XmlUtil.addXmlElement(strBldr, "DateEnd",
                Format.safeFormatCalendar(Parse.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, checkOutDate));
        XmlUtil.addXmlElement(strBldr, "DateStart",
                Format.safeFormatCalendar(Parse.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, checkInDate));
        if (hotelChain == null || hotelChain.length() == 0) {
            hotelChain = "";
        }
        XmlUtil.addXmlElement(strBldr, "Hotel", hotelChain);
        XmlUtil.addXmlElementTF(strBldr, "IncludeDepositRequired", includeDepositRequired);
        XmlUtil.addXmlElement(strBldr, "Lat", lat);
        XmlUtil.addXmlElement(strBldr, "Lon", lon);
        XmlUtil.addXmlElement(strBldr, "Radius", radius);
        XmlUtil.addXmlElement(strBldr, "Scale", radiusUnits);
        XmlUtil.addXmlElement(strBldr, "Smoking", "0");
        XmlUtil.addXmlElement(strBldr, "Start", start);
        strBldr.append("</HotelCriteria>");
        postBody = strBldr.toString();
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
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {

                choicesParser = new HotelChoicesParser();
                mwsResponseParser = new MWSResponseParser();

                // register the parsers of interest
                parser.registerParser(choicesParser, MWSResponseParser.TAG_RESPONSE);
                parser.registerParser(mwsResponseParser, MWSResponseParser.TAG_MWS_RESPONSE);

                // Parse.
                parser.parse();

            } else {
                result = BaseAsyncRequestTask.RESULT_ERROR;
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to construct common parser!");
            }
        } catch (XmlPullParserException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } catch (IOException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
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

        reqStatus = mwsResponseParser.getRequestTaskStatus();

        // Set any MWS response data.
        setMWSResponseStatusIntoResultBundle(reqStatus);

        // check if response is success
        if (reqStatus.isSuccess()) {

            resultcode = RESULT_OK;

            HotelSearchResult reply = new HotelSearchResult();
            reply.startIndex = choicesParser.startIndex;
            reply.length = choicesParser.length;
            reply.totalCount = choicesParser.totalCount;
            reply.hasRecommendation = choicesParser.hasRecommendation;
            reply.pollingId = choicesParser.pollingId;
            reply.hotelChoices = choicesParser.choices;
            reply.isFinal = choicesParser.isFinal;

            resultData.putSerializable(HotelSearchAsyncRequestTask.HOTEL_SEARCH_RESULT_EXTRA_KEY, (Serializable) reply);
        } else {
            // log the error message
            String errorMessage = "could not perform hotel search";
            if (reqStatus.getErrors().isEmpty()) {
                Log.e(Const.LOG_TAG, errorMessage);
            } else {
                errorMessage = reqStatus.getErrors().get(0).getSystemMessage();
                Log.e(Const.LOG_TAG, errorMessage);
                resultData.putString("mwsErrorMessage", errorMessage);
            }
        }

        return resultcode;
    }

}
