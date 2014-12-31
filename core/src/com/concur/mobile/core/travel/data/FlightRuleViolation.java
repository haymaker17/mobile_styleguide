package com.concur.mobile.core.travel.data;

public class FlightRuleViolation extends RuleViolation {

    private String cost;
    private String refundable;

    public String getQuotedTotal() {
        StringBuilder sbr = new StringBuilder(cost);
        sbr.append('\n');
        sbr.append(refundable);
        return sbr.toString();
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getRefundable() {
        return refundable;
    }

    public void setRefundable(String refundable) {
        this.refundable = refundable;
    }

}
