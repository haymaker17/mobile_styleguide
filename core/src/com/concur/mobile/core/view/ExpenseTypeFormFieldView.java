/**
 * 
 */
package com.concur.mobile.core.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.activity.ExpenseTypeSpinnerAdapter;
import com.concur.mobile.core.expense.charge.data.ExpenseTypeCategory;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ExpenseType.ExpenseTypeSAXHandler;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.service.ReportEntryFormRequest;
import com.concur.mobile.core.expense.report.service.ReportItemizationEntryFormRequest;
import com.concur.mobile.core.expense.service.GetExpenseTypesRequest;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.core.R;

/**
 * An extension of <code>FormFieldView</code> to construct and manage a view for editing expense type information.
 * 
 * @author AndrewK
 */
public class ExpenseTypeFormFieldView extends FormFieldView {

    private static String CLS_TAG = ExpenseTypeFormFieldView.class.getSimpleName();

    // An expense type selection dialog.
    private static final int EXPENSE_TYPE_DIALOG = DIALOG_ID_BASE + 0;

    // An expense type retrieval dialog.
    private static final int RETRIEVE_EXPENSE_DIALOG = DIALOG_ID_BASE + 1;

    // An expense form retrieval dialog.
    private static final int RETRIEVE_EXPENSE_FORM_DIALOG = DIALOG_ID_BASE + 2;

    private static final String IS_SIMPLE_SELECTOR_BUNDLE_KEY = "is.simple.selector";

    private static final String SELECTED_EXPENSE_TYPE_KEY_BUNDLE_KEY = "selected.expense.type.key";

    private static final String SELECTED_EXPENSE_TYPE_NAME_BUNDLE_KEY = "selected.expense.type.name";

    private static final String EXPENSE_TYPE_RECEIVER_KEY = "expense.type.receiver";

    private static final String EXPENSE_FORM_RECEIVER_KEY = "expense.form.receiver";

    private static final String ITEMIZATION_EXPENSE_FORM_RECEIVER_KEY = "itemization.expense.form.receiver";

    private static final String EXPENSE_TYPE_LIST_KEY = "expense.type.list";

    // Contains a list of expense types for this field.
    private List<ExpenseType> expTypes;

    // A broadcast receiver to handle the result of an expense types request.
    private ExpenseTypesReceiver expenseTypesReceiver;

    // The filter used to register the above receiver.
    private IntentFilter expenseTypesFilter;

    // A reference to an outstanding request.
    private GetExpenseTypesRequest expenseTypesRequest;

    // A broadcast receiver to handle the result of requesting a form for
    // an expense entry.
    private ExpenseFormReceiver expenseFormReceiver;

    // The filter used to register the above receiver.
    private IntentFilter expenseFormFilter;

    // A reference to an outstanding request.
    private ReportEntryFormRequest expenseFormRequest;

    // A broadcast receiver to handle the result of requesting a form for
    // an expense itemization entry.
    private ItemizationExpenseFormReceiver itemizationExpenseFormReceiver;

    // The filter used to register the above receiver.
    private IntentFilter itemizationExpenseFormFilter;

    // A reference to an outstanding request.
    private ReportItemizationEntryFormRequest itemizationExpenseFormRequest;

    // Contains a reference to the expense type adapter.
    private ExpenseTypeSpinnerAdapter expTypeAdapter;

    // Contains the selected expense type key.
    private String selectedExpenseTypeKey;

    // Contains the selected expense type name.
    private String selectedExpenseTypeName;

    // Contains the previous selected expense type key & name. This is here to
    // handle if the end-user
    // selects an expense type, then cancels the retrieval of the form fields
    // for any reason.
    // The previous selection needs to be put back in place.

    // Contains the previous selected expense type key.
    private String previousSelectedExpenseTypeKey;

    // Contains the previous selected expense type name.
    private String previousSelectedExpenseTypeName;

    // Indicate whether this is a full blown control for change entry expense
    // types
    // or a simple control for just selecting an expense type
    private boolean isSimpleSelector;

    /**
     * Constructs and instance of <code>ExpenseTypeFormFieldView</code> given an expense report form field.
     * 
     * @param frmFld
     *            the expense report form field.
     */
    public ExpenseTypeFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
        expenseTypesFilter = new IntentFilter(Const.ACTION_EXPENSE_EXPENSE_TYPES_DOWNLOADED);
        expenseFormFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_FORM_UPDATED);
        itemizationExpenseFormFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ITEMIZATION_ENTRY_FORM_UPDATED);
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
                        if (frmFld.getValue() != null) {
                            txtVal = frmFld.getValue();
                        }
                        setTextViewText(view, R.id.field_value, txtVal);
                        // Set the expense type icon.
                        // setImageViewImage(view, R.id.field_image,
                        // android.R.drawable.ic_menu_more);
                        // Enable focusability and make it clickable.
                        // These are set in the layout now.
                        // view.setFocusable(true);
                        // view.setClickable(true);
                        // Add a click handler.
                        // TODO click in caps.
                        setOnClickListener(view);
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
                break;
            }
            default:
                break;
            }
        }
        return view;
    }

    protected void setOnClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (listener != null) {
                    if (listener.getActivity() != null) {
                        if (listener.getExpenseReport() != null) {
                            ExpenseReport expRep = listener.getExpenseReport();
                            ConcurCore concurMobile = (ConcurCore) listener.getActivity().getApplication();
                            IExpenseEntryCache expEntCache = concurMobile.getExpenseEntryCache();
                            expTypes = expEntCache.getExpenseTypes(expRep.polKey);
                            if (expTypes != null) {
                                // Show the expense types list
                                // selection dialog.
                                listener.showDialog(ExpenseTypeFormFieldView.this, EXPENSE_TYPE_DIALOG);
                            } else {
                                // Register an expense type
                                // receiver.
                                registerExpenseTypeReceiver();
                                // Make the request.
                                ConcurService concurService = concurMobile.getService();
                                expenseTypesRequest = concurService.sendGetExpenseTypesRequest(getUserId(),
                                        expRep.polKey);
                                // If the request couldn't be
                                // made, then unregister the
                                // receiver.
                                if (expenseTypesRequest == null) {
                                    // Unregister the expense
                                    // type receiver.
                                    unregisterExpenseTypeReceiver();
                                    expTypes = null;
                                } else {
                                    // Set the request on the
                                    // expense types receiver.
                                    expenseTypesReceiver.setRequest(expenseTypesRequest);
                                    // Display an expense type
                                    // retrieval dialog.
                                    listener.showDialog(ExpenseTypeFormFieldView.this, RETRIEVE_EXPENSE_DIALOG);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".getView.OnClick: form field view listener expense report is null!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView.OnClick: form field view listener activity is null!");
                    }
                }
            }
        });
    }

    public void setSimpleSelector() {
        isSimpleSelector = true;
    }

    @Override
    public boolean hasValue() {
        return (selectedExpenseTypeKey != null || (frmFld.getValue() != null && frmFld.getValue().length() > 0));
    }

    @Override
    public ValidityCheck isValueValid() {
        return SUCCESS;
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            String origLiKey = (frmFld.getLiKey() != null) ? frmFld.getLiKey() : "";
            if (selectedExpenseTypeKey != null) {
                retVal = !selectedExpenseTypeKey.contentEquals(origLiKey);
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
        if (selectedExpenseTypeKey != null) {
            retVal = selectedExpenseTypeKey;
        }
        return retVal;
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
     * @see com.concur.mobile.util.FormFieldView#updateEditedValue(com.concur.mobile .util.FormFieldView)
     */
    @Override
    public void updateEditedValue(FormFieldView frmFldView) {
        // Check for whether this field is editable.
        if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
            // Check for whether value has changed in source form field view.
            if (frmFldView.hasValueChanged()) {
                // Check for same type of field.
                if (frmFldView instanceof ExpenseTypeFormFieldView) {
                    ExpenseTypeFormFieldView expTypeFrmFldView = (ExpenseTypeFormFieldView) frmFldView;
                    // Just perform a straight copy of the values.
                    expTypes = expTypeFrmFldView.expTypes;
                    selectedExpenseTypeName = expTypeFrmFldView.selectedExpenseTypeName;
                    selectedExpenseTypeKey = expTypeFrmFldView.selectedExpenseTypeKey;
                    // Update the view.
                    setTextViewText(view, R.id.field_value, selectedExpenseTypeName);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onRestoreInstanceState(android.os .Bundle)
     */
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        // Restore 'isSimpleSelector'.
        String key = getPrefixedKey(IS_SIMPLE_SELECTOR_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            isSimpleSelector = bundle.getBoolean(key);
        }
        // Restore 'selectedExpenseTypeKey'.
        key = getPrefixedKey(SELECTED_EXPENSE_TYPE_KEY_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            selectedExpenseTypeKey = bundle.getString(SELECTED_EXPENSE_TYPE_KEY_BUNDLE_KEY);
        }
        // Restore 'selectedExpenseTypeName'.
        key = getPrefixedKey(SELECTED_EXPENSE_TYPE_NAME_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            selectedExpenseTypeName = bundle.getString(SELECTED_EXPENSE_TYPE_NAME_BUNDLE_KEY);
        }
        // Restore 'expTypes'.
        key = getPrefixedKey(EXPENSE_TYPE_LIST_KEY);
        if (bundle.containsKey(key)) {
            String expTypesXml = bundle.getString(key);
            if (expTypesXml != null) {
                expTypes = ExpenseType.parseExpenseTypeXml(expTypesXml);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onRestoreInstanceState: bundle contains null expense type list string!");
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android .os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        // Store 'isSimpleSelector'.
        bundle.putBoolean(getPrefixedKey(IS_SIMPLE_SELECTOR_BUNDLE_KEY), isSimpleSelector);
        // Store 'selectedExpenseTypeKey'.
        bundle.putString(getPrefixedKey(SELECTED_EXPENSE_TYPE_KEY_BUNDLE_KEY), selectedExpenseTypeKey);
        // Store 'selectedExpenseTypeName'.
        bundle.putString(getPrefixedKey(SELECTED_EXPENSE_TYPE_NAME_BUNDLE_KEY), selectedExpenseTypeName);
        if (expTypes != null) {
            // Store 'expTypes'.
            StringBuilder strBldr = new StringBuilder();
            ExpenseTypeSAXHandler.serializeToXML(strBldr, expTypes);
            bundle.putString(getPrefixedKey(EXPENSE_TYPE_LIST_KEY), strBldr.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle )
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (hasValueChanged()) {
            // Store 'isSimpleSelector'.
            bundle.putBoolean(getPrefixedKey(IS_SIMPLE_SELECTOR_BUNDLE_KEY), isSimpleSelector);
            // Store 'selectedExpenseTypeKey'.
            bundle.putString(getPrefixedKey(SELECTED_EXPENSE_TYPE_KEY_BUNDLE_KEY), selectedExpenseTypeKey);
            // Store 'selectedExpenseTypeName'.
            bundle.putString(getPrefixedKey(SELECTED_EXPENSE_TYPE_NAME_BUNDLE_KEY), selectedExpenseTypeName);
        }
        if (expTypes != null) {
            // Store 'expTypes'.
            StringBuilder strBldr = new StringBuilder();
            ExpenseTypeSAXHandler.serializeToXML(strBldr, expTypes);
            bundle.putString(getPrefixedKey(EXPENSE_TYPE_LIST_KEY), strBldr.toString());
        }

    }

    /*
     * (non-Javadoc
     * 
     * @see com.concur.mobile.util.FormFieldView#onApplyNonConfigurationInstance( java.util.HashMap)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onApplyNonConfigurationInstance(RetainerFragment retainer) {

        // Restore the expense types receiver.
        String key = getPrefixedKey(EXPENSE_TYPE_RECEIVER_KEY);
        if (retainer.contains(key)) {
            expenseTypesReceiver = (ExpenseTypesReceiver) retainer.get(key);
            if (expenseTypesReceiver != null) {
                expenseTypesReceiver.setFormFieldView(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onApplyNonConfigurationInstance: retainer contains null expense types receiver!");
            }
        }

        // Restore the expense form receiver.
        key = getPrefixedKey(EXPENSE_FORM_RECEIVER_KEY);
        if (retainer.contains(key)) {
            expenseFormReceiver = (ExpenseFormReceiver) retainer.get(key);
            if (expenseFormReceiver != null) {
                expenseFormReceiver.setFormFieldView(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onApplyNonConfigurationInstance: retainer contains null expense form receiver!");
            }
        }

        // Restore the itemization expense form receiver.
        key = getPrefixedKey(ITEMIZATION_EXPENSE_FORM_RECEIVER_KEY);
        if (retainer.contains(key)) {
            itemizationExpenseFormReceiver = (ItemizationExpenseFormReceiver) retainer.get(key);
            if (itemizationExpenseFormReceiver != null) {
                itemizationExpenseFormReceiver.setFormFieldView(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onApplyNonConfigurationInstance: retainer contains null itemization expense form receiver!");
            }
        }

        // Restore expense types list.
        key = getPrefixedKey(EXPENSE_TYPE_LIST_KEY);
        if (retainer.contains(key)) {
            expTypes = (List<ExpenseType>) retainer.get(key);
        }
    }

    @Override
    public void onRetainNonConfigurationInstance(RetainerFragment retainer) {

        // Retain the expense type receiver if not null.
        if (expenseTypesReceiver != null) {
            // Unattach the receiver from the form field view.
            expenseTypesReceiver.setFormFieldView(null);
            // Add it to the retainer
            retainer.put(getPrefixedKey(EXPENSE_TYPE_RECEIVER_KEY), expenseTypesReceiver);
        }

        // Retain the expense form receiver if not null.
        if (expenseFormReceiver != null) {
            // Unattach the receiver from the form field view.
            expenseFormReceiver.setFormFieldView(null);
            // Add it to the retainer
            retainer.put(getPrefixedKey(EXPENSE_FORM_RECEIVER_KEY), expenseFormReceiver);
        }

        // Retain the itemization expense form receiver if not null.
        if (itemizationExpenseFormReceiver != null) {
            // Unattach the receiver from the form field view.
            itemizationExpenseFormReceiver.setFormFieldView(null);
            // Add it to the retainer
            retainer.put(getPrefixedKey(ITEMIZATION_EXPENSE_FORM_RECEIVER_KEY), itemizationExpenseFormReceiver);
        }

        // Retain expense type list.
        if (expTypes != null) {
            // Add it to the retainer
            retainer.put(getPrefixedKey(EXPENSE_TYPE_LIST_KEY), expTypes);
        }

    }

    /**
     * Will register the expense type receiver.
     */
    private void registerExpenseTypeReceiver() {
        expenseTypesReceiver = new ExpenseTypesReceiver(this);
        listener.getActivity().getApplicationContext().registerReceiver(expenseTypesReceiver, expenseTypesFilter);
    }

    /**
     * Will unregister the expense type receiver and set its reference to <code>null</code>.
     */
    private void unregisterExpenseTypeReceiver() {
        listener.getActivity().getApplicationContext().unregisterReceiver(expenseTypesReceiver);
        expenseTypesReceiver = null;
    }

    /**
     * Will register the expense form receiver.
     */
    private void registerExpenseFormReceiver() {
        expenseFormReceiver = new ExpenseFormReceiver(this);
        listener.getActivity().getApplicationContext().registerReceiver(expenseFormReceiver, expenseFormFilter);
    }

    /**
     * Will unregister the expense form receiver and set its reference to <code>null</code>.
     */

    private void unregisterExpenseFormReceiver() {
        listener.getActivity().getApplicationContext().unregisterReceiver(expenseFormReceiver);
        expenseFormReceiver = null;
    }

    /**
     * Will register the itemization expense form receiver.
     */
    private void registerItemizationExpenseFormReceiver() {
        itemizationExpenseFormReceiver = new ItemizationExpenseFormReceiver(this);
        listener.getActivity().getApplicationContext()
                .registerReceiver(itemizationExpenseFormReceiver, itemizationExpenseFormFilter);
    }

    /**
     * Will unregister the itemization expense form receiver and set its reference to <code>null</code>.
     */
    private void unregisterItemizationExpenseFormReceiver() {
        listener.getActivity().getApplicationContext().unregisterReceiver(itemizationExpenseFormReceiver);
        itemizationExpenseFormReceiver = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        if (selectedExpenseTypeKey != null) {
            frmFld.setLiKey(selectedExpenseTypeKey);
            frmFld.setValue(selectedExpenseTypeName);
        }
    }

    /**
     * Will reset <code>selectedExpenseType[Key|Name]</code> to any previous selection, if a previous selection was made, or to
     * the original value stored in the <code>frmFld</code> object.
     * 
     * This method should be invoked in order to reset the expense type name in the event the end-user cancel's the request to
     * retrieve a set of form fields from the MWS.
     */
    private void resetToPreviousExpenseTypeSelection() {
        selectedExpenseTypeKey = previousSelectedExpenseTypeKey;
        selectedExpenseTypeName = previousSelectedExpenseTypeName;
        previousSelectedExpenseTypeKey = null;
        previousSelectedExpenseTypeName = null;
        if (selectedExpenseTypeKey != null) {
            setTextViewText(view, R.id.field_value, selectedExpenseTypeName);
        } else {
            String expTypeName = (frmFld.getValue() != null) ? frmFld.getValue() : "";
            setTextViewText(view, R.id.field_value, expTypeName);
        }
    }

    /**
     * Will fill in a list of <code>ExpenseType</code> object from which an end-user can make a selection. <br>
     * <b>NOTE:</b>&nbsp;This method will determine whether child expense types should be made available.
     * 
     * @param expTypeList
     *            the list in which expense types will be placed.
     * 
     * @return whether or not parent expense types have been added to <code>expTypeList</code>.
     */
    private boolean getExpenseTypes(List<ExpenseType> expTypeList) {
        boolean retVal = true;
        if (listener != null) {
            if (listener.getExpenseReportEntry() != null) {
                if (listener.getExpenseReportEntry().parentReportEntryKey != null
                        && listener.getExpenseReportEntry().parentReportEntryKey.length() > 0) {
                    // Check for child/regular expense types and add to return
                    // list.
                    for (ExpenseType expType : expTypes) {
                        if (expType instanceof ExpenseTypeCategory) {
                            expTypeList.add(expType);
                        } else if (expType.access == ExpenseType.Access.CHILD
                                || expType.access == ExpenseType.Access.REGULAR) {
                            expTypeList.add(expType);
                        }
                    }
                    retVal = false;
                } else {
                    expTypeList.addAll(expTypes);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseTypes: report entry is null!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getExpenseTypes: listener is null!");
        }
        return retVal;
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
        case RETRIEVE_EXPENSE_FORM_DIALOG: {
            ProgressDialog progDlg = new ProgressDialog(listener.getActivity());
            progDlg.setMessage(listener.getActivity().getText(R.string.retrieve_report_entry_form));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    if (expenseTypesRequest != null) {
                        expenseTypesRequest.cancel();
                        resetToPreviousExpenseTypeSelection();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: null request!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case EXPENSE_TYPE_DIALOG: {
            AlertDialog.Builder builder = new AlertDialog.Builder(listener.getActivity());
            builder.setTitle(R.string.expense_type_prompt);
            expTypeAdapter = new ExpenseTypeSpinnerAdapter(listener.getActivity(), null);
            List<ExpenseType> expTypeList = new ArrayList<ExpenseType>();
            boolean showParentTypes = getExpenseTypes(expTypeList);

            expTypeAdapter.setExpenseTypes(expTypeList, null, showParentTypes, !showParentTypes,
                    Const.EXPENSE_CODE_PERSONAL_MILEAGE, Const.EXPENSE_CODE_COMPANY_MILEAGE);
            expTypeAdapter.setUseDropDownOnly(true);

            LayoutInflater inflater = LayoutInflater.from(listener.getActivity());
            View customView = inflater.inflate(R.layout.expense_mru, null);

            ListView customListView = (ListView) customView.findViewById(R.id.list_expense_mru);
            EditText customEditText = (EditText) customView.findViewById(R.id.list_search_mru);
            customListView.setAdapter(expTypeAdapter);

            customListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            builder.setView(customView);

            customEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    expTypeAdapter.clearSearchFilter();
                    expTypeAdapter.getFilter().filter(s);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

            });

            customListView.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                    listener.getActivity().dismissDialog(EXPENSE_TYPE_DIALOG);
                    // Hang onto the previous selection in the event the
                    // end-user
                    // cancels the form field retrieval
                    // request.
                    previousSelectedExpenseTypeKey = selectedExpenseTypeKey;
                    previousSelectedExpenseTypeName = selectedExpenseTypeName;
                    ExpenseType selectedExpenseType = (ExpenseType) expTypeAdapter.getItem(which);
                    if (selectedExpenseType != null) {

                        selectedExpenseTypeKey = selectedExpenseType.getKey();
                        selectedExpenseTypeName = selectedExpenseType.getName();
                        // Update the view.
                        setTextViewText(ExpenseTypeFormFieldView.this.view, R.id.field_value, selectedExpenseTypeName);

                        if (!isSimpleSelector) {
                            if (ConcurCore.isConnected()) {
                                if (listener.getExpenseReportEntry().parentReportEntryKey == null) {
                                    // Register an expense form receiver.
                                    registerExpenseFormReceiver();
                                    // Make the request.
                                    ConcurCore concurMobile = (ConcurCore) listener.getActivity().getApplication();
                                    ConcurService concurService = concurMobile.getService();
                                    expenseFormRequest = concurService.sendReportEntryFormRequest(
                                            selectedExpenseTypeKey, listener.getExpenseReport().reportKey,
                                            listener.getExpenseReportEntry().reportEntryKey);
                                    // If the request couldn't be made, then
                                    // unregister the
                                    // receiver.
                                    if (expenseFormRequest == null) {
                                        // Unregister the expense form receiver.
                                        unregisterExpenseFormReceiver();
                                        // If the request couldn't be made, then
                                        // reset to the
                                        // previous expense type.
                                        resetToPreviousExpenseTypeSelection();
                                    } else {
                                        // Set the request on the expense form
                                        // receiver.
                                        expenseFormReceiver.setRequest(expenseFormRequest);
                                        // Display an expense form retrieval
                                        // dialog.
                                        listener.showDialog(ExpenseTypeFormFieldView.this, RETRIEVE_EXPENSE_FORM_DIALOG);
                                    }
                                } else {
                                    // Register the itemization expense
                                    // receiver.
                                    registerItemizationExpenseFormReceiver();
                                    // Make the request.
                                    ConcurCore concurMobile = (ConcurCore) listener.getActivity().getApplication();
                                    ConcurService concurService = concurMobile.getService();
                                    itemizationExpenseFormRequest = concurService
                                            .sendReportItemizationEntryFormRequest(false, selectedExpenseTypeKey,
                                                    listener.getExpenseReport().reportKey,
                                                    listener.getExpenseReportEntry().parentReportEntryKey,
                                                    listener.getExpenseReportEntry().reportEntryKey);
                                    // If the request couldn't be made, then
                                    // unregister the
                                    // receiver.
                                    if (itemizationExpenseFormRequest == null) {
                                        // Unregister the itemization expense
                                        // receiver.
                                        unregisterItemizationExpenseFormReceiver();
                                        // If the request couldn't be made, then
                                        // reset to the
                                        // previous expense type.
                                        resetToPreviousExpenseTypeSelection();
                                    } else {
                                        // Set the request on the receiver.
                                        itemizationExpenseFormReceiver.setRequest(itemizationExpenseFormRequest);
                                        // Display an expense form retrieval
                                        // dialog.
                                        listener.showDialog(ExpenseTypeFormFieldView.this, RETRIEVE_EXPENSE_FORM_DIALOG);
                                    }
                                }
                            } else {
                                // Revert the selection.
                                resetToPreviousExpenseTypeSelection();
                                // Ensure the current form field view is
                                // cleared.
                                listener.clearCurrentFormFieldView();
                                // Display the generic dialog about the system
                                // being offline.
                                listener.getActivity().showDialog(Const.DIALOG_NO_CONNECTIVITY);
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: selectedExpenseType is null!");
                    }
                }
            });

            AlertDialog alertDlg = builder.create();
            dialog = alertDlg;
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                public void onDismiss(DialogInterface dialog) {
                    listener.getActivity().removeDialog(EXPENSE_TYPE_DIALOG);
                    listener.clearCurrentFormFieldView();
                }
            });
            break;
        }
        case RETRIEVE_EXPENSE_DIALOG: {
            ProgressDialog progDlg = new ProgressDialog(listener.getActivity());
            progDlg.setMessage(listener.getActivity().getText(R.string.retrieve_expense_types));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    if (expenseTypesRequest != null) {
                        expenseTypesRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: null request!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: unhandled dialog id of '" + id + "'.");
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
        case EXPENSE_TYPE_DIALOG: {
            // No-op.
            break;
        }
        case RETRIEVE_EXPENSE_DIALOG: {
            // No-op.
            break;
        }
        case RETRIEVE_EXPENSE_FORM_DIALOG: {
            // No-op.
            break;
        }
        default: {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unhandled dialog id of '" + id + "'.");
            break;
        }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve a list of expense types for
     * this form field view.
     * 
     * @author AndrewK
     */
    static class ExpenseTypesReceiver extends BroadcastReceiver {

        private String CLS_TAG = ExpenseTypeFormFieldView.CLS_TAG + '.' + ExpenseTypesReceiver.class.getSimpleName();

        // A reference to the form field view.
        private ExpenseTypeFormFieldView frmFldView;

        // A reference to the get expense types request.
        private GetExpenseTypesRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ExpenseTypesReceiver</code>.
         * 
         * @param frmFldView
         *            the form field view associated with this receiver.
         */
        ExpenseTypesReceiver(ExpenseTypeFormFieldView frmFldView) {
            this.frmFldView = frmFldView;
        }

        /**
         * Sets the form field view associated with this broadcast receiver.
         * 
         * @param frmFldView
         *            the form field view associated with this broadcast receiver.
         */
        void setFormFieldView(ExpenseTypeFormFieldView frmFldView) {
            this.frmFldView = frmFldView;
            if (this.frmFldView != null) {
                this.frmFldView.expenseTypesRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(frmFldView.listener.getActivity().getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the get expense types request object associated with this broadcast receiver.
         * 
         * @param request
         *            the get expense types request object associated with this broadcast receiver.
         */
        void setRequest(GetExpenseTypesRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current form field view?
            if (frmFldView != null) {
                try {
                    // Remove the dialog.
                    frmFldView.listener.getActivity().removeDialog(RETRIEVE_EXPENSE_DIALOG);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }
                // Clear the current form view.
                frmFldView.listener.clearCurrentFormFieldView();
                // Unregister this receiver.
                frmFldView.unregisterExpenseTypeReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // Attempt to pull the set of expense types
                                    // now from in-memory cache and present the
                                    // dialog
                                    // for selection.
                                    ConcurCore concurMobile = (ConcurCore) frmFldView.listener.getActivity()
                                            .getApplication();
                                    IExpenseEntryCache expEntCache = concurMobile.getExpenseEntryCache();
                                    frmFldView.expTypes = expEntCache.getExpenseTypes(frmFldView.listener
                                            .getExpenseReport().polKey);
                                    if (frmFldView.expTypes != null) {
                                        // if(frmFldView.getListFromDB()!=null){
                                        frmFldView.listener.showDialog(frmFldView, EXPENSE_TYPE_DIALOG);
                                        // }
                                        // Display the expense type selection
                                        // dialog.

                                        // frmFldView.listener.showDialog(frmFldView,
                                        // EXPENSE_TYPE_DIALOG);
                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: no expense types fetched!");
                                        frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
            } else {
                // The new form field view has not yet been set on the receiver,
                // defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve a form for the currently
     * selected expense type.
     * 
     * @author AndrewK
     */
    static class ExpenseFormReceiver extends BroadcastReceiver {

        private String CLS_TAG = ExpenseTypeFormFieldView.CLS_TAG + '.' + ExpenseFormReceiver.class.getSimpleName();

        // A reference to the form field view.
        private ExpenseTypeFormFieldView frmFldView;

        // A reference to the report entry form request.
        private ReportEntryFormRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ExpenseFormReceiver</code>.
         * 
         * @param frmFldView
         *            the form field view associated with this receiver.
         */
        ExpenseFormReceiver(ExpenseTypeFormFieldView frmFldView) {
            this.frmFldView = frmFldView;
        }

        /**
         * Sets the form field view associated with this broadcast receiver.
         * 
         * @param frmFldView
         *            the form field view associated with this broadcast receiver.
         */
        void setFormFieldView(ExpenseTypeFormFieldView frmFldView) {
            this.frmFldView = frmFldView;
            if (this.frmFldView != null) {
                this.frmFldView.expenseFormRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(frmFldView.listener.getActivity().getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report entry form request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report entry form request object associated with this broadcast receiver.
         */
        void setRequest(ReportEntryFormRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current form field view?
            if (frmFldView != null) {
                try {
                    // Remove the dialog.
                    frmFldView.listener.getActivity().removeDialog(RETRIEVE_EXPENSE_FORM_DIALOG);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }
                // Clear the current form view.
                frmFldView.listener.clearCurrentFormFieldView();
                // Unregister this receiver.
                frmFldView.unregisterExpenseFormReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    ConcurCore concurMobile = (ConcurCore) frmFldView.listener.getActivity()
                                            .getApplication();
                                    ExpenseReportEntryDetail expRepEntDet = concurMobile.getCurrentEntryDetailForm();
                                    if (expRepEntDet != null) {
                                        // Update the listener with the newly
                                        // loaded expense report entry.
                                        frmFldView.listener.setExpenseReportEntry(expRepEntDet);
                                        // Inform the listener that it should
                                        // regenerate any form field view
                                        // objects.
                                        frmFldView.listener.regenerateFormFieldViews();
                                        // Inform the listener that the expense
                                        // type has been changed.
                                        frmFldView.listener.valueChanged(frmFldView);
                                    } else {
                                        frmFldView.resetToPreviousExpenseTypeSelection();
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".onReceive: current expense report entry detail form is null!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
            } else {
                // The new form field view has not yet been set on the receiver,
                // defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle the result of an attempt to retrieve a form for the currently
     * selected expense type.
     * 
     * @author AndrewK
     */
    static class ItemizationExpenseFormReceiver extends BroadcastReceiver {

        private String CLS_TAG = ExpenseTypeFormFieldView.CLS_TAG + '.'
                + ItemizationExpenseFormReceiver.class.getSimpleName();

        // A reference to the form field view.
        private ExpenseTypeFormFieldView frmFldView;

        // A reference to the report itemization entry form request.
        private ReportItemizationEntryFormRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive'
        // method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ItemizationExpenseFormReceiver</code> .
         * 
         * @param frmFldView
         *            the form field view associated with this receiver.
         */
        ItemizationExpenseFormReceiver(ExpenseTypeFormFieldView frmFldView) {
            this.frmFldView = frmFldView;
        }

        /**
         * Sets the form field view associated with this broadcast receiver.
         * 
         * @param frmFldView
         *            the form field view associated with this broadcast receiver.
         */
        void setFormFieldView(ExpenseTypeFormFieldView frmFldView) {
            this.frmFldView = frmFldView;
            if (this.frmFldView != null) {
                this.frmFldView.itemizationExpenseFormRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(frmFldView.listener.getActivity().getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report itemization entry form request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report itemization entry form request object associated with this broadcast receiver.
         */
        void setRequest(ReportItemizationEntryFormRequest request) {
            this.request = request;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current form field view?
            if (frmFldView != null) {
                // Remove the dialog.
                frmFldView.listener.getActivity().removeDialog(RETRIEVE_EXPENSE_FORM_DIALOG);
                // Clear the current form view.
                frmFldView.listener.clearCurrentFormFieldView();
                // Unregister this receiver.
                frmFldView.unregisterItemizationExpenseFormReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    ConcurCore concurMobile = (ConcurCore) frmFldView.listener.getActivity()
                                            .getApplication();
                                    ExpenseReportEntryDetail expRepEntDet = concurMobile.getCurrentEntryDetailForm();
                                    if (expRepEntDet != null) {
                                        // Update the listener with the newly
                                        // loaded expense report entry.
                                        frmFldView.listener.setExpenseReportEntry(expRepEntDet);
                                        // Inform the listener that it should
                                        // regenerate any form field view
                                        // objects.
                                        frmFldView.listener.regenerateFormFieldViews();
                                        // Inform the listener that the expense
                                        // type has been changed.
                                        frmFldView.listener.valueChanged(frmFldView);
                                    } else {
                                        frmFldView.resetToPreviousExpenseTypeSelection();
                                        Log.e(Const.LOG_TAG, CLS_TAG
                                                + ".onReceive: current expense report entry detail form is null!");
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            frmFldView.listener.getActivity().showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
            } else {
                // The new form field view has not yet been set on the receiver,
                // defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

}
