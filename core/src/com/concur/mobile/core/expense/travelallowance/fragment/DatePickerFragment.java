package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.Calendar;
import java.util.Date;

/**
 * Handles date picker dialog called by an activity. The dialog is parametrized via the arguments
 * bundle given to an object of this class.
 * Notifies the caller using callback interface {@link IFragmentCallback}, which should be
 * implemented by the caller. The fragment message is given as bundle value for the following
 * argument keys:
 * {@link DatePickerFragment#ARG_SET_BUTTON}.
 * Using the extras bundle the selected values are transferred to the caller. The following
 * keys are used:
 * {@link DatePickerFragment#EXTRA_YEAR},
 * {@link DatePickerFragment#EXTRA_MONTH},
 * {@link DatePickerFragment#EXTRA_DAY}.
 *
 * Created by D049515 on 22.07.2015.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String CLASS_NAME = DatePickerFragment.class.getName();
    private static final String CLASS_TAG = DatePickerFragment.class.getSimpleName();

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link String}.
     * This value denotes the fragment message sent to callback object when set button was pressed
     * by the user.
     */
    public static final String ARG_SET_BUTTON = CLASS_NAME + ".set.button";

    /**
     * Used as key of an argument. The associated value is supposed to be of type {@link Date}.
     * This value is taken as initial date to be displayed.
     */
    public static final String ARG_DATE = CLASS_NAME + ".date";

    /**
     * Used as key of an extra information. The associated value is of type {@code int}.
     * This value is passed via the extras bundle to the callback object and contains the
     * user's selected year.
     */
    public static final String EXTRA_YEAR = CLASS_NAME + ".year";

    /**
     * Used as key of an extra information. The associated value is of type {@code int}.
     * This value is passed via the extras bundle to the callback object and contains the
     * user's selected month.
     */
    public static final String EXTRA_MONTH = CLASS_NAME + ".month";

    /**
     * Used as key of an extra information. The associated value is of type {@code int}.
     * This value is passed via the extras bundle to the callback object and contains the
     * user's selected day.
     */
    public static final String EXTRA_DAY = CLASS_NAME + ".day";

    private IFragmentCallback callback;
    private Date date;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            Object obj = arguments.getSerializable(ARG_DATE);
            if (obj instanceof Date) {
                date = (Date) obj;
            }
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

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_SET_BUTTON)) {
                final String callBackMsg = arguments.getString(ARG_SET_BUTTON);
                if (callback != null && !StringUtilities.isNullOrEmpty(callBackMsg)) {
                    Bundle extras = new Bundle();
                    extras.putInt(EXTRA_YEAR, year);
                    extras.putInt(EXTRA_MONTH, month);
                    extras.putInt(EXTRA_DAY, day);
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