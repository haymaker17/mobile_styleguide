package com.concur.mobile.core.expense.travelallowance.service.parser;

import android.os.Bundle;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

/**
 * Created by D028778 on 25-Sep-15.
 */
public class StatusParser extends BaseParser {

    private static final String BODY_TAG = "Body";
    private static final String STATUS_TAG = "Status";
    private static final String STATUS_TEXT_TAG = "StatusText";
    private static final String STATUS_TEXT_LOCALIZED_TAG = "StatusTextLocalized";

    private static final String failure = "FAILURE";

    private String status;
    private String statusText;
    private String statusTextLocalized;

    private Bundle resultData;

    /**
     * Getter method
     * @return The parsed data using a bundle containing two elements:
     * 1) A boolean with key {@link BundleId#IS_SUCCESS} to indicate a success response status.
     * 2) Optionally a {@link Message} object with key {@link BundleId#MESSAGE_OBJECT} whereas
     * the message objects contains the error description from the backend.
     */
    public Bundle getResultData() {
        return this.resultData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTag(String tag) {
        if (BODY_TAG.equalsIgnoreCase(tag)) {
            status = StringUtilities.EMPTY_STRING;
            statusText = StringUtilities.EMPTY_STRING;
            statusTextLocalized = StringUtilities.EMPTY_STRING;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleText(String tag, String text) {
        if (STATUS_TAG.equalsIgnoreCase(tag)) {
            this.status = text;
        }
        if (STATUS_TEXT_TAG.equalsIgnoreCase(tag)) {
            this.statusText = text;
        }
        if (STATUS_TEXT_LOCALIZED_TAG.equalsIgnoreCase(tag)) {
            this.statusTextLocalized = text;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endTag(String tag) {
        if (BODY_TAG.equalsIgnoreCase(tag)) {
            resultData = new Bundle();
            if (!StringUtilities.isNullOrEmpty(status) && status.equalsIgnoreCase(failure)) {
                Message msg = new Message(Message.Severity.ERROR, statusText, statusTextLocalized);
                resultData.putSerializable(BundleId.MESSAGE_OBJECT, msg);
                resultData.putBoolean(BundleId.IS_SUCCESS, false);
            } else {
                resultData.putBoolean(BundleId.IS_SUCCESS, true);
            }
        }
    }

}
