/**
 * 
 */
package com.concur.mobile.base.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAsyncTask;

import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.platform.test.Const;

/**
 * An extension of <code>ShadowAsyncTask</code> providing a shadow of <code>BaseAsyncRequestTask</code> which enables response
 * capture.
 * 
 * @author andrewk
 */
@Implements(value = BaseAsyncRequestTask.class, inheritImplementationMethods = true)
public class ShadowBaseAsyncRequestTask extends ShadowAsyncTask<Void, Void, Integer> {

    private static final String CLS_TAG = "ShadowBaseAsyncRequestTask";

    @Implementation
    public boolean isRetainResponseEnabled() {
        Log.d(Const.LOG_TAG, CLS_TAG + ".isRetainResponseEnabled: enabling response capture.");
        return true;
    }

}
