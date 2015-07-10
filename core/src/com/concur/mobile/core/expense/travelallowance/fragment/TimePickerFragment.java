package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Dialog;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Michael Becherer on 10-Jul-15.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    private OnTimeSetListener onTimeSetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
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
