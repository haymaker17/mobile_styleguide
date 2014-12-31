/**
 * 
 */
package com.concur.mobile.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.fragment.RetainerFragment;
import com.concur.mobile.core.view.BooleanFormFieldView;
import com.concur.mobile.core.view.ComboListFormFieldView;
import com.concur.mobile.core.view.ConnectedListFormFieldView;
import com.concur.mobile.core.view.DatePickerFormFieldView;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.core.view.FormFieldView.ValidityCheck;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.InlineTextFormFieldView;
import com.concur.mobile.core.view.MultiLineTextFormFieldView;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.core.view.StaticPickListFormFieldView;
import com.concur.mobile.core.view.StaticTextFormFieldView;
import com.concur.mobile.core.view.YesNoPickListFormFieldView;

/**
 * Provides a utility class for constructing form field view objects.
 */
public class FormUtil {

    private static final String CLS_TAG = FormUtil.class.getSimpleName();

    // Contains the key used to store/retrieve the current form field view set on a form field view listener.
    private static final String CURRENT_FORM_FIELD_VIEW_KEY = "current.form.field.view";

    /**
     * Will store into an instance of <code>Bundle</code> form field state.
     * 
     * @param frmFldViewListener
     *            an instance of <code>IFormFieldViewListener</code> containing form field view state.
     * @param outState
     *            an instance of <code>Bundle</code> in which to store form field view state.
     * @param ignoreValueChanged
     *            a boolean value indicating whether the form field view state information should be saved regardless of whether
     *            there is a change from the underlying form field object.
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
            outState.putString(CURRENT_FORM_FIELD_VIEW_KEY, frmFldViewListener.getCurrentFormFieldView().getFormField()
                    .getId());
        }
    }

    /**
     * Will retain form field view broadcast receiver information.
     * 
     * @param frmFldViewListener
     *            references a form field view listener pre-populated with the form field views to be restored.
     * @param retainer
     *            an instance of a <code>RetainerFragment</code> in which referenced to broadcast receivers
     */
    public static void retainFormFieldState(IFormFieldViewListener frmFldViewListener, RetainerFragment retainer) {
        // Store any form field view non-configuration data.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.onRetainNonConfigurationInstance(retainer);
                }
            }
        }
    }

    /**
     * Will restore the state of any form fields contained in a form field listener. <br>
     * <br>
     * <b>NOTE:<br>
     * This method will restore form field view state from both a <code>Bundle</code> object and a <code>RetainerFragment</code>
     * object.</b> <br>
     * 
     * @param frmFldViewListener
     *            references a form field view listener pre-populated with the form field views to be restored.
     * @param inState
     *            a reference to a <code>Bundle</code> in which form field view state has been written.
     * @param retainer
     *            an instance of a <code>RetainerFragment</code> in which non-configuration data has been stored.
     */
    public static void restoreFormFieldState(IFormFieldViewListener frmFldViewListener, Bundle inState,
            RetainerFragment retainer) {

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

        // Restore any application context registered receivers.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.onApplyNonConfigurationInstance(retainer);
                }
            }
        }
    }

    /**
     * Will add all the <code>View</code> objects as children to <code>viewGroup</code> with associated
     * <code>ExpenseReportFormField</code> objects with access type non-hidden.
     * 
     * @param context
     *            the application context.
     * @param viewGroup
     *            the view group to add view children.
     * @param frmFldViews
     *            the list of form field views.
     */
    public static void populateViewWithFormFieldViews(Context context, ViewGroup viewGroup,
            List<FormFieldView> frmFldViews) {
        if (viewGroup != null) {
            if (frmFldViews != null) {
                boolean addedFormFieldView = false;
                for (FormFieldView frmFldView : frmFldViews) {
                    if (frmFldView.view != null) {
                        if (frmFldView.frmFld.getAccessType() != ExpenseReportFormField.AccessType.HD) {
                            if (addedFormFieldView) {
                                // Add a separator and the view.
                                ViewUtil.addSeparatorView(context, viewGroup);
                            }
                            viewGroup.addView(frmFldView.getView(context));
                            addedFormFieldView = true;
                        }
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateViewWithFormFieldViews: null view to populate!");
        }
    }

    /**
     * Will populate <code>view</code> with form editing views and will return back a list of <code>FormFieldView</code> objects
     * that manage the created views.
     * 
     * @param viewGroup
     *            the view group to contain the created form fields.
     * @param frmFlds
     *            the list of form fields used to create views.
     * @param ignoreFldIds
     *            the list of form field ids that should be ignored.
     * @return a list of <code>FormFieldView</code> objects that manage the created form field views.
     */
    public static List<FormFieldView> populateViewWithFormFields(Context context, ViewGroup viewGroup,
            List<? extends ExpenseReportFormField> frmFlds, List<String> ignoreFldIds,
            IFormFieldViewListener frmFldViewListener) {
        List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
        if (viewGroup != null) {
            if (frmFlds != null) {
                ListIterator<? extends ExpenseReportFormField> frmFldIter = frmFlds.listIterator();
                boolean addedFormFieldView = false;
                while (frmFldIter.hasNext()) {
                    ExpenseReportFormField frmFld = frmFldIter.next();
                    // Check for non-hidden and non-ignored form field.
                    if ((ignoreFldIds == null) || (!ignoreFldIds.contains(frmFld.getId()))) {
                        // Construct the 'FormFieldView' object.
                        FormFieldView frmFldView = FormUtil.buildFormFieldView(frmFld, frmFldViewListener);
                        if (frmFldView != null) {
                            frmFldViews.add(frmFldView);
                            if (frmFld.getAccessType() != ExpenseReportFormField.AccessType.HD) {
                                if (addedFormFieldView) {
                                    // Add a separator and the view.
                                    ViewUtil.addSeparatorView(context, viewGroup);
                                }
                                viewGroup.addView(frmFldView.getView(context));
                                addedFormFieldView = true;
                                
                                // MAN-24239 Set access type to hidden when ctrlType is hidden
                                if (frmFld.getControlType() == ControlType.HIDDEN) {
                                    frmFld.setAccessType(ExpenseReportFormField.AccessType.HD);
                                    hideField(context, frmFldView);
                                }                                
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".populateViewWithFormFields: unable to build 'FormFieldView' object for field '"
                                    + frmFld.getLabel() + "'.");
                        }
                    }
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
     * @param formField
     *            the expense report form field.
     * @param listener
     *            an instance of <code>IFormFieldViewListener</code>.
     * @return an instance of <code>FormFieldView</code> responsible for managing the view.
     */
    public static FormFieldView buildFormFieldView(ExpenseReportFormField formField, IFormFieldViewListener listener) {
        FormFieldView frmFldView = null;
        if (formField != null) {
            // First look at ControlType and for 'textarea' or 'picklist' controls.
        	ControlType type = (formField.getControlType() == ControlType.HIDDEN) ? formField.getOriginalCtrlType() : formField.getControlType();
        	if(type == null) {
        	    // MOB-20418 - If the ControlType is null, set it to UNSPECIFIED to avoid crashing.
        	    type = ControlType.UNSPECIFED;
        	}
        	
            switch (type) {
            case TEXT_AREA: {
                frmFldView = new MultiLineTextFormFieldView(formField, listener);
                break;
            }
            case LIST_EDIT:
            case PICK_LIST: {
                switch (formField.getDataType()) {
                case CONNECTED_LIST: {
                    frmFldView = new ConnectedListFormFieldView(formField, listener);
                    break;
                }
                case BOOLEAN: {
                    frmFldView = new YesNoPickListFormFieldView(formField, listener);
                    break;
                }
                default: {
                    // If a static list has been set on the field then we create a static,
                    // non-searchable picklist. Everything else defaults to the search list.
                    SpinnerItem[] staticList = formField.getStaticList();
                    if (staticList != null) {
                        if (formField.hasLargeValueCount()) {
                            // MOB-14331
                            frmFldView = formField.getSearchListFormFieldView(listener);
                        } else {
                            frmFldView = new StaticPickListFormFieldView(formField, listener, staticList);
                        }
                    } else {
                        frmFldView = formField.getSearchListFormFieldView(listener);
                    }
                    break;
                }
                }
                break;
            }
            case CHECKBOX: {
                frmFldView = new BooleanFormFieldView(formField, listener);
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
                    frmFldView = new InlineTextFormFieldView(formField, listener);
                    break;
                }
                case INTEGER: {
                    // Check if ControlType is 'edit', if so, then use a one-line text edit field.
                    if (type == ExpenseReportFormField.ControlType.EDIT) {
                        frmFldView = new InlineTextFormFieldView(formField, listener);
                    } else {
                        Log.e(Const.LOG_TAG, ViewUtil.CLS_TAG
                                + ".buildFormViewView: DataType == INTEGER but ControlType != EDIT!");
                    }
                    break;
                }
                case TIMESTAMP: {
                    frmFldView = new DatePickerFormFieldView(formField, listener);
                    break;
                }
                case EXPENSE_TYPE: {
                    frmFldView = formField.getExpensePickListFormFieldView(listener);
                    // frmFldView = new ExpenseTypeFormFieldView(formField, listener);
                    break;
                }
                case BOOLEAN: {
                    frmFldView = new BooleanFormFieldView(formField, listener);
                    break;
                }
                case CONNECTED_LIST: {
                    frmFldView = new ConnectedListFormFieldView(formField, listener);
                    break;
                }
                case LIST:
                case CURRENCY:
                case LOCATION: {
                    frmFldView = new SearchListFormFieldView(formField, listener);
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
        return frmFldView;
    }

    /**
     * Will clone the list of passed in <code>ExpenseReportFormField</code> objects. <br>
     * <b>NOTE:<br>
     * This clone operation performs a shallow clone, i.e., object references off the <code>ExpenseReportFormField</code> objects
     * are directly copied, not cloned.
     * 
     * @param flds
     *            the list of fields to clone.
     * @return returns the list of cloned fields.
     */
    public static List<ExpenseReportFormField> cloneFields(List<? extends ExpenseReportFormField> flds) {
        List<ExpenseReportFormField> retVal = null;
        if (flds != null) {
            retVal = new ArrayList<ExpenseReportFormField>(flds.size());
            for (ExpenseReportFormField tcf : flds) {
                try {
                    retVal.add((ExpenseReportFormField) tcf.clone());
                } catch (CloneNotSupportedException cnsExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".cloneFields: ", cnsExc);
                }
            }
        }
        return retVal;
    }

    /**
     * Will copy just the field value from each field from <code>src</code> to <code>dst</code>.
     * 
     * @param src
     *            the list of source form fields.
     * @param dst
     *            the list of destination form fields.
     */
    public static void copyFieldValues(List<ExpenseReportFormField> src, List<ExpenseReportFormField> dst) {
        for (ExpenseReportFormField srcFrmFld : src) {
            ExpenseReportFormField dstFrmFld = FormUtil.findFieldById(dst, srcFrmFld.getId());
            if (dstFrmFld != null) {
                if (srcFrmFld.getValue() != null && srcFrmFld.getValue().length() > 0) {
                    dstFrmFld.setValue(srcFrmFld.getValue());
                }
            }
        }
    }

    /**
     * Will clone <code>master</code> and fill the clone with information from <code>source</code>.
     * 
     * @param master
     *            contains an instance of <code>ExpenseReportAttendee</code> serving as a master template for form fields, etc.
     * @param source
     *            contains an instance of <code>ExpenseReportAttendee</code> providing field information to be copied into the
     *            cloned <code>master</code>.
     * @return returns an instance of <code>ExpenseReportAttendee</code> representing a clone of <code>master</code> and with form
     *         field information filled in from <code>source</code>.
     */
    public static ExpenseReportAttendee cloneAndFill(ExpenseReportAttendee master, ExpenseReportAttendee source) {
        ExpenseReportAttendee clone = null;
        clone = new ExpenseReportAttendee(master);
        clone.copy(source);
        clone.setFormFields(FormUtil.cloneFields(master.getFormFields()));
        for (ExpenseReportFormField srcFrmFld : source.getFormFields()) {
            ExpenseReportFormField dstFrmFld = FormUtil.findFieldById(clone.getFormFields(), srcFrmFld.getId());
            if (dstFrmFld != null) {
                if (srcFrmFld.getValue() != null && srcFrmFld.getValue().length() > 0) {
                    dstFrmFld.setValue(srcFrmFld.getValue());
                }
            }
        }
        return clone;
    }

    /**
     * Will locate an instance of <code>ExpenseReportFormField</code> in a list of fields based on the <code>Id</code> match.
     * 
     * @param fields
     *            contains the list of expense report form fields.
     * @param fldId
     *            contains the expense report form field id to match on.
     * @return returns an instance of <code>ExpenseReportFormField</code> upon success; <code>null</code> otherwise.
     */
    public static ExpenseReportFormField findFieldById(List<ExpenseReportFormField> fields, String fldId) {
        ExpenseReportFormField expRepFrmFld = null;
        if (fields != null && fldId != null) {
            for (ExpenseReportFormField frmFld : fields) {
                if (frmFld.getId() != null && frmFld.getId().equalsIgnoreCase(fldId)) {
                    expRepFrmFld = frmFld;
                    break;
                }
            }
        }
        return expRepFrmFld;
    }

    /**
     * Will locate an instance of <code>FormFieldView</code> based on an exponse report form field id.
     * 
     * @param frmFldViews
     *            contains the list of <code>FormFieldView</code> objects.
     * @param expRepFrmFldId
     *            contains the expense report form field id.
     * @return returns an instance of <code>FormFieldView</code>; otherwise, <code>null</code> is returned.
     */
    public static FormFieldView findFormFieldViewById(List<FormFieldView> frmFldViews, String expRepFrmFldId) {
        FormFieldView frmFldView = null;
        if (frmFldViews != null) {
            for (FormFieldView ffv : frmFldViews) {
                if (ffv.getFormField().getId() != null && expRepFrmFldId != null
                        && ffv.getFormField().getId().equalsIgnoreCase(expRepFrmFldId)) {
                    frmFldView = ffv;
                    break;
                }
            }
        }
        return frmFldView;
    }

    /**
     * Gets an instance of <code>FormFieldView</code> based on the underlying field id.
     * 
     * @param id
     *            the form field id.
     * @return an instance of <code>FormFieldView</code> whose form field id matches <code>id</code>; otherwise <code>null</code>
     *         is returned.
     */
    public static FormFieldView getFieldById(IFormFieldViewListener frmFldViewListener, String id) {
        FormFieldView retVal = null;
        List<FormFieldView> frmFldViews = frmFldViewListener.getFormFieldViews();
        if (frmFldViews != null) {
            for (FormFieldView frmFldView : frmFldViews) {
                if (frmFldView.getFormField().getId().equalsIgnoreCase(id)) {
                    retVal = frmFldView;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Will hide the field whose field id is <code>id</code>.
     * 
     * @param id
     *            the id of the form field view to hide.
     */
    public static void hideFieldById(Context ctx, IFormFieldViewListener frmFldViewListener, String id) {
        FormFieldView frmFldView = getFieldById(frmFldViewListener, id);
        hideField(ctx, frmFldView);
    }
    
    public static void hideField(Context ctx, FormFieldView frmFldView) {
        if (frmFldView != null && frmFldView.view != null) {
            frmFldView.getView(ctx).setVisibility(View.GONE);
            // Hide the previous view as it's just a separator!
            ViewParent viewParent = frmFldView.getView(ctx).getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                int viewGroupInd = -1;
                if ((viewGroupInd = viewGroup.indexOfChild(frmFldView.getView(ctx))) != -1) {
                    if (viewGroupInd > 0) {
                        View view = viewGroup.getChildAt((viewGroupInd - 1));
                        if (R.id.group_view_separator == view.getId()) {
                            view.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }        
    }

    /**
     * Will show the form field view with field id <code>id</code>.
     * 
     * @param id
     *            the id of the form field view to show.
     */
    public static void showFieldById(Context ctx, IFormFieldViewListener frmFldViewListener, String id) {
        FormFieldView frmFldView = getFieldById(frmFldViewListener, id);
        if (frmFldView != null && frmFldView.view != null) {
            frmFldView.getView(ctx).setVisibility(View.VISIBLE);
            // Show the previous view as it's just a separator!
            ViewParent viewParent = frmFldView.getView(ctx).getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                int viewGroupInd = -1;
                if ((viewGroupInd = viewGroup.indexOfChild(frmFldView.getView(ctx))) != -1) {
                    if (viewGroupInd > 0) {
                        View view = viewGroup.getChildAt((viewGroupInd - 1));
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    /**
     * Will find an instance of <code>ComboListFormFieldView</code> with contained <code>InlineTextFormFieldView</code> having id
     * equal to <code>id</code>.
     * 
     * @param frmFldViewListener
     *            contains a reference to the form field view listener.
     * @param id
     *            contains the <code>ExpenseReportFormField</code> id being referenced.
     * @return returns an instance of <code>ComboListFormFieldView</code> that references an <code>InlineTextFormFieldView</code>
     *         having id equal to <code>id</code>.
     */
    public static ComboListFormFieldView findReferencedInlineTextFormFieldView(
            FormFieldViewListener frmFldViewListener, String id) {
        ComboListFormFieldView retVal = null;
        List<FormFieldView> frmFldViews = frmFldViewListener.getFormFieldViews();
        if (frmFldViews != null) {
            for (FormFieldView frmFldView : frmFldViews) {
                if (frmFldView instanceof ComboListFormFieldView) {
                    ComboListFormFieldView clFFV = (ComboListFormFieldView) frmFldView;
                    InlineTextFormFieldView inTxtFFV = clFFV.getInlineTextFormFieldView();
                    if (inTxtFFV.getFormField().getId() != null && inTxtFFV.getFormField().getId().equalsIgnoreCase(id)) {
                        retVal = clFFV;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will find an instance of <code>ComboListFormFieldView</code> with contained <code>SearchListFormFieldView</code> having id
     * equal to <code>id</code>.
     * 
     * @param frmFldViewListener
     *            contains a reference to the form field view listener.
     * @param id
     *            contains the <code>ExpenseReportFormField</code> id being referenced.
     * @return returns an instance of <code>ComboListFormFieldView</code> that references an <code>SearchListFormFieldView</code>
     *         having id equal to <code>id</code>.
     */
    public static ComboListFormFieldView findReferencedSearchListFormFieldView(
            FormFieldViewListener frmFldViewListener, String id) {
        ComboListFormFieldView retVal = null;
        List<FormFieldView> frmFldViews = frmFldViewListener.getFormFieldViews();
        if (frmFldViews != null) {
            for (FormFieldView frmFldView : frmFldViews) {
                if (frmFldView instanceof ComboListFormFieldView) {
                    ComboListFormFieldView clFFV = (ComboListFormFieldView) frmFldView;
                    SearchListFormFieldView srchLstFFV = clFFV.getSearchListFormFieldView();
                    if (srchLstFFV.getFormField().getId() != null
                            && srchLstFFV.getFormField().getId().equalsIgnoreCase(id)) {
                        retVal = clFFV;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * A helper method that checks an ExpenseReportFormField value against Valid Expression, Min Value, and Max Value Endpoints
     * and sets the result of the ValidityCheck accordingly. If there's an error, check the endpoint for an error message.
     * 
     * @param frmFld
     *            contains the <code>ExpenseReportFormField</code> id being referenced.
     * @param listener
     *            contains a reference to the IFormFieldViewListener
     * @param curValue
     *            contains a reference to the value currently in the text field
     * @return returns the <code>ValidityCheck</code> with result set to whether or not the value followed the rules passed in the
     *         Endpoints, and if applicable an error message from the Endpoint (or default if n/a).
     */
    public static ValidityCheck isVarCharValueValid(ExpenseReportFormField frmFld, Context ctx, String curValue) {
        Boolean validityCheckResult = true;
        String validityCheckReason = null;
        String validExp = frmFld.getValidExp();
        if (validExp != null && validExp.length() > 0) {
            validityCheckResult = regexFoundInString(validExp, curValue);
            if (!validityCheckResult) {
                if (frmFld.getFailureMsg() != null && frmFld.getFailureMsg().length() > 0) {
                    validityCheckReason = frmFld.getFailureMsg();
                } else {
                    validityCheckReason = ctx.getText(R.string.general_field_value_invalid).toString();
                }
            }
        }
        if (validityCheckResult && frmFld.getMaxLength() != -1) {
            validityCheckResult = ((curValue == null) || ((curValue != null) && curValue.length() <= frmFld
                    .getMaxLength()));
        }
        if (validityCheckResult && frmFld.getMinLength() != -1) {
            validityCheckResult = ((curValue == null) || ((curValue != null) && curValue.length() >= frmFld
                    .getMinLength()));
        }
        if (!validityCheckResult && validityCheckReason == null) {
            validityCheckReason = FormUtil.getHintText(frmFld, ctx);
        }
        return new ValidityCheck(validityCheckResult, validityCheckReason);
    }

    /**
     * A simple helper class to see if a text value passes a regular expression test
     * 
     * @param validExp
     *            The regular expression we're testing the text value against
     * 
     * @param textFieldValue
     *            The current text value in the text field
     * 
     * @return whether or not the string passed
     */
    public static boolean regexFoundInString(String validExp, String textFieldValue) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(validExp);
            Matcher matches = pattern.matcher(textFieldValue);
            return matches.find();
        } catch (PatternSyntaxException pse) {
            Log.e(Const.LOG_TAG, CLS_TAG + "validExp received an invalid regex string: " + validExp);
            return false;
        }
    }

    /**
     * A helper method that displays an error (ValidityCheck.reason) to the field_note of the calling View.
     * 
     * @param view
     *            The view which will expand a field_note to show the message.
     * @param check
     *            The ValidityCheck which holds the reason the validity check failed.
     * @param ctx
     *            The context that the calling view holds.
     */
    public static void displayFieldNoteIfInvalid(View view, ValidityCheck check, Context ctx) {
        if (!check.result) {
            if (check.reason != null) {
                // setFieldNoteText(check.reason, R.style.FormFieldNoteRedText);
                TextView txtView = (TextView) view.findViewById(R.id.field_note);
                if (txtView != null) {
                    txtView.setText(check.reason);
                    txtView.setTextAppearance(ctx, R.style.FormFieldNoteRedText);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNoteText: unable to locate 'field_note' view.");
                }
                ViewUtil.setVisibility(view, R.id.field_note, View.VISIBLE);
            } else {
                // Ensure the field note is hidden.
                ViewUtil.setVisibility(view, R.id.field_note, View.GONE);
            }
        } else {
            // Ensure the field note is hidden.
            ViewUtil.setVisibility(view, R.id.field_note, View.GONE);
        }
    }

    /**
     * Gets the hint text for a form field.
     * 
     * @param frmFld
     *            an instance of <code>ExpenseReportFormField</code>.
     * @return the form field hint text.
     */
    public static String getHintText(ExpenseReportFormField frmFld, Context ctx) {
        String hintText = null;
        if (frmFld != null && frmFld.getControlType() != null
                && (frmFld.getControlType() == ControlType.EDIT || frmFld.getControlType() == ControlType.TEXT_AREA)) {
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

    /**
     * A helper method that checks an email value against Valid Expression.
     * 
     * @param email
     *            contains a an email which requires validity check.
     * @return returns with result whether or not the value followed the rules/regex.
     */
    public static boolean isEmailValid(String email) {
        boolean validityCheckResult = false;
        String validExp = "^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,4})$";
        Pattern pattern;
        if (!TextUtils.isEmpty(email)) {
            try {
                pattern = Pattern.compile(validExp);
                Matcher matches = pattern.matcher(email);
                validityCheckResult = matches.matches();
            } catch (PatternSyntaxException pse) {
                Log.e(Const.LOG_TAG, CLS_TAG + "validExp received an invalid regex string: " + pse);
                validityCheckResult = false;
            }
        }
        return validityCheckResult;
    }
    
    /**
     * A helper method that checks a login ID value against Valid Expression.
     * 
     * @param emailOrUsername
     *            contains a an email or Concur username which requires validity check.
     * @return returns with result whether or not the value followed the rules/regex.
     */
    public static boolean isLoginUsernameValid(String emailOrUsername) {
        boolean validityCheckResult = false;
        // Note: A valid Concur username must have the '@' symbol plus host/domain/company,
        // but it does not need to be followed by .com, .net, .jp, etc. In addition,
        // it must not start or end in @
        if (!TextUtils.isEmpty(emailOrUsername)) {
            int index = emailOrUsername.indexOf('@');
            validityCheckResult = (index > 0 && index < (emailOrUsername.length() -1)); 
        }
        return validityCheckResult;
    }    

}
