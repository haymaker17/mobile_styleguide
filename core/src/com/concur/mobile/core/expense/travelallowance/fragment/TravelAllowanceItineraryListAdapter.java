package com.concur.mobile.core.expense.travelallowance.fragment;

import java.util.ArrayList;
import java.util.List;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TravelAllowanceItineraryListAdapter extends ArrayAdapter<Object> {
	
	private static class ViewHolderHeaderRow {
		public TextView itineraryName;
		public View headerDivider;
		public View subtitle1;
		public View subtitle2;
		public View value;
	}

	private static class ViewHolderEntryRow {
		public TextView location;
		public TextView fromDateTime;
		public TextView toDateTime;
		public TextView borderCrossingDateTime;
		public View segmentDivider;
		public View itineraryDivider;
		public View dateDivider;
		public ImageView rowIcon;
		public ImageView rowIconLocation;
	}

	private static final int LAYOUT_ID_SEGMENT = R.layout.travel_allowance_itinerary_segment_row;
	private static final int LAYOUT_ID_HEADER = R.layout.generic_table_row_layout;

	private static final int HEADER_ROW = 0;
	private static final int ENTRY_ROW = 1;

	//private List<Stop> stopList = new ArrayList<Stop>();
	private Context ctx;
	//private IDateFormat dateTimeConverter;


	
	public TravelAllowanceItineraryListAdapter(Context ctx, List<CompactItinerary> stopList) {
		super(ctx, 0);
		this.ctx = ctx;
		addAll(createFlatList(stopList));
	}

	private List<Object> createFlatList(List<CompactItinerary> stopList) {
		List<Object> newList = new ArrayList<Object>();

		for(CompactItinerary itin : stopList) {
			newList.add(itin);
			for (CompactItinerarySegment segement : itin.getSegmentList()) {
				newList.add(segement);
			}
		}

		return newList;
	}


	private View getViewHeader(int position, View convertView) {
		CompactItinerary itin = (CompactItinerary) getItem(position);
		View view = convertView;
		ViewHolderHeaderRow holder;

		if (view == null || view.getTag() == null || !(view.getTag() instanceof ViewHolderHeaderRow)) {
			final LayoutInflater inflater =
					(LayoutInflater) this.ctx
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(LAYOUT_ID_HEADER, null);
			holder = new ViewHolderHeaderRow();
			holder.itineraryName = (TextView) view.findViewById(R.id.tv_title);
			holder.headerDivider = view.findViewById(R.id.v_divider_bottom);
			holder.subtitle1 = view.findViewById(R.id.tv_subtitle_1);
			holder.subtitle2 = view.findViewById(R.id.tv_subtitle_2);
			holder.value = view.findViewById(R.id.tv_value);
			view.setTag(holder);
		}

		holder = (ViewHolderHeaderRow) view.getTag();

		holder.itineraryName.setText(itin.getName());
		holder.itineraryName.setTextAppearance(ctx, R.style.DefaultTitle_Big);
		holder.headerDivider.setVisibility(View.VISIBLE);
		holder.subtitle1.setVisibility(View.GONE);
		holder.subtitle2.setVisibility(View.GONE);
		holder.value.setVisibility(View.GONE);

		return view;
	}

	private View getViewEntry(int position, View convertView) {
		CompactItinerarySegment segment = (CompactItinerarySegment) getItem(position);
		View view = convertView;
		ViewHolderEntryRow holder;
		if (view == null || view.getTag() == null || !(view.getTag() instanceof ViewHolderEntryRow)) {
			final LayoutInflater inflater =
					(LayoutInflater) this.ctx
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(LAYOUT_ID_SEGMENT, null);
			holder = new ViewHolderEntryRow();
			holder.location = (TextView) view.findViewById(R.id.tv_location);
			holder.fromDateTime = (TextView) view.findViewById(R.id.tv_from_date_time);
			holder.toDateTime = (TextView) view.findViewById(R.id.tv_to_date_time);
			holder.borderCrossingDateTime = (TextView) view.findViewById(R.id.tv_border_crossing_date_time);
			holder.segmentDivider = view.findViewById(R.id.segment_separator_line);
			holder.itineraryDivider = view.findViewById(R.id.itinerary_separator_line);
			holder.dateDivider = view.findViewById(R.id.date_separator_line);
			holder.rowIcon = (ImageView) view.findViewById(R.id.iv_row_icon);
			holder.rowIconLocation = (ImageView) view.findViewById(R.id.iv_row_icon_location);
			view.setTag(holder);
		}

		holder = (ViewHolderEntryRow) view.getTag();

		holder.location.setText(segment.getLocation().getName());
		holder.fromDateTime.setText(DateUtils.startEndDateToString(segment.getDepartureDateTime(), null, new DefaultDateFormat(this.ctx), true, false, false));
		if (segment.getArrivalDateTime() != null) {
			holder.toDateTime.setText(DateUtils.startEndDateToString(segment.getArrivalDateTime(), null, new DefaultDateFormat(this.ctx), true, false, false));
			holder.toDateTime.setVisibility(View.VISIBLE);
			holder.dateDivider.setVisibility(View.VISIBLE);
			holder.rowIconLocation.setVisibility(View.VISIBLE);
			holder.rowIcon.setVisibility(View.GONE);
		} else {
			holder.toDateTime.setVisibility(View.GONE);
			holder.dateDivider.setVisibility(View.GONE);
			holder.rowIconLocation.setVisibility(View.GONE);
			holder.rowIcon.setVisibility(View.VISIBLE);
		}

		holder.borderCrossingDateTime.setText(DateUtils.startEndDateToString(segment.getBorderCrossingDateTime(), null, new DefaultDateFormat(this.ctx), true, false, false));

		if(position + 1 == getCount()) {
			// In case of last element at all.
			holder.segmentDivider.setVisibility(View.GONE);
			holder.itineraryDivider.setVisibility(View.GONE);
		}
		if (position + 1 < getCount() && getItem(position+1) instanceof CompactItinerary) {
			// Case last element in an itinerary.
			holder.segmentDivider.setVisibility(View.GONE);
			holder.itineraryDivider.setVisibility(View.VISIBLE);
		} else if (position + 1 < getCount()) {
			// Case not last element in an itinerary.
			holder.segmentDivider.setVisibility(View.VISIBLE);
			holder.itineraryDivider.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (getItemViewType(position) == HEADER_ROW) {
			return getViewHeader(position, convertView);
		}
		if (getItemViewType(position) == ENTRY_ROW) {
			return getViewEntry(position, convertView);
		}

		return null;
	}

	@Override
	public int getItemViewType(int position) {
		if (getItem(position) instanceof CompactItinerary) {
			return HEADER_ROW;
		} else {
			return ENTRY_ROW;
		}
	}
}
