package com.concur.mobile.platform.config.provider;

import net.sqlcipher.SQLException;
import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * An extension of <code>SchemaUpgradeAction</code> for the purposes of:<br>
 * <br>
 * 
 * Addition of <code>LOGIN_ID</code>, <code>SERVER_URL</code>, <code>SIGN_IN_METHOD</code> and <code>SSO_URL</code> columns to the
 * <code>SESSION</code> table.
 */
public class AddEmailLookUpToSessionAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "AddEmailLookUpToSessionSchemaUpgrade";

    /**
     * Constructs an instance of <code>AddEmailLookUpToSessionAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    protected AddEmailLookUpToSessionAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean upgraded = true;
        try {
            // Add LOGIN_ID
            db.execSQL("ALTER TABLE " + Config.SessionColumns.TABLE_NAME + " ADD COLUMN "
                    + Config.SessionColumns.LOGIN_ID + " TEXT");
            // Add SERVER_URL.
            db.execSQL("ALTER TABLE " + Config.SessionColumns.TABLE_NAME + " ADD COLUMN "
                    + Config.SessionColumns.SERVER_URL + " TEXT");
            // Add SIGN_IN_METHOD.
            db.execSQL("ALTER TABLE " + Config.SessionColumns.TABLE_NAME + " ADD COLUMN "
                    + Config.SessionColumns.SIGN_IN_METHOD + " TEXT");
            // Add SSO_URL.
            db.execSQL("ALTER TABLE " + Config.SessionColumns.TABLE_NAME + " ADD COLUMN "
                    + Config.SessionColumns.SSO_URL + " TEXT");
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            upgraded = false;
            throw sqlExc;
        }
        return upgraded;
    }

}
