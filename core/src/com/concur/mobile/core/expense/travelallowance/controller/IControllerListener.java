package com.concur.mobile.core.expense.travelallowance.controller;

import android.os.Bundle;

/**
 * Created by D049515 on 21.07.2015.
 */
public interface IControllerListener {

    void actionFinished(IController controller, ControllerAction action, boolean isSuccess, Bundle result);
}
