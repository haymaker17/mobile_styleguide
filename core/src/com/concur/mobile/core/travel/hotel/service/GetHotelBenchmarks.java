package com.concur.mobile.core.travel.hotel.service;

import java.io.IOException;
import java.io.Serializable;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CustomAsyncRequestTask;
import com.concur.mobile.core.travel.hotel.service.parser.HotelBenchmarkParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * Async request for retrieving Hotel Price to Beat
 * 
 * @author RatanK
 * 
 */
public class GetHotelBenchmarks extends CustomAsyncRequestTask {

    private static final String CLS_TAG = GetHotelBenchmarks.class.getSimpleName();

    public static final String HOTEL_BENCHMARKS = "hotel_benchmarks";

    private MWSResponseParser mwsRespParser;
    private HotelBenchmarkParser benchmarksParser;

    private String lat;
    private String lon;
    private String radius;
    private String scale;
    private String monthNumber;

    private ProgressDialog progressDialog;

    public GetHotelBenchmarks(Activity activity, Context context, int id, BaseAsyncResultReceiver receiver, String lat,
            String lon, String radius, String scale, String monthNumber) {

        super(activity, context, id, receiver);

        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.scale = scale;
        this.monthNumber = monthNumber;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/mobile/Hotel/GetBenchmarks";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showSearchProgressDialog();
    }

    @Override
    protected void onActivityDetached() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onActivityAttached() {
        showSearchProgressDialog();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        mwsRespParser = new MWSResponseParser();
        benchmarksParser = new HotelBenchmarkParser();

        parser.registerParser(benchmarksParser, "Benchmarks");
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
    protected String getPostBody() {
        String postBody = null;
        StringBuilder strBldr = new StringBuilder();

        strBldr.append("<PriceToBeatHotelCriteria>");
        FormatUtil.addXMLElement(strBldr, "Lat", lat);
        FormatUtil.addXMLElement(strBldr, "Lon", lon);
        FormatUtil.addXMLElement(strBldr, "MonthNumber", monthNumber);
        FormatUtil.addXMLElement(strBldr, "Radius", radius);
        FormatUtil.addXMLElement(strBldr, "Scale", scale);
        strBldr.append("</PriceToBeatHotelCriteria>");

        postBody = strBldr.toString();

        return postBody;
    }

    @Override
    protected int onPostParse() {
        int resultcode = RESULT_OK;

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();

        if (reqStatus.isSuccess() && benchmarksParser.hotelBenchMarks != null) {
            resultData.putSerializable(HOTEL_BENCHMARKS, (Serializable) benchmarksParser.hotelBenchMarks);
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".onPostParse: HotelBenchmarks is null");
            String errorMessage = null;
            if (reqStatus.getErrors().isEmpty()) {
                errorMessage = reqStatus.getResponseMessage();
            } else {
                errorMessage = reqStatus.getErrors().get(0).getUserMessage();
            }
            resultData.putString(Const.MWS_ERROR_MESSAGE, errorMessage);
        }

        return resultcode;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (activity != null) {
            progressDialog.dismiss();
        } else {
            Log.d(CLS_TAG, "GetHotelBenchmarks Async Task finished while no Activity was attached.");
        }
    }

    private void showSearchProgressDialog() {
        progressDialog = ProgressDialog.show(activity, "",
                activity.getText(R.string.search_for_price_to_beat_progress), true, true);

        progressDialog.setOnCancelListener(new OnCancelListener() {

            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });

        progressDialog.show();
    }
}
