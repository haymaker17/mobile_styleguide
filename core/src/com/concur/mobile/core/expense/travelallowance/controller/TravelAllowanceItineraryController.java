package com.concur.mobile.core.expense.travelallowance.controller;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.GetItinerariesRequest;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TravelAllowanceItineraryController {

    private static final String CLASS_TAG = TravelAllowanceItineraryController.class.getSimpleName();

    private List<IServiceRequestListener> listeners;

    private GetItinerariesRequest getItinerariesRequest;

    private Context context;

    public TravelAllowanceItineraryController(Context context) {
        this.listeners = new ArrayList<IServiceRequestListener>();
        this.context = context;
    }



    public void refreshItineraries(String expenseReportKey, boolean isManager) {
        if (getItinerariesRequest != null && getItinerariesRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(
                new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                notifyListener(false);
                Log.d(CLASS_TAG, "Request success.");
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(true);
                Log.d(CLASS_TAG, "Request failed.");
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                // Not needed yet.
                return;
            }

            @Override
            public void cleanup() {
                // Not needed yet.
                return;
            }
        });

        getItinerariesRequest = new GetItinerariesRequest(context, 1, receiver,
                expenseReportKey);

        getItinerariesRequest.execute();
    }

    private synchronized void notifyListener(boolean isFailed) {
        for(IServiceRequestListener listener : listeners) {
            if (isFailed) {
                listener.onRequestSuccess();
            } else {
                listener.onRequestFail();
            }
        }
    }

    public synchronized void registerListener(IServiceRequestListener listener) {
        listeners.add(listener);
    }

    public synchronized void unregisterListener(IServiceRequestListener listener) {
        listeners.remove(listener);
    }
}
