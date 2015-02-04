package com.concur.mobile.platform.request.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author olivierb
 */
public class RequestDTO implements FormDTO {

    public enum ApprovalStatus {
        CREATION("Q_NOTF"),
        PENDING_VALIDATION("Q_PEND"),
        PENDING_EBOOKING("Q_PEBK"),
        APPROVED("Q_APPR"),
        RECALLED("Q_RESU");

        private String code;

        ApprovalStatus(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static String SUBMIT = "submit";
    public static String SAVE = "save";

    @Expose @SerializedName("RequestID")
    private String id;
    @Expose @SerializedName("Name")
    private String name;
    @Expose @SerializedName("Purpose")
    private String purpose;
    @Expose @SerializedName("CurrencyCode")
    private String currencyCode;
    @SerializedName("EmployeeName")
    private String employeeName;
    @SerializedName("ApprovalStatusName")
    private String approvalStatus;
    @Expose @SerializedName("ApprovalStatusCode")
    private String approvalStatusCode;
    @SerializedName("TotalApprovedAmount")
    private Double total;
    @Expose @SerializedName("StartDate")
    private Date startDate;
    @Expose @SerializedName("EndDate")
    private Date endDate;
    @Expose @SerializedName("CreationDate")
    private Date requestDate;
    @Expose @SerializedName("LastComment")
    private String lastComment;
    @SerializedName("UserLoginID")
    private String userLoginId;
    @SerializedName("ApproverLoginID")
    private String approverLoginId;
    @SerializedName("RequestDetailsUrl")
    private String detailsUrl;
    @SerializedName("SegmentTypes")
    private String segmentListString;
    @Expose @SerializedName("HeaderFormID")
    private String headerFormId;

    // --- required to post/put
    @Expose @SerializedName(("PolicyID"))
    private String policyId;

    private Map<String, RequestEntryDTO> entriesMap;

    private List<String> listPermittedActions;

    @Override
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

    public Map<String, RequestEntryDTO> getEntriesMap() {
        return entriesMap;
    }

    public void setEntriesMap(Map<String, RequestEntryDTO> entriesMap) {
        this.entriesMap = entriesMap;
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
