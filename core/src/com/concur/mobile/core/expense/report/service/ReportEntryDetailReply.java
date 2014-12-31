/**
 * 
 */
package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.service.ServiceReply;

/**
 * An extension of <code>ServiceReply</code> for handling the response to a report entry detail request.
 * 
 * @author andy
 */
public class ReportEntryDetailReply extends ServiceReply {

    /**
     * Contains the parsed entry detail.
     */
    public ExpenseReportEntryDetail expRepEntDet;

    /**
     * Contains the XML representation of the response body.
     */
    public String xmlReply;

}
