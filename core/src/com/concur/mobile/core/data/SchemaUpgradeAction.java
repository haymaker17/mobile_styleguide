/**
 * 
 */
package com.concur.mobile.core.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * @author andy
 */
public abstract class SchemaUpgradeAction {

    private static final String CLS_TAG = SchemaUpgradeAction.class.getSimpleName();

    /**
     * Contains the name of the column returned in the "PRAGMA table_info" result set that contains the column name.
     */
    private static String PRAGMA_TABLE_INFO_COLUMN_NAME = "name";

    protected SQLiteDatabase db;

    protected int oldVersion;

    protected int newVersion;

    /**
     * Constructs an instance of <code>DBUpgradeAction</code> to perform a database upgrade.
     * 
     * @param db
     *            contains a reference to the database being upgraded.
     * @param oldVersion
     *            contains the old database version.
     * @param newVersion
     *            contains the new database version.
     */
    protected SchemaUpgradeAction(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.db = db;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    /**
     * Will perform the upgrade action.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    protected abstract boolean upgrade();

    /**
     * Determines whether a column exists within a specific table using the <code>PRAGMA table_info(<table name>)</code> SQLite
     * statement.
     * 
     * @param tableName
     *            contains the table name.
     * @param columnName
     *            contains the column name.
     * @return returns <code>true</code> if the column is located; <code>false</code> otherwise.
     */
    protected boolean columnExists(String tableName, String columnName) {

        boolean retVal = false;

        StringBuilder strBldr = new StringBuilder("PRAGMA table_info(");
        strBldr.append(tableName);
        strBldr.append(")");
        String pragmaSQL = strBldr.toString();

        Cursor results = null;
        try {
            results = db.rawQuery(pragmaSQL, (String[]) null);
            if (results.moveToFirst()) {
                do {
                    // Grab the table name.
                    int colInd = results.getColumnIndex(PRAGMA_TABLE_INFO_COLUMN_NAME);
                    if (colInd != -1) {
                        if (!results.isNull(colInd)) {
                            String pragmaColumnName = results.getString(colInd);
                            if (pragmaColumnName != null && pragmaColumnName.equalsIgnoreCase(columnName)) {
                                retVal = true;
                                break;
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".columnExists: unable to locate column index for column '"
                                + PRAGMA_TABLE_INFO_COLUMN_NAME + "'.");
                    }
                } while (results.moveToNext());
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }
        return retVal;
    }

}
