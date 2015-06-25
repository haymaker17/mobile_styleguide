package com.concur.mobile.core.expense.travelallowance;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.service.CoreAsyncRequestTask;
import com.concur.mobile.core.util.Const;

public class GetTAConfigRequest extends CoreAsyncRequestTask {

    public static final String LOG_TAG = GetTAItinerariesRequest.class.getSimpleName();

    private GetTAConfigRequestResultsParser resultsParser;

    public GetTAConfigRequest(Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, id, receiver);
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetTAConfigForEmployee";
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        resultsParser = new GetTAConfigRequestResultsParser();
        parser.registerParser(resultsParser, "TaConfig");

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
        core.setTAConfig(resultsParser.taConfig);

        return RESULT_OK;
    }

    static class GetTAConfigRequestResultsParser extends ReflectionActionResponseParser {

        public TaConfig taConfig = new TaConfig();

        private static HashMap<String, PropType> props = new HashMap<String, ReflectionActionResponseParser.PropType>();

        static {
            props.put("tacKey", PropType.STRING);
            props.put("configCode", PropType.STRING);
            props.put("rfKey", PropType.STRING);
            props.put("isDeleted", PropType.STRING);
            props.put("mealsTat", PropType.STRING);
            props.put("lodgingTat", PropType.STRING);
            props.put("govtCompRateTypes", PropType.STRING);
            props.put("combineMealsAndLodgingRate", PropType.STRING);
            props.put("deductForProvidedBreakfast", PropType.STRING);
            props.put("deductForProvidedLunch", PropType.STRING);
            props.put("deductForProvidedDinner", PropType.STRING);
            props.put("bikMealsDeduction", PropType.STRING);
            props.put("defaultBreakfastToProvided", PropType.STRING);
            props.put("defaultLunchToProvided", PropType.STRING);
            props.put("defaultDinnerToProvided", PropType.STRING);
            props.put("userEntryOfBreakfastAmount", PropType.STRING);
            props.put("sameDay", PropType.STRING);
            props.put("userEntryOfRateLocation", PropType.STRING);
            props.put("useOvernight", PropType.STRING);
            props.put("displayCompanyAndGovernment", PropType.STRING);
            props.put("doubleDipCheck", PropType.STRING);
            props.put("singleRowCheck", PropType.STRING);
            props.put("displayBaseMealsRate", PropType.STRING);
            props.put("displayWizard", PropType.STRING);
            props.put("useLodgingType", PropType.STRING);
            props.put("useExtendedTripRule", PropType.STRING);
            props.put("usePercentRule", PropType.STRING);
            props.put("useBorderCrossTime", PropType.STRING);
            props.put("exchangeRateDay", PropType.STRING);
            props.put("useWithinMunicipalArea", PropType.STRING);
            props.put("userEntryOfMealsAmount", PropType.STRING);
            props.put("displayQuickItinPage", PropType.STRING);
        }

        @Override
        public void handleText(String tag, String text) {
            try {
                PropType propType = props.get(tag);
                if (propType != null) {
                    switch (propType) {
                    case STRING:
                        setStringProperty(taConfig, tag, text);
                        break;
                    case DATE:
                        setDateProperty(taConfig, tag, text);
                        break;
                    case BOOLEAN:
                        setBooleanProperty(taConfig, tag, text);
                        break;
                    default:
                        super.handleText(tag, text);
                    }
                }
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, "exception parsing response.  tag=" + tag, e);
            }
        }
    }
}
