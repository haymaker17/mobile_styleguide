package com.concur.mobile.platform.travel.search.hotel;

import com.google.gson.annotations.SerializedName;

/**
 * Value object to hold the violation, reason and justification
 * 
 * @author ratank
 * 
 */
public class ViolationReason {

    @SerializedName("RuleValueId")
    public String ruleValueId;

    @SerializedName("Code")
    public String violationReasonCode;

    @SerializedName("Justification")
    public String justification;
}
