package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;
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
 * Created by D023077 on 02.07.2015.
 */
public class TravelAllowanceConfigurationController {

    private static final String CLASS_TAG = TravelAllowanceConfigurationController.class.getSimpleName();

    private BaseAsyncResultReceiver receiver;

    private GetTAConfigurationRequest getConfigurationRequest;

    private Context context;

    //ToDo: Check if we really need a list or only one instance of data model
    private List<TravelAllowanceConfiguration> travelAllowanceConfigList;

    public TravelAllowanceConfigurationController(Context context) {
//        this.listeners = new ArrayList<IServiceRequestListener>();
        this.context = context;
    }

    public void refreshConfiguration(){

        receiver = new BaseAsyncResultReceiver(new Handler());

        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                travelAllowanceConfigList = getConfigurationRequest.getTravelAllowanceConfigurationList();
//                notifyListener(false);
                Log.d(CLASS_TAG, "Request success.");

            }

            @Override
            public void onRequestFail(Bundle resultData) {
//                notifyListener(true);
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

    getConfigurationRequest = new GetTAConfigurationRequest(context, 2, receiver);

    getConfigurationRequest.execute();

    }

// private synchronized void notifyListener(boolean isFailed) {}

// public synchronized void registerListener(IServiceRequestListener listener) {}

// public synchronized void unregisterListener(IServiceRequestListener listener) {

  public List<TravelAllowanceConfiguration> getTravelAllowanceConfigurationList(){
      if (travelAllowanceConfigList == null){
          return new ArrayList<TravelAllowanceConfiguration>();
      }
      return travelAllowanceConfigList;
  }

}
