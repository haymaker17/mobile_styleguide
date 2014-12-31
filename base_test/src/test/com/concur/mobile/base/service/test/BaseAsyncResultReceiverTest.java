/**
 * 
 */
package com.concur.mobile.base.service.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.os.Bundle;
import android.os.Handler;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;

/**
 * @author Harold Frazier, Jr.
 * 
 */
@RunWith(RobolectricTestRunner.class)
public class BaseAsyncResultReceiverTest {

	/**
	 * Creating a new class which extends <code>BaseAsyncResultReceiver</code>
	 * so we can override (and test) the <code>onReceiveResult(int, Bundle)</code>
	 * method.  If we just used the <code>BaseAsyncResultReceiver</code> directly,
	 * we wouldn't be able to invoke the <code>onReceiveResult(int, Bundle)</code>
	 * method because it is a protected method.
	 * 
	 * @author Chris N. Diaz
	 *
	 */
	private class TestBaseAsyncResultReceiver extends BaseAsyncResultReceiver {

		public TestBaseAsyncResultReceiver(Handler handler) {
			super(handler);
		}
		
		@Override
	    protected void onReceiveResult(int resultCode, Bundle resultData) {
	    	super.onReceiveResult(resultCode, resultData);
	    }
		
	}
	
    private TestBaseAsyncResultReceiver mBaseAsyncResultReceiver = null;
    private Handler mHandler = null;
    private Bundle data = null;
    private boolean bCleanupCalled = false;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

    	mBaseAsyncResultReceiver = new TestBaseAsyncResultReceiver(mHandler);
        mHandler = new Handler();
        data = null;
        bCleanupCalled = false;
    }

    
    @Test
    public void test_onReceiveResult_onRequestSuccess() {
        Bundle bundle = null;
        AsyncReplyListener listener = null;
        final CountDownLatch j = new CountDownLatch(1);

        listener = new AsyncReplyListener() {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                data = resultData;
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Assert.fail();

            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                Assert.fail();
            }

            @Override
            public void cleanup() {
                bCleanupCalled = true;
                j.countDown();
            }
        };

        bundle = new Bundle();
        bundle.putString("Test_onRequestSuccess", "TestData");

        data = null;
        bCleanupCalled = false;
        mBaseAsyncResultReceiver.setListener(listener);
        mBaseAsyncResultReceiver.onReceiveResult(BaseAsyncRequestTask.RESULT_OK, bundle);

        try {
            j.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertNotNull(data);
        Assert.assertTrue(bCleanupCalled);
        Assert.assertEquals(data, bundle);
    }

    @Test
    public void test_onReceiveResult_onRequestFail() {
        Bundle bundle = null;
        AsyncReplyListener listener = null;
        final CountDownLatch j = new CountDownLatch(1);

        listener = new AsyncReplyListener() {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                Assert.fail();
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                data = resultData;
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                Assert.fail();
            }

            @Override
            public void cleanup() {
                bCleanupCalled = true;
                j.countDown();
            }
        };

        bundle = new Bundle();
        bundle.putString("Test_onRequestFail", "TestData");

        data = null;
        bCleanupCalled = false;
        mBaseAsyncResultReceiver.setListener(listener);
        mBaseAsyncResultReceiver.onReceiveResult(BaseAsyncRequestTask.RESULT_ERROR, bundle);

        try {
            j.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertNotNull(data);
        Assert.assertTrue(bCleanupCalled);
        Assert.assertEquals(data, bundle);
    }

    @Test
    public void test_onReceiveResult_onRequestCancel() {
        Bundle bundle = null;
        AsyncReplyListener listener = null;
        final CountDownLatch j = new CountDownLatch(1);

        listener = new AsyncReplyListener() {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                Assert.fail();
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Assert.fail();
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                data = resultData;
            }

            @Override
            public void cleanup() {
                bCleanupCalled = true;
                j.countDown();
            }
        };

        bundle = new Bundle();
        bundle.putString("Test_onRequestCancel", "TestData");

        data = null;
        bCleanupCalled = false;
        mBaseAsyncResultReceiver.setListener(listener);
        mBaseAsyncResultReceiver.onReceiveResult(BaseAsyncRequestTask.RESULT_CANCEL, bundle);

        try {
            j.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertNotNull(data);
        Assert.assertTrue(bCleanupCalled);
        Assert.assertEquals(data, bundle);
    }

    @Test
    public void test_setListener() {
        AsyncReplyListener listener = null;

        listener = new AsyncReplyListener() {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRequestFail(Bundle resultData) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                // TODO Auto-generated method stub

            }

            @Override
            public void cleanup() {
                // TODO Auto-generated method stub

            }
        };

        mBaseAsyncResultReceiver.setListener(listener);
    }
}
