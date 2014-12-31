/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.expense.doc.data.GovDocument;
import com.concur.mobile.platform.util.Format;

public class DocumentListItem extends ListItem {

    private static final String CLS_TAG = DocumentListItem.class.getSimpleName();
    private GovDocument document;

    public DocumentListItem(GovDocument document) {
        this.document = document;
    }

    public GovDocument getDocument() {
        return document;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = null;
        if (convertView == null) {
            // Inflate a new view.
            inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.document_list_row, null);
        } else {
            rowView = convertView;
        }
        // Populate main row container and static elements
        if (document != null) {
            // Set employee name
            TextView txtView = (TextView) rowView.findViewById(R.id.document_row_employee_name);
            if (txtView != null) {
                txtView.setText(document.travelerName);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate employee name text view!");
            }
            // set document name
            txtView = (TextView) rowView.findViewById(R.id.document_row_docname);
            if (txtView != null) {
                txtView.setText(document.docName);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate docname text view!");
            }
            // set amount
            txtView = (TextView) rowView.findViewById(R.id.document_row_amount);
            if (txtView != null) {
                // Format amount
                String reportTotal = FormatUtil
                    .formatAmount(document.totalExpCost, com.concur.mobile.gov.util.Const.GOV_LOCALE, com.concur.mobile.gov.util.Const.GOV_CURR_CODE, true, true);
                txtView.setText(reportTotal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate doc amount text view!");
            }
            // set doctype
            txtView = (TextView) rowView.findViewById(R.id.document_row_type);
            if (txtView != null) {
                txtView.setText(document.docTypeLabel);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate doc type text view!");
            }
            // set date
            txtView = (TextView) rowView.findViewById(R.id.document_row_date);
            if (txtView != null) {
                StringBuilder strBuilder = new StringBuilder("");
                strBuilder
                    .append(Format
                        .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, document.tripBeginDate));
                strBuilder.append(" - ");
                strBuilder
                    .append(Format
                        .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY, document.tripEndDate));
                txtView.setText(strBuilder.toString());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate date text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: Gov Document is null!");
        }
        return rowView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
