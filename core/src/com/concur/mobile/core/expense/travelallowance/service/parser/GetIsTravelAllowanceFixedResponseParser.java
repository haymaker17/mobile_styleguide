package com.concur.mobile.core.expense.travelallowance.service.parser;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceSystemConfiguration;

/**
 * Created by Michael Becherer on 08-Aug-15.
 */
public class GetIsTravelAllowanceFixedResponseParser extends BaseParser {

    private static final String CLASS_TAG = GetIsTravelAllowanceFixedResponseParser.class.getSimpleName();
    private TravelAllowanceSystemConfiguration configuration;

    public GetIsTravelAllowanceFixedResponseParser() {
        this.configuration = new TravelAllowanceSystemConfiguration();
    }

    @Override
    public void handleText(String tag, String text) {
        super.handleText(tag, text);
        switch (tag) {
            case "IsTravelAllowanceFixed":
                if ("Y".equals(text)) {
                    configuration.setTravelAllowanceEnabled(true);
                }
                break;
            default: break;
        }
    }

    public TravelAllowanceSystemConfiguration getConfiguration() {
        return configuration;
    }
}
