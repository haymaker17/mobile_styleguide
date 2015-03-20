package com.concur.mobile.platform.ui.travel.view;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;

import com.concur.mobile.platform.ui.common.view.FormFieldView;
import com.concur.mobile.platform.ui.common.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.platform.ui.travel.activity.BaseActivity;

/**
 * An implementation of <code>IFormFieldViewListener</code> for listening to form field view requests. Similar to core
 * FormFieldViewListener
 * 
 * @author RatanK
 * 
 */
public class FormFieldViewListener implements IFormFieldViewListener {

    private BaseActivity activity;

    /**
     * Contains the last form field view providing a notification.
     */
    private FormFieldView currentFrmFldView;

    /**
     * Contains the list of form field view objects.
     */
    private List<FormFieldView> frmFldViews;

    /**
     * Constructs an instance of <code>FormFieldViewListener</code> with an associated activity.
     * 
     * @param activity
     *            the associated activity.
     */
    public FormFieldViewListener(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void showDialog(FormFieldView frmFldView, int id) {
        currentFrmFldView = frmFldView;
        activity.showDialog(id);
    }

    @Override
    public void dismissDialog(FormFieldView frmFldView, Dialog dialog) {
        currentFrmFldView = frmFldView;
        dialog.dismiss();
    }

    @Override
    public void startActivity(FormFieldView frmFldView, Intent intent) {
        currentFrmFldView = frmFldView;
        activity.startActivity(intent);
    }

    @Override
    public void startActivityForResult(FormFieldView frmFldView, Intent intent, int requestCode) {
        currentFrmFldView = frmFldView;
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public FormFieldView getCurrentFormFieldView() {
        return currentFrmFldView;
    }

    @Override
    public void setCurrentFormFieldView(FormFieldView curFrmFldView) {
        currentFrmFldView = curFrmFldView;
    }

    @Override
    public boolean isCurrentFormFieldViewSet() {
        return (currentFrmFldView != null);
    }

    @Override
    public void clearCurrentFormFieldView() {
        currentFrmFldView = null;
    }

    @Override
    public void regenerateFormFieldViews() {
        clearCurrentFormFieldView();
        setFormFieldViews(null);
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public String getCurrencyCodeForAmountFieldById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getCurrentLocaleInContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDocumentKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean shouldShowListCodes() {
        // TODO Auto-generated method stub
        return false;
    }

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
        return activity.getFragmentManager();
    }

}
