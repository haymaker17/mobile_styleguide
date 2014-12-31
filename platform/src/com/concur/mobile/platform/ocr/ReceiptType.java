package com.concur.mobile.platform.ocr;

public enum ReceiptType {
    JPEG("JPG"), PDF("PDF");

    // Used to Store the human readable string used to identify the Enum Type.
    private String mToStringValue = null;

    /***
     * Consturctor used to set the value of mToStringValue.
     * 
     * @param newToStringValue
     *            New Custom String Value
     */
    private ReceiptType(String newToStringValue) {
        mToStringValue = newToStringValue;
    }

    @Override
    public String toString() {
        return mToStringValue;
    }
}
