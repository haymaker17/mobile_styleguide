package com.concur.mobile.platform.ui.common.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concur.mobile.platform.ui.common.R;

public class CalendarPickerDialogV1 extends DialogFragment implements CalendarPicker.OnDateChangedListener,
        DialogInterface.OnClickListener {

    public static final String KEY_INITIAL_YEAR = "key.initial.year";
    public static final String KEY_INITIAL_MONTH = "key.initial.month";
    public static final String KEY_INITIAL_DAY = "key.initial.day";
    private static final String KEY_PICKED_YEAR = "key.picked.year";
    private static final String KEY_PICKED_MONTH = "key.initial.month";
    private static final String KEY_PICKED_DAY = "key.initial.day";
    public static final String KEY_TEXT_COLOR = "key.text.color";
    public static final String KEY_GRID_COLOR = "key.grid.color";
    public static final String KEY_HIGHLIGHT_COLOR = "key.highlight.color";
    public static final String KEY_ARROW_COLOR = "key.arrow.color";
    public static final String KEY_TITLE = "key.title";

    private CalendarPicker mCalendarPicker;
    private OnDateSetListener mListener;

    protected boolean mSingleSelect;

    private int mInitialYear;
    private int mInitialMonth;
    private int mInitialDay;

    private String title;

    private int mPickedYear;
    private int mPickedMonth;
    private int mPickedDay;

    private int mTextColor = CalendarPicker.DEFAULT_TEXT_COLOR;
    private int mGridColor = CalendarPicker.DEFAULT_GRID_COLOR;
    private int mHighlightColor = CalendarPicker.DEFAULT_HILITE_COLOR;
    private int mArrowColor = CalendarPicker.DEFAULT_ARROW_COLOR;

    public interface OnDateSetListener {

        void onDateSet(CalendarPicker view, int year, int month, int day);
    }

    public CalendarPickerDialogV1() {

    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog resultDialog;
        mCalendarPicker = new CalendarPicker(getActivity());

        resultDialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // do something...
                    }
                }).setNegativeButton(getText(R.string.general_cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).setView(mCalendarPicker).create();

        return resultDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            savedInstanceState = getArguments();
        }

        if (savedInstanceState != null) {
            mPickedYear = savedInstanceState.getInt(KEY_PICKED_YEAR);
            mPickedMonth = savedInstanceState.getInt(KEY_PICKED_MONTH);
            mPickedDay = savedInstanceState.getInt(KEY_PICKED_DAY);

            mInitialYear = savedInstanceState.getInt(KEY_INITIAL_YEAR);
            mInitialMonth = savedInstanceState.getInt(KEY_INITIAL_MONTH);
            mInitialDay = savedInstanceState.getInt(KEY_INITIAL_DAY);

            title = savedInstanceState.getString(KEY_TITLE);

            if (savedInstanceState.containsKey(KEY_TEXT_COLOR)) {
                mTextColor = savedInstanceState.getInt(KEY_TEXT_COLOR);
            }
            if (savedInstanceState.containsKey(KEY_HIGHLIGHT_COLOR)) {
                mHighlightColor = savedInstanceState.getInt(KEY_HIGHLIGHT_COLOR);
            }
            if (savedInstanceState.containsKey(KEY_ARROW_COLOR)) {
                mArrowColor = savedInstanceState.getInt(KEY_ARROW_COLOR);
            }
            if (savedInstanceState.containsKey(KEY_GRID_COLOR)) {
                mGridColor = savedInstanceState.getInt(KEY_GRID_COLOR);
            }
        }

        if (mPickedYear == 0 || mPickedMonth == 0 || mPickedDay == 0) {
            mCalendarPicker.init(mInitialYear, mInitialMonth, mInitialDay, this);
        } else {
            mCalendarPicker.init(mPickedYear, mPickedMonth, mPickedDay, this);
        }

        mCalendarPicker.setTextColor(mTextColor);
        mCalendarPicker.setGridColor(mGridColor);
        mCalendarPicker.setHighlightColor(mHighlightColor);
        // mCalendarPicker.setArrowColor(mArrowColor);

        setSingleSelect(true);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);

        arg0.putString(KEY_TITLE, title);
        arg0.putInt(KEY_INITIAL_YEAR, mInitialYear);
        arg0.putInt(KEY_INITIAL_MONTH, mInitialMonth);
        arg0.putInt(KEY_INITIAL_DAY, mInitialDay);

        arg0.putInt(KEY_PICKED_YEAR, mPickedYear);
        arg0.putInt(KEY_PICKED_MONTH, mPickedMonth);
        arg0.putInt(KEY_PICKED_DAY, mPickedDay);

        arg0.putInt(KEY_ARROW_COLOR, mArrowColor);
        arg0.putInt(KEY_GRID_COLOR, mGridColor);
        arg0.putInt(KEY_HIGHLIGHT_COLOR, mHighlightColor);
        arg0.putInt(KEY_TEXT_COLOR, mTextColor);

        // mListener = listener;
    }

    public void setSingleSelect(boolean ss) {
        mSingleSelect = ss;
    }

    public boolean isSingleSelect() {
        return mSingleSelect;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null) {
            mListener.onDateSet(mCalendarPicker, mPickedYear, mPickedMonth, mPickedDay);
        }
    }

    /**
     * Handler for the calendar widget date set event
     */
    @Override
    public void onDateChanged(CalendarPicker view, int year, int month, int day) {
        mPickedYear = year;
        mPickedMonth = month;
        mPickedDay = day;

        if (mSingleSelect) {
            this.onClick(getDialog(), AlertDialog.BUTTON_POSITIVE);
        }
    }
}
