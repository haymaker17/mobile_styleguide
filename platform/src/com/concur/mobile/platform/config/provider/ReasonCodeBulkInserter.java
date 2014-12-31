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
 * An implementation of <code>BulkInserter</code> for performing bulk insertion of reason code information.
 * 
 * @author andrewk
 */
public class ReasonCodeBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "OfficeLocationBulkInserter";

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
            strBldr.append(Config.ReasonCodeColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Config.ReasonCodeColumns.TYPE);
            strBldr.append(",");
            strBldr.append(Config.ReasonCodeColumns.DESCRIPTION);
            strBldr.append(",");
            strBldr.append(Config.ReasonCodeColumns.ID);
            strBldr.append(",");
            strBldr.append(Config.ReasonCodeColumns.VIOLATION_TYPE);
            strBldr.append(",");
            strBldr.append(Config.ReasonCodeColumns.USER_ID);
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
                        Config.ReasonCodeColumns.TYPE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values,
                        Config.ReasonCodeColumns.DESCRIPTION);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 3, values,
                        Config.ReasonCodeColumns.ID);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 4, values,
                        Config.ReasonCodeColumns.VIOLATION_TYPE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 5, values,
                        Config.ReasonCodeColumns.USER_ID);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }

    }

}
