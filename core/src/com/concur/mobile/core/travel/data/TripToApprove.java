package com.concur.mobile.core.travel.data;

import java.util.Calendar;

/**
 * Object to be used in Trip approval functionality
 * 
 * @author RatanK
 * 
 */
public class TripToApprove {

    private Calendar bookedDate;
    private Calendar approveByDate;
    private String recordLocator;
    private String itinLocator;
    private Calendar startDate;
    private String travelerName;
    private String tripId;
    private String tripName;
    private Double totalTripCost;
    private String totalTripCostCrnCode;
    private String travelerCompanyId;
    private String travelerUserId;

    public Calendar getBookedDate() {
        return bookedDate;
    }

    public void setBookedDate(Calendar bookedDate) {
        this.bookedDate = bookedDate;
    }

    public Calendar getApproveByDate() {
        return approveByDate;
    }

    public void setApproveByDate(Calendar approveByDate) {
        this.approveByDate = approveByDate;
    }

    public String getRecordLocator() {
        return recordLocator;
    }

    public void setRecordLocator(String recordLocator) {
        this.recordLocator = recordLocator;
    }

    public String getItinLocator() {
        return itinLocator;
    }

    public void setItinLocator(String itinLocator) {
        this.itinLocator = itinLocator;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public String getTravelerName() {
        return travelerName;
    }

    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public Double getTotalTripCost() {
        return totalTripCost;
    }

    public void setTotalTripCost(Double totalTripCost) {
        this.totalTripCost = totalTripCost;
    }

    public String getTotalTripCostCrnCode() {
        return totalTripCostCrnCode;
    }

    public void setTotalTripCostCrnCode(String totalTripCostCrnCode) {
        this.totalTripCostCrnCode = totalTripCostCrnCode;
    }

    public String getTravelerCompanyId() {
        return travelerCompanyId;
    }

    public void setTravelerCompanyId(String travelerCompanyId) {
        this.travelerCompanyId = travelerCompanyId;
    }

    public String getTravelerUserId() {
        return travelerUserId;
    }

    public void setTravelerUserId(String travelerUserId) {
        this.travelerUserId = travelerUserId;
    }

}
