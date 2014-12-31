/**
 * 
 */
package com.concur.mobile.platform.config.provider;

import net.sqlcipher.SQLException;
import android.content.ContentValues;
import android.util.Log;

import com.concur.mobile.platform.provider.BulkInserter;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.provider.PlatformSQLiteStatement;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;

/**
 * An implementation of <code>BulkInserter</code> for performing bulk insertion of currency data.
 * 
 * @author andrewk
 */
public class CurrencyBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "CurrencyBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.BulkInserter#prepareSQLiteStatement(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Config.CurrencyColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Config.CurrencyColumns.CRN_CODE);
            strBldr.append(",");
            strBldr.append(Config.CurrencyColumns.CRN_NAME);
            strBldr.append(",");
            strBldr.append(Config.CurrencyColumns.DECIMAL_DIGITS);
            strBldr.append(",");
            strBldr.append(Config.CurrencyColumns.IS_REIMBURSEMENT);
            strBldr.append(",");
            strBldr.append(Config.CurrencyColumns.USER_ID);

            strBldr.append(") values (?,?,?,?,?)");
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.BulkInserter#bindSQLiteStatmentValues(android.database.sqlite.SQLiteStatement,
     * android.content.ContentValues)
     */
    @Override
    public void bindSQLiteStatmentValues(PlatformSQLiteStatement sqlStmt, ContentValues values) {
        if (sqlStmt != null) {
            if (values != null) {

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 1, values,
                        Config.CurrencyColumns.CRN_CODE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values,
                        Config.CurrencyColumns.CRN_NAME);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 3, values,
                        Config.CurrencyColumns.DECIMAL_DIGITS);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 4, values,
                        Config.CurrencyColumns.IS_REIMBURSEMENT);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 5, values,
                        Config.CurrencyColumns.USER_ID);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }

    }

}
