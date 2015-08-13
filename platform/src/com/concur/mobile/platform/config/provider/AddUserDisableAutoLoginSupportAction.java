package com.concur.mobile.platform.config.provider;

import android.util.Log;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;
import net.sqlcipher.SQLException;

public class AddUserDisableAutoLoginSupportAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddUserDisableAutoLoginSupportAction";

    /**
     * Constructs an instance of <code>AddUserDisableAutoLoginSupportAction</code>.
     *
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    protected AddUserDisableAutoLoginSupportAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {

            // Add IS_DISABLE_AUTO_LOGIN to User table.
            db.execSQL("ALTER TABLE " + Config.UserColumns.TABLE_NAME + " ADD COLUMN "
                    + Config.UserColumns.IS_DISABLE_AUTO_LOGIN + " INTEGER DEFAULT 0");

        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}
