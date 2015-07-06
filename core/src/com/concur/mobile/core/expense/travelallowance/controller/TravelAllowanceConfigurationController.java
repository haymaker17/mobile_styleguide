package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.datamodel.TravelAllowanceConfiguration;
import com.concur.mobile.core.expense.travelallowance.service.GetTAConfigurationRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Holger Rose on 02.07.2015.
 */
public class TravelAllowanceConfigurationController {

    public static final String CONTROLLER_TAG = TravelAllowanceConfigurationController.class.getName();

    private static final String CLASS_TAG = TravelAllowanceConfigurationController.class.getSimpleName();

    private BaseAsyncResultReceiver receiver;

    private List<IServiceRequestListener> listeners;

    private GetTAConfigurationRequest getConfigurationRequest;

    private Context context;

    //ToDo: Check if we really need a list or only one instance of data model
    private TravelAllowanceConfiguration travelAllowanceConfig;

    public TravelAllowanceConfigurationController(Context context) {
        this.listeners = new ArrayList<IServiceRequestListener>();
        this.context = context;
    }

    public void refreshConfiguration(){


//        this.itineraryList = new ArrayList<Itinerary>();

        if (getConfigurationRequest != null && getConfigurationRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return;
        }


        receiver = new BaseAsyncResultReceiver(new Handler());

        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                travelAllowanceConfig = getConfigurationRequest.getTravelAllowanceConfiguration();
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

    getConfigurationRequest = new GetTAConfigurationRequest(context, receiver);

    getConfigurationRequest.execute();
    }

     private synchronized void notifyListener(boolean isFailed) {
         for (IServiceRequestListener listener: listeners) {
             if (isFailed){
                 listener.onRequestFail(CONTROLLER_TAG);
             } else {
                 listener.onRequestSuccess(CONTROLLER_TAG);
             }
         }
     }

     public synchronized void registerListener(IServiceRequestListener listener) {
         listeners.add(listener);
     }

     public synchronized void unregisterListener(IServiceRequestListener listener) {
         listeners.remove(listener);
     }

  public TravelAllowanceConfiguration getTravelAllowanceConfigurationList(){
      if (travelAllowanceConfig == null){
          return new TravelAllowanceConfiguration();
      }
      return travelAllowanceConfig;
  }

//    public List<CompactItinerary> getCompactItineraryList() {
//
//    }

}
