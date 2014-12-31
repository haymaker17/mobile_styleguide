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
 * An implementation of <code>BulkInserter</code> support bulk insertion of expense types.
 * 
 * @author andrewk
 */
public class ExpenseTypeBulkInserter implements BulkInserter {

    private static final String CLS_TAG = "ExpenseTypeBulkInserter";

    /**
     * Contains a reference to the <code>SQLiteStatment</code> object used for bulk insertion.
     */
    private PlatformSQLiteStatement sqlStatement;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.BulkInserter#prepareSQLiteStatement()
     */
    @Override
    public PlatformSQLiteStatement prepareSQLiteStatement(PlatformSQLiteDatabase db) {
        if (sqlStatement == null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("insert into ");
            strBldr.append(Config.ExpenseTypeColumns.TABLE_NAME);
            strBldr.append(" (");
            strBldr.append(Config.ExpenseTypeColumns.EXP_CODE);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.EXP_KEY);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.EXP_NAME);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.FORM_KEY);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.HAS_POST_AMT_CALC);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.HAS_TAX_FORM);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ITEMIZATION_STYLE);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ITEMIZATION_TYPE);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.PARENT_EXP_KEY);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.PARENT_EXP_NAME);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.VENDOR_LIST_KEY);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.ALLOW_NO_SHOWS);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT);
            strBldr.append(",");
            strBldr.append(Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS);
            strBldr.append(",");
            strBldr.append(Config.SystemConfigColumns.USER_ID);

            strBldr.append(") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
                        Config.ExpenseTypeColumns.EXP_CODE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 2, values,
                        Config.ExpenseTypeColumns.EXP_KEY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 3, values,
                        Config.ExpenseTypeColumns.EXP_NAME);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 4, values,
                        Config.ExpenseTypeColumns.FORM_KEY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 5, values,
                        Config.ExpenseTypeColumns.HAS_POST_AMT_CALC);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 6, values,
                        Config.ExpenseTypeColumns.HAS_TAX_FORM);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 7, values,
                        Config.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 8, values,
                        Config.ExpenseTypeColumns.ITEMIZATION_FORM_KEY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 9, values,
                        Config.ExpenseTypeColumns.ITEMIZATION_STYLE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 10, values,
                        Config.ExpenseTypeColumns.ITEMIZATION_TYPE);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 11, values,
                        Config.ExpenseTypeColumns.PARENT_EXP_KEY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 12, values,
                        Config.ExpenseTypeColumns.PARENT_EXP_NAME);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 13, values,
                        Config.ExpenseTypeColumns.SUPPORTS_ATTENDEES);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.LONG, 14, values,
                        Config.ExpenseTypeColumns.VENDOR_LIST_KEY);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 15, values,
                        Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 16, values,
                        Config.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 17, values,
                        Config.ExpenseTypeColumns.ALLOW_NO_SHOWS);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 18, values,
                        Config.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 19, values,
                        Config.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.BOOLEAN, 20, values,
                        Config.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 21, values,
                        Config.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS);

                ContentUtils.bindSqlStatementValues(sqlStmt, ContentUtils.StatementBindTypeEnum.STRING, 22, values,
                        Config.ExpenseTypeColumns.USER_ID);

            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: values is null.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".bindSQLiteStatementValues: sqlStmt is null.");
        }
    }
}
