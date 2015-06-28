package com.concur.mobile.core.expense.travelallowance.controller;

/**
 * Created by D049515 on 24.06.2015.
 */
public interface IServiceRequestListener {

    void onRequestSuccess(final String controllerTag);

    void onRequestFail(final String controllerTag);

}
