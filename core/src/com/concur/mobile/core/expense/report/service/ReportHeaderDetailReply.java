/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>ServiceReply</code> for handling the result of a report header detail request.
 * 
 * @author andy
 */
public class ReportHeaderDetailReply extends ServiceReply {

    public static final String CLS_TAG = ReportHeaderDetailReply.class.getSimpleName();

    /**
     * Contains the XML representation of the response body.
     */
    public String xmlReply;

    /**
     * Contains a parsed expense report detail object.
     */
    public ExpenseReportDetail reportDetail;

}
