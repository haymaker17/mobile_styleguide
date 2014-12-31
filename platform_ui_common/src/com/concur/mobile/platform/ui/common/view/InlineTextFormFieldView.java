package com.concur.mobile.platform.ui.common.view;

import java.math.BigInteger;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.concur.mobile.base.util.Format;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.ui.common.util.FormUtil;
import com.concur.mobile.platform.ui.common.util.FormatUtil;

/**
 * An extension of <code>FormFieldView</code> to construct and manage a view for editing single-line text items.
 * 
 * @author AndrewK
 */
public class InlineTextFormFieldView extends FormFieldView {

    private static final String CLS_TAG = InlineTextFormFieldView.class.getSimpleName();
    private static final String TRANSACTION_AMOUNT_FIELD_ID = "TransactionAmount";
    private static final long TEXT_NOTIFICATION_DELAY = 750L;

    private static final String INLINE_TEXT_BUNDLE_KEY = "inline.text";

    protected Handler notificationDelayHandler;
    protected Runnable notificationDelayRunnable;
    protected NotificationTextWatcher textWatcher;
    protected String hintText;

    protected Locale locale;

    /**
     * Constructs an instance of <code>InlineTextFormFieldView</code> given an expense report form field.
     * 
     * @param frmFld
     *            the expense report form field.
     * @param listener
     * @param locale
     *            , locale from context, e.g. ConcurCore.getContext() .getResources().getConfiguration().locale
     */
    public InlineTextFormFieldView(IFormField frmFld, IFormFieldViewListener listener, Locale locale) {
        super(frmFld, listener);
        notificationDelayHandler = new Handler();
        notificationDelayRunnable = new ChangeNotify();
        this.locale = locale;
    }

    /**
     * Sets the hint text to be displayed in the edit text field.
     * 
     * @param hintText
     *            contains the hint text.
     */
    public void setHintText(String hintText) {
        this.hintText = hintText;
        if (view != null) {
            setEditTextHint(view, fieldValueId, hintText);
        }
    }

    /**
     * Will initialize an instance of <code>View</code> containing an inline text edit widget.
     * 
     * @param view
     *            the view container.
     */
    public void initEditableInlineTextView(View view) {
        if (view != null) {
            // Set a 'fieldValueId' value.
            setViewFieldValueId(view);
            // Set the field label.
            setTextViewText(view, R.id.field_name, buildLabel());
            // Set the input type on the inline text field.
            setEditTextInputType(view, fieldValueId);
            setEditTextHint(view, fieldValueId, hintText);
            // NOTE: If 'frmFld.copyDownSource' is not null and 'frmFld.maxLength' != -1 and
            // the current 'frmFld.value' is not null and it's length is greater than 'frmFld.maxLength',
            // then don't impose a length restriction on the field.
            // MOB-4894.
            if (frmFld.getCopyDownSource() != null && frmFld.getCopyDownSource().length() > 0
                    && frmFld.getMaxLength() != -1 && frmFld.getValue() != null
                    && frmFld.getValue().length() > frmFld.getMaxLength()) {
                // No-op.
            } else {
                // Set any input length restriction.
                setEditTextInputLength(view, fieldValueId);
            }
            // Set the value.
            String txtValue = "";
            if (frmFld.getValue() != null) {
                txtValue = frmFld.getValue();
            }
            setEditTextText(view, fieldValueId, formatValueForDisplay(txtValue));
            EditText editText = (EditText) view.findViewById(fieldValueId);
            if (editText != null) {
                textWatcher = new NotificationTextWatcher();
                editText.addTextChangedListener(textWatcher);
                if (frmFld.getDataType() != null) {
                    switch (frmFld.getDataType()) {
                    case MONEY:
                    case NUMERIC: {
                        editText.setKeyListener(FormatUtil.getLocaleDecimalListener(listener.getActivity()));
                        break;
                    }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to located EditText view!");
            }
            view.setTag(frmFld.getId());
        }
        this.view = view;
    }

    @Override
    public View getView(Context context) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (frmFld.getAccessType()) {
            case RW: {
                switch (frmFld.getInputType()) {
                case USER: {
                    view = inflater.inflate(R.layout.edit_text_form_field, null);
                    if (view != null) {
                        initEditableInlineTextView(view);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".getView: unable to inflate layout file 'edit_text_form_field'!");
                    }
                    break;
                }
                case CALC: {
                    view = buildStaticTextView(inflater);
                    if (view != null) {
                        setViewFieldValueId(view);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: view is null!");
                    }
                    break;
                }
                }
                break;
            }
            case RO: {
                view = buildStaticTextView(inflater);
                if (view != null) {
                    setViewFieldValueId(view);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: view is null!");
                }
                break;
            }
            case HD: {
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

    @Override
    public boolean hasValue() {
        String strVal = null;
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            strVal = getTextViewText(view, fieldValueId).trim();
        } else {
            strVal = frmFld.getValue();
        }
        return (strVal != null && strVal.length() > 0);
    }

    @Override
    public ValidityCheck isValueValid() {
        ValidityCheck check = new ValidityCheck();
        check.result = true;
        // Skip check for validity if field is hidden.
        if (frmFld.getAccessType() != IFormField.AccessType.HD && frmFld.getAccessType() == IFormField.AccessType.RW
                && frmFld.isVerifyValue()) {
            // MOB-10638 - Company Car Mileage can have 0 amount value.
            String curValue = getCurrentValue();
            if (curValue != null && curValue.length() > 0) {
                if (frmFld.getDataType() != null) {
                    switch (frmFld.getDataType()) {
                    case MONEY:
                        // check transaction amount != 0
                        // MOB-10783 - also check amount is less than 1 quadrillion (the max amount on CTE).
                        if (TRANSACTION_AMOUNT_FIELD_ID.equalsIgnoreCase(frmFld.getId())) {
                            Double finalAmount = FormatUtil.parseAmount(curValue, this.locale);
                            // MOB-10928 - negative amounts are okay.
                            // if (finalAmount == null || finalAmount == 0) {
                            // TODO zero amount is OK
                            if (finalAmount == null) {
                                check.result = false;
                                check.reason = Format.localizeText(listener.getActivity(),
                                        R.string.general_field_value_invalid, curValue);
                            } else if (finalAmount > Double.valueOf(1000000000000000.00)) {
                                check.result = false;
                                check.reason = Format.localizeText(listener.getActivity(),
                                        R.string.general_field_value_too_large, curValue);
                            } else {
                                check.result = true;
                            }
                            break;
                        }

                    case NUMERIC: {
                        // Check whether the end-user has entered a valid number.
                        Double amtDbl = FormatUtil.parseAmount(curValue, locale);
                        check.result = (amtDbl != null);
                        break;
                    }
                    case INTEGER: {
                        // Check whether the end-user has entered a valid integer value.
                        try {
                            BigInteger bigInt = new BigInteger(curValue);
                            if (bigInt.doubleValue() > Integer.MAX_VALUE) {
                                check.result = false;
                                check.reason = Format.localizeText(listener.getActivity(),
                                        R.string.general_field_value_too_large, curValue);
                            } else {
                                check.result = true;
                            }
                        } catch (NumberFormatException nfe) {
                            // Number can't be parsed as an integer.
                            check.result = false;
                            check.reason = listener.getActivity().getText(R.string.general_field_value_invalid)
                                    .toString();
                        }
                        break;
                    }
                    case VARCHAR: {
                        check = FormUtil.isVarCharValueValid(frmFld, listener.getActivity(), curValue);
                        break;
                    }
                    default:
                        break;
                    }
                }
            }
        }
        return check;
    }

    @Override
    public void displayFieldNoteIfValueInvalid() {
        FormUtil.displayFieldNoteIfInvalid(view, isValueValid(), listener.getActivity());
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            String editedValue = getTextViewText(view, fieldValueId);
            editedValue = (editedValue != null) ? editedValue.trim() : "";
            String origValue = (frmFld.getValue() != null) ? frmFld.getValue() : "";
            // origValue = formatValueForDisplay(origValue);

            switch (frmFld.getDataType()) {
            case MONEY:
            case NUMERIC: {
                Double origDouble = FormatUtil.parseAmount(origValue, Locale.US);
                Double editedDouble = FormatUtil.parseAmount(editedValue, Locale.getDefault());
                if (origDouble != null && editedDouble != null) {
                    // Both are non-null; hence compare the numbers.
                    retVal = !editedDouble.equals(origDouble);
                } else if ((origDouble != null && editedDouble == null) || (origDouble == null && editedDouble != null)) {
                    // One or the other, but not both, are null, so one must have
                    // changed.
                    retVal = true;
                } else {
                    // Both are null, so no change has occurred.
                    retVal = false;
                }
                // try {
                // Double origDouble = FormatUtil.parseAmount(origValue, Locale.getDefault());
                // Double editedDouble = FormatUtil.parseAmount(editedValue, Locale.getDefault());
                //
                // retVal = !editedDouble.equals(origDouble);
                // } catch (Exception e) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: error parsing double. " + origValue + " -- "
                // + editedValue);
                // }

                break;
            }
            case INTEGER: {
                Integer origInteger = null;
                try {
                    origInteger = Integer.valueOf(origValue);
                } catch (NumberFormatException numFormExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: unable to parse origValue of '" + origValue
                            + "'.");
                }
                Integer editedInteger = null;
                try {
                    editedInteger = Integer.valueOf(editedValue);
                } catch (NumberFormatException numFormExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: unable to parse editedValue of '" + editedValue
                            + "'.");
                }
                if (origInteger != null && editedInteger != null) {
                    // Both are non-null; hence compare the numbers.
                    retVal = !editedInteger.equals(origInteger);
                } else if ((origInteger == null && editedInteger != null)
                        || (origInteger != null && editedInteger == null)) {
                    // One or the other, but not both, are null, so one must have
                    // changed.
                    retVal = true;
                } else {
                    // Both are null, so no change has occurred.
                    retVal = false;
                }

                // try {
                // Integer origInteger = Integer.valueOf(origValue);
                // Integer editedInteger = Integer.valueOf(editedValue);
                //
                // retVal = !editedInteger.equals(origInteger);
                // } catch (Exception e) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: error parsing integer. " + origValue + " -- "
                // + editedValue);
                // }
                break;
            }
            default:
                retVal = !editedValue.contentEquals(origValue);
                break;
            }
        }
        return retVal;
    }

    @Override
    public String getCurrentValue() {
        String retVal = getTextViewText(view, fieldValueId);
        if (retVal != null) {
            retVal = retVal.trim();
        }
        return retVal;
    }

    /**
     * Will set the current value within this inline text form field.
     * 
     * @param value
     *            the current value.
     * @param notify
     *            whether to notify a listener of this value change.
     */
    @Override
    public void setCurrentValue(String value, boolean notify) {
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            // Disable the text watcher if we have one.
            if (textWatcher != null && !notify) {
                textWatcher.enabled = false;
            }
            switch (frmFld.getInputType()) {
            case USER: {
                setEditTextText(view, fieldValueId, value);
                break;
            }
            case CALC: {
                setTextViewText(view, fieldValueId, value);
                break;
            }
            }
            // Re-enable the text watcher if we have one.
            if (textWatcher != null) {
                if (!notify) {
                    textWatcher.enabled = true;
                }
            } else if (listener != null && notify) {
                displayFieldNoteIfValueInvalid();
                listener.valueChanged(this);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#updateEditedValue(com.concur.mobile.util.FormFieldView)
     */
    @Override
    public void updateEditedValue(FormFieldView frmFldView) {
        // Check for whether this field is editable.
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            // Check for whether value has changed in source form field view.
            if (frmFldView.hasValueChanged()) {
                // Check for same type of field.
                if (frmFldView instanceof InlineTextFormFieldView) {
                    InlineTextFormFieldView inTxtFrmFldView = (InlineTextFormFieldView) frmFldView;
                    setCurrentValue(inTxtFrmFldView.getCurrentValue(), false);
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
        String key = getPrefixedKey(INLINE_TEXT_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            setCurrentValue(bundle.getString(key), false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        bundle.putString(getPrefixedKey(INLINE_TEXT_BUNDLE_KEY), getCurrentValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (hasValueChanged()) {
            bundle.putString(getPrefixedKey(INLINE_TEXT_BUNDLE_KEY), getCurrentValue());
        }
    }

    @Override
    public void commit() {
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            frmFld.setValue(formatValueForWire(getCurrentValue().trim()));
        }
    }

    /**
     * An implementation of <code>TextWatcher</code> that tracks when changes occur within an edit text view.
     * 
     * @author andy
     */
    class NotificationTextWatcher implements TextWatcher {

        boolean enabled = true;

        @Override
        public void afterTextChanged(Editable s) {
            if (enabled) {
                doNotification(TEXT_NOTIFICATION_DELAY);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No-op.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // No-op.
        }

    }

    /**
     * An implementation of <code>Runnable</code> for the purposes of providing a notification of a change occurring.
     * 
     * @author andy
     */
    class ChangeNotify implements Runnable {

        @Override
        public void run() {
            if (listener != null) {
                displayFieldNoteIfValueInvalid();
                listener.valueChanged(InlineTextFormFieldView.this);
            }
        }
    }

    /**
     * Will post a notification to a handler to be executed after <code>delay</code> milliseconds.
     * 
     * @param delay
     *            the delay in milliseconds.
     */
    protected void doNotification(long delay) {
        notificationDelayHandler.removeCallbacks(notificationDelayRunnable);
        if (delay > 0L) {
            notificationDelayHandler.postDelayed(notificationDelayRunnable, delay);
        } else {
            notificationDelayHandler.post(notificationDelayRunnable);
        }
    }

}
