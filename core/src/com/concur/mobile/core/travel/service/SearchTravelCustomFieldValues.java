package com.concur.mobile.core.travel.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.travel.service.parser.TravelCustomFieldsParser;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

/**
 * 
 * @author RatanK
 * 
 */
public class SearchTravelCustomFieldValues extends CoreAsyncRequestTask {

    private String attributeId;
    private String searchPattern;
    private TravelCustomFieldsParser custFieldsParser;

    public SearchTravelCustomFieldValues(Context context, int id, BaseAsyncResultReceiver receiver, String attributeId,
            String searchPattern) {
        super(context, id, receiver);
        this.attributeId = attributeId;
        this.searchPattern = searchPattern;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/Config/SearchTravelCustomFieldValues";
    }

    @Override
    protected String getPostBody() {
        // form the the request XML
        StringBuilder sb = new StringBuilder();

        sb.append("<SearchTravelCustomField>");
        FormatUtil.addXMLElementEscaped(sb, "AttributeId", attributeId);
        FormatUtil.addXMLElementEscaped(sb, "SearchPattern", searchPattern);
        sb.append("</SearchTravelCustomField>");

        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        custFieldsParser = new TravelCustomFieldsParser();

        // register the parsers of interest
        parser.registerParser(custFieldsParser, "TravelCustomFieldSearch");

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

        if (custFieldsParser.custField != null) {
            // add in the app object
            ConcurCore core = (ConcurCore) ConcurCore.getContext();
            core.setTravelCustomField(custFieldsParser.custField);
            core.setTravelCustomFieldLastRetrieved(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        } else {
            // log the error message
            Log.e(Const.LOG_TAG, "Error occured while doing search");
            resultcode = RESULT_ERROR;
        }

        return resultcode;
    }
}
