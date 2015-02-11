package com.concur.mobile.platform.request.dto;

import com.concur.mobile.platform.request.ConnectExceptionMessage;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestSegmentDTO implements FormDTO {

    @Expose @SerializedName("SegmentID")
    private String id;
    @SerializedName("SegmentType")
    private String segmentType;
    @SerializedName("SegmentTypeCode")
    private String segmentTypeCode;
    @Expose @SerializedName("DepartureDate")
    private Date departureDate;
    @Expose @SerializedName("ArrivalDate")
    private Date arrivalDate;
    @Expose @SerializedName("FromLocationID")
    private String fromLocationId;
    @Expose @SerializedName("ToLocationID")
    private String toLocationId;
    @SerializedName("FromLocationName")
    private String fromLocationName;
    @SerializedName("ToLocationName")
    private String toLocationName;
    //@SerializedName()
    private int exceptionCount;
    @SerializedName("SegmentFormID")
    private String segmentFormId;
    @SerializedName("ForeignAmount")
    private Double foreignAmount;
    @SerializedName("ForeignCurrencyCode")
    private String foreignCurrencyCode;
    @SerializedName("Exceptions")
    private List<ConnectExceptionMessage> exeptionList = new ArrayList<ConnectExceptionMessage>();
    @SerializedName("Comments")
    private List<RequestCommentDTO> commentList = null;
    @Expose @SerializedName("Comment")
    private String lastComment;
    // --- useless but required by ws
    @Expose @SerializedName("ArrivalTime")
    private String arrivalTime;
    @Expose @SerializedName("DepartureTime")
    private String departureTime;

    private Integer displayOrder;

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getFromLocationId() {
        return fromLocationId;
    }

    public void setFromLocationId(String fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public String getToLocationId() {
        return toLocationId;
    }

    public void setToLocationId(String toLocationId) {
        this.toLocationId = toLocationId;
    }

    @Override
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public List<RequestCommentDTO> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<RequestCommentDTO> commentList) {
        this.commentList = commentList;
    }

    @Override
    public String getId() {
        return id;
    }

    public Double getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(Double foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public String getForeignCurrencyCode() {
        return foreignCurrencyCode;
    }

    public void setForeignCurrencyCode(String foreignCurrencyCode) {
        this.foreignCurrencyCode = foreignCurrencyCode;
    }

    public String getSegmentTypeCode() {
        return segmentTypeCode;
    }

    public String getSegmentFormId() {
        return segmentFormId;
    }

    public void setSegmentFormId(String segmentFormId) {
        this.segmentFormId = segmentFormId;
    }

    public String getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(String segmentType) {
        this.segmentType = segmentType;
    }

    public String getFromLocationName() {
        return fromLocationName;
    }

    public void setFromLocationName(String fromLocationName) {
        this.fromLocationName = fromLocationName;
    }

    public String getToLocationName() {
        return toLocationName;
    }

    public void setToLocationName(String toLocationName) {
        this.toLocationName = toLocationName;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public List<ConnectExceptionMessage> getExeptionList() {
        return exeptionList;
    }

    public void setExeptionList(List<ConnectExceptionMessage> exeptionList) {
        this.exeptionList = exeptionList;
    }
}
