package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.service.parser.GetTAConfigurationParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by D023077 on 02.07.2015.
 */
public class GetTAConfigurationRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetTAItinerariesRequest.class.getSimpleName();

//    private String rptKey;

    private GetTAConfigurationParser parser;

    private List<TravelAllowanceConfiguration> configurationList;

//    private boolean isManager;


    public GetTAConfigurationRequest(Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, 0, receiver);
    }

//    @Override
//    protected void onPreExecute() {
//        startMillis = System.currentTimeMillis();
//        super.onPreExecute();
//    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetTAConfigForEmployee";
    }

    @Override
    protected int parse(CommonParser parser) {
//        parserStartMillis = System.currentTimeMillis();
        int result = RESULT_OK;

        // register the parser of interest
        this.parser = new GetTAConfigurationParser();
        parser.registerParser(this.parser, "Configuration");

        try {
            Log.d(LOG_TAG, "Start parsing configuration...");
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
//        long currentMillisParser = System.currentTimeMillis();
//        Log.i(LOG_TAG, "Parsing time: " + (currentMillisParser - parserStartMillis) + "ms");
//
        resultData.putBoolean(IS_SUCCESS, true);
        this.configurationList = parser.getConfigurationList();

//        long currentMillis = System.currentTimeMillis();
//        Log.i(LOG_TAG, "Request total: " + (currentMillis - startMillis) + "ms");
//
        return RESULT_OK;
    }


    public List<TravelAllowanceConfiguration> getTravelAllowanceConfigurationList(){
        return configurationList;
    }


}
