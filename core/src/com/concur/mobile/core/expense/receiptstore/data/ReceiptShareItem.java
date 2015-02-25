/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.data;

import android.location.Location;
import android.net.Uri;

/**
 * @author andy
 */
public class ReceiptShareItem {

    /**
     * Models the receipt share item status.
     */
    public static enum Status {
        /**
         * Pending.
         */
        PENDING("PENDING"),
        /**
         * Success.
         */
        SUCCESS("SUCCESS"),
        /**
         * Fail.
         */
        FAIL("FAIL"),
        /**
         * Hold.
         */
        HOLD("HOLD");

        private String name;

        Status(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>Status</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>ReceiptShareItem.Status</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static Status fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (Status st : Status.values()) {
                    if (st.name.equalsIgnoreCase(name)) {
                        return st;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    }

    public ReceiptShareItem() {
    };

    public ReceiptShareItem(Uri uri, String mimeType, String fileName, String displayName, Status status) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.displayName = displayName;
        this.status = status;
    }

    /**
     * Contains the <code>Uri</code> of the receipt share item.
     */
    public Uri uri;
    /**
     * Contains the mime-type of the receipt share item.
     */
    public String mimeType;
    /**
     * Contains the file name of the file within the receipt share upload directory.
     */
    public String fileName;
    /**
     * Contains the display name for the receipt share item.
     */
    public String displayName;
    /**
     * Contains the <code>Status</code> for the receipt share item.
     */
    public Status status = Status.PENDING;

    /**
     * Contains the location of where this image was taken.
     */
    public Location locationTaken;

}
