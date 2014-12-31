/**
 * 
 */
package com.concur.mobile.platform.travel.location;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.travel.provider.TravelUtil;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of performing a location search.
 */
public class LocationSearchRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "LocationSearchRequestTask";

    private static final String SERVICE_ENDPOINT = "/mobile/Location/Search";

    /**
     * Contains some fragment of the address.
     */
    protected String address;

    /**
     * Contains whether the search should be limited to airports only.
     */
    protected boolean airportsOnly;

    /**
     * Contains the list of returned locations.
     */
    protected List<LocationChoice> locations;

    /**
     * Constructs an instance of <code>LocationSearchRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains a request id.
     * @param receiver
     *            contains the result receiver.
     * @param address
     *            contains some fragment of the address.
     * @param airportsOnly
     *            contains whether or not the search should be limited to airports.
     */
    public LocationSearchRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String address,
            boolean airportsOnly) {
        super(context, requestId, receiver);
        this.address = address;
        this.airportsOnly = airportsOnly;
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
        strBldr.append("<LocationCriteria>");
        strBldr.append("<Address>");
        strBldr.append(((address != null) ? address.trim() : ""));
        strBldr.append("</Address>");
        strBldr.append("<AirportsOnly>");
        strBldr.append(Boolean.toString(airportsOnly));
        strBldr.append("</AirportsOnly>");
        strBldr.append("</LocationCriteria>");

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
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Construct the location search list parser and register it.
                String listTag = "ArrayOfLocationChoice";
                ListParser<LocationChoice> locationSearchListParser = new ListParser<LocationChoice>(parser, listTag,
                        "LocationChoice", LocationChoice.class);
                parser.registerParser(locationSearchListParser, listTag);

                // Parse.
                parser.parse();

                // Set the
                locations = locationSearchListParser.getList();
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

        int result = super.onPostParse();

        TravelUtil.updateLocationSearch(getContext(), locations);

        return result;

    }

}
