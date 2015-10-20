package com.concur.mobile.core.expense.travelallowance.fragment.test;

import android.support.v4.app.FragmentActivity;

import com.concur.mobile.core.expense.travelallowance.fragment.ProgressDialogFragment;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by d028778 on 20.10.2015.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class ProgressDialogFragmentTest extends TestCase {

    @Test
    public void showProgressDialog() {
        ProgressDialogFragment progressDialog = new ProgressDialogFragment();
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).create()
                .start().resume().get();
        progressDialog.show(activity.getSupportFragmentManager(), "PROGRESS_FRAGMENT");
        assertNotNull(progressDialog);
    }
}
