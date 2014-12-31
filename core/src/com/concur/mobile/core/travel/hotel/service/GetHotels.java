package com.concur.mobile.core.travel.hotel.service;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Calendar;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.travel.hotel.service.parser.HotelChoicesParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Format;

/**
 * 
 * @author RatanK
 * 
 */
public class GetHotels extends CoreAsyncRequestTask {

    private static final String CLS_TAG = GetHotels.class.getSimpleName();

    private static final String SERVICE_END_POINT = "/Mobile/Hotel/Search3";

    public Calendar dateEnd;
    public Calendar dateStart;
    public String hotelChain;
    public String lat;
    public String lon;
    public String radius;
    public String scale;
    public Integer startIndex;
    public Integer count;

    protected HotelChoicesParser choicesParser;
    protected MWSResponseParser mwsRespParser;
    protected MWSResponseStatus reqStatus;

    public GetHotels(Context context, int id, BaseAsyncResultReceiver receiver, Calendar dateEnd, Calendar dateStart,
            String hotelChain, String lat, String lon, String radius, String scale, Integer startIndex, Integer count) {

        super(context, id, receiver);

        this.dateEnd = dateEnd;
        this.dateStart = dateStart;
        this.hotelChain = hotelChain;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.scale = scale;
        this.startIndex = startIndex;
        this.count = count;
    }

    @Override
    protected String getServiceEndpoint() {
        return SERVICE_END_POINT;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        // MOB-17575 - Increase timeout value to 60 secs from default 30 secs
        connection.setReadTimeout(60000);
    }

    @Override
    protected String getPostBody() {
        String postBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<HotelCriteria>");
        ViewUtil.addXmlElement(strBldr, "Count", String.valueOf(count));
        ViewUtil.addXmlElement(strBldr, "DateEnd",
                Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, dateEnd));
        ViewUtil.addXmlElement(strBldr, "DateStart",
                Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_DISPLAY, dateStart));
        if (hotelChain == null || hotelChain.length() == 0) {
            hotelChain = "";
        }
        ViewUtil.addXmlElement(strBldr, "GetBenchmarks", "true");
        ViewUtil.addXmlElement(strBldr, "Hotel", FormatUtil.escapeForXML(hotelChain));
        ViewUtil.addXmlElement(strBldr, "IncludeDepositRequired", "true");
        ViewUtil.addXmlElement(strBldr, "Lat", lat);
        ViewUtil.addXmlElement(strBldr, "Lon", lon);
        ViewUtil.addXmlElement(strBldr, "Radius", radius);
        ViewUtil.addXmlElement(strBldr, "Scale", scale);
        ViewUtil.addXmlElement(strBldr, "Smoking", "0");
        ViewUtil.addXmlElement(strBldr, "Start", String.valueOf(startIndex));
        strBldr.append("</HotelCriteria>");
        postBody = strBldr.toString();
        return postBody;
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        choicesParser = new HotelChoicesParser();
        mwsRespParser = new MWSResponseParser();

        // register the parsers of interest
        parser.registerParser(choicesParser, "Response");
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

        reqStatus = mwsRespParser.getRequestTaskStatus();

        // check if response is success
        if (reqStatus.isSuccess()) {

            resultcode = RESULT_OK;

            HotelSearchReply reply = new HotelSearchReply();
            reply.startIndex = choicesParser.startIndex;
            reply.length = choicesParser.length;
            reply.totalCount = choicesParser.totalCount;
            reply.hasRecommendation = choicesParser.hasRecommendation;
            reply.pollingId = choicesParser.pollingId;
            reply.hotelChoices = choicesParser.choices;
            reply.isFinal = choicesParser.isFinal;
            reply.hotelBenchmarks = choicesParser.hotelBenchmarks;
            reply.travelPointsBank = choicesParser.travelPointsBank;

            ConcurCore core = (ConcurCore) ConcurCore.getContext();
            core.setRequestTaskStatus(reqStatus);

            // search end point invoked
            if (core.getHotelSearchResults() == null) {
                core.setHotelSearchResults(reply);
            } else {
                // invoked by the sub class parsers, so don't update the app object
                resultData.putSerializable("HotelSearchReply", (Serializable) reply);
            }
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
