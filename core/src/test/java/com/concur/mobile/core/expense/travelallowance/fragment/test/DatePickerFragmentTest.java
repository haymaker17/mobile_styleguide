package com.concur.mobile.core.expense.travelallowance.fragment.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.concur.mobile.core.expense.travelallowance.fragment.DatePickerFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.IFragmentCallback;

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
public class DatePickerFragmentTest extends TestCase {

    public static class MyFragmentActivity extends FragmentActivity implements IFragmentCallback {
        public int year;

        @Override
        public void handleFragmentMessage(String fragmentMessage, Bundle extras) {
            if ("MSG_DATE_SELECTED".equals(fragmentMessage) && extras != null) {
                this.year = extras.getInt(DatePickerFragment.EXTRA_YEAR);
            }
        }
    }

    @Test
    public void showCalendarDialog() {
        DatePickerFragment calendarDialog = new DatePickerFragment();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, Calendar.JANUARY, 1, 1, 1, 0);
        Bundle arguments = new Bundle();
        arguments.putSerializable(DatePickerFragment.ARG_DATE, calendar.getTime());
        arguments.putString(DatePickerFragment.ARG_SET_BUTTON, "MSG_DATE_SELECTED");
        calendarDialog.setArguments(arguments);
        MyFragmentActivity activity = Robolectric.buildActivity(MyFragmentActivity.class).create()
                .start().resume().get();
        calendarDialog.show(activity.getSupportFragmentManager(), "CALENDAR_FRAGMENT");
        assertNotNull(calendarDialog);
        calendarDialog.onDateSet(null, 2016, Calendar.JANUARY, 1);
        assertEquals(2016, activity.year);
    }

}
