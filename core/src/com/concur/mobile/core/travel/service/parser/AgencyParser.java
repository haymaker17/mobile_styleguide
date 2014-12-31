package com.concur.mobile.core.travel.service.parser;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.core.travel.data.Agency;
import com.concur.mobile.core.travel.data.Agency.PreferredTimeForPhoneEnum;

/**
 * Parser for Agency elements a response for /Mobile/Agency/GetAgencyAssistance request
 * 
 * @author RatanK
 * 
 */
public class AgencyParser implements Parser {

    public Agency agency;

    @Override
    public void startTag(String tag) {
        if (tag.equals("Agency")) {
            agency = new Agency();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("Name")) {
            agency.setName(text);
        } /*
           * else if (tag.equals("Address")) { agency.setAddress(text); }
           */else if (tag.equals("DaytimeHoursStarts")) {
            agency.setDayTimeHoursStarts(text);
        } else if (tag.equals("DaytimeHoursEnds")) {
            agency.setDayTimeHoursEnds(text);
        } else if (tag.equals("NightHoursStarts")) {
            agency.setNightTimeHoursStarts(text);
        } else if (tag.equals("NightHoursEnds")) {
            agency.setNightTimeHoursEnds(text);
        } else if (tag.equals("DaytimeMessage")) {
            agency.setDayTimeMessage(text);
        } else if (tag.equals("NightMessage")) {
            agency.setNightTimeMessage(text);
        } else if (tag.equals("DaytimePhone")) {
            agency.setDayTimePhone(text);
        } else if (tag.equals("NightPhone")) {
            agency.setNightTimePhone(text);
        } else if (tag.equals("PreferredPhone")) {
            agency.setPreferredTimeForPhoneEnum(PreferredTimeForPhoneEnum.valueOf(text));
        }

    }

    @Override
    public void endTag(String tag) {
        // nothing to do here
    }

}
