package com.concur.mobile.core.travel.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.travel.data.TripToApprove;
import com.concur.mobile.core.travel.service.parser.TripsForApprovalParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * Async task for getting the list of trips that needed approval
 * 
 * @author RatanK
 * 
 */
public class GetTripsToApprove extends CoreAsyncRequestTask {

    private TripsForApprovalParser tripsParser;
    private MWSResponseParser mwsRespParser;

    public GetTripsToApprove(Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, id, receiver);
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TripApproval/TripsV3";
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        tripsParser = new TripsForApprovalParser();
        mwsRespParser = new MWSResponseParser();

        // register the parsers of interest
        parser.registerParser(tripsParser, "TripToApprove");
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

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();

        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        core.setRequestTaskStatus(reqStatus);

        // check if response is success
        if (reqStatus.isSuccess()) {

            List<TripToApprove> tripsToApprove = tripsParser.getTripsToApprove();
            // sort the trips on ascending 'ApproveByDate' i.e. earliest first
            Collections.sort(tripsToApprove, new Comparator<TripToApprove>() {

                @Override
                public int compare(TripToApprove lhs, TripToApprove rhs) {
                    int val = 0;
                    if (lhs.getApproveByDate() != null) {
                        val = lhs.getApproveByDate().compareTo(rhs.getApproveByDate());
                    }
                    return val;
                }

            });

            core.setTripsToApprove(tripsToApprove);
            core.setTripsToApproveLastRetrieved(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            resultcode = RESULT_OK;
        } else {
            // log the error message
            Log.e(Const.LOG_TAG, reqStatus.getResponseMessage());
        }

        return resultcode;
    }
}