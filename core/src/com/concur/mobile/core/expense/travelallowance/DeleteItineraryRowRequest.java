package com.concur.mobile.core.expense.travelallowance;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

public class DeleteItineraryRowRequest extends CoreAsyncRequestTask {

    public static final String STATUS = "STATUS";
    public static final String STATUS_TEXT = "STATUSTEXT";
    public static final String SUCCESS = "SUCCESS";
    private String itinKey;
    private String irKey;

    private DeleteItineraryRowResultParser myParser;

    public DeleteItineraryRowRequest(Context context, int id, BaseAsyncResultReceiver receiver, String itinKey,
            String irKey) {
        super(context, id, receiver);
        this.itinKey = itinKey;
        this.irKey = irKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/DeleteItineraryRow/" + itinKey + "/" + irKey;
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        myParser = new DeleteItineraryRowResultParser();
        parser.registerParser(myParser, "Body");

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
        resultData.putBoolean(IS_SUCCESS, true);
        resultData.putString(STATUS, myParser.status);
        resultData.putString(STATUS_TEXT, myParser.statusText);

        return RESULT_OK;
    }
}
