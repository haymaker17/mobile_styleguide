package com.concur.mobile.base.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;

public class BaseAsyncResultReceiver extends ResultReceiver {

    /**
     * A reference to the {@link AsyncReplyListener} that will receive the results callback.
     */
    private AsyncReplyListener listener;

    /**
     * A holding place for the received result code when the listener is not available.
     */
    private Integer heldResultCode;

    /**
     * A holding place for the received result data when the listener is not available.
     */
    private Bundle heldResultData;

    // -------------------------------------------------

    /**
     * Constructor
     * 
     * @param handler
     *            A {@link Handler} that will receive messages posted to this receiver.
     */
    public BaseAsyncResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     * Set the {@link AsyncReplyListener} for the {@link BaseAsyncRequestTask}. If the receiver has received a result while the
     * listener was disconnected then the results will be immediately sent.
     * 
     * @param listener
     *            An {@link AsyncReplyListener} that will receive the results callback
     */
    public void setListener(AsyncReplyListener listener) {
        this.listener = listener;
        if (heldResultCode != null) {
            send(heldResultCode, heldResultData);
        }
    }

    /**
     * Returns the <code>AsyncReplyListener</code> or <code>null</code> if none is set.
     * 
     * @return
     */
    public AsyncReplyListener getListener() {
        return this.listener;
    }

    /**
     * Handle the result of the request. A result of {@link RESULT_CANCEL} will generate a call to
     * {@link AsyncReplyListener#onRequestCancel(Bundle)}. A result of {@link RESULT_ERROR} will generate a call to
     * {@link BaseAsyncReplyListener#onRequestError(Bundle)}. Any other result will generate a call to
     * {@link AsyncReplyListener#onRequestSuccess(Bundle)}.
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        heldResultCode = null;
        heldResultData = null;

        if (listener != null) {
            switch (resultCode) {
            case BaseAsyncRequestTask.RESULT_CANCEL:
                listener.onRequestCancel(resultData);
                break;
            case BaseAsyncRequestTask.RESULT_ERROR:
                listener.onRequestFail(resultData);
                break;
            default:
                listener.onRequestSuccess(resultData);
                break;
            }
            listener.cleanup();
        } else {
            // Hold the results until we get a listener
            heldResultCode = resultCode;
            heldResultData = resultData;
        }
    }

}
