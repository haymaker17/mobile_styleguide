package com.concur.mobile.platform.ui.travel.view;

import java.util.ArrayList;

import android.content.Intent;

import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.ui.common.view.ComboListFormFieldView;
import com.concur.mobile.platform.common.FieldValueSpinnerItem;
import com.concur.mobile.platform.ui.common.view.FormFieldView;
import com.concur.mobile.platform.ui.common.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.platform.ui.common.view.IFormFieldViewEditHandler;
import com.concur.mobile.platform.ui.common.view.SearchListFormFieldView;
import com.concur.mobile.platform.ui.travel.activity.TravelCustomFieldSearch;
import com.concur.mobile.platform.ui.travel.util.Const;

public class FormFieldViewEditHandler implements IFormFieldViewEditHandler {

    @Override
    public void onEditField(SearchListFormFieldView ffv, IFormFieldViewListener listener, int requestCode) {
        Intent intent = null;

        if (ffv.frmFld.hasLargeValueCount()) {
            // Launch the static list with search activity
            intent = getStaticAndDynamicListSearchLaunchIntent(listener, ffv.frmFld, ffv.selectedListItem);

        }
        // else {
        // // Launch the list search activity with the appropriate parameters.
        // intent = getListSearchLaunchIntent(listener, ffv.frmFld);
        // }

        if (intent != null) {
            // Launch the search list activity.
            listener.startActivityForResult(ffv, intent, requestCode);
        }

    }

    @Override
    public void onEditField(ComboListFormFieldView ffv, IFormFieldViewListener listener, int requestCode) {
        // TODO Auto-generated method stub

    }

    /**
     * Will return an <code>Intent</code> object that can be used to launch a static list with search option based on the
     * <code>ExpenseReportFormField</code> backing this view.
     * 
     * @return returns an <code>Intent</code> object that can be used to launch a static list with search option based on the
     *         <code>ExpenseReportFormField</code> backing this view.
     */
    protected Intent getStaticAndDynamicListSearchLaunchIntent(IFormFieldViewListener listener, IFormField frmFld,
            FieldValueSpinnerItem selectedListItem) {
        Intent intent = new Intent(listener.getActivity(), TravelCustomFieldSearch.class);
        if (frmFld.getId() != null) {
            intent.putExtra(Const.EXTRA_LIST_SEARCH_FIELD_ID, frmFld.getId());
        }
        if (frmFld.getLabel() != null) {
            intent.putExtra(Const.EXTRA_LIST_SEARCH_TITLE, frmFld.getLabel());
        }
        SpinnerItem[] ssl = frmFld.getStaticList();
        if (ssl != null) {
            ArrayList<SpinnerItem> sItemList = new ArrayList<SpinnerItem>(ssl.length);
            for (SpinnerItem sItem : ssl) {
                sItemList.add(sItem);
            }
            intent.putExtra(Const.EXTRA_LIST_SEARCH_STATIC_LIST, sItemList);
        }

        intent.putExtra(Const.EXTRA_SEARCH_SELECTED_ITEM, selectedListItem);

        // Add any parent list item key if this is a connected list.
        addParentLiKey(intent, listener, frmFld);

        return intent;
    }

    /**
     * Adds the parent list item key.
     * 
     * @param intent
     *            the intent to add the parent list item key.
     */
    /*
     * MOB-14509
     * 
     * This is a mess that mirrors how iOS hacked it. Basically, us getting the CtrySubCode relies on sending a parentLiKey to the
     * server that is the liKey of CtryCode. Unfortunately with how the back end sits right now, that's exactly what we had to do.
     * In the future, iOS and Android should fix this when we have back end support.
     */
    private void addParentLiKey(Intent intent, IFormFieldViewListener listener, IFormField frmFld) {
        String formFieldId = frmFld.getId();
        if (formFieldId != null && formFieldId.equalsIgnoreCase("CtrySubCode")) {
            FormFieldView ffv = listener.findFormFieldViewById("CtryCode");

            if (ffv != null && ffv instanceof SearchListFormFieldView) {
                String parentLiKey = ((SearchListFormFieldView) ffv).getLiKey();
                if (parentLiKey != null) {
                    intent.putExtra(Const.EXTRA_LIST_SEARCH_PARENT_LI_KEY, parentLiKey);
                }
            } else if (ffv != null) {
                // If it's not a SearchListFormFieldView (IE if the CtryCode view is hidden), then grab the formField value that
                // we get back from the server and use that.
                IFormField parentFrmFld = ffv.getFormField();
                if (parentFrmFld != null) {
                    String parentLiKey = parentFrmFld.getLiKey();
                    if (parentLiKey != null) {
                        intent.putExtra(Const.EXTRA_LIST_SEARCH_PARENT_LI_KEY, parentLiKey);
                    }
                }
            }
        }
    }

    // /**
    // * Will return an <code>Intent</code> object that can be used to launch a list search based on the
    // * <code>ExpenseReportFormField</code> backing this view.
    // *
    // * @return returns an <code>Intent</code> object that can be used to launch a list search based on the
    // * <code>ExpenseReportFormField</code> backing this view.
    // */
    // protected Intent getListSearchLaunchIntent(IFormFieldViewListener listener, IFormField frmFld) {
    // Intent intent = new Intent(listener.getActivity(), ListSearch.class);
    // if (frmFld.getId() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_FIELD_ID, frmFld.getId());
    // }
    // if (frmFld.getFtCode() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_FT_CODE, frmFld.getFtCode());
    // }
    // if (frmFld.getListKey() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_LIST_KEY, frmFld.getListKey());
    // }
    // if (frmFld.getLabel() != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_TITLE, frmFld.getLabel());
    // }
    // ArrayList<IListFieldItem> ssl = frmFld.getSearchableStaticList();
    // if (ssl != null) {
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_STATIC_LIST, ssl);
    // }
    //
    // // Add any parent list item key if this is a connected list.
    // addParentLiKey(intent, listener, frmFld);
    //
    // // Add any MRU intent extra.
    // intent.putExtra(Const.EXTRA_LIST_SEARCH_IS_MRU, Boolean.TRUE);
    //
    // return intent;
    // }

}
