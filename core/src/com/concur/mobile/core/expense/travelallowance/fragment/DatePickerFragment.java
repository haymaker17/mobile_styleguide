package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by D049515 on 22.07.2015.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface OnDateSetListener {

        void onDateSet(DatePicker datePicker, int requestCode, int year, int month, int day);
    }

    public static final String BUNDLE_ID_DATE = "date";
    public static final String BUNDLE_ID_REQUEST_CODE = "requestCode";

    private Date date;
    private int requestCode;

    private OnDateSetListener listener;

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
        final Calendar c = Calendar.getInstance();
        if (date != null) {
            c.setTime(date);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void setListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (listener != null){
            listener.onDateSet(datePicker, requestCode, year, month, day);
        }
    }
}
