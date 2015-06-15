package com.concur.mobile.platform.ui.common.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;

import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>FormFieldView</code> to construct and manage a view containing boolean information.
 * 
 * @author AndrewK
 */
public class BooleanFormFieldView extends FormFieldView {

    private static final String CLS_TAG = BooleanFormFieldView.class.getSimpleName();

    private static final String BOOLEAN_BUNDLE_KEY = "state";

    /**
     * Constructs an instance of <code>BooleanFormFieldView</code> given an expense report form field.
     * 
     * @param frmFld
     *            the expense report form field.
     */
    public BooleanFormFieldView(IFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
        layoutResourceId = R.layout.checkbox_form_field;
    }

    @Override
    public View getView(Context context) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (frmFld.getAccessType()) {
            case RW: {
                switch (frmFld.getInputType()) {
                case USER: {
                    view = inflater.inflate(layoutResourceId, null);
                    if (view != null) {
                        setTextViewText(view, R.id.field_name, frmFld.getLabel());
                        // Set the state of the checked text view.
                        setCurrentValue(frmFld.getLiKey(), false);
                        // Enable focusability and make it clickable.
                        view.setFocusable(true);
                        view.setClickable(true);
                        // Add a click handler.
                        view.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                setCheckedTextViewState(view, R.id.field_name,
                                        !getCheckedTextViewState(view, R.id.field_name));
                                if (listener != null) {
                                    listener.valueChanged(BooleanFormFieldView.this);
                                }
                            }
                        });
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to inflate layout file 'checkbox_form_field'!");
                    }
                    break;
                }
                case CALC: {
                    // Calculated fields present static views.
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
                // No-op, no view will be built.
                break;
            }
            }
        }
        return view;
    }

    /**
     * Gets the checked state of a <code>CheckedTextView</code> contained in <code>view</code> with resource id
     * <code>checkedTextViewResId</code>.
     * 
     * @param container
     *            the checked text view container.
     * @param checkedTextViewResId
     *            the checked text view resource ID.
     * @return returns <code>true</code> if the text view is checked; <code>false</code> otherwise.
     */
    private boolean getCheckedTextViewState(View container, int checkedTextViewResId) {
        boolean retVal = false;
        if (container != null) {
            CheckedTextView chkTxtView = (CheckedTextView) container.findViewById(R.id.field_name);
            if (chkTxtView != null) {
                retVal = chkTxtView.isChecked();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setCheckedTextViewText: unable to locate 'field_name' CheckedTextView in view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCheckedTextViewText: view is null!");
        }
        return retVal;
    }

    /**
     * Sets the checked state of a <code>CheckedTextView</code> object.
     * 
     * @param container
     *            the view containing the checked text view.
     * @param checkedTextViewResId
     *            the resource id of the checked text view.
     * @param value
     *            the boolean checked state.
     */
    private void setCheckedTextViewState(View container, int checkedTextViewResId, boolean value) {
        if (container != null) {
            CheckedTextView chkTxtView = (CheckedTextView) container.findViewById(R.id.field_name);
            if (chkTxtView != null) {
                chkTxtView.setChecked(value);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setCheckedTextViewText: unable to locate 'field_name' CheckedTextView in view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCheckedTextViewText: view is null!");
        }
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public ValidityCheck isValueValid() {
        return SUCCESS;
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            boolean origValue = Parse.safeParseBoolean(frmFld.getLiKey()).booleanValue();
            boolean curValue = false;
            switch (frmFld.getInputType()) {
            case USER: {
                curValue = getCheckedTextViewState(view, R.id.field_name);
                break;
            }
            case CALC: {
                curValue = Parse.safeParseBoolean(getCurrentValue()).booleanValue();
                break;
            }
            default: {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: invalid input type -- defaulting to 'false'!");
                break;
            }
            }
            retVal = ((origValue && !curValue) || (!origValue && curValue));
        }
        return retVal;
    }

    @Override
    public String getCurrentValue() {
        String retVal = "N";
        switch (frmFld.getInputType()) {
        case USER: {
            retVal = getCheckedTextViewState(view, R.id.field_name) ? "Y" : "N";
            break;
        }
        case CALC: {
            retVal = getTextViewText(view, R.id.field_value);
            break;
        }
        default: {
            Log.w(Const.LOG_TAG, CLS_TAG + ".getCurrentValue: invalid input type -- defaulting to 'N'!");
            break;
        }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#setCurrentValue(java.lang.String)
     */
    @Override
    public void setCurrentValue(String value, boolean notify) {
        if (value != null) {
            switch (frmFld.getInputType()) {
            case USER: {
                setCheckedTextViewState(view, R.id.field_name, Parse.safeParseBoolean(value).booleanValue());
                if (notify && listener != null) {
                    listener.valueChanged(this);
                }
                break;
            }
            case CALC: {
                setTextViewText(view, R.id.field_value, value);
                if (notify && listener != null) {
                    listener.valueChanged(this);
                }
                break;
            }
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
                if (frmFldView instanceof BooleanFormFieldView) {
                    BooleanFormFieldView boolFrmFldView = (BooleanFormFieldView) frmFldView;
                    setCurrentValue(boolFrmFldView.getCurrentValue(), false);
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
        String key = getPrefixedKey(BOOLEAN_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            boolean restoredValue = bundle.getBoolean(key);
            setCurrentValue(((restoredValue) ? "Yes" : "No"), false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        bundle.putBoolean(getPrefixedKey(BOOLEAN_BUNDLE_KEY), Parse.safeParseBoolean(getCurrentValue()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        // Only save out the value if it has changed.
        if (hasValueChanged()) {
            bundle.putBoolean(getPrefixedKey(BOOLEAN_BUNDLE_KEY), Parse.safeParseBoolean(getCurrentValue()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            String curValStr = getCurrentValue();
            if (curValStr != null) {
                if (curValStr.equalsIgnoreCase("Y")) {
                    frmFld.setValue("Yes");
                } else if (curValStr.equalsIgnoreCase("N")) {
                    frmFld.setValue("No");
                }
            }
            frmFld.setLiKey(curValStr);
        }
    }
}
