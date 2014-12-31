/**
 * 
 */
package com.concur.mobile.core.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormUtil;

/**
 * An extension of <code>FormFieldView</code> to construct and manage a view for editing multi-line text information.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.MultiLineTextFormFieldView} instead.
 * 
 * @author AndrewK
 */
public class MultiLineTextFormFieldView extends FormFieldView {

    private static final String CLS_TAG = MultiLineTextFormFieldView.class.getSimpleName();

    private static final int MULTI_LINE_TEXT_DIALOG = DIALOG_ID_BASE + 0;

    private static final String VALUE_BUNDLE_KEY = "value";

    private static final String LAST_CHANGED_TEXT_BUNDLE_KEY = "last.changed.text";

    private EditText textEdit;

    // Contains the last changed text as reported via the text change listener.
    // This is used to track the last changed text so that in the event of an orientation
    // change it can be re-populated into the field.
    private String lastChangedText;

    // Contains the edited value.
    private String value;

    private String hintText;

    /**
     * Constructs an instance of <code>MultiLineTextFormFieldView</code> given an expense report form field.
     * 
     * @param frmFld
     *            the expense report form field.
     */
    public MultiLineTextFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
        value = frmFld.getValue();
    }

    /**
     * Sets the hint text to be displayed in the edit text field.
     * 
     * @param hintText
     *            contains the hint text.
     */
    public void setHintText(String hintText) {
        this.hintText = hintText;
        setEditTextHint(textEdit, this.hintText);
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
                        String txtVal = "";
                        if (value != null) {
                            txtVal = value;
                        }
                        setTextViewText(view, R.id.field_value, txtVal);
                        // Set the expense type icon.
                        // setImageViewImage(view, R.id.field_image, android.R.drawable.ic_menu_more);
                        // Enable focusability and make it clickable.
                        // These are set in the layout now.
                        // view.setFocusable(true);
                        // view.setClickable(true);
                        // Add a click handler.
                        view.setOnClickListener(new View.OnClickListener() {

                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.showDialog(MultiLineTextFormFieldView.this, MULTI_LINE_TEXT_DIALOG);
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
        }
        return view;
    }

    @Override
    public boolean hasValue() {
        // First check whether the end-user has set a value through a pop-up dialog, if not, then
        // check if the frmFld.getValue contains a non zero-length value.
        boolean hasValue = false;
        if (value != null) {
            hasValue = (value.trim().length() > 0);
        } else if (frmFld.getValue() != null) {
            hasValue = (frmFld.getValue().trim().length() > 0);
        }
        return hasValue;
    }

    @Override
    public ValidityCheck isValueValid() {
        String curValue = getCurrentValue();
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW && curValue != null && curValue.length() > 0) {
            return FormUtil.isVarCharValueValid(frmFld, listener.getActivity(), value);
        }
        return new ValidityCheck(true, null);
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            String origValue = (frmFld.getValue() != null) ? frmFld.getValue() : "";
            if (value != null) {
                retVal = !value.contentEquals(origValue);
            }
        }
        return retVal;
    }

    @Override
    public String getCurrentValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#setCurrentValue(java.lang.String, boolean)
     */
    @Override
    public void setCurrentValue(String value, boolean notify) {
        this.value = value;
        if (listener != null && notify) {
            listener.valueChanged(this);
        }
        setTextViewText(view, R.id.field_value, value);
        FormUtil.displayFieldNoteIfInvalid(view, isValueValid(), listener.getActivity());
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
                if (frmFldView instanceof MultiLineTextFormFieldView) {
                    MultiLineTextFormFieldView mltLineTxtFrmFldView = (MultiLineTextFormFieldView) frmFldView;
                    value = mltLineTxtFrmFldView.value;
                    setTextViewText(view, R.id.field_value, value);
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
        // Restore 'value'.
        String key = getPrefixedKey(VALUE_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            setCurrentValue(bundle.getString(key), false);
        }
        // Restore 'lastChangedText'.
        key = getPrefixedKey(LAST_CHANGED_TEXT_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            lastChangedText = bundle.getString(key);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        bundle.putString(getPrefixedKey(VALUE_BUNDLE_KEY), value);
        if (lastChangedText != null) {
            bundle.putString(getPrefixedKey(LAST_CHANGED_TEXT_BUNDLE_KEY), lastChangedText);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (hasValueChanged()) {
            bundle.putString(getPrefixedKey(VALUE_BUNDLE_KEY), value);
        }
        if (lastChangedText != null) {
            bundle.putString(getPrefixedKey(LAST_CHANGED_TEXT_BUNDLE_KEY), lastChangedText);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        if (value != null) {
            frmFld.setValue(value.trim());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onCreateDialog(int)
     */
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case MULTI_LINE_TEXT_DIALOG: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(listener.getActivity());
            dlgBldr.setTitle(frmFld.getLabel());
            dlgBldr.setCancelable(true);
            dlgBldr.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    listener.clearCurrentFormFieldView();
                    listener.getActivity().removeDialog(MULTI_LINE_TEXT_DIALOG);
                    lastChangedText = null;
                }
            });
            dlgBldr.setPositiveButton(listener.getActivity().getText(R.string.okay),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            listener.clearCurrentFormFieldView();
                            listener.getActivity().removeDialog(MULTI_LINE_TEXT_DIALOG);
                            setCurrentValue(textEdit.getText().toString().trim(), true);
                            lastChangedText = null;
                        }
                    });
            dlgBldr.setNegativeButton(listener.getActivity().getText(R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            listener.clearCurrentFormFieldView();
                            listener.getActivity().removeDialog(MULTI_LINE_TEXT_DIALOG);
                            lastChangedText = null;
                        }
                    });

            textEdit = new EditText(listener.getActivity());
            textEdit.setMinLines(3);
            textEdit.setMaxLines(3);
            textEdit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            setEditTextInputLength(textEdit);
            setEditTextHint(textEdit, hintText);
            textEdit.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                    lastChangedText = s.toString();
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No-op.
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // No-op.
                }
            });
            dlgBldr.setView(textEdit);
            dialog = dlgBldr.create();
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: dialog id (" + id
                    + ") not of value 'MULTI_LINE_TEXT_DIALOG'!");
            break;
        }
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case MULTI_LINE_TEXT_DIALOG: {
            if (textEdit != null) {
                String txtVal = (value != null) ? value : "";
                if (lastChangedText != null) {
                    txtVal = lastChangedText;
                }
                textEdit.setText(txtVal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: textEdit is null!");
            }
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: dialog id (" + id
                    + ") not of value 'MULTI_LINE_TEXT_DIALOG'!");
            break;
        }
        }
    }

}
