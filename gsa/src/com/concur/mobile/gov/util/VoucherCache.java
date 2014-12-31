package com.concur.mobile.gov.util;

import java.util.List;

import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.gov.expense.charge.service.AddToVchReply;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListReply;

public class VoucherCache {

    // TODO this is temporary, it will be change eventually. we need to store data into database.
    public MobileExpenseListReply mobileExpenseListReply;
    public AddToVchReply addToVchReply;

    public MobileExpenseListReply getMobileExpenseListReply() {
        return mobileExpenseListReply;
    }

    public void setMobileExpenseListReply(MobileExpenseListReply mobileExpenseListReply) {
        this.mobileExpenseListReply = mobileExpenseListReply;
    }

    public void setAddToVchReply(AddToVchReply addToVchReply) {
        this.addToVchReply = addToVchReply;
    }

    public AddToVchReply getAddToVchReply() {
        return this.addToVchReply;
    }

    /**
     * Deletes the given <code>MobileExpense</code> from the
     * cached <code>MobileExpenseListReply</code>.
     * 
     * 
     * @param exp
     *            the <code>MobileExpense</code> to delete.
     * @return <code>true</code> if the given <code>MobileExpense</code> was deleted, otherwise <code>false</code> is returned.
     */
    public boolean deleteUnappliedExpense(MobileExpense exp) {
        if (mobileExpenseListReply != null && exp != null) {
            List<MobileExpense> expenses = mobileExpenseListReply.mobExpList;
            if (expenses != null) {
                return expenses.remove(exp);
            }
        }

        return false;
    }

    /**
     * Deletes the <code>MobileExpense</code> with the specified <code>ccExpId</code> from the cached
     * <code>MObileExpenseListReply</code>.
     * 
     * @param ccExpId
     *            the ID of the MobileExpense to delete
     * @return <code>true</code> if the <code>MobileExpense</code> with
     *         the specified <code>ccExpId</codde> was deleted, otherwise 
     *  <code>false</code> is returned.
     */
    public boolean deleteUnappliedExpense(String ccExpId) {

        if (mobileExpenseListReply != null
            && mobileExpenseListReply.mobExpList != null
            && ccExpId != null) {

            List<MobileExpense> mobExpList = mobileExpenseListReply.mobExpList;
            MobileExpense expToDelete = null;
            // Go through and find the Expense we just delete.
            for (MobileExpense exp : mobExpList) {
                if (exp.ccexpid.equals(ccExpId)) {
                    expToDelete = exp;
                    break;
                }
            }
            if (expToDelete != null) {
                return mobExpList.remove(expToDelete);
            }
        }

        return false;
    }
}
