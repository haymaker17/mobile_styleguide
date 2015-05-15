package com.concur.mobile.platform.travel.search.hotel;

import com.google.gson.annotations.SerializedName;

/**
 * Value object to hold the violation, reason and justification
 * 
 * @author ratank
 * 
 */
public class ViolationReason {

    public String ruleValueId;

    @SerializedName("code")
    public String violationReasonCode;

    @SerializedName("justification")
    public String justification;
}
