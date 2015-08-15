package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Dialog;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Michael Becherer on 10-Jul-15.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    public static final String BUNDLE_ID_DATE = "date";
    public static final String BUNDLE_ID_REQUEST_CODE = "requestCode";

    private Date date;
    private int requestCode;

    private OnTimeSetListener onTimeSetListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable(BUNDLE_ID_DATE);
            if (obj instanceof Date) {
                date = (Date) obj;
            }
            requestCode = args.getInt(BUNDLE_ID_REQUEST_CODE);
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
