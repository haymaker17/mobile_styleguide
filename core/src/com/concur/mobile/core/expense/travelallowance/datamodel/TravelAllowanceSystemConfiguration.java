package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Created by Michael Becherer on 08-Aug-15.
 */
public class TravelAllowanceSystemConfiguration implements Serializable {

    private static final String CLASS_TAG = TravelAllowanceSystemConfiguration.class.getSimpleName();
    private static final long serialVersionUID = -3152180400769835301L;

    /**
     * Indicates, whether Travel Allowances is enabled in general
     */
    private boolean travelAllowanceEnabled;

    public boolean isTravelAllowanceEnabled() {
        return travelAllowanceEnabled;
    }

    public void setTravelAllowanceEnabled(boolean travelAllowanceEnabled) {
        this.travelAllowanceEnabled = travelAllowanceEnabled;
    }
}
