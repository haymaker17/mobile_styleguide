package com.concur.mobile.core.travel.service;

/**
 * Value object to be used in sending data across the activity and the AsyncTask
 * 
 * @author RatanK
 * 
 */
public class TripApprovalReqObject {

    protected String tripApproveAction;
    protected String travellerCompanyId;
    protected String travellerUserId;
    protected String tripIdOfTripForApproval;

    public String getTripApproveAction() {
        return tripApproveAction;
    }

    public void setTripApproveAction(String tripApproveAction) {
        this.tripApproveAction = tripApproveAction;
    }

    public String getTravellerCompanyId() {
        return travellerCompanyId;
    }

    public void setTravellerCompanyId(String travellerCompanyId) {
        this.travellerCompanyId = travellerCompanyId;
    }

    public String getTravellerUserId() {
        return travellerUserId;
    }

    public void setTravellerUserId(String travellerUserId) {
        this.travellerUserId = travellerUserId;
    }

    public String getTripIdOfTripForApproval() {
        return tripIdOfTripForApproval;
    }

    public void setTripIdOfTripForApproval(String tripIdOfTripForApproval) {
        this.tripIdOfTripForApproval = tripIdOfTripForApproval;
    }
}
