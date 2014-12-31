/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.concur.mobile.core.data.MobileDatabaseHelper;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.util.SerializerUtil;
import com.concur.mobile.platform.util.Parse;

public class DsDocDetailInfo implements Serializable {

    private static final long serialVersionUID = -2568383094190569044L;

    public String travelerId;
    public String xmlReply;
    public String documentName;
    public String TANumber;
    public String currentStatus;
    public String purposeCode;
    public String comments;
    public String imageId;

    public Double totalEstCost;
    public Double nonReimbursableAmount;
    public Double advAmtRequested;
    public Double advApplied;
    public Double payToChargeCard;
    public Double payToTraveler;
    public Double emissionsLbs;

    public Calendar tripBeginDate;
    public Calendar tripEndDate;

    public List<PerdiemTDY> perdiemList;
    public List<AccountCode> accountCodeList;
    public List<Exceptions> exceptionsList;
    public List<GovExpense> expensesList;
    public List<ReasonCodes> reasonCodeList;

    public Audit audit;

    // database fields
    public String userID;
    public Calendar lastUsed;
    public String docType;

    public DsDocDetailInfo() {
        perdiemList = new ArrayList<PerdiemTDY>();
        accountCodeList = new ArrayList<AccountCode>();
        exceptionsList = new ArrayList<Exceptions>();
        expensesList = new ArrayList<GovExpense>();
        reasonCodeList = new ArrayList<ReasonCodes>();
        audit = new Audit();
    }

    @SuppressWarnings("unchecked")
    public DsDocDetailInfo(Cursor cursor) {
        this.userID = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USER_ID));
        this.docType = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_DOCTYPE));
        this.travelerId = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_TRAVID));
        this.documentName = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_DOCNAME));
        this.TANumber = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_TANUMBER));
        this.currentStatus = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_CURRENT_STATUS));
        this.purposeCode = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_PURPOSECODE));
        this.comments = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_COMMENT));
        this.imageId = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_IMAGE_ID));
        String amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_NON_REIMBURS_AMOUNT));
        Double nonrimAmt = Parse.safeParseDouble(amt);
        this.nonReimbursableAmount = nonrimAmt;
        amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_ADV_AMTREQ));
        Double advAmtReq = Parse.safeParseDouble(amt);
        this.advAmtRequested = advAmtReq;
        amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_ADV_APPLIED));
        Double advApplied = Parse.safeParseDouble(amt);
        this.advApplied = advApplied;
        amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_ADV_PAYTO_CARD));
        Double payToCard = Parse.safeParseDouble(amt);
        this.payToChargeCard = payToCard;
        amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_PAYTO_TRAVELER));
        Double payToTrav = Parse.safeParseDouble(amt);
        this.payToTraveler = payToTrav;
        amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_TOTAL_EST_AMT));
        Double totalEstAmt = Parse.safeParseDouble(amt);
        this.totalEstCost = totalEstAmt;
        amt = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_EMISSIONS));
        Double emission = Parse.safeParseDouble(amt);
        this.emissionsLbs = emission;
        String date = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_TRIP_BEGINDATE));
        Calendar startDate = Parse.parseTimestamp(date, FormatUtil.LONG_YEAR_MONTH_DAY);
        this.tripBeginDate = startDate;
        date = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_TRIP_ENDDATE));
        Calendar endDate = Parse.parseTimestamp(date, FormatUtil.LONG_YEAR_MONTH_DAY);
        this.tripEndDate = endDate;
        date = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_LAST_USED));
        Calendar now = Parse.parseTimestamp(date, FormatUtil.XML_DF);
        this.lastUsed = now;
        byte[] list = cursor.getBlob(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_PERDIEM_LIST));
        Object value = SerializerUtil.deserializeObject(list);
        List<PerdiemTDY> perdiemList = (List<PerdiemTDY>) value;
        this.perdiemList = perdiemList;
        list = cursor.getBlob(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_ACC_CODELIST));
        value = SerializerUtil.deserializeObject(list);
        List<AccountCode> accCodeList = (List<AccountCode>) value;
        this.accountCodeList = accCodeList;
        list = cursor.getBlob(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_EXCEPTIONLIST));
        value = SerializerUtil.deserializeObject(list);
        List<Exceptions> exceptionsList = (List<Exceptions>) value;
        this.exceptionsList = exceptionsList;
        list = cursor.getBlob(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_EXPENSELIST));
        value = SerializerUtil.deserializeObject(list);
        List<GovExpense> expensesList = (List<GovExpense>) value;
        this.expensesList = expensesList;
        list = cursor.getBlob(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_REASON_CODE));
        value = SerializerUtil.deserializeObject(list);
        List<ReasonCodes> reasonCodeList = (List<ReasonCodes>) value;
        this.reasonCodeList = reasonCodeList;
        list = cursor.getBlob(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_AUDIT));
        value = SerializerUtil.deserializeObject(list);
        Audit audit = (Audit) value;
        this.audit = audit;
    }

    public GovExpense findExpense(String expId) {
        GovExpense exp = null;

        if (expId != null && expensesList != null) {
            for (GovExpense e : expensesList) {
                if (expId.equals(e.expid)) {
                    exp = e;
                    break;
                }
            }
        }

        return exp;
    }

    public boolean deleteExpense(String expId) {
        GovExpense exp = findExpense(expId);
        if (exp != null) {
            return expensesList.remove(exp);
        }

        return false;
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("TravelerId")) {
            travelerId = cleanChars;
        } else if (localName.equalsIgnoreCase("DocumentName")) {
            documentName = cleanChars;
        } else if (localName.equalsIgnoreCase("TANumber")) {
            TANumber = cleanChars;
        } else if (localName.equalsIgnoreCase("CurrentStatus")) {
            currentStatus = cleanChars;
        } else if (localName.equalsIgnoreCase("PurposeCode")) {
            purposeCode = cleanChars;
        } else if (localName.equalsIgnoreCase("EmissionsLbs")) {
            emissionsLbs = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("TotalEstCost")) {
            totalEstCost = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("NonReimbursableAmount")) {
            nonReimbursableAmount = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("AdvAmtRequested")) {
            advAmtRequested = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("AdvApplied")) {
            advApplied = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("PayToChargeCard")) {
            payToChargeCard = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("PayToTraveler")) {
            payToTraveler = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Comments")) {
            comments = cleanChars;
        } else if (localName.equalsIgnoreCase("DocImageID")) {
            imageId = cleanChars;
        } else if (localName.equalsIgnoreCase("TripBeginDate")) {
            tripBeginDate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("TripEndDate")) {
            tripEndDate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        }
    }

    public static ContentValues getContentVals(DsDocDetailInfo info) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MobileDatabaseHelper.COLUMN_USER_ID, info.userID);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_TRAVID, info.travelerId);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_DOCNAME, info.documentName);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_DOCTYPE, info.docType);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_TANUMBER, info.TANumber);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_CURRENT_STATUS, info.currentStatus);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_PURPOSECODE, info.purposeCode);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_COMMENT, info.comments);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_IMAGE_ID, info.imageId);
        String amt = FormatUtil.localizeDouble(info.totalEstCost);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_TOTAL_EST_AMT, amt);
        amt = FormatUtil.localizeDouble(info.nonReimbursableAmount);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_NON_REIMBURS_AMOUNT, amt);
        amt = FormatUtil.localizeDouble(info.advAmtRequested);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_ADV_AMTREQ, amt);
        amt = FormatUtil.localizeDouble(info.advApplied);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_ADV_APPLIED, amt);
        amt = FormatUtil.localizeDouble(info.payToChargeCard);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_ADV_PAYTO_CARD, amt);
        amt = FormatUtil.localizeDouble(info.payToTraveler);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_PAYTO_TRAVELER, amt);
        amt = FormatUtil.localizeDouble(info.emissionsLbs);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_EMISSIONS, amt);
        if (info.tripBeginDate != null) {
            String date = FormatUtil.XML_DF.format(info.tripBeginDate.getTime());
            initialValues.put(MobileDatabaseHelper.COLUMN_GOV_TRIP_BEGINDATE, date);
        } else {
            initialValues.putNull(MobileDatabaseHelper.COLUMN_GOV_TRIP_BEGINDATE);
        }
        if (info.tripEndDate != null) {
            String date = FormatUtil.XML_DF.format(info.tripEndDate.getTime());
            initialValues.put(MobileDatabaseHelper.COLUMN_GOV_TRIP_ENDDATE, date);
        } else {
            initialValues.putNull(MobileDatabaseHelper.COLUMN_GOV_TRIP_ENDDATE);
        }
        if (info.lastUsed != null) {
            initialValues.put(MobileDatabaseHelper.COLUMN_LAST_USED, FormatUtil.XML_DF
                .format(info.lastUsed.getTime()));
        } else {
            initialValues.putNull(MobileDatabaseHelper.COLUMN_LAST_USED);
        }
        byte[] list = SerializerUtil.serializeObject(info.perdiemList);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_PERDIEM_LIST, list);
        list = SerializerUtil.serializeObject(info.accountCodeList);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_ACC_CODELIST, list);
        list = SerializerUtil.serializeObject(info.exceptionsList);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_EXCEPTIONLIST, list);
        list = SerializerUtil.serializeObject(info.expensesList);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_EXPENSELIST, list);
        list = SerializerUtil.serializeObject(info.reasonCodeList);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_REASON_CODE, list);
        list = SerializerUtil.serializeObject(info.audit);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_AUDIT, list);
        return initialValues;
    }
}
