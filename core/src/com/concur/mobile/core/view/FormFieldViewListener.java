/**
 * 
 */
package com.concur.mobile.core.view;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.view.FormFieldView.IFormFieldViewListener;

/**
 * An implementation of <code>IFormFieldViewListener</code> for listening to form field view requests.
 * 
 * @author AndrewK
 */
public class FormFieldViewListener implements IFormFieldViewListener {

    /**
     * Contains the associated activity.
     */
    private BaseActivity activity;

    /**
     * Contains the associated expense report.
     */
    private ExpenseReport expenseReport;

    /**
     * Contains the associated expense report entry.
     */
    private ExpenseReportEntry expenseReportEntry;

    /**
     * Contains the last form field view providing a notification.
     */
    private FormFieldView currentFrmFldView;

    /**
     * Contains the list of form field view objects.
     */
    private List<FormFieldView> frmFldViews;

    /**
     * Contains the list of tax form field view objects.
     */
    private List<FormFieldView> taxFrmFldViews;

    /**
     * Constructs an instance of <code>FormFieldViewListener</code> with an associated activity.
     * 
     * @param activity
     *            the associated activity.
     */
    public FormFieldViewListener(BaseActivity activity) {
        this(activity, null, null);
    }

    /**
     * Constructs an instance of <code>FormFieldViewListener</code> with an associated activity and expense report.
     * 
     * @param activity
     *            the associated activity.
     * @param expenseReport
     *            the associated expense report.
     */
    public FormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport) {
        this(activity, expenseReport, null);
    }

    /**
     * Constructs an instance of <code>FormFieldViewListener</code> with an associated activity, expense report and expense report
     * entry.
     * 
     * @param activity
     *            the associated activity.
     * @param expenseReport
     *            the associated expense report.
     * @param expenseReportEntry
     *            the associated expense report entry.
     */
    public FormFieldViewListener(BaseActivity activity, ExpenseReport expenseReport,
            ExpenseReportEntry expenseReportEntry) {
        this.activity = activity;
        this.expenseReport = expenseReport;
        this.expenseReportEntry = expenseReportEntry;
    }

    /**
     * Sets the activity associated with this form field view listener.
     * 
     * @param activity
     *            the form field view listener activity.
     */
    public void setActivity(BaseActivity activity) {
        this.activity = activity;
    }

    /**
     * Sets the associated instance of <code>ExpenseReport</code>.
     * 
     * @param expenseReport
     *            the associated expense report.
     */
    public void setExpenseReport(ExpenseReport expenseReport) {
        this.expenseReport = expenseReport;
    }

    /**
     * Sets the associated instance of <code>ExpenseReportEntry</code>.
     * 
     * @param expenseReportEntry
     *            the associated expense report entry.
     */
    @Override
    public void setExpenseReportEntry(ExpenseReportEntry expenseReportEntry) {
        this.expenseReportEntry = expenseReportEntry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#dismissDialog(com.concur.mobile.util.FormFieldView,
     * android.app.Dialog)
     */
    @Override
    public void dismissDialog(FormFieldView frmFldView, Dialog dialog) {
        currentFrmFldView = frmFldView;
        dialog.dismiss();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#showDialog(com.concur.mobile.util.FormFieldView, int)
     */
    @Override
    public void showDialog(FormFieldView frmFldView, int id) {
        currentFrmFldView = frmFldView;
        activity.showDialog(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#startActivity(com.concur.mobile.util.FormFieldView,
     * android.content.Intent)
     */
    @Override
    public void startActivity(FormFieldView frmFldView, Intent intent) {
        currentFrmFldView = frmFldView;
        activity.startActivity(intent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.util.FormFieldView.IFormFieldViewListener#startActivityForResult(com.concur.mobile.util.FormFieldView,
     * android.content.Intent, int)
     */
    @Override
    public void startActivityForResult(FormFieldView frmFldView, Intent intent, int requestCode) {
        currentFrmFldView = frmFldView;
        activity.startActivityForResult(intent, requestCode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#clearCurrentFormFieldView()
     */
    @Override
    public void clearCurrentFormFieldView() {
        currentFrmFldView = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#regenerateFormFieldViews()
     */
    @Override
    public void regenerateFormFieldViews() {
        clearCurrentFormFieldView();
        setFormFieldViews(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView.IFormFieldViewListener#getCurrentFormFieldView()
     */
    @Override
    public FormFieldView getCurrentFormFieldView() {
        return currentFrmFldView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView.IFormFieldViewListener#setCurrentFormFieldView(com.concur.mobile.core.util.
     * FormFieldView)
     */
    @Override
    public void setCurrentFormFieldView(FormFieldView curFrmFldView) {
        currentFrmFldView = curFrmFldView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView.IFormFieldViewListener#isCurrentFormFieldViewSet()
     */
    @Override
    public boolean isCurrentFormFieldViewSet() {
        return (currentFrmFldView != null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#getActivity()
     */
    @Override
    public Activity getActivity() {
        return activity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#getExpenseReport()
     */
    @Override
    public ExpenseReport getExpenseReport() {
        return expenseReport;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#getExpenseReportEntry()
     */
    @Override
    public ExpenseReportEntry getExpenseReportEntry() {
        return expenseReportEntry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#getFormFieldViews()
     */
    @Override
    public List<FormFieldView> getFormFieldViews() {
        return frmFldViews;
    }

    /**
     * Sets the list of <code>FormFieldView</code> objects associated with this listener.
     * 
     * @param frmFldViews
     *            the list of form <code>FormFieldView</code> objects.
     */
    public void setFormFieldViews(List<FormFieldView> frmFldViews) {
        this.frmFldViews = frmFldViews;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#getFormFieldViews()
     */
    public List<FormFieldView> getTaxFormFieldViews() {
        return taxFrmFldViews;
    }

    /**
     * Sets the list of <code>FormFieldView</code> objects associated with this listener.
     * 
     * @param frmFldViews
     *            the list of form <code>FormFieldView</code> objects.
     */
    public void setTaxFormFieldViews(List<FormFieldView> frmFldViews) {
        this.taxFrmFldViews = frmFldViews;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView.IFormFieldViewListener#valueChanged(com.concur.mobile.util.FormFieldView)
     */
    @Override
    public void valueChanged(FormFieldView frmFldView) {
        // No-op.
    }

    /**
     * Will perform an required initialization over the form fields.
     */
    public void initFields() {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.FormFieldView.IFormFieldViewListener#findFormFieldViewById(java.lang.String)
     */
    @Override
    public FormFieldView findFormFieldViewById(String id) {

        if (id != null && id.trim().length() > 0 && frmFldViews != null) {
            FormFieldView ffv;
            final int size = frmFldViews.size();
            for (int i = 0; i < size; i++) {
                ffv = frmFldViews.get(i);
                if (id.equalsIgnoreCase(ffv.frmFld.getId())) {
                    return ffv;
                }
            }
        }

        return null;
    }

    @Override
    public FragmentManager getFragmentManager() {
        return activity.getSupportFragmentManager();
    }
}
