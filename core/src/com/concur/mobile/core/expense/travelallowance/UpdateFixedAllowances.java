package com.concur.mobile.core.expense.travelallowance;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.service.parser.ActionResponseParser;

public class UpdateFixedAllowances extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetTAItinerariesRequest.class.getSimpleName();

    private ResultsParser resultsParser;
    private FixedAllowances fixedAllowances;
    private String rptKey;

    public UpdateFixedAllowances(Context context, int id, BaseAsyncResultReceiver receiver, String rptKey, FixedAllowances fixedAllowances) {
        super(context, id, receiver);
        this.fixedAllowances = fixedAllowances;
        this.rptKey = rptKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/UpdateFixedAllowances/" + rptKey;
    }

    @Override
    protected String getPostBody() {
        StringBuilder sb = new StringBuilder();

        for (FixedAllowanceRow row : fixedAllowances.getRows()) {
            sb.append("<FixedAllowanceRow>");
            FormatUtil.addXMLElementEscaped(sb, "TaDayKey", row.getTaDayKey());
            TaXmlUtil.appendXmlIfTrue(sb, "MarkedExcluded", row.getMarkedExcluded());
            TaXmlUtil.appendXmlIfNotEmpty(sb, "FixedRptKey", row.getFixedRptKey());
            TaXmlUtil.appendXmlIfTrue(sb, "IsFirstDay", row.getIsFirstDay());
            TaXmlUtil.appendXmlIfTrue(sb, "IsLastDay", row.getIsLastDay());
            if (fixedAllowances.getShowOvernightCheckBox()) {
                TaXmlUtil.appendXmlIfTrue(sb, "Overnight", row.getOvernight());
            }
            if (fixedAllowances.getShowExtendedTripCheckBox()) {
                TaXmlUtil.appendXmlIfTrue(sb, "ApplyExtendedTripRule", row.getApplyExtendedTripRule());
            }
            if (fixedAllowances.getShowPercentRuleCheckBox()) {
                TaXmlUtil.appendXmlIfTrue(sb, "ApplyPercentRule", row.getApplyPercentRule());
            }
            if (fixedAllowances.getShowMunicipalityCheckBox()) {
                TaXmlUtil.appendXmlIfTrue(sb, "WithinMunicipalArea", row.getWithinMunicipalArea());
            }
            if (fixedAllowances.getShowLunchProvidedCheckBox() || fixedAllowances.getShowLunchProvidedPickList()) {
                TaXmlUtil.appendXmlIfNotEmpty(sb, "LunchProvided", row.getLunchProvided());
            }
            if (fixedAllowances.getShowDinnerProvidedCheckBox() || fixedAllowances.getShowDinnerProvidedPickList()) {
                TaXmlUtil.appendXmlIfNotEmpty(sb, "DinnerProvided", row.getDinnerProvided());
            }
            if (fixedAllowances.getShowUserEntryOfBreakfastAmount()) {
            	// TODO
            	/*
                var aArr = record.data.BreakfastAmount_P.split("|");
				if (aArr[0]!= "")
                    s.push('<BreakfastTransactionAmount>'+aArr[0]+'</BreakfastTransactionAmount>');
				if (aArr[1]!= "")
                    s.push('<BreakfastPostedAmount>'+aArr[1]+'</BreakfastPostedAmount>');
                if (aArr[2]!= "")
					s.push('<BreakfastCrnCode>'+aArr[2]+'</BreakfastCrnCode>');
				if (aArr[3]!= "")
					s.push('<BreakfastExchangeRate>'+aArr[3]+'</BreakfastExchangeRate>');
				if (aArr[4]!= "")
					s.push('<BreakfastErDirection>'+aArr[4]+'</BreakfastErDirection>');
            	 */
            }
            if (fixedAllowances.getShowBreakfastProvidedCheckBox() || fixedAllowances.getShowBreakfastProvidedPickList()) {
                TaXmlUtil.appendXmlIfNotEmpty(sb, "BreakfastProvided", row.getBreakfastProvided());
            }
            if (fixedAllowances.getShowLodgingTypePickList()) {
            	TaXmlUtil.appendXmlIfNotEmpty(sb, "LogdingType", row.getLodgingType());
            }
            sb.append("</FixedAllowanceRow>");
        }
        
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

    static class ResultsParser extends ActionResponseParser {
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
}
