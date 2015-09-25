package com.concur.mobile.core.expense.travelallowance.fragment;

import android.os.Bundle;

/**
 * Created by D049515 on 23.06.2015.
 */
public interface IFragmentCallback {

    /**
     * This method handles the message usually passed from a fragment.
     * @param fragmentMessage The fragment message passed by the fragment.
     * @param extras A bundle with specific extras parameters needed to handle the message.
     */
    void handleFragmentMessage(String fragmentMessage, Bundle extras);
}
