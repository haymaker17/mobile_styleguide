package com.concur.mobile.platform.request.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

/**
 * @author olivierb
 */
public class RequestDTO {
	
	public static String SUBMIT = "submit";
    public static String SAVE = "save";

    public static String SUBMIT = "submit";

    @SerializedName("RequestID")
    private String id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Purpose")
    private String purpose;
    @SerializedName("CurrencyCode")
    private String currencyCode;
    @SerializedName("EmployeeName")
    private String employeeName;
    @SerializedName("ApprovalStatusName")
    private String approvalStatus;
    @SerializedName("ApprovalStatusCode")
    private String approvalStatusCode;
    @SerializedName("TotalApprovedAmount")
    private Double total;
    @SerializedName("StartDate")
    private Date startDate;
    @SerializedName("EndDate")
    private Date endDate;
    @SerializedName("CreationDate")
    private Date requestDate;
    @SerializedName("LastComment")
    private String lastComment;
    @SerializedName("UserLoginID")
    private String userLoginId;
    @SerializedName("ApproverLoginID")
    private String approverLoginId;
    @SerializedName("RequestDetailsUrl")
    private String detailsUrl;
    @SerializedName("SegmentTypes")
    private String segmentListString;
    @SerializedName("HeaderFormID")
    private String headerFormId;

    // --- required to post/put
    @SerializedName(("PolicyID"))
    private String policyId;

    //@SerializedName("Entries")
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public boolean isActionPermitted(String action) {
        for (String permittedAction : this.listPermittedActions) {
            if (permittedAction.equals(action)) {
                return true;
            }
        }
        return false;
    }

}
