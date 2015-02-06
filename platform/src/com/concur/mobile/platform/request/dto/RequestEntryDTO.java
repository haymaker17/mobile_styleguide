package com.concur.mobile.platform.request.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestEntryDTO {

    @SerializedName("ID")
    private String id;
    @SerializedName("Segments")
    private List<RequestSegmentDTO> listSegment = new ArrayList<RequestSegmentDTO>();
    @SerializedName("TransactionCurrencyCode")
    private String transactionCurrencyCode;
    @SerializedName("TransactionAmount")
    private Double transactionAmount;
    @SerializedName("Comments")
    private List<RequestCommentDTO> comments;
    @SerializedName("TransactionDate")
    private Date transactionDate;
    @SerializedName("ExpenseTypeName")
    private String expenseTypeName;

    private String segmentFormId;
    private String segmentType = null;
    private String segmentTypeCode = null;

    public String getExpenseTypeName() {
        return expenseTypeName;
    }

    public void setExpenseTypeName(String expenseTypeName) {
        this.expenseTypeName = expenseTypeName;
    }

    public String getSegmentTypeCode() {
        return segmentTypeCode;
    }

    public void setSegmentTypeCode(String segmentTypeCode) {
        this.segmentTypeCode = segmentTypeCode;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getSegmentFormId() {
        return segmentFormId;
    }

    public void setSegmentFormId(String segmentFormId) {
        this.segmentFormId = segmentFormId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<RequestSegmentDTO> getListSegment() {
        return listSegment;
    }

    public void setListSegment(List<RequestSegmentDTO> listSegment) {
        this.listSegment = listSegment;
    }

    public String getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(String segmentType) {
        this.segmentType = segmentType;
    }

    public String getTransactionCurrencyCode() {
        return transactionCurrencyCode;
    }

    public void setTransactionCurrencyCode(String transactionCurrencyCode) {
        this.transactionCurrencyCode = transactionCurrencyCode;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public List<RequestCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<RequestCommentDTO> comments) {
        this.comments = comments;
    }
}
