/**
 * @author sunill
 */
package com.concur.mobile.gov.util;

import com.concur.mobile.gov.expense.doc.stamp.data.ReasonCodeReqdResponse;
import com.concur.mobile.gov.expense.doc.stamp.service.DsStampReply;
import com.concur.mobile.gov.expense.doc.stamp.service.StampTMDocumentResponse;

public class StampCache {

    private DsStampReply stampReply;
    private ReasonCodeReqdResponse reqResReply;
    private StampTMDocumentResponse stampTMDocumentResponse;

    public StampCache() {
    }

    public DsStampReply getStampReply() {
        return stampReply;
    }

    public void setStampReply(DsStampReply stampReply) {
        this.stampReply = stampReply;
    }

    public void setStampReqRes(ReasonCodeReqdResponse reqResReply) {
        this.reqResReply = reqResReply;
    }

    public ReasonCodeReqdResponse getStampReqRes() {
        return reqResReply;
    }

    public StampTMDocumentResponse getStampTMDocumentResponse() {
        return stampTMDocumentResponse;
    }

    public void setStampTMDocumentResponse(StampTMDocumentResponse stampTMDocumentResponse) {
        this.stampTMDocumentResponse = stampTMDocumentResponse;
    }
}
