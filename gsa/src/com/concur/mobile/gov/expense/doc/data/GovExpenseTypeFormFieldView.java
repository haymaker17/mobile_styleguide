package com.concur.mobile.gov.expense.doc.data;

import android.util.Log;
import android.view.View;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ExpenseTypeFormFieldView;
import com.concur.mobile.gov.expense.doc.activity.Expense;

public class GovExpenseTypeFormFieldView extends ExpenseTypeFormFieldView {

    private static final String CLS_TAG = GovExpenseTypeFormFieldView.class.getSimpleName();

    public GovExpenseTypeFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
    }

    @Override
    protected void setOnClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    if (listener.getActivity() != null) {
                        listener.showDialog(GovExpenseTypeFormFieldView.this, Expense.DIALOG_EXPENSE_TYPE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView.OnClick: form field view listener activity is null!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView.OnClick: form field view listener activity is null!");
                }

            }
        });
    }

}
