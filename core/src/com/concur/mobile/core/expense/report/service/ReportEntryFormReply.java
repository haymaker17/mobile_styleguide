package com.concur.mobile.core.expense.report.service;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.service.ServiceReply;

public class ReportEntryFormReply extends ServiceReply {

    public ExpenseReportEntryDetail entryDetail;

    // Contains the XML representation of the response body.
    public String xmlReply;

}
