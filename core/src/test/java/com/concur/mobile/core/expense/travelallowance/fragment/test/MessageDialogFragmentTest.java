package com.concur.mobile.core.expense.travelallowance.fragment.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.MessageDialogFragment;
import com.concur.mobile.core.expense.travelallowance.util.Message;

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
public class MessageDialogFragmentTest extends TestCase {


    public static class MyFragmentActivity extends FragmentActivity implements IFragmentCallback {
        @Override
        public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
            return;
        }
    }

    @Test
    public void showIsDirtyDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.MESSAGE_TITLE, "Confirm");
        bundle.putString(MessageDialogFragment.MESSAGE_TEXT, "Do you want to proceeed?");
        bundle.putString(MessageDialogFragment.POSITIVE_BUTTON, "MSG_POSITIVE");
        bundle.putString(MessageDialogFragment.NEUTRAL_BUTTON, "MSG_NEUTRAL");
        bundle.putString(MessageDialogFragment.NEGATIVE_BUTTON, "MSG_NEGATIVE");
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(bundle);
        MyFragmentActivity activity = Robolectric.buildActivity(MyFragmentActivity.class).create()
                .start().resume().get();
//        FragmentManager fragmentManager = activity.getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(messageDialog, null);
//        fragmentTransaction.commit();
        messageDialog.show(activity.getSupportFragmentManager(), "MSG_DIALOG_TAG");
        assertNotNull(messageDialog);
    }

    @Test
    public void showErrorDialog() {
        Message msg = new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES, "Mandatory fields are missing");
        Bundle bundle = new Bundle();
        bundle.putSerializable(MessageDialogFragment.MESSAGE_OBJECT, msg);
        bundle.putString(MessageDialogFragment.POSITIVE_BUTTON, "MSG_POSITIVE");
        MessageDialogFragment messageDialog = new MessageDialogFragment();
        messageDialog.setArguments(bundle);
        MyFragmentActivity activity = Robolectric.buildActivity(MyFragmentActivity.class).create()
                .start().resume().get();
        messageDialog.show(activity.getSupportFragmentManager(), "MSG_DIALOG_TAG");
        assertNotNull(messageDialog);
    }

}
