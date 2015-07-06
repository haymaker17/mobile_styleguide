package com.concur.mobile.core.expense.travelallowance.service.parser;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;

/**
 * Created by Holger Rose on 02.07.2015.
 */
public class GetTAConfigurationParser extends BaseParser {

    private static final String TACONFIG_TAG = "TaConfig";

//    SimpleDateFormat dateFormat;

    private TravelAllowanceConfiguration configuration;

    public GetTAConfigurationParser() {
        this.configuration = new TravelAllowanceConfiguration();
//        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public TravelAllowanceConfiguration getConfigurationList() {

        // Start: Hard-code some test data
        TravelAllowanceConfiguration TAConfig = new TravelAllowanceConfiguration();
        TAConfig.setDeductForProvidedBreakfast("Y");
        TAConfig.setDeductForProvidedLunch("Y");
        TAConfig.setDeductForProvidedDinner("Y");
        TAConfig.setDefaultBreakfastToProvided("Y");
        TAConfig.setDefaultLunchToProvided("Y");
        TAConfig.setDefaultDinnerToProvided("Y");
        TAConfig.setUseBorderCrossTime(true);
        TAConfig.setMealDeductionList("DE");

        configuration = (TAConfig);
        // End: Hard-code some test data

        return configuration;
    }

    @Override
    public void startTag(String tag) {
        super.startTag(tag);
        int i = 1;
        if (TACONFIG_TAG.equals(tag)) {
            int j = 1;
        }
    }

    @Override
    public void handleText(String tag, String text) {
        super.handleText(tag, text);
        int i = 1;
    }

    @Override
    public void endTag(String tag) {
        super.endTag(tag);
        int i = 1;
    }
}
