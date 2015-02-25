/**
 * Copyright (c) 2015 Concur Technologies, Inc.
 */
package com.concur.mobile.platform.expense.receipt.ocr;

import java.io.Serializable;
import java.util.List;

import com.concur.mobile.platform.service.parser.ActionStatus;
import com.google.gson.annotations.SerializedName;

/**
 * Object representing the response returned back from the StopOCR endpoint.
 * 
 * @author Chris N. Diaz
 */
public class StopOCR implements Serializable {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = 4039630275482906317L;

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
     * <p>
     * Contains the image origin (e.g.MOB).
     * </p>
     * <p>
     * <ul>
     * <li>MOB - mobile</li>
     * <li>APP - ExpenseIt</li>
     * <li>CTE - Concur T&E</li>
     * <li>EML - email</li>
     * <li>INV - invoice</li>
     * </ul>
     * </p>
     */
    @SerializedName("imageOrigin")
    public String imageOrigin;

    /**
     * <p>
     * Contains the OCR status, one of the following:
     * 
     * The ocrStatus field can contain the following values:
     * </p>
     * <p>
     * <ul>
     * <li>OCR_NOT_COMPANY_ENABLED - OCR is not enabled for the end-users company.</li>
     * <li>OCR_NOT_AVAILABLE - OCR service could not be reached.</li>
     * <li>OCR_STAT_UNKNOWN - OCR does not have any knowledge of the receipt (not OCR Enqueued).</li>
     * <li>A_PEND - pending auto OCR - "auto" OCR pending.</li>
     * <li>A_DONE - completed auto OCR - "auto" OCR completed.</li>
     * <li>A_CNCL - cancelled auto OCR - "auto" OCR cancelled.</li>
     * <li>A_FAIL - failure auto OCR - "auto" OCR failed.</li>
     * <li>M_PEND - pending manual OCR - "manual" OCR pending.</li>
     * <li>M_DONE - completed manual OCR - "manual" OCR completed.</li>
     * <li>M_CNCL - cancelled manual OCR - "manual" OCR cancelled.</li>
     * </ul>
     * </p>
     */
    @SerializedName("ocrStatus")
    public String ocrStatus;

    /**
     * <p>
     * Contains the OCR reject code. The reject code can be one of the following:
     * </p>
     * <p>
     * <ul>
     * <li>PF - processing failure</li>
     * <li>NR - not a receipt</li>
     * <li>UR - unreadable receipt</li>
     * <li>MR - multiple receipt</li>
     * <li>AC - auto OCR cancelled</li>
     * <li>MC - manual OCR cancelled</li>
     * </ul>
     * </p>
     */
    @SerializedName("rejectCode")
    public String rejectCode;

    /**
     * List of errors, if any.
     */
    @SerializedName("errors")
    public List<com.concur.mobile.platform.service.parser.Error> errors;

    /**
     * Default constructor.
     */
    StopOCR() {

    }

}
