package com.concur.mobile.platform.request.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestSegmentDTO {

    private String segmentType;
    private String foreignCurrencyName;
    private String foreignCurrencyCode;
    private Double foreignAmount;
    private Date departureDate;
    private Date arrivalDate;
    private String fromLocationName;
    private String toLocationName;
    private int exceptionCount;
    private List<String> exeptionList = new ArrayList<String>();

    public String getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(String segmentType) {
        this.segmentType = segmentType;
    }

    public String getForeignCurrencyName() {
        return foreignCurrencyName;
    }

    public void setForeignCurrencyName(String foreignCurrencyName) {
        this.foreignCurrencyName = foreignCurrencyName;
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

    public Double getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(Double foreignAmount) {
        this.foreignAmount = foreignAmount;
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

    public String getForeignCurrencyCode() {
        return foreignCurrencyCode;
    }

    public void setForeignCurrencyCode(String foreignCurrencyCode) {
        this.foreignCurrencyCode = foreignCurrencyCode;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public List<String> getExeptionList() {
        return exeptionList;
    }

    public void setExeptionList(List<String> exeptionList) {
        this.exeptionList = exeptionList;
    }
}
