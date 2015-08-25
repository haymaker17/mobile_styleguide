package com.concur.mobile.core.expense.ta.service;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.util.TaXmlUtil;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Const;

public class GetUpdatedFixedAllowanceAmounts extends CoreAsyncRequestTask {

    public static final String KEY = "UpdatedFixedAllowanceAmounts";
    public static final String LOG_TAG = GetUpdatedFixedAllowanceAmounts.class.getSimpleName();
    private String rptKey;
    private FixedAllowances fixedAllowances;
    private FixedAllowanceRow fixedAllowanceRow;
    
    private ResultsParser resultsParser;

    public GetUpdatedFixedAllowanceAmounts(Context context, int id, BaseAsyncResultReceiver receiver, String rptKey, FixedAllowances fixedAllowances, FixedAllowanceRow row) {
        super(context, id, receiver);
        this.rptKey = rptKey;
        this.fixedAllowanceRow = row;
        this.fixedAllowances = fixedAllowances;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetUpdatedFixedAllowanceAmounts/" + rptKey;
    }

    @Override
    protected String getPostBody() {
        StringBuilder sb = new StringBuilder();

        TaXmlUtil.appendXml(sb, "TaDayKey", fixedAllowanceRow.getTaDayKey());
        TaXmlUtil.appendXml(sb, "MarkedExcluded", fixedAllowanceRow.getMarkedExcluded());
        TaXmlUtil.appendXml(sb, "FixedRptKey", fixedAllowanceRow.getFixedRptKey());
        TaXmlUtil.appendXml(sb, "IsFirstDay", fixedAllowanceRow.getIsFirstDay());
        TaXmlUtil.appendXml(sb, "IsLastDay", fixedAllowanceRow.getIsFirstDay());
        if (fixedAllowances.getShowOvernightCheckBox()) {
        	TaXmlUtil.appendXml(sb, "Overnight", fixedAllowanceRow.getOvernight());
        }
        if (fixedAllowances.getShowExtendedTripCheckBox()) {
            TaXmlUtil.appendXml(sb, "ApplyExtendedTripRule", fixedAllowanceRow.getApplyExtendedTripRule());
        }
        if (fixedAllowances.getShowPercentRuleCheckBox()) {
            TaXmlUtil.appendXml(sb, "ApplyPercentRule", fixedAllowanceRow.getApplyPercentRule());
        }
        if (fixedAllowances.getShowMunicipalityCheckBox()) {
            TaXmlUtil.appendXml(sb, "WithinMunicipalArea", fixedAllowanceRow.getWithinMunicipalArea());
        }
        if (fixedAllowances.getShowLunchProvidedCheckBox() || fixedAllowances.getShowLunchProvidedPickList()) {
            TaXmlUtil.appendXml(sb, "LunchProvided", fixedAllowanceRow.getLunchProvided());
        }
        if (fixedAllowances.getShowDinnerProvidedCheckBox() || fixedAllowances.getShowDinnerProvidedPickList()) {
            TaXmlUtil.appendXml(sb, "DinnerProvided", fixedAllowanceRow.getDinnerProvided());
        }
        if (fixedAllowances.getShowUserEntryOfBreakfastAmount()) {
            // TODO
            // BreakfastTransactionAmount
            // BreakfastPostedAmount
            // BreakfastCrnCode
            // BreakfastExchangeRate
            // BreakfastErDirection
        }
        if (fixedAllowances.getShowBreakfastProvidedCheckBox() || fixedAllowances.getShowBreakfastProvidedPickList()) {
            TaXmlUtil.appendXml(sb, "BreakfastProvided", fixedAllowanceRow.getBreakfastProvided());
        }
        if (fixedAllowances.getShowLodgingTypePickList()) {
            TaXmlUtil.appendXml(sb, "LodgingType", fixedAllowanceRow.getLodgingType());
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
        resultData.putBoolean(IS_SUCCESS, true);
        resultData.putSerializable(KEY, resultsParser.amounts);

        return RESULT_OK;
    }

    static class ResultsParser extends ReflectionActionResponseParser {
        FixedAllowanceAmounts amounts = new FixedAllowanceAmounts();
        
        private static HashMap<String, PropType> props;

        static {
            props = new HashMap<String, PropType>();
            props.put("TaDayKey", PropType.STRING);
            props.put("AllowanceAmount", PropType.DOUBLE);
            props.put("AboveLimitAmount", PropType.DOUBLE);
        }
        
        @Override
        public void handleText(String tag, String text) {
            try {
                super.handleText(tag, text, props, amounts);
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, "exception parsing response. tag=" + tag, e);
            }
        }
    }
}
