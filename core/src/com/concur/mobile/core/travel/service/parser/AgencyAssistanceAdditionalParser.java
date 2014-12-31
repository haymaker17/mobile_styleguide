package com.concur.mobile.core.travel.service.parser;

import com.concur.mobile.base.service.parser.Parser;

/**
 * Parser for additional elements that are part of response for /Mobile/Agency/GetAgencyAssistance request
 * 
 * @author RatanK
 * 
 */
public class AgencyAssistanceAdditionalParser implements Parser {

    public String tripRecordLocator;
    public String itinLocator;

    @Override
    public void startTag(String tag) {
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("TripRecordLocator")) {
            tripRecordLocator = text;
        } else if (tag.equals("ItinLocator")) {
            itinLocator = text;
        }
    }

    @Override
    public void endTag(String tag) {
    }

}
