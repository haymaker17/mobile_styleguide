/**
 * 
 */
package com.concur.mobile.platform.travel.trip;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.travel.provider.TravelUtil;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purposes of retrieving a single trip. <br>
 * <br>
 * <br>
 * NOTE: This request currently uses the old end-point 'SingleItinerary' for non-approver itinerary requests since the newer
 * end-point 'SingleItineraryV2' requires a protected value for an 'itinLocator'. Clients passing <code>true</code> for the
 * <code>forApprover</code> parameter need to ensure they're using a protected value for <code>itinLocator</code> (i.e., a trip to
 * be approved); otherwise, the service will return a '500'.
 */
public class TripRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "TripRequestTask";

    private static final String SERVICE_END_POINT = "/mobile/SingleItinerary";

    private static final String SERVICE_END_POINT_V2 = "/mobile/SingleItineraryV2";

    /**
     * Contains the company id.
     */
    protected String companyId;

    /**
     * Contains whether this request is for an approver.
     */
    protected boolean forApprover;

    /**
     * Contains the itin locator.
     */
    protected String itinLocator;

    /**
     * Contains the user id.
     */
    protected String userId;

    /**
     * Contains the parsed itinerary.
     */
    protected Itinerary itinerary;

    /**
     * Contains the response status.
     */
    protected MWSResponseStatus responseStatus;

    public TripRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String companyId,
            boolean forApprover, String itinLocator, String userId) {
        super(context, requestId, receiver);
        this.companyId = companyId;
        this.forApprover = forApprover;
        this.itinLocator = itinLocator;
        this.userId = userId;
    }

    @Override
    protected String getPostBody() {

        String postBody = null;
        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<TripSpecifier>");
        if (!TextUtils.isEmpty(companyId)) {
            strBldr.append("<CompanyId>");
            strBldr.append(companyId);
            strBldr.append("</CompanyId>");
        }
        if (forApprover) {
            strBldr.append("<ForApprover>");
            strBldr.append(Boolean.toString(forApprover));
            strBldr.append("</ForApprover>");
            strBldr.append("<ItinLocator>");
            strBldr.append(itinLocator);
            strBldr.append("</ItinLocator>");
        } else {
            strBldr.append("<TripId>");
            strBldr.append(itinLocator);
            strBldr.append("</TripId>");
        }
        if (forApprover) {
            strBldr.append("<UserId>");
            strBldr.append(userId);
            strBldr.append("</UserId>");
        }
        strBldr.append("</TripSpecifier>");
        postBody = strBldr.toString();
        return postBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#getServiceEndPoint()
     */
    @Override
    protected String getServiceEndPoint() {
        if (forApprover) {
            return SERVICE_END_POINT_V2;
        } else {
            return SERVICE_END_POINT;
        }
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

                MWSResponseParser mwsResponseParser = null;
                if (forApprover) {
                    // Construct and register the MWS response parser.
                    mwsResponseParser = new MWSResponseParser();
                    parser.registerParser(mwsResponseParser, MWSResponseParser.TAG_MWS_RESPONSE);
                }

                // Construct and register an itinerary item parser.
                String itemTag = "Itinerary";
                ItemParser<Itinerary> itinItemParser = new ItemParser<Itinerary>(parser, itemTag, Itinerary.class);
                parser.registerParser(itinItemParser, itemTag);

                // Parse.
                parser.parse();

                if (mwsResponseParser != null) {
                    // Set the response status.
                    responseStatus = mwsResponseParser.getRequestTaskStatus();
                }
                // Set the itinerary.
                itinerary = itinItemParser.getItem();
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
        if (result == RESULT_OK) {
            if (responseStatus != null) {

                // Set any MWS response data.
                setMWSResponseStatusIntoResultBundle(responseStatus);

                if (forApprover) {
                    if (responseStatus.isSuccess()) {
                        if (itinerary != null) {
                            SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
                            TravelUtil.updateTripInfo(getContext(), itinerary, sessInfo.getUserId());
                            result = RESULT_OK;
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: MWS returned success, but itinerary is null.");
                            result = RESULT_ERROR;
                        }
                    } else {
                        result = RESULT_ERROR;
                    }
                } else {
                    if (itinerary != null) {
                        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
                        TravelUtil.updateTripInfo(getContext(), itinerary, sessInfo.getUserId());
                        result = RESULT_OK;
                    } else {
                        result = RESULT_ERROR;
                    }
                }
            } else {
                if (itinerary != null) {
                    SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
                    TravelUtil.updateTripInfo(getContext(), itinerary, sessInfo.getUserId());
                    result = RESULT_OK;
                } else {
                    result = RESULT_ERROR;
                }
            }
        }
        return result;
    }

}
