/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.io.Serializable;

/**
 * Models a list item field.
 * 
 * @author AndrewK
 */
public class ListItemField implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6419768527313545127L;

    /**
     * Contains the list item field id.
     */
    public String id;

    /**
     * Contains the list item field value.
     */
    public String value;

    public ListItemField() {
        // TODO Auto-generated constructor stub
    }

    public ListItemField(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
