package com.concur.mobile.core.travel.data;

public class RuleViolationReason {

    /**
     * violation reason code i.e. Spouse / Family travel
     */
    private String reasonCode;

    /**
     * traveler comments
     */
    private String bookerComments;

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getBookerComments() {
        return bookerComments;
    }

    public void setBookerComments(String bookerComments) {
        this.bookerComments = bookerComments;
    }
}
