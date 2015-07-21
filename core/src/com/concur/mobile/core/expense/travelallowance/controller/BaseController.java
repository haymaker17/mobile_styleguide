package com.concur.mobile.core.expense.travelallowance.controller;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D049515 on 21.07.2015.
 */
public class BaseController implements IController {

    private List<IControllerListener> listenerList;

    @Override
    public synchronized void registerListener(IControllerListener listener) {
        if (listenerList == null) {
            listenerList = new ArrayList<IControllerListener>();
        }
        listenerList.add(listener);
    }

    @Override
    public synchronized void unregisterListener(IControllerListener listener) {
        if (listenerList != null) {
            listenerList.remove(listener);
        }
    }

    @Override
    public synchronized void notifyListener(ControllerAction action, boolean isSuccess, Bundle result) {
        if (listenerList != null) {
            for (IControllerListener listener : listenerList) {
                listener.actionFinished(this, action, isSuccess, result);
            }
        }
    }


}
