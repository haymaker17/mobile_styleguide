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
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Holger Rose on 02.07.2015.
 */
public class TravelAllowanceConfigurationController extends BaseController {

    private static final String CLASS_TAG = TravelAllowanceConfigurationController.class.getSimpleName();

    private BaseAsyncResultReceiver receiver;

    private GetTAConfigurationRequest getConfigurationRequest;

    private Context context;

    private TravelAllowanceConfiguration travelAllowanceConfig;

    public TravelAllowanceConfigurationController(Context context) {
        this.context = context;
    }

    public void refreshConfiguration() {

        if (getConfigurationRequest != null && getConfigurationRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshConfiguration", "Refresh still in progress."));
            return;
        }

        receiver = new BaseAsyncResultReceiver(new Handler());

        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                travelAllowanceConfig = getConfigurationRequest.getTravelAllowanceConfiguration();
                notifyListener(ControllerAction.REFRESH, true, resultData);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshConfiguration", "Request success."));
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(ControllerAction.REFRESH, false, resultData);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshConfiguration", "Request failed."));
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

    /**
     *
     * @return can return null if there is no ta config loaded yet.
     */
  public TravelAllowanceConfiguration getTravelAllowanceConfigurationList(){
      return travelAllowanceConfig;
  }


}
