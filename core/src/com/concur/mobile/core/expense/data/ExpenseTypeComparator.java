/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.util.Comparator;

/**
 * An implementation of <code>Comparator<T></code> used to compare expense types by name.
 * 
 * @author AndrewK
 */
public class ExpenseTypeComparator implements Comparator<ExpenseType> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(ExpenseType expType1, ExpenseType expType2) {
        return expType1.getName().compareTo(expType2.getName());
    }

}
