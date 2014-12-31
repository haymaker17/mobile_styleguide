package com.concur.mobile.core.travel.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CustomAsyncRequestTask;
import com.concur.mobile.core.travel.data.Segment.SegmentType;
import com.concur.mobile.core.travel.service.parser.CancelSegmentResponseParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * An extension of <code>CustomAsyncRequestTask</code> for containing the results of a segment cancellation.
 * 
 * @author TejoA
 * 
 */
public class CancelSegment extends CustomAsyncRequestTask {

    private static final String CLS_TAG = CancelSegment.class.getSimpleName();

    public final static String IS_SUCCESS = "cancel_segment_is_success";
    public final static String ERROR_MESSAGE = "cancel_segment_error_message";
    public final static String IS_ENTIRE_TRIP_CANCELLED = "is_entire_trip_cancelled";
    public final static String CANCELLATION_NUMBER = "cancellation_number";

    private SegmentType segmentType;
    private String bookingSource;
    private String reason;
    private String recordLocator;
    private String segmentKey;
    private String tripId;

    private MWSResponseParser mwsRespParser;
    // common parser for Hotel/Air/Car/Rail
    private CancelSegmentResponseParser cancelRespParser;

    public ProgressDialog progressDialog;

    // An enumeration describing segment type cancellation.
    // public enum CancelSegmentType {
    // AIR, HOTEL, CAR, RAIL
    // }

    /**
     * 
     * @param activity
     * @param context
     * @param id
     * @param receiver
     * @param segmentType
     * @param bookingSource
     * @param reason
     * @param recordLocator
     * @param segmentKey
     * @param tripId
     */
    public CancelSegment(Activity activity, Context context, int id, BaseAsyncResultReceiver receiver,
            SegmentType segmentType, String bookingSource, String reason, String recordLocator, String segmentKey,
            String tripId) {

        super(activity, context, id, receiver);

        this.segmentType = segmentType;
        this.bookingSource = bookingSource;
        this.reason = reason;
        this.recordLocator = recordLocator;
        this.segmentKey = segmentKey;
        this.tripId = tripId;
    }

    @Override
    protected String getServiceEndpoint() {
        String endPoint = null;
        switch (segmentType) {
        case HOTEL:
            endPoint = "/Mobile/Hotel/CancelV2";
            break;
        case AIR:
            // TODO
            break;
        case CAR:
            // TODO
            break;
        case RAIL:
            // TODO
            break;
        default:
            // nothing to do here
            break;
        }
        return endPoint;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // need to handle different progress dialog per CancelSegmentType
        showCancelProgressDialog(); // hotel
    }

    @Override
    protected void onActivityDetached() {
        cancelProgressDialog();
    }

    @Override
    protected void onActivityAttached() {
        cancelProgressDialog();
        showCancelProgressDialog();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        mwsRespParser = new MWSResponseParser();
        cancelRespParser = new CancelSegmentResponseParser(parser, CancelSegmentResponseParser.TAG_CANCEL_RESPONSE);

        // register the parsers of interest
        parser.registerParser(mwsRespParser, "MWSResponse");
        parser.registerParser(cancelRespParser, "TravelCancelResponse");

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
    protected String getPostBody() {
        // form the the request XML
        StringBuilder sb = new StringBuilder();
        // need to be handled according to request, may change for Car/Rail/Air
        sb.append("<CancelCriteria>");
        FormatUtil.addXMLElement(sb, "BookingSource", ((bookingSource != null) ? FormatUtil.escapeForXML(bookingSource)
                : ""));
        FormatUtil.addXMLElementEscaped(sb, "Reason", ((reason != null) ? FormatUtil.escapeForXML(reason) : ""));
        FormatUtil.addXMLElement(sb, "RecordLocator", recordLocator);
        FormatUtil.addXMLElement(sb, "SegmentKey", ((segmentKey != null) ? FormatUtil.escapeForXML(segmentKey) : ""));
        FormatUtil.addXMLElement(sb, "TripId", tripId);
        sb.append("</CancelCriteria>");

        return sb.toString();
    }

    @Override
    protected int onPostParse() {
        int resultcode = RESULT_OK;
        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();
        boolean isSuccess = reqStatus.isSuccess();
        resultData.putBoolean(IS_SUCCESS, isSuccess);
        if (reqStatus.isSuccess() && cancelRespParser != null) {

            if (cancelRespParser.hotelCancelAry != null && cancelRespParser.hotelCancelAry.size() > 0) {
                resultData.putString(CANCELLATION_NUMBER, cancelRespParser.hotelCancelAry.get(0).cancellationNumber);
                resultData.putBoolean(IS_ENTIRE_TRIP_CANCELLED, cancelRespParser.isEntireTripCancelled);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: no value for response id was parsed.");
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".onPostParse: Cancellation status is false");
            String errorMessage = null;
            if (reqStatus.getErrors().isEmpty()) {
                errorMessage = reqStatus.getResponseMessage();
            } else {
                errorMessage = reqStatus.getErrors().get(0).getUserMessage();
            }
            resultData.putString(ERROR_MESSAGE, errorMessage);
        }

        return resultcode;

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (activity != null) {
            cancelProgressDialog();
        } else {
            Log.d(CLS_TAG, "CancelSegment Async Task finished while no Activity was attached.");
        }
    }

    private void cancelProgressDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Progress Dialog for hotel cancellation
     */
    private void showCancelProgressDialog() {

        progressDialog = ProgressDialog.show(activity, "",
                activity.getText(R.string.dlg_hotel_cancel_progress_message), true, true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

    }
}