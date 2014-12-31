/**
 * 
 */
package com.concur.mobile.platform.config.provider;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.platform.config.provider.Config.ClientDataColumns;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;

/**
 * Provides a model of client data information.
 */
public class ClientData {

    private static final String CLS_TAG = "ClientData";

    private static final Boolean DEBUG = Boolean.TRUE;

    /**
     * Contains the content provider client data id.
     */
    public int contentId;

    /**
     * Contains the user id.
     */
    public String userId;

    /**
     * Contains the key.
     */
    public String key;

    /**
     * Contains a text value.
     */
    public String text;

    /**
     * Contains a binary value.
     */
    public byte[] blob;

    // Contains a reference to the content resolver.
    private ContentResolver resolver;

    /**
     * Constructs an instance of <code>ClientData</code> given a context.
     * 
     * @param context
     *            contains a reference to an application context.
     */
    public ClientData(Context context) {
        resolver = context.getContentResolver();
    }

    /**
     * Will update the current values in this client data object.
     * 
     * @return returns <code>true</code> upon success; otherwise, <code>false</code> is returned.
     */
    public boolean update() {
        boolean result = false;

        final String MTAG = CLS_TAG + ".update: ";

        // Perform an assertion check.
        try {
            Assert.assertNotNull(MTAG + "userId is null!", userId);
            Assert.assertNotNull(MTAG + "key is null!", key);

            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.ClientDataColumns.KEY);
            strBldr.append(" = ? AND ");
            strBldr.append(Config.ClientDataColumns.USER_ID);
            strBldr.append(" = ?");
            String whereClause = strBldr.toString();
            String[] whereArgs = { key, userId };

            ContentValues values = new ContentValues();

            // Set the text value.
            ContentUtils.putValue(values, ClientDataColumns.VALUE_TEXT, text);
            // Set the blob value.
            ContentUtils.putValue(values, ClientDataColumns.VALUE_BLOB, blob);

            int rowsAffected = resolver.update(Config.ClientDataColumns.CONTENT_URI, values, whereClause, whereArgs);
            if (rowsAffected == 0) {
                // Rows affected is 0, try an insertion.
                // Set the key.
                ContentUtils.putValue(values, ClientDataColumns.KEY, key);
                // Set user id.
                ContentUtils.putValue(values, ClientDataColumns.USER_ID, userId);
                Uri uri = resolver.insert(Config.ClientDataColumns.CONTENT_URI, values);
                if (uri != null) {
                    String contentIdStr = uri.getLastPathSegment();
                    try {
                        contentId = Integer.parseInt(contentIdStr);
                    } catch (NumberFormatException nfe) {
                        Log.e(Const.LOG_TAG, MTAG + "last path segment is a non-integer '" + contentIdStr + "'.");
                    }
                    result = true;
                } else {
                    Log.e(Const.LOG_TAG, MTAG + " update affected 0 rows, insert failed!");
                }
            } else {
                result = true;
            }

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, afe.getMessage(), afe);
        }

        return result;
    }

    /**
     * Will load values into this client data object based on <code>userId</code> and <code>key</code>
     * 
     * @return returns <code>true</code> upon success; otherwise, <code>false</code> is returned.
     */
    public boolean load() {
        boolean result = false;

        final String MTAG = CLS_TAG + ".load: ";

        // Perform an assertion check.
        try {
            Assert.assertNotNull(MTAG + "userId is null!", userId);
            Assert.assertNotNull(MTAG + "key is null!", key);

            Cursor cursor = null;
            try {
                String[] clientDataColumns = { Config.ClientDataColumns._ID, Config.ClientDataColumns.KEY,
                        Config.ClientDataColumns.VALUE_TEXT, Config.ClientDataColumns.VALUE_BLOB };

                StringBuilder strBldr = new StringBuilder();
                strBldr.append(Config.ClientDataColumns.KEY);
                strBldr.append(" = ? AND ");
                strBldr.append(Config.ClientDataColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = { key, userId };

                cursor = resolver.query(Config.ClientDataColumns.CONTENT_URI, clientDataColumns, where, whereArgs,
                        Config.SessionColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        // Set the content id.
                        contentId = CursorUtil.getIntValue(cursor, Config.ClientDataColumns._ID);
                        // Set the text value.
                        text = CursorUtil.getStringValue(cursor, Config.ClientDataColumns.VALUE_TEXT);
                        // Set the blob value.
                        blob = CursorUtil.getBlobValue(cursor, Config.ClientDataColumns.VALUE_BLOB);

                        result = true;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, afe.getMessage(), afe);
        }

        return result;
    }

    /**
     * Will delete this client data object based on <code>userId</code> and <code>key</code>
     * 
     * @return returns <code>true</code> upon success; otherwise, <code>false</code> is returned.
     */
    public boolean delete() {
        boolean result = false;

        final String MTAG = CLS_TAG + ".delete: ";

        // Perform an assertion check.
        try {
            Assert.assertNotNull(MTAG + "userId is null!", userId);
            Assert.assertNotNull(MTAG + "key is null!", key);

            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Config.ClientDataColumns.KEY);
            strBldr.append(" = ? AND ");
            strBldr.append(Config.ClientDataColumns.USER_ID);
            strBldr.append(" = ?");
            String whereClause = strBldr.toString();
            String[] whereArgs = { key, userId };

            // Punt all system configuration information.
            int rowsAffected = resolver.delete(Config.ClientDataColumns.CONTENT_URI, whereClause, whereArgs);
            if (DEBUG) {
                Log.d(Const.LOG_TAG, MTAG + "deleted " + Integer.toString(rowsAffected) + " client data rows.");
            }

            result = (rowsAffected >= 1);

        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, afe.getMessage(), afe);
        }

        return result;
    }

}
