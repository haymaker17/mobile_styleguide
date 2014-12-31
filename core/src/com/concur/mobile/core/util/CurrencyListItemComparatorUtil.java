package com.concur.mobile.core.util;

import java.util.Calendar;
import java.util.Comparator;

import android.util.Log;

import com.concur.mobile.core.expense.data.ListItem;

public class CurrencyListItemComparatorUtil implements Comparator<ListItem> {

    private static final String CLS_TAG = CurrencyListItemComparatorUtil.class.getSimpleName();

    public enum Operation {
        DATESORT, INTEGERSORT, ALPHABATICALLY;
    }

    private Operation isSortBy;

    public CurrencyListItemComparatorUtil(Operation datesort) {
        this.isSortBy = datesort;
    }

    public int compare(ListItem type1, ListItem type2) {
        int returnVal = 0;
        switch (isSortBy) {
        case DATESORT:
            try {
                Calendar date1 = type1.getLastUsed();
                Calendar date2 = (type2).getLastUsed();
                if (date1.before(date2))
                    returnVal = -1;

                if (date1.after(date2))
                    returnVal = 1;
            } catch (NullPointerException e) {
                returnVal = 0;
                Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseEntryCache.sorCurrencyTypeList NullPointerException");
            }
            break;

        case INTEGERSORT:
            returnVal = (type1.getLastUseCount() < type2.getLastUseCount() ? -1 : (type1.getLastUseCount() == type2
                    .getLastUseCount() ? 0 : 1));
            break;
        default:
            break;
        }

        return returnVal;
    }

}