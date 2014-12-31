package com.concur.mobile.core.util;

import java.util.Calendar;
import java.util.Comparator;

import android.util.Log;

import com.concur.mobile.core.expense.data.ExpenseType;

public class ComparatorUtil implements Comparator<ExpenseType> {

    private static final String CLS_TAG = ComparatorUtil.class.getSimpleName();

    public enum Operation {
        DATESORT, INTEGERSORT;
    }

    private Operation isSortBy;

    public ComparatorUtil(Operation isSortBy) {
        this.isSortBy = isSortBy;
    }

    public int compare(ExpenseType expType1, ExpenseType expType2) {
        int returnVal = 0;
        switch (isSortBy) {
        case DATESORT:
            try {
                Calendar date1 = expType1.getLastUsed();
                Calendar date2 = (expType2).getLastUsed();
                if (date1.before(date2))
                    returnVal = -1;

                if (date1.after(date2))
                    returnVal = 1;
            } catch (NullPointerException e) {
                returnVal = 0;
                Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.sorExpenseList NullPointerException");
            }
            break;

        case INTEGERSORT:
            returnVal = (expType1.getuseCount() < expType2.getuseCount() ? -1 : (expType1.getuseCount() == expType2
                    .getuseCount() ? 0 : 1));
            break;
        }
        return returnVal;
    }

}