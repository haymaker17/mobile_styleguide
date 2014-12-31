/**
 * 
 */
package com.concur.mobile.core.view;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.core.widget.CalendarPickerDialog.OnDateSetListener;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>FormFieldView</code> to construct and manage a view editing date information.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.DatePickerFormFieldView} instead.
 * 
 * @author AndrewK
 */
public class DatePickerFormFieldView extends FormFieldView {

    private static final String CLS_TAG = DatePickerFormFieldView.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = DatePickerFormFieldView.class.getSimpleName()
            + ".calendar.dialog.fragment";

    private static final int DATE_PICKER_DIALOG = DIALOG_ID_BASE + 0;

    private static final String CALENDAR_BUNDLE_KEY = "calendar";

    private Calendar calendar;

    /**
     * Constructs an instance of <code>DatePickerFormFieldView</code> given an expense report form field.
     * 
     * @param frmFld
     *            the expense report form field.
     */
    public DatePickerFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
    }

    @Override
    public View getView(Context context) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (frmFld.getAccessType()) {
            case RW: {
                switch (frmFld.getInputType()) {
                case USER: {
                    view = inflater.inflate(R.layout.image_form_field, null);
                    if (view != null) {
                        // Set the field label.
                        setTextViewText(view, R.id.field_name, buildLabel());
                        // Set the field value.
                        if (frmFld.getValue() != null) {
                            // Init 'calendar' to the currently provided timestamp.
                            calendar = Parse.parseXMLTimestamp(frmFld.getValue());
                            if (calendar != null) {
                                setTextViewText(view, R.id.field_value, Format.safeFormatCalendar(
                                        FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, calendar));
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to convert timestamp value of '"
                                        + frmFld.getValue() + "' into a Calendar object!");
                            }
                        } else {
                            setTextViewText(view, R.id.field_value, "");
                        }
                        // Set the date picker icon.
                        // setImageViewImage(view, R.id.field_image, android.R.drawable.ic_menu_more);
                        // Enable focusability and make it clickable.
                        // These are set in the layout now.
                        // view.setFocusable(true);
                        // view.setClickable(true);
                        // Add a click handler.
                        view.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    showCalendarDialog();
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: null form field view listener!");
                                }
                            }
                        });
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to inflate layout file 'image_form_field'!");
                    }
                    break;
                }
                case CALC: {
                    view = buildStaticTextView(inflater);
                    break;
                }
                default: {
                    Log.w(Const.LOG_TAG, CLS_TAG
                            + ".getView: unknown form field input type, defaulting to static view!");
                    view = buildStaticTextView(inflater);
                    break;
                }
                }
                break;
            }
            case RO: {
                view = buildStaticTextView(inflater);
                break;
            }
            case HD: {
                // No-op.
                break;
            }
            }

            // Set the tag so we can find the view later
            if (view != null) {
                view.setTag(frmFld.getId());
            }

        }
        return view;
    }

    public Calendar getCalendar() {
        if (calendar != null) {
            return (Calendar) calendar.clone();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasValue() {
        return ((calendar != null) || frmFld.getValue() != null);
    }

    @Override
    public ValidityCheck isValueValid() {
        return SUCCESS;
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            Calendar origValue = null;
            if (frmFld.getValue() != null && frmFld.getValue().length() > 0) {
                origValue = Parse.parseXMLTimestamp(frmFld.getValue());
                if (origValue != null) {
                    if (calendar != null) {
                        retVal = ((origValue.get(Calendar.YEAR) != calendar.get(Calendar.YEAR))
                                || (origValue.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) || (origValue
                                .get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)));
                    } else {
                        // No-op, editing was never invoked for this field.
                    }
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".hasValueChanged: unable to convert timestamp value of '" + frmFld.getValue()
                                    + "' into a Calendar object!");
                }
            } else {
                // Just check for whether a date was set via editing.
                retVal = (calendar != null);
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#getCurrentValue()
     */
    @Override
    public String getCurrentValue() {
        String retVal = null;
        if (calendar != null) {
            retVal = Format.safeFormatCalendar(FormatUtil.XML_DF, calendar);
        } else {
            retVal = frmFld.getValue();
        }
        return retVal;
    }

    public void setCurrentValue(Calendar value, boolean notify) {
        if (value != null) {
            calendar = value;

            if (notify && listener != null) {
                listener.valueChanged(this);
            }

            // Update the display.
            updateDatePickerDisplayValue();

        }
    }

    @Override
    public void setCurrentValue(String value, boolean notify) {
        if (value != null) {
            Calendar cal = Parse.parseXMLTimestamp(value);
            if (cal != null) {
                setCurrentValue(cal, notify);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setCurrentValue: unable to parse XML timestamp value of '" + value
                        + "'.");
            }
        }
    }

    /**
     * Will retrieve the current value as a Calendar object.
     * 
     * @return the current value as a Calendar object.
     */
    public Calendar getCurrentValueAsCalendar() {
        return calendar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#updateEditedValue(com.concur.mobile.util.FormFieldView)
     */
    @Override
    public void updateEditedValue(FormFieldView frmFldView) {
        // Check for whether this field is editable.
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            // Check for whether value has changed in source form field view.
            if (frmFldView.hasValueChanged()) {
                // Check for same type of field.
                if (frmFldView instanceof DatePickerFormFieldView) {
                    DatePickerFormFieldView dtPckrFrmFldView = (DatePickerFormFieldView) frmFldView;
                    // If 'frmFldView.hasValueChanged' returned 'true' from above, then a calendar had
                    // to have been set, so it's safe to just assign it here.
                    calendar = dtPckrFrmFldView.calendar;
                    // Update the display.
                    updateDatePickerDisplayValue();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        String key = getPrefixedKey(CALENDAR_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            setCurrentValue((Calendar) bundle.getSerializable(key), false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        bundle.putSerializable(getPrefixedKey(CALENDAR_BUNDLE_KEY), calendar);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (hasValueChanged()) {
            bundle.putSerializable(getPrefixedKey(CALENDAR_BUNDLE_KEY), calendar);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        if (calendar != null) {
            frmFld.setValue(getCurrentValue());
        }
    }

    private void updateDatePickerDisplayValue() {
        setTextViewText(view, R.id.field_value,
                Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, calendar));
    }

    private void hideCalendarDialog() {
        CalendarPickerDialog dialog;
        FragmentTransaction transaction;

        dialog = (CalendarPickerDialog) listener.getFragmentManager().findFragmentByTag(TAG_CALENDAR_DIALOG_FRAGMENT);
        if (dialog != null) {
            dialog.dismiss();

            transaction = listener.getFragmentManager().beginTransaction();
            listener.getFragmentManager().popBackStack();
            transaction.remove(dialog);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            transaction.commit();
        }

    }

    private void showCalendarDialog() {
        Bundle bundle;
        CalendarPickerDialog datePickerDlg;
        // If there is no currently set date, then initialize one.
        Calendar startEditingDate = calendar;
        if (startEditingDate == null) {
            // We're specifically getting 'now' in the device local timezone because then
            // we just pull the date values out and use them to populate our UTC-standard
            // calendar.
            startEditingDate = Calendar.getInstance();
        }

        bundle = new Bundle();
        datePickerDlg = new CalendarPickerDialog();

        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, startEditingDate.get(Calendar.YEAR));
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, startEditingDate.get(Calendar.MONTH));
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, startEditingDate.get(Calendar.DAY_OF_MONTH));
        bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);

        datePickerDlg.setOnDateSetListener(new DatePickerDialogListener());
        datePickerDlg.setArguments(bundle);
        datePickerDlg.show(listener.getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);

        // MOB-17930 : require to finish all the pending transactions.
        listener.getFragmentManager().executePendingTransactions();

        Dialog dlg = datePickerDlg.getDialog();
        if (dlg != null) {
            dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Remove the dialog. This will force recreation and a reset of the displayed values.
                    // If we do not do this then the dialog may be shown with it's old values (from the first
                    // time around) instead of the newest values (if the underlying Calendar value changes).
                    listener.clearCurrentFormFieldView();
                    hideCalendarDialog();
                }
            });
        }
    }

    /**
     * An implementation of <code>DatePickerDialog.OnDateSetListener</code> to handle setting the date.
     * 
     * @author AndrewK
     */
    class DatePickerDialogListener implements OnDateSetListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
         */
        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            // If there was no original date provided, then default to now, but set the year, monthOfYear
            // and dayOfMonth to the chosen value.
            if (calendar == null) {
                calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            }
            // Use 00:00:00 for time instead of current phone time. Everything stays in UTC all the way through xml request
            calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
            updateDatePickerDisplayValue();
            if (listener != null) {
                listener.valueChanged(DatePickerFormFieldView.this);
            }
            // sendTaxFormField();
            // Remove the dialog. This will force recreation and a reset of the displayed values.
            // If we do not do this then the dialog may be shown with it's old values (from the first
            // time around) instead of the newest values (if the underlying Calendar value changes).
            listener.clearCurrentFormFieldView();
            hideCalendarDialog();
        }

    }

}
