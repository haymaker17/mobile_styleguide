/**
 * 
 */
package com.concur.mobile.core.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ListItem</code> for rendering headers within a list.
 */
public class HeaderListItem extends ListItem {

    private static final String CLS_TAG = HeaderListItem.class.getSimpleName();

    String header;

    /**
     * Constructs an instance of <code>HeaderListItem</code> with a header.
     * 
     * @param header
     *            the header text.
     */
    public HeaderListItem(String header, int listItemViewType) {
        this.header = header;
        this.listItemViewType = listItemViewType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#buildView(android.content.Context, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View headerView = null;
        // Create the main row container and static elements
        LayoutInflater inflater = LayoutInflater.from(context);
        headerView = inflater.inflate(R.layout.list_section_header, null);
        TextView txtView = (TextView) headerView.findViewById(R.id.list_section_header);
        if (txtView != null) {
            txtView.setText(header);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate list section header text view!");
        }
        return headerView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

}
