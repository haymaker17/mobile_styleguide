package com.concur.mobile.core.expense.travelallowance.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.TaXmlUtil;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.service.parser.ActionResponseParser;

public class UpdateFixedAllowances extends CoreAsyncRequestTask {

    private class ResultsParser extends ActionResponseParser {
        String status;
        String statusText;

        @Override
        public void handleText(String tag, String text) {
            if ("Status".equals(tag)) {
                status = text;
            } else if ("StatusText".equals(tag)) {
                statusText = text;
            }
        }
    }

    public static final String LOG_TAG = GetTAItinerariesRequest.class.getSimpleName();

    private ResultsParser resultsParser;
    private FixedTravelAllowance fixedAllowance;
    private String rptKey;

    public UpdateFixedAllowances(Context context, BaseAsyncResultReceiver receiver, String rptKey, FixedTravelAllowance fixedAllowance) {
        super(context, 0, receiver);
        this.fixedAllowance = fixedAllowance;
        this.rptKey = rptKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/UpdateFixedAllowances/" + rptKey;
    }

    @Override
    protected String getPostBody() {
        StringBuilder sb = new StringBuilder();

        sb.append("<FixedAllowanceRow>");
        FormatUtil.addXMLElementEscaped(sb, "TaDayKey", fixedAllowance.getFixedTravelAllowanceId());
        TaXmlUtil.appendXmlIfTrue(sb, "MarkedExcluded", fixedAllowance.getExcludedIndicator());
        TaXmlUtil.appendXmlIfTrue(sb, "Overnight", fixedAllowance.getOvernightIndicator());
        TaXmlUtil.appendXmlIfNotEmpty(sb, "BreakfastProvided", fixedAllowance.getBreakfastProvision().getCode());
        TaXmlUtil.appendXmlIfNotEmpty(sb, "LunchProvided", fixedAllowance.getLunchProvision().getCode());
        TaXmlUtil.appendXmlIfNotEmpty(sb, "DinnerProvided", fixedAllowance.getDinnerProvision().getCode());
        if (fixedAllowance.getLodgingType() != null) {
            TaXmlUtil.appendXmlIfNotEmpty(sb, "LodgingType", fixedAllowance.getLodgingType().getCode());
        }

        sb.append("</FixedAllowanceRow>");

        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        resultsParser = new ResultsParser();
        parser.registerParser(resultsParser, "Body");

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
        resultData.putString("StatusText", resultsParser.statusText);
        if ("SUCCESS".equals(resultsParser.status)) {
            resultData.putBoolean(IS_SUCCESS, true);
            return RESULT_OK;
        } else {
            resultData.putBoolean(IS_SUCCESS, false);
            return RESULT_ERROR;
        }
    }

}
