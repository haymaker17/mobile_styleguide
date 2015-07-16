/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit.dao;

import android.content.Context;
import android.net.Uri;

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
     *
     * @param context
     * @param userId
     */
    boolean update(Context context, String userId);

    /**
     * Will delete the receipt.
     *
     * @param context
     * @param userId
     *
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    boolean delete(Context context, String userId);

    /**
     *
     * @param context
     * @param userId
     * @return
     */
    Uri getContentUri(Context context, String userId);

    /**
     * Get all receipts list
     * @return
     */
    List<ExpenseItPostReceipt> getReceipts();
}
