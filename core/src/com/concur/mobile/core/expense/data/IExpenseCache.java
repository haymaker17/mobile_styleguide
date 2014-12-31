/**
 * 
 */
package com.concur.mobile.core.expense.data;

import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;

/**
 * An interface describing access to expense-related information.
 * 
 * @author AndrewK
 */
public interface IExpenseCache {

    /**
     * Gets a reference to the expense report approval cache.
     * 
     * @return a reference to the expense approval cache.
     */
    public IExpenseReportCache getApprovalCache();

    /**
     * Gets a reference to the expense active report cache.
     * 
     * @return a reference to the expense active report cache.
     */
    public IExpenseReportCache getActiveCache();

    /**
     * Gets a reference to the expense entry cache.
     * 
     * @return a reference to the expense entry cache.
     */
    public IExpenseEntryCache getEntryCache();

    /**
     * Gets a reference to the expense receipt store cache.
     * 
     * @return a reference to the expense receipt store cache.
     */
    public ReceiptStoreCache getReceiptStoreCache();

}
