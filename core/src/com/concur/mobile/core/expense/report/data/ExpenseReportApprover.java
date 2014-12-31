/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.report.data;

import java.io.Serializable;

/**
 * POJO for an Expense Report Approver.
 * 
 * @author Chris N. Diaz
 * 
 */
public class ExpenseReportApprover implements Serializable {

    private static final long serialVersionUID = 4686562126667250183L;

    public String approverRptKey;

    public String email;

    public String empKey;

    public String externalUserName;

    public String firstName;

    public String lastName;

    public String loginId;

    /**
     * Default no-arg constructor.
     */
    public ExpenseReportApprover() {
        // empty constructor.
    }

    /**
     * Returns a clone of this <code>ExpenseReportApprover</code>.
     */
    public ExpenseReportApprover clone() {
        ExpenseReportApprover clone = new ExpenseReportApprover();
        clone.approverRptKey = this.approverRptKey;
        clone.email = this.email;
        clone.empKey = this.empKey;
        clone.externalUserName = this.externalUserName;
        clone.firstName = this.firstName;
        clone.lastName = this.lastName;
        clone.loginId = this.loginId;

        return clone;
    }

    /**
     * Resets all the field values to <code>null</code>
     */
    public void reset() {
        approverRptKey = null;
        email = null;
        empKey = null;
        externalUserName = null;
        firstName = null;
        lastName = null;
        loginId = null;
    }
}
