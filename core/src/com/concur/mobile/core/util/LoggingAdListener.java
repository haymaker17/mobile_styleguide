package com.concur.mobile.core.util;

import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

/**
 * An {@link AdListener} that logs ad events for google dfp ads.
 * 
 */
public class LoggingAdListener extends AdListener {

    /** Called when an ad is loaded. */
    @Override
    public void onAdLoaded() {
        Log.d(ViewUtil.CLS_TAG, "onAdLoaded");
    }

    /** Called when an ad failed to load. */
    @Override
    public void onAdFailedToLoad(int errorCode) {
        String message = String.format("onAdFailedToLoad (%s)", getErrorReason(errorCode));
        Log.d(ViewUtil.CLS_TAG, message);
    }

    /**
     * Called when an Activity is created in front of the app (e.g. an interstitial is shown, or an ad is clicked and launches a
     * new Activity).
     */
    @Override
    public void onAdOpened() {
        Log.d(ViewUtil.CLS_TAG, "onAdOpened");
    }

    /** Called when an ad is closed and about to return to the application. */
    @Override
    public void onAdClosed() {
        Log.d(ViewUtil.CLS_TAG, "onAdClosed");
    }

    /**
     * Called when an ad is clicked and going to start a new Activity that will leave the application (e.g. breaking out to the
     * Browser or Maps application).
     */
    @Override
    public void onAdLeftApplication() {
        Log.d(ViewUtil.CLS_TAG, "onAdLeftApplication");
    }

    /** Gets a string error reason from an error code. */
    private String getErrorReason(int errorCode) {
        String errorReason = "";
        switch (errorCode) {
        case AdRequest.ERROR_CODE_INTERNAL_ERROR:
            errorReason = "Internal error";
            break;
        case AdRequest.ERROR_CODE_INVALID_REQUEST:
            errorReason = "Invalid request";
            break;
        case AdRequest.ERROR_CODE_NETWORK_ERROR:
            errorReason = "Network Error";
            break;
        case AdRequest.ERROR_CODE_NO_FILL:
            errorReason = "No fill";
            break;
        }
        return errorReason;
    }
}
