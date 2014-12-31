package com.concur.mobile.platform.request.dto;

import java.util.Date;
import java.util.List;

/**
 * @author olivierb
 */
public class RequestDTO {
	
	public static String SUBMIT = "submit";	
	
    private String id;
    private String name;
    private String purpose;
    private String currency;
    private String employeeName;
    private String approvalStatus;
    private String approvalStatusCode;
    private Double total;
    private Date startDate;
    private Date endDate;
    private Date requestDate;
    private String lastComment;
    private String userLoginId;
    private String approverLoginId;
    private String detailsUrl;
    private String segmentListString;
    private String headerFormId;
    private List<RequestEntryDTO> entriesList;
    private List<String> listPermittedActions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalStatusCode() {
        return approvalStatusCode;
    }

    public void setApprovalStatusCode(String approvalStatusCode) {
        this.approvalStatusCode = approvalStatusCode;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public String getApproverLoginId() {
        return approverLoginId;
    }

    public void setApproverLoginId(String approverLoginId) {
        this.approverLoginId = approverLoginId;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getSegmentListString() {
        return segmentListString;
    }

    public void setSegmentListString(String segmentList) {
        this.segmentListString = segmentList;
    }

    public String getHeaderFormId() {
        return headerFormId;
    }

    public void setHeaderFormId(String headerFormId) {
        this.headerFormId = headerFormId;
    }

    public List<RequestEntryDTO> getEntriesList() {
        return entriesList;
    }

    public void setEntriesList(List<RequestEntryDTO> entriesList) {
        this.entriesList = entriesList;
    }

	public List<String> getListPermittedActions() {
		return listPermittedActions;
	}

	public void setListPermittedActions(List<String> listPermittedActions) {
		this.listPermittedActions = listPermittedActions;
	}
	
	public boolean isActionPermitted(String action){
		for(String permittedAction : this.listPermittedActions)
			if(permittedAction.equals(action))
				return true;
		return false;
	}
	
}
