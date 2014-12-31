package com.concur.mobile.core.fragment.navigation;

import com.concur.mobile.core.fragment.navigation.Navigation.TextNavigationItem;

/**
 * Provides a default implementation of <code>TextNavigationItem</code>.
 */
public class DefaultTextNavigationItem extends DefaultNavigationItem implements TextNavigationItem {

    /**
     * Contains the the resource ID of the text to be displayed.
     */
    protected int textResId;

    /**
     * Constructs an instance of <code>DefaultTextNavigationItem</code> with a layout resource ID and a text resource id.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the layout resource ID. A value of <code>-1</code> indicates to use the
     *            <code>R.layout.navigation_item</code> layout file.
     * @param textResId
     *            contains the text resource ID of text to be displayed in a <code>TextView</code> with id <code>text</code>.
     * @param selectable
     *            contains whether this navigation item is selectable.
     */
    public DefaultTextNavigationItem(int id, int layoutResId, int textResId, boolean selectable) {
        super(id, layoutResId, selectable);
        this.textResId = textResId;
    }

    /**
     * Constructs an instance of <code>DefaultTextNavigationItem</code> with a layout resource ID and a text resource id that is
     * selectable.
     * 
     * @param id
     *            contains the integer-based ID for this navigation item that will be set on the constructed view.
     * @param layoutResId
     *            contains the layout resource ID. A value of <code>-1</code> indicates to use the
     *            <code>R.layout.navigation_item</code> layout file.
     * @param textResId
     *            contains the text resource ID of text to be displayed in a <code>TextView</code> with id <code>text</code>.
     */
    public DefaultTextNavigationItem(int id, int layoutResId, int textResId) {
        this(id, layoutResId, textResId, true);
    }

    @Override
    public int getTextResId() {
        return textResId;
    }

}
