package com.concur.mobile.platform.provider;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.concur.mobile.platform.util.Const;

public abstract class AbstractPlatformSQLiteDatabase implements PlatformSQLiteDatabase {

    private static final String CLS_TAG = "AbstractPlatformSQLiteDatabase";

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#enableForeignKeySupport()
     */
    public boolean enableForeignKeySupport() {

        boolean retVal = false;

        try {
            if (!isReadOnly()) {
                // Enable foreign key constraints
                execSQL("PRAGMA foreign_keys=ON;");
                retVal = true;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".enableForeignKeySupport: db is read-only.");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".enableForeignKeySupport: ", sqlExc);
        }

        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#disableForeignKeySupport()
     */
    public boolean disableForeignKeySupport() {

        boolean retVal = false;

        try {
            if (!isReadOnly()) {
                // Disable foreign key constraints
                execSQL("PRAGMA foreign_keys=OFF;");
                retVal = true;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".disableForeignKeySupport: db is read-only.");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".disableForeignKeySupport: ", sqlExc);
        }

        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformSQLiteDatabase#getSQLiteVersion()
     */
    public String getSQLiteVersion() {

        Cursor results = null;
        String version = null;
        try {
            results = rawQuery("SELECT sqlite_version() AS 'SQLite Version';", (String[]) null);
            if (results.moveToFirst()) {
                // Grab the table name.
                int colInd = results.getColumnIndex("SQLite Version");
                if (colInd != -1) {
                    if (!results.isNull(colInd)) {
                        version = results.getString(colInd);
                        Log.d(Const.LOG_TAG, CLS_TAG + ".outputSQLiteVersion: version -> '" + version + "'.");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".columnExists: unable to locate column index.");
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        return version;
    }

}
