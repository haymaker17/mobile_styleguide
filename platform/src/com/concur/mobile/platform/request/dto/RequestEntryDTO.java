package com.concur.mobile.platform.request.dto;

import com.concur.mobile.platform.common.formfield.IFormField;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RequestEntryDTO {

    public static enum TripType implements IFormField.EnumField<IFormField.DataType> {
        ONE_WAY("ONE_WAY"),
        ROUND_TRIP("ROUND_TRIP"),
        MULTI_SEGMENT("MULTI_STOP");

        private String name;

        @Override public String getName() {
            return name;
        }

        TripType(String name) {
            this.name = name;
        }
    }

    @Expose @SerializedName("ID")
    private String id;
    @Expose @SerializedName("Segments")
    private List<RequestSegmentDTO> listSegment = new ArrayList<RequestSegmentDTO>();
    @Expose @SerializedName("ForeignCurrencyCode")
    private String foreignCurrencyCode;
    @Expose @SerializedName("ForeignAmount")
    private Double foreignAmount;
    @SerializedName("Comments")
    private List<RequestCommentDTO> comments;
    @SerializedName("ExpenseTypeName")
    private String expenseTypeName;
    @Expose @SerializedName("TripType")
    private TripType tripType;

    @SerializedName("SegmentFormID")
    private String segmentFormId;
    @SerializedName("SegmentTypeName")
    private String segmentType;
    @SerializedName("SegmentTypeCode")
    private String segmentTypeCode;

    private Integer displayOrder = 0;

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

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

    public String getForeignCurrencyCode() {
        return foreignCurrencyCode;
    }

    public void setForeignCurrencyCode(String foreignCurrencyCode) {
        this.foreignCurrencyCode = foreignCurrencyCode;
    }

    public Double getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(Double foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public List<RequestCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<RequestCommentDTO> comments) {
        this.comments = comments;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
