package com.concur.mobile.core.expense.travelallowance.controller;

import android.os.Bundle;

/**
 * Created by D049515 on 21.07.2015.
 */
public interface IController {

    void registerListener(IControllerListener listener);

    void unregisterListener(IControllerListener listener);

    void notifyListener(ControllerAction action, boolean isSuccess, Bundle result);

}
