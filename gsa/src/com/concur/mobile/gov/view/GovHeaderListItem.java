/**
 * 
 */
package com.concur.mobile.gov.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.gov.travel.air.activity.GovRateType;

/**
 * An extension of <code>ListItem</code> for rendering headers within a list.
 */
public class GovHeaderListItem extends ListItem {

    private static final String CLS_TAG = GovHeaderListItem.class.getSimpleName();

    String header;

    int count;

    GovRateType rateType;

    OnClickListener headerClickListener;

    /**
     * Constructs an instance of <code>HeaderListItem</code> with a header.
     * 
     * @param header
     *            the header text.
     */
    public GovHeaderListItem(String header, int listItemViewType) {
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
        headerView = inflater.inflate(R.layout.gov_list_section_header, null);
        TextView txtView = (TextView) headerView.findViewById(R.id.list_section_header);
        txtView.setText(header);

        txtView = (TextView) headerView.findViewById(R.id.list_section_total_count);
        Resources res = context.getResources();
        int finalCount = getCount();
        String countVal = res.getString(R.string.gov_travel_see_all_result_negative);
        if (finalCount > 0) {
            countVal = res.getQuantityString(R.plurals.gov_travel_see_all_result, finalCount, finalCount);
        }
        txtView.setText(countVal);

        txtView.setOnClickListener(getHeaderClickListener());
        return headerView;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setRateType(GovRateType rateType) {
        this.rateType = rateType;
    }

    public GovRateType getRateType() {
        return rateType;
    }

    public void setOnClick(OnClickListener headerClickListener) {
        this.headerClickListener = headerClickListener;
    }

    public OnClickListener getHeaderClickListener() {
        return headerClickListener;
    }

}
