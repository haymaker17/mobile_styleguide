/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.platform.expense.receipt.ocr;

import java.io.Serializable;
import java.util.List;

import com.concur.mobile.platform.service.parser.ActionStatus;
import com.google.gson.annotations.SerializedName;

/**
 * @author Chris N. Diaz
 *
 */
public class StartOCR implements Serializable {

    /**
     * Serializable ID
     */
    private static final long serialVersionUID = 8359339103272512815L;

    /**
     * Contains the action status object associated with retrieving the receipt list.
     */
    @SerializedName("actionStatus")
    public ActionStatus actionStatus;

    /**
     * Contains the (protected) receipt image ID.
     */
    @SerializedName("receiptImageId")
    public String receiptImageId;

    /**
     * Contains the image origin (e.g.MOB).
     */
    @SerializedName("imageOrigin")
    public String imageOrigin;

    /**
     * Contains the OCR status, one of the following:
     * 
     * The ocrStatus field can contain the following values: OCR_NOT_COMPANY_ENABLED - OCR is not enabled for the end-users
     * company. OCR_NOT_AVAILABLE - OCR service could not be reached. OCR_STAT_UNKNOWN - OCR does not have any knowledge of the
     * receipt (not OCR Enqueued). A_PEND - pending auto OCR - "auto" OCR pending. A_DONE - completed auto OCR - "auto" OCR
     * completed. A_CNCL - cancelled auto OCR - "auto" OCR cancelled. A_FAIL - failure auto OCR - "auto" OCR failed. M_PEND -
     * pending manual OCR - "manual" OCR pending. M_DONE - completed manual OCR - "manual" OCR completed. M_CNCL - cancelled
     * manual OCR - "manual" OCR cancelled.
     */
    @SerializedName("ocrStatus")
    public String ocrStatus;

    /**
     * List of errors, if any.
     */
    @SerializedName("errors")
    public List<com.concur.mobile.platform.service.parser.Error> errors;

    /**
     * Default constructor.
     */
    StartOCR() {

    }

}
