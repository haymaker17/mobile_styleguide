package com.concur.mobile.platform.travel.provider;

import android.content.ContentValues;
import android.util.Log;

import com.concur.mobile.platform.provider.BulkInserter;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.provider.PlatformSQLiteStatement;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

import net.sqlcipher.SQLException;

/**
 * An implementation of <code>BulkInserter</code> for the purposes of performing bulk insertion for hotel violations.
 *
 * Created by RatanK
 */
public class HotelViolationBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "HotelViolationBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Travel.HotelViolationColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID);
            strBldr.append(",");
            strBldr.append(Travel.HotelViolationColumns.ENFORCEMENT_LEVEL);
            strBldr.append(",");
            strBldr.append(Travel.HotelViolationColumns.MESSAGE);
            strBldr.append(",");
            strBldr.append(Travel.HotelViolationColumns.VIOLATION_VALUE_ID);
            strBldr.append(") values (?,?,?,?)");
            try {
                sqlStatement = db.compileStatement(strBldr.toString());
            } catch (SQLException sqlExc) {
                Throwable throwable = sqlExc.getCause();
                if (throwable == null) {
                    throwable = sqlExc;
                }
                Log.e(Const.LOG_TAG, CLS_TAG + ".prepareSQLiteStatement: " + throwable);
            }
        }
        return sqlStatement;
    }

    @Override
    public void bindSQLiteStatmentValues(PlatformSQLiteStatement sqlStmt, ContentValues values) {
        if (sqlStmt != null) {
            if (values != null) {

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 1, values, Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values, Travel.HotelViolationColumns.ENFORCEMENT_LEVEL);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 3, values, Travel.HotelViolationColumns.MESSAGE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 4, values, Travel.HotelViolationColumns.VIOLATION_VALUE_ID);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }

    }
}
