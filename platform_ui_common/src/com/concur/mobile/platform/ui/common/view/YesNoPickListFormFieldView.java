package com.concur.mobile.platform.ui.common.view;

import com.concur.mobile.platform.common.SpinnerItem;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.ui.common.R;

/**
 * An extension of <code>StaticListFormFieldView</code> for providing an explicit yes/no selection.
 * 
 * @author AndrewK
 */
public class YesNoPickListFormFieldView extends StaticPickListFormFieldView {

    /**
     * Constructs an instance of <code>YesNoPickListFormFieldView</code> based on a report form field.
     * 
     * @param frmFld
     *            the report form field.
     * @param listener
     *            the view listener.
     */
    public YesNoPickListFormFieldView(IFormField frmFld, IFormFieldViewListener listener) {
        super(frmFld, listener);

        setItems(new SpinnerItem[] {
                new SpinnerItem("N", listener.getActivity().getText(R.string.general_no).toString()),
                new SpinnerItem("Y", listener.getActivity().getText(R.string.general_yes).toString()) });

        // Default to the value of 'Yes' if 'LiKey' is 'null'. CTE does this! MOB-4895.
        if (frmFld.getLiKey() == null) {
            curValue = items[1];
        }
    }

}
