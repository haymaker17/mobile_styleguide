/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("unused")
public class ExpenseItPostReceipt implements Serializable {
    public static final Integer ERROR_CODE_NO_ERROR = -1;
    public static final Integer DEFAULT_ERROR_CODE = 99;
    public static final int CONCUR_INVALID_ACCESS = 990;                // user's concur OAuth token is invalid
    public static final int CONCUR_OAUTH_EXPIRED_OR_REVOKED = 991;      // user's concur OAuth token is revoked or expired
    public static final int RUBICON_ERROR = 999;                        // permanent failure or unknown error
    public static final int CONCUR_TIMEOUT = 994;                       // timeout calling Concur/Rubicon API
    public static final int RUBICON_LONGER_THAN_EXPECTED = 996;         // fake message, OCR upload longer than expected
    public static final int INVALID_LOCATION_ERROR = 199;

    //eta constants
    public static final int NOT_COMPUTED_ETA = -1;
    public static final int ALREADY_PROCESSED_EXPENSE_ETA = 0;

    @SerializedName("id")
    private Long id;

    @SerializedName("reportId")
    private Long reportId;

    @SerializedName("note")
    private String note;

    @SerializedName("ccType")
    private String ccType;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("sentToCteAt")
    private Date sendToCteAt;

    @SerializedName("imageData")
    private Bitmap imageData;

    @SerializedName("totalImageCount")
    private int totalImageCount;

    @SerializedName("totalImagesUploaded")
    private int totalImagesUploaded;

    @SerializedName("parsingStatusCode")
    private int parsingStatusCode;

    @SerializedName("processingEngine")
    private String processingEngine;

    @SerializedName("eta")
    private int eta;

    @SerializedName("error")
    private ErrorResponse expenseError = new ErrorResponse();

    //non serializable
    private transient String userId;

    private transient long contentId;

    public void setExpenseError(ErrorResponse expenseError) {
        this.expenseError = expenseError;
    }

    public long getContentId() {
        return contentId;
    }

    public void setContentId(long id) {
        this.contentId = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCcType() {
        return ccType;
    }

    public void setCcType(String ccType) {
        this.ccType = ccType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getSendToCteAt() {
        return sendToCteAt;
    }

    public void setSendToCteAt(Date sendToCteAt) {
        this.sendToCteAt = sendToCteAt;
    }

    public Bitmap getImageData() {
        return imageData;
    }

    public void setImageData(Bitmap imageData) {
        this.imageData = imageData;
    }

    public int getTotalImageCount() {
        return totalImageCount;
    }

    public void setTotalImageCount(int totalImageCount) {
        this.totalImageCount = totalImageCount;
    }

    public int getTotalImagesUploaded() {
        return totalImagesUploaded;
    }

    public void setTotalImagesUploaded(int totalImagesUploaded) {
        this.totalImagesUploaded = totalImagesUploaded;
    }

    public void incrementTotalImagesUploaded(int amount) {
        this.totalImagesUploaded += amount;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public String getExpenseErrorMessage() {
        return expenseError.getErrorMessage();
    }

    public void setExpenseErrorMessage(String message) {
        this.expenseError.setErrorMessage(message);
    }

    public Integer getExpenseErrorCode() {
        return expenseError.getErrorCode();
    }

    public void setExpenseErrorCode(Integer code) {
        this.expenseError.setErrorCode(code);
    }

    public int getParsingStatusCode() {
        return parsingStatusCode;
    }

    public void setParsingStatusCode(int parsingStatusCode) {
        this.parsingStatusCode = parsingStatusCode;
    }

    public String getProcessingEngine() {
        return processingEngine;
    }

    public void setProcessingEngine(String processingEngine) {
        this.processingEngine = processingEngine;
    }

    public ErrorResponse getExpenseError() {
        return expenseError;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseItPostReceipt)) return false;

        ExpenseItPostReceipt that = (ExpenseItPostReceipt) o;

        return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
