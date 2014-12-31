/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.gov.travel.air.activity;

import com.concur.mobile.core.ConcurCore;
import com.concur.gov.R;

/**
 * @author Chris N. Diaz
 * 
 */
public enum GovRateType {

    LIMITED_CAPACITY ("LimitedCapacity", R.string.air_cat_limited_capacity),
    CONTRACT ("Contract", R.string.air_cat_contract),
    CONTRACT_BUSINESS ("ContractBusiness", R.string.air_cat_contract_business),
    ME_TOO ("MeToo", R.string.air_cat_me_too),
    LOWEST_PUBLISHED ("LowestPublished", R.string.air_cat_lowest_published),
    NONE ("None", com.concur.core.R.string.other);

    /**
     * The type of Gov't rate.
     */
    public String type;
    /**
     * The human-readable (localized) display name of this Gov't rate type.
     */
    public String displayName;

    /**
     * 
     * @param type
     *            the Gov't rate type.
     * @param rateTypeNameId
     *            the resource ID of this Gov't rate type.
     */
    private GovRateType(String type, int rateTypeNameId) {
        this.type = type;
        this.displayName = ConcurCore.getContext().getString(rateTypeNameId);
    }

    /**
     * Finds the corresponding GovRateType of the specified <code>rateType</code> or <code>null</code> if none is found.
     * 
     * @param rateType
     *            the rate type to find.
     * @return the corresponding GovRateType of the specified <code>rateType</code> or <code>null</code> if none is found.
     */
    public final static GovRateType getRateType(String rateType) {
        if (rateType != null) {
            for (GovRateType type : values()) {
                if (type.type.equals(rateType)) {
                    return type;
                }
            }
        }

        return null;
    }

}
