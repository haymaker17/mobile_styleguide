package com.concur.mobile.gov.expense.doc.data;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.activity.GovListSearch;

public class GovSearchListFormFieldView extends SearchListFormFieldView {

    private static final String CLS_TAG = GovSearchListFormFieldView.class.getSimpleName();

    public GovSearchListFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
    }

    /**
     * Will return an <code>Intent</code> object that can be used to launch a list search based on the
     * <code>ExpenseReportFormField</code> backing this view.
     * 
     * @return returns an <code>Intent</code> object that can be used to launch a list search based on the
     *         <code>ExpenseReportFormField</code> backing this view.
     */
    @Override
    protected Intent getListSearchLaunchIntent() {
        Intent intent = new Intent(listener.getActivity(), GovListSearch.class);
        if (frmFld.getId() != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, frmFld.getId());
        }
        if (frmFld.getFtCode() != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE, frmFld.getFtCode());
        }
        if (frmFld.getListKey() != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_LIST_KEY, frmFld.getListKey());
        }
        if (frmFld.getLabel() != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, frmFld.getLabel());
        }
        ArrayList<ListItem> ssl = frmFld.getSearchableStaticList();
        if (ssl != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_STATIC_LIST, ssl);
        }

        // Add any parent list item key if this is a connected list.
        addParentLiKey(intent);
        // Add any MRU intent extra.
        addMRU(intent);
        if (listener.getExpenseReport() != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY, listener.getExpenseReport().reportKey);
        }

        // pass description
        GovAppMobile app = (GovAppMobile) listener.getActivity().getApplication();
        GovExpenseForm expenseForm = app.getCurrentExpenseForm();
        intent.putExtra(com.concur.mobile.gov.util.Const.EXTRA_GOV_QE_DESC, expenseForm.description);
        // Add any exclude keys
        if (excludeKeys != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_EXCLUDE_KEYS, excludeKeys);
        }

        // Add flag to show codes if needed
        // The design puts this here (instead of totally embedding in ListSearch) so that we keep the possibility of one
        // day switching this per field.
        intent.putExtra(Const.EXTRA_EXPENSE_LIST_SHOW_CODES, Preferences.shouldShowListCodes());

        return intent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SEARCH_LIST_REQUEST_CODE: {
            if (resultCode == Activity.RESULT_OK) {
                String selectedListItemKey = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY);
                String selectedListItemCode = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE);
                if (selectedListItemKey != null || selectedListItemCode != null) {
                    // for Gov licode,text and key are same
                    listItemSelected(selectedListItemCode, selectedListItemKey, selectedListItemKey);
                    updateView();
                    // Only inform the listener if this form field view object's class object is
                    // 'SearchListFormFieldView'.
                    if (this.getClass().equals(SearchListFormFieldView.class)) {
                        if (listener != null) {
                            listener.valueChanged(this);
                        }
                    }
                    break;
                } else {
                    // none is selected so update view with ""
                    listItemSelected("", "", "");
                    updateView();
                }
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

}
