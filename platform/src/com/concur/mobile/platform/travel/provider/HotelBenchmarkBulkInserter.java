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
 * An implementation of <code>BulkInserter</code> for the purposes of performing bulk insertion for hotel benchmarks.
 * <p/>
 * Created by RatanK
 */
public class HotelBenchmarkBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "HotelBenchmarkBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Travel.HotelBenchmarkColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Travel.HotelBenchmarkColumns.HOTEL_SEARCH_RESULT_ID);
            strBldr.append(",");
            strBldr.append(Travel.HotelBenchmarkColumns.LOCATION_NAME);
            strBldr.append(",");
            strBldr.append(Travel.HotelBenchmarkColumns.CRN_CODE);
            strBldr.append(",");
            strBldr.append(Travel.HotelBenchmarkColumns.PRICE);
            strBldr.append(",");
            strBldr.append(Travel.HotelBenchmarkColumns.SUB_DIV_CODE);
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

    @Override
    public void bindSQLiteStatmentValues(PlatformSQLiteStatement sqlStmt, ContentValues values) {
        if (sqlStmt != null) {
            if (values != null) {

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 1, values,
                        Travel.HotelBenchmarkColumns.HOTEL_SEARCH_RESULT_ID);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values,
                        Travel.HotelBenchmarkColumns.LOCATION_NAME);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 3, values,
                        Travel.HotelBenchmarkColumns.CRN_CODE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.DOUBLE, 3, values,
                        Travel.HotelBenchmarkColumns.PRICE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 5, values,
                        Travel.HotelBenchmarkColumns.SUB_DIV_CODE);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }
    }
}
