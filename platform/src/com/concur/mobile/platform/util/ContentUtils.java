package com.concur.mobile.platform.util;

import java.text.DateFormat;
import java.util.Calendar;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteStatement;

/**
 * A set of utility methods for putting values into a <code>ContentValue</code> object.
 */
public class ContentUtils {

    private static final String CLS_TAG = "ContentUtils";

    /**
     * An enumeration used to specify a SQLiteStatement argument bind type.
     */
    public static enum StatementBindTypeEnum {
        BLOB, DOUBLE, LONG, STRING, BOOLEAN, NULL
    };

    private ContentUtils() {
    }

    /**
     * Will put an <code>Integer</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, Integer value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>Long</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, Long value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>Double</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, Double value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>Float</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, Float value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>String</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, String value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>Boolean</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, Boolean value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>byte[]</code> value into a <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param value
     *            contains the value.
     */
    public static void putValue(ContentValues values, String key, byte[] value) {
        if (value != null) {
            values.put(key, value);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will put a <code>Calendar</code> value into <code>values</code>.
     * 
     * @param values
     *            contains the <code>ContentValues</code> object.
     * @param key
     *            contains the key.
     * @param dateFormat
     *            contains an instance of <code>DateFormat</code> used to format <code>value</code>.
     * @param value
     *            contains the calendar value object.
     */
    public static void putValue(ContentValues values, String key, DateFormat dateFormat, Calendar value) {
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat is null!");
        }
        if (value != null) {
            String formattedDate = null;
            formattedDate = Format.safeFormatCalendar(dateFormat, value);
            values.put(key, formattedDate);
        } else {
            values.putNull(key);
        }
    }

    /**
     * Will check for whether <code>key</code> is in <code>values</code> and non-null. If so, then <code>true</code> is returned;
     * otherwise <code>false</code> is returned.
     * 
     * @param values
     *            contains a reference to a <code>ContentValues</code> object.
     * @param key
     *            contains the key to check for.
     * @return returns <code>true</code> if <code>key</code> is in <code>values</code> and non-null; otherwise <code>false</code>
     *         is returned.
     */
    public static boolean isNull(ContentValues values, String key) {
        boolean isNull = false;
        if (key != null && values != null && values.containsKey(key)) {
            isNull = (values.get(key) == null);
        } else {
            isNull = true;
        }
        return isNull;
    }

    /**
     * Will bind to <code>sqlStmt</code> at index <code>bindIndex</code> the value of type <code>bindType</code> in
     * <code>values</code> keyed by <code>key</code>. <br>
     * <br>
     * <b>NOTE:</b>This method will check whether the value in <code>values</code> keyed by <code>key</code> is <code>NULL</code>.
     * If so, it will override <code>bindType</code> and set it to <code>StatementBindTypeEnum.NULL</code>.
     * 
     * @param sqlStmt
     *            contains a reference to a <code>SQLiteStatement</code>.
     * @param bindType
     *            contains a reference to <code>StatementBindTypeEnum</code> indicating the type of argument.
     * @param bindIndex
     *            contains the 1-based index at which the value will be bound in <code>sqlStmt</code>.
     * @param values
     *            contains the content values.
     * @param key
     *            contains the key for the value.
     */
    public static void bindSqlStatementValues(PlatformSQLiteStatement sqlStmt, StatementBindTypeEnum bindType,
            int bindIndex, ContentValues values, String key) {

        final String MTAG = CLS_TAG + ".bindSqlStatementValues: ";

        try {

            // Assertions.
            Assert.assertNotNull(MTAG + "sqlStmt is null!", sqlStmt);
            Assert.assertNotNull(MTAG + "values is null!", values);
            Assert.assertNotNull(MTAG + "key is null!", key);
            Assert.assertNotNull(MTAG + "bindType is null!", bindType);

            // Set 'bindType' to 'NULL' if not already and "values.get(key) == null".
            if (bindType != StatementBindTypeEnum.NULL && isNull(values, key)) {
                bindType = StatementBindTypeEnum.NULL;
            }

            switch (bindType) {
            case BLOB: {
                sqlStmt.bindBlob(bindIndex, values.getAsByteArray(key));
                break;
            }
            case DOUBLE: {
                sqlStmt.bindDouble(bindIndex, values.getAsDouble(key));
                break;
            }
            case LONG: {
                sqlStmt.bindLong(bindIndex, values.getAsLong(key));
                break;
            }
            case STRING: {
                sqlStmt.bindString(bindIndex, values.getAsString(key));
                break;
            }
            case BOOLEAN: {
                Boolean bool = values.getAsBoolean(key);
                long boolLongVal = 0L;
                if (bool != null) {
                    boolLongVal = (bool) ? 1 : 0L;
                } else {
                    boolLongVal = 0L;
                }
                sqlStmt.bindLong(bindIndex, boolLongVal);
                break;
            }
            case NULL: {
                sqlStmt.bindNull(bindIndex);
                break;
            }
            }
        } catch (AssertionFailedError afe) {
            Log.e(Const.LOG_TAG, afe.getMessage(), afe);
        }
    }

    /**
     * Will perform a content Uri look-up based on querying the content resolver against a content directory Uri for the value of
     * a particular column.
     * 
     * @param context
     *            contains an application context.
     * @param contentDirectoryUri
     *            contains a directory Uri.
     * @param keyColumn
     *            contains a column name.
     * @param keyValue
     *            contains the column value.
     * @return returns an instance of <code>Uri</code> if found; otherwise returns <code>null</code>.
     */
    public static Uri getContentUri(Context context, Uri contentDirectoryUri, String keyColumn, String keyValue) {

        Uri retVal = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder selection = new StringBuilder();
            selection.append(keyColumn);
            selection.append(" = ?");
            String[] contentColumns = { BaseColumns._ID };
            String[] selectionArgs = { keyValue };
            cursor = resolver.query(contentDirectoryUri, contentColumns, selection.toString(), selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Long rowId = CursorUtil.getLongValue(cursor, BaseColumns._ID);
                    if (rowId != null) {
                        retVal = ContentUris.withAppendedId(contentDirectoryUri, rowId);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retVal;
    }

    /**
     * Will perform a content Uri look-up based on querying the content resolver against a content directory Uri for the value of
     * a particular column.
     * 
     * @param context
     *            contains an application context.
     * @param contentDirectoryUri
     *            contains a directory Uri.
     * @param keyColumns
     *            contains a list of column names.
     * @param keyValues
     *            contains a list of column value.
     * @return returns an instance of <code>Uri</code> if found; otherwise returns <code>null</code>.
     */
    public static Uri getContentUri(Context context, Uri contentDirectoryUri, String[] keyColumns, String[] keyValues) {

        Uri retVal = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            StringBuilder selection = new StringBuilder();
            String[] selectionArgs = null;
            if (keyColumns != null) {
                selectionArgs = new String[keyColumns.length];
                for (int keyColInd = 0; keyColInd < keyColumns.length; ++keyColInd) {
                    if (selection.length() > 0) {
                        selection.append(" AND ");
                    }
                    // Add the column.
                    selection.append(keyColumns[keyColInd]);
                    selection.append(" = ?");
                    // Add the selection value.
                    selectionArgs[keyColInd] = keyValues[keyColInd];
                }
            }

            String[] contentColumns = { BaseColumns._ID };
            cursor = resolver.query(contentDirectoryUri, contentColumns, selection.toString(), selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    Long rowId = CursorUtil.getLongValue(cursor, BaseColumns._ID);
                    if (rowId != null) {
                        retVal = ContentUris.withAppendedId(contentDirectoryUri, rowId);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retVal;
    }

    /**
     * Will determine whether or not a URI exists.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content Uri.
     * @return returns <code>true</code> if <code>contentUri</code> is valid; <code>false</code> otherwise.
     */
    public static boolean uriExists(Context context, Uri contentUri) {
        boolean uriExists = false;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] contentColumns = { BaseColumns._ID };
            cursor = resolver.query(contentUri, contentColumns, null, null, null);
            if (cursor != null) {
                uriExists = (cursor.moveToFirst());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return uriExists;
    }

    /**
     * Will retrieve a string value stored in a column given a content Uri.
     * 
     * @param context
     *            contains the application context.
     * @param contentUri
     *            contains the content Uri.
     * @param columnName
     *            contains the column name.
     * @return the value stored in <code>columnName</code> at the <code>contentUri</code>.
     */
    public static String getColumnStringValue(Context context, Uri contentUri, String columnName) {

        String columnValue = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] contentColumns = { columnName };
            cursor = resolver.query(contentUri, contentColumns, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    columnValue = CursorUtil.getStringValue(cursor, columnName);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return columnValue;
    }

    /**
     * Will retrieve a boolean value stored in a column given a content Uri.
     * 
     * @param context
     *            contains the application context.
     * @param contentUri
     *            contains the content Uri.
     * @param columnName
     *            contains the column name.
     * @return the value stored in <code>columnName</code> at the <code>contentUri</code>.
     */
    public static Boolean getColumnBooleanValue(Context context, Uri contentUri, String columnName) {

        Boolean columnValue = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] contentColumns = { columnName };
            cursor = resolver.query(contentUri, contentColumns, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    columnValue = CursorUtil.getBooleanValue(cursor, columnName);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return columnValue;
    }

    /**
     * Will retrieve a BLOG value stored in a column given a content Uri.
     * 
     * @param context
     *            contains the application context.
     * @param contentUri
     *            contains the content Uri.
     * @param columnName
     *            contains the column name.
     * @return the value stored in <code>columnName</code> at the <code>contentUri</code>.
     */
    public static byte[] getColumnBlobValue(Context context, Uri contentUri, String columnName) {

        byte[] columnValue = null;

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] contentColumns = { columnName };
            cursor = resolver.query(contentUri, contentColumns, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    columnValue = CursorUtil.getBlobValue(cursor, columnName);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return columnValue;
    }

}
