/**
 * 
 */
package com.concur.mobile.core.fragment.navigation;

import com.concur.mobile.core.fragment.navigation.Navigation.NavigationItem;

/**
 * Provides a default implementation of <code>NavigationItem</code>.
 */
public class DefaultNavigationItem implements NavigationItem {

    /**
     * Contains the resource ID of the layout file containing the navigation content.
     */
    protected int layoutResId;
    /**
     * Contains whether this navigation item is selectable.
     */
    protected boolean selectable;
    /**
     * Contains the integer-based ID of the contructed navigation view item.
     */
    protected int id;

    /**
     * Constructs an instance of <code<DefaultNavigationItem</code>
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the resource ID of the layout file containing the navigation content.
     * @param selectable
     *            contains whether this navigation item is selectable.
     */
    public DefaultNavigationItem(int id, int layoutResId, boolean selectable) {
        this.id = id;
        this.layoutResId = layoutResId;
        this.selectable = selectable;
    }

    /**
     * Constructs an instance of <code<DefaultNavigationItem</code> that can be selected.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the resource ID of the layout file containing the navigation content.
     */
    public DefaultNavigationItem(int id, int layoutResId) {
        this(id, layoutResId, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.fragment.navigation.Navigation.NavigationItem#getLayoutResId()
     */
    @Override
    public int getLayoutResId() {
        return layoutResId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.fragment.navigation.Navigation.NavigationItem#isSelectable()
     */
    @Override
    public boolean isSelectable() {
        return selectable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.fragment.navigation.Navigation.NavigationItem#getId()
     */
    @Override
    public int getId() {
        return id;
    }

}
