/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.core.expense.charge.data;

import java.util.Calendar;

import android.text.TextUtils;

import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.ocr.OcrStatusEnum;

/**
 * POJO used to represent an OCR item in the Expense List Row.
 * 
 * @author Chris N. Diaz
 *
 */
public class OCRItem {

    public static final String CLS_TAG = OCRItem.class.getName();

    /**
     * Reference to the DAO object containing the OCR info.
     * 
     */
    private ReceiptDAO receipt;

    /**
     * Constructor that creates an OCRItem based on the DAO object
     * 
     * @param receipt
     *            to the DAO object containing the OCR info.
     */
    public OCRItem(ReceiptDAO receipt) {
        this.receipt = receipt;
    }

    /**
     * 
     * @return the OCR status (e.g. "A_PEND", "A_FAIL", "M_DONE", etc.).
     */
    public OcrStatusEnum getOcrStatus() {

        if (!TextUtils.isEmpty(receipt.getOcrStatus())) {
            return OcrStatusEnum.valueOf(receipt.getOcrStatus());
        } else
            return null;
    }

    /**
     * 
     * @return the <code>Calendar</code> date the receipt image was uploaded for OCR.
     */
    public Calendar getUploadDate() {
        return receipt.getReceiptUploadTime();
    }

    /**
     * @return the receipt image id
     */
    public String getReceiptImageId() {
        return receipt.getId();
    }

}
