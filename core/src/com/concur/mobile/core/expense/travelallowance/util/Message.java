package com.concur.mobile.core.expense.travelallowance.util;

/**
 * Created by Michael Becherer on 19-Jul-15.
 */
public class Message {

    /**
     * MSG 100: Start date before end date
     */
    public final static int MSG_TA_START_BEFORE_END = 100;

    /**
     * MSG 101: Dates are missing in a period
     */
    public final static int MSG_TA_MISSING_DATES = 101;

    /**
     * MSG 102: Period overlaps predecessor
     */
    public final static int MSG_TA_OVERLAPPING_PREDECESSOR = 102;

    public enum Severity {
        INFO(),
        WARNING(),
        ERROR();
    }

    private Severity severity;

    /**
     * The message code
     */
    private int code;

    private String messageText;

    public Message(Severity severity, int code) {
        this.severity = severity;
        this.code = code;
        this.messageText = StringUtilities.EMPTY_STRING;
    }

    public Message(Severity severity, int code, String messageText) {
        this.severity = severity;
        this.code = code;
        this.messageText = messageText;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getCode() {
        return code;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public String toString() {
        return this.messageText;
    }
}
