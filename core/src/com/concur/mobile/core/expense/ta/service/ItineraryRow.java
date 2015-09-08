package com.concur.mobile.core.expense.ta.service;

import java.util.Date;

public class ItineraryRow {

    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_SUCCESS = "SUCCESS";
    String irKey;
    Date arrivalDateTime;
    String arrivalLocation;
    String arrivalLnKey;
    String arrivalRlKey;
    Date departDateTime;
    String departLocation;
    String departLnKey;
    String arrivalRateLocation;
    Date borderCrossDateTime;
    boolean isRowLocked;
    boolean isArrivalRateLocationEditable;
    String status;
    String statusText;

    public String getIrKey() {
        return irKey;
    }

    public void setIrKey(String irKey) {
        this.irKey = irKey;
    }

    public Date getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(Date arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public String getArrivalLocation() {
        return arrivalLocation;
    }

    public void setArrivalLocation(String arrivalLocation) {
        this.arrivalLocation = arrivalLocation;
    }

    public String getArrivalLnKey() {
        return arrivalLnKey;
    }

    public void setArrivalLnKey(String arrivalLnKey) {
        this.arrivalLnKey = arrivalLnKey;
    }

    public Date getDepartDateTime() {
        return departDateTime;
    }

    public void setDepartDateTime(Date departDateTime) {
        this.departDateTime = departDateTime;
    }

    public String getDepartLocation() {
        return departLocation;
    }

    public void setDepartLocation(String departLocation) {
        this.departLocation = departLocation;
    }

    public String getDepartLnKey() {
        return departLnKey;
    }

    public void setDepartLnKey(String departLnKey) {
        this.departLnKey = departLnKey;
    }

    public String getArrivalRateLocation() {
        return arrivalRateLocation;
    }

    public void setArrivalRateLocation(String arrivalRateLocation) {
        this.arrivalRateLocation = arrivalRateLocation;
    }

    public Date getBorderCrossDateTime() {
        return borderCrossDateTime;
    }

    public void setBorderCrossDateTime(Date borderCrossDateTime) {
        this.borderCrossDateTime = borderCrossDateTime;
    }

    public boolean isRowLocked() {
        return isRowLocked;
    }

    public void setIsRowLocked(boolean isRowLocked) {
        this.isRowLocked = isRowLocked;
    }

    public boolean isArrivalRateLocationEditable() {
        return isArrivalRateLocationEditable;
    }

    public void setIsArrivalRateLocationEditable(boolean isArrivalRateLocationEditable) {
        this.isArrivalRateLocationEditable = isArrivalRateLocationEditable;
    }

    public String getArrivalRlKey() {
        return arrivalRlKey;
    }

    public void setArrivalRlKey(String arrivalRlKey) {
        this.arrivalRlKey = arrivalRlKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String toString() {
        return getDepartLocation() + " - " + getArrivalLocation();
    }
}
