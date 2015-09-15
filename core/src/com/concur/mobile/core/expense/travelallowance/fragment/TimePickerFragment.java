package com.concur.mobile.core.expense.travelallowance.fragment;

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

import com.concur.mobile.core.expense.travelallowance.util.BundleId;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael Becherer on 10-Jul-15.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

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
                Class<?> classForid = Class.forName("com.android.internal.R$id");
                Field timePickerField = classForid.getField("timePicker");
                this.timePicker = (TimePicker) findViewById(timePickerField
                        .getInt(null));
                Field field = classForid.getField("minute");

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

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    private Date date;
    private int interval;

    private OnTimeSetListener onTimeSetListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable(BundleId.DATE);
            if (obj instanceof Date) {
                date = (Date) obj;
            }
            this.interval = args.getInt(BundleId.INTERVAL);
            if (interval <= 0 || interval > 60) {
                this.interval = 1;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar cal = Calendar.getInstance();

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

    public void setOnTimeSetListener (OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (this.onTimeSetListener != null) {
            onTimeSetListener.onTimeSet(view, hourOfDay, minute);
        }
    }
}
