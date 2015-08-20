package com.concur.mobile.platform.request;

/**
 * Created by OlivierB on 07/08/2015.
 */

import com.concur.mobile.platform.BuildConfig;
import com.concur.mobile.platform.request.dto.RequestDTO;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SaveAndSubmitTaskTest
        extends com.concur.mobile.platform.test.AsyncRequestTest {

    private RequestDTO tr;

    private void initCreate() {
        tr = null;
        // --- TR initialization
        //tr.setHeaderFormId(rgc.getFormId());
        //tr.setPolicyId(rgc.getDefaultPolicyId());
        //tr.setCurrencyCode(Currency.getInstance(locale).getCurrencyCode());
        tr.setRequestDate(new Date());

    }

    @Test
    public void saveTest() {
    }
}
