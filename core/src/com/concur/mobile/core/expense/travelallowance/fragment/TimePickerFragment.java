package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Handles time picker dialog called by an activity. The dialog is parametrized via the arguments
 * bundle given to an object of this class.
 * Notifies the caller using callback interface {@link IFragmentCallback}, which should be
 * implemented by the caller. The fragment message is given as bundle value for the following
 * argument keys:
 * {@link TimePickerFragment#ARG_SET_BUTTON}.
 * Using the extras bundle the selected values are transferred to the caller. The following
 * keys are used:
 * {@link TimePickerFragment#EXTRA_HOUR},
 * {@link TimePickerFragment#EXTRA_MINUTE}.
 *
 * Created by Michael Becherer on 10-Jul-15.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    private static final String CLASS_NAME = TimePickerFragment.class.getName();
    private static final String CLASS_TAG = TimePickerFragment.class.getSimpleName();

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}.
     * This value denotes the fragment message sent to callback object when set button was pressed
     * by the user.
     */
    public static final String ARG_SET_BUTTON = CLASS_NAME + ".set.button";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link Date}.
     * This value is taken as initial time to be displayed.
     */
    public static final String ARG_DATE = CLASS_NAME + ".date";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link int}.
     * This value is taken as minute's interval and is supposed to be between 0 and 60.
     */
    public static final String ARG_INTERVAL = CLASS_NAME + ".interval";

    /**
     * Used as key of an extra information. The associated value is of type {@code int}.
     * This value is passed via the extras bundle to the callback object and contains the
     * user's selected hour.
     */
    public static final String EXTRA_HOUR = CLASS_NAME + ".hour";

    /**
     * Used as key of an extra information. The associated value is of type {@code int}.
     * This value is passed via the extras bundle to the callback object and contains the
     * user's selected minute.
     */
    public static final String EXTRA_MINUTE = CLASS_NAME + ".minute";

    private IFragmentCallback callback;

    private class SpinnerTimePickerDialog extends TimePickerDialog {

        private TimePicker timePicker;
        private final OnTimeSetListener timeSetListener;

        public SpinnerTimePickerDialog(Context context, OnTimeSetListener timeSetListener,
                                       int hourOfDay, int minute, boolean is24HourFormat) {
            super(context, timeSetListener, hourOfDay, minute / interval, is24HourFormat);
            this.timeSetListener = timeSetListener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (this.timeSetListener != null && timePicker != null) {
                this.timePicker.clearFocus();
                this.timeSetListener.onTimeSet(this.timePicker, this.timePicker.getCurrentHour(),
                        this.timePicker.getCurrentMinute() * interval);
            }
        }

        @Override
        protected void onStop() {
        }

        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            try {
                Class<?> classForId = Class.forName("com.android.internal.R$id");
                Field timePickerField = classForId.getField("timePicker");
                this.timePicker = (TimePicker) findViewById(timePickerField
                        .getInt(null));
                Field field = classForId.getField("minute");

                NumberPicker numberPicker = (NumberPicker) timePicker
                        .findViewById(field.getInt(null));
                numberPicker.setMinValue(0);
                numberPicker.setMaxValue((60 / interval) - 1);
                List<String> displayedValues = new ArrayList<String>();
                for (int i = 0; i < 60; i += interval) {
                    displayedValues.add(String.format("%02d", i));
                }
                numberPicker.setDisplayedValues(displayedValues
                        .toArray(new String[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Date date;
    private int interval;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            Object obj = arguments.getSerializable(ARG_DATE);
            if (obj instanceof Date) {
                date = (Date) obj;
            }
            this.interval = arguments.getInt(ARG_INTERVAL, 1);
            if (interval <= 0 || interval > 60) {
                this.interval = 1;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new SpinnerTimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        } else {
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_SET_BUTTON)) {
                final String callBackMsg = arguments.getString(ARG_SET_BUTTON);
                if (callback != null && !StringUtilities.isNullOrEmpty(callBackMsg)) {
                    Bundle extras = new Bundle();
                    extras.putInt(EXTRA_HOUR, hourOfDay);
                    extras.putInt(EXTRA_MINUTE, minute);
                    callback.handleFragmentMessage(callBackMsg, extras);
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (IFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentCallback") ;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
