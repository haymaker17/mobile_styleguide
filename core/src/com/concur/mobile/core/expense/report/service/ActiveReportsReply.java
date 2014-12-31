package com.concur.mobile.core.expense.report.service;

import java.util.ArrayList;

import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.service.ServiceReply;

public class ActiveReportsReply extends ServiceReply {

    public ArrayList<ExpenseReport> reports;

    public String xmlReply;
}
