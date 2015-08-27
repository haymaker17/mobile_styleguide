package com.concur.mobile.core.travel.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.travel.data.TravelCustomFieldValueSpinnerItem;
import com.concur.mobile.core.view.ListItem;

/**
 * 
 * @author RatanK
 * 
 */
public class TravelCustomFieldListItem extends ListItem {

    private TravelCustomFieldValueSpinnerItem spItem;
    private int colourId;

    public TravelCustomFieldListItem(TravelCustomFieldValueSpinnerItem spItem, int listItemViewType) {
        this.spItem = spItem;
        this.listItemViewType = listItemViewType;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        RelativeLayout row = (RelativeLayout) convertView;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = (RelativeLayout) inflater.inflate(R.layout.expense_list_search_row, null);
        }

        TextView tv = (TextView) row.findViewById(R.id.listItemName);
        String displayText = (spItem.optionText == null ? spItem.name : spItem.optionText);
        tv.setText(displayText);

        // set the background colour
        tv.setBackgroundColor(colourId);

        return row;
    }

    public TravelCustomFieldValueSpinnerItem getSelectedItem() {
        return spItem;
    }

    public void setColourId(int colourId) {
        this.colourId = colourId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        TravelCustomFieldListItem other = (TravelCustomFieldListItem) obj;
        return (spItem.valueId.equals(other.spItem.valueId));
    }
}