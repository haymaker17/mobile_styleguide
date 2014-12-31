package com.concur.mobile.platform.ui.common.view;

import java.util.Calendar;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * An abstract class that models a list item to be used with an adapter.
 */
public abstract class ListItem {

    /**
     * Contains the list item view type for this list item.
     */
    protected int listItemViewType;

    /**
     * Contains a reference to an object that can be used to tag this item.
     */
    protected Object listItemTag;

    /**
     * Gets the list item view type.
     * 
     * <b>NOTE:</b><br>
     * This implementation uses the hash code of the class of this object.
     * 
     * @return an integer representing the list item view type.
     */
    public int getListItemViewType() {
        return listItemViewType;
    }

    /**
     * Gets whether the list item is enabled.
     * 
     * @return whether the list item is enabled.
     */
    public abstract boolean isEnabled();

    /**
     * Constructs a list item view.
     * 
     * @param context
     *            an activity context.
     * @param convertView
     *            a view that may be converted based on the view type.
     * @param parent
     *            the view group.
     */
    public abstract View buildView(Context context, View convertView, ViewGroup parent);

    /**
     * Return a calendar representing the date/time for this item
     */
    public Calendar getCalendar() {
        return null;
    }
}