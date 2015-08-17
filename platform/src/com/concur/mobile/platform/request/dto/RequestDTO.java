package com.concur.mobile.platform.request.dto;

import com.concur.mobile.platform.request.permission.Link;
import com.concur.mobile.platform.request.permission.UserPermission;
import com.concur.mobile.platform.request.util.RequestParser;
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

    @Expose
    @SerializedName("RequestID")
    private String id;
    @Expose
    @SerializedName("Name")
    private String name;
    @Expose
    @SerializedName("Purpose")
    private String purpose;
    @Expose
    @SerializedName("CurrencyCode")
    private String currencyCode;
    @SerializedName("EmployeeName")
    private String employeeName;
    @SerializedName("ApprovalStatusName")
    private String approvalStatus;
    @Expose
    @SerializedName("ApprovalStatusCode")
    private String approvalStatusCode;
    @SerializedName("TotalApprovedAmount")
    private Double total;
    @Expose
    @SerializedName("StartDate")
    private Date startDate;
    @Expose
    @SerializedName("EndDate")
    private Date endDate;
    @Expose
    @SerializedName("CreationDate")
    private Date requestDate;
    @Expose
    @SerializedName("Comment")
    private String lastComment;
    @SerializedName("Comments")
    private List<RequestCommentDTO> commentHistory;
    @SerializedName("LoginID")
    private String userLoginId;
    @SerializedName("HighestExceptionLevel")
    private RequestExceptionDTO.ExceptionLevel highestExceptionLevel;
    @Expose
    @SerializedName("HeaderFormID")
    private String headerFormId;
    @SerializedName("Exceptions")
    private List<RequestExceptionDTO> exceptions;

    // --- required to post/put
    @Expose
    @SerializedName(("PolicyID"))
    private String policyId;

    @SerializedName("SegmentsEntries")
    private List<RequestEntryDTO> entryList;

    private Map<String, RequestEntryDTO> entriesMap;

    @SerializedName("UserPermissions")
    private Link permissionsLink;

    private int displayOrder = 1;

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

    public String getHeaderFormId() {
        return headerFormId;
    }

    public void setHeaderFormId(String headerFormId) {
        this.headerFormId = headerFormId;
    }

    public void setPermissionsLink(Link permissionsLink) {
        this.permissionsLink = permissionsLink;
    }

    public Map<String, RequestEntryDTO> getEntriesMap() {
        return entriesMap;
    }

    public void setEntriesMap(Map<String, RequestEntryDTO> entriesMap) {
        this.entriesMap = entriesMap;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public List<RequestCommentDTO> getCommentHistory() {
        return commentHistory;
    }

    public void setCommentHistory(List<RequestCommentDTO> commentHistory) {
        this.commentHistory = commentHistory;
    }

    public RequestExceptionDTO.ExceptionLevel getHighestExceptionLevel() {
        return highestExceptionLevel;
    }

    public void setHighestExceptionLevel(RequestExceptionDTO.ExceptionLevel highestExceptionLevel) {
        this.highestExceptionLevel = highestExceptionLevel;
    }

    public List<RequestExceptionDTO> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<RequestExceptionDTO> exceptions) {
        this.exceptions = exceptions;
    }

    public boolean isActionPermitted(RequestParser.PermittedAction action) {
        if (this.permissionsLink != null && this.permissionsLink.getPermissions() != null) {
            final List<UserPermission> userPermissions = this.permissionsLink.getPermissions();
            for (UserPermission permission : userPermissions) {
                if (permission.getAction().equals(action.getAction())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
