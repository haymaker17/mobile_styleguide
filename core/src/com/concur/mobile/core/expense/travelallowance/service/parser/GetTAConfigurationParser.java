package com.concur.mobile.core.expense.travelallowance.service.parser;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D023077 on 02.07.2015.
 */
public class GetTAConfigurationParser extends BaseParser {


    SimpleDateFormat dateFormat;

    private List<TravelAllowanceConfiguration> configurations;

    public GetTAConfigurationParser() {
        this.configurations  = new ArrayList<TravelAllowanceConfiguration>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public List<TravelAllowanceConfiguration> getConfigurationList() {

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

        configurations.add(TAConfig);
        // End: Hard-code some test data

        return configurations;
    }

    @Override
    public void startTag(String tag) {
        super.startTag(tag);
        int i = 1;
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
