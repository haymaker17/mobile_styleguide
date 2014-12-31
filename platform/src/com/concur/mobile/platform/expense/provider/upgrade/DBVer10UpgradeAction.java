package com.concur.mobile.platform.expense.provider.upgrade;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purpose of adding version 10 columns to the smart expense table.
 * 
 * @author andrewk
 */
public class DBVer10UpgradeAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "DBVer10UpgradeAction";

    /**
     * Constructs an instance of <code>DBVer10UpgradeAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    public DBVer10UpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {

        boolean upgraded = true;
        try {
            // Add REJECT_CODE.
            if (!columnExists(Expense.SmartExpenseColumns.TABLE_NAME, Expense.SmartExpenseColumns.REJECT_CODE)) {
                db.execSQL("ALTER TABLE " + Expense.SmartExpenseColumns.TABLE_NAME + " ADD COLUMN "
                        + Expense.SmartExpenseColumns.REJECT_CODE + " TEXT");
            }
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;

    }

}
