package com.concur.mobile.platform.util;

import android.database.Cursor;
import android.util.Log;

/**
 * A set of utility methods for obtaining different types of values from a cursor object by column name.
 */
public class CursorUtil {

    private static final String CLS_TAG = "CursorUtil";

    private CursorUtil() {
    }

    /**
     * Will attempt to retrieve the value of column <code>columnName</code> as an <code>Integer</code> data type.
     * 
     * @param cursor
     *            contains the cursor.
     * @param columnName
     *            contains the column name.
     * @return an instance of <code>Integer</code> containing the value; otherwise, a null reference is returned.
     */
    public static Integer getIntValue(Cursor cursor, String columnName) {

        Integer value = null;

        int colInd = cursor.getColumnIndex(columnName);
        if (colInd != -1) {
            if (!cursor.isNull(colInd)) {
                try {
                    value = cursor.getInt(colInd);
                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getIntValue: exception reading column '" + columnName
                            + "' as integer", exc);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getIntValue: unable to locate column '" + columnName + "'.");
        }
        return value;
    }

    /**
     * Will attempt to retrieve the value of column <code>columnName</code> as an <code>Long</code> data type.
     * 
     * @param cursor
     *            contains the cursor.
     * @param columnName
     *            contains the column name.
     * @return an instance of <code>Long</code> containing the value; otherwise, a null reference is returned.
     */
    public static Long getLongValue(Cursor cursor, String columnName) {

        Long value = null;

        int colInd = cursor.getColumnIndex(columnName);
        if (colInd != -1) {
            if (!cursor.isNull(colInd)) {
                try {
                    value = cursor.getLong(colInd);
                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getLongValue: exception reading column '" + columnName
                            + "' as long", exc);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getLongValue: unable to locate column '" + columnName + "'.");
        }
        return value;
    }

    /**
     * Will attempt to retrieve the value of column <code>columnName</code> as an <code>Double</code> data type.
     * 
     * @param cursor
     *            contains the cursor.
     * @param columnName
     *            contains the column name.
     * @return an instance of <code>Double</code> containing the value; otherwise, a null reference is returned.
     */
    public static Double getDoubleValue(Cursor cursor, String columnName) {

        Double value = null;

        int colInd = cursor.getColumnIndex(columnName);
        if (colInd != -1) {
            if (!cursor.isNull(colInd)) {
                try {
                    value = cursor.getDouble(colInd);
                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getDoubleValue: exception reading column '" + columnName
                            + "' as double", exc);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getDoubleValue: unable to locate column '" + columnName + "'.");
        }
        return value;
    }

    /**
     * Will attempt to retrieve the value of column <code>columnName</code> as a <code>String</code> data type.
     * 
     * @param cursor
     *            contains the cursor.
     * @param columnName
     *            contains the column name.
     * @return an instance of <code>String</code> containing the value; otherwise, a null reference is returned.
     */
    public static String getStringValue(Cursor cursor, String columnName) {

        String value = null;

        int colInd = cursor.getColumnIndex(columnName);
        if (colInd != -1) {
            if (!cursor.isNull(colInd)) {
                try {
                    value = cursor.getString(colInd);
                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getStringValue: exception reading column '" + columnName
                            + "' as string", exc);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getStringValue: unable to locate column '" + columnName + "'.");
        }
        return value;
    }

    /**
     * Will attempt to retrieve the value of column <code>columnName</code> as a <code>Boolean</code> data type. <br>
     * <br>
     * NOTE: The implementation assumes that <code>columnName</code> in cursor is modeled as an integer data-type </br>
     * 
     * @param cursor
     *            contains the cursor.
     * @param columnName
     *            contains the column name.
     * @return an instance of <code>Boolean</code> containing the value; otherwise, a null reference is returned.
     */
    public static Boolean getBooleanValue(Cursor cursor, String columnName) {

        Boolean value = null;

        int colInd = cursor.getColumnIndex(columnName);
        if (colInd != -1) {
            if (!cursor.isNull(colInd)) {
                try {
                    value = (cursor.getInt(colInd) == 1) ? true : false;
                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getBooleanValue: exception reading column '" + columnName
                            + "' as boolean", exc);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getBooleanValue: unable to locate column '" + columnName + "'.");
        }
        return value;
    }

    /**
     * Will attempt to retrieve the value of column <code>columnName</code> as a <code>byte[]</code> data type. <br>
     * <br>
     * NOTE: The implementation assumes that <code>columnName</code> in cursor is modeled as a <code>BLOB</code> data-type </br>
     * 
     * @param cursor
     *            contains the cursor.
     * @param columnName
     *            contains the column name.
     * @return an instance of <code>byte[]</code> containing the value; otherwise, a null reference is returned.
     */
    public static byte[] getBlobValue(Cursor cursor, String columnName) {

        byte[] value = null;

        int colInd = cursor.getColumnIndex(columnName);
        if (colInd != -1) {
            if (!cursor.isNull(colInd)) {
                try {
                    value = cursor.getBlob(colInd);
                } catch (Exception exc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getBlobValue: exception reading column '" + columnName
                            + "' as blob", exc);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getBlobValue: unable to locate column '" + columnName + "'.");
        }
        return value;
    }

}
