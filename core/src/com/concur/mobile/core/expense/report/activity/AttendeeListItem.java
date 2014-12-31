/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.util.HashSet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> for rendering attendee information.
 */
public class AttendeeListItem extends ListItem {

    private static final String CLS_TAG = AttendeeListItem.class.getSimpleName();

    /**
     * A constant defining a list item type based on a favorites attendee search. search.
     */
    public static final int FAVORITE_SEARCH_ATTENDEE_LIST_ITEM = 1;
    /**
     * A constant defining a list item type based on an advanced attendee search.
     */
    public static final int ADVANCED_SEARCH_ATTENDEE_LIST_ITEM = 2;

    /**
     * Contains the attendee.
     */
    protected ExpenseReportAttendee attendee;

    /**
     * Contains a reference to listener to handle a checkbox checking/unchecking.
     */
    protected OnCheckedChangeListener checkChangeListener;

    /**
     * Contains a reference to a hash set of attendees.
     */
    protected HashSet<ExpenseReportAttendee> checkedAttendees;

    /**
     * Constructs an instance of <code>AttendeeListItem</code> to be rendered in a list.
     * 
     * @param attendee
     *            the instance of <code>ExpenseReportAttendee</code>.
     * @param checkedAttendees
     *            a reference to a set containing currently checked attendees.
     * @param checkChangeListener
     *            a reference to listener to handle check events.
     * @param listItemViewType
     *            the list item type.
     */
    public AttendeeListItem(ExpenseReportAttendee attendee, HashSet<ExpenseReportAttendee> checkedAttendees,
            OnCheckedChangeListener checkChangeListener, int listItemViewType) {
        this.attendee = attendee;
        this.checkedAttendees = checkedAttendees;
        this.checkChangeListener = checkChangeListener;
        this.listItemViewType = listItemViewType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View attendeeView = null;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (listItemViewType) {
            case ADVANCED_SEARCH_ATTENDEE_LIST_ITEM: {
                attendeeView = inflater.inflate(R.layout.attendee_advanced_search_result_row, null);
                break;
            }
            case FAVORITE_SEARCH_ATTENDEE_LIST_ITEM: {
                attendeeView = inflater.inflate(R.layout.attendee_favorite_search_result_row, null);
                break;
            }
            default: {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: invalid list item type!");
                break;
            }
            }
        } else {
            attendeeView = convertView;
        }
        if (attendeeView != null) {

            // Set the Attendee name information.
            TextView txtView = (TextView) attendeeView.findViewById(R.id.attendee_name);
            if (txtView != null) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(attendee.lastName);
                if (attendee.firstName != null && attendee.firstName.length() > 0) {
                    strBldr.append(", ");
                    strBldr.append(attendee.firstName);
                }
                txtView.setText(strBldr.toString());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: attendee_name view not found!");
            }
            // If the view type is for an advanced attendee search, then fill in the first 3 fields.
            if (listItemViewType == ADVANCED_SEARCH_ATTENDEE_LIST_ITEM) {

                // Set the attendee title.
                txtView = (TextView) attendeeView.findViewById(R.id.attendee_field_1);
                if (txtView != null) {
                    if (attendee.title != null) {
                        txtView.setText(attendee.title);
                        txtView.setVisibility(View.VISIBLE);
                    } else {
                        txtView.setVisibility(View.INVISIBLE);
                    }
                }
                // Set the attendee company.
                // Set the attendee title.
                txtView = (TextView) attendeeView.findViewById(R.id.attendee_field_2);
                if (txtView != null) {
                    if (attendee.company != null) {
                        txtView.setText(attendee.company);
                        txtView.setVisibility(View.VISIBLE);
                    } else {
                        txtView.setVisibility(View.INVISIBLE);
                    }
                }

                // Set the first 3 fields of information in the view.
                // List<ExpenseReportFormField> attFrmFlds = attendee.getFormFields();
                // if( attFrmFlds != null && attFrmFlds.size() > 0) {
                // // Set form field value 1.
                // ExpenseReportFormField expFrmFld = attFrmFlds.get(0);
                // txtView = (TextView) attendeeView.findViewById(R.id.attendee_field_1);
                // if( txtView != null ) {
                // txtView.setText(expFrmFld.getValue());
                // } else {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: attendee_field_1 view not found!");
                // }
                // // Set form field value 2.
                // expFrmFld = null;
                // if( attFrmFlds.size() > 1) {
                // expFrmFld = attFrmFlds.get(1);
                // }
                // txtView = (TextView) attendeeView.findViewById(R.id.attendee_field_2);
                // if( txtView != null ) {
                // if( expFrmFld != null ) {
                // txtView.setText(expFrmFld.getValue());
                // } else {
                // txtView.setVisibility(View.INVISIBLE);
                // }
                // } else {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: attendee_field_2 view not found!");
                // }
                // // Set form field value 3.
                // expFrmFld = null;
                // if( attFrmFlds.size() > 2) {
                // expFrmFld = attFrmFlds.get(2);
                // }
                // txtView = (TextView) attendeeView.findViewById(R.id.attendee_field_2);
                // if( txtView != null ) {
                // if( expFrmFld != null ) {
                // txtView.setText(expFrmFld.getValue());
                // } else {
                // txtView.setVisibility(View.INVISIBLE);
                // }
                // } else {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: attendee_field_2 view not found!");
                // }
                // } else {
                // // No form fields, so hide the additional text fields.
                // View view = attendeeView.findViewById(R.id.attendee_field_1);
                // if( view != null ) {
                // view.setVisibility(View.INVISIBLE);
                // }
                // view = attendeeView.findViewById(R.id.attendee_field_2);
                // if( view != null ) {
                // view.setVisibility(View.INVISIBLE);
                // }
                // view = attendeeView.findViewById(R.id.attendee_field_3);
                // if( view != null ) {
                // view.setVisibility(View.INVISIBLE);
                // }
                // }

                // Hook-up the checkbox.
                // Add a listener to check for at least one selected item.
                CheckBox ckBox = (CheckBox) attendeeView.findViewById(R.id.attendee_check);
                if (ckBox != null) {
                    // Set the tag on the checkbox to the attendee item.
                    ckBox.setTag(attendee);
                    // Set the state of the checkbox.
                    ckBox.setChecked(checkedAttendees.contains(attendee));
                    // Set the on check change listener.
                    ckBox.setOnCheckedChangeListener(checkChangeListener);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate checkbox!");
                }
            }
        }
        return attendeeView;
    }

    /**
     * Gets the instance of <code>ExpenseReportAttendee</code> referenced by this list item.
     * 
     * @return the instance of <code>ExpenseReportAttendee</code> referenced by this list item.
     */
    public ExpenseReportAttendee getAttendee() {
        return attendee;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
