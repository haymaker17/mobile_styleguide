/**
 * 
 */
package com.concur.mobile.platform.travel.recommendation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.util.Const;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of searching for hotel recommendations.
 */
public class HotelRecommendationSearchRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "HotelRecommendationSearchRequestTask";

    private static final String SERVICE_ENDPOINT = "/mobile/Hotel/GetRecommendations";

    /**
     * Contains the check-in date.
     */
    protected Calendar checkInDate;

    /**
     * Contains the latitude.
     */
    protected Double latitude;

    /**
     * Contains the longitude.
     */
    protected Double longitude;

    /**
     * Contains the radius.
     */
    protected Integer radius;

    /**
     * Contains the radius units.
     */
    protected String radiusUnits;

    /**
     * Constructs an instance of <code>HotelRecommendationSearchRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains the receiver.
     * @param checkInDate
     *            contains the check-in date.
     * @param latitude
     *            contains the latitude.
     * @param longitude
     *            contains the longitude.
     * @param radius
     *            contains the radius.
     * @param radiusUnits
     *            contains the radius units.
     */
    public HotelRecommendationSearchRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
            Calendar checkInDate, Double latitude, Double longitude, Integer radius, String radiusUnits) {
        super(context, requestId, receiver);
        this.checkInDate = checkInDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.radiusUnits = radiusUnits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#getServiceEndPoint()
     */
    @Override
    protected String getServiceEndPoint() {
        return SERVICE_ENDPOINT;
    }

    @Override
    protected String getPostBody() {
        String body = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<HotelRecommendations>");
        strBldr.append("<CheckInDate>");
        if (checkInDate != null) {
            strBldr.append(Format.safeFormatCalendar(Parse.XML_DF_LOCAL, checkInDate));
        }
        strBldr.append("</CheckInDate>");
        strBldr.append("<Latitude>");
        if (latitude != null) {
            strBldr.append(Double.toString(latitude));
        }
        strBldr.append("</Latitude>");
        strBldr.append("<Longitude>");
        if (longitude != null) {
            strBldr.append(Double.toString(longitude));
        }
        strBldr.append("</Longitude>");
        strBldr.append("<Radius>");
        if (radius != null) {
            strBldr.append(Integer.toString(radius));
        }
        strBldr.append("</Radius>");
        strBldr.append("<Unit>");
        if (radiusUnits != null) {
            strBldr.append(radiusUnits);
        }
        strBldr.append("</Unit>");
        strBldr.append("</HotelRecommendations>");

        body = strBldr.toString();

        return body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.io.InputStream)
     */
    @Override
    protected int parseStream(InputStream is) {

        int result = BaseAsyncRequestTask.RESULT_OK;

        StringBuilder strBldr = new StringBuilder();
        BufferedReader bufRdr = null;
        String line = null;
        try {
            bufRdr = new BufferedReader(new InputStreamReader(is), 8 * 1024);
            while ((line = bufRdr.readLine()) != null) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".parseStream: " + line);
                strBldr.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufRdr != null) {
                try {
                    bufRdr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        String response = strBldr.toString();

        return result;
        // try {
        // CommonParser parser = initCommonParser(is);
        // if (parser != null) {
        // // Construct the location search list parser and register it.
        // String listTag = "ArrayOfLocationChoice";
        // ListParser<LocationChoice> locationSearchListParser = new ListParser<LocationChoice>(parser, listTag,
        // "LocationChoice", LocationChoice.class);
        // parser.registerParser(locationSearchListParser, listTag);
        //
        // // Parse.
        // parser.parse();
        //
        // } else {
        // result = BaseAsyncRequestTask.RESULT_ERROR;
        // Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to construct common parser!");
        // }
        // } catch (XmlPullParserException e) {
        // result = BaseAsyncRequestTask.RESULT_ERROR;
        // Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        // } catch (IOException e) {
        // result = BaseAsyncRequestTask.RESULT_ERROR;
        // Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        // } finally {
        // if (is != null) {
        // try {
        // is.close();
        // is = null;
        // } catch (IOException ioExc) {
        // Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
        // }
        // }
        // }
        // return result;
    }

    @Override
    protected int onPostParse() {

        int result = super.onPostParse();

        return result;

    }

}
