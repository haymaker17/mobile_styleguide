package com.concur.mobile.core.request.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.request.util.DateUtil;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.request.dto.RequestDTO;

/**
 * @author olivierb Split a list of request by status (active/approved) and
 *         manage display
 */
public class SplitRequestListAdapter extends AbstractGenericAdapter<RequestDTO> {

    private static final String CLS_TAG = SplitRequestListAdapter.class.getSimpleName();

    public final static int TYPE_HEADER = 0;
    public final static int TYPE_ITEM = 1;

    private SparseArray<String> sectionHeaders = new SparseArray<String>();

    private LayoutInflater mInflater;

    public SplitRequestListAdapter(Context context, List<RequestDTO> itemList) {
        super(context, null);

        mInflater = LayoutInflater.from(context);
        updateList(itemList);
    }

    @Override
    public int getItemViewType(int position) {
        return (sectionHeaders.get(position) != null) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void updateList(List<RequestDTO> objList) {
        clearListItems();
        final List<RequestDTO> approvedRequests = objList;
        final List<RequestDTO> activeRequests = new ArrayList<RequestDTO>();
        if (approvedRequests != null) {
            splitRequestsByStatus(approvedRequests.iterator(), activeRequests);

            // adding headers & building final list
            sectionHeaders.put(0, getContext().getResources().getString(R.string.tr_list_header_active));
            sectionHeaders.put(activeRequests.size() + 1,
                    getContext().getResources().getString(R.string.tr_list_header_approved));

            if (activeRequests.size() > 0) {
                getList().add(null);
                getList().addAll(activeRequests);
            }
            if (approvedRequests.size() > 0) {
                getList().add(null);
                getList().addAll(approvedRequests);
            }

            activeRequests.clear();
            approvedRequests.clear();
        } else {
            // put empty header here if needed
        }

        notifyDataSetChanged();
    }

    private void splitRequestsByStatus(Iterator<RequestDTO> approvedIterator,
            List<RequestDTO> activeRequests) {
        // splitting values
        while (approvedIterator.hasNext()) {
            final RequestDTO trDto = approvedIterator.next();
            if (!trDto.getApprovalStatusCode().equalsIgnoreCase("Q_PEBK")
                    && !trDto.getApprovalStatusCode().equalsIgnoreCase("Q_APPR")) {
                try {
                    approvedIterator.remove();
                    activeRequests.add(trDto);
                } catch (UnsupportedOperationException unsupOpExc) {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".<init>: unsupported operation exception while removing travel request '"
                                    + trDto.getName() + "' from list.", unsupOpExc);
                } catch (IllegalStateException illStateExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: illegal state exception while removing expense report '"
                            + trDto.getName() + "' from list.", illStateExc);
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup row = (ViewGroup) convertView;
        final boolean isHeader = getItemViewType(position) == TYPE_HEADER;

        if (row == null) {
            if (isHeader)
                row = (LinearLayout) mInflater.inflate(R.layout.request_list_section_header, null);
            else
                row = (RelativeLayout) mInflater.inflate(R.layout.request_row, null);
        }

        if (isHeader) {
            final TextView text = (TextView) row.findViewById(R.id.list_section_header);
            text.setText(sectionHeaders.get(position));
        } else {
            final RequestDTO request = (RequestDTO) getItem(position);

            final TextView id = (TextView) row.findViewById(R.id.requestRowId);
            final TextView name = (TextView) row.findViewById(R.id.requestRowName);
            final TextView segmentTypes = (TextView) row.findViewById(R.id.requestSegmentTypes);
            final TextView amount = (TextView) row.findViewById(R.id.requestRowAmount);
            final TextView status = (TextView) row.findViewById(R.id.requestStatus);
            final TextView startDate = (TextView) row.findViewById(R.id.requestStartDate);

            id.setText(request.getId());
            name.setText(request.getName());
            segmentTypes.setText(request.getSegmentListString());

            final String formattedAmount = FormatUtil.formatAmount((request.getTotal() != null ? request.getTotal() : 0d), getContext().getResources()
                    .getConfiguration().locale, request.getCurrencyCode(), true, true);
            amount.setText(formattedAmount);
            final Locale loc = getContext().getResources().getConfiguration().locale;
            startDate.setText(DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.SHORT, (loc != null) ? loc
                    : Locale.US, request.getStartDate()));
            status.setText(request.getApprovalStatus());

            name.setTypeface(Typeface.DEFAULT_BOLD);
            amount.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return row;
    }
}
