package com.concur.mobile.platform.ui.common.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
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

import com.concur.mobile.base.util.Format;
import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.common.formfield.IFormField.ControlType;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.fragment.RetainerFragment;
import com.concur.mobile.platform.ui.common.view.BooleanFormFieldView;
import com.concur.mobile.platform.ui.common.view.ComboListFormFieldView;
import com.concur.mobile.platform.ui.common.view.ConnectedListFormFieldView;
import com.concur.mobile.platform.ui.common.view.DatePickerFormFieldView;
import com.concur.mobile.platform.ui.common.view.FormFieldView;
import com.concur.mobile.platform.ui.common.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.platform.ui.common.view.FormFieldView.ValidityCheck;
import com.concur.mobile.platform.ui.common.view.IFormFieldViewEditHandler;
import com.concur.mobile.platform.ui.common.view.InlineTextFormFieldView;
import com.concur.mobile.platform.ui.common.view.MultiLineTextFormFieldView;
import com.concur.mobile.platform.ui.common.view.SearchListFormFieldView;
import com.concur.mobile.platform.ui.common.view.StaticPickListFormFieldView;
import com.concur.mobile.platform.ui.common.view.StaticTextFormFieldView;
import com.concur.mobile.platform.ui.common.view.YesNoPickListFormFieldView;

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
     * Will add all the <code>View</code> objects as children to <code>viewGroup</code> with associated <code>IFormField</code>
     * objects with access type non-hidden.
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
                        if (frmFldView.frmFld.getAccessType() != IFormField.AccessType.HD) {
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
     * @param locale
     *            locale from context, e.g. ConcurCore.getContext() .getResources().getConfiguration().locale. Used for parsing
     *            amount, etc.
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
                    IFormField frmFld = frmFldIter.next();
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
     *            the form field.
     * @param listener
     *            an instance of <code>IFormFieldViewListener</code>.
     * @return an instance of <code>FormFieldView</code> responsible for managing the view.
     */
    public static FormFieldView buildFormFieldView(IFormField formField, IFormFieldViewListener listener,
            IFormFieldViewEditHandler editHandler) {
        FormFieldView frmFldView = null;
        if (formField != null) {
            // First look at ControlType and for 'textarea' or 'picklist' controls.
            switch (formField.getControlType()) {
            case TEXT_AREA: {
                frmFldView = new MultiLineTextFormFieldView(formField, listener);
                break;
            }
            // case LIST_EDIT:
            case PICK_LIST: {
                switch (formField.getDataType()) {
                case CONNECTED_LIST: {
                    frmFldView = new ConnectedListFormFieldView(formField, listener, editHandler);
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
                            // frmFldView = formField.getSearchListFormFieldView(listener);
                            frmFldView = new SearchListFormFieldView(formField, listener, editHandler);
                        } else {
                            frmFldView = new StaticPickListFormFieldView(formField, listener, staticList);
                        }
                    } else {
                        // frmFldView = formField.getSearchListFormFieldView(listener);
                        frmFldView = new SearchListFormFieldView(formField, listener, editHandler);
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
                    frmFldView = new InlineTextFormFieldView(formField, listener, listener.getCurrentLocaleInContext());
                    break;
                }
                case INTEGER: {
                    // Check if ControlType is 'edit', if so, then use a one-line text edit field.
                    if (formField.getControlType() == IFormField.ControlType.EDIT) {
                        frmFldView = new InlineTextFormFieldView(formField, listener,
                                listener.getCurrentLocaleInContext());
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
                // case EXPENSE_TYPE: {
                // frmFldView = formField.getExpensePickListFormFieldView(listener);
                // // frmFldView = new ExpenseTypeFormFieldView(formField, listener);
                // break;
                // }
                case BOOLEAN: {
                    frmFldView = new BooleanFormFieldView(formField, listener);
                    break;
                }
                case CONNECTED_LIST: {
                    frmFldView = new ConnectedListFormFieldView(formField, listener, editHandler);
                    break;
                }
                case LIST:
                case CURRENCY:
                case LOCATION: {
                    frmFldView = new SearchListFormFieldView(formField, listener, editHandler);
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
     * Will clone the list of passed in <code>IFormField</code> objects. <br>
     * <b>NOTE:<br>
     * This clone operation performs a shallow clone, i.e., object references off the <code>IFormField</code> objects are directly
     * copied, not cloned.
     * 
     * @param flds
     *            the list of fields to clone.
     * @return returns the list of cloned fields.
     */
    public static List<IFormField> cloneFields(List<? extends IFormField> flds) {
        List<IFormField> retVal = null;
        if (flds != null) {
            retVal = new ArrayList<IFormField>(flds.size());
            for (IFormField tcf : flds) {
                try {
                    retVal.add((IFormField) tcf.clone());
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
    public static void copyFieldValues(List<IFormField> src, List<IFormField> dst) {
        for (IFormField srcFrmFld : src) {
            IFormField dstFrmFld = FormUtil.findFieldById(dst, srcFrmFld.getId());
            if (dstFrmFld != null) {
                if (srcFrmFld.getValue() != null && srcFrmFld.getValue().length() > 0) {
                    dstFrmFld.setValue(srcFrmFld.getValue());
                }
            }
        }
    }

    // /**
    // * Will clone <code>master</code> and fill the clone with information from <code>source</code>.
    // *
    // * @param master
    // * contains an instance of <code>ExpenseReportAttendee</code> serving as a master template for form fields, etc.
    // * @param source
    // * contains an instance of <code>ExpenseReportAttendee</code> providing field information to be copied into the
    // * cloned <code>master</code>.
    // * @return returns an instance of <code>ExpenseReportAttendee</code> representing a clone of <code>master</code> and with
    // form
    // * field information filled in from <code>source</code>.
    // */
    // public static ExpenseReportAttendee cloneAndFill(ExpenseReportAttendee master, ExpenseReportAttendee source) {
    // ExpenseReportAttendee clone = null;
    // clone = new ExpenseReportAttendee(master);
    // clone.copy(source);
    // clone.setFormFields(FormUtil.cloneFields(master.getFormFields()));
    // for (IFormField srcFrmFld : source.getFormFields()) {
    // IFormField dstFrmFld = FormUtil.findFieldById(clone.getFormFields(), srcFrmFld.getId());
    // if (dstFrmFld != null) {
    // if (srcFrmFld.getValue() != null && srcFrmFld.getValue().length() > 0) {
    // dstFrmFld.setValue(srcFrmFld.getValue());
    // }
    // }
    // }
    // return clone;
    // }

    /**
     * Will locate an instance of <code>IFormField</code> in a list of fields based on the <code>Id</code> match.
     * 
     * @param fields
     *            contains the list of form fields.
     * @param fldId
     *            contains the form field id to match on.
     * @return returns an instance of <code>ExpenseReportFormField</code> upon success; <code>null</code> otherwise.
     */
    public static IFormField findFieldById(List<IFormField> fields, String fldId) {
        IFormField expRepFrmFld = null;
        if (fields != null && fldId != null) {
            for (IFormField frmFld : fields) {
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
     * @param frmFldId
     *            contains the form field id.
     * @return returns an instance of <code>FormFieldView</code>; otherwise, <code>null</code> is returned.
     */
    public static FormFieldView findFormFieldViewById(List<FormFieldView> frmFldViews, String frmFldId) {
        FormFieldView frmFldView = null;
        if (frmFldViews != null) {
            for (FormFieldView ffv : frmFldViews) {
                if (ffv.getFormField().getId() != null && frmFldId != null
                        && ffv.getFormField().getId().equalsIgnoreCase(frmFldId)) {
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
     *            contains the <code>IFormField</code> id being referenced.
     * @return returns an instance of <code>ComboListFormFieldView</code> that references an <code>InlineTextFormFieldView</code>
     *         having id equal to <code>id</code>.
     */
    public static ComboListFormFieldView findReferencedInlineTextFormFieldView(
            IFormFieldViewListener frmFldViewListener, String id) {
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
     *            contains the <code>IFormField</code> id being referenced.
     * @return returns an instance of <code>ComboListFormFieldView</code> that references an <code>SearchListFormFieldView</code>
     *         having id equal to <code>id</code>.
     */
    public static ComboListFormFieldView findReferencedSearchListFormFieldView(
            IFormFieldViewListener frmFldViewListener, String id) {
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
     * A helper method that checks an IFormField value against Valid Expression, Min Value, and Max Value Endpoints and sets the
     * result of the ValidityCheck accordingly. If there's an error, check the endpoint for an error message.
     * 
     * @param frmFld
     *            contains the <code>IFormField</code> id being referenced.
     * @param listener
     *            contains a reference to the IFormFieldViewListener
     * @param curValue
     *            contains a reference to the value currently in the text field
     * @return returns the <code>ValidityCheck</code> with result set to whether or not the value followed the rules passed in the
     *         Endpoints, and if applicable an error message from the Endpoint (or default if n/a).
     */
    public static ValidityCheck isVarCharValueValid(IFormField frmFld, Context ctx, String curValue) {
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
     *            an instance of <code>IFormField</code>.
     * @return the form field hint text.
     */
    public static String getHintText(IFormField frmFld, Context ctx) {
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
            validityCheckResult = (index > 0 && index < (emailOrUsername.length() - 1));
        }
        return validityCheckResult;
    }

    /**
     * 
     * 
     * @param amount
     * @param loc
     * @param crnCode
     * @param useSymbol
     * @param useGrouping
     * @return
     */
    public static String formatAmount(double amount, Locale loc, String crnCode, boolean useSymbol, boolean useGrouping) {
        Currency curr = null;
        if (crnCode != null) {
            try {
                curr = Currency.getInstance(crnCode);
            } catch (IllegalArgumentException iae) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".formatAmount: invalid currency code: " + crnCode, iae);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".formatAmount: currency code is null!");
        }

        int decDigits = 2; // A sane default
        String symbol = "";
        if (curr != null) {
            decDigits = curr.getDefaultFractionDigits();
            symbol = curr.getSymbol(loc);
        }

        NumberFormat nf = DecimalFormat.getInstance(loc);
        nf.setMaximumFractionDigits(decDigits);
        nf.setMinimumFractionDigits(decDigits);
        nf.setGroupingUsed(useGrouping);

        StringBuilder amountSB = new StringBuilder(nf.format(amount));

        if (useSymbol) {
            if (isSymbolASuffix(loc)) {
                amountSB.append(' ').append(symbol);
            } else {
                if (symbol.length() > 1) {
                    // For currency abbreviations, we want a space...
                    amountSB.insert(0, ' ').insert(0, symbol);
                } else {
                    // For one character symbols, we do not.
                    amountSB.insert(0, symbol);
                }
            }
        }

        return amountSB.toString();
    }

    private static boolean isSymbolASuffix(Locale loc) {
        boolean isSuffix = false;

        NumberFormat nf = NumberFormat.getCurrencyInstance(loc);
        if (nf instanceof DecimalFormat) {
            String patt = ((DecimalFormat) nf).toLocalizedPattern();
            if (patt.indexOf('\u00a4') > 0) {
                isSuffix = true;
            }
        }

        return isSuffix;
    }

}
