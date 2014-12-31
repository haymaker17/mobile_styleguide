/**
 * 
 */
package com.concur.mobile.core.expense.data;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.IExpenseReportInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.report.data.ExpenseReportCache;

/**
 * An implementation of <code>IExpenseCache</code>.
 * 
 * @author AndrewK
 */
public class ExpenseCache implements IExpenseCache {

    /**
     * A reference to an in-memory cache of expense reports for approval.
     */
    private IExpenseReportCache expenseApprovalCache;

    /**
     * A reference to an in-memory cache of active expense reports.
     */
    private IExpenseReportCache expenseActiveCache;

    /**
     * A reference to a cache containing expense entry information.
     */
    private IExpenseEntryCache expenseEntryCache;

    /**
     * A reference to the receipt store cache.
     */
    private ReceiptStoreCache receiptStoreCache;

    /**
     * A reference to the application.
     */
    private ConcurCore concurMobile;

    /**
     * Constructs an instance of <code>ExpenseCache</code> with the application.
     * 
     * @param concurMobile
     *            the application.
     */
    public ExpenseCache(ConcurCore concurMobile) {
        this.concurMobile = concurMobile;
        expenseApprovalCache = new ExpenseReportCache(IExpenseReportInfo.ReportType.APPROVAL, this.concurMobile);
        expenseActiveCache = new ExpenseReportCache(IExpenseReportInfo.ReportType.ACTIVE, this.concurMobile);
        expenseEntryCache = new ExpenseEntryCache(this.concurMobile);
        receiptStoreCache = new ReceiptStoreCache(this.concurMobile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseCache#getApprovalCache()
     */
    public IExpenseReportCache getApprovalCache() {
        return expenseApprovalCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseCache#getActiveCache()
     */
    public IExpenseReportCache getActiveCache() {
        return expenseActiveCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseCache#getEntryCache()
     */
    public IExpenseEntryCache getEntryCache() {
        return expenseEntryCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.IExpenseCache#getReceiptStoreCache()
     */
    public ReceiptStoreCache getReceiptStoreCache() {
        return receiptStoreCache;
    }

}
