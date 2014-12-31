/**
 * 
 */
package com.concur.mobile.core.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>FormFieldView</code> that combines both a list selection and a free form text selection. This particular
 * view component is really just a container for two existing view components, an <code>InlineTextFormFieldView</code> and a
 * <code>SearchListFormFieldView</code>; however the existing views of the two components are not used.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.ComboListFormFieldView} instead.
 */
public class ComboListFormFieldView extends FormFieldView {

    private static final String CLS_TAG = ComboListFormFieldView.class.getSimpleName();

    private static final String COMBO_LIST_BUNDLE_KEY = "combo.list";

    /**
     * Contains a reference to the inline text field form field view.
     */
    protected InlineTextFormFieldView inTxtFrmFldView;

    /**
     * Contains a reference to the list search form field view.
     */
    protected SearchListFormFieldView srchLstFrmFldView;

    /**
     * An enumeration describing the source for values for this field.
     */
    public enum ValueSource {
        /**
         * Indicates the end-user has provided explicit text.
         */
        INLINE,
        /**
         * Indicates the end-user has selected an entry from a list.
         */
        SEARCH,
        /**
         * Indicates there is no source for a value at this time, i.e., neither field has a value.
         */
        NONE
    };

    /**
     * Contains the current source of the value.
     */
    protected ValueSource valSrc;

    /**
     * Constructs an instance of <code>ComboListFormFieldView</code> with two expense form field object.
     * 
     * @param listFrmFld
     *            contains the list form field object.
     * @param inlineTextFld
     *            contain the inline text form field object.
     * @param listener
     *            contains a reference to the form field view listener.
     */
    public ComboListFormFieldView(ExpenseReportFormField expRepFrmFld, SearchListFormFieldView srchLstFrmFldView,
            InlineTextFormFieldView inTxtFrmFldView, IFormFieldViewListener listener) {
        super(expRepFrmFld, listener);
        this.inTxtFrmFldView = inTxtFrmFldView;
        this.srchLstFrmFldView = srchLstFrmFldView;
        valSrc = ValueSource.NONE;
    }

    /**
     * Sets the source that provides a value for this combo list form field view.
     * 
     * @param valSrc
     *            contains the source providing a value for this form field view.
     */
    public void setValueSource(ValueSource valSrc) {
        this.valSrc = valSrc;
    }

    /**
     * Gets the source that provides a value for this combo list form field view.
     * 
     * @return returns the source providing a value for this form field view.
     */
    public ValueSource getValueSource() {
        return this.valSrc;
    }

    /**
     * Gets the instance of <code>InlineTextFormFieldView</code> handling typed text for combo list form field view.
     * 
     * @return returns the instance of <code>InlineTextFormFieldView</code> handling typed text for combo list form field view.
     */
    public InlineTextFormFieldView getInlineTextFormFieldView() {
        return inTxtFrmFldView;
    }

    /**
     * Gets the instance of <code>SearchListFormFieldView</code> handling list selection for this combo list form field view.
     * 
     * @return return the instance of <code>SearchListFormFieldView</code> handling list selection for this combo list form field
     *         view.
     */
    public SearchListFormFieldView getSearchListFormFieldView() {
        return srchLstFrmFldView;
    }

    @Override
    public void commit() {
        switch (valSrc) {
        case INLINE: {
            if (inTxtFrmFldView != null) {
                inTxtFrmFldView.commit();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".commit: inTxtFrmFldView is null!");
            }
            break;
        }
        case SEARCH: {
            if (srchLstFrmFldView != null) {
                srchLstFrmFldView.commit();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".commit: srchLstFrmFldView is null!");
            }
            break;
        }
        }
    }

    @Override
    public String getCurrentValue() {
        String retVal = null;
        switch (valSrc) {
        case INLINE: {
            if (inTxtFrmFldView != null) {
                retVal = inTxtFrmFldView.getCurrentValue();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrentValue: inTxtFrmFldView is null!");
            }
            break;
        }
        case SEARCH: {
            if (srchLstFrmFldView != null) {
                retVal = srchLstFrmFldView.getValue();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getCurrentValue: srchLstFrmFldView is null!");
            }
            break;
        }
        }
        return retVal;
    }

    @Override
    public View getView(Context context) {
        if (view == null) {
            // Set the initial value based.
            updateValueSource();
            // Inflate the proper view based on whether the underlying form value for
            // 'inTxtFrmFldView' is editable.
            LayoutInflater inflater = LayoutInflater.from(context);
            if (isInlineTextFormFieldViewEditable()) {
                view = inflater.inflate(R.layout.combo_box_form_field, null);
                if (view != null) {
                    // Set up the click handler for the list search icon.
                    View imgView = view.findViewById(R.id.field_search);
                    initListSearchClickHandler(imgView);
                    if (imgView != null) {
                        imgView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (srchLstFrmFldView != null) {
                                    // Set up the launch intent.
                                    Intent intent = srchLstFrmFldView.getListSearchLaunchIntent();
                                    String inlineCurVal = inTxtFrmFldView.getCurrentValue();
                                    // If there is a value for the inline text field, then pass it to the list search.
                                    if (inlineCurVal != null && inlineCurVal.length() > 0) {
                                        intent.putExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT, inlineCurVal);
                                    }
                                    // Launch the list search activity.
                                    listener.startActivityForResult(ComboListFormFieldView.this, intent,
                                            SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE);
                                }
                            }
                        });
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'field_search' view!");
                    }
                    // NOTE: The call to 'updateView' below should be made after the call to initialize the text view
                    // handling as that call will clear out the editable text view.
                    // Initialize the text change handling utilizing logic in 'InlineTextFormFieldView'.
                    inTxtFrmFldView.initEditableInlineTextView(view);
                    // Set the field label.
                    setTextViewText(view, R.id.field_name, buildLabel());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to inflate layout file 'combo_box_form_field'!");
                }
            } else {
                // Set the initial value based.
                updateValueSource();
                // Since the inline text field is read-only, display a static text view rather than
                // a text edit.
                view = inflater.inflate(R.layout.image_form_field, null);
                // Set the image button click handler.
                View imgView = view.findViewById(R.id.field_image);
                initListSearchClickHandler(imgView);
                // Set the field label.
                setTextViewText(view, R.id.field_name, buildLabel());
            }
            // Update the view.
            updateView();
        }
        return view;
    }

    @Override
    protected String getFormFieldValue() {
        String value = null;
        if (valSrc != null) {
            switch (valSrc) {
            case INLINE: {
                if (inTxtFrmFldView != null) {
                    value = inTxtFrmFldView.getCurrentValue();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".updateView: inTxtFrmFldView is null!");
                }
                break;
            }
            case SEARCH: {
                if (srchLstFrmFldView != null) {
                    value = srchLstFrmFldView.getValue();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".updateView: srchLstFrmFldView is null!");
                }
                break;
            }
            }
        }
        return value;
    }

    /**
     * Will initialize the click handling to start a list-based search.
     * 
     * @param view
     *            the view upon which to set up the click handler.
     */
    private void initListSearchClickHandler(View view) {
        if (view != null) {
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (srchLstFrmFldView != null) {
                        // Set up the launch intent.
                        Intent intent = srchLstFrmFldView.getListSearchLaunchIntent();
                        String inlineCurVal = inTxtFrmFldView.getCurrentValue();
                        // If there is a value for the inline text field, then pass it to the list search.
                        if (inlineCurVal != null && inlineCurVal.length() > 0) {
                            intent.putExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT, inlineCurVal);
                        }
                        // Launch the list search activity.
                        listener.startActivityForResult(ComboListFormFieldView.this, intent,
                                SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initImageViewClickHandler: imgView is null!");
        }
    }

    private boolean isInlineTextFormFieldViewEditable() {
        boolean retVal = false;
        if (inTxtFrmFldView != null) {
            retVal = (inTxtFrmFldView.getFormField().getAccessType() == AccessType.RW);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".isInlineTextFormFieldViewEditable: 'inTxtFrmFldView' is null!");
        }
        return retVal;
    }

    /**
     * Updates the value of <code>valSrc</code> based on whether <code>inTxtFrmFldView</code> or <code>srchLstFrmFldView</code>
     * have a value.
     */
    protected void updateValueSource() {
        if (inTxtFrmFldView != null && inTxtFrmFldView.hasValue()) {
            valSrc = ValueSource.INLINE;
        } else if (srchLstFrmFldView != null && srchLstFrmFldView.hasValue()) {
            valSrc = ValueSource.SEARCH;
        } else {
            valSrc = ValueSource.NONE;
        }
    }

    /**
     * Will update the view based on the current value of 'valSrc'.
     */
    protected void updateView() {
        if (valSrc != null) {
            switch (valSrc) {
            case INLINE: {
                if (inTxtFrmFldView != null) {
                    String curValue = inTxtFrmFldView.getCurrentValue();
                    if (isInlineTextFormFieldViewEditable()) {
                        setEditTextText(view, inTxtFrmFldView.getViewFieldValueId(), curValue);
                    } else {
                        setTextViewText(view, R.id.field_value, curValue);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".updateView: inTxtFrmFldView is null!");
                }
                break;
            }
            case SEARCH: {
                if (srchLstFrmFldView != null) {
                    String curValue = srchLstFrmFldView.getValue();
                    if (isInlineTextFormFieldViewEditable()) {
                        setEditTextText(view, inTxtFrmFldView.getViewFieldValueId(), curValue);
                    } else {
                        setTextViewText(view, R.id.field_value, curValue);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".updateView: srchLstFrmFldView is null!");
                }
                break;
            }
            }
        }
    }

    @Override
    public boolean hasValue() {
        boolean retVal = false;
        switch (valSrc) {
        case INLINE: {
            if (inTxtFrmFldView != null) {
                retVal = inTxtFrmFldView.hasValue();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValue: inTxtFrmFldView is null!");
            }
            break;
        }
        case SEARCH: {
            if (srchLstFrmFldView != null) {
                retVal = srchLstFrmFldView.hasValue();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValue: srchLstFrmFldView is null!");
            }
            break;
        }
        }
        return retVal;
    }

    @Override
    public boolean hasValueChanged() {
        boolean retVal = false;
        switch (valSrc) {
        case INLINE: {
            if (inTxtFrmFldView != null) {
                retVal = inTxtFrmFldView.hasValueChanged();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: inTxtFrmFldView is null!");
            }
            break;
        }
        case SEARCH: {
            if (srchLstFrmFldView != null) {
                retVal = srchLstFrmFldView.hasValueChanged();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: srchLstFrmFldView is null!");
            }
            break;
        }
        }
        return retVal;
    }

    @Override
    public ValidityCheck isValueValid() {
        ValidityCheck check = null;
        switch (valSrc) {
        case INLINE: {
            if (inTxtFrmFldView != null) {
                check = inTxtFrmFldView.isValueValid();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: inTxtFrmFldView is null!");
            }
            break;
        }
        case SEARCH: {
            if (srchLstFrmFldView != null) {
                check = srchLstFrmFldView.isValueValid();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".hasValueChanged: srchLstFrmFldView is null!");
            }
            break;
        }
        }
        if (check == null) {
            check = SUCCESS;
        }
        return check;
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        String key = getPrefixedKey(COMBO_LIST_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            setValueSource(ValueSource.valueOf(bundle.getString(key)));
        }
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString(getPrefixedKey(COMBO_LIST_BUNDLE_KEY), getValueSource().name());
    }

    @Override
    public void onSaveInstanceStateIgnoreChange(Bundle bundle) {
        bundle.putString(getPrefixedKey(COMBO_LIST_BUNDLE_KEY), getValueSource().name());
    }

    @Override
    public void setCurrentValue(String value, boolean notify) {
        if (inTxtFrmFldView != null) {
            inTxtFrmFldView.setCurrentValue(value, notify);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCurrentValue: inTxtFrmFldView is null!");
        }
        if (srchLstFrmFldView != null) {
            srchLstFrmFldView.setValue(value);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setCurrentValue: srchLstFrmFldView is null!");
        }
    }

    @Override
    public void updateEditedValue(FormFieldView frmFldView) {
        if (frmFldView instanceof ComboListFormFieldView) {
            ComboListFormFieldView clFFV = (ComboListFormFieldView) frmFldView;
            setValueSource(clFFV.getValueSource());
            updateView();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateEditedValue: 'frmFldView' is not of type 'ComboListFormFieldView'!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SearchListFormFieldView.SEARCH_LIST_REQUEST_CODE: {
            if (data != null) {
                if (data.hasExtra(Const.EXTRA_COMBO_BOX_ACTION)) {
                    switch (data.getIntExtra(Const.EXTRA_COMBO_BOX_ACTION, -1)) {
                    case Const.COMBO_BOX_INLINE_TEXT: {
                        if (isInlineTextFormFieldViewEditable()) {
                            if (inTxtFrmFldView != null) {
                                if (data.hasExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT)) {
                                    String inlineText = data.getStringExtra(Const.EXTRA_COMBO_BOX_INLINE_TEXT);
                                    // Set the typed value in the inline text field.
                                    inTxtFrmFldView.setCurrentValue(inlineText, true);
                                    // Clear out the values in the search list.
                                    srchLstFrmFldView.clear();
                                    setValueSource(ValueSource.INLINE);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: data is missing inline text!");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: inTxtFrmFldView is null!");
                            }
                        } else {
                            // Since the inline text isn't editable, we won't transfer any value.
                            // No-op.
                        }
                        break;
                    }
                    case Const.COMBO_BOX_LIST_SELECTION: {
                        // Forward on the result to the search list.
                        if (srchLstFrmFldView != null) {
                            srchLstFrmFldView.onActivityResult(requestCode, resultCode, data);
                            inTxtFrmFldView.setCurrentValue("", false);
                            setValueSource(ValueSource.SEARCH);
                            updateView();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: srchListFrmFldView is null!");
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult: data is missing extra combo box action!");
                }
            }
            break;
        }
        }
    }

}
