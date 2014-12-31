package com.concur.mobile.platform.travel.triplist;

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
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.travel.provider.TravelUtil;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purposes of retrieving a list of <code>TripListItinerary</code>
 * objects.
 */
public class TripListRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "TripListRequestTask";

    private static final String SERVICE_ENDPOINT = "/mobile/Itinerary/GetUserTripListV2";

    /**
     * Contains whether or not this request should retrieve widthdrawn trips.
     */
    protected boolean includeWithdrawn;

    /**
     * Contains the list of parsed trip summaries.
     */
    protected List<TripSummary> tripSummaries;

    /**
     * Constructs an instance of <code>TripListRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains a request id.
     * @param receiver
     *            contains a result receiver.
     */
    public TripListRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
            boolean includeWithdrawn) {
        super(context, requestId, receiver);
        this.includeWithdrawn = includeWithdrawn;
    }

    @Override
    protected String getServiceEndPoint() {
        if (includeWithdrawn) {
            StringBuilder strBldr = new StringBuilder(SERVICE_ENDPOINT);
            strBldr.append("?includeWithdrawn=true");
            return strBldr.toString();
        } else {
            return SERVICE_ENDPOINT;
        }
    }

    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Construct the trip itinerary list parser and register it.
                String listTag = "TripListItinerarySummaries";
                ListParser<TripSummary> tripListItineraryListParser = new ListParser<TripSummary>(parser, listTag,
                        "TripListItinerary", TripSummary.class);
                parser.registerParser(tripListItineraryListParser, listTag);

                // Parse.
                parser.parse();

                // Set the
                tripSummaries = tripListItineraryListParser.getList();
            } else {
                result = BaseAsyncRequestTask.RESULT_ERROR;
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseContentAsXML: unable to construct common parser!");
            }
        } catch (XmlPullParserException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseContentAsXML: ", e);
        } catch (IOException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseContentAsXML: ", e);
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

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        TravelUtil.updateTripSummaryInfo(getContext(), tripSummaries, sessInfo.getUserId());

        return result;

    }

}
