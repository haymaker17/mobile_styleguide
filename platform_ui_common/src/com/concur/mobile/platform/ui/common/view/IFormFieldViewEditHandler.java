package com.concur.mobile.platform.ui.common.view;

import com.concur.mobile.platform.ui.common.view.FormFieldView.IFormFieldViewListener;

/**
 * An interface to handle dedicated (not in-place) editing of a form field view, e.g. list search.
 * 
 * This allows different activities to be used for editing expense vs travel vs invoice form field.
 * 
 * @author yiwenw
 * 
 */
public interface IFormFieldViewEditHandler {

    public void onEditField(SearchListFormFieldView ffv, IFormFieldViewListener listener, int requestCode);

    public void onEditField(ComboListFormFieldView ffv, IFormFieldViewListener listener, int requestCode);

    // TODO More APIs for dedicated editor for other FormFieldViews.
}
