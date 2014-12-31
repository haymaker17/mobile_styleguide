package com.concur.mobile.core.fragment.navigation;

import android.view.View;

import com.concur.mobile.core.fragment.navigation.Navigation.CustomNavigationItem;

/**
 * Provides a default implementation of <code>CustomNavigationItem</code>
 */
public class DefaultCustomNavigationItem extends DefaultNavigationItem implements CustomNavigationItem {

    /**
     * Contains the custom view.
     */
    protected View view;

    /**
     * Constructs an instance of <code>DefaultCustomNavigationItem</code> given a layout resource ID and a view.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the layout resource ID. A value of <code>-1</code> indicates to use the
     *            <code>R.layout.navigation_item</code> layout file.
     * @param view
     *            contains a custom view object.
     * @param selectable
     *            contains whether this navigation item is selectable.
     */
    public DefaultCustomNavigationItem(int id, int layoutResId, View view, boolean selectable) {
        super(id, layoutResId, selectable);
        this.view = view;
    }

    /**
     * Constructs an instance of <code>DefaultCustomNavigationItem</code> given a layout resource ID and a view that is
     * selectable.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the layout resource ID. A value of <code>-1</code> indicates to use the
     *            <code>R.layout.navigation_item</code> layout file.
     * @param view
     *            contains a custom view object.
     */
    public DefaultCustomNavigationItem(int id, int layoutResId, View view) {
        this(id, layoutResId, view, true);
    }

    /**
     * Constructs an instance of <code>DefaultCustomNavigationItem</code> given a view.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param view
     *            contains a custom view object.
     */
    public DefaultCustomNavigationItem(int id, View view) {
        this(id, -1, view);
    }

    @Override
    public View getView() {
        return view;
    }

}
