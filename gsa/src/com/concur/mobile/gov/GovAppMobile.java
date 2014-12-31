/**
 * Main Application class.
 * */
package com.concur.mobile.gov;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.GovExpenseForm;
import com.concur.mobile.gov.expense.doc.service.DocumentListReply;
import com.concur.mobile.gov.expense.service.GovSearchListResponse;
import com.concur.mobile.gov.service.GovMessagesReply;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.StampCache;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.gov.util.VoucherCache;

public class GovAppMobile extends ConcurCore {

    // show privacy act notice on home page only when click login using pin/password. Autologin doesnt required to show notice
    private boolean showPrivacyActNotice;
    // show rules once in a 6 months
    // private boolean isAgreeWithRules;

    private boolean isExpListRefreshReq;
    private boolean isDocumentListRefReqAfterCreateVch;

    private DocumentListReply documentListReply;
    private DocumentListReply authForVchDocumentList;

    private DsDocDetailInfo docDetailInfoFromServiceHandler;
    private GovMessagesReply msgs;

    public StampCache stampCache;
    public VoucherCache vchCache;
    public TravelBookingCache trvlBookingCache;

    public ArrayList<ExpenseType> expenseTypes;

    public GovExpenseForm currentExpenseForm;

    public GovSearchListResponse govSearchListResponse;

    /**
     * Good ol' default constructor
     */
    public GovAppMobile() {
        appContext = this;
    }

    @Override
    protected String getServerAddress() {
        // TODO Auto-generated method stub
        return "https://cge.concursolutions.com";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stampCache = new StampCache();
        vchCache = new VoucherCache();
        trvlBookingCache = new TravelBookingCache();

        expenseTypes = new ArrayList<ExpenseType>();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        stampCache = new StampCache();
        vchCache = new VoucherCache();
        // trvlBookingCache = new TravelBookingCache();
        expenseTypes = new ArrayList<ExpenseType>();
        documentListReply = null;
        docDetailInfoFromServiceHandler = null;
        authForVchDocumentList = null;
    }

    public void setProduct(String componentName) {
        product = Product.GOV;
    }

    public String getStringResourcePackageName() {
        return "com.concur.gsa";
    }

    /*
     * 
     */
    @Override
    public String getGATrackingId() {
        return ""; // Not using GA for Gov tracking.
    }    

    @Override
    protected boolean bindProductService() {
        return bindService(new Intent(this, GovService.class), serviceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void expireLogin() {
        // TODO cdiaz: clear cache
        
    }

    @Override
    public GovService getService() {
        return (GovService) concurService;
    }

    public DocumentListReply getDocumentListReply() {
        return documentListReply;
    }

    public void setDocumentListReply(DocumentListReply documentListReply) {
        this.documentListReply = documentListReply;
    }

    public DsDocDetailInfo getDocDetailInfo() {
        return docDetailInfoFromServiceHandler;
    }

    public void setDocDetailInfo(DsDocDetailInfo docDetailInfo) {
        this.docDetailInfoFromServiceHandler = docDetailInfo;
    }

    public GovMessagesReply getMsgs() {
        return msgs;
    }

    public void setMsgs(GovMessagesReply msgs) {
        this.msgs = msgs;
    }

    public boolean isPrivacyActNoticeShow() {
        return showPrivacyActNotice;
    }

    public void setShowPrivacyActNotice(boolean privacyActNotice) {
        this.showPrivacyActNotice = privacyActNotice;
    }

    public void setExpenseTypes(ArrayList<ExpenseType> list) {
        this.expenseTypes = list;
    }

    public ArrayList<ExpenseType> getExpenseTypes() {
        return expenseTypes;
    }

    public void setCurrentExpenseForm(GovExpenseForm form) {
        this.currentExpenseForm = form;
    }

    public GovExpenseForm getCurrentExpenseForm() {
        return currentExpenseForm;
    }

    @Override
    public boolean isTraveler(String roles) {
        boolean canTravel = roles.contains(Const.MOBILE_TRAVELER);
        boolean canGovTravel = roles.contains(com.concur.mobile.gov.util.Const.MOBILE_GOV_TM_TRAVELER);
        if (canTravel || canGovTravel) {
            canTravel = true;
        } else {
            canTravel = false;
        }
        return canTravel;
    }

    public boolean isExpListRefreshReq() {
        return isExpListRefreshReq;
    }

    public void setExpListRefreshReq(boolean isExpListRefreshReq) {
        this.isExpListRefreshReq = isExpListRefreshReq;
    }

    public boolean isDocumentListRefReq() {
        return isDocumentListRefReqAfterCreateVch;
    }

    public void setDocumentListRefReq(boolean isDocumentListRefReq) {
        this.isDocumentListRefReqAfterCreateVch = isDocumentListRefReq;
    }

    public GovSearchListResponse getGovSearchListResponse() {
        return govSearchListResponse;
    }

    public void setGovSearchListResponse(GovSearchListResponse response) {
        this.govSearchListResponse = response;
    }

    public DocumentListReply getAuthForVchDocumentList() {
        return authForVchDocumentList;
    }

    public void setAuthForVchDocumentList(DocumentListReply authForVchDocumentList) {
        this.authForVchDocumentList = authForVchDocumentList;
    }
}
