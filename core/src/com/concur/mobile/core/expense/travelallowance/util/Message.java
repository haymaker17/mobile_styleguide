package com.concur.mobile.core.expense.travelallowance.util;

import android.content.Context;

import com.concur.core.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Becherer on 19-Jul-15.
 */
public class Message implements Serializable {

    public final static String MSG_UI_START_BEFORE_END = "UI.StartBeforeEnd";
    public final static String MSG_UI_MISSING_DATES = "UI.MissingDates";
    public final static String MSG_UI_OVERLAPPING_PREDECESSOR = "UI.OverlappingPredecessor";
    public final static String MSG_UI_OVERLAPPING_SUCCESSOR = "UI.OverlappingSuccessor";

    private static final long serialVersionUID = 1176695456567753781L;
    private static final String CLASS_TAG = Message.class.getSimpleName();

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

    private List<String> fields;

    private Object sourceObject;

    /**
     * Creates a message object. Defaults {@link #messageText} with an empty String.
     * Usually called for device related messages.
     * @param severity The severity of the message
     * @param code The coded representation (see Message.MSG_UI_<...>)
     */
    public Message(Severity severity, String code) {
        this(severity, code, StringUtilities.EMPTY_STRING);
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
     * Usually called for device related messages.
     * @param severity The severity of the message
     * @param code The coded representation (see Message.MSG_UI_<...>)
     * @param fields The fields this message is referring to
     */
    public Message(Severity severity, String code, String messageText, String... fields) {
        this(severity, code, messageText);
        this.fields = new ArrayList<String>();
        for (String field : fields) {
            if (!StringUtilities.isNullOrEmpty(field) && !this.fields.contains(field)) {
                this.fields.add(field);
            }
        }
    }

    /**
     * Creates a message object.
     * Usually called for backend related messages.
     * @param statusText The status text representing the message code
     * @param statusTextLocalized The readable localized message text
     */
    public Message(String statusText, String statusTextLocalized) {
        this.code = statusText;
        this.messageText = statusTextLocalized;
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
     * Checks, whether this {@code Message} refers to the given field
     * @param field The field to be checked
     * @return true, if the {@code Message} refers to the given field, otherwise false.
     */
    public boolean containsField(String field) {
        if (StringUtilities.isNullOrEmpty(field) || this.fields == null || this.fields.size() == 0){
            return false;
        }
        return fields.contains(field);
    }

    /**
     * Removes the given field references from this {@code Message}
     * @param fields The field references ro be removed
     */
    public void removeField(String... fields) {
        if (this.fields == null || this.fields.size() == 0) {
            return;
        }
        for (String field : fields) {
            if (!StringUtilities.isNullOrEmpty(field)) {
                this.fields.remove(field);
            }
        }
    }

    /**
     * Removes all field references from this {@code Message}
     */
    public void removeAllFields() {
        if (this.fields != null) {
            this.fields = new ArrayList<String>();
        }
    }

    /**
     * Getter method
     * @return the human readable localized message text
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Getter method for convenience
     *
     * @return the human readable localized message text, if available.
     * If context equals null, an empty String is returned. If no message text is
     * available a standard text will be derived from the severity using the context
     * at least for messages with severity Error (Internal Error). Other severities
     * cause empty strings.
     */
    public String getMessageText(Context context) {
        if (context == null) {
            return StringUtilities.EMPTY_STRING;
        }
        if (StringUtilities.isNullOrEmpty(messageText)) {
            String text = StringUtilities.EMPTY_STRING;
            if (severity == Severity.ERROR) {
                text = context.getString(R.string.general_error);
            }
            return text;
        } else {
            return messageText;
        }
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

    @Override
    public String toString() {
        return "Message{" +
                "severity=" + severity +
                ", code='" + code + '\'' +
                ", messageText='" + messageText + '\'' +
                ", fields=" + fields +
                '}';
    }

    public Object getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(Object sourceObject) {
        this.sourceObject = sourceObject;
    }
}
