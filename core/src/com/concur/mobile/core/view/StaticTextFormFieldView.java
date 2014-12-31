/**
 * 
 */
package com.concur.mobile.core.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;

/**
 * An extension of <code>FormFieldValue</code> to construct and manage a static text form field value.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.StaticTextFormFieldView} instead.
 * @author AndrewK
 */
public class StaticTextFormFieldView extends FormFieldView {

    /**
     * Constructs an instance of <code>StaticTextFormFieldValue</code> given an expense report form field.
     * 
     * @param expRepFrmFld
     *            the expense report form field value.
     */
    public StaticTextFormFieldView(ExpenseReportFormField expRepFrmFld, IFormFieldViewListener listener) {
        super(expRepFrmFld, listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#getView(android.content.Context)
     */
    @Override
    public View getView(Context context) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = buildStaticTextView(inflater);
        }
        return view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#hasValue()
     */
    @Override
    public boolean hasValue() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#isValueValid()
     */
    @Override
    public ValidityCheck isValueValid() {
        return SUCCESS;
    }

    @Override
    public boolean hasValueChanged() {
        return false;
    }

    @Override
    public String getCurrentValue() {
        // TODO: Get the current value.
        return null;
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
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView#onSaveInstanceStateIgnoreChange(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        // No-op.
    }

}
