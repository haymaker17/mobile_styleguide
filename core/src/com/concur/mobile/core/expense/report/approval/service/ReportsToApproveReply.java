/**
 * 
 */
package com.concur.mobile.core.expense.report.approval.service;

import java.util.List;

import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>ServiceReply</code> containing report approval information.
 * 
 * @author AndrewK
 */
public class ReportsToApproveReply extends ServiceReply {

    /**
     * Contains the parsed report list.
     */
    public List<ExpenseReport> reports;

    /**
     * Contains the XML representation of the response body.
     */
    public String xmlReply;

}
