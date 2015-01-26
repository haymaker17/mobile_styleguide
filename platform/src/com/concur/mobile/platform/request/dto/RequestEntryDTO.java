package com.concur.mobile.platform.request.dto;

import java.util.ArrayList;
import java.util.List;

public class RequestEntryDTO {
    private List<RequestSegmentDTO> listSegment = new ArrayList<RequestSegmentDTO>();
    private String segmentType = null;
    private String foreignCurrencyCode;
    private Double foreignAmount;
    private String approvalStatusCode;

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

	public String getApprovalStatusCode() {
		return approvalStatusCode;
	}

	public void setApprovalStatusCode(String approvalStatusCode) {
		this.approvalStatusCode = approvalStatusCode;
	}


}
