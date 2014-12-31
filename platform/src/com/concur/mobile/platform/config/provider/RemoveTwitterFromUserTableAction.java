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
 * Removal of Twitter information from the user table.
 */
public class RemoveTwitterFromUserTableAction extends SchemaUpgradeAction {

    private static final String CLS_TAG = "RemoveTwitterFromUserTableAction";

    /**
     * Constructs an instance of <code>RemoveTwitterFromUserTableAction</code>.
     * 
     * @param db
     *            contains a reference to the database.
     * @param oldVersion
     *            contains the old version.
     * @param newVersion
     *            contains the new version.
     */
    protected RemoveTwitterFromUserTableAction(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {
        super(db, oldVersion, newVersion);
    }

    @Override
    public boolean upgrade() {
        boolean success = true;

        try {
            String allColumns = Config.UserColumns._ID + "," + Config.UserColumns.ENTITY_TYPE + ","
                    + Config.UserColumns.EXPENSE_COUNTRY_CODE + "," + Config.UserColumns.HAS_REQUIRED_CUSTOM_FIELDS
                    + "," + Config.UserColumns.PIN_EXPIRATION_DATE + "," + Config.UserColumns.PRODUCT_OFFERING + ","
                    + Config.UserColumns.PROFILE_STATUS + "," + Config.UserColumns.ROLES_MOBILE + ","
                    + Config.UserColumns.CONTACT_COMPANY_NAME + "," + Config.UserColumns.CONTACT_EMAIL + ","
                    + Config.UserColumns.CONTACT_FIRST_NAME + "," + Config.UserColumns.CONTACT_LAST_NAME + ","
                    + Config.UserColumns.CONTACT_MIDDLE_INITIAL + "," + Config.UserColumns.USER_CURRENCY_CODE + ","
                    + Config.UserColumns.USER_ID;

            db.execSQL("ALTER TABLE " + Config.UserColumns.TABLE_NAME + " RENAME TO TEMP1");
            db.execSQL(ConfigDBSchema.SCHEMA_CREATE_USER_TABLE);
            db.execSQL("INSERT INTO " + Config.UserColumns.TABLE_NAME + " SELECT " + allColumns + " FROM TEMP1");
            db.execSQL("DROP TABLE TEMP1");
        } catch (SQLException sqlExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".upgrade: " + sqlExc.getMessage());
            success = false;
            throw sqlExc;
        }
        return success;
    }

}
