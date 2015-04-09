/**
 * 
 */
package com.concur.mobile.core.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>SearchListFormField</view> for handling related
 * list views.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.ConnectedListFormFieldView} instead.
 * 
 * @author AndrewK
 */
public class ConnectedListFormFieldView extends SearchListFormFieldView {

    private static final String CLS_TAG = ConnectedListFormFieldView.class.getSimpleName();

    private static final String PARENT_LI_BUNDLE_KEY = "parentLiKey";

    // Contains the edited parent list item key (connected list).
    protected String parentLiKey;

    /**
     * Constructs an instance of <code>ConnectedListFormFieldView</code> with a form field view listener and a parent list.
     * 
     * @param frmFld
     *            the expense report form field.
     * @param listener
     *            the form field view listener.
     */
    public ConnectedListFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);
        parentLiKey = frmFld.getParLiKey();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#parentHasValue()
     */
    @Override
    protected boolean parentHasValue() {
        boolean retVal = true;
        // Check whether this field has a 'hierLevel' of 1, if so, then it's already
        // at the top of the hierarchy and so has no parent. We'll just return true
        // in that case.
        if (frmFld.getHierKey() > 0 && frmFld.getHierLevel() > 1) {
            for (FormFieldView frmFldView : listener.getFormFieldViews()) {
                if (frmFldView.frmFld.getHierKey() > 0 && frmFldView.frmFld.getHierKey() == frmFld.getHierKey()) {
                    if (frmFldView.frmFld.getHierLevel() == (frmFld.getHierLevel() - 1)) {
                        retVal = frmFldView.hasValue();
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#getParentFieldLabel()
     */
    @Override
    protected String getParentFieldLabel() {
        String retVal = null;
        // Check whether this field has a 'hierLevel' of 1, if so, then it's already
        // at the top of the hierarchy and so has no parent. We'll just return true
        // in that case.
        if (frmFld.getHierKey() > 0 && frmFld.getHierLevel() > 1) {
            for (FormFieldView frmFldView : listener.getFormFieldViews()) {
                if (frmFldView.frmFld.getHierKey() > 0 && frmFldView.frmFld.getHierKey() == frmFld.getHierKey()) {
                    if (frmFldView.frmFld.getHierLevel() == (frmFld.getHierLevel() - 1)) {
                        retVal = frmFldView.frmFld.getLabel();
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#addParentLiKey(android.content.Intent)
     */
    @Override
    protected void addParentLiKey(Intent intent) {
        if (parentLiKey != null) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_PARENT_LI_KEY, parentLiKey);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#addMRU(android.content.Intent)
     */
    @Override
    protected void addMRU(Intent intent) {
        if (frmFld.getHierLevel() != -1) {
            intent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_IS_MRU, Boolean.TRUE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".addMRU: HierLevel for form field '" + frmFld.getLabel() + "' is not set!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#listItemSelected()
     */
    @Override
    public void listItemSelected(String liCode, String liKey, String value) {

        super.listItemSelected(liCode, liKey, value);

        if (listener != null) {
            if (listener.getFormFieldViews() != null && listener.getFormFieldViews().size() > 0) {
                if (frmFld.getHierKey() > 0) {
                    // Set up the MRU keys/codes/texts.
                    List<String> nodeKeys = null;
                    List<String> nodeCodes = null;
                    List<String> nodeTexts = null;
                    if (liKey != null) {
                        nodeKeys = splitString(liKey, '-');
                        nodeCodes = splitString(liCode, '\t');
                        nodeTexts = splitString(value, '\t');
                    }
                    List<FormFieldView> updatedFrmFldViews = new ArrayList<FormFieldView>();
                    updatedFrmFldViews.add(this);
                    int levelsUpdated = ((nodeKeys == null) ? 1 : nodeKeys.size());
                    for (FormFieldView frmFldView : listener.getFormFieldViews()) {
                        // Check for hiearchy existence.
                        if (frmFldView.frmFld.getHierKey() > 0) {
                            // Check for hierarchy match.
                            if (frmFldView.frmFld.getHierKey() == frmFld.getHierKey()) {
                                if (frmFldView instanceof ConnectedListFormFieldView) {
                                    ConnectedListFormFieldView conLstFrmFldView = (ConnectedListFormFieldView) frmFldView;
                                    int ix = frmFldView.frmFld.getHierLevel() - frmFld.getHierLevel();
                                    if (ix > 0 && ix <= levelsUpdated) {
                                        String curLiKey = conLstFrmFldView.liKey;
                                        if (nodeKeys != null) {
                                            curLiKey = nodeKeys.get(ix - 1);
                                        }
                                        conLstFrmFldView.parentLiKey = curLiKey;
                                        if (ix < levelsUpdated) {
                                            conLstFrmFldView.liKey = nodeKeys.get(ix);
                                            conLstFrmFldView.liCode = nodeCodes.get(ix);
                                            conLstFrmFldView.value = nodeTexts.get(ix);
                                            conLstFrmFldView.updateView();
                                            if (conLstFrmFldView.listener != null) {
                                                conLstFrmFldView.listener.valueChanged(conLstFrmFldView);
                                            }
                                        } else {
                                            conLstFrmFldView.liKey = null;
                                            conLstFrmFldView.liCode = null;
                                            conLstFrmFldView.value = null;
                                            conLstFrmFldView.updateView();
                                            if (conLstFrmFldView.listener != null) {
                                                conLstFrmFldView.listener.valueChanged(conLstFrmFldView);
                                            }
                                        }
                                        updatedFrmFldViews.add(conLstFrmFldView);

                                    } else if (ix > levelsUpdated) {
                                        conLstFrmFldView.parentLiKey = null;
                                        conLstFrmFldView.liKey = null;
                                        conLstFrmFldView.liCode = null;
                                        conLstFrmFldView.value = null;
                                        conLstFrmFldView.updateView();
                                        updatedFrmFldViews.add(frmFldView);
                                        if (conLstFrmFldView.listener != null) {
                                            conLstFrmFldView.listener.valueChanged(conLstFrmFldView);
                                        }
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".listItemSelected: form field view in hiearchy is not a connected list form field view!");
                                }
                            }
                        }
                    }
                    // Update 'liKey', 'liCode' and 'value' with the first element in key, code and text lists.
                    Boolean valueChanged = false;
                    if (nodeKeys != null && nodeKeys.size() > 0) {
                        setLiKey(nodeKeys.get(nodeKeys.size() -1));
                        valueChanged = true;
                    }
                    if (nodeCodes != null && nodeCodes.size() > 0) {
                        setLiCode(nodeCodes.get(nodeCodes.size() -1));
                        valueChanged = true;
                    }
                    if (nodeTexts != null && nodeTexts.size() > 0) {
                        setValue(nodeTexts.get(nodeTexts.size() -1));
                        valueChanged = true;
                    }
                    if (valueChanged) {
                        updateView();
                        if (listener != null) {
                            listener.valueChanged(this);
                        }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".listItemSelected: form field views is null or size 0!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".listItemSelected: listener is null!");
        }
    }

    /**
     * Will split <code>value</code> into separate strings based on <code>delimiter</code>.
     * 
     * @param value
     *            the value to split.
     * @param delimiter
     *            the character delimiter.
     * @return a list of <code>String</code> objects.
     */
    private List<String> splitString(String value, char delimiter) {
        List<String> retVal = new ArrayList<String>();
        if (value.indexOf(delimiter) != -1) {
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(delimiter);
            splitter.setString(value);
            Iterator<String> splitIter = splitter.iterator();
            while (splitIter.hasNext()) {
                retVal.add(splitIter.next());
            }
        } else {
            retVal.add(value);
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#commit()
     */
    @Override
    public void commit() {
        super.commit();
        frmFld.setParLiKey((parentLiKey != null) ? parentLiKey : "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#hasValueChanged()
     */
    @Override
    public boolean hasValueChanged() {
        boolean retVal = super.hasValueChanged();
        // If the parent class reported no change, then examine the parentLiKey property.
        if (!retVal) {
            String origParentLiKey = (frmFld.getParLiKey() != null) ? frmFld.getParLiKey() : "";
            String curParentLiKey = (parentLiKey != null) ? parentLiKey : "";
            retVal = !curParentLiKey.contentEquals(origParentLiKey);
        }
        return retVal;
    }

    @Override
    public String getCurrentValue() {
        // TODO: get the current value.
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.SearchListFormFieldView#updateEditedValue(com.concur.mobile.util.FormFieldView)
     */
    @Override
    public void updateEditedValue(FormFieldView frmFldView) {

        // Check whether the list keys are non-null and have changed.
        if (frmFld.getListKey() != null && frmFldView.frmFld.getListKey() != null
                && !frmFld.getListKey().equalsIgnoreCase(frmFldView.frmFld.getListKey())) {
            // Due to an existing MWS bug, we'll clear out the 'value', 'liKey' and 'liCode' values
            // as the new attributes contian values from the previous expense type selection!
            value = null;
            frmFld.setValue(null);
            liKey = null;
            frmFld.setLiKey(null);
            liCode = null;
            frmFld.setLiCode(null);
            updateView();
        } else {
            // Check for whether this field is editable.
            if (frmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
                // Check for whether value has changed in source form field view.
                if (frmFldView.hasValueChanged()) {
                    // Check for same type of field.
                    if (frmFldView instanceof ConnectedListFormFieldView) {
                        ConnectedListFormFieldView conLstFrmFldView = (ConnectedListFormFieldView) frmFldView;
                        parentLiKey = conLstFrmFldView.parentLiKey;
                        super.updateEditedValue(frmFldView);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        String key = getPrefixedKey(PARENT_LI_BUNDLE_KEY);
        if (bundle.containsKey(key)) {
            parentLiKey = bundle.getString(key);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.FormFieldView#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (hasValueChanged()) {
            super.onSaveInstanceState(bundle);
            bundle.putString(getPrefixedKey(PARENT_LI_BUNDLE_KEY), parentLiKey);
        }
    }

}
