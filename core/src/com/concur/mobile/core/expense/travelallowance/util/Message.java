package com.concur.mobile.core.expense.travelallowance.util;

/**
 * Created by Michael Becherer on 19-Jul-15.
 */
public class Message {

    public final static String MSG_UI_START_BEFORE_END = "UI.StartBeforeEnd";
    public final static String MSG_UI_MISSING_DATES = "UI.MissingDates";
    public final static String MSG_UI_OVERLAPPING_PREDECESSOR = "UI.OverlappingPredecessor";

    /**
     * The possible severities
     */
    public enum Severity {
        INFO(),
        WARNING(),
        ERROR();
    }

    /**
     * Severity of this message object
     */
    private Severity severity;

    /**
     * The message code
     */
    private String code;

    /**
     * The readable localized message text
     */
    private String messageText;

    /**
     * Creates a message object. Defaults {@link #messageText} with an empty String.
     * Usually called for device related messages.
     * @param severity The severity of the message
     * @param code The coded representation (see Message.MSG_UI_<...>)
     */
    public Message(Severity severity, String code) {
        this.severity = severity;
        this.code = code;
        this.messageText = StringUtilities.EMPTY_STRING;
    }

    /**
     * Creates a message object.
     * Usually called for device related messages.
     * @param severity The severity of the message
     * @param code The coded representation (see Message.MSG_UI_<...>)
     * @param messageText The readable localized message text
     */
    public Message(Severity severity, String code, String messageText) {
        this.severity = severity;
        this.code = code;
        this.messageText = messageText;
    }

    /**
     * Creates a message object.
     * Usually called for backend related messages.
     * @param statusText The status text representing the message code
     * @param statusTextLocalized The readable localized message text
     */
    public Message(String statusText, String statusTextLocalized) {
        this.code = statusText;
        this.messageText =  statusTextLocalized;
        if (statusText.contains(".Error")) {
            this.severity = Severity.ERROR;
            return;
        }
        if (statusText.contains(".Warning")) {
            this.severity = Severity.WARNING;
            return;
        }
        this.severity = Severity.INFO;
    }

    /**
     * Creates a message object. Defaults {@link #messageText} with an empty String.
     * Usually called for backend related messages.
     * @param statusText The status text representing the message code
     */
    public Message(String statusText) {
        this(statusText, StringUtilities.EMPTY_STRING);
    }

    /**
     * Getter method
     * @return the severity of this message object
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Getter method
     * @return the coded representation of this message object
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method
     * @return the human readable localized message text
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Setter method
     * @param severity the severity to be set. Overwrites the existing one.
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * Setter method
     * @param messageText the human readable localized message text to be set. Overwrites
     *                    the existing one.
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * {@InheritDoc}
     *
     * @return the human readable localized message text or an empty string.
     */
    @Override
    public String toString() {
        return this.messageText;
    }
}
