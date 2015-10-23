package com.concur.mobile.platform.expense.provider.upgrade;

import android.util.Log;

import com.concur.mobile.platform.expense.provider.ExpenseDBSchema;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

import net.sqlcipher.SQLException;

/**
 * @author Elliott Jacobsen-Watts
 */
public class DBVer11UpgradeAction extends SchemaUpgradeAction {

    public static final String CLS_TAG = DBVer11UpgradeAction.class.getSimpleName();

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
    public DBVer11UpgradeAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {
            // Add Corporate Card Transaction table.
            db.execSQL(ExpenseDBSchema.SCHEMA_CREATE_EXPENSEIT_RECEIPT_TABLE);
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }
}
