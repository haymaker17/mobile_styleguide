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
 * An implementation of <code>BulkInserter</code> for the purposes of performing bulk insertion for office location data.
 * 
 * @author andrewk
 */
public class OfficeLocationBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "OfficeLocationBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Config.OfficeLocationColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Config.OfficeLocationColumns.ADDRESS);
            strBldr.append(",");
            strBldr.append(Config.OfficeLocationColumns.CITY);
            strBldr.append(",");
            strBldr.append(Config.OfficeLocationColumns.COUNTRY);
            strBldr.append(",");
            strBldr.append(Config.OfficeLocationColumns.LAT);
            strBldr.append(",");
            strBldr.append(Config.OfficeLocationColumns.LON);
            strBldr.append(",");
            strBldr.append(Config.OfficeLocationColumns.STATE);
            strBldr.append(",");
            strBldr.append(Config.OfficeLocationColumns.USER_ID);
            strBldr.append(") values (?,?,?,?,?,?,?)");
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

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 1, values,
                        Config.OfficeLocationColumns.ADDRESS);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values,
                        Config.OfficeLocationColumns.CITY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 3, values,
                        Config.OfficeLocationColumns.COUNTRY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.DOUBLE, 4, values,
                        Config.OfficeLocationColumns.LAT);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.DOUBLE, 5, values,
                        Config.OfficeLocationColumns.LON);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 6, values,
                        Config.OfficeLocationColumns.STATE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 7, values,
                        Config.OfficeLocationColumns.USER_ID);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }

    }

}
