package com.concur.mobile.platform.service;

import android.content.Context;
import android.os.Bundle;

/**
 * Provides an interface for a platform manager.
 */
public interface PlatformManager {

    /**
     * Notifies the platform manager that a request is about to be started.
     *
     * @param context    contains a reference to an application context.
     * @param request    contains a reference to a platform request.
     * @param resultData contains the result data bundle for the request about to be started.
     * @return returns a result code of either <code>BaseAsyncRequestTask.RESULT_OK</code>,
     * <code>BaseAsyncRequestTask.RESULT_OKAY</code>, <code>BaseAsyncRequestTask.RESULT_ERROR</code> or
     * <code>BaseAsyncRequestTask.RESULT_CANCEL</code>.
     */
    public int onRequestStarted(Context context, PlatformAsyncRequestTask request, Bundle resultData);

    /**
     * Notifies the platform manager that a request has completed.
     *
     * @param context contains a reference to an application context.
     * @param request contains a reference to a platform request.
     */
    public void onRequestCompleted(Context context, PlatformAsyncRequestTask request);

    public int onRequestStarted(Context context, PlatformAsyncTaskLoader loader, Bundle resultData);

    public void onRequestCompleted(Context context, PlatformAsyncTaskLoader loader);

}
