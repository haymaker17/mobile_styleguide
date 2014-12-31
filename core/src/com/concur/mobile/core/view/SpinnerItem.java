package com.concur.mobile.core.view;

import java.io.Serializable;

import com.concur.mobile.core.ConcurCore;

/**
 * Models an item that can be used with a spinner widget.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.SpinnerItem} instead.
 * @author WaltA
 */
public class SpinnerItem implements Serializable {

    private static final long serialVersionUID = -5389823144931126742L;

    public String id;
    public String name;

    /**
     * 
     * @param id
     * @param nameResId
     */
    public SpinnerItem(String id, int nameResId) {
        this(id, ConcurCore.getContext().getText(nameResId));
    }

    public SpinnerItem(String id, CharSequence name) {
        this.id = id;
        this.name = name.toString();
    }

    @Override
    public String toString() {
        return name;
    }

    public static SpinnerItem findById(SpinnerItem[] items, String id) {

        if (id == null)
            return null;

        SpinnerItem item = null;

        for (int i = 0; i < items.length; i++) {
            if (items[i].id.equals(id)) {
                item = items[i];
                break;
            }
        }

        return item;
    }
}
