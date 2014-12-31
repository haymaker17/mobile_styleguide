/**
 * 
 */
package com.concur.mobile.core.view;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * An abstract
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.FormFieldView} instead.
 * @author AndrewK
 */
public abstract class FormFieldView {

    private static final String CLS_TAG = FormFieldView.class.getSimpleName();

    public static final int DIALOG_ID_BASE = 100000;
    public static final int REQUEST_ID_BASE = 10000;

    public static final NumberFormat deviceLocaleNumericFormatter;
    public static final NumberFormat serverLocaleNumbericFormatter;

    static {
        deviceLocaleNumericFormatter = DecimalFormat.getInstance();
        deviceLocaleNumericFormatter.setMaximumFractionDigits(340);
        serverLocaleNumbericFormatter = DecimalFormat.getInstance(Locale.US);
    }

    protected static final ValidityCheck SUCCESS = new ValidityCheck(true);

    /**
     * Provides the result of a validity check with reason.
     */
    public static class ValidityCheck {

        /**
         * Contains the result of the validity check.
         */
        public boolean result;
        /**
         * Contains an optional reason for validity check result.
         */
        public String reason;

        /**
         * Constructs an instance of <code>ValidityCheck</code> with a result of <code>false</code> and a <code>reason</code> of
         * <code>null</code>.
         */
        public ValidityCheck() {
        }

        /**
         * Constructs an instance of <code>ValidityCheck</code> with a result.
         * 
         * @param result
         *            contains the result.
         */
        public ValidityCheck(boolean result) {
            this(result, null);
        }

        /**
         * Constructs an instance of <code>ValidityCheck</code> with both a result and reason.
         * 
         * @param result
         *            contains the result.
         * @param reason
         *            contains a reason for the result.
         */
        public ValidityCheck(boolean result, String reason) {
            this.result = result;
            this.reason = reason;
        }
    }

    /**
     * An interface for listening for handling events related to a <code>FormFieldView</code> object.
     * 
     * @author AndrewK
     */
    public static interface IFormFieldViewListener {

        /**
         * Notifies the form field view listener to display a dialog.
         * 
         * @param frmFldView
         *            the form field view.
         * @param id
         *            the dialog id.
         */
        public void showDialog(FormFieldView frmFldView, int id);

        /**
         * Notifies the form field view listener to dismiss the dialog.
         * 
         * @param frmFldView
         *            the form field view.
         * @param dialog
         *            the dialog.
         */
        public void dismissDialog(FormFieldView frmFldView, Dialog dialog);

        /**
         * Notifies the form field view listener to launch an activity.
         * 
         * @param frmFldView
         *            the form field view.
         * @param intent
         *            the activity intent.
         */
        public void startActivity(FormFieldView frmFldView, Intent intent);

        /**
         * Notifies the form field view listener to launch an activity and return the result.
         * 
         * @param frmFldView
         *            the form field view.
         * @param intent
         *            the activity intent.
         * @param requestCode
         *            the request code.
         */
        public void startActivityForResult(FormFieldView frmFldView, Intent intent, int requestCode);

        /**
         * Gets the current <code>FormFieldView</code> that last called into this listener.
         * 
         * @return the current <code>FormFieldView</code> that last called into this listener.
         */
        public FormFieldView getCurrentFormFieldView();

        /**
         * Sets the current form field view associated with this listener.
         * 
         * @param curFrmFldView
         *            the current form field view associated with this listener.
         */
        public void setCurrentFormFieldView(FormFieldView curFrmFldView);

        /**
         * Gets whether the current form field view is set on this listener.
         * 
         * @return whether the current form field view object is set on this listener.
         */
        public boolean isCurrentFormFieldViewSet();

        /**
         * Notifies the form field view listener that it should clear any current reference to a <code>FormFieldView</code>
         * instance.
         */
        public void clearCurrentFormFieldView();

        /**
         * Notifies the form field view listener that is should regenerate the set of form field view objects based on the
         * underlying data.
         */
        public void regenerateFormFieldViews();

        public FragmentManager getFragmentManager();

        /**
         * Gets a reference to the activity containing this form field view.
         * 
         * @return the activity containing this form field view.
         */
        public Activity getActivity();

        /**
         * Gets an instance of <code>ExpenseReport</code> associated with this listener.
         * 
         * @return an instance of <code>ExpenseReport</code> associated with this listener.
         */
        public ExpenseReport getExpenseReport();

        /**
         * Gets an instance of <code>ExpenseReportEntry</code> associated with this listener.
         * 
         * @return an instance of <code>ExpenseReportEntry</code> associated with this listener.
         */
        public ExpenseReportEntry getExpenseReportEntry();

        /**
         * Will set the instance of <code>ExpenseReportEntry</code> currently associated with this form field view listener.
         * 
         * @param entry
         *            the expense report entry.
         */
        public void setExpenseReportEntry(ExpenseReportEntry entry);

        /**
         * Gets the list of <code>FormFieldView</objects>.
         * 
         * @return the list of form field view objects.
         */
        public List<FormFieldView> getFormFieldViews();

        /**
         * Notifies the form field view listener that the value in this field has changed.
         * 
         * @param frmFldView
         *            the instance of <code>FormFieldView</code> whose edited value has changed.
         */
        public void valueChanged(FormFieldView frmFldView);

        /**
         * Retrieves an instance of <code>FormFieldView</code> given the field id of the underlying instance of
         * <code>ExpenseReportFormField</code>.
         * 
         * @param id
         *            the id of the instance of <code>ExpenseReportFormField</code> backing the form field view.
         * 
         * @return an instance of <code>FormFieldView</code> backed by an instance of <code>ExpenseReportFormField</code> with id
         *         <code>id</code>.
         */
        public FormFieldView findFormFieldViewById(String id);

    }

    // Contains the expense report form field.
    public ExpenseReportFormField frmFld;

    // Contains the built view.
    public View view;

    // Contains the form field view listener.
    protected IFormFieldViewListener listener;

    // Contains the view ID of the <code>TextView</code> object holding the field value.
    // This ID should be unique and by default should probably be the hashcode value of
    // the frmFld.id (string) value.
    // Currently (6/6/2012), only sub-classes that use EditText views to hold values
    // set this value as screen rotation will result in the last EditText's current value
    // being set on all TextView's objects with an ID of 'field_value'.
    protected int fieldValueId;
    // contains the display label for the field.
    protected String fieldLabel;

    /**
     * Gets a build view object for the form field.
     * 
     * @param context
     *            the context used to create the view.
     * 
     * @return an instance of <code>View<code> containing the necessary UI elements
     *      to support editing.
     */
    public abstract View getView(Context context);

    /**
     * Commits the data stored in this <code>FormFieldView</code> object to its backing <code>ExpenseReportFormField</code>
     * object.
     */
    public abstract void commit();

    /**
     * Whether this view has a value specified.
     * 
     * @return whether this view has a value specified.
     */
    public abstract boolean hasValue();

    /**
     * Whether this view has a valid value specified.
     * 
     * @return whether this view has a valid value specified.
     */
    public abstract ValidityCheck isValueValid();

    /**
     * Will display a field note if the current value is invalid.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * This default implementation of this method is a no-op, sub-classes must provide a useful implementation.
     */
    public void displayFieldNoteIfValueInvalid() {
    }

    /**
     * Gets whether or not the form field view object contains edited changes.
     * 
     * @return whether or not the form field view object contains edited changes.
     */
    public abstract boolean hasValueChanged();

    /**
     * Will get the current edited value of the field.
     * 
     * @return the current edited value of the field.
     */
    public abstract String getCurrentValue();

    /**
     * Will set the current edited value.
     * 
     * @param value
     *            the current edited value.
     * @param notify
     *            whether to notify the associated form field view listener.
     */
    public abstract void setCurrentValue(String value, boolean notify);

    /**
     * Will update the current edited value in this <code>FormFieldView</code> object with a value that has been edited in
     * <code>frmFldView</code>.
     * 
     * If <code>frmFldView</code> has not been edited, then this edited value will not be updated.
     * 
     * @param frmFldView
     *            the form field view provided an edited value, if any.
     */
    public abstract void updateEditedValue(FormFieldView frmFldView);

    /**
     * Creates an appropriate dialog for id <code>id</code>.
     * 
     * @param id
     *            the id of the dialog to create.
     * @return an instance of <code>Dialog</code>.
     */
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        return dialog;
    }

    /**
     * Prepares a dialog to be displayed.
     * 
     * @param id
     *            the dialog id.
     * @param dialog
     *            the dialog to prepare.
     */
    public void onPrepareDialog(int id, Dialog dialog) {
    }

    /**
     * Informs the <code>FormFieldView</code> of the result of an activity.
     * 
     * @param requestCode
     *            the request code.
     * @param resultCode
     *            the result code.
     * @param data
     *            the result data.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * Will save the state of the form field view regardless of whether the value has changed from the underlying form field
     * object.
     * 
     * @param bundle
     *            the saved instance state bundle.
     */
    public abstract void onSaveInstanceStateIgnoreChange(Bundle bundle);

    /**
     * Will save the state of the form field view into <code>bundle</code>.
     * 
     * @param bundle
     *            the saved instance state bundle.
     */
    public abstract void onSaveInstanceState(Bundle bundle);

    /**
     * Will restore the state of this form field view from <code>bundle</code>.
     * 
     * @param bundle
     *            the bundle containing the state for this form field view.
     */
    public abstract void onRestoreInstanceState(Bundle bundle);

    /**
     * Will store non-configuration data.
     * 
     * @param retainer
     *            the retainer fragment
     */
    public void onRetainNonConfigurationInstance(RetainerFragment retainer) {
        // No-op.
    }

    /**
     * Will restore non-configuration data.
     * 
     * @param retainer
     *            the retainer fragment
     */
    public void onApplyNonConfigurationInstance(RetainerFragment retainer) {
        // No-op.
    }

    /**
     * Constructs an instance of <code>FormFieldView</code> given an expense report form field.
     * 
     * @param frmFld
     *            the expense report form field.
     * @param listener
     *            an instance of <code>IFormFieldViewListener</code>.
     */
    protected FormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        this.frmFld = frmFld;
        this.listener = listener;
        // Assign a unique value for 'fieldValueId'.
        if (frmFld.getId() != null && frmFld.getId().length() > 0) {
            // Use hash-code value based on form field id.
            fieldValueId = Math.abs(frmFld.getId().hashCode());
        } else {
            // Use hash-code value for current time stamp in milliseconds.
            fieldValueId = Long.toString(System.currentTimeMillis()).hashCode();
        }
        fieldLabel = frmFld.getLabel();
    }

    /**
     * Get the form field view label.
     * 
     * @return returns the form field view label.
     */
    public String getFieldLabel() {
        return fieldLabel;
    }

    /**
     * Set the form field view label.
     * 
     * @param fieldLabel
     *            contains the form field view label.
     */
    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
        if (view != null) {
            CharSequence label = buildLabel(fieldLabel);
            setTextViewText(view, R.id.field_name, label);
        }
    }

    /**
     * Will set the view field value id by locating an existing view with ID 'field_value' and set it's ID to the value of
     * <code>fieldValueId</code>.
     */
    protected void setViewFieldValueId(View view) {
        TextView txtView = (TextView) view.findViewById(R.id.field_value);
        if (txtView != null) {
            txtView.setId(fieldValueId);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setViewFieldValueId: field_value view not found!");
            Thread.dumpStack();
        }
    }

    /**
     * Gets the unique ID set on the <code>TextView</code> instance holding the value of the field.
     * 
     * @return returns the unique ID set on the <code>TextView</code> instance holding the value of the field.
     */
    public int getViewFieldValueId() {
        return fieldValueId;
    }

    /**
     * Gets the cross-session user id based on the current login.
     * 
     * @return the cross-session user id associated with the current login.
     */
    protected String getUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(listener.getActivity()
                .getApplicationContext());
        String userId = prefs.getString(Const.PREF_USER_ID, null);
        return userId;
    }

    /**
     * Will return a prefixed key with the prefix being 'frmFld.getId()' + '.' + prefix.
     * 
     * @param prefix
     *            the prefix to append onto the form field id.
     * @return a prefix key of 'form field' + '.' + prefix.
     */
    protected String getPrefixedKey(String prefix) {
        StringBuffer strBuf = new StringBuffer(frmFld.getId());
        strBuf.append('.');
        strBuf.append(prefix);
        return strBuf.toString();
    }

    /**
     * Gets a reference to <code>ExpenseReportFormField</code> backing this view object.
     * 
     * @return a reference to the <code>ExpenseReportFormField</code> backing this view object.
     */
    public ExpenseReportFormField getFormField() {
        return frmFld;
    }

    /**
     * Sets the text in a <code>TextView</object> view contained within <code>view</code>.
     * 
     * @param container
     *            the view containing the <code>TextView</code> object.
     * @param textViewId
     *            the resource ID of the <code>TextView</code> object within <code>view</code>.
     * @param text
     *            the text view text value.
     */
    public void setTextViewText(View container, int textViewId, CharSequence text) {
        if (text != null) {
            if (container != null) {
                TextView txtView = (TextView) container.findViewById(textViewId);
                if (txtView != null) {
                    txtView.setText(text);
                } else {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".setTextViewText: unable to locate 'TextView' in container!");
                }
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".setTextViewText: container is null!");
            }
        }
    }

    /**
     * Gets the text string in a <code>TextView</code> object contained within <code>container</code>.
     * 
     * @param container
     *            the container of the text view.
     * @param textViewId
     *            the resource ID of the text view.
     * @return the text as a string in the text view.
     */
    protected String getTextViewText(View container, int textViewId) {
        String retVal = null;
        if (container != null) {
            TextView txtView = (TextView) container.findViewById(textViewId);
            if (txtView != null) {
                retVal = txtView.getText().toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: unable to locate 'TextView' in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getTextViewText: container is null!");
        }
        return retVal;
    }

    /**
     * Will set the right drawable on a text view object.
     * 
     * @param container
     *            the view containing the text view.
     * @param textViewId
     *            the id of the text view.
     * @param drawableResId
     *            the resource id of the drawable.
     */
    protected void setTextViewRightDrawable(View container, int textViewId, int drawableResId) {
        if (container != null) {
            TextView txtView = (TextView) container.findViewById(textViewId);
            if (txtView != null) {
                txtView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableResId, 0);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewText: unable to locate 'TextView' in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setTextViewRightDrawable: container is null!");
        }
    }

    /**
     * Will set the image resource id on an <code>ImageView</code> object contained in <code>container</code>.
     * 
     * @param container
     *            the containing view.
     * @param imageViewId
     *            the resource id of the image view.
     * @param drawableResId
     *            the image resource id.
     */
    protected void setImageViewImage(View container, int imageViewId, int drawableResId) {
        if (container != null) {
            ImageView imgView = (ImageView) container.findViewById(imageViewId);
            if (imgView != null) {
                imgView.setImageResource(drawableResId);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setImageViewImage: unable to locate 'ImageView' in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setImageViewImage: container is null!");
        }
    }

    /**
     * Will set the on-click listener for a particular view within a container.
     * 
     * @param container
     *            the containing view.
     * @param viewId
     *            the id of the view within <code>container</code>.
     * @param listener
     *            the listener.
     */
    protected void setViewOnClickListener(View container, int viewId, View.OnClickListener listener) {
        if (container != null) {
            View clickView = container.findViewById(viewId);
            if (clickView != null) {
                clickView.setOnClickListener(listener);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setViewOnClickListener: unable to locate 'View' in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setViewOnClickListener: container is null!");
        }
    }

    /**
     * Will set the text in an <code>EditText</code> view within <code>container</code>.
     * 
     * @param container
     *            the containing view.
     * @param editTextId
     *            the resource id of the edit text view.
     * @param text
     *            the text.
     */
    protected void setEditTextText(View container, int editTextId, String text) {
        if (container != null) {
            EditText editText = (EditText) container.findViewById(editTextId);
            if (editText != null) {
                editText.setText(text);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextText: unable to locate 'EditText' view in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextText: container is null!");
        }
    }

    /**
     * Will get the text in an <code>EditText</code> view within <code>container</code>.
     * 
     * @param container
     *            the containing view.
     * @param editTextId
     *            the resource id of the edit text view.
     * @return the text contained within the field.
     */
    protected String getEditTextText(View container, int editTextId) {
        String retVal = null;
        if (container != null) {
            EditText editText = (EditText) container.findViewById(editTextId);
            if (editText != null) {
                retVal = editText.getText().toString();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getEditTextText: unable to locate 'EditText' view in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getEditTextText: container is null!");
        }
        return retVal;
    }

    /**
     * Will build a static text view with a label and value.
     * 
     * @param inflater
     *            the inflater used inflate the view.
     * @return an static text view.
     */
    protected View buildStaticTextView(LayoutInflater inflater) {
        View retVal = null;
        retVal = inflater.inflate(R.layout.static_text_form_field, null);
        if (retVal != null) {
            CharSequence txtVal = "";
            if (frmFld.getLabel() != null) {
                txtVal = buildLabel();
            }
            setTextViewText(retVal, R.id.field_name, txtVal);
            txtVal = "";
            if (frmFld.getValue() != null) {
                txtVal = formatValueForDisplay(frmFld.getValue());
            }
            setTextViewText(retVal, R.id.field_value, txtVal);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildStaticTextView: can't inflate layout 'static_text_form_field'.");
        }
        return retVal;
    }

    /**
     * Will format the value based on any data type information.
     * 
     * @param value
     *            the raw value to format.
     * 
     * @return the formatted value.
     */
    protected String formatValueForDisplay(String value) {
        String retVal = value;
        if (value != null) {
            if (frmFld.getDataType() != null) {
                switch (frmFld.getDataType()) {
                case TIMESTAMP: {
                    Calendar calendar = Parse.parseXMLTimestamp(frmFld.getValue());
                    if (calendar != null) {
                        retVal = Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, calendar);
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".formatValue: unable to convert timestamp value of '" + frmFld.getValue()
                                        + "' into a Calendar object!");
                    }
                    break;
                }
                case MONEY: {
                    // Convert from US locale to device locale.
                    Double amtDbl = FormatUtil.parseAmount(value, Locale.US);
                    if (amtDbl != null) {
                        String crnCode = (listener.getExpenseReportEntry() != null) ? listener.getExpenseReportEntry().transactionCrnCode
                                : listener.getExpenseReport().crnCode;
                        retVal = FormatUtil.formatAmount(amtDbl, ConcurCore.getContext().getResources()
                                .getConfiguration().locale, crnCode, false);
                    }
                    break;
                }
                case NUMERIC: {
                    Double amtDbl = FormatUtil.parseAmount(value, Locale.US);
                    if (amtDbl != null) {
                        // MOB-20261 no separator for numeric fields for integer values
                        boolean hasDecimalPlaces = value != null && value.matches(".*\\.0*[1-9]+0*");

                        if (!hasDecimalPlaces)
                            deviceLocaleNumericFormatter.setGroupingUsed(false);
                        else
                            deviceLocaleNumericFormatter.setGroupingUsed(true);
                        retVal = deviceLocaleNumericFormatter.format(amtDbl);
                    }
                    break;
                }
                }
            }
        }
        return retVal;
    }

    /**
     * Will format <code>value</code> appropriate for transmitting to the server according to the data type.
     * 
     * @param value
     *            the value to be formatted.
     * @return the value formatted for transmit to the server.
     */
    public String formatValueForWire(String value) {
        String retVal = value;
        if (value != null) {
            if (frmFld.getDataType() != null) {
                switch (frmFld.getDataType()) {
                case TIMESTAMP: {
                    // No-op.
                    break;
                }
                case MONEY:
                case NUMERIC: {
                    // Convert value in device locale to wire format.
                    Double amtDbl = FormatUtil.parseAmount(value, Locale.getDefault());
                    if (amtDbl != null) {
                        // Format as US standard, up to 20 digits to the right of the decimal.
                        NumberFormat nf = DecimalFormat.getInstance(Locale.US);
                        nf.setMaximumFractionDigits(20);
                        nf.setGroupingUsed(false);
                        retVal = nf.format(amtDbl);
                    }
                    break;
                }
                }
            }
        }
        return retVal;
    }

    /**
     * Will set the hint text on an <code>EditText</code> view.
     * 
     * @param container
     *            the view containing the <code>EditText</code> view.
     * @param editTextId
     *            the resource ID of the <code>EditText</code> view within <code>container</code>.
     * @param hintText
     *            the hint text.
     */
    protected void setEditTextHint(View container, int editTextId, String hintText) {
        if (container != null) {
            EditText editText = (EditText) container.findViewById(editTextId);
            if (editText != null) {
                if (hintText != null) {
                    editText.setHint(hintText);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextHint: unable to locate 'EditText' view in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextHint: container is null!");
        }
    }

    /**
     * Will set the input type on a <code>EditText</code> view based on the data type backing this form.
     * 
     * @param container
     *            the view containing the <code>EditText</code> view.
     * @param editTextId
     *            the resource ID of the <code>EditText</code> view within <code>container</code>.
     */
    protected void setEditTextInputType(View container, int editTextId) {
        if (container != null) {
            EditText editText = (EditText) container.findViewById(editTextId);
            if (editText != null) {
                if (frmFld != null) {
                    if (frmFld.getDataType() != null) {
                        switch (frmFld.getDataType()) {
                        case MONEY: {
                            editText.setInputType((InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
                            editText.setKeyListener(FormatUtil.getLocaleDecimalListener(listener.getActivity()));
                            break;
                        }
                        case NUMERIC: {
                            editText.setInputType((InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));
                            editText.setKeyListener(FormatUtil.getLocaleDecimalListener(listener.getActivity()));
                            break;
                        }
                        case INTEGER: {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            break;
                        }
                        }
                    } else {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: form field data type is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: frmFld is null!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: unable to locate 'EditText' view in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: container is null!");
        }
    }

    /**
     * Will set the input length on a <code>EditText</code> view based on the data type backing this form.
     * 
     * @param container
     *            the view containing the <code>EditText</code> view.
     * @param editTextId
     *            the resource ID of the <code>EditText</code> view within <code>container</code>.
     */
    protected void setEditTextInputLength(View container, int editTextId) {
        if (container != null) {
            EditText editText = (EditText) container.findViewById(editTextId);
            if (editText != null) {
                setEditTextInputLength(editText);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: unable to locate 'EditText' view in container!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: container is null!");
        }
    }

    /**
     * Will set the hint text for <code>editText</code> to <code>hintText</code>.
     * 
     * @param editText
     *            the edit text upon which to set the hint.
     * @param hintText
     *            the hint text.
     */
    protected void setEditTextHint(EditText editText, String hintText) {
        if (editText != null && hintText != null) {
            editText.setHint(hintText);
        }
    }

    /**
     * Will set the input length on a <code>EditText</code> view based on the data type backing this form.
     * 
     * @param editText
     *            the edit text.
     */
    protected void setEditTextInputLength(EditText editText) {
        if (frmFld != null) {
            if (frmFld.getMaxLength() != -1) {
                // MOB-20261/CRMC-43484 Do not count optional group separator in length for NUMERIC field.
                // Need to be cautious on expanding to other fields, e.g. MONEY (length is currency dependent).
                InputFilter.LengthFilter filter = new FormFieldLengthFilter(frmFld.getMaxLength(), frmFld.getDataType()
                        .equals(ExpenseReportFormField.DataType.NUMERIC));
                InputFilter[] inputFilters = editText.getFilters();
                if (inputFilters == null) {
                    inputFilters = new InputFilter[1];
                    inputFilters[0] = filter;
                } else {
                    // Attempt to replace all existing length filters with 'filter'.
                    boolean replacedExistingLengthFilter = false;
                    for (int filtInd = 0; filtInd < inputFilters.length; ++filtInd) {
                        if (inputFilters[filtInd] instanceof InputFilter.LengthFilter) {
                            replacedExistingLengthFilter = true;
                            inputFilters[filtInd] = filter;
                        }
                    }
                    if (!replacedExistingLengthFilter) {
                        // If there was no replacement, then add onto the end.
                        InputFilter[] replacementFilters = new InputFilter[inputFilters.length + 1];
                        System.arraycopy(inputFilters, 0, replacementFilters, 0, inputFilters.length);
                        replacementFilters[replacementFilters.length - 1] = filter;
                        inputFilters = replacementFilters;
                    }
                }
                // Reset the input filters.
                editText.setFilters(inputFilters);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setEditTextInputType: frmFld is null!");
        }
    }

    /**
     * Builds an instance of <code>CharSequence</code> that optionally contains a styled "(required)" string.
     * 
     * @return An instance of <code>CharSequence</code> containing the field label.
     */
    public CharSequence buildLabel(CharSequence label) {
        if (label != null) {
            if (frmFld.isRequired() && frmFld.getAccessType() != ExpenseReportFormField.AccessType.RO) {
                SpannableStringBuilder strBldr = new SpannableStringBuilder(label);
                strBldr.append(' ');
                int spanStart = strBldr.length();
                strBldr.append('(');
                strBldr.append(listener.getActivity().getText(R.string.required));
                strBldr.append(')');

                // Turn the label red if the field is blank
                int requiredStyle = R.style.FormFieldLabelRequired;
                String val = getFormFieldValue();
                if (val == null || val.trim().length() < 1) {
                    strBldr.setSpan(new TextAppearanceSpan(listener.getActivity(), R.style.PrimaryTextRed), 0,
                            spanStart, 0);

                    requiredStyle = R.style.FormFieldLabelRequiredBlank;
                }

                strBldr.setSpan(new TextAppearanceSpan(listener.getActivity(), requiredStyle), spanStart,
                        strBldr.length(), 0);

                label = strBldr;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildLabel: form fields label is null!");
        }
        return label;
    }

    /**
     * Gets the current value in the underlying form field.
     * 
     * @return returns the current value in the underlying form field.
     */
    protected String getFormFieldValue() {
        return frmFld.getValue();
    }

    /**
     * Builds an instance of <code>CharSequence</code> that optionally contains a styled "(required)" string.
     * 
     * @return An instance of <code>CharSequence</code> containing the field label.
     */
    public CharSequence buildLabel() {
        return buildLabel(frmFld.getLabel());
    }

    /**
     * This filter will constrain edits not to make the length of the text greater than the specified length.
     * 
     * Note: MOB-20261/CRMC-43484 Do not count optional group separator in length for NUMERIC field. Need to be cautious on
     * expanding to other fields, e.g. MONEY (length is currency dependent).
     */
    public static class FormFieldLengthFilter extends InputFilter.LengthFilter {

        /**
         * Constructor with max length and whether to check optional characters for numeric fields.
         * 
         * @param max
         *            max length for the field
         * @param isNumeric
         *            whether to check optional characters for numeric fields
         */
        public FormFieldLengthFilter(int max, boolean isNumeric) {
            super(max);
            mMax = max;
            mIsNumeric = isNumeric;
        }

        /**
         * Adapted from InputFilter.LengthFilter to not count optional characters for numeric fields.
         */
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            int keep = mMax - (dest.length() - (dend - dstart));

            if (mIsNumeric) {
                // count group separators and exclude from counting
                DecimalFormat formatter = (DecimalFormat) FormFieldView.deviceLocaleNumericFormatter;
                DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
                char groupingSeparator = symbols.getGroupingSeparator();
                for (int ix = 0; ix < dest.length(); ix++) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".filter: cur char " + dest.charAt(ix));
                    // Increment keep for every separator found outside of replacement region
                    if ((ix < dstart || ix >= dend) && dest.charAt(ix) == groupingSeparator)
                        keep++;
                }
                // Allow insertion of additional separator
                for (int jx = start; jx < end; jx++) {
                    if (source.charAt(jx) == groupingSeparator)
                        keep++;
                }
            }

            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }

                return source.subSequence(start, keep);
            }
        }

        private int mMax;
        private boolean mIsNumeric;
    }
}
