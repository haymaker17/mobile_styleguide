/**
 * Class contains all the constant value for current project.
 * 
 * @author sunill
 * 
 * */
package com.concur.mobile.gov.util;

import java.util.Locale;

public class Const {

    private Const() {
        // non constructed class
    }

    /* LOCALE and CODE for currency */
    public static final Locale GOV_LOCALE = Locale.US;
    public static final String GOV_CURR_CODE = "USD";

    /* prevented values */
    public static final int preventedValueDialogs = 8000;
    public static final int preventedValuesForMsg = 10000;

    // Gov Traveller
    public static final String MOBILE_GOV_TM_TRAVELER = "TravelManagerTraveler";

    /* Dialog integer values */
    public static final int DIALOG_PRIVACY_ACT_NOTICE = preventedValueDialogs + 1;
    public static final int DIALOG_SEARCHING_DOCUMENT = preventedValueDialogs + 2;
    public static final int DIALOG_DETAIL_DOCUMENT = preventedValueDialogs + 3;
    public static final int DIALOG_STAMP_LIST = preventedValueDialogs + 4;
    public static final int DIALOG_STAMP_REQ_INFO = preventedValueDialogs + 5;
    public static final int DIALOG_STAMP_DOC_STAMPLIST = preventedValueDialogs + 6;
    public static final int DIALOG_STAMP_DOC_REASONLIST = preventedValueDialogs + 7;
    public static final int DIALOG_STAMP_DOC_RETURN_TO_LIST = preventedValueDialogs + 8;
    public static final int DIALOG_GOV_COMMENTS = preventedValueDialogs + 9;
    public static final int DIALOG_GOV_SIGNING_PIN = preventedValueDialogs + 10;
    public static final int DIALOG_STAMP_DOC = preventedValueDialogs + 11;
    public static final int DIALOG_STAMP_DOC_FAILURE = preventedValueDialogs + 12;
    public static final int DIALOG_GOV_UNAPP_EXP_LIST = preventedValueDialogs + 13;
    public static final int DIALOG_CREATE_VOUCHER_FROM_AUTH = preventedValueDialogs + 14;
    public static final int DIALOG_CREATE_VOUCHER_FAIL = preventedValueDialogs + 15;
    public static final int DIALOG_ADD_EXP_TO_VCH = preventedValueDialogs + 16;
    public static final int DIALOG_ADD_EXP_TO_VCH_FAIL = preventedValueDialogs + 17;
    public static final int DIALOG_RETRIEVE_RECEIPT = preventedValueDialogs + 18;
    public static final int DIALOG_RETRIEVE_RECEIPT_UNAVAILABLE = preventedValueDialogs + 19;
    public static final int DIALOG_GOV_MSGS_INPROGRESS = preventedValueDialogs + 20;
    public static final int DIALOG_GOV_MSGS_FAIL = preventedValueDialogs + 21;
    public static final int DIALOG_GENERATE_AUTH = preventedValueDialogs + 22;
    public static final int DIALOG_GENERATE_AUTH_FAIL = preventedValueDialogs + 23;
    public static final int DIALOG_GET_PERDIEM_LOCATION = preventedValueDialogs + 24;
    public static final int DIALOG_GET_PERDIEM_LOCATION_FAIL = preventedValueDialogs + 25;
    public static final int DIALOG_GET_PERDIEM_RATE_LOCATION = preventedValueDialogs + 26;
    public static final int DIALOG_GET_PERDIEM_RATE_LOCATION_FAIL = preventedValueDialogs + 27;
    public static final int DIALOG_GET_AUTH_NUM = preventedValueDialogs + 28;
    public static final int DIALOG_GET_AUTH_NUMS_FAIL = preventedValueDialogs + 29;
    public static final int DIALOG_SEND_AGREEMENT = preventedValueDialogs + 30;
    public static final int DIALOG_SEND_AGREEMENT_FAIL = preventedValueDialogs + 31;
    public static final int DIALOG_SEND_AGREEMENT_SUCCESS = preventedValueDialogs + 32;
    public static final int DIALOG_GET_TM_EXPENSE_TYPES = preventedValueDialogs + 33;
    public static final int DIALOG_GET_TM_EXPENSE_FORM = preventedValueDialogs + 34;
    public static final int DIALOG_SAVE_TM_EXPENSE_FORM = preventedValueDialogs + 35;
    public static final int DIALOG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL = preventedValueDialogs + 36;
    public static final int DIALOG_STAMP_LIST_FAIL = preventedValueDialogs + 37;
    public static final int DIALOG_UPDATING_EXPENSE_LIST = preventedValueDialogs + 38;
    public static final int DIALOG_DETAIL_DOCUMENT_FAIL = preventedValueDialogs + 39;

    /* Filter action values */
    public static final String ACTION_GET_DOCUMENT = "com.concur.mobile.gov.action.GET_DOC";
    public static final String ACTION_GET_DOCUMENT_DETAIL = "com.concur.mobile.gov.action.GET_DOC_DETAIL";
    public static final String ACTION_GET_LIST_OF_STAMP = "com.concur.mobile.gov.action.GET_LIST_OF_STAMP";
    public static final String ACTION_GET_STAMP_REQ_INFO = "com.concur.mobile.gov.action.GET_STAMP_REQ_INFO";
    public static final String ACTION_STAMP_DOC = "com.concur.mobile.gov.action.STAMP_DOC";
    public static final String ACTION_UNAPP_EXPENSE = "com.concur.mobile.gov.action.UNAPP_EXPENSE";
    public static final String ACTION_VOUCHER_CREATE_FROM_AUTH = "com.concur.mobile.gov.action.VOUCHER_CREATE_FROM_AUTH";
    public static final String ACTION_ADD_TO_VCH_EXP = "com.concur.mobile.gov.action.ADD_TO_VCH_EXP";
    public static final String ACTION_GOV_MSGS = "com.concur.mobile.gov.action.GOV_MSGS";
    public static final String ACTION_ATTACH_TM_RECEIPT = "com.concur.mobile.gov.action.ATTACH_TM_RECEIPT";
    public static final String ACTION_GET_AUTH_NUMS = "com.concur.mobile.gov.action.GET_AUTH_NUMS";
    public static final String ACTION_GET_PERDIEM_LOCATIONS = "com.concur.mobile.gov.action.GET_PERDIEM_LOCATION";
    public static final String ACTION_GET_PERDIEM_RATE_LOCATIONS = "com.concur.mobile.gov.action.GET_PERDIEM_RATE_LOCATION";
    public static final String ACTION_GENERATE_AUTH_NUMS = "com.concur.mobile.gov.action.GENERATE_AUTH_NUM";
    public static final String ACTION_SEND_AGREEMENT = "com.concur.mobile.gov.action.SEND_AGREEMENT";
    public static final String ACTION_GET_TM_EXPENSE_TYPES = "com.concur.mobile.gov.action.GET_TM_EXPENSE_TYPES";
    public static final String ACTION_GET_TM_EXPENSE_FORM = "com.concur.mobile.gov.action.GET_TM_EXPENSE_FORM";
    public static final String ACTION_SAVE_TM_EXPENSE_FORM = "com.concur.mobile.gov.action.SAVE_TM_EXPENSE_FORM";
    public static final String ACTION_DOC_EXPENSE_DELETED = "com.concur.mobile.gov.action.DELETE_DOC_EXPENSE";
    public static final String ACTION_UNAPPLIED_EXPENSE_DELETED = "com.concur.mobile.gov.action.DELETE_UNAPPLIED_EXPENSE";
    public static final String ACTION_GET_AUTH_FOR_VCH_DOCUMENT = "com.concur.mobile.gov.action.GET_AUTH_FOR_VCH_DOC";

    // Extras
    public static final String EXTRA_SINGLE_SELECT = "select.single";
    public static final String EXTRA_GOV_TRAVELER_ID = "gov.traveler.id";
    public static final String EXTRA_GOV_AUTH_NAME = "gov.auth.name";
    public static final String EXTRA_GOV_AUTH_TYPE = "gov.auth.type";
    public static final String EXTRA_GOV_PER_DIEM_LOC_ID = "gov.per.diem.loc.id";
    public static final String EXTRA_GOV_EXISTING_TA_NUMBER = "gov.existing.ta.number";
    public static final String EXTRA_GOV_AIR_AUTHORIZATION_NUM = "gov.air.authorization.number";
    public static final String EXTRA_GOV_AIR_TRIP_LOCATOR = "gov.air.trip.locator";
    public static final String EXTRA_GOV_QE_ID = "gov.quick.expense.expId";
    public static final String EXTRA_GOV_QE_DATE = "gov.quick.expense.date";
    public static final String EXTRA_GOV_QE_DESC = "gov.quick.expense.description";

    /* service handler values */
    public static final int HANDLER_MSG_GET_DOCUMENT = preventedValuesForMsg + 1;
    public static final int HANDLER_MSG_GET_DOCUMENT_DETAIL = preventedValuesForMsg + 2;
    public static final int HANDLER_MSG_GET_LIST_OF_STAMP = preventedValuesForMsg + 3;
    public static final int HANDLER_MSG_GET_STAMP_REQ_RESPONSE = preventedValuesForMsg + 4;
    public static final int HANDLER_MSG_STAMP_DOC_RESPONSE = preventedValuesForMsg + 5;
    public static final int HANDLER_MSG_GET_UNAPP_EXP_LIST = preventedValuesForMsg + 6;
    public static final int HANDLER_MSG_CREATE_VOUCHER_FROM_AUTH = preventedValuesForMsg + 7;
    public static final int HANDLER_MSG_ADD_EXP_TO_VCH = preventedValuesForMsg + 8;
    public static final int HANDLER_MSG_GOV_MSGS = preventedValuesForMsg + 9;
    public static final int HANDLER_MSG_ATTACH_TM_RECEIPT = preventedValuesForMsg + 10;
    public static final int HANDLER_MSG_GET_PERDIEM_LOCATION = preventedValuesForMsg + 11;
    public static final int HANDLER_MSG_GET_PERDIEM_RATE_LOCATION = preventedValuesForMsg + 12;
    public static final int HANDLER_MSG_GENERATE_AUTH = preventedValuesForMsg + 13;
    public static final int HANDLER_MSG_GET_AUTH_NUMS = preventedValuesForMsg + 14;
    public static final int HANDLER_MSG_SEND_AGREEMENT = preventedValuesForMsg + 15;
    public static final int HANDLER_MSG_GET_TM_EXPENSE_TYPES = preventedValuesForMsg + 16;
    public static final int HANDLER_MSG_GET_TM_EXPENSE_FORM = preventedValuesForMsg + 17;
    public static final int HANDLER_MSG_SAVE_TM_EXPENSE_FORM = preventedValuesForMsg + 18;
    public static final int HANDLER_MSG_GET_DOC_INFO_FROM_TRIPLOCATOR_FAIL = preventedValuesForMsg + 19;
    public static final int HANDLER_MSG_DELETE_TM_EXPENSE = preventedValuesForMsg + 20;
    public static final int HANDLER_MSG_DELETE_TM_UNAPPLIED_EXPENSE = preventedValuesForMsg + 21;
    public static final int HANDLER_MSG_GET_AUTH_FOR_VCH_DOCUMENT = preventedValuesForMsg + 22;

    /* retainer map values */
    public static final String RETAINER_DOCUMENT_RECIEVER_KEY = "retainer.documentlist.documentreciever.key";
    public static final String RETAINER_DOCUMENT_DETAIL_RECIEVER_KEY = "retainer.documentlist.document.detailreciever.key";
    public static final String RETAINER_DOCUMENT_AUTH_LIST_KEY = "retainer.documentlist.authlist.key";
    public static final String RETAINER_DOCUMENT_VCH_LIST_KEY = "retainer.documentlist.vchlist.key";
    public static final String RETAINER_DOCUMENT_STAMP_LIST_KEY = "retainer.documentlist.stamplist.key";
    public static final String RETAINER_DOCUMENT_DETAIL_DBTHREAD_KEY = "retainer.documentlist.document.database.thread.key";
    public static final String RETAINER_DOCUMENT_DETAIL_KEY = "retainer.document.detail.key";
    public static final String RETAINER_STAMP_LIST_RECEIVER_KEY = "retainer.stamp.document.list.receiver.key";
    public static final String RETAINER_STAMP_REQ_RESPONSE_RECEIVER_KEY = "retainer.stamp.document.required.receiver.info.key";
    public static final String RETAINER_STAMP_DOCUMENT_RECEIVER_KEY = "retainer.stamp.document.key";
    public static final String RETAINER_UNAPP_EXP_RECEIVER_KEY = "retainer.unapplied.expense.reciever.key";
    public static final String RETAINER_ADD_EXP_TO_VCH = "retainer.add.unapplied.expense.to.vch.reciever.key";
    public static final String RETAINER_GOV_MSGS = "retainer.gov.privacyActNotice.msgs.reciever.key";
    public static final String RETAINER_GET_AUTH_NUMS = "retainer.gov.get.auth.num.reciever.key";
    public static final String RETAINER_GENERATE_AUTH = "retainer.gov.generate.authnum.reciever.key";
    public static final String RETAINER_PERDIEM_LOCATION = "retainer.gov.perdiem.location.reciever.key";
    public static final String RETAINER_SEND_AGREEMENT = "retainer.gov.send.safe.harbor.agreement.reciever.key";
    public static final String RETAINER_DELETE_DOC_EXPENSE = "retainer.gov.delete.doc.expense";
    public static final String RETAINER_DELETE_UNAPPLIED_DOC_EXPENSE = "retainer.gov.delete.unapplied.doc.expense";
    public static final String RETAINER_DOCUMENT_AUTH_FOR_VCH_LIST_KEY = "retainer.documentlist.authforvchlist.key";

}