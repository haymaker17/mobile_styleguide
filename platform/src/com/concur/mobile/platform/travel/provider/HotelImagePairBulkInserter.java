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
 * An implementation of <code>BulkInserter</code> for the purposes of performing bulk insertion for hotel image pairs.
 * <p/>
 * Created by RatanK
 */
public class HotelImagePairBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "HotelImagePairBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Travel.HotelImagePairColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Travel.HotelImagePairColumns.HOTEL_DETAIL_ID);
            strBldr.append(",");
            strBldr.append(Travel.HotelImagePairColumns.THUMBNAIL_URL);
            strBldr.append(",");
            strBldr.append(Travel.HotelImagePairColumns.IMAGE_URL);
            strBldr.append(") values (?,?,?)");
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
                        Travel.HotelImagePairColumns.HOTEL_DETAIL_ID);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values,
                        Travel.HotelImagePairColumns.THUMBNAIL_URL);
                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 3, values,
                        Travel.HotelImagePairColumns.IMAGE_URL);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }

    }
}
