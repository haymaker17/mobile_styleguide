/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.data;

import java.io.Serializable;

import android.content.ContentValues;
import android.database.Cursor;

import com.concur.mobile.core.data.MobileDatabaseHelper;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class ReasonCodeReqdResponse implements Serializable {

    private static final long serialVersionUID = 4873776445698660656L;
    public String stampName;
    public Boolean reasonReqd;
    public String userId, travId, docName, docType, stampReqUserId;

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("StampName")) {
            stampName = cleanChars;
        } else if (localName.equalsIgnoreCase("ReasonReqd")) {
            reasonReqd = Parse.safeParseBoolean(cleanChars);
        }
    }

    public ReasonCodeReqdResponse() {
        // TODO Auto-generated constructor stub
    }

    public ContentValues getContentVals(ReasonCodeReqdResponse response) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MobileDatabaseHelper.COLUMN_USER_ID, response.userId);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_TRAVID, response.travId);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_DOCNAME, response.docName);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_DOCTYPE, response.docType);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_STAMP_NAME, response.stampName);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_REQUIRED_REASON_USERID, response.stampReqUserId);
        int reasonReq = FormatUtil.convertBooleanIntoInt(response.reasonReqd);
        initialValues.put(MobileDatabaseHelper.COLUMN_GOV_REQUIRED_REASON_CODE, reasonReq);
        return initialValues;
    }

    public ReasonCodeReqdResponse(Cursor cursor) {
        String userID = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USER_ID));
        this.userId = userID;
        String docType = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_DOCTYPE));
        this.docType = docType;
        String travID = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_TRAVID));
        this.travId = travID;
        String docName = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_DOCNAME));
        this.docName = docName;
        String stamp = cursor
            .getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_STAMP_NAME));
        this.stampName = stamp;
        this.stampReqUserId = cursor
            .getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_REQUIRED_REASON_USERID));
        int reasonReq = cursor.getInt(cursor
            .getColumnIndex(MobileDatabaseHelper.COLUMN_GOV_REQUIRED_REASON_CODE));
        this.reasonReqd = FormatUtil.getValueFromInt(reasonReq);
    }
}
