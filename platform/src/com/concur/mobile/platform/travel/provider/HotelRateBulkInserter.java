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
 * An implementation of <code>BulkInserter</code> for the purposes of performing bulk insertion for hotel rates.
 *
 * Created by RatanK.
 */
public class HotelRateBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "HotelRateBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Travel.HotelRateDetailColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.RATE_ID);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.AMOUNT);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.CURRENCY_CODE);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.SOURCE);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.ROOM_TYPE);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.DESCRIPTION);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.MAX_ENF_LEVEL);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.SELL_OPTIONS_URL);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.TRAVEL_POINTS);
            strBldr.append(",");
            strBldr.append(Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS);
            strBldr.append(") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 1, values, Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values, Travel.HotelRateDetailColumns.RATE_ID);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.DOUBLE, 3, values, Travel.HotelRateDetailColumns.AMOUNT);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 4, values, Travel.HotelRateDetailColumns.CURRENCY_CODE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 5, values, Travel.HotelRateDetailColumns.SOURCE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 6, values, Travel.HotelRateDetailColumns.ROOM_TYPE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 7, values, Travel.HotelRateDetailColumns.DESCRIPTION);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 8, values, Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 9, values, Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 10, values, Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 11, values, Travel.HotelRateDetailColumns.MAX_ENF_LEVEL);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 12, values, Travel.HotelRateDetailColumns.SELL_OPTIONS_URL);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 13, values, Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 14, values, Travel.HotelRateDetailColumns.TRAVEL_POINTS);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 15, values, Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }

    }
}
