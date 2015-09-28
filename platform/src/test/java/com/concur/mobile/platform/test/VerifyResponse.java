/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.test;

import android.content.Context;

public interface VerifyResponse<T> {

    T serializeResponse(String result);

    void verify(Context context, T response) throws Exception;

}
