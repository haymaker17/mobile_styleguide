package com.concur.mobile.core.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.view.ListItem;

/**
 * ListItem subclass to work with ListItemAdapter
 */
public class MessageCenterListItem extends ListItem {

    private String title;
    private String body;

    /**
     * Constructs an instance of <code>MessageCenterListItem</code>.
     * 
     * @param title
     *            title of the message
     * @param body
     *            body of the message
     * 
     */
    public MessageCenterListItem(String title, String body, int listItemViewType) {
        this.title = title;
        this.body = body;
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
        View messageView = null;

        if (convertView == null) {
            // Create the main row container and static elements
            LayoutInflater inflater = LayoutInflater.from(context);
            messageView = inflater.inflate(R.layout.message_center_row, null);
        } else {
            messageView = convertView;
        }

        TextView txtView = (TextView) messageView.findViewById(R.id.title);
        if (txtView != null) {
            if (title != null)
                txtView.setText(title);
        }

        TextView bodyView = (TextView) messageView.findViewById(R.id.msgbody);
        if (bodyView != null) {
            if (body != null)
                bodyView.setText(body);
        }

        return messageView;
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

}