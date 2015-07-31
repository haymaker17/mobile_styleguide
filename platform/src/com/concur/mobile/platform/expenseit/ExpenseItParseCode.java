package com.concur.mobile.platform.expenseit;


import com.concur.mobile.platform.R;

/**
 * @author Elliott Jacobsen-Watts
 */
public enum  ExpenseItParseCode {

    // NAME	                    (CODE, ID)
    UNPARSED                    (0, R.string.expenseit_expense_detail_submitted),
    PARSED                      (1, 0),
    MULTIPLE_RECEIPTS           (2, R.string.expenseit_expense_detail_mulitple),  // indicates multiple receipts on same image, which is a Rubicon failure state
    UNREADABLE                  (3, R.string.expenseit_expense_detail_unreadable),  // indicates that Rubicon could not make heads or tails of the image
    EXPIRED                     (4, R.string.expenseit_expense_detail_not_analyzed),  // indicates that we deleted the image after the expiration period
    UPLOADED                    (5, R.string.expenseit_expense_detail_submitted),    // indicates that we uploaded successfully to Rubicon, but haven't heard back yet
    UPLOADED_BUT_NOT_QUEUED     (6, R.string.expenseit_expense_detail_submitted),   // indicates that we uploaded successfully to Rubicon, but they didn't queue it for OCR
    FAILED_UPLOAD_ATTEMPTS      (7, R.string.expenseit_expense_detail_submitted),   // indicates one or more failed upload attempts, but we'll keep trying
    NOT_RECEIPT                 (8, R.string.expenseit_expense_detail_not_receipt),    // indicates that Rubicon believes the image is not of a receipt
    OTHER                       (99, R.string.expenseit_expense_detail_not_analyzed),
    NO_IMAGE_FOUND              (98, R.string.expenseit_expense_detail_not_analyzed),  // indicates that we have a record for a user having uploaded an image to us, but no image data
    ANALYZING_REMOTELY_PENDING  (100, R.string.expenseit_expense_detail_submitted),
    UPLOADING_IN_PROGRESS       (101, R.string.expenseit_expense_detail_uploading),
    QUEUED_FOR_UPLOAD           (102, R.string.expenseit_expense_detail_queued_waiting),
    QUEUED_FOR_EXPORT           (103, R.string.expenseit_expense_detail_export_pending),
    QUEUED_FOR_DELETE           (104, R.string.expenseit_expense_detail_delete_pending),
    QUEUED_FOR_MODIFY           (105, R.string.expenseit_expense_detail_save_pending),
    QUEUED_FOR_CREATION         (106, R.string.expenseit_expense_detail_create_pending),
    SUCCESS_HIDDEN              (107, R.string.expenseit_expense_detail_success),
    INTERVENTION_NEEDED         (108, R.string.expenseit_expense_detail_user_needed),
    PERMANENT_FAILURE           (109, R.string.expenseit_expense_permanent_failure),
    SUCCESS_VISIBLE             (110, R.string.expenseit_expense_detail_success),
    EXPORTED                    (1000, 0),
    QUEUED_FOR_EXPORT_ON_SERVER (1001, R.string.expenseit_expense_detail_export_pending),
    DEFAULT                     (-1, 0);

    private int value;
    private int resId;

    private ExpenseItParseCode(int code, int resId) {
        this.value = code;
        this.resId = resId;
    }

    public int value() {
        return value;
    }
    public int getResId() {
        return resId;
    }

    /**
     * Returns true if the object is processing.
     *
     * @param status
     * @return
     */
    public static boolean isProcessing(int status) {
        return ((status == UNPARSED.value()) ||
                (status == UPLOADED.value()) ||
                (status == UPLOADED_BUT_NOT_QUEUED.value()) ||
                (status == FAILED_UPLOAD_ATTEMPTS.value()) ||
                (status == ANALYZING_REMOTELY_PENDING.value()) ||
                (status == UPLOADING_IN_PROGRESS.value()) ||
                (status == QUEUED_FOR_UPLOAD.value()) ||
                (status == QUEUED_FOR_EXPORT.value()) ||
                (status == QUEUED_FOR_DELETE.value()) ||
                (status == QUEUED_FOR_MODIFY.value()) ||
                (status == QUEUED_FOR_CREATION.value()) ||
                (status == QUEUED_FOR_EXPORT_ON_SERVER.value()) ||
                (status == DEFAULT.value()));
    }

    /**
     * Returns true if the object is in error state.
     *
     * @param status
     * @return
     */
    public static boolean isInErrorState(int status) {
        return ((status == ExpenseItParseCode.MULTIPLE_RECEIPTS.value()) ||
                (status == ExpenseItParseCode.UNREADABLE.value()) ||
                (status == ExpenseItParseCode.EXPIRED.value()) ||
                (status == ExpenseItParseCode.NO_IMAGE_FOUND.value()) ||
                (status == ExpenseItParseCode.NOT_RECEIPT.value()) ||
                (status == ExpenseItParseCode.OTHER.value()) ||
                (status == ExpenseItParseCode.INTERVENTION_NEEDED.value()) ||
                (status == ExpenseItParseCode.PERMANENT_FAILURE.value()) ||
                (status == ExpenseItParseCode.DEFAULT.value()));
    }

    /**
     * Returns true if the object is in an export state - do we need this as a transition condition?
     *
     * @param status
     * @return
     */
    public static boolean isInExportState(int status) {
        return ((status == ExpenseItParseCode.PARSED.value()) ||
                (status == ExpenseItParseCode.SUCCESS_HIDDEN.value()) ||
                (status == ExpenseItParseCode.SUCCESS_VISIBLE.value()) || // XXX: Not sure about this one.
                (status == ExpenseItParseCode.EXPORTED.value()));   // XXX: Not sure about this one.
    }
}
