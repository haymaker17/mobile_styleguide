/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.data;

import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.gov.travel.air.activity.GovRateType;
import com.concur.mobile.platform.util.Parse;

/**
 * @author Chris N. Diaz
 * 
 */
public class GovAirChoice extends AirChoice {

    public Integer maxEnforcementLevel;
    public GovRateType rateType;

    /**
     * 
     */
    public GovAirChoice() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.core.data.travel.AirChoice#handleElement(java.lang.String, java.lang.String)
     */
    @Override
    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("RateType")) {
            rateType = GovRateType.getRateType(cleanChars);
        } else if (localName.equalsIgnoreCase("MaxEnforcementLevel")) {
            maxEnforcementLevel = Parse.safeParseInteger(cleanChars);
        } else {
            super.handleElement(localName, cleanChars);
        }
    }

}
