package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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

    private List<BaseAsyncResultReceiver> receiverList;

    private GetTAConfigurationRequest getConfigurationRequest;

    private Context context;

    private TravelAllowanceConfiguration travelAllowanceConfig;

    public TravelAllowanceConfigurationController(Context context) {
        this.context = context;
        this.receiverList = new ArrayList<BaseAsyncResultReceiver>();
    }

    public void refreshConfiguration() {

        if (getConfigurationRequest != null && getConfigurationRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshConfiguration", "Refresh still in progress."));
            return;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, null) {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                travelAllowanceConfig = getConfigurationRequest.getTravelAllowanceConfiguration();
                notifyListener(ControllerAction.REFRESH, true, resultData);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshConfiguration", "Request success."));
                super.onRequestSuccess(resultData);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(ControllerAction.REFRESH, false, resultData);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshConfiguration", "Request failed."));
                super.onRequestFail(resultData);
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
