/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit.dao;

import com.concur.mobile.platform.expenseit.ExpenseItPostReceipt;

import java.util.Calendar;
import java.util.List;

@SuppressWarnings("unused")
public interface ExpenseItReceiptDAO {

    long getId();

    Long getReportId();

    String getNote();

    String getCcType();

    Calendar getCreatedAt();

    Calendar getSendToCteAt();

    String getImageDataUrl();

    int getTotalImageCount();

    int getTotalImagesUploaded();

    int getParsingStatusCode();

    String getProcessingEngine();

    int getEta();

    void setId(long id);

    void setReportId(long reportId);

    void setNote(String note);

    void setCcType(String ccType);

    void setCreatedAt(Calendar createdAt);

    void setSendToCteAt(Calendar sendToCteAt);

    void setImageDataUrl(String imageDataUrl);

    void setTotalImageCount(int totalImageCount);

    void setTotalImagesUploaded(int totalImagesUploaded);

    void setParsingStatusCode(int parsingStatusCode);

    void setProcessingEngine(String processingEngine);

    void setEta(int eta);

    /**
     * Will update persistence with the current receipt values.
     */
    boolean update();

    /**
     * Will delete the receipt.
     *
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    boolean delete();

    /**
     * Will clear all entries in the expenseIt receipts store
     * @return
     */
    void deleteAll();

    /**
     * Get all receipts list
     * @return
     */
    List<ExpenseItPostReceipt> getReceipts();
}
