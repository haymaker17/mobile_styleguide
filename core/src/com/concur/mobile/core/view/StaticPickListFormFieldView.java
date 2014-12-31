/**
 * 
 */
package com.concur.mobile.core.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>FormFieldView</code> for providing a static picklist.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.StaticPickListFormFieldView} instead.
 */
public class StaticPickListFormFieldView extends FormFieldView {

    private static final String CLS_TAG = StaticPickListFormFieldView.class.getSimpleName();

    private static final int LIST_DIALOG = DIALOG_ID_BASE + 0;

    private static String VALUE_BUNDLE_KEY = "value";

    protected SpinnerItem curValue;

    protected SpinnerItem[] items;

    /**
     * Constructs an instance of <code>StaticPickListFormFieldView</code> based on a report form field.
     * 
     * @param frmFld
     *            the report form field.
     * @param listener
     *            the view listener.
     */
    public StaticPickListFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        this(frmFld, listener, null);
    }

    /**
     * Constructs an instance of <code>StaticPickListFormFieldView</code> based on a report form field.
     * 
     * @param frmFld
     *            the report form field.
     * @param listener
     *            the view listener.
     * @param items
     *            the list of items
     */
    public StaticPickListFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener,
            SpinnerItem[] items) {
        super(frmFld, listener);

        setItems(items);
    }

    protected void setItems(SpinnerItem[] items) {

        // Set list items
        this.items = items;

        // Set default value
        if (this.items != null && frmFld.getLiKey() != null) {
            curValue = findItem(frmFld.getLiKey());
        }
    }

    protected SpinnerItem findItem(String id) {
        SpinnerItem item = null;
        if (items != null && id != null) {
            for (int i = 0; i < items.length; i++) {
                // if (id.equals(items[i].id)) {
                // item = items[i];
                // break;
                // }

                /*
                 * Following changes has been made by Sunil to support Gov 1.0 release. Previously to find spinner item,
                 * implementation was to compare id. if id matches then get that spinner item item as a selected item and return.
                 * Now, as we need to support Gov1.0 following changes are required. (Approved by Walt.)
                 */

                String spinnerId = items[i].id;
                String spinnerName = items[i].name;
                if (id.equals(spinnerId)) {
                    item = items[i];
                    break;
                } else if (id.equals(spinnerName)) {
                    item = items[i];
                    break;
                }
            }
        }
        return item;
    }

    @Override
    public void commit() {
        if (curValue != null) {
            frmFld.setLiKey(curValue.id);
            frmFld.setValue(curValue.name);
        }
    }

    @Override
    public String getCurrentValue() {
        if (curValue != null) {
            return curValue.id;
        }
        return null;
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
                        updateView();
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
                                    listener.showDialog(StaticPickListFormFieldView.this, LIST_DIALOG);
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
            default:
                break;
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
        return curValue != null;
    }

    @Override
    public boolean hasValueChanged() {
        boolean hasChanged = false;
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            if (curValue != null) {
                hasChanged = !curValue.id.equals(frmFld.getLiKey());
            }
        }
        return hasChanged;
    }

    @Override
    public ValidityCheck isValueValid() {
        return SUCCESS;
    }

    @Override
    public void setCurrentValue(String value, boolean notify) {
        if (value != null) {
            curValue = findItem(value);

            if (listener != null && notify) {
                listener.valueChanged(this);
            }

            updateView();
        }
    }

    @Override
    public void updateEditedValue(FormFieldView frmFldView) {
        // Check for whether this field is editable.
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            // Check for whether value has changed in source form field view.
            if (frmFldView.hasValueChanged()) {
                // Check for same type of field.
                if (frmFldView instanceof StaticPickListFormFieldView) {
                    StaticPickListFormFieldView staticPckLstFrmFldView = (StaticPickListFormFieldView) frmFldView;
                    setCurrentValue(staticPckLstFrmFldView.getCurrentValue(), false);
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
        String key = getPrefixedKey(VALUE_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            curValue = findItem(bundle.getString(key));
            updateView();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        if (curValue != null) {
            bundle.putString(getPrefixedKey(VALUE_BUNDLE_KEY), curValue.id);
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
            bundle.putString(getPrefixedKey(VALUE_BUNDLE_KEY), curValue.id);
        }
    }

    /**
     * Will update the view, if one is present with the current value of <code>value</code>.
     */
    protected void updateView() {
        if (view != null) {
            String txtVal = "";
            if (curValue != null) {
                txtVal = curValue.name;
            }
            setTextViewText(view, R.id.field_value, txtVal);
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
        case LIST_DIALOG: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(listener.getActivity());
            dlgBldr.setTitle(frmFld.getLabel());
            dlgBldr.setCancelable(true);
            ArrayAdapter<SpinnerItem> listAdapter = new ArrayAdapter<SpinnerItem>(listener.getActivity(),
                    android.R.layout.simple_spinner_item, items) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    return super.getDropDownView(position, convertView, parent);
                }
            };

            listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            int selectedItem = -1;
            if (curValue != null) {
                for (int i = 0; i < items.length; i++) {
                    // if (curValue.id.equals(items[i].id)) {
                    // selectedItem = i;
                    // break;
                    // }
                    /*
                     * Following changes has been made by Sunil to support Gov 1.0 release. Previously to find selected item,
                     * implementation was to compare id. if id matches then get that indexed item as a selected item. Now, as we
                     * need to support Gov1.0 following changes are required. (Approved by Walt.)
                     */
                    if (curValue.id.equals(items[i].id) && curValue.name.equals(items[i].name)) {
                        selectedItem = i;
                        break;
                    }
                }
            }

            dlgBldr.setSingleChoiceItems(listAdapter, selectedItem, new OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    curValue = items[which];
                    updateView();
                    listener.clearCurrentFormFieldView();
                    listener.getActivity().removeDialog(LIST_DIALOG);
                    listener.valueChanged(StaticPickListFormFieldView.this);
                }
            });

            dlgBldr.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    listener.clearCurrentFormFieldView();
                    listener.getActivity().removeDialog(LIST_DIALOG);
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: dialog id (" + id + ") not of value 'LIST_DIALOG'!");
            break;
        }
        }
        return dialog;
    }

}
