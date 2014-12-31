/**
 * 
 */
package com.concur.mobile.core.view;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;

/**
 * An extension of <code>StaticListFormFieldView</code> for providing an explicity yes/no selection.
 * 
 * @deprecated - use {@link com.concur.platform.ui.common.view.YesNoPickListFormFieldView} instead.
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
    public YesNoPickListFormFieldView(ExpenseReportFormField frmFld, IFormFieldViewListener listener) {
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
