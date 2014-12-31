/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import com.concur.mobile.core.expense.data.ListItem;

/**
 * An extension of <code>ListItem</code> used to represent a currency type category, i.e., "Most recently used", "Other", etc.
 * 
 * @author sunill
 */
public class CategoryListItem extends ListItem {

    /**
     * 
     */
    private static final long serialVersionUID = -5595663315714797052L;

    /**
     * Constructs an instance of <code>CategoryListItem</code> with a category name.
     * 
     * @param name
     *            the currency type category name.
     */
    public CategoryListItem(String name) {
        super(name);
    }

}
