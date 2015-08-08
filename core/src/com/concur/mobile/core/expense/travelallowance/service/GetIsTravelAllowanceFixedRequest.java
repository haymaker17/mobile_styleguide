package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceSystemConfiguration;
import com.concur.mobile.core.expense.travelallowance.service.parser.GetIsTravelAllowanceFixedResponseParser;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by Michael Becherer on 02.07.2015.
 */
public class GetIsTravelAllowanceFixedRequest extends CoreAsyncRequestTask {

    private static final String CLASS_TAG = GetIsTravelAllowanceFixedRequest.class.getSimpleName();

    private GetIsTravelAllowanceFixedResponseParser parser;

    private TravelAllowanceSystemConfiguration configuration;

    private long startMillis;

    private long parserStartMillis;

    public GetIsTravelAllowanceFixedRequest(Context context, BaseAsyncResultReceiver receiver) {
        super(context, 0, receiver);
    }

    @Override
    protected void onPreExecute() {
        this.startMillis = System.currentTimeMillis();
        super.onPreExecute();
    }

    @Override
    protected String getServiceEndpoint() {
        return "Expense/GetIsTravelAllowanceFixed";
    }

    @Override
    protected int parse(CommonParser parser) {
        parserStartMillis = System.currentTimeMillis();
        int result = RESULT_OK;

        // register the parser of interest
        this.parser = new GetIsTravelAllowanceFixedResponseParser();
        parser.registerParser(this.parser, ""); //Provide the name of teh StartTag in the response

        try {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "parse", "Start parsing system configuration..."));
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
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPostParse",
                "Parsing time = " + (currentMillisParser - parserStartMillis) + "ms"));

        resultData.putBoolean(IS_SUCCESS, true);
        this.configuration = parser.getConfiguration();
        resultData.putSerializable(BundleId.SYSTEM_CONFIGURATION, this.configuration);

        long currentMillis = System.currentTimeMillis();
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPostParse",
                "Request total = " + (currentMillis - startMillis) + "ms"));
        return RESULT_OK;
    }

}
