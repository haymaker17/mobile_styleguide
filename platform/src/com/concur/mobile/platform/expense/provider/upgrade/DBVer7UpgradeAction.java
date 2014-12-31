package com.concur.mobile.platform.expense.provider.upgrade;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purpose of adding version 6 columns to the smart expenses table.
 * 
 * @author andrewk
 */
public class DBVer7UpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "DBVer7UpgradeAction";

    /**
     * Constructs an instance of <code>DBVer7UpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public DBVer7UpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {

        boolean upgraded = true;
        try {
            // Add TOTAL_DAYS.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.TOTAL_DAYS)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.TOTAL_DAYS + " INTEGER");
            }
            // Add PICK_UP_DATE.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.PICK_UP_DATE)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.PICK_UP_DATE + " INTEGER");
            }
            // Add RETURN_DATE.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.RETURN_DATE)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.RETURN_DATE + " INTEGER");
            }
            // Add CONFIRMATION_NUMBER.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.CONFIRMATION_NUMBER)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.CONFIRMATION_NUMBER + " TEXT");
            }
            // Add AVERAGE_DAILY_RATE.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE + " REAL");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;

    }

}
