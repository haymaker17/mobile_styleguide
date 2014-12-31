/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>ServiceReply</code> for retrieving report details information.
 * 
 * @author AndrewK
 */
public class ReportDetailReply extends ServiceReply {

    /**
     * Contains a reference to the parsed report.
     */
    public ExpenseReportDetail report;

    /**
     * Contains the XML representation of the response body.
     */
    public String xmlReply;

}
