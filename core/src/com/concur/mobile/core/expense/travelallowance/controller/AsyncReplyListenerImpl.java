package com.concur.mobile.core.expense.travelallowance.controller;

import android.os.Bundle;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.service.IRequestListener;

import java.util.List;

/**
 * Created by D049515 on 16.09.2015.
 */
public class AsyncReplyListenerImpl implements BaseAsyncRequestTask.AsyncReplyListener {

    private IRequestListener listener;
    private BaseAsyncResultReceiver receiver;
    private List<BaseAsyncResultReceiver> receiverList;

    public AsyncReplyListenerImpl(List<BaseAsyncResultReceiver> receiverList, BaseAsyncResultReceiver receiver,
            IRequestListener listener) {
        this.listener = listener;
        this.receiver = receiver;
        this.receiverList = receiverList;
    }

    @Override
    public void onRequestSuccess(Bundle resultData) {
        if (listener != null) {
            listener.onRequestSuccess();
        }
        if (receiverList != null) {
            receiverList.remove(receiver);
        }
    }

    @Override
    public void onRequestFail(Bundle resultData) {
        if (listener != null) {
            listener.onRequestFailed();
        }
        if (receiverList != null) {
            receiverList.remove(receiver);
        }
    }

    @Override
    public void onRequestCancel(Bundle resultData) {
        if (receiverList != null) {
            receiverList.remove(receiver);
        }
    }

    @Override
    public void cleanup() {

    }

}
