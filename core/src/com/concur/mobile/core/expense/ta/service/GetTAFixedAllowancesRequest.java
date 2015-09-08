package com.concur.mobile.core.expense.ta.service;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Const;

public class GetTAFixedAllowancesRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetTAFixedAllowancesRequest.class.getSimpleName();
    private String rptKey;

    private GetTAFixedAllowancesResultsParser resultsParser;

    public GetTAFixedAllowancesRequest(Context context, int id, BaseAsyncResultReceiver receiver, String rptKey) {
        super(context, id, receiver);
        this.rptKey = rptKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetTaFixedAllowances/" + rptKey;
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        resultsParser = new GetTAFixedAllowancesResultsParser();
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
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        core.setFixedAllowances(resultsParser.getFixedAllowances());

        return RESULT_OK;
    }

    static class GetTAFixedAllowancesResultsParser extends ReflectionActionResponseParser {
        private FixedAllowances fixedAllowances = new FixedAllowances();
        private FixedAllowanceRow currentRow = null;
        
        private static HashMap<String, PropType> controlProps;
        private static HashMap<String, PropType> rowProps;

        static {
           // controlProps = new HashMap<String, GetTAItinerariesResponseParser.PropType>();
            controlProps.put("ShowUserEntryOfBreakfastAmount", PropType.BOOLEAN);
            controlProps.put("ShowUserEntryOfMealsAmount", PropType.BOOLEAN);
            controlProps.put("ShowBreakfastProvidedCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowLunchProvidedCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowDinnerProvidedCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowBreakfastProvidedPickList", PropType.BOOLEAN);
            controlProps.put("ShowLunchProvidedPickList", PropType.BOOLEAN);
            controlProps.put("ShowDinnerProvidedPickList", PropType.BOOLEAN);
            controlProps.put("ShowOvernightCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowOvernightAsNightAllowance", PropType.BOOLEAN);
            controlProps.put("ShowAboveLimit", PropType.BOOLEAN);
            controlProps.put("ShowMealsBaseAmount", PropType.BOOLEAN);
            controlProps.put("ShowLodgingTypePickList", PropType.BOOLEAN);
            controlProps.put("ShowPercentRuleCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowExtendedTripCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowMunicipalityCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowExcludeCheckBox", PropType.BOOLEAN);
            controlProps.put("ShowAllowanceAmount", PropType.BOOLEAN);
            controlProps.put("ExcludeLabel", PropType.STRING);
            controlProps.put("LodgingTypeLabel", PropType.STRING);
            controlProps.put("ApplyPercentRuleLabel", PropType.STRING);
            controlProps.put("ApplyExtendedTripRuleLabel", PropType.STRING);
            controlProps.put("MunicipalAreaLabel", PropType.STRING);
            controlProps.put("OvernightLabel", PropType.STRING);
            controlProps.put("BreakfastProvidedLabel", PropType.STRING);
            controlProps.put("LunchProvidedLabel", PropType.STRING);
            controlProps.put("DinnerProvidedLabel", PropType.STRING);
            
           // rowProps = new HashMap<String, GetTAItinerariesResponseParser.PropType>();
            rowProps.put("IsFirstDay", PropType.BOOLEAN);
            rowProps.put("IsLastDay", PropType.BOOLEAN);
            rowProps.put("IsLocked", PropType.BOOLEAN);
            rowProps.put("IsReadOnly", PropType.BOOLEAN);
            rowProps.put("TaDayKey", PropType.STRING);
            rowProps.put("ItinKey", PropType.STRING);
            rowProps.put("MarkedExcluded", PropType.BOOLEAN);
            rowProps.put("FixedRptKey", PropType.STRING);
            rowProps.put("InUseLock", PropType.BOOLEAN);
            rowProps.put("AllowanceDate", PropType.DATE);
            rowProps.put("Overnight", PropType.BOOLEAN);
            rowProps.put("ApplyExtendedTripRule", PropType.BOOLEAN);
            rowProps.put("ApplyPercentRule", PropType.BOOLEAN);
            rowProps.put("LodgingType", PropType.STRING);
            rowProps.put("WithinMunicipalArea", PropType.BOOLEAN);
            rowProps.put("AllowanceAmount", PropType.DOUBLE);
            rowProps.put("BreakfastProvided", PropType.STRING);
            rowProps.put("LunchProvided", PropType.STRING);
            rowProps.put("DinnerProvided", PropType.STRING);
            rowProps.put("BreakfastTransactionAmount", PropType.DOUBLE);
            rowProps.put("BreakfastPostedAmount", PropType.DOUBLE);
            rowProps.put("Location", PropType.STRING);
            rowProps.put("AboveLimitAmount", PropType.DOUBLE);
            rowProps.put("MealsBaseAmount", PropType.DOUBLE);
        }
        
        FixedAllowances getFixedAllowances() {
            return fixedAllowances;
        }
        
        @Override
        public void startTag(String tag) {
            super.startTag(tag);
            if ("FixedAllowanceRow".equals(tag)) {
                currentRow = new FixedAllowanceRow();
            }
        }

        @Override
        public void endTag(String tag) {
            super.endTag(tag);
            if ("FixedAllowanceRow".equals(tag)) {
                fixedAllowances.getRows().add(currentRow);
            }
        }

        @Override
        public void handleText(String tag, String text) {
            try {
                if (currentRow == null) {
                    super.handleText(tag, text, controlProps, fixedAllowances);
                } else {
                    super.handleText(tag, text, rowProps, currentRow);
                }
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, "exception parsing response. tag=" + tag, e);
            }
        }
    }
}
