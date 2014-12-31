/**
 * 
 */
package com.concur.mobile.core.fragment.navigation;

import com.concur.mobile.core.fragment.navigation.Navigation.SimpleNavigationItem;

/**
 * Provides a default implementation of <code>SimpleNavigationItem</code>
 */
public class DefaultSimpleNavigationItem extends DefaultTextNavigationItem implements SimpleNavigationItem {

    /**
     * Contains the resource id for the icon drawable.
     */
    protected int iconResId;
    /**
     * Contains the visibility for the icon. Should be one of <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
     * <code>View.GONE</code>.
     */
    protected int iconVisibility;

    /**
     * Contains the visibility for the navigation item. Should be one of <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
     * <code>View.GONE</code>.
     */
    protected int navItemVisibility;

    /**
     * Constructs an instance of <code>DefaultSimpleNavigationItem</code> given a layout resource ID, text resource ID, icon
     * resource ID and icon visibility.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the layout resource ID. A value of <code>-1</code> indicates to use the
     *            <code>R.layout.navigation_item</code> layout file.
     * @param textResId
     *            contains the text resource ID of text to be displayed in a <code>TextView</code> with id <code>text</code>.
     * @param iconResId
     *            contains the drawable resource ID to be used as an icon.
     * @param iconVisibility
     *            Contains the visibility for the icon. Should be one of <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
     *            <code>View.GONE</code>.
     * @param selectable
     *            contains whether this navigation item is selectable.
     */
    public DefaultSimpleNavigationItem(int id, int layoutResId, int textResId, int iconResId, int iconVisibility,
            int navItemVisibility, boolean selectable) {
        super(id, layoutResId, textResId, selectable);
        this.iconResId = iconResId;
        this.iconVisibility = iconVisibility;
        this.navItemVisibility = navItemVisibility;
    }

    /**
     * Constructs an instance of <code>DefaultSimpleNavigationItem</code> given a layout resource ID, text resource ID, icon
     * resource ID and icon visibility that is selectable.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the layout resource ID. A value of <code>-1</code> indicates to use the
     *            <code>R.layout.navigation_item</code> layout file.
     * @param textResId
     *            contains the text resource ID of text to be displayed in a <code>TextView</code> with id <code>text</code>.
     * @param iconResId
     *            contains the drawable resource ID to be used as an icon.
     * @param iconVisibility
     *            Contains the visibility for the icon. Should be one of <code>View.VISIBLE</code>, <code>View.INVISIBLE</code> or
     *            <code>View.GONE</code>.
     */
    public DefaultSimpleNavigationItem(int id, int layoutResId, int textResId, int iconResId, int iconVisibility,
            int navItemVisibility) {
        this(id, layoutResId, textResId, iconResId, iconVisibility, navItemVisibility, true);
    }

    @Override
    public int getIconResourceId() {
        return iconResId;
    }

    @Override
    public int getIconVisibility() {
        return iconVisibility;
    }

    @Override
    public int getNavItemVisibility() {
        return navItemVisibility;
    }

}
