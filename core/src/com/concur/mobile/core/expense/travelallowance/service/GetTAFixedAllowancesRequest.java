package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.service.parser.GetTAFixedAllowancesResponseParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by Michael Becherer on 26-Jun-15.
 */
public class GetTAFixedAllowancesRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetTAFixedAllowancesRequest.class
            .getSimpleName();

    private String rptKey;

    private GetTAFixedAllowancesResponseParser parser;

    private List<FixedTravelAllowance> fixedTravelAllowances;

    private long startMillis;

    private long parserStartMillis;

    private Context context;

    public GetTAFixedAllowancesRequest(Context context,
                                   BaseAsyncResultReceiver receiver, String rptKey) {
        super(context, 0, receiver);
        this.context = context;
        this.rptKey = rptKey;
    }


    @Override
    protected void onPreExecute() {
        startMillis = System.currentTimeMillis();
        super.onPreExecute();
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetTaFixedAllowances/" + rptKey;
    }

    @Override
    protected int parse(CommonParser parser) {
        parserStartMillis = System.currentTimeMillis();
        int result = RESULT_OK;

        // register the parser of interest
        this.parser = new GetTAFixedAllowancesResponseParser(context);
        parser.registerParser(this.parser, "Body");

        try {
            Log.d(LOG_TAG, "Start parsing fixed travel allowances...");
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
        long currentMillisParser = System.currentTimeMillis();
        Log.i(LOG_TAG, "Parsing time: " + (currentMillisParser - parserStartMillis) + "ms");

        resultData.putBoolean(IS_SUCCESS, true);
        this.fixedTravelAllowances = parser.getFixedTravelAllowances();

        long currentMillis = System.currentTimeMillis();
        Log.i(LOG_TAG, "Request total: " + (currentMillis - startMillis) + "ms");

        return RESULT_OK;
    }

    public List<FixedTravelAllowance> getFixedTravelAllowances() {
        return fixedTravelAllowances;
    }
}
