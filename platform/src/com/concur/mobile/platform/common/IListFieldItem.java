package com.concur.mobile.platform.common;

/**
 * Interface extracted from {@link core.expense.data.ListItem}
 * 
 * @author yiwenw
 * 
 */
public interface IListFieldItem {

    // TODO - can we rid of this?
    public static final String DEFAULT_KEY_LOCATION = "LocName";

    /**
     * @return List Item Key
     */
    public String getKey();

    public String getCode();

    public String getText();

    /**
     * Convenience method for getting the <code>ListItemField</code> value with the given <code>id</code>.
     * 
     * @param id
     *            the ID of the <code>ListItemField</code> whose value to get.
     * @return the <code>ListItemField</code> value with the given <code>id</code>.
     */
    public String getFieldValueById(String id);

}
