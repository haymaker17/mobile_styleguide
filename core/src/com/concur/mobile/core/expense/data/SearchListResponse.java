/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.io.Serializable;
import java.util.List;

/**
 * Models a search list response.
 * 
 * @author AndrewK
 */
public class SearchListResponse implements Serializable {

    /**
     * Generated serializable ID.
     */
    private static final long serialVersionUID = 4345005739786039157L;

    /**
     * Contains the original query text.
     */
    public String query;

    /**
     * Contains the search field id.
     */
    public String fieldId;

    /**
     * Contains the search ft code.
     */
    public String ftCode;

    /**
     * Contains the report key.
     */
    public String rptKey;

    /**
     * Contains the list of parsed items.
     */
    public List<ListItem> listItems;

}
