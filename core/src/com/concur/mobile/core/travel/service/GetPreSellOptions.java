package com.concur.mobile.core.travel.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.travel.service.parser.PreSellOptionsParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.service.parser.MWSResponseParser;
import com.concur.mobile.platform.service.parser.MWSResponseStatus;

/**
 * 
 * @author RatanK
 * 
 */
public class GetPreSellOptions extends CoreAsyncRequestTask {

    private String choiceId;
    private PreSellOptionsParser preSellOptionsParser;
    private MWSResponseParser mwsRespParser;
    public static final String PRE_SELL_OPTIONS = "pre_sell_options";

    public GetPreSellOptions(Context context, int id, BaseAsyncResultReceiver receiver, String choiceId) {
        super(context, id, receiver);
        this.choiceId = choiceId;
    }

    @Override
    protected String getServiceEndpoint() {
        StringBuilder sbr = new StringBuilder("/Mobile/PreSell/PreSellOptions?choiceId=");
        try {
            sbr.append(URLEncoder.encode(choiceId, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sbr.toString();
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        // MOB-15476 - Set timeout value
        connection.setReadTimeout(120000);
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        mwsRespParser = new MWSResponseParser();
        preSellOptionsParser = new PreSellOptionsParser();

        // register the parsers of interest
        parser.registerParser(preSellOptionsParser, "Response");
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
        int resultcode = RESULT_OK;

        MWSResponseStatus reqStatus = mwsRespParser.getRequestTaskStatus();
        if (reqStatus.isSuccess()) {
            resultData.putSerializable(PRE_SELL_OPTIONS, preSellOptionsParser.preSellOption);
        } else {
            resultcode = RESULT_ERROR;
            // log the error message
            String errorMessage = "could not retrieve pre-sell options";
            if (reqStatus.getErrors().isEmpty()) {
                Log.e(Const.LOG_TAG, errorMessage);
            } else {
                errorMessage = reqStatus.getErrors().get(0).getSystemMessage();
                Log.e(Const.LOG_TAG, errorMessage);
            }
        }

        return resultcode;
    }

}
