package com.concur.mobile.platform.ui.common.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.concur.mobile.base.util.Format;
import com.concur.mobile.platform.common.FieldValueSpinnerItem;
import com.concur.mobile.platform.common.IListFieldItem;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.util.Const;

/**
 * An extension of <code>FormFieldView</code> to construct and manage a view from with a search can be performed.
 * 
 * @author AndrewK
 */
public class SearchListFormFieldView extends FormFieldView {

    private static final String CLS_TAG = SearchListFormFieldView.class.getSimpleName();

    private static final int PARENT_FIELD_DIALOG = DIALOG_ID_BASE + 0;

    public static final int SEARCH_LIST_REQUEST_CODE = REQUEST_ID_BASE + 1;

    private static final String LIST_ITEM_KEY_BUNDLE_KEY = "liKey";
    private static final String LIST_ITEM_CODE_BUNDLE_KEY = "liCode";
    private static final String VALUE_KEY = "value";

    // Contains the edited list item key value.
    protected String liKey;

    // Contains the edited list item code.
    protected String liCode;

    // Contains the edited value (Text).
    protected String value;
    // Contains the edited list item crnkey value.
    protected String liCrnKey;
    // Contains the edited list item crncode value.
    protected String liCrnCode;
    // A list of item keys to exclude
    protected String[] excludeKeys;

    public FieldValueSpinnerItem selectedListItem;

    /**
     * handle dedicated editing for the given form field
     */
    protected IFormFieldViewEditHandler editHandler;

    /**
     * Constructs an instance of <code>SearchListFormFieldValue</code> based on a report form field.
     * 
     * @param frmFld
     *            the report form field.
     */
    public SearchListFormFieldView(IFormField frmFld, IFormFieldViewListener listener, IFormFieldViewEditHandler eh) {
        super(frmFld, listener);
        liKey = frmFld.getLiKey();
        liCode = frmFld.getLiCode();
        value = frmFld.getValue();
        editHandler = eh;
        layoutResourceId = R.layout.image_form_field;
    }

    /**
     * Will clear out the current values.
     */
    public void clear() {
        liKey = null;
        liCode = null;
        value = null;
        excludeKeys = null;
        updateView();
    }

    /**
     * Gets the LiCode for the list selection.
     * 
     * @return the LiCode for the list selection.
     */
    public String getLiCode() {
        return liCode;
    }

    public void setLiCode(String liCode) {
        this.liCode = liCode;
    }

    /**
     * Gets the value for the list selection.
     * 
     * @return the value for the list selection.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value part of the list selection.
     * 
     * @param value
     *            the list selection value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the LiKey for the list selection.
     * 
     * @return the LiKey for the list selection.
     */
    public String getLiKey() {
        return liKey;
    }

    public void setLiKey(String liKey) {
        this.liKey = liKey;
    }

    public String getLiCrnKey() {
        return liCrnKey;
    }

    public void setLiCrnKey(String liCrnKey) {
        this.liCrnKey = liCrnKey;
    }

    public String getLiCrnCode() {
        return liCrnCode;
    }

    public void setLiCrnCode(String liCrnCode) {
        this.liCrnCode = liCrnCode;
    }

    public String[] getExcludeKeys() {
        return excludeKeys;
    }

    public void setExcludeKeys(String[] keys) {
        excludeKeys = keys;
    }

    /**
     * Will return an <code>Intent</code> object that can be used to launch a list search based on the <code>IFormField</code>
     * backing this view.
     * 
     * @return returns an <code>Intent</code> object that can be used to launch a list search based on the <code>IFormField</code>
     *         backing this view.
     */
    // protected Intent getListSearchLaunchIntent() {
    // Intent intent = new Intent(listener.getActivity(), listSearchClass);
    // if (frmFld.getId() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_FIELD_ID, frmFld.getId());
    // }
    // if (frmFld.getFtCode() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_FT_CODE, frmFld.getFtCode());
    // }
    // if (frmFld.getListKey() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_LIST_KEY, frmFld.getListKey());
    // }
    // if (frmFld.getLabel() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_TITLE, frmFld.getLabel());
    // }
    // ArrayList<IListFieldItem> ssl = frmFld.getSearchableStaticList();
    // if (ssl != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_STATIC_LIST, ssl);
    // }
    //
    // // Add any parent list item key if this is a connected list.
    // addParentLiKey(intent);
    // // Add any MRU intent extra.
    // addMRU(intent);
    // if (listener.getDocumentKey() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_REPORT_KEY, listener.getDocumentKey());
    // }
    // // Add any exclude keys
    // if (excludeKeys != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_EXCLUDE_KEYS, excludeKeys);
    // }
    //
    // // Add flag to show codes if needed
    // // The design puts this here (instead of totally embedding in ListSearch) so that we keep the possibility of one
    // // day switching this per field.
    // intent.putExtra(Const.EXTRA_LIST_SHOW_CODES, listener.shouldShowListCodes());
    //
    // return intent;
    // }

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
                                    if (parentHasValue()) {
                                        editHandler.onEditField(SearchListFormFieldView.this, listener,
                                                SEARCH_LIST_REQUEST_CODE);
                                        // Intent intent;
                                        // if (frmFld.hasLargeValueCount()) {
                                        // // Launch the static list with search activity
                                        // intent = getStaticAndDynamicListSearchLaunchIntent();
                                        // } else {
                                        // // Launch the list search activity with the appropriate parameters.
                                        // intent = getListSearchLaunchIntent();
                                        // }
                                        //
                                        // // Launch the search list activity.
                                        // listener.startActivityForResult(SearchListFormFieldView.this, intent,
                                        // SEARCH_LIST_REQUEST_CODE);
                                    } else {
                                        // Parent field has no value, display a dialog to the end-user indicating they
                                        // need to fill in the parent value.
                                        listener.showDialog(SearchListFormFieldView.this, PARENT_FIELD_DIALOG);
                                    }
                                }
                            }
                        });
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to inflate layout file 'image_form_field'!");
                    }

                }
                    break;
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
        boolean retVal = (value != null && value.length() > 0);
        return retVal;
    }

    @Override
    public ValidityCheck isValueValid() {
        return SUCCESS;
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        if (frmFld.getAccessType() == IFormField.AccessType.RW) {
            String origLiCode = (frmFld.getLiCode() != null) ? frmFld.getLiCode() : "";
            String origLiKey = (frmFld.getLiKey() != null) ? frmFld.getLiKey() : "";
            String origValue = (frmFld.getValue() != null) ? frmFld.getValue() : "";
            String curLiCode = (liCode != null) ? liCode : "";
            String curLiKey = (liKey != null) ? liKey : "";
            String curValue = (value != null) ? value : "";
            retVal = (!curLiCode.contentEquals(origLiCode) || !curLiKey.contentEquals(origLiKey) || !curValue
                    .contentEquals(origValue));
        }
        return retVal;
    }

    @Override
    public String getCurrentValue() {
        // TODO: Get the current value.
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#setCurrentValue(java.lang.String, boolean)
     */
    @Override
    public void setCurrentValue(String value, boolean notify) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#updateEditedValue(com.concur.mobile.util.FormFieldView)
     */
    @Override
    public void updateEditedValue(FormFieldView frmFldView) {

        // Check whether the list keys are non-null and have changed.
        if (frmFld.getListKey() != null && frmFldView.frmFld.getListKey() != null
                && !frmFld.getListKey().equalsIgnoreCase(frmFldView.frmFld.getListKey())) {
            // Due to an existing MWS bug, we'll clear out the 'value', 'liKey' and 'liCode' values
            // as the new attributes contian values from the previous expense type selection!
            value = null;
            frmFld.setValue(null);
            liKey = null;
            frmFld.setLiKey(null);
            liCode = null;
            frmFld.setLiCode(null);
            updateView();
        } else {
            // Check for whether this field is editable.
            if (frmFld.getAccessType() == IFormField.AccessType.RW) {
                // Check for whether value has changed in source form field view.
                if (frmFldView.hasValueChanged()) {
                    // Check for same type of field.
                    if (frmFldView instanceof SearchListFormFieldView) {
                        SearchListFormFieldView srchLstFrmFldView = (SearchListFormFieldView) frmFldView;
                        listItemSelected(srchLstFrmFldView.liCode, srchLstFrmFldView.liKey, srchLstFrmFldView.value);
                        // For MRU update crnCode,crnKey if applicable
                        this.liCrnCode = srchLstFrmFldView.liCrnCode;
                        this.liCrnKey = srchLstFrmFldView.liCrnKey;
                        updateView();
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onCreateDialog(int)
     */
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
        case PARENT_FIELD_DIALOG: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(listener.getActivity());
            dlgBldr.setTitle(R.string.dlg_dependent_field_value_title);
            dlgBldr.setMessage("");
            dlgBldr.setCancelable(true);
            dlgBldr.setPositiveButton(listener.getActivity().getText(R.string.okay),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            listener.getActivity().removeDialog(PARENT_FIELD_DIALOG);
                            listener.clearCurrentFormFieldView();
                        }
                    });
            dlgBldr.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    listener.getActivity().removeDialog(PARENT_FIELD_DIALOG);
                    listener.clearCurrentFormFieldView();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: dialog id (" + id
                    + ") not of value 'PARENT_FIELD_DIALOG'!");
            break;
        }
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case PARENT_FIELD_DIALOG: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            String parentFieldLabel = getParentFieldLabel();
            if (parentFieldLabel != null) {
                alertDlg.setMessage(Format.localizeText(listener.getActivity(),
                        R.string.dlg_dependent_field_value_message, parentFieldLabel));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: parentFieldLabel is null!");
            }
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: dialog id (" + id
                    + ") not of value 'PARENT_FIELD_DIALOG'!");
            break;
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

        boolean updateView = false;

        // Restore 'liKey'.
        String key = getPrefixedKey(LIST_ITEM_KEY_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            liKey = bundle.getString(key);
            updateView = true;
        }
        // Restore 'liCode'.
        key = getPrefixedKey(LIST_ITEM_CODE_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            liCode = bundle.getString(key);
            updateView = true;
        }
        // Restore 'value'.
        key = getPrefixedKey(VALUE_KEY);
        if (bundle.containsKey(key)) {
            value = bundle.getString(key);
            updateView = true;
        }

        // Restore the selected item
        if (bundle.containsKey("selectedListItem")) {
            selectedListItem = (FieldValueSpinnerItem) bundle.getSerializable("selectedListItem");
        }

        // Update the UI.
        if (updateView) {
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
        // Store 'liKey'.
        bundle.putString(getPrefixedKey(LIST_ITEM_KEY_BUNDLE_KEY), liKey);
        // Store 'liCode'.
        bundle.putString(getPrefixedKey(LIST_ITEM_CODE_BUNDLE_KEY), liCode);
        // Store 'value'.
        bundle.putString(getPrefixedKey(VALUE_KEY), value);
        // store selected item
        bundle.putSerializable("selectedListItem", selectedListItem);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (hasValueChanged()) {
            // Store 'liKey'.
            bundle.putString(getPrefixedKey(LIST_ITEM_KEY_BUNDLE_KEY), liKey);
            // Store 'liCode'.
            bundle.putString(getPrefixedKey(LIST_ITEM_CODE_BUNDLE_KEY), liCode);
            // Store 'value'.
            bundle.putString(getPrefixedKey(VALUE_KEY), value);
            // store selected item
            bundle.putSerializable("selectedListItem", selectedListItem);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        frmFld.setLiKey((liKey != null) ? liKey : "");
        frmFld.setLiCode((liCode != null) ? liCode : "");
        frmFld.setValue((value != null) ? value : "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult:requestCode '" + requestCode + "'. resultcode " + resultCode);
        switch (requestCode) {
        case SEARCH_LIST_REQUEST_CODE: {
            if (resultCode == Activity.RESULT_OK) {
                if (frmFld.hasLargeValueCount()) {
                    selectedListItem = (FieldValueSpinnerItem) data
                            .getSerializableExtra(Const.EXTRA_SEARCH_SELECTED_ITEM);
                    if (selectedListItem != null) {
                        String selectedText = (selectedListItem.optionText == null ? selectedListItem.value
                                : selectedListItem.optionText);
                        listItemSelected(frmFld.getLiCode(), selectedListItem.valueId, selectedText);
                        updateView();
                    }
                } else {
                    String selectedListItemKey = data.getStringExtra(Const.EXTRA_LIST_SELECTED_LIST_ITEM_KEY);
                    String selectedListItemCode = data.getStringExtra(Const.EXTRA_LIST_SELECTED_LIST_ITEM_CODE);
                    String selectedListItemText = data.getStringExtra(Const.EXTRA_LIST_SELECTED_LIST_ITEM_TEXT);
                    String crnCode = data.getStringExtra(Const.EXTRA_LIST_SELECTED_LIST_ITEM_CRN_CODE);
                    String crnKey = data.getStringExtra(Const.EXTRA_LIST_SELECTED_LIST_ITEM_CRN_KEY);
                    if (selectedListItemKey != null || selectedListItemCode != null) {

                        // Porting code change:
                        // Use listItem info directly from data bundle, instead of going through search results.
                        this.liCrnCode = crnCode;
                        this.liCrnKey = crnKey;
                        listItemSelected(selectedListItemCode, selectedListItemKey, selectedListItemText);
                        updateView();
                        // Only inform the listener if this form field view object's class object is
                        // 'SearchListFormFieldView'.
                        if (this.getClass().equals(SearchListFormFieldView.class)) {
                            if (listener != null) {
                                listener.valueChanged(this);
                            }
                        }

                        // old code - No need to go through last search results. All data needed are in result bundle
                        // Commented out begin
                        // See if this field is using a searchable static list
                        // List<IListFieldItem> items = frmFld.getSearchableStaticList();
                        // if (items == null) {
                        // // Nope, go get them from the app
                        // ConcurCore app = (ConcurCore) listener.getActivity().getApplication();
                        // final SearchListResponse expenseSearchListResults = app.getExpenseSearchListResults();
                        // if (expenseSearchListResults != null) {
                        // items = expenseSearchListResults.listItems;
                        // }
                        // }
                        //
                        // if (items != null) {
                        // for (IListFieldItem listItem : items) {
                        // if (listItem.getKey() != null
                        // && listItem.getKey().equalsIgnoreCase(selectedListItemKey)
                        // || listItem.getCode() != null
                        // && listItem.getCode().equalsIgnoreCase(selectedListItemCode)) {
                        // // For MRU set the value for currency code, currency key
                        // this.liCrnCode = crnCode;
                        // this.liCrnKey = crnKey;
                        // listItemSelected(listItem.getCode(), listItem.getKey(), listItem.getText());
                        // updateView();
                        // // Only inform the listener if this form field view object's class object is
                        // // 'SearchListFormFieldView'.
                        // if (this.getClass().equals(SearchListFormFieldView.class)) {
                        // if (listener != null) {
                        // listener.valueChanged(this);
                        // }
                        // }
                        // break;
                        // }
                        // }
                        // }
                        // Commented out end
                    } else {
                        // none is selected so update view with ""
                        listItemSelected("", "", "");
                        updateView();
                    }
                }
                // sendTaxFormField();
            }
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: unknown request code of '" + requestCode + "'.");
            break;
        }
        }
        // Ensure we clear out the current form field view.
        if (listener != null) {
            listener.clearCurrentFormFieldView();
        }
    }

    /**
     * Will determine whether any parent field already has a value filled in.
     * 
     * @return whether any parent field already has a value filled in.
     */
    protected boolean parentHasValue() {
        return true;
    }

    /**
     * Gets the field label of the parent field.
     * 
     * @return the parent field label.
     */
    protected String getParentFieldLabel() {
        return null;
    }

    /**
     * Adds the parent list item key.
     * 
     * @param intent
     *            the intent to add the parent list item key.
     */
    /*
     * MOB-14509
     * 
     * This is a mess that mirrors how iOS hacked it. Basically, us getting the CtrySubCode relies on sending a parentLiKey to the
     * server that is the liKey of CtryCode. Unfortunately with how the back end sits right now, that's exactly what we had to do.
     * In the future, iOS and Android should fix this when we have back end support.
     */
    protected void addParentLiKey(Intent intent) {
        String formFieldId = frmFld.getId();
        if (formFieldId != null && formFieldId.equalsIgnoreCase("CtrySubCode")) {
            FormFieldView ffv = listener.findFormFieldViewById("CtryCode");

            if (ffv != null && ffv instanceof SearchListFormFieldView) {
                String parentLiKey = ((SearchListFormFieldView) ffv).getLiKey();
                if (parentLiKey != null) {
                    intent.putExtra(Const.EXTRA_LIST_SEARCH_PARENT_LI_KEY, parentLiKey);
                }
            } else if (ffv != null) {
                // If it's not a SearchListFormFieldView (IE if the CtryCode view is hidden), then grab the formField value that
                // we get back from the server and use that.
                IFormField parentFrmFld = ffv.getFormField();
                if (parentFrmFld != null) {
                    String parentLiKey = parentFrmFld.getLiKey();
                    if (parentLiKey != null) {
                        intent.putExtra(Const.EXTRA_LIST_SEARCH_PARENT_LI_KEY, parentLiKey);
                    }
                }
            }
        }
    }

    /**
     * Adds the MRU boolean.
     * 
     * @param intent
     *            the intent to add the boolean extra data.
     */
    protected void addMRU(Intent intent) {
        intent.putExtra(Const.EXTRA_LIST_SEARCH_IS_MRU, Boolean.TRUE);
    }

    /**
     * Indicates that a list item has been selecte
     */
    public void listItemSelected(String liCode, String liKey, String value) {
        this.liCode = liCode;
        this.liKey = liKey;
        this.value = value;
    }

    /**
     * Will update the view, if one is present with the current value of <code>value</code>.
     */
    public void updateView() {
        if (view != null) {
            String txtVal = "";
            if (value != null) {
                if (listener.shouldShowListCodes() && liCode != null && liCode.length() > 0
                        && (!frmFld.getId().equalsIgnoreCase(IListFieldItem.DEFAULT_KEY_LOCATION))) {
                    StringBuilder strBldr = new StringBuilder();
                    strBldr.append('(');
                    strBldr.append(liCode);
                    strBldr.append(") ");
                    strBldr.append(value);
                    txtVal = strBldr.toString();
                } else {
                    txtVal = value;
                }
            }
            setTextViewText(view, R.id.field_value, txtVal);
        }
    }

    /**
     * Will return an <code>Intent</code> object that can be used to launch a static list with search option based on the
     * <code>ExpenseReportFormField</code> backing this view.
     * 
     * @return returns an <code>Intent</code> object that can be used to launch a static list with search option based on the
     *         <code>IFormField</code> backing this view.
     */
    // TODO - Use a generic class instead of TravelCustomFieldSearch, or pass in the activity class
    // protected Intent getStaticAndDynamicListSearchLaunchIntent() {
    // Intent intent = new Intent(listener.getActivity(), this.staticAndDynamicListSearchClass);
    // if (frmFld.getId() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_FIELD_ID, frmFld.getId());
    // }
    // if (frmFld.getLabel() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_TITLE, frmFld.getLabel());
    // }
    // SpinnerItem[] ssl = frmFld.getStaticList();
    // if (ssl != null) {
    // ArrayList<SpinnerItem> sItemList = new ArrayList<SpinnerItem>(ssl.length);
    // for (SpinnerItem sItem : ssl) {
    // sItemList.add(sItem);
    // }
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_STATIC_LIST, sItemList);
    // }
    //
    // intent.putExtra(Const.EXTRA_SEARCH_SELECTED_ITEM, selectedListItem);
    //
    // // Add any parent list item key if this is a connected list.
    // addParentLiKey(intent);
    //
    // return intent;
    // }
}
