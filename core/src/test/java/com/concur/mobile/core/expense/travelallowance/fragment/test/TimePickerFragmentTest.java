package com.concur.mobile.core.expense.travelallowance.fragment.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;
import com.concur.mobile.core.expense.travelallowance.fragment.TimePickerFragment;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by d028778 on 20.10.2015.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class TimePickerFragmentTest extends TestCase {

    public static class MyFragmentActivity extends FragmentActivity implements IFragmentCallback {

        public int hourOfDay;
        public int minute;

        @Override
        public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
            if ("MSG_TIME_SELECTED".equals(fragmentMessage) && extras != null) {
                this.hourOfDay = extras.getInt(TimePickerFragment.EXTRA_HOUR);
                this.minute = extras.getInt(TimePickerFragment.EXTRA_MINUTE);
            }
        }
    }

    @Test
    public void showTimePickerDialog() {
        TimePickerFragment timePickerDialog = new TimePickerFragment();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, Calendar.JANUARY, 1, 1, 1, 0);
        Bundle arguments = new Bundle();
        arguments.putSerializable(TimePickerFragment.ARG_DATE, calendar.getTime());
        arguments.putString(TimePickerFragment.ARG_SET_BUTTON, "MSG_TIME_SELECTED");
        timePickerDialog.setArguments(arguments);
        MyFragmentActivity activity = Robolectric.buildActivity(MyFragmentActivity.class).create()
                .start().resume().get();
        timePickerDialog.show(activity.getSupportFragmentManager(), "TIME_PICKER_FRAGMENT");
        assertNotNull(timePickerDialog);
        timePickerDialog.onTimeSet(null, 2, 5);
        assertEquals(2, activity.hourOfDay);
        assertEquals(5, activity.minute);
    }

}
