/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.core.expense.data.ExpenseType;

/**
 * An extension of <code>ExpenseType</code> providing an expense type category.
 * 
 * @author AndrewK
 */
public class ExpenseTypeCategory extends ExpenseType {

    /**
     * Contains the resource id of the category drawable.
     */
    private int drawableResId;

    /**
     * Contains the list of expense types associated with this category.
     */
    private ArrayList<ExpenseType> expenseTypes = new ArrayList<ExpenseType>();

    /**
     * Constructs an instance of <code>ExpenseTypeCategory</code> with a name and drawable resource id.
     * 
     * @param name
     *            the category name.
     * @param drawableResId
     *            the drawable resource id.
     */
    public ExpenseTypeCategory(String name, int drawableResId) {
        super(name);
        this.drawableResId = drawableResId;
    }

    /**
     * Constructs an instance of <code>ExpenseTypeCategory</code> with a name, key and drawable resource id.
     * 
     * @param name
     *            the category name.
     * @param key
     *            the category key.
     * @param drawableResId
     *            the drawable resource id.
     */
    public ExpenseTypeCategory(String name, String key, int drawableResId) {
        super(name, key);
        this.drawableResId = drawableResId;
    }

    public void addExpenseType(ExpenseType expenseType) {
        expenseTypes.add(expenseType);
        // Set the parent property.
        expenseType.setParentExpenseType(this);
    }

    public List<ExpenseType> getExpenseTypes() {
        return expenseTypes;
    }

    public int getDrawableResourceId() {
        return drawableResId;
    }

    public boolean isDrawableResourceIdSet() {
        return (drawableResId != 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getName().substring(0, 1);
    }

}
