package com.concur.mobile.gov.util;

import com.concur.mobile.core.util.Flurry;

public class GovFlurry extends Flurry {

    public GovFlurry() {
        // TODO Auto-generated constructor stub
    }

    /* Categories */

    public static final String CATEGORY_AUTH_NUMBER_LIST = "Auth Number List";
    public static final String CATEGORY_DOCUMENT_LIST = "Document List";
    public static final String CATEGORY_DOCUMENT_DETAIL = "Document Detail";
    public static final String CATEGORY_UNAPP_EXPLIST = "Unapplied Expense";
    public static final String CATEGORY_STAMP_DOCUMENT = "Stamp Document";
    public static final String CATEGORY_EXPENSE = "Expense";

    /* Flurry event names. */
    public static final String EVENT_CREATE_AUTH = "Create new auth";
    public static final String EVENT_VIEW_AUTH = "View authorization list";
    public static final String EVENT_VIEW_VCH = "View voucher list";
    public static final String EVENT_VIEW_STAMP = "View stamp document list";
    public static final String EVENT_VIEW_DOCUMENT_DETAIL = "View Document Detail";
    public static final String EVENT_STAMP_DOCUMENT = "Stamp selected document";
    public static final String EVENT_ATTACH_EXP_TO_DOCUMENT = "Attached selected expense(s) to document";
    public static final String EVENT_EXPENSE = "Create Expense";
    public static final String EVENT_NAME_ATTACH = "Attach";
    public static final String EVENT_NAME_UPDATE = "Update";
    public static final String EVENT_CREATE_VCH_FROM_AUTH = "Create new voucher from Auth";
   
    //param name
    public static final String PARAM_NAME_AUTH_COUNT = "Authorization list count";
    public static final String PARAM_NAME_VCH_COUNT = "Voucher list count";
    public static final String PARAM_NAME_STAMP_COUNT = "Stamp list count";
    public static final String PARAM_NAME_AUDIT_PASS_COUNT = "Document Audit Pass Count";
    public static final String PARAM_NAME_AUDIT_FAIL_COUNT = "Document Audit Fail Count";
    public static final String PARAM_NAME_DOCUMENT_TYPE = "Document Type";
    public static final String PARAM_NAME_UNAPP_EXPENSE_COUNT = "Unapplied Expense Count";
    public static final String PARAM_NAME_STAMP_DOCUMENT_WITH_PIN = "Stamp document with signing pin";
    public static final String PARAM_NAME_ATTACHED_FROM = "Attached from";
    public static final String PARAM_NAME_UPDATE_FROM = "Update from";
    
    /* param Values */
    public static final String PARAM_VALUE_VIEW_AUTH_LIST = "Authorization List";
    public static final String PARAM_VALUE_VIEW_VCH_LIST = "Voucher List";
    public static final String PARAM_VALUE_VIEW_STAMP_LIST = "Stamp Document List";
    public static final String PARAM_VALUE_CREATE_NEW_AUTH = "Create new auth";
    public static final String PARAM_VALUE_OPEN_GROPUP_AUTH = "Open or group auth";
    public static final String PARAM_VALUE_EXISTING_AUTH = "Existing auth";
    public static final String PARAM_VALUE_LOCATION_SEARCH = "Location Search";
    public static final String PARAM_VALUE_PER_DIEM_LOC_SEARCH = "PerDiem Location Search";
    public static final String PARAM_VALUE_VIEW_PER_DIEM_LOCATION = "View Per-diem location";
    public static final String PARAM_VALUE_VIEW_EXPENSES = "View Expenses";
    public static final String PARAM_VALUE_VIEW_ACC_ALLOCATION = "View Accounting Allocation";
    public static final String PARAM_VALUE_VIEW_TOTAL_AND_TRAVELS = "View Total and Travel Advances";
    public static final String PARAM_VALUE_VIEW_AUDITS = "View Audits";
    public static final String PARAM_VALUE_VIEW_COMMENTS = "View Comments";
    public static final String PARAM_VALUE_DOC_DETAIL = "Document Detail";
    
}
