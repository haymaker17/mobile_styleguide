package com.concur.mobile.core.travel.air.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

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
import com.concur.mobile.core.travel.air.service.parser.AirBenchmarkParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;
import com.concur.mobile.platform.util.Format;

/**
 * Async request for retrieving Air Price to Beat
 * 
 * @author RatanK
 * 
 */
public class GetAirBenchmarks extends CustomAsyncRequestTask {

    private static final String CLS_TAG = GetAirBenchmarks.class.getSimpleName();

    public static final String AIR_BENCHMARKS = "air_benchmarks";

    private MWSResponseParser mwsRespParser;
    private AirBenchmarkParser abmParser;

    private String departIATA;
    private String arriveIATA;
    private Calendar departDate;
    private boolean roundTrip;

    private ProgressDialog progressDialog;

    public GetAirBenchmarks(Activity activity, Context context, int id, BaseAsyncResultReceiver receiver,
            String departIATA, String arriveIATA, Calendar departDate, boolean roundTrip) {

        super(activity, context, id, receiver);

        this.departIATA = departIATA;
        this.arriveIATA = arriveIATA;
        this.departDate = departDate;
        this.roundTrip = roundTrip;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/mobile/Air/GetBenchmarks";
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
        abmParser = new AirBenchmarkParser();

        parser.registerParser(abmParser, "AirBenchmarks");
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
        StringBuilder body = new StringBuilder();

        body.append("<PriceToBeatAirCriteria>");
        FormatUtil.addXMLElement(body, "DepartDate",
                Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY, departDate));
        FormatUtil.addXMLElement(body, "EndIata", arriveIATA);
        FormatUtil.addXMLElement(body, "RoundTrip", Boolean.toString(roundTrip));
        FormatUtil.addXMLElement(body, "StartIata", departIATA);
        body.append("</PriceToBeatAirCriteria>");

        return body.toString();
    }

    @Override
    protected int onPostParse() {
        int resultcode = RESULT_OK;

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();

        if (reqStatus.isSuccess() && abmParser.airBenchMarks != null) {
            resultData.putSerializable(AIR_BENCHMARKS, (Serializable) abmParser.airBenchMarks);
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".onPostParse: AirBenchmarks is null");
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
            Log.d(CLS_TAG, "GetAirBenchmarks Async Task finished while no Activity was attached.");
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
