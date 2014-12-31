/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>ServiceReply</code> for handling the response to a <code>ReportItemizationEntryFormRequest</code>.
 * 
 * @author andy
 */
public class ReportItemizationEntryFormReply extends ServiceReply {

    public ExpenseReportEntryDetail entryDetail;

    // Contains the XML representation of the response body.
    public String xmlReply;

}
