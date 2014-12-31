package com.concur.mobile.core.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.IExpenseReportInfo.ReportType;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Crypt;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class MobileDatabase {

    private static final String CLS_TAG = MobileDatabase.class.getSimpleName();

    private static final int TOKEN_REQUEST_ID = -2;
    private static final String TOKEN_USER_ID = "SYS";

    private Crypt crypto;
    private MobileDatabaseHelper dbHelper;

    public MobileDatabase(Context context) {

        // Get our helper
        dbHelper = new MobileDatabaseHelper(context);

        // Get our crypto up and running
        crypto = new Crypt(getPassword());

        // Check to see if the encryption key still works.
        checkEncryption();

    }

    /**
     * Will close any open database handles.
     */
    public void close() {
        if (dbHelper != null) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".close: calling close on helper.");
            dbHelper.close();
            dbHelper = null;
        }
    }

    public void reset() {

        // Login/pin and session ID are in the DB so cannot be wiped out. Hold them to put back in the DB after the wipe.
        String login = readComComponent(Const.COM_COMPONENT_1);
        String pin = readComComponent(Const.COM_COMPONENT_2);
        String sessionId = readComComponent(Const.COM_COMPONENT_3);

        // Wipe it
        dbHelper.deleteDatabase();

        // Go ahead and recreate it
        dbHelper.getWritableDatabase();

        // If needed, repopulate the info
        if (login != null && login.length() > 0) {
            writeComComponent(Const.COM_COMPONENT_1, login);
        }
        if (pin != null && pin.length() > 0) {
            writeComComponent(Const.COM_COMPONENT_2, pin);
        }
        if (sessionId != null && sessionId.length() > 0) {
            writeComComponent(Const.COM_COMPONENT_3, sessionId);
        }
    }

    /**
     * Verify that the encryption keys still work. If not, reset the database and put a new token in with the current encryption
     * key.
     */
    protected void checkEncryption() {
        // We use a token row in the RESPONSE table to hold a value we know will always be there
        String token = null;

        try {
            token = loadResponse(TOKEN_REQUEST_ID, TOKEN_USER_ID);
        } catch (SQLiteException sqle) {
            // These are sometimes showing up in the Market. No explanation. Possibly a resource
            // constraint issue. Set the token to a blank string which will result in a reset() below.
            token = "";
            Log.e(Const.LOG_TAG, CLS_TAG + ".checkEncryption: error checking encryption", sqle);
        }

        boolean needToken = false;
        if (token == null) {
            // The token row does not exist. A clean DB.
            needToken = true;
        }

        if (token != null && token.equals("")) {
            // Decryption failed. We will assume that it was a BadPaddingException at this point
            // and reset the DB and put in a new token.
            Log.d(Const.LOG_TAG, CLS_TAG + ".checkEncryption: Resetting DB due to key change");
            reset();
            needToken = true;
        }

        if (needToken) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".checkEncryption: Writing new DB token");
            Calendar cal = Calendar.getInstance();
            saveResponse(TOKEN_REQUEST_ID, cal, cal.toString(), TOKEN_USER_ID);
        }
    }

    /**
     * Retrieve the password used for key generation for the DB
     */
    protected String getPassword() {
        // Create our password. The default values below don't matter too much because that
        // should only occur the very first time the app is run. In that case we will shortly
        // be setting the login and pin and the next time we crank up the DB it will get
        // wiped (because of the changed key) and we will then be running with the real key.

        String login = readComComponent(Const.COM_COMPONENT_1);
        if (login == null || login.length() == 0) {
            login = "FOO";
        }

        String pin = readComComponent(Const.COM_COMPONENT_2);
        if (pin == null || pin.length() == 0) {
            pin = "BAR";
        }

        StringBuilder sb = new StringBuilder(login).append(pin);

        return sb.toString();
    }

    /**
     * Trivial wrapper around our encryption object.
     */
    protected String encrypt(String plainText) {
        return crypto.encrypt(plainText);
    }

    /**
     * Trivial wrapper around our encryption object.
     */
    protected String decrypt(String cipherText) {
        return crypto.decrypt(cipherText);
    }

    /**
     * Trivial wrapper around our encryption object.
     */
    protected String encryptDouble(Double plainText) {
        return crypto.encrypt(Double.toHexString(plainText));
    }

    /**
     * Trivial wrapper around our encryption object.
     */
    protected Double decryptDouble(String cipherText) {
        return Double.valueOf(crypto.decrypt(cipherText));
    }

    /**
     * Trivial wrapper around our encryption object.
     */
    protected byte[] encrypt(byte[] data) {
        return crypto.encrypt(data);
    }

    /**
     * Trivial wrapper around our encryption object.
     */
    protected byte[] decrypt(byte[] cipherBytes) {
        return crypto.decrypt(cipherBytes);
    }

    public void writeComComponent(int id, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int[] ids = { id, id * 2 + 5, id + 17, id * 3 + 22, id * 3 + 8, id * 7 + 13, id + 95 };

        // Delete this and the chaff. Each id (which must be between 1 and 6) has its own chaff.
        // Repeat is predictable but the purpose of this chaff is to just make the table look fuller.
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[0] });
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[1] });
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[2] });
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[3] });
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[4] });
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[5] });
        db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new Object[] { ids[6] });

        // Store this and the chaff encrypted
        byte[] valueBytes = value.getBytes();
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[0], Preferences.PREF_CRYPT.encrypt(valueBytes) });

        // Munge for the chaff
        if (valueBytes.length < 1) {
            // Blank string, throw some garbage
            valueBytes = new byte[] { ((Double) Math.random()).byteValue(), ((Double) Math.random()).byteValue(),
                    ((Double) Math.random()).byteValue(), ((Double) Math.random()).byteValue(),
                    ((Double) Math.random()).byteValue(), ((Double) Math.random()).byteValue() };
        }
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[1], Preferences.PREF_CRYPT.encrypt(valueBytes) });
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[2], Preferences.PREF_CRYPT.encrypt(valueBytes) });
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[3], Preferences.PREF_CRYPT.encrypt(valueBytes) });
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[4], Preferences.PREF_CRYPT.encrypt(valueBytes) });
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[5], Preferences.PREF_CRYPT.encrypt(valueBytes) });
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        valueBytes[(int) (Math.random() * (valueBytes.length - 1))] = (byte) (Math.random() * Byte.MAX_VALUE);
        db.execSQL(MobileDatabaseHelper.INSERT_COM_COMPONENT_SQL,
                new Object[] { ids[6], Preferences.PREF_CRYPT.encrypt(valueBytes) });

    }

    public String readComComponent(int id) {

        String value = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor row = null;
        try {
            row = db.rawQuery(MobileDatabaseHelper.LOAD_COM_COMPONENT_SQL, new String[] { Integer.toString(id) });
            if (row.moveToFirst()) {
                int col = row.getColumnIndex(MobileDatabaseHelper.COLUMN_COMP_VALUE);
                if (col > -1) {
                    byte[] encValue = row.getBlob(col);
                    if (encValue != null) {
                        byte[] decryptedData = Preferences.PREF_CRYPT.decrypt(encValue);
                        if (decryptedData != null) {
                            value = new String(decryptedData);
                        }
                    }
                }
            }
        } finally {
            if (row != null) {
                row.close();
            }
        }

        return value;
    }

    public void deleteComComponent(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL(MobileDatabaseHelper.DELETE_COM_COMPONENT_SQL, new String[] { Integer.toString(id) });
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".dcc", e);
        }
    }

    /**
     * Will load the set of <code>ReceiptShareItem</code> objects from persistence.
     * 
     * @return returns a list of <code>ReceiptShareItem</code> objects loaded from persistence.
     */
    public List<ReceiptShareItem> loadReceiptShareItems() {
        return loadReceiptShareItems(null);
    }

    /**
     * Will load the set of <code>ReceiptShareItem</code> objects from persistence filtered by status.
     * 
     * @param status
     *            A status to filter the results. No filtering occurs if status is null
     * 
     * @return returns a list of <code>ReceiptShareItem</code> objects loaded from persistence.
     */
    public List<ReceiptShareItem> loadReceiptShareItems(ReceiptShareItem.Status status) {
        List<ReceiptShareItem> rsItems = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            if (status == null) {
                results = db.rawQuery(MobileDatabaseHelper.SELECT_RECEIPT_SHARE_SQL, (String[]) null);
            } else {
                StringBuilder query = new StringBuilder(MobileDatabaseHelper.SELECT_RECEIPT_SHARE_SQL);
                query.append(" WHERE ").append(MobileDatabaseHelper.WHERE_RECEIPT_SHARE_STATUS);
                results = db.rawQuery(query.toString(), new String[] { status.toString() });
            }
            if (results.moveToFirst()) {
                rsItems = new ArrayList<ReceiptShareItem>(results.getCount());
                do {
                    ReceiptShareItem rsItem = new ReceiptShareItem();
                    // Grab the Uri.
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_URI);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            String uriStr = results.getString(colInd);
                            if (uriStr != null) {
                                rsItem.uri = Uri.parse(uriStr);
                            }
                        } else {
                            rsItem.uri = null;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".loadReceiptShareItems: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_URI + "'.");
                    }
                    // Grab the mimeType.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_MIME_TYPE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            rsItem.mimeType = results.getString(colInd);
                        } else {
                            rsItem.mimeType = null;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".loadReceiptShareItems: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_MIME_TYPE + "'.");
                    }
                    // Grab the file name.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_FILE_NAME);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            rsItem.fileName = results.getString(colInd);
                        } else {
                            rsItem.fileName = null;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".loadReceiptShareItems: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_FILE_NAME + "'.");
                    }
                    // Grab the display name.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_DISPLAY_NAME);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            rsItem.displayName = results.getString(colInd);
                        } else {
                            rsItem.displayName = null;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".loadReceiptShareItems: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_DISPLAY_NAME + "'.");
                    }
                    // Grab the status.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_STATUS);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            String statusStr = results.getString(colInd);
                            if (statusStr != null) {
                                rsItem.status = ReceiptShareItem.Status.fromString(statusStr);
                            }
                        } else {
                            rsItem.status = null;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".loadReceiptShareItems: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_STATUS + "'.");
                    }
                    rsItems.add(rsItem);
                } while (results.moveToNext());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return rsItems;
    }

    /**
     * Will add a list of <code>ReceiptShareItem</code> objects to persistence.
     * 
     * @param rsItems
     *            contains the list of <code>ReceiptShareItem</code> objects to be added to persistence.
     */
    public void insertReceiptShareItems(List<ReceiptShareItem> rsItems) {
        if (rsItems != null && rsItems.size() > 0) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues contVals = new ContentValues();
            for (ReceiptShareItem rsItem : rsItems) {
                if (rsItem.uri != null) {
                    contVals.put(MobileDatabaseHelper.COLUMN_URI, rsItem.uri.toString());
                } else {
                    contVals.putNull(MobileDatabaseHelper.COLUMN_URI);
                }
                if (rsItem.mimeType != null) {
                    contVals.put(MobileDatabaseHelper.COLUMN_MIME_TYPE, rsItem.mimeType);
                } else {
                    contVals.putNull(MobileDatabaseHelper.COLUMN_MIME_TYPE);
                }
                if (rsItem.fileName != null) {
                    contVals.put(MobileDatabaseHelper.COLUMN_FILE_NAME, rsItem.fileName);
                } else {
                    contVals.putNull(MobileDatabaseHelper.COLUMN_FILE_NAME);
                }
                if (rsItem.displayName != null) {
                    contVals.put(MobileDatabaseHelper.COLUMN_DISPLAY_NAME, rsItem.displayName);
                } else {
                    contVals.putNull(MobileDatabaseHelper.COLUMN_DISPLAY_NAME);
                }
                if (rsItem.status != null) {
                    contVals.put(MobileDatabaseHelper.COLUMN_STATUS, rsItem.status.getName());
                } else {
                    contVals.putNull(MobileDatabaseHelper.COLUMN_DISPLAY_NAME);
                }
                long rowID = db.insert(MobileDatabaseHelper.TABLE_RECEIPT_SHARE, null, contVals);
                if (rowID == -1L) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertReceiptShareItems: unable to insert ReceiptShareItem!");
                }
                contVals.clear();
            }
        }
    }

    /**
     * Will remove a list of <code>ReceiptShareItem</code> objects from persistence.
     * 
     * @param rsItems
     *            contains the list of <code>ReceiptShareItem</code> objects to be removed from persistence.
     */
    public void deleteReceiptShareItems(List<ReceiptShareItem> rsItems) {
        if (rsItems != null && rsItems.size() > 0) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            for (ReceiptShareItem rsItem : rsItems) {
                int rowsDeleted = db.delete(MobileDatabaseHelper.TABLE_RECEIPT_SHARE,
                        MobileDatabaseHelper.WHERE_RECEIPT_SHARE_URI, new String[] { rsItem.uri.toString() });
                if (rowsDeleted == 0) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReceiptShareItems: SQL delete affected 0 rows!");
                }
            }
        }
    }

    /**
     * Will remove an offline receipt from persistence
     */
    public void deleteOfflineReceipt(ReceiptInfo ri) {
        if (ri != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete(MobileDatabaseHelper.TABLE_RECEIPT_SHARE,
                    MobileDatabaseHelper.WHERE_RECEIPT_SHARE_FILE, new String[] { ri.getFileName() });
            if (rowsDeleted == 0) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteOfflineReceipt: SQL delete affected 0 rows!");
            }
        }
    }

    /**
     * Save the provided response
     * 
     * @param responseId
     *            The ID of the response (see Const.MSG_*_RESULT)
     * @param responseXml
     *            The XML retrieved from the MWS
     * @throws Exception
     * @throws SQLException
     */
    public void saveResponse(int responseId, Calendar lastRetrieve, String responseXml, String userId) {
        saveResponse(responseId, lastRetrieve, responseXml, userId, true);
    }

    /**
     * Deletes all instances of a particular response.
     * 
     * @param responseId
     *            the id of the response.
     * @param userId
     *            the user id.
     */
    public void deleteResponse(int responseId, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Delete an existing response
        db.execSQL(MobileDatabaseHelper.DELETE_RESPONSE_SQL, new Object[] { responseId, userId });
    }

    /**
     * Save the provided response but allow for an unencrypted save
     * 
     * @param responseId
     *            The ID of the response (see Const.MSG_*_RESULT)
     * @param responseXml
     *            The XML retrieved from the MWS
     * @throws Exception
     * @throws SQLException
     */
    public void saveResponse(int responseId, Calendar lastRetrieve, String responseXml, String userId, boolean encrypt) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Delete an existing response
        db.execSQL(MobileDatabaseHelper.DELETE_RESPONSE_SQL, new Object[] { responseId, userId });
        // Insert the new one
        String retrieveTS = FormatUtil.XML_DF.format(lastRetrieve.getTime());

        try {
            String storedResponse;
            if (encrypt) {
                storedResponse = encrypt(responseXml);
            } else {
                storedResponse = responseXml;
            }

            db.execSQL(MobileDatabaseHelper.INSERT_RESPONSE_SQL, new Object[] { responseId, userId, retrieveTS,
                    storedResponse });
        } catch (OutOfMemoryError oome) {
            // The encrypt (most likely) failed. The record won't be written but the data
            // should still be in memory for the client.
            // Log the failure.
            Log.e(Const.LOG_TAG, CLS_TAG + ".saveResponse: failure saving response to database", oome);
        }
    }

    /**
     * Will update the response time for a previously saved response.
     * 
     * @param responseId
     *            the id of the response time to update.
     * @param lastRetrieve
     *            the last retrieval update time.
     * @param userId
     *            the user id.
     */
    public void updateResponseTime(int responseId, Calendar lastRetrieve, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Update the existing response time.
        String retrieveTS = FormatUtil.XML_DF.format(lastRetrieve.getTime());
        try {
            db.execSQL(MobileDatabaseHelper.UPDATE_RESPONSE_TIME_SQL, new Object[] { retrieveTS, responseId, userId });
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG,
                    CLS_TAG + ".updateResponseTime: SQL Exception updating response time -- " + sqlExc.getMessage(),
                    sqlExc);
        }
    }

    /**
     * Loads the the last server response based on <code>responseId</code> for the user <code>userId</code>.
     * 
     * @param responseId
     *            the response id.
     * @param userId
     *            the user id.
     * 
     * @return the last server response based on <code>responseId</code> for the user <code>userId</code>.
     */
    public String loadResponse(int responseId, String userId) {
        return loadResponse(responseId, userId, true);
    }

    /**
     * Loads the the last server response based on <code>responseId</code> for the user <code>userId</code>. Allows retrieval of
     * unencrypted responses.
     * 
     * @param responseId
     *            the response id.
     * @param userId
     *            the user id.
     * 
     * @return the last server response based on <code>responseId</code> for the user <code>userId</code>.
     */
    public String loadResponse(int responseId, String userId, boolean decrypt) {
        String response = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.LOAD_RESPONSE_SQL, new String[] { Integer.toString(responseId),
                    userId });
            if (results.moveToFirst()) {
                // Market stacks indicate a failure in retrieving the value here for some number of users
                // We can't tell why at this point but we'll buttress this code to prevent the FC
                int col = results.getColumnIndex(MobileDatabaseHelper.RESPONSE_RESPONSE);
                if (col > -1) {
                    try {
                        response = results.getString(col);
                    } catch (IllegalStateException ise) {
                        // And this is the real bugger from the market. Not sure why it occurs.
                        // Log it but get out clean.
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".loadResponse: Illegal state exception thrown while retrieving response "
                                + responseId, ise);
                    }
                    // MOB-21385 This may or may not be the cause for the crash, but one of the exception in the log points to
                    // an exception possibly caused by trying to decrypt an empty string.
                    // Add check for empty string, in addition to null pointer check.
                    if (!TextUtils.isEmpty(response) && decrypt) {
                        response = decrypt(response);
                    }
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return response;
    }

    /**
     * Gets the last server response time for the response <code>responseId</code> and user <code>userId</code>.
     * 
     * @param responseId
     *            the response id.
     * @param userId
     *            the user id.
     * 
     * @return the last server response time for the response <code>responseId</code> and user <code>userId</code>.
     */
    public Calendar getReponseLastRetrieveTS(int responseId, String userId) {

        Calendar response = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.RESPONSE_LAST_UPDATE_SQL,
                    new String[] { Integer.toString(responseId), userId });
            if (results.moveToFirst()) {
                String ts = results.getString(results.getColumnIndex(MobileDatabaseHelper.RESPONSE_CLIENT_LAST_UPDATE));
                response = Parse.parseXMLTimestamp(ts);
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return response;
    }

    // TODO E-DAO Need to delete old ExpenseList XML saved in DB.

    /**
     * Will load a set of report header objects.
     * 
     * @param type
     *            the report type.
     * @param userId
     *            the user id.
     * @param isDetail
     *            whether to load detail or summary headers.
     * @param loadXML
     *            whether to load the header XML.
     * @return a list of <code>IExpenseReportDBInfo</code> objects containing report header information.
     */
    public List<IExpenseReportDBInfo> loadReportHeaders(ReportType type, String userId, boolean isDetail,
            boolean loadXML) {
        final String MTAG = CLS_TAG + ".loadReportHeaders: ";
        List<IExpenseReportDBInfo> reportInfos = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            String queryStr = null;
            if (loadXML) {
                queryStr = MobileDatabaseHelper.SELECT_REPORT_HEADERS_SQL;
            } else {
                queryStr = MobileDatabaseHelper.SELECT_REPORT_HEADERS_NO_XML_SQL;
            }
            results = db.rawQuery(queryStr, new String[] { type.name(), userId, Boolean.toString(isDetail) });
            if (results.moveToFirst()) {
                reportInfos = new ArrayList<IExpenseReportDBInfo>(results.getCount());
                do {
                    try {
                        // Read the report key.
                        int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_KEY);
                        Assert.assertTrue(MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_REPORT_KEY,
                                (colInd != -1));
                        String reportKey = results.getString(colInd);

                        // Read report header.
                        String headerXML = null;
                        if (loadXML) {
                            colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_HEADER);
                            Assert.assertTrue(MTAG + "results missing column "
                                    + MobileDatabaseHelper.COLUMN_REPORT_HEADER, (colInd != -1));
                            headerXML = decrypt(results.getString(colInd));
                        }

                        // Read the last update time stamp.
                        colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE);
                        Assert.assertTrue(MTAG + "results missing colum "
                                + MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE, (colInd != -1));
                        String updateTimeStr = results.getString(colInd);
                        Calendar updateTime = Parse.parseXMLTimestamp(updateTimeStr);

                        // If we wanted the header and it came back blank then do not add the report. That will
                        // cause FCs down the line. Something happened to the DB crypt key and the decrypt failed.
                        // The report list will be updated right after this anyway.
                        if (!loadXML || (loadXML && headerXML.length() > 0)) {
                            // Construct the new header info object.
                            reportInfos.add(new ExpenseReportDBInfo(reportKey, null, type, updateTime, isDetail,
                                    headerXML));
                        }
                    } catch (AssertionFailedError afe) {
                        Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                    }
                } while (results.moveToNext());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return reportInfos;
    }

    /**
     * Will load the report header info object.
     * 
     * @param type
     *            the report type.
     * @param userId
     *            the user id.
     * @param reportKey
     *            the report key.
     * @param isDetail
     *            whether to load detail or summary header.
     * @return An instance of <code>IExpenseReportDBInfo</code> if the report exists; otherwise, <code>null</code>.
     */
    public IExpenseReportDBInfo loadReportHeader(ReportType type, String userId, String reportKey, boolean isDetail) {
        final String MTAG = CLS_TAG + ".loadReportHeader: ";
        IExpenseReportDBInfo reportInfo = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_HEADER_SQL, new String[] { type.name(), userId,
                    reportKey, Boolean.toString(isDetail) });
            if (results.moveToFirst()) {
                try {
                    // Read the header XML.
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_HEADER);
                    Assert.assertTrue(MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_REPORT_HEADER,
                            (colInd != -1));
                    String headerXML = decrypt(results.getString(colInd));

                    // Read the last update time stamp.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE);
                    Assert.assertTrue(MTAG + "results missing colum "
                            + MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE, (colInd != -1));
                    String updateTimeStr = results.getString(colInd);
                    Calendar updateTime = Parse.parseXMLTimestamp(updateTimeStr);

                    if (headerXML.length() > 0) {
                        // Construct the new header info object.
                        reportInfo = new ExpenseReportDBInfo(reportKey, null, type, updateTime, isDetail, headerXML);
                    }
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                    reportInfo = null;
                }
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return reportInfo;
    }

    /**
     * Will load the report entry info object.
     * 
     * @param headerDBKey
     *            the report header database key.
     * @param rptKey
     *            the report key.
     * @param rptEntKey
     *            the report entry key.
     * @param userId
     *            the user id.
     * @return an instance of <code>IExpenseReportEntryInfo</code> object containing the report entry XML; otherwise,
     *         <code>null</code> is returned.
     */
    public IExpenseReportEntryInfo loadReportEntry(long headerDBKey, String rptKey, String rptEntKey, String userId) {
        final String MTAG = CLS_TAG + ".loadReportEntries";
        IExpenseReportEntryInfo entryInfo = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_ENTRY_SQL,
                    new String[] { Long.toString(headerDBKey), rptKey, rptEntKey, userId });
            if (results.moveToFirst()) {
                try {
                    // Read the report entry.
                    String entryXML = "";
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_ENTRY);
                    Assert.assertTrue(MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_REPORT_ENTRY,
                            (colInd != -1));
                    byte[] compressedData = decrypt(results.getBlob(colInd));
                    if (compressedData != null) {
                        // Uncompress the byte array.
                        try {
                            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
                            GZIPInputStream gzipIn = new GZIPInputStream(bais);

                            InputStreamReader inRdr = new InputStreamReader(gzipIn);
                            char[] chBuf = new char[(8 * 1024)];
                            int charsRead = 0;
                            StringBuilder strBldr = new StringBuilder();
                            while ((charsRead = inRdr.read(chBuf, 0, chBuf.length)) != -1) {
                                strBldr.append(chBuf, 0, charsRead);
                            }
                            entryXML = strBldr.toString();

                            // Read the 'is detail" flag.
                            colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_IS_DETAIL);
                            Assert.assertTrue(
                                    MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_REPORT_KEY,
                                    (colInd != -1));
                            boolean isDetail = Boolean.parseBoolean(results.getString(colInd));

                            // Construct the new entry info object.
                            entryInfo = new ExpenseReportEntryInfo(null, entryXML, isDetail);

                        } catch (IOException ioExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".loadReportEntries: I/O exception decompressing string!",
                                    ioExc);
                        }
                    }

                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return entryInfo;
    }

    /**
     * Will load all report entries associated with a particular report object.
     * 
     * @param headerDBKey
     *            the report header database key.
     * @return a list of <code>IExpenseReportEntryInfo</code> objects.
     */
    public List<IExpenseReportEntryInfo> loadReportEntries(long headerDBKey) {
        final String MTAG = CLS_TAG + ".loadReportEntries: ";
        List<IExpenseReportEntryInfo> entryInfos = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_ENTRIES_SQL,
                    new String[] { Long.toString(headerDBKey) });
            if (results.moveToFirst()) {
                entryInfos = new ArrayList<IExpenseReportEntryInfo>(results.getCount());
                do {
                    try {
                        // Read the report entry.
                        String entryXML = "";
                        int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_ENTRY);
                        Assert.assertTrue(MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_REPORT_ENTRY,
                                (colInd != -1));
                        byte[] compressedData = decrypt(results.getBlob(colInd));
                        if (compressedData != null) {
                            // Uncompress the byte array.
                            try {
                                ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
                                GZIPInputStream gzipIn = new GZIPInputStream(bais);

                                InputStreamReader inRdr = new InputStreamReader(gzipIn);
                                char[] chBuf = new char[(8 * 1024)];
                                int charsRead = 0;
                                StringBuilder strBldr = new StringBuilder();
                                while ((charsRead = inRdr.read(chBuf, 0, chBuf.length)) != -1) {
                                    strBldr.append(chBuf, 0, charsRead);
                                }
                                entryXML = strBldr.toString();

                                // Read the 'is detail" flag.
                                colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_IS_DETAIL);
                                Assert.assertTrue(MTAG + "results missing column "
                                        + MobileDatabaseHelper.COLUMN_REPORT_KEY, (colInd != -1));
                                boolean isDetail = Boolean.parseBoolean(results.getString(colInd));

                                // Construct the new entry info object.
                                entryInfos.add(new ExpenseReportEntryInfo(null, entryXML, isDetail));

                            } catch (IOException ioExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".loadReportEntries: I/O exception decompressing string!", ioExc);
                            }
                        }

                    } catch (AssertionFailedError afe) {
                        Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                    }
                } while (results.moveToNext());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return entryInfos;
    }

    /**
     * Will look up the primary key for a report header.
     * 
     * @param reportKey
     *            the report key.
     * @param type
     *            the report type.
     * @param detail
     *            whether the report is a detailed report.
     * @param userId
     *            the user is associated with the report.
     * @return returns the primary key of the report header; otherwise, <code>-1</code> is returned.
     */
    public long lookUpReportHeaderID(String reportKey, ReportType type, boolean detail, String userId) {
        final String MTAG = CLS_TAG + ".lookUpReportHeaderID: ";
        long headerId = -1L;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_HEADER_ID_SQL, new String[] { type.name(),
                    reportKey, userId, Boolean.toString(detail) });
            if (results.moveToFirst()) {
                try {
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_ID);
                    Assert.assertTrue(MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_ID, (colInd != -1));
                    headerId = results.getLong(colInd);
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                }
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return headerId;
    }

    /**
     * Will update the report header.
     * 
     * @param headerXML
     *            the report header represented as XML.
     * @param reportKey
     *            the report key.
     * @param type
     *            the report type.
     * @param userId
     *            the user id.
     * @param detail
     *            whether this is a report detail object.
     * @param updateTime
     *            the report update time.
     * @param headerDBKey
     *            the header database key.
     * @return an instance of <code>IExpenseReportDBInfo</code>.
     */
    public IExpenseReportDBInfo updateReportHeader(String headerXML, String reportKey, ReportType type, String userId,
            boolean detail, Calendar updateTime) {
        IExpenseReportDBInfo reportInfo = null;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        contVals.put(MobileDatabaseHelper.COLUMN_REPORT_KEY, reportKey);
        contVals.put(MobileDatabaseHelper.COLUMN_REPORT_TYPE, type.name());
        contVals.put(MobileDatabaseHelper.COLUMN_REPORT_HEADER, encrypt(headerXML));
        contVals.put(MobileDatabaseHelper.COLUMN_IS_DETAIL, Boolean.toString(detail));
        contVals.put(MobileDatabaseHelper.RESPONSE_CLIENT_LAST_UPDATE, FormatUtil.XML_DF.format(updateTime.getTime()));
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_REPORT_HEADER, contVals,
                MobileDatabaseHelper.WHERE_REPORT_HEADER,
                new String[] { reportKey, userId, type.name(), Boolean.toString(detail) });
        if (numRowsAffected == 0) {
            long rowId = db.insert(MobileDatabaseHelper.TABLE_REPORT_HEADER, null, contVals);
            if (rowId != -1L) {
                reportInfo = new ExpenseReportDBInfo(reportKey, null, type, updateTime, detail, headerXML);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateReportHeader: unable to insert new report header record!");
            }
        } else if (numRowsAffected == 1) {
            reportInfo = new ExpenseReportDBInfo(reportKey, null, type, updateTime, detail, headerXML);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDetailReport: more than one detail report updated for same key!");
        }
        return reportInfo;
    }

    /**
     * Will update the report header update time.
     * 
     * @param reportKey
     *            the report key.
     * @param type
     *            the report type.
     * @param userId
     *            the user id.
     * @param detail
     *            whether this a report detail object.
     * @param updateTime
     *            the report update time.
     * @return an instance of <code>IExpenseReportDBInfo</code>.
     */
    public IExpenseReportDBInfo updateReportHeader(String reportKey, ReportType type, String userId, boolean detail,
            Calendar updateTime) {
        IExpenseReportDBInfo reportInfo = null;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.RESPONSE_CLIENT_LAST_UPDATE, FormatUtil.XML_DF.format(updateTime.getTime()));
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_REPORT_HEADER, contVals,
                MobileDatabaseHelper.WHERE_REPORT_HEADER,
                new String[] { reportKey, userId, type.name(), Boolean.toString(detail) });
        if (numRowsAffected == 1) {
            reportInfo = new ExpenseReportDBInfo(reportKey, null, type, updateTime, detail, null);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateReportHeader: unable to update header update time!");
        }
        return reportInfo;
    }

    /**
     * Will delete an entry in the REPORT_HEADER table whose ID is <code>headerDBKey</code>.
     * 
     * @param headerDBKey
     *            the report header primary header key.
     */
    public boolean deleteReportHeader(long headerDBKey) {
        boolean retVal = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(MobileDatabaseHelper.TABLE_REPORT_HEADER, MobileDatabaseHelper.WHERE_ID,
                new String[] { Long.toString(headerDBKey) });
        retVal = (rowsDeleted > 0);
        return retVal;
    }

    /**
     * Will delete all entries in the REPORT_ENTRY table whose HEADER_KEY value is <code>headerDBKey</code>.
     * 
     * @param headerDBKey
     *            the unique database ID for the report header.
     */
    public boolean deleteReportEntries(long headerDBKey) {
        boolean retVal = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(MobileDatabaseHelper.TABLE_REPORT_ENTRY,
                MobileDatabaseHelper.WHERE_REPORT_HEADER_KEY, new String[] { Long.toString(headerDBKey) });
        retVal = (rowsDeleted > 0);
        return retVal;
    }

    /**
     * Will delete an entry from the REPORT_ENTRY table whose HEADER_KEY value is <code>headerDBKey</code> and detail is
     * <code>detail</code>.
     * 
     * @param headerDBKey
     *            the report header database key.
     * @param detail
     *            whether the report entry is a detailed entry.
     * @return returns <code>true</code> if a report entry was deleted; <code>false</code> otherwise.
     */
    public boolean deleteReportEntry(long headerDBKey, String userId, String reportKey, String reportEntryKey,
            boolean detail) {
        boolean retVal = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(MobileDatabaseHelper.TABLE_REPORT_ENTRY,
                MobileDatabaseHelper.WHERE_DELETE_REPORT_ENTRY_SQL, new String[] { Long.toString(headerDBKey), userId,
                        reportKey, reportEntryKey, Boolean.toString(detail) });
        retVal = (rowsAffected > 0);
        return retVal;
    }

    /**
     * Will update a report entry or insert one if it doesn't exist.
     * 
     * @param headerDBKey
     *            the report header database key.
     * @param userId
     *            the user id.
     * @param reportKey
     *            the report key.
     * @param reportEntryKey
     *            the report entry key.
     * @param detail
     *            whether this is a detailed report entry.
     * @param entryXML
     *            the entry XML.
     */
    public boolean updateReportEntry(long headerDBKey, String userId, String reportKey, String reportEntryKey,
            boolean detail, String entryXML) {
        boolean success = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_HEADER_KEY, headerDBKey);
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        contVals.put(MobileDatabaseHelper.COLUMN_REPORT_KEY, reportKey);
        contVals.put(MobileDatabaseHelper.COLUMN_REPORT_ENTRY_KEY, reportEntryKey);
        // Create Gzip compress 'entryXML'.
        byte[] blobData = null;
        if (entryXML != null) {
            OutputStreamWriter outStrWriter = null;
            try {
                ByteArrayOutputStream byteOutStr = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(byteOutStr);
                outStrWriter = new OutputStreamWriter(gzipOut);
                outStrWriter.write(entryXML);
                outStrWriter.flush();
                gzipOut.finish();
                blobData = byteOutStr.toByteArray();
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateReportEntry: unable to GZip compress XML data!", ioExc);
                return false;
            }
        }
        contVals.put(MobileDatabaseHelper.COLUMN_REPORT_ENTRY, encrypt(blobData));
        contVals.put(MobileDatabaseHelper.COLUMN_IS_DETAIL, Boolean.toString(detail));
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_REPORT_ENTRY, contVals,
                MobileDatabaseHelper.WHERE_REPORT_ENTRY, new String[] { Long.toString(headerDBKey), reportKey,
                        reportEntryKey, Boolean.toString(detail) });
        if (numRowsAffected == 0) {
            long rowId = db.insert(MobileDatabaseHelper.TABLE_REPORT_ENTRY, null, contVals);
            success = (rowId != -1L);
            if (!success) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateReportEntry: unable to insert new report entry record!");
            }
        } else if (!(success = (numRowsAffected == 1))) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateReportEntry: more than one report entry updated for same key!");
        }
        return success;
    }

    /**
     * Will update an existing itinerary or insert one if not already in the database.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @param userId
     *            the user id
     * @param itinXml
     *            the itinerary serialized as XML.
     * @param updateTime
     *            the itinerary update time.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public IItineraryDBInfo updateItinerary(String itinLocator, String userId, String itinXml, Calendar updateTime) {
        IItineraryDBInfo itinInfo = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_ITINERARY_LOCATOR, itinLocator);
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        contVals.put(MobileDatabaseHelper.RESPONSE_CLIENT_LAST_UPDATE, FormatUtil.XML_DF.format(updateTime.getTime()));
        // Create Gzip compress 'itinXML'.
        byte[] blobData = null;
        if (itinXml != null) {
            OutputStreamWriter outStrWriter = null;
            try {
                ByteArrayOutputStream byteOutStr = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(byteOutStr);
                outStrWriter = new OutputStreamWriter(gzipOut);
                outStrWriter.write(itinXml);
                outStrWriter.flush();
                gzipOut.finish();
                blobData = byteOutStr.toByteArray();
            } catch (IOException ioExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateItinerary: unable to GZip compress XML data!", ioExc);
                return null;
            }
        }
        contVals.put(MobileDatabaseHelper.COLUMN_ITINERARY, encrypt(blobData));
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_ITINERARY, contVals,
                MobileDatabaseHelper.WHERE_ITINERARY_SQL, new String[] { userId, itinLocator });
        if (numRowsAffected == 0) {
            long rowId = db.insert(MobileDatabaseHelper.TABLE_ITINERARY, null, contVals);
            if (rowId != -1L) {
                itinInfo = new ItineraryDBInfo(itinLocator, null, updateTime, itinXml);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateItinerary: unable to insert new itinerary record!");
            }
        } else if (numRowsAffected == 1) {
            itinInfo = new ItineraryDBInfo(itinLocator, null, updateTime, itinXml);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateItinerary: more than one itinerary updated for same itin locator!");
        }
        return itinInfo;
    }

    /**
     * Will load the update time for an itinerary.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @param userId
     *            the user id.
     * @return returns an instance of <code>Calendar</code> containing the update time; <code>null</code> if itinerary doesn't
     *         exist.
     */
    public Calendar loadItineraryUpdateTime(String itinLocator, String userId) {
        Calendar updateTime = null;
        final String MTAG = CLS_TAG + ".loadItineraryUpdateTime";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_ITINERARY_UPDATE_TIME_SQL, new String[] { userId,
                    itinLocator });
            if (results.moveToFirst()) {
                try {
                    // Read the last update time stamp.
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE);
                    Assert.assertTrue(MTAG + "results missing colum "
                            + MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE, (colInd != -1));
                    String updateTimeStr = results.getString(colInd);
                    updateTime = Parse.parseXMLTimestamp(updateTimeStr);
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return updateTime;
    }

    /**
     * Will load the XML representation and update time for an itinerary.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @param userId
     *            the user id.
     * @return an instance of <code>IItineraryDBInfo</code> if the itinerary exists; <code>null</code> otherwise.
     */
    public IItineraryDBInfo loadItinerary(String itinLocator, String userId) {
        IItineraryDBInfo itinInfo = null;
        final String MTAG = CLS_TAG + ".loadItinerary";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_ITINERARY_SQL, new String[] { userId, itinLocator });
            if (results.moveToFirst()) {
                try {
                    // Read the itin XML.
                    String itinXml = "";
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_ITINERARY);
                    Assert.assertTrue(MTAG + "results missing column " + MobileDatabaseHelper.COLUMN_ITINERARY,
                            (colInd != -1));
                    byte[] compressedData = decrypt(results.getBlob(colInd));
                    if (compressedData != null) {
                        // Uncompress the byte array.
                        try {
                            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
                            GZIPInputStream gzipIn = new GZIPInputStream(bais);

                            InputStreamReader inRdr = new InputStreamReader(gzipIn);
                            char[] chBuf = new char[(8 * 1024)];
                            int charsRead = 0;
                            StringBuilder strBldr = new StringBuilder();
                            while ((charsRead = inRdr.read(chBuf, 0, chBuf.length)) != -1) {
                                strBldr.append(chBuf, 0, charsRead);
                            }
                            itinXml = strBldr.toString();

                            // Read the last update time stamp.
                            colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE);
                            Assert.assertTrue(MTAG + "results missing colum "
                                    + MobileDatabaseHelper.COLUMN_RESPONSE_CLIENT_LAST_UPDATE, (colInd != -1));
                            String updateTimeStr = results.getString(colInd);
                            Calendar updateTime = Parse.parseXMLTimestamp(updateTimeStr);

                            // Construct the itin info object.
                            if (itinXml.length() > 0) {
                                itinInfo = new ItineraryDBInfo(itinLocator, null, updateTime, itinXml);
                            }
                        } catch (IOException ioExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".loadItinerary: I/O exception decompressing string!", ioExc);
                        }
                    }
                } catch (AssertionFailedError afe) {
                    Log.e(Const.LOG_TAG, afe.getMessage(), afe);
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return itinInfo;
    }

    /**
     * Will delete an itinerary from the database.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @param userId
     *            the user id.
     * @return returns <code>true</code> if the itinerary was found and deleted; <code>false</code> otherwise.
     */
    public boolean deleteItinerary(long itinLocator, String userId) {
        boolean success = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(MobileDatabaseHelper.TABLE_ITINERARY, MobileDatabaseHelper.WHERE_ITINERARY_SQL,
                new String[] { userId, Long.toString(itinLocator) });
        success = (rowsDeleted > 0);
        return success;
    }

    /**
     * Will delete all itineraries from the database associated with a user id.
     * 
     * @param userId
     *            the user id.
     * @return returns <code>true</code> if at least one itinerary was deleted; <code>false</code> otherwise.
     */
    public boolean deleteItineraries(String userId) {
        boolean success = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(MobileDatabaseHelper.TABLE_ITINERARY, MobileDatabaseHelper.WHERE_USER_ID,
                new String[] { userId });
        success = (rowsDeleted > 0);
        return success;
    }

    /**
     * Gets the list of report keys for reports that have been submitted for approval and pending a response from the MWS.
     * 
     * <b>NOTE:</b> The method returns the list of reports submitted for approval, not reports that are in the act of approval,
     * i.e, by an approver.
     * 
     * @return the list of report keys for reports submitted for approval pending a response from the MWS.
     */
    public ArrayList<String> getReportsSubmitted(String userId) {

        ArrayList<String> reportKeys = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_KEY_SUBMIT_SQL,
                    new String[] { userId, Integer.toString(Const.MSG_EXPENSE_REPORT_SUBMIT_REQUEST) });
            if (results != null) {
                while (results.moveToNext()) {
                    reportKeys.add(results.getString(results.getColumnIndex(MobileDatabaseHelper.COLUMN_OTHER)));
                }
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return reportKeys;
    }

    /**
     * Gets the list of report keys for reports that have been submitted for approval and pending a response from the MWS.
     * 
     * @return the list of report keys for reports submitted for approval pending a response from the MWS.
     */
    public ArrayList<String> getReportsSubmittedApprove(String userId) {

        ArrayList<String> reportKeys = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_KEY_SUBMIT_APPROVE_SQL, new String[] { userId });
            if (results != null) {
                while (results.moveToNext()) {
                    reportKeys.add(results.getString(results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_KEY)));
                }
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return reportKeys;
    }

    /**
     * Gets the list of report keys for reports that have been submitted for rejection and pending a response from the MWS.
     * 
     * @return the list of report keys for reports submitted for rejection pending a response from the MWS.
     */
    public ArrayList<String> getReportsSubmittedReject(String userId) {

        ArrayList<String> reportKeys = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_REPORT_KEY_SUBMIT_REJECT_SQL, new String[] { userId });
            if (results != null) {
                while (results.moveToNext()) {
                    reportKeys.add(results.getString(results.getColumnIndex(MobileDatabaseHelper.COLUMN_REPORT_KEY)));
                }
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return reportKeys;
    }

    /**
     * Will add a new report approval request to the database.
     * 
     * @param reportKey
     *            the report key of the report being approval.
     * @param request
     *            the report approval request.
     * @param clientTransactionId
     *            the local client-generated transaction.
     * 
     * @return returns <code>true</code> if the addition was successful; <code>false</code> otherwise.
     */
    public boolean addReportSubmittedApprove(String userId, String reportKey, String request, long clientTransactionId) {

        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (reportKey != null) {
            ContentValues contVals = new ContentValues();
            contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
            contVals.put(MobileDatabaseHelper.COLUMN_REPORT_KEY, reportKey);
            contVals.put(MobileDatabaseHelper.COLUMN_CLIENT_TRANSACTION_ID, clientTransactionId);
            contVals.put(MobileDatabaseHelper.COLUMN_REQUEST, encrypt(request));
            long rowId = db.insert(MobileDatabaseHelper.TABLE_REPORT_SUBMIT_APPROVE, null, contVals);
            if (rowId != -1) {
                result = true;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".addReportSubmittedApprove: failed to add approve request for report '"
                        + reportKey + "'.");
            }
        }
        return result;
    }

    /**
     * Will delete the report submission approval information for the report identified by <code>reportKey</code>.
     * 
     * @param reportKey
     *            the key of the report to delete.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean deleteReportSubmittedApprove(String reportKey) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (reportKey != null) {
            ContentValues contVals = new ContentValues();
            contVals.put(MobileDatabaseHelper.COLUMN_REPORT_KEY, reportKey);
            int numRows = db.delete(MobileDatabaseHelper.TABLE_REPORT_SUBMIT_APPROVE,
                    MobileDatabaseHelper.WHERE_REPORT_KEY, new String[] { reportKey });
            if (numRows == 1) {
                result = true;
            } else if (numRows > 1) {
                result = true;
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReportSubmittedApprove: deleted " + numRows
                        + " instances for report '" + reportKey + "'");
            } else {
                result = false;
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReportSubmittedApprove: report submission for report '"
                        + reportKey + "' not found!");
            }
        }
        return result;
    }

    /**
     * Will add a new report reject request to the database.
     * 
     * @param reportKey
     *            the report key of the report being rejected.
     * @param request
     *            the report reject request.
     * @param clientTransactionId
     *            the local client-generated transaction.
     * 
     * @return returns <code>true</code> if the addition was successful; <code>false</code> otherwise.
     */
    public boolean addReportSubmittedReject(String userId, String reportKey, String request, long clientTransactionId) {

        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (reportKey != null) {
            ContentValues contVals = new ContentValues();
            contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
            contVals.put(MobileDatabaseHelper.COLUMN_REPORT_KEY, reportKey);
            contVals.put(MobileDatabaseHelper.COLUMN_CLIENT_TRANSACTION_ID, clientTransactionId);
            contVals.put(MobileDatabaseHelper.COLUMN_REQUEST, encrypt(request));
            long rowId = db.insert(MobileDatabaseHelper.TABLE_REPORT_SUBMIT_REJECT, null, contVals);
            if (rowId != -1) {
                result = true;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".addReportSubmittedReject: failed to add reject request for report '"
                        + reportKey + "'.");
            }
        }
        return result;
    }

    /**
     * Will delete the report submission rejection information for the report identified by <code>reportKey</code>.
     * 
     * @param reportKey
     *            the key of the report to delete.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean deleteReportSubmittedReject(String reportKey) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (reportKey != null) {
            ContentValues contVals = new ContentValues();
            contVals.put(MobileDatabaseHelper.COLUMN_REPORT_KEY, reportKey);
            int numRows = db.delete(MobileDatabaseHelper.TABLE_REPORT_SUBMIT_REJECT,
                    MobileDatabaseHelper.WHERE_REPORT_KEY, new String[] { reportKey });
            if (numRows == 1) {
                result = true;
            } else if (numRows > 1) {
                result = true;
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReportSubmittedReject: deleted " + numRows
                        + " instances for report '" + reportKey + "'");
            } else {
                result = false;
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReportSubmittedReject: report submission for report '"
                        + reportKey + "' not found!");
            }
        }
        return result;
    }

    /**
     * Will check the reference count for the receipt image file <code>receiptImageFilePath</code> referenced by mobile expense
     * entries other than <code>localKey</code>.
     * 
     * @param localKey
     *            the local key of the mobile entry to be ignored.
     * @param receiptImageFilePath
     *            the path of the receipt image file.
     * 
     * @return the reference count.
     */
    public int getReceiptImageFilePathReferenceCount(String localKey, String receiptImageFilePath) {

        int refCount = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(MobileDatabaseHelper.SELECT_MOBILE_EXPENSE_RECEIPT_REFERENCE_COUNT_SQL, new String[] {
                    localKey, receiptImageFilePath });
            refCount = cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return refCount;
    }

    /**
     * Will load the list of mobile expense entries.
     * 
     * @param userId
     *            the id of the user.
     * 
     * @return a list of <code>MobileEntry</code> objects; otherwise <code>null</code> will be returned.
     */
    public ArrayList<MobileEntry> loadMobileEntries(String userId) {
        return loadMobileEntries(userId, null);
    }

    /**
     * Will load the list of mobile expense entries.
     * 
     * @param userId
     *            the id of the user.
     * 
     * @return a list of <code>MobileEntry</code> objects; otherwise <code>null</code> will be returned.
     */
    public ArrayList<MobileEntry> loadMobileEntries(String userId, MobileEntryStatus status) {

        ArrayList<MobileEntry> entries = null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            if (status == null) {
                results = db.rawQuery(MobileDatabaseHelper.SELECT_EXPENSE_ENTRY_SQL, new String[] { userId });
            } else {
                results = db.rawQuery(MobileDatabaseHelper.SELECT_EXPENSE_ENTRY_BY_STATUS_SQL, new String[] { userId,
                        status.toString() });
            }
            if (results.moveToFirst()) {
                entries = new ArrayList<MobileEntry>(results.getCount());
                do {
                    MobileEntry mobileEntry = new MobileEntry();
                    // Grab the local key.
                    int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_ID);
                    if (colInd != -1) {
                        int id = results.getInt(colInd);
                        mobileEntry.setLocalKey(Integer.toString(id));
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_ID + "'.");
                    }
                    // Grab the currency code.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_CRN_CODE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setCrnCode(decrypt(results.getString(colInd)));
                        } else {
                            mobileEntry.setCrnCode(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_CRN_CODE + "'.");
                    }
                    // Grab the expense type key.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_KEY);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setExpKey(decrypt(results.getString(colInd)));
                        } else {
                            mobileEntry.setExpKey(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_EXP_KEY + "'.");
                    }
                    // Grab the expense type name.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_NAME);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setExpName(decrypt(results.getString(colInd)));
                        } else {
                            mobileEntry.setExpName(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_EXP_NAME + "'.");
                    }
                    // Set receipt image id.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_ID);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setReceiptImageId(results.getString(colInd));
                        } else {
                            mobileEntry.setReceiptImageId(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_ID + "'.");
                    }
                    // Grab whether the expense has a receipt image.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setHasReceiptImage(Boolean.parseBoolean(results.getString(colInd)));
                        } else {
                            mobileEntry.setHasReceiptImage(false);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE + "'.");
                    }
                    // Grab the expense receipt flag indicating we have local image data.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setReceiptImageDataLocal(Boolean.parseBoolean(results.getString(colInd)));
                        } else {
                            mobileEntry.setReceiptImageDataLocal(false);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL + "'.");
                    }
                    // Grab the expense receipt local image data file path.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setReceiptImageDataLocalFilePath(results.getString(colInd));
                        } else {
                            mobileEntry.setReceiptImageDataLocalFilePath(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH + "'.");
                    }

                    // Grab the expense location name.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_LOCATION_NAME);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setLocationName(decrypt(results.getString(colInd)));
                        } else {
                            mobileEntry.setLocationName(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_LOCATION_NAME + "'.");
                    }
                    // Grab the expense vendor name.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_VENDOR_NAME);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setVendorName(decrypt(results.getString(colInd)));
                        } else {
                            mobileEntry.setVendorName(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_VENDOR_NAME + "'.");
                    }
                    // Grab the expense entry type.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_EXPENSE_ENTRY_TYPE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            Expense.ExpenseEntryType expType = Expense.ExpenseEntryType.valueOf(decrypt(results
                                    .getString(colInd)));
                            if (expType != null) {
                                mobileEntry.setEntryType(expType);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: invalid expense entry type!");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: null expense entry type!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_EXPENSE_ENTRY_TYPE + "'.");
                    }
                    // Grab the expense mobile entry key.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_ENTRY_KEY);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setMeKey(results.getString(colInd));
                        } else {
                            mobileEntry.setMeKey(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_ENTRY_KEY + "'.");
                    }
                    // Grab the expense mobile pca key.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_CA_KEY);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setPcaKey(results.getString(colInd));
                        } else {
                            mobileEntry.setPcaKey(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_CA_KEY + "'.");
                    }
                    // Grab the expense mobile pct key.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_CT_KEY);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            // Both Pct/Cct use same field in database.
                            switch (mobileEntry.getEntryType()) {
                            case CASH:
                                // no-op.
                                break;
                            case PERSONAL_CARD:
                                mobileEntry.setPctKey(results.getString(colInd));
                                break;
                            case CORPORATE_CARD:
                                mobileEntry.setCctKey(results.getString(colInd));
                                break;
                            default:
                                break;
                            }
                        } else {
                            // Both Pct/Cct use same field in database.
                            mobileEntry.setPctKey(null);
                            mobileEntry.setCctKey(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_CT_KEY + "'.");
                    }
                    // Grab the expense transaction amount.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_TRANSACTION_AMOUNT);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setTransactionAmount(decryptDouble(results.getString(colInd)));
                        } else {
                            mobileEntry.setTransactionAmount(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_TRANSACTION_AMOUNT + "'.");
                    }
                    // Grab the expense transaction date.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_TRANSACTION_DATE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setTransactionDate(decrypt(results.getString(colInd)));
                            mobileEntry.setTransactionDateCalendar(Parse.parseXMLTimestamp(mobileEntry
                                    .getTransactionDate()));
                        } else {
                            mobileEntry.setTransactionDate(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_TRANSACTION_DATE + "'.");
                    }
                    // Grab the expense comment.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_COMMENT);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setComment(decrypt(results.getString(colInd)));
                        } else {
                            mobileEntry.setComment(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_COMMENT + "'.");
                    }
                    // Grab the expense update date.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_UPDATE_DATE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setUpdateDate(Parse.parseXMLTimestamp(results.getString(colInd)));
                        } else {
                            mobileEntry.setUpdateDate(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_UPDATE_DATE + "'.");
                    }
                    // Grab the expense create date.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_CREATE_DATE);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setCreateDate(Parse.parseXMLTimestamp(results.getString(colInd)));
                        } else {
                            mobileEntry.setCreateDate(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_CREATE_DATE + "'.");
                    }
                    // Grab the status.
                    colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_STATUS);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            mobileEntry.setStatus(MobileEntryStatus.valueOf(results.getString(colInd)));
                        } else {
                            mobileEntry.setStatus(null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".loadMobileEntries: unable to locate column index for column '"
                                + MobileDatabaseHelper.COLUMN_STATUS + "'.");
                    }
                    // Add to the list of entries.
                    entries.add(mobileEntry);
                } while (results.moveToNext());
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return entries;
    }

    /**
     * Will update the mobile entry in the database if it already exists.
     * 
     * @param userId
     *            the user id associated with the mobile entry.
     * @param mobileEntry
     *            the mobile entry.
     * @param updateTime
     *            the last save time.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean updateMobileEntry(String userId, MobileEntry mobileEntry, Calendar updateTime) {

        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        if (mobileEntry.getCrnCode() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_CRN_CODE, encrypt(mobileEntry.getCrnCode()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CRN_CODE);
        }
        if (mobileEntry.getExpKey() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_EXP_KEY, encrypt(mobileEntry.getExpKey()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_EXP_KEY);
        }
        if (mobileEntry.getExpName() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_EXP_NAME, encrypt(mobileEntry.getExpName()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_EXP_NAME);
        }
        if (mobileEntry.getReceiptImageId() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_ID, mobileEntry.getReceiptImageId());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_ID);
        }
        contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE, Boolean.toString(mobileEntry.hasReceiptImage()));
        contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL,
                Boolean.toString(mobileEntry.hasReceiptImageDataLocal()));
        if (mobileEntry.hasReceiptImageDataLocal()) {
            contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH,
                    mobileEntry.getReceiptImageDataLocalFilePath());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
        }
        if (mobileEntry.getLocationName() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_LOCATION_NAME, encrypt(mobileEntry.getLocationName()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_LOCATION_NAME);
        }
        if (mobileEntry.getVendorName() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_VENDOR_NAME, encrypt(mobileEntry.getVendorName()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_VENDOR_NAME);
        }
        if (mobileEntry.getEntryType() != null) {
            // Mobile entries of type "SMART" represent the paired cash transaction and as such are persisted as
            // cash transactions.
            String entryTypeText = mobileEntry.getEntryType().name();
            if (mobileEntry.getEntryType() == Expense.ExpenseEntryType.SMART_CORPORATE
                    || mobileEntry.getEntryType() == Expense.ExpenseEntryType.SMART_PERSONAL) {
                entryTypeText = Expense.ExpenseEntryType.CASH.name();
            }
            contVals.put(MobileDatabaseHelper.COLUMN_EXPENSE_ENTRY_TYPE, encrypt(entryTypeText));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateMobileEntry: mobile entry has a null expense type!");
            contVals.putNull(MobileDatabaseHelper.COLUMN_EXPENSE_ENTRY_TYPE);
        }
        if (mobileEntry.getMeKey() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_ENTRY_KEY, mobileEntry.getMeKey());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_ENTRY_KEY);
        }
        if (mobileEntry.getPcaKey() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_CA_KEY, mobileEntry.getPcaKey());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CA_KEY);
        }
        if (mobileEntry.getPctKey() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_CT_KEY, mobileEntry.getPctKey());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CT_KEY);
        }
        // Mobile entries associated with a smart expense retain their CT keys based on
        // the paired card transaction. The PCT key is not retained in the DB, but
        // rather reset upon smart expense edit based on matched corporate card transaction.
        if ((mobileEntry.getCctKey() != null && mobileEntry.getEntryType() != Expense.ExpenseEntryType.SMART_CORPORATE)
                || (mobileEntry.getPctKey() != null && mobileEntry.getEntryType() != Expense.ExpenseEntryType.SMART_PERSONAL)) {
            contVals.put(MobileDatabaseHelper.COLUMN_CT_KEY, mobileEntry.getCctKey());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CT_KEY);
        }
        if (mobileEntry.getTransactionAmount() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_TRANSACTION_AMOUNT,
                    encryptDouble(mobileEntry.getTransactionAmount()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_TRANSACTION_AMOUNT);
        }
        if (mobileEntry.getTransactionDate() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_TRANSACTION_DATE, encrypt(mobileEntry.getTransactionDate()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_TRANSACTION_DATE);
        }
        if (mobileEntry.getComment() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_COMMENT, encrypt(mobileEntry.getComment()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_COMMENT);
        }
        if (updateTime != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_UPDATE_DATE, FormatUtil.XML_DF.format(updateTime.getTime()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_UPDATE_DATE);
        }
        if (mobileEntry.getCreateDate() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_CREATE_DATE,
                    FormatUtil.XML_DF.format(mobileEntry.getCreateDate().getTime()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CREATE_DATE);
        }
        if (mobileEntry.getStatus() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_STATUS, mobileEntry.getStatus().name());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_STATUS);
        }
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, contVals,
                MobileDatabaseHelper.WHERE_ID, new String[] { mobileEntry.getLocalKey() });
        if (numRowsAffected == 0) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateMobileEntry: no mobile entries could be updated.");
        } else if (numRowsAffected == 1) {
            result = true;
        } else {
            result = true;
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateMobileEntry: updated more than 1 row based with local id of '"
                    + mobileEntry.getLocalKey() + "'.");
        }
        return result;
    }

    /**
     * Loads the receipt image data based on <code>localKey</code> for a mobile expense receipt.
     * 
     * @param localKey
     *            the local key.
     * 
     * @return the base 64 encoded receipt image data.
     */
    public String loadMobileEntryReceiptImageData(String localKey) {

        String imageData = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_EXPENSE_RECEIPT_IMAGE_DATA, new String[] { localKey });
            if (results.moveToFirst()) {
                int colInd = results.getColumnIndex(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
                if (colInd != -1) {
                    imageData = results.getString(colInd);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".loadMobileEntryReceiptImageData: unable to locate index for column '"
                            + MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH + "' in result set!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + "loadMobileEntryReceiptImageData: no mobile entry for local key '"
                        + localKey + "'.");
            }
        } finally {
            if (results != null) {
                results.close();
                results = null;
            }
        }
        return imageData;
    }

    /**
     * Will insert an entry into the database for a mobile entry.
     * 
     * @param userId
     *            the user id associated with the mobile entry.
     * @param mobileEntry
     *            the mobile entry to insert.
     * @param insertTime
     *            the insertion time.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean insertMobileEntry(String userId, MobileEntry mobileEntry, Calendar insertTime) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        contVals.put(MobileDatabaseHelper.COLUMN_CRN_CODE, encrypt(mobileEntry.getCrnCode()));
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_KEY, encrypt(mobileEntry.getExpKey()));
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_NAME, encrypt(mobileEntry.getExpName()));
        if (mobileEntry.getReceiptImageId() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_ID, mobileEntry.getReceiptImageId());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_ID);
        }
        contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE, Boolean.toString(mobileEntry.hasReceiptImage()));
        contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL,
                Boolean.toString(mobileEntry.hasReceiptImageDataLocal()));
        if (mobileEntry.hasReceiptImageDataLocal()) {
            contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH,
                    mobileEntry.getReceiptImageDataLocalFilePath());
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
        }
        contVals.put(MobileDatabaseHelper.COLUMN_LOCATION_NAME, encrypt(mobileEntry.getLocationName()));
        contVals.put(MobileDatabaseHelper.COLUMN_VENDOR_NAME, encrypt(mobileEntry.getVendorName()));
        // Mobile entries of type "SMART" represent the paired cash transaction and as such are persisted as
        // cash transactions.
        String entryTypeText = mobileEntry.getEntryType().name();
        if (mobileEntry.getEntryType() == Expense.ExpenseEntryType.SMART_CORPORATE
                || mobileEntry.getEntryType() == Expense.ExpenseEntryType.SMART_PERSONAL) {
            entryTypeText = Expense.ExpenseEntryType.CASH.name();
        }
        contVals.put(MobileDatabaseHelper.COLUMN_EXPENSE_ENTRY_TYPE, encrypt(entryTypeText));
        contVals.put(MobileDatabaseHelper.COLUMN_ENTRY_KEY, mobileEntry.getMeKey());
        switch (mobileEntry.getEntryType()) {
        case CASH:
            contVals.putNull(MobileDatabaseHelper.COLUMN_CA_KEY);
            contVals.putNull(MobileDatabaseHelper.COLUMN_CT_KEY);
            break;
        case PERSONAL_CARD: {
            contVals.put(MobileDatabaseHelper.COLUMN_CA_KEY, mobileEntry.getPcaKey());
            contVals.put(MobileDatabaseHelper.COLUMN_CT_KEY, mobileEntry.getPctKey());
            break;
        }
        case CORPORATE_CARD: {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CA_KEY);
            contVals.put(MobileDatabaseHelper.COLUMN_CT_KEY, mobileEntry.getCctKey());
            break;
        }
        case SMART_CORPORATE:
        case SMART_PERSONAL: {
            contVals.putNull(MobileDatabaseHelper.COLUMN_CA_KEY);
            contVals.putNull(MobileDatabaseHelper.COLUMN_CT_KEY);
        }
        }
        contVals.put(MobileDatabaseHelper.COLUMN_TRANSACTION_AMOUNT, encryptDouble(mobileEntry.getTransactionAmount()));
        contVals.put(MobileDatabaseHelper.COLUMN_TRANSACTION_DATE, encrypt(mobileEntry.getTransactionDate()));
        contVals.put(MobileDatabaseHelper.COLUMN_COMMENT, encrypt(mobileEntry.getComment()));
        contVals.put(MobileDatabaseHelper.COLUMN_UPDATE_DATE, FormatUtil.XML_DF.format(insertTime.getTime()));
        contVals.put(MobileDatabaseHelper.COLUMN_CREATE_DATE, FormatUtil.XML_DF.format(insertTime.getTime()));

        contVals.put(MobileDatabaseHelper.COLUMN_STATUS, mobileEntry.getStatus().toString());
        long rowID = db.insert(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, null, contVals);
        result = (rowID != -1L);
        if (result) {
            mobileEntry.setLocalKey(Long.toString(rowID));
        }
        return result;
    }

    /**
     * Will insert into the HTTP request table an MWS bound request.
     * 
     * @param userId
     *            the user id associated with the request.
     * @param verb
     *            the HTTP verb, i.e., POST, PUT, etc.
     * @param endPoint
     *            the service endpoint (including arguments).
     * @param body
     *            the body of the request.
     * @param msgId
     *            the client-generated message id.
     * @param other
     *            a piece of opaque data specific to a client of this method.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean insertHTTPRequest(String userId, String verb, String endPoint, String body, int requestId,
            String msgId, String other) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        contVals.put(MobileDatabaseHelper.COLUMN_VERB, verb);
        contVals.put(MobileDatabaseHelper.COLUMN_SERVICE_ENDPOINT, endPoint);
        if (body != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_BODY, encrypt(body));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_BODY);
        }
        contVals.put(MobileDatabaseHelper.COLUMN_CLIENT_REQUEST_ID, requestId);
        contVals.put(MobileDatabaseHelper.COLUMN_CLIENT_TRANSACTION_ID, msgId);
        if (other != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_OTHER, other);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_OTHER);
        }
        long rowId = db.insert(MobileDatabaseHelper.TABLE_HTTP_REQUEST, null, contVals);
        result = (rowId != -1L);
        return result;
    }

    public boolean doesHTTPRequestExist(int requestId) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor exist = null;
        try {
            exist = db.query(MobileDatabaseHelper.TABLE_HTTP_REQUEST,
                    new String[] { MobileDatabaseHelper.COLUMN_CLIENT_REQUEST_ID },
                    MobileDatabaseHelper.WHERE_CLIENT_REQUEST_ID, new String[] { Integer.toString(requestId) }, null,
                    null, null);

            result = exist.moveToFirst();
        } finally {
            exist.close();
        }
        return result;
    }

    /**
     * Will delete an entry in the HTTP request table based on <code>msgId</code>.
     * 
     * @param msgId
     *            the message id.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean deleteHTTPRequest(String msgId) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_CLIENT_TRANSACTION_ID, msgId);
        int numRows = db.delete(MobileDatabaseHelper.TABLE_HTTP_REQUEST,
                MobileDatabaseHelper.WHERE_CLIENT_TRANSACTION_ID, new String[] { msgId });
        if (numRows == 1) {
            result = true;
        } else if (numRows > 1) {
            result = true;
            Log.e(Const.LOG_TAG, CLS_TAG + ".deleteHTTPRequest: deleted " + numRows + " instances for msgId '" + msgId
                    + "'");
        } else {
            result = false;
            Log.e(Const.LOG_TAG, CLS_TAG + ".deleteHTTPRequest: message id '" + msgId + "' not found!");
        }
        return result;
    }

    /**
     * Will delete an entry in the HTTP request table based on <code>requestId</code>.
     * 
     * @param requestId
     *            the request id.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean deleteHTTPRequest(int requestId) {
        boolean result = false;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_CLIENT_REQUEST_ID, requestId);
        int numRows = db.delete(MobileDatabaseHelper.TABLE_HTTP_REQUEST, MobileDatabaseHelper.WHERE_CLIENT_REQUEST_ID,
                new String[] { Integer.toString(requestId) });
        if (numRows == 1) {
            result = true;
        } else if (numRows > 1) {
            result = true;
            Log.e(Const.LOG_TAG, CLS_TAG + ".deleteHTTPRequest: deleted " + numRows + " instances for requestId '"
                    + requestId + "'");
        } else {
            result = false;
            Log.e(Const.LOG_TAG, CLS_TAG + ".deleteHTTPRequest: request id '" + requestId + "' not found!");
        }
        return result;
    }

    /**
     * Will update the mobile entry key for a mobile entry whose local key is <code>localKey</code>.
     * 
     * @param localKey
     *            the local key.
     * @param mobileEntryKey
     *            the mobile entry key.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean updateMobileEntryKey(String localKey, String mobileEntryKey) {

        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_ENTRY_KEY, mobileEntryKey);
        contVals.put(MobileDatabaseHelper.COLUMN_STATUS, MobileEntryStatus.NORMAL.toString());
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, contVals,
                MobileDatabaseHelper.WHERE_ID, new String[] { localKey });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Will update the mobile entry "has receipt" property for a mobile entry whose local key is <code>localKey</code>.
     * 
     * @param localKey
     *            the local key.
     * @param hasReceipt
     *            contains whether the mobile entry has a receipt.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean updateMobileEntryHasReceipt(String localKey, boolean hasReceipt) {

        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_ID, localKey);
        contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE, Boolean.toString(hasReceipt));
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, contVals,
                MobileDatabaseHelper.WHERE_ID, new String[] { localKey });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Will update the mobile entry status for a mobile entry whose local key is <code>localKey</code>.
     * 
     * @param localKey
     *            the local key.
     * @param status
     *            the mobile entry status.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean updateMobileEntryStatus(String localKey, MobileEntryStatus status) {

        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_STATUS, status.toString());
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, contVals,
                MobileDatabaseHelper.WHERE_ID, new String[] { localKey });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Will update the mobile entry data local file path indicating where the receipt image for a mobile entry is stored.
     * 
     * @param localKey
     *            the mobile entry local key.
     * @param filePath
     *            the mobiel entry local file path.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean updateMobileEntryDataLocalPath(String localKey, String filePath) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        boolean imageDataLocal = (filePath != null);
        contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL, Boolean.toString(imageDataLocal));
        if (imageDataLocal) {
            contVals.put(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH, filePath);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
        }
        int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, contVals,
                MobileDatabaseHelper.WHERE_ID, new String[] { localKey });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Will delete a mobile entry based on the mobile entry key.
     * 
     * @param mobileEntryKey
     *            the mobile entry key.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean deleteMobileEntry(String mobileEntryKey) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numRowsAffected = db.delete(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, MobileDatabaseHelper.WHERE_ENTRY_KEY,
                new String[] { mobileEntryKey });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Will delete a mobile entry based on the local key
     * 
     * @param localKey
     *            the local key
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean deleteMobileEntryByLocalKey(String localKey) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numRowsAffected = db.delete(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY, MobileDatabaseHelper.WHERE_ID,
                new String[] { localKey });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Will delete all expense entries that match on <code>userId</code>.
     * 
     * @param userId
     *            the user id.
     * 
     * @return <code>true</code> if any were deleted; <code>false</code> if not.
     */
    public boolean deleteNonLocalExpenseEntries(String userId) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numRowsAffected = db.delete(MobileDatabaseHelper.TABLE_EXPENSE_ENTRY,
                MobileDatabaseHelper.WHERE_USER_ID_AND_NON_LOCAL, new String[] { userId });
        result = (numRowsAffected != 0);
        return result;
    }

    /**
     * Adds a list of personal card transaction keys to the table of not-visible transactions.
     * 
     * @param pctKeys
     *            the list of personal card transaction keys.
     */
    public void addHiddenPersonalCardTransactions(ArrayList<String> pctKeys) {
        addHiddenCardTransactions(pctKeys, MobileDatabaseHelper.PERSONAL_CARD_STATUS_HIDDEN);
    }

    /**
     * Adds a list of corporate card transaction keys to the table of non-visible transactions.
     * 
     * @param cctKeys
     *            the list of corporate card transaction keys.
     */
    public void addHiddenCorporateCardTransactions(ArrayList<String> cctKeys) {
        addHiddenCardTransactions(cctKeys, MobileDatabaseHelper.CORPORATE_CARD_STATUS_HIDDEN);
    }

    /**
     * Adds a list of transaction keys and their status to the hidden transaction table.
     * 
     * @param tranKeys
     *            the list of transaction keys.
     * @param hiddenStatus
     *            the hidden status.
     */
    private void addHiddenCardTransactions(ArrayList<String> tranKeys, String hiddenStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int c = tranKeys.size();
        for (int i = 0; i < c; i++) {
            db.execSQL(MobileDatabaseHelper.INSERT_CARD_TRANSACTION_SQL, new Object[] { tranKeys.get(i), hiddenStatus });
        }
    }

    /**
     * Retrieves the list of hidden personal card transaction keys.
     * 
     * @return the list of personal card transaction keys.
     */
    public ArrayList<String> selectHiddenPersonalCardTransactions() {
        return selectHiddenCardTransactions(MobileDatabaseHelper.PERSONAL_CARD_STATUS_HIDDEN);
    }

    /**
     * Retrieves the list of hidden corporate card transaction keys.
     * 
     * @return the list of personal card transaction keys.
     */
    public ArrayList<String> selectHiddenCorporateCardTransactions() {
        return selectHiddenCardTransactions(MobileDatabaseHelper.CORPORATE_CARD_STATUS_HIDDEN);
    }

    /**
     * Retrieves a list of card transaction keys based on the value of <code>hiddenStatus</code>.
     * 
     * @param hiddenStatus
     *            the hidden status.
     * @return the list of card transaction keys with a hidden status of <code>hiddenStatus</code>.
     */
    private ArrayList<String> selectHiddenCardTransactions(String hiddenStatus) {
        ArrayList<String> tranKeys = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.SELECT_CARD_TRANSACTIONS_SQL, new String[] { hiddenStatus });
            if (results.moveToFirst()) {
                tranKeys = new ArrayList<String>(results.getCount());
                do {
                    tranKeys.add(results.getString(0));
                } while (results.moveToNext());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return tranKeys;
    }

    /**
     * Clears the list of hidden personal card transaction keys.
     */
    public void clearHiddenPersonalCardTransactions() {
        clearHiddenCardTransactions(MobileDatabaseHelper.PERSONAL_CARD_STATUS_HIDDEN);
    }

    /**
     * Clears the list of hidden corporate card transaction keys.
     */
    public void clearHiddenCorporateCardTransactions() {
        clearHiddenCardTransactions(MobileDatabaseHelper.CORPORATE_CARD_STATUS_HIDDEN);
    }

    /**
     * Clears the hidden card transaction table for those transactions whose status is <code>hiddenStatus</code>.
     * 
     * @param hiddenStatus
     *            the hidden status.
     */
    private void clearHiddenCardTransactions(String hiddenStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(MobileDatabaseHelper.CLEAR_CARD_TRANSACTIONS_SQL, new Object[] { hiddenStatus });
    }

    /**
     * InsertExpenseType into database.
     * 
     * @param expTypeList
     *            : all the object of this list going to be insert into database
     * @param userId
     *            : logged in user.
     * @param insertTime
     *            : current time or time where database has been updated.
     * @param polKey
     *            : policyKey.
     * @return true if this operation is successfully finished.
     */
    public boolean insertExpenseType(List<ExpenseType> expTypeList, String userId, String polKey) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_EXPENSE_TYPE_USING_USERID, new String[] { userId,
                polKey });

        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                Map<String, ExpenseType> expTypInMemoryMap = new HashMap<String, ExpenseType>();
                do {
                    ExpenseType dbHelperExpTyp = new ExpenseType(cursor);
                    expTypInMemoryMap.put(dbHelperExpTyp.key, dbHelperExpTyp);

                } while (cursor.moveToNext());

                cursor.close();

                if (expTypInMemoryMap.size() > 0) {
                    int numRowsAffected = db.delete(MobileDatabaseHelper.TABLE_EXPENSE_TYPE,
                            MobileDatabaseHelper.WHERE_UPDATE_EXPENSE_TYPE_ROW_USING_POLKEY, new String[] { userId,
                                    polKey });
                    result = (numRowsAffected != 0);
                    if (result) {
                        // Update operation : after deleting rows insert required rows again.
                        final int expTypeListSize = expTypeList.size();
                        for (int i = 0; i < expTypeListSize; i++) {
                            ExpenseType expTypeListObject = expTypeList.get(i);
                            ExpenseType memoryObject = expTypInMemoryMap.get(expTypeListObject.key);
                            if (memoryObject != null) {
                                expTypeListObject.setLastUsed(memoryObject.getLastUsed());
                                expTypeListObject.setuseCount(memoryObject.getuseCount());
                            }
                            result = insertExpenseTypeDB(expTypeListObject, userId, polKey, db);
                        }// outer for loop ends.

                    } else {
                        // delete operation fails.
                        result = false;
                        Log.e(Const.LOG_TAG, CLS_TAG + ".MobileDatabase.insertExpenseType: delete operation fails.");
                    }
                } else {
                    // expTypInMemory is empty
                    result = false;
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".MobileDatabase.insertExpenseType: List of ExpenseType in memory is empty.");
                }
            } else {
                // cursor is empty
                result = false;
                cursor.close();
                Log.e(Const.LOG_TAG, CLS_TAG + ".MobileDatabase.insertExpenseType: cursor is empty.");
            }

        } else {
            // table is empty
            cursor.close();
            for (ExpenseType expenseType : expTypeList) {
                expenseType.userID = userId;
                expenseType.polKey = polKey;
                result = insertExpenseTypeDB(expenseType, userId, polKey, db);
            }

        }
        return result;
    }

    /**
     * Final call to insert expense type data into database.
     * 
     * @param expTypeList
     *            : list which needs to insert into databases.
     * @param inMemoryList
     * @param userId
     *            : logged in user
     * @param polKey
     * @param insertTime
     *            : current time or time where database has been updated.
     * @param db
     *            : database object.
     * @return true : if successfully finished insertion.
     */
    private boolean insertExpenseTypeDB(ExpenseType expenseType, String userId, String polKey, SQLiteDatabase db) {
        ContentValues contVals = expenseType.getContentValuesForExpType(expenseType);
        if (expenseType.getLastUsed() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_LAST_USED,
                    FormatUtil.XML_DF.format(expenseType.getLastUsed().getTime()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_LAST_USED);
        }
        long rowId = db.insert(MobileDatabaseHelper.TABLE_EXPENSE_TYPE, null, contVals);
        boolean result = (rowId != -1L);
        return result;
    }

    /***
     * Load Expense Type from Database.
     * 
     * @param userId
     *            : logged in user.
     * @param polKey
     *            : policy key.
     * @return list of expense type from database.
     * */
    public List<ExpenseType> loadExpenseTypeFromDB(String userId, String polKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ExpenseType> expTypeList = new ArrayList<ExpenseType>();
        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_EXPENSE_TYPE_USING_USER_ID_POL_KEY_ORDER, new String[] {
                userId, polKey });
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    ExpenseType dbHelperExpTyp = new ExpenseType(cursor);
                    expTypeList.add(dbHelperExpTyp);

                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return expTypeList;
    }

    /**
     * Update most recently used expense type
     * 
     * @param userId
     * @param expKey
     * @param polKey
     * @param count
     * @param time
     * @return true : if successful update.
     */
    public boolean updateExpenseType(String userId, String expKey, String polKey, int count, Calendar time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result = false;
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, userId);
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_KEY, expKey);
        contVals.put(MobileDatabaseHelper.COLUMN_POL_KEY, polKey);
        if (time != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_LAST_USED, FormatUtil.XML_DF.format(time.getTime()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_LAST_USED);
        }

        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_POL_KEY_COUNT, new String[] { userId, expKey, polKey });
        int dataRecord = cursor.getCount();
        if (dataRecord > 0) {
            if (cursor.moveToFirst()) {
                Integer userCount = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USE_COUNT));
                Integer updateCount = Integer.valueOf(userCount.intValue() + count);
                contVals.put(MobileDatabaseHelper.COLUMN_USE_COUNT, updateCount);
                int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_EXPENSE_TYPE, contVals,
                        MobileDatabaseHelper.WHERE_UPDATE_EXPENSE_TYPE_ROW, new String[] { userId, expKey, polKey });
                result = (numRowsAffected > 0);
            }
        } else {
            Log.e(Const.LOG_TAG,
                    CLS_TAG
                            + ".MobileDatabase.updateExpenseType: no record match or more than one record is available which is invaild.");
        }
        cursor.close();
        return result;

    }

    /**
     * insert gov document detail into database.
     * 
     * @param userID
     *            : user id
     * @param travID
     *            : travller id
     * @param docName
     *            :document name
     * @param initialValues
     *            : content values
     * @return : successfull returned
     */
    public boolean insertGovDocument(String userId, String travId, String docName, String docType,
            ContentValues initialValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result = false;
        int numRowsAffected = db.delete(MobileDatabaseHelper.TABLE_GOV_DOCUMENT_DETAIL,
                MobileDatabaseHelper.WHERE_UPDATE_DELETE_DOC_DETAIL, new String[] { userId, travId, docName, docType });
        result = (numRowsAffected != 0);
        result = insertGovDocDetail(db, initialValues);
        return result;
    }

    /** final insertion call */
    private boolean insertGovDocDetail(SQLiteDatabase db, ContentValues initialValues) {
        long rowId = db.insert(MobileDatabaseHelper.TABLE_GOV_DOCUMENT_DETAIL, null, initialValues);
        boolean result = (rowId != -1L);
        return result;
    }

    /**
     * load gov document detail from database
     * 
     * @param userId
     *            : current logged in user
     * @param travID
     *            : traveler id
     * @param docName
     *            : document name
     * @param docType
     *            : document type
     * */
    public Cursor loadGovDocument(String userId, String travID, String docName, String docType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_GOV_DOC_DETAIL, new String[] { userId, travID, docName,
                docType });
        return cursor;
    }

    /**
     * insert gov stamp document-required-info into database.
     * 
     * @param userId
     *            : user id
     * @param travId
     *            : travller id
     * @param docName
     *            :document name
     * @param docType
     *            :document type
     * @param initialValues
     *            : content values
     * @return : successful insertion returns true
     */
    public boolean insertStampDocumentReqInfo(ContentValues initialValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String travId = (String) initialValues.get(MobileDatabaseHelper.COLUMN_GOV_TRAVID);
        String stampName = (String) initialValues.get(MobileDatabaseHelper.COLUMN_GOV_STAMP_NAME);
        String docUserId = (String) initialValues.get(MobileDatabaseHelper.COLUMN_GOV_REQUIRED_REASON_USERID);
        boolean result = false;
        db.delete(MobileDatabaseHelper.TABLE_GOV_DOC_STAMP_REQ_REASON,
                MobileDatabaseHelper.WHERE_INSERT_STAMP_DOC_REQ_INFO, new String[] { docUserId, travId, stampName });
        result = insertStampDocumentReq(db, initialValues);
        return result;
    }

    /** final insertion call for stamp document requirement info */
    private boolean insertStampDocumentReq(SQLiteDatabase db, ContentValues initialValues) {
        long rowId = db.insert(MobileDatabaseHelper.TABLE_GOV_DOC_STAMP_REQ_REASON, null, initialValues);
        boolean result = (rowId != -1L);
        return result;
    }

    /**
     * load stamp document requirement info.
     * 
     * @param docUserId
     *            : current logged in user
     * @param travId
     *            : traveler id
     * @param stampName
     *            : stamp name;
     * @return Cursor : database cursor.
     * */
    public Cursor loadStampDocumentRequirementInfo(String docUserId, String travId, String stampName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_STAMP_DOC_REQ_INFO, new String[] { docUserId, travId,
                stampName });
        return cursor;
    }

    /**
     * insert gov warning messages
     * 
     * @param initialValues
     *            : content values
     * @return : successful returned
     */
    public boolean insertGovWarningMsgs(ContentValues initialValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result = false;
        int numRowsAffected = db.delete(MobileDatabaseHelper.TABLE_GOV_MESSAGES, null, null);
        result = (numRowsAffected != 0);
        result = insertGovWarningMsgs(db, initialValues);
        return result;
    }

    /** final insertion call */
    private boolean insertGovWarningMsgs(SQLiteDatabase db, ContentValues initialValues) {
        long rowId = db.insert(MobileDatabaseHelper.TABLE_GOV_MESSAGES, null, initialValues);
        boolean result = (rowId != -1L);
        return result;
    }

    /**
     * load gov warning messages from databases.
     * */
    public Cursor loadGovWarningMsgs() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_GOV_MESGS, null);
        return cursor;
    }

    /**
     * get offline expense count
     * 
     * @param userId
     * */
    public int getOfflineExpenseCount(String userId) {
        int count = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.COUNT_OFFLINE_EXPENSES_SQL, new String[] { userId });
            if (results.moveToFirst()) {
                try {
                    count = results.getInt(0);
                } catch (Exception e) {
                    // Log it but get out clean.
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getOfflineExpenseCount: exception thrown while retrieving count ",
                            e);
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return count;
    }

    public int getOfflineReceiptCount(String userId) {
        int count = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor results = null;
        try {
            results = db.rawQuery(MobileDatabaseHelper.COUNT_OFFLINE_RECEIPTS_SQL, null);
            if (results.moveToFirst()) {
                try {
                    count = results.getInt(0);
                } catch (Exception e) {
                    // Log it but get out clean.
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".getOfflineReceiptsCount: exception thrown while retrieving count ", e);
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return count;
    }

    /**
     * Insert MRU listitem in DB
     * 
     * @param listItem
     *            : listitem needs to be inserted.
     * @return : whether insert operation is successful or not.
     */
    public boolean insertListItemToDB(ListItem listItem) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contVals = listItem.getContentValuesForListItem(listItem);
        if (listItem.getLastUsed() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_LAST_USED,
                    FormatUtil.XML_DF.format(listItem.getLastUsed().getTime()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_LAST_USED);
        }
        boolean result = false;
        if (!isDuplicate(db, listItem)) {
            long rowId = db.insert(MobileDatabaseHelper.TABLE_MRU, null, contVals);
            result = (rowId != -1L);
        } else {
            result = updateListItem(listItem, listItem.fieldId);
        }
        return result;
    }

    /**
     * Check MRU listitem is available or not in DB
     * 
     * @param db
     * @param listItem
     * @return : Whether MRU listitem is available or not in DB
     */
    private boolean isDuplicate(SQLiteDatabase db, ListItem listItem) {
        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_MRU_FIELD_ID_CODE, new String[] { listItem.getUserID(),
                listItem.code, listItem.fieldId });
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    /**
     * load MRU list items from DB
     * 
     * @param userId
     *            : user id
     * @param fieldId
     *            : field id
     * @return : return MRU list.
     */
    public List<ListItem> loadListItemFromDB(String userId, String fieldId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ListItem> crnTypeList = new ArrayList<ListItem>();
        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_MRU_FIELD_VALUE, new String[] { userId, fieldId });
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    ListItem dbHelperCrnTyp = new ListItem(cursor);
                    crnTypeList.add(dbHelperCrnTyp);

                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return crnTypeList;
    }

    /**
     * Update MRU List items
     * 
     * @param listItem
     *            : listItem which needs to be approved
     * @param fieldID
     *            : fieldId of listItem
     */
    public boolean updateListItem(ListItem listItem, String fieldId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result = false;
        ContentValues contVals = listItem.getContentValuesForListItem(listItem);
        if (listItem.getLastUsed() != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_LAST_USED,
                    FormatUtil.XML_DF.format(listItem.getLastUsed().getTime()));
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_LAST_USED);
        }

        String userId = contVals.getAsString(MobileDatabaseHelper.COLUMN_USER_ID);

        Cursor cursor = db.rawQuery(MobileDatabaseHelper.SELECT_MRU_FIELD_ID_CODE, new String[] { userId,
                listItem.code, fieldId });
        int dataRecord = cursor.getCount();
        if (dataRecord > 0) {
            if (cursor.moveToFirst()) {
                String fieldIDFromCursor = cursor
                        .getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_FIELD_ID));
                if (fieldIDFromCursor.equalsIgnoreCase(fieldId)) {
                    Integer userCount = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USE_COUNT));
                    Integer updateCount = Integer.valueOf(userCount.intValue() + 1);
                    contVals.put(MobileDatabaseHelper.COLUMN_USE_COUNT, updateCount);
                    int numRowsAffected = db.update(MobileDatabaseHelper.TABLE_MRU, contVals,
                            MobileDatabaseHelper.WHERE_UPDATE_FIELD_VALUE_CODE, new String[] { userId, fieldId,
                                    listItem.code });
                    result = (numRowsAffected > 0);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".MobileDatabase.updateCrnType: no record match ");
        }
        cursor.close();
        return result;

    }

    // public static void DEBUGdumpOfflineExpenses(String event, String userId) {
    // ConcurCore app = (ConcurCore) ConcurCore.getContext();
    // MobileDatabase mdb = app.getService().getMobileDatabase();
    // mdb.DEBUGoutputOfflineExpenses(event, userId);
    // }
    //
    // public void DEBUGoutputOfflineExpenses(String event, String userId) {
    //
    // Log.d(Const.LOG_TAG, "Offline expenses // " + event);
    // Log.d(Const.LOG_TAG, "DB contents");
    // SQLiteDatabase db = dbHelper.getReadableDatabase();
    // Cursor results = null;
    // try {
    // results = db.rawQuery(MobileDatabaseHelper.SELECT_EXPENSE_ENTRY_SQL, new String[] { userId });
    // if (results.moveToFirst()) {
    // try {
    // String out = results.getString(results.getColumnIndex(MobileDatabaseHelper.COLUMN_STATUS)) + " "
    // + results.getString(results.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_KEY)) + " "
    // + results.getDouble(results.getColumnIndex(MobileDatabaseHelper.COLUMN_TRANSACTION_AMOUNT));
    // Log.d(Const.LOG_TAG, out);
    // } catch (Exception e) {
    // // Log it but get out clean.
    // Log.e(Const.LOG_TAG, CLS_TAG + ".DEBUG", e);
    // }
    // }
    // } finally {
    // if (results != null) {
    // results.close();
    // }
    // }
    //
    // Log.d(Const.LOG_TAG, "Cache MEs");
    // ConcurCore app = (ConcurCore) ConcurCore.getContext();
    // IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
    // ArrayList<MobileEntry> cacheList = expEntCache.getMobileEntries();
    // if (cacheList != null && cacheList.size() > 0) {
    // for (MobileEntry me : cacheList) {
    // String out = me.getStatus() + " " + me.getExpKey() + " " + me.getTransactionAmount();
    // Log.d(Const.LOG_TAG, out);
    // }
    // }
    // }
    //
}
