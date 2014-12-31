package com.concur.mobile.platform.common;

import java.io.Serializable;

import android.content.Context;

/**
 * Models an item that can be used with a spinner widget.
 * 
 * Ported from com.concur.mobile.core.view.SpinnerItem.
 * 
 * - Added constructor with String
 * 
 * - Updated constructor with resource id to accept the context.
 * 
 * @author WaltA
 */
public class SpinnerItem implements Serializable {

    private static final long serialVersionUID = -4026709869662471808L;

    public String id;
    public String name;

    /**
     * 
     * @param id
     * @param nameResId
     */
    public SpinnerItem(String id, int nameResId, Context context) {
        this(id, context.getText(nameResId));
    }

    public SpinnerItem(String id, CharSequence name) {
        this(id, name.toString());
    }

    public SpinnerItem(String id, String name) {
        this.id = id;
        this.name = name;
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
