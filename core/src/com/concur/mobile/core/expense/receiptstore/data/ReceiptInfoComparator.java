/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.data;

import java.util.Comparator;

/**
 * An implementation of <code>Comparator</code> for comparing instances of <code>ReceiptInfo</code> based on receipt upload date.
 */
public class ReceiptInfoComparator implements Comparator<ReceiptInfo> {

    public int compare(ReceiptInfo rcptInfo1, ReceiptInfo rcptInfo2) {
        int retVal = 0;
        if (rcptInfo1 != rcptInfo2) {
            if (rcptInfo1.getImageCalendar() != null && rcptInfo2.getImageCalendar() != null) {
                // 'compareTo' should return either -1, 0 or 1. To reverse the order, we negate
                // the value from 'compareTo'.
                retVal = -rcptInfo1.getImageCalendar().compareTo(rcptInfo2.getImageCalendar());
            } else if (rcptInfo1.getImageCalendar() != null) {
                retVal = -1;
            } else if (rcptInfo1.getImageCalendar() == null) {
                retVal = 1;
            }
        }
        return retVal;
    }

}
