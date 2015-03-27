package com.concur.mobile.platform.ui.travel.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.common.formfield.IFormField.ControlType;
import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.ui.common.view.*;
import com.concur.mobile.platform.ui.common.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.loader.TravelCustomField;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FormUtil {

    private static final String CLS_TAG = FormUtil.class.getSimpleName();

    // Contains the key used to store/retrieve the current form field view set on a form field view listener.
    private static final String CURRENT_FORM_FIELD_VIEW_KEY = "current.form.field.view";

    /**
     * Will populate <code>view</code> with form editing views and will return back a list of <code>FormFieldView</code> objects
     * that manage the created views.
     *
     * @param viewGroup    the view group to contain the created form fields.
     * @param frmFlds      the list of form fields used to create views.
     * @param ignoreFldIds the list of form field ids that should be ignored.
     * @param locale       locale from context, e.g. ConcurCore.getContext() .getResources().getConfiguration().locale. Used for parsing
     *                     amount, etc.
     * @return a list of <code>FormFieldView</code> objects that manage the created form field views.
     */
    public static List<FormFieldView> populateViewWithFormFields(Context context, ViewGroup viewGroup,
            List<? extends IFormField> frmFlds, List<String> ignoreFldIds, IFormFieldViewListener frmFldViewListener,
            IFormFieldViewEditHandler editHandler) {
        List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
        if (viewGroup != null) {
            if (frmFlds != null) {
                ListIterator<? extends IFormField> frmFldIter = frmFlds.listIterator();
                boolean addedFormFieldView = false;
                while (frmFldIter.hasNext()) {
                    TravelCustomField frmFld = (TravelCustomField) frmFldIter.next();
                    // Check for non-hidden and non-ignored form field.
                    if ((ignoreFldIds == null) || (!ignoreFldIds.contains(frmFld.getId()))) {
                        // Construct the 'FormFieldView' object.
                        FormFieldView frmFldView = FormUtil.buildFormFieldView(frmFld, frmFldViewListener, editHandler);
                        if (frmFldView != null) {
                            frmFldViews.add(frmFldView);
                            if (frmFld.getAccessType() != IFormField.AccessType.HD) {
                                if (addedFormFieldView) {
                                    // Add a separator and the view.
                                    ViewUtil.addSeparatorView(context, viewGroup);
                                }
                                viewGroup.addView(frmFldView.getView(context));
                                addedFormFieldView = true;

                                // MAN-24239 Set access type to hidden when ctrlType is hidden
                                if (frmFld.getControlType() == ControlType.HIDDEN) {
                                    frmFld.setAccessType(IFormField.AccessType.HD);
                                    com.concur.mobile.platform.ui.common.util.FormUtil.hideField(context, frmFldView);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".populateViewWithFormFields: unable to build 'FormFieldView' object for field '"
                                    + frmFld.getLabel() + "'.");
                        }
                    }
                }
                if (addedFormFieldView) {
                    // Add a separator at the end
                    ViewUtil.addSeparatorView(context, viewGroup);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateViewWithFields: null view to populate!");
        }
        return frmFldViews;
    }

    /**
     * Builds an instance of <code>FormFieldView</code> based on <code>formField</code>.
     *
     * @param formField the form field.
     * @param listener  an instance of <code>IFormFieldViewListener</code>.
     * @return an instance of <code>FormFieldView</code> responsible for managing the view.
     */
    public static FormFieldView buildFormFieldView(TravelCustomField formField, IFormFieldViewListener listener,
            IFormFieldViewEditHandler editHandler) {
        FormFieldView frmFldView = null;
        if (formField != null) {
            // First look at ControlType and for 'textarea' or 'picklist' controls.
            switch (formField.getControlType()) {
            case TEXT_AREA: {
                frmFldView = new MultiLineTextFormFieldView(formField, listener);
                frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                break;
            }
            // case LIST_EDIT:
            case PICK_LIST: {
                switch (formField.getDataType()) {
                case CONNECTED_LIST: {
                    frmFldView = new ConnectedListFormFieldView(formField, listener, editHandler);
                    frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                    break;
                }
                case BOOLEAN: {
                    frmFldView = new YesNoPickListFormFieldView(formField, listener);
                    frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                    break;
                }
                default: {
                    // If a static list has been set on the field then we create a static,
                    // non-searchable picklist. Everything else defaults to the search list.
                    SpinnerItem[] staticList = formField.getStaticList();
                    if (staticList != null) {
                        if (formField.hasLargeValueCount()) {
                            // MOB-14331
                            // frmFldView = formField.getSearchListFormFieldView(listener);
                            frmFldView = new SearchListFormFieldView(formField, listener, editHandler);
                            frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                        } else {
                            frmFldView = new StaticPickListFormFieldView(formField, listener, staticList);
                            frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                        }
                    } else {
                        // frmFldView = formField.getSearchListFormFieldView(listener);
                        frmFldView = new SearchListFormFieldView(formField, listener, editHandler);
                        frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                    }
                    break;
                }
                }
                break;
            }
            case CHECKBOX: {
                frmFldView = new BooleanFormFieldView(formField, listener);
                frmFldView.layoutResourceId = R.layout.travel_checkbox_form_field;
                break;
            }
            case STATIC: {
                frmFldView = new StaticTextFormFieldView(formField, listener);
                break;
            }
            default: {
                // Next, look at DataType.
                switch (formField.getDataType()) {
                case VARCHAR:
                case MONEY:
                case NUMERIC:
                case CHAR: {
                    frmFldView = new InlineTextFormFieldView(formField, listener, listener.getCurrentLocaleInContext());
                    frmFldView.layoutResourceId = R.layout.travel_edit_text_form_field;
                    break;
                }
                case INTEGER: {
                    // Check if ControlType is 'edit', if so, then use a one-line text edit field.
                    if (formField.getControlType() == IFormField.ControlType.EDIT) {
                        frmFldView = new InlineTextFormFieldView(formField, listener,
                                listener.getCurrentLocaleInContext());
                        frmFldView.layoutResourceId = R.layout.travel_edit_text_form_field;
                    } else {
                        Log.e(Const.LOG_TAG,
                                ViewUtil.CLS_TAG + ".buildFormViewView: DataType == INTEGER but ControlType != EDIT!");
                    }
                    break;
                }
                case TIMESTAMP: {
                    frmFldView = new DatePickerFormFieldView(formField, listener);
                    frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                    break;
                }
                // case EXPENSE_TYPE: {
                // frmFldView = formField.getExpensePickListFormFieldView(listener);
                // // frmFldView = new ExpenseTypeFormFieldView(formField, listener);
                // break;
                // }
                case BOOLEAN: {
                    frmFldView = new BooleanFormFieldView(formField, listener);
                    frmFldView.layoutResourceId = R.layout.travel_checkbox_form_field;
                    break;
                }
                case CONNECTED_LIST: {
                    frmFldView = new ConnectedListFormFieldView(formField, listener, editHandler);
                    frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                    break;
                }
                case LIST:
                case CURRENCY:
                case LOCATION: {
                    frmFldView = new SearchListFormFieldView(formField, listener, editHandler);
                    frmFldView.layoutResourceId = R.layout.travel_custom_form_field;
                    break;
                }
                default: {
                    Log.e(Const.LOG_TAG, ViewUtil.CLS_TAG + ".buildFormFieldView: unknown data type value!");
                    break;
                }
                }
                break;
            }
            }
        } else {
            Log.e(Const.LOG_TAG, ViewUtil.CLS_TAG + ".buildFormFieldView: form field is null!");
        }
        if (frmFldView != null) {
            frmFldView.staticTextLayoutResourceId = R.layout.travel_static_text_form_field;
        }
        return frmFldView;
    }

    /**
     * Will store into an instance of <code>Bundle</code> form field state.
     *
     * @param frmFldViewListener an instance of <code>IFormFieldViewListener</code> containing form field view state.
     * @param outState           an instance of <code>Bundle</code> in which to store form field view state.
     * @param ignoreValueChanged a boolean value indicating whether the form field view state information should be saved regardless of whether
     *                           there is a change from the underlying form field object.
     */
    public static void storeFormFieldState(IFormFieldViewListener frmFldViewListener, Bundle outState,
            boolean ignoreValueChanged) {
        // Save out any form field view objects.
        if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null) {
            for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                if (ignoreValueChanged) {
                    frmFldView.onSaveInstanceStateIgnoreChange(outState);
                } else {
                    frmFldView.onSaveInstanceState(outState);
                }
            }
        }

        // Save out any current form field view.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()) {
            outState.putString(CURRENT_FORM_FIELD_VIEW_KEY,
                    frmFldViewListener.getCurrentFormFieldView().getFormField().getId());
        }
    }

    /**
     * Will restore the state of any form fields contained in a form field listener. <br>
     * <br>
     * <b>NOTE:<br>
     * This method will restore form field view state from both a <code>Bundle</code> object and a <code>RetainerFragment</code>
     * object.</b> <br>
     *
     * @param frmFldViewListener references a form field view listener pre-populated with the form field views to be restored.
     * @param inState            a reference to a <code>Bundle</code> in which form field view state has been written.
     */
    public static void restoreFormFieldState(IFormFieldViewListener frmFldViewListener, Bundle inState) {

        // Restore any values to the various form field views.
        if (frmFldViewListener != null && inState != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.onRestoreInstanceState(inState);
                }
            }
        }

        // Restore any current form field view.
        if (frmFldViewListener != null && inState != null && inState.containsKey(CURRENT_FORM_FIELD_VIEW_KEY)) {
            String curFrmFldViewId = inState.getString(CURRENT_FORM_FIELD_VIEW_KEY);
            FormFieldView curFrmFldView = frmFldViewListener.findFormFieldViewById(curFrmFldViewId);
            if (curFrmFldView != null) {
                frmFldViewListener.setCurrentFormFieldView(curFrmFldView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".restoreFormFieldState: lastSavedState contains a value for '"
                        + CURRENT_FORM_FIELD_VIEW_KEY + "' of '" + curFrmFldViewId
                        + "' but unable to locate form field view!");
            }
        }
    }

    /**
     * Gets the hint text for a form field.
     *
     * @param frmFld an instance of <code>ExpenseReportFormField</code>.
     * @return the form field hint text.
     */
    public static String getHintText(IFormField frmFld, Context ctx) {
        String hintText = null;
        if (frmFld != null && frmFld.getControlType() != null && (frmFld.getControlType() == ControlType.EDIT
                || frmFld.getControlType() == ControlType.TEXT_AREA)) {
            if (frmFld.getMinLength() != -1 && frmFld.getMaxLength() != -1) {
                // Set "from min to max" hint text.
                int strResId = -1;
                switch (frmFld.getDataType()) {
                case INTEGER: {
                    strResId = R.string.general_enter_n_to_m_digits_hint;
                    break;
                }
                case CHAR: {
                    strResId = R.string.general_enter_n_to_m_characters_hint;
                    break;
                }
                case VARCHAR: {
                    strResId = R.string.general_enter_n_to_m_characters_hint;
                    break;
                }
                }
                if (strResId != -1) {
                    hintText = Format.localizeText(ctx, strResId, frmFld.getMinLength(), frmFld.getMaxLength());
                }
            } else if (frmFld.getMinLength() != -1) {
                // Set "at least" hint text.
                int strResId = -1;
                switch (frmFld.getDataType()) {
                case INTEGER: {
                    strResId = R.string.general_enter_at_least_n_digits_hint;
                    break;
                }
                case CHAR: {
                    strResId = R.string.general_enter_at_least_n_characters_hint;
                    break;
                }
                case VARCHAR: {
                    strResId = R.string.general_enter_n_to_m_characters_hint;
                    break;
                }
                }
                if (strResId != -1) {
                    hintText = Format.localizeText(ctx, strResId, frmFld.getMinLength());
                }
            } else if (frmFld.getMaxLength() != -1) {
                // Set "at most" hint text.
                int strResId = -1;
                switch (frmFld.getDataType()) {
                case INTEGER: {
                    strResId = R.string.general_enter_up_to_n_digits_hint;
                    break;
                }
                case CHAR: {
                    strResId = R.string.general_enter_up_to_n_characters_hint;
                    break;
                }
                case VARCHAR: {
                    strResId = R.string.general_enter_up_to_n_characters_hint;
                    break;
                }
                }
                if (strResId != -1) {
                    hintText = Format.localizeText(ctx, strResId, frmFld.getMaxLength());
                }
            }
        }
        return hintText;
    }

}
