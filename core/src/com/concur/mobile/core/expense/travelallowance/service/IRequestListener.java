package com.concur.mobile.core.expense.travelallowance.service;

import android.os.Bundle;

/**
 * Interface to be implemented when there is a need to get notified by the request receiver
 * from the resulting response of a service request.
 *
 * Created by D049515 on 09.09.2015.
 */
public interface IRequestListener {

    /**
     * The service request itself has been successfully handled.
     * @param resultData from Response
     */
    void onRequestSuccess(Bundle resultData);

    /**
     * The service request itself has not been successfully handled. Typically an HTTP-Error
     * was caught.
     */
    void onRequestFailed();

}
