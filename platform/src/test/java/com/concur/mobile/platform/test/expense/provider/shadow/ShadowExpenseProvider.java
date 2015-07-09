/**
 * 
 */
package com.concur.mobile.platform.test.expense.provider.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowContentProvider;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.ClearExpenseDBHelper;
import com.concur.mobile.platform.expense.provider.ExpenseProvider;
import com.concur.mobile.platform.provider.ClearSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.test.Const;

/**
 * Provides a Robolectric shadow class for the ExpenseProvider. This shadow will provide an implemenation of
 * <code>ExpenseProvider.initPlatformSQLiteOpenHelper</code> that will install a non-encrypting expense DB helper object.
 * 
 * @author andrewk
 */
@Implements(value = ExpenseProvider.class, inheritImplementationMethods = true)
public class ShadowExpenseProvider extends ShadowContentProvider {

    private static final String CLS_TAG = "ShadowExpenseProvider";

    @Implementation
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".initPlatformSQLiteOpenHelper: configured clear expense db helper.");

        // This implementation will use an unencrypted database.
        PlatformSQLiteOpenHelper helper = new ClearSQLiteOpenHelper(new ClearExpenseDBHelper(context));
        return helper;
    }

}
