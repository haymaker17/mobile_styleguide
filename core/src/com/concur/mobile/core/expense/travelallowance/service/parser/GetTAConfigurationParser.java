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
    private  TravelAllowanceConfiguration newConfiguration;

    public GetTAConfigurationParser() {
        this.configuration = new TravelAllowanceConfiguration();
//        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public TravelAllowanceConfiguration getConfigurationList() {
        return configuration;
    }

    @Override
    public void startTag(String tag) {
        super.startTag(tag);
        if (TACONFIG_TAG.equals(tag)) {
            newConfiguration = new TravelAllowanceConfiguration();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        super.handleText(tag, text);
        switch (tag){
            case "useBorderCrossTime":
                if ( "Y".equals(text) ) {
                    newConfiguration.setUseBorderCrossTime(true);
                }else {
                    newConfiguration.setUseBorderCrossTime(false);
                }
                break;
            case "configCode":
                newConfiguration.setConfigCode(text);
                break;
            case "deductForProvidedBreakfast":
                newConfiguration.setDeductForProvidedBreakfast(text);
                break;
            case "deductForProvidedLunch":
                newConfiguration.setDeductForProvidedLunch(text);
                break;
            case "deductForProvidedDinner":
                newConfiguration.setDeductForProvidedDinner(text);
                break;
            case "defaultBreakfastToProvided":
                newConfiguration.setDefaultBreakfastToProvided(text);
                break;
            case "defaultLunchToProvided":
                newConfiguration.setDefaultLunchToProvided(text);
                break;
            case "defaultDinnerToProvided":
                newConfiguration.setDefaultDinnerToProvided(text);
                break;
            case "lodgingTat":
                newConfiguration.setLodgingTat(text);
                break;
            case "mealDeductionList":
                newConfiguration.setMealDeductionList(text);
                break;
            case "mealsTat":
                newConfiguration.setMealsTat(text);
                break;
            case "singleRowCheck":
                newConfiguration.setSingleRowCheck(text);
                break;
            case "tacKey":
                newConfiguration.setTacKey(text);
                break;
            case "useLodgingType":
                if ( "Y".equals(text) ) {
                    newConfiguration.setUseLodgingType(true);
                }else {
                    newConfiguration.setUseLodgingType(false);
                }
                break;
            case "useOvernight":
                if ( "Y".equals(text) ) {
                    newConfiguration.setUseOvernight(true);
                }else {
                    newConfiguration.setUseOvernight(false);
                }
                break;
            case "displayWizard":
                if ("NEVER".equals(text)) {
                    newConfiguration.setDisplayWizard(false);
                } else {
                    newConfiguration.setDisplayWizard(true);
                }
            default:
                break;
        };
    }

    @Override
    public void endTag(String tag) {
        super.endTag(tag);
        if (TACONFIG_TAG.equals(tag)) {
            configuration = newConfiguration;
        }
    }
}
