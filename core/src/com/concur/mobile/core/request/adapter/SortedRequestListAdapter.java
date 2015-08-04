package com.concur.mobile.core.request.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.concur.core.R;
import com.concur.mobile.core.request.activity.RequestListActivity;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestExceptionDTO;
import com.concur.mobile.platform.request.util.DateUtil;
import com.concur.mobile.platform.ui.common.view.SwipeableRowView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @author olivierb Sort a list of request by dates
 */
public class SortedRequestListAdapter extends AbstractGenericAdapter<RequestDTO> {

    private static final String CLS_TAG = SortedRequestListAdapter.class.getSimpleName();

    private LayoutInflater mInflater;

    private Comparator<RequestDTO> requestSortByDate = new Comparator<RequestDTO>() {

        @Override public int compare(RequestDTO r1, RequestDTO r2) {
            if (r1.getStartDate() != null) {
                if (r2.getStartDate() != null) {
                    return r1.getStartDate().getTime() > r2.getStartDate().getTime() ?
                            -1 :
                            (r1.getStartDate().getTime() == r2.getStartDate().getTime() ? 0 : 1);
                    // --- r1 > r2
                    // --- r1 == r2
                    // --- r1 < r2
                } else {
                    return -1; // --- r1 not null, r2 null => r1 < r2
                }
            } else if (r2.getStartDate() != null) {
                return 1;// --- r1 null, r2 not null => r1 < r2
            } else {
                return 0;// --- both objects are null => r1 < r2
            }
        }
    };

    public SortedRequestListAdapter(Context context, List<RequestDTO> itemList) {
        super(context, null);

        mInflater = LayoutInflater.from(context);
        updateList(itemList);
    }

    @Override public void updateList(List<RequestDTO> objList) {
        clearListItems();
        if (objList != null) {
            Collections.sort(objList, requestSortByDate);
            getList().addAll(objList);
        }

        notifyDataSetChanged();
    }

    @SuppressLint("InflateParams") @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewGroup row = (ViewGroup) convertView;
        final RequestDTO request = getItem(position);

        if (row == null) {
            row = new SwipeableRowView(getContext(), (RelativeLayout) mInflater.inflate(R.layout.request_row, null),
                    (LinearLayout) mInflater.inflate(R.layout.request_row_buttons, null),
                    RequestListActivity.REQUEST_LIST_SWIPE_TO_LEFT);
        }

        final ImageView requestRowExceptionIcon = (ImageView) row.findViewById(R.id.requestRowExceptionIcon);

        final TextView id = (TextView) row.findViewById(R.id.requestRowId);
        final TextView name = (TextView) row.findViewById(R.id.requestRowName);
        final TextView amount = (TextView) row.findViewById(R.id.requestRowAmount);
        final TextView status = (TextView) row.findViewById(R.id.requestStatus);
        final TextView startDate = (TextView) row.findViewById(R.id.requestStartDate);

        id.setText(request.getId());
        name.setText(request.getName());

        final String formattedAmount = FormatUtil.formatAmount((request.getTotal() != null ? request.getTotal() : 0d),
                getContext().getResources().getConfiguration().locale, request.getCurrencyCode(), true, true);
        amount.setText(formattedAmount);
        final Locale loc = getContext().getResources().getConfiguration().locale;
        startDate.setText(DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.MVP, (loc != null) ? loc : Locale.US,
                request.getStartDate()));
        status.setText(request.getApprovalStatus());

        name.setTypeface(Typeface.DEFAULT_BOLD);
        amount.setTypeface(Typeface.DEFAULT_BOLD);

        requestRowExceptionIcon.setVisibility(View.GONE);
        if (request.getHighestExceptionLevel() == RequestExceptionDTO.ExceptionLevel.NON_BLOCKING) {
            requestRowExceptionIcon.setImageResource(R.drawable.icon_status_yellow);
            requestRowExceptionIcon.setVisibility(View.VISIBLE);
        } else if (request.getHighestExceptionLevel() == RequestExceptionDTO.ExceptionLevel.BLOCKING) {
            requestRowExceptionIcon.setImageResource(R.drawable.icon_status_red);
            requestRowExceptionIcon.setVisibility(View.VISIBLE);
        }

        return row;
    }
}
