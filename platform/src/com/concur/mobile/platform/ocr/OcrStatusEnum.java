package com.concur.mobile.platform.ocr;

public enum OcrStatusEnum {
    OCR_NOT_COMPANY_ENABLED("OCR_NOT_COMPANY_ENABLED"), // OCR is not enabled.
    OCR_NOT_AVAILABLE("OCR_NOT_AVAILABLE"), // OCR is not available.
    OCR_STAT_UNKNOWN("OCR_STAT_UNKNOWN"), // OCR has no knowledge of this receipt
    A_PEND("A_PEND"), // Pending automated processing
    A_FAIL("A_FAIL"), // Automated processing failed
    A_DONE("A_DONE"), // Automated processing completed
    A_CNCL("A_CNCL"), // Automated processing canceled
    M_PEND("M_PEND"), // Pending manual processing
    M_FAIL("M_FAIL"), // Manual processing failed
    M_DONE("M_DONE"), // Manual processing completed
    M_CNCL("M_CNCL"); // Manual processing canceled

    // Used to Store the human readable string used to identify the Enum Type.
    private String mToStringValue = null;

    /***
     * Constructor used to set the value of mToStringValue.
     * 
     * @param newToStringValue
     *            New Custom String Value
     */
    private OcrStatusEnum(String newToStringValue) {
        mToStringValue = newToStringValue;
    }

    @Override
    public String toString() {
        return mToStringValue;
    }

    /**
     * Helper APIs to check ocr failed status
     * 
     * @return true if is failed; false, otherwise including null status
     */
    public static boolean isFailed(OcrStatusEnum status) {
        return status != null && (A_FAIL == status || M_FAIL == status);
    }

    /**
     * Helper APIs to check ocr completed status
     * 
     * @return true if is completed; false, otherwise including null status
     */
    public static boolean isCompleted(OcrStatusEnum status) {
        return status != null && (A_DONE == status || M_DONE == status);
    }

    /**
     * Helper APIs to check ocr cancelled status
     * 
     * @return true if is cancelled; false, otherwise including null status
     */
    public static boolean isCancelled(OcrStatusEnum status) {
        return status != null && (A_CNCL == status || M_CNCL == status);
    }

    /**
     * Helper APIs to check ocr in processing status
     * 
     * @return true if is in processing; false, otherwise including null status
     */
    public static boolean isProcessing(OcrStatusEnum status) {
        return status != null && (A_PEND == status || M_PEND == status);
    }
}
