package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

import java.util.List;

public class GetConditionalFieldActionReply extends ServiceReply {

    public static final String CLS_TAG = GetConditionalFieldActionReply.class.getSimpleName();

    /**
     * Contains the list of dynamic action values.
     */
    public List<ConditionalFieldAction> conditionalFieldActionList;

    /**
     * Parses the list of dynamic actions contained in <code>responseXml</code>.
     *
     * @param responseXml the XML encoded list of expense types.
     * @return an instance of <code> GetConditionalFieldActionReply </code> containing the parsed response.
     */
    public static GetConditionalFieldActionReply parseReply(String responseXml) {
        GetConditionalFieldActionReply srvReply = new GetConditionalFieldActionReply();
        srvReply.conditionalFieldActionList = ConditionalFieldAction.parseConditionalFieldActionXml(responseXml);
        srvReply.mwsStatus = Const.STATUS_SUCCESS;
        return srvReply;
    }
}
