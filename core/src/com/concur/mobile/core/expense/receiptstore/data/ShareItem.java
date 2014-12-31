/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.data;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * An extension of <code>ReceiptShareItem</code> to hold thumbnail data, etc.
 */
public class ShareItem extends ReceiptShareItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum UIStatus {
        PENDING, PREPARE, UPLOAD, FINISH_FAILED, FINISH_RETRY, FINISH_COMPLETE
    }

    /**
     * Contains a thumbnail image of the selected item.
     */
    transient public Bitmap thumbnail;

    /**
     * Contains whether or not the item is currently selected.
     */
    public boolean selected;

    /**
     * Contains the status of the share item.
     */
    public UIStatus uiStatus = UIStatus.PENDING;

    /**
     * Contains the reason text after the last upload attempt.
     */
    public String reason;

    /**
     * Contains the progress if the share item is being uploaded.
     */
    public int progress;

}
