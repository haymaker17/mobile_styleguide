package com.concur.mobile.gov.util;

import java.util.Calendar;
import java.util.Comparator;

import com.concur.mobile.gov.expense.charge.data.MobileExpense;

/**
 * Compare Mobile Expense dates and sort the list.
 * 
 * @author sunill
 * */

public class GovDateSortComparatorUtil implements Comparator<MobileExpense> {

    public int compare(MobileExpense exp1, MobileExpense exp2) {
        int retVal = 0;

        if (exp1 == null && exp2 != null) {
            return -1;
        } else if (exp1 != null && exp2 == null) {
            return 1;
        } else if (exp1 != null && exp2 != null) {
            Calendar expDate1 = exp1.tranDate;
            Calendar expDate2 = exp2.tranDate;
            if (expDate1 == null && expDate2 != null) {
                retVal = -1;
            } else if (expDate2 == null && expDate1 != null) {
                retVal = 1;
            } else if (expDate1 != null && expDate2 != null) {
                retVal = expDate1.compareTo(expDate2);
            }
        }
        return retVal;
    }

}
