package com.concur.mobile.core.expense.travelallowance.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.GetItinerariesRequest;

/**
 * A simple {@link Fragment} subclass.
 */
public class TravelAllowanceControllerFragment extends Fragment {

    public interface IRequestCallback {
        void onRequestSuccess(Bundle resultData);

        void onRequestFail(Bundle resultData);

        //void onRequestCancel(Bundle resultData);
    }

    private IRequestCallback requestCallback;

    private GetItinerariesRequest getItinerariesRequest;

    public TravelAllowanceControllerFragment() { }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            requestCallback = (IRequestCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RequestCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        requestCallback = null;
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
                if (requestCallback != null) {
                    Toast.makeText((Context) requestCallback, "Request successful.", Toast.LENGTH_SHORT).show();
                    requestCallback.onRequestSuccess(resultData);
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                if (requestCallback != null) {
                    Toast.makeText((Context) requestCallback, "Request failed.", Toast.LENGTH_SHORT).show();
                    requestCallback.onRequestFail(resultData);
                }
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

        getItinerariesRequest = new GetItinerariesRequest(getActivity().getApplicationContext(), 1, receiver,
                expenseReportKey);

        getItinerariesRequest.execute();
    }
}
