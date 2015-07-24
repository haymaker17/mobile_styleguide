package com.concur.mobile.platform.ui.common.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of <code>BaseAdapter</code>.
 */
public class ListItemAdapter<T extends ListItem> extends BaseAdapter {

    // Contains the item list.
    private List<T> listItems;

    // Contains a context.
    private Context context;

    // An explicitly set count of view types
    // This is needed for those cases where the number of view types in use is dynamic and can be less than the maximum types
    // If not explicitly set then this will be -1
    private int maxViewTypes;

    // A calculated number of view types in the current list
    // If not yet calculated then this will be 0
    private int viewTypeCount;

    /**
     * Constructs an instance of <code>ListItemAdapter</code> given a context and a list of items.
     *
     * @param context   the context.
     * @param listItems the item list.
     */
    public ListItemAdapter(Context context, List<T> listItems) {
        this(context, listItems, -1);
    }

    /**
     * Constructs an instance of <code>ListItemAdapter</code> given a context and a list of items. This version sets the maximum
     * view types to the highest possible view type count even if fewer than that will be in the list. This is to handle a problem
     * where the underlying implementation uses the view type integer as an index into an array with maxViewTypes elements.
     *
     * @param context      the context.
     * @param listItems    the item list.
     * @param maxViewTypes the maximum number of view types that are defined for this list regardless of whether they will actually be in
     *                     the list this time
     */
    public ListItemAdapter(Context context, List<T> listItems, int maxViewTypes) {
        this.listItems = listItems;
        this.context = context;
        this.maxViewTypes = maxViewTypes;
    }

    /**
     * Sets the list of items.
     *
     * @param listItems the list of items.
     */
    public void setItems(List<T> listItems) {
        this.listItems = listItems;
        viewTypeCount = 0;
        notifyDataSetChanged();
    }

    /**
     * Gets the list of items.
     *
     * @return the list of items.
     */
    public List<T> getItems() {
        return listItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        int count = 0;
        if (listItems != null) {
            count = listItems.size();
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    public T getItem(int position) {
        T retVal = null;
        if (position >= 0 && listItems != null && position < listItems.size()) {
            retVal = listItems.get(position);
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        int typeCount = maxViewTypes;

        // If not explicitly set then use the calculated value
        if (typeCount == -1) {
            typeCount = viewTypeCount;
        }

        // If not already calculated then count the types
        if (typeCount == 0) {
            if (listItems != null) {
                List<Integer> viewTypes = new ArrayList<Integer>();
                for (T listItem : listItems) {
                    Integer viewType = listItem.getListItemViewType();
                    if (!viewTypes.contains(viewType)) {
                        viewTypes.add(viewType);
                    }
                }
                typeCount = viewTypes.size();
            }
        }

        if (typeCount == 0) {
            typeCount = 1;
        }

        viewTypeCount = typeCount;

        return typeCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getItemViewType(int)
     */
    @Override
    public int getItemViewType(int position) {
        int itemViewType = 0;
        T listItem = getItem(position);
        if (listItem != null) {
            itemViewType = listItem.getListItemViewType();
        } else {
            itemViewType = super.getItemViewType(position);
        }
        return itemViewType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#isEnabled(int)
     */
    @Override
    public boolean isEnabled(int position) {
        boolean enabled = false;
        if (position >= 0 && listItems != null && position < listItems.size()) {
            enabled = listItems.get(position).isEnabled();
        }
        return enabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        T listItem = getItem(position);
        if (listItem != null) {
            view = listItem.buildView(context, convertView, parent);
        }
        return view;
    }

    /**
     * Will refresh each visible view contained in a list view that match on <code>ListItem.listItemTag</code>.
     *
     * @param listView    contains the list view instance.
     * @param listItemTag contains the tag identifying the views that should be refreshed.
     */
    public void refreshView(ListView listView, Object listItemTag) {
        if (listView != null) {
            int start = listView.getFirstVisiblePosition();
            for (int i = start, j = listView.getLastVisiblePosition(); i <= j; i++) {
                T listItem = getItem(i);
                // NOTE: Need to check for 'listItem' not being null as the last visible position within the list
                // could be a list footer, which accounts for a visible position, but not reflecting any data
                // within the adapter.
                if (listItem != null && listItem.listItemTag != null && listItem.listItemTag.equals(listItemTag)) {
                    View view = listView.getChildAt(i - start);
                    getView(i, view, listView);
                }
            }
        }
    }

}