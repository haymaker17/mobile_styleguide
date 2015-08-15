package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.platform.service.parser.ActionResponseParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * This is the base class for the delete itinerary and delete itinerary row.
 *
 * @author Patricius Komarnicki created on 20.07.2015.
 */
public abstract class AbstractItineraryDeleteRequest extends CoreAsyncRequestTask {

    private static class ResponseParser extends ActionResponseParser {

        String status;
        String statusText;

        @Override
        public void handleText(String tag, String text) {
            if ("Status".equals(tag)) {
                this.status = text;
            } else if ("StatusText".equals(tag)) {
                this.statusText = text;
            }
        }
    }

    public static final String RESULT_BUNDLE_ID_MESSAGE = "message";

    private static final String SUCCESS = "SUCCESS";

    private ResponseParser responseParser;

    public AbstractItineraryDeleteRequest(Context context, BaseAsyncResultReceiver receiver) {
        super(context, 0, receiver);
    }

    @Override
    protected abstract String getServiceEndpoint();


    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        responseParser = new ResponseParser();
        parser.registerParser(responseParser, "Body");

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
        if (SUCCESS.equals(responseParser.status)) {
            resultData.putBoolean(IS_SUCCESS, true);
        } else {
            resultData.putBoolean(IS_SUCCESS, false);
            Message msg = new Message(Message.Severity.ERROR, responseParser.statusText, "@ Delete Failed @");
            resultData.putSerializable(RESULT_BUNDLE_ID_MESSAGE, msg);
        }

        return RESULT_OK;
    }

}
