package com.concur.mobile.core.expense.travelallowance.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is the list adapter for the travel allowance itinerary list. The itinerary list is not created directly out from the
 * travel allowance data model coming from the backend. Before the itinerary list can be build the data model needs to be
 * transformed into a ui data model. This is done in the {@code TravelAllowanceItineraryController}. The resulting ui model is the
 * {@code CompactItinerary}.
 * 
 * The adapter provides two different types of list items which have different row layouts.
 * {@see LAYOUT_ID_HEADER}
 * {@see LAYOUT_ID_SEGMENT}
 * 
 * The #HEADER_ROW represents the itinerary itself and the row shows only the itinerary name. The row layout is reused from the
 * row layout of the fixed travel allowance list. Not needed view are set to GONE.
 * {@see #getViewHeader}
 * 
 * The #ENTRY_ROW represents the itinerary segments which belongs to a specific itinerary. The corresponding row is more complex
 * and shows information like location, dates, and icons.
 * {@see getViewEntry}
 * 
 * @author Patricius Komarnicki
 */
public class TravelAllowanceItineraryListAdapter extends ArrayAdapter<Object> {

    /**
     * The view holder for the header row which represents the itinerary.
     */
	private static class ViewHolderHeaderRow {
		public TextView itineraryName;
	}

	/**
	 * The view holder for the entry row which represents an itinerary segment.
	 */
	private static class ViewHolderEntryRow {
		public View headerDivider;
		public TextView location;
		public TextView fromDateTime;
		public TextView toDateTime;
		public View borderCrossingLabel;
		public TextView borderCrossingDateTime;
		public View segmentDivider;
		public View itineraryDivider;
		public View dateDivider;
		public ImageView rowIcon;
		public ImageView rowIconLocation;
	}

	/**
	 * The layout ID for the entry row (itinerary segment).
	 */
	private static final int LAYOUT_ID_SEGMENT = R.layout.travel_allowance_itinerary_segment_row;

	/**
	 * The layout ID for the header row (itinerary).
	 */
	private static final int LAYOUT_ID_HEADER = R.layout.generic_table_row_layout;

	/**
	 * Item view type ID for the header row.
	 * {@see getItemViewType}
	 */
	private static final int HEADER_ROW = 0;

	/**
	 * Item view type ID for the entry row.
	 * {@see getItemViewType}
	 */
	private static final int ENTRY_ROW = 1;

	private Context ctx;


    /**
     * Creates a list adapter for the travel allowance itinerary list.
     * 
     * @param ctx
     *            The application context.
     * @param itineraryList
     *            The list of {@code CompactItinerary}s which results from a transformation of the data model. {@see
     *            TravelAllowanceItineraryController}
     */
	public TravelAllowanceItineraryListAdapter(Context ctx, List<CompactItinerary> itineraryList) {
		super(ctx, 0);
		this.ctx = ctx;
		addAll(createFlatList(itineraryList));
	}

    /**
     * Each itinerary has a list of segments. In order to map this to the {@code ListView} the mapping list needs to be flat. So
     * the flat list will have two types of items. The header and the entry.
     *
     * {@see getItemViewType}
     * 
     * @param itineraryList
     *            The itinerary list which will be flattened.
     * @return The flat list
     */
	private List<Object> createFlatList(List<CompactItinerary> itineraryList) {
		List<Object> newList = new ArrayList<Object>();

		for(CompactItinerary itin : itineraryList) {
			newList.add(itin);
			for (CompactItinerarySegment segment : itin.getSegmentList()) {
				newList.add(segment);
			}
		}

		return newList;
	}

    /**
	 * Map the header fields to the corresponding views.
	 *
	 * @param position
	 *            The list position
	 * @param convertView
	 *            The convert view needed for reuse
	 * @return The header view for the specific header on the passed position
     */
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
			view.setTag(holder);

            // Make not needed views GONE
            // Needed because the layout is a reuse and these fields are useless for this case.
            view.findViewById(R.id.tv_subtitle_1).setVisibility(View.GONE);
            view.findViewById(R.id.tv_subtitle_2).setVisibility(View.GONE);
            view.findViewById(R.id.tv_value).setVisibility(View.GONE);
			view.findViewById(R.id.v_divider_bottom).setVisibility(View.GONE);
		}

		holder = (ViewHolderHeaderRow) view.getTag();

		holder.itineraryName.setText(itin.getName());
		holder.itineraryName.setTextAppearance(ctx, R.style.DefaultTitle_Big);

		return view;
	}

	/**
	 * Map the segment fields to the corresponding views.
	 *
	 * @param position
	 *            The list position
	 * @param convertView
	 *            The convert view needed for reuse
	 * @return The entry view for the specific segment on the passed position
	 */
    private View getViewEntry(int position, View convertView) {
        CompactItinerarySegment segment = (CompactItinerarySegment) getItem(position);
        View view = convertView;
        ViewHolderEntryRow holder;
        if (view == null || view.getTag() == null || !(view.getTag() instanceof ViewHolderEntryRow)) {
            final LayoutInflater inflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(LAYOUT_ID_SEGMENT, null);
            holder = new ViewHolderEntryRow();
			holder.headerDivider = view.findViewById(R.id.header_separator_line);
            holder.location = (TextView) view.findViewById(R.id.tv_location);
            holder.fromDateTime = (TextView) view.findViewById(R.id.tv_from_date_time);
            holder.toDateTime = (TextView) view.findViewById(R.id.tv_to_date_time);
			holder.borderCrossingLabel = view.findViewById(R.id.tv_border_crossing_label);
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

        if (segment.getArrivalDateTime() != null && segment.getDepartureDateTime() != null) {
			holder.fromDateTime.setText(formatDate(segment.getArrivalDateTime(), false));
			holder.fromDateTime.setVisibility(View.VISIBLE);
			holder.dateDivider.setVisibility(View.VISIBLE);
			holder.toDateTime.setText(formatDate(segment.getDepartureDateTime(), false));
			holder.toDateTime.setVisibility(View.VISIBLE);
			holder.rowIconLocation.setVisibility(View.VISIBLE);
			holder.rowIcon.setVisibility(View.GONE);
		} else {
			holder.dateDivider.setVisibility(View.GONE);
			holder.rowIconLocation.setVisibility(View.GONE);
			holder.rowIcon.setVisibility(View.VISIBLE);
			holder.fromDateTime.setVisibility(View.VISIBLE);
			holder.toDateTime.setVisibility(View.GONE);
			if (segment.getArrivalDateTime() != null) {
				holder.fromDateTime.setText(formatDate(segment.getArrivalDateTime(), true));
			} else {
				holder.fromDateTime.setText(formatDate(segment.getDepartureDateTime(), true));
			}
		}

        if (segment.getBorderCrossingDateTime() != null) {
			holder.borderCrossingLabel.setVisibility(View.VISIBLE);
            holder.borderCrossingDateTime.setVisibility(View.VISIBLE);
			holder.borderCrossingDateTime.setText(formatDate(segment.getBorderCrossingDateTime(), false));
        } else {
			holder.borderCrossingLabel.setVisibility(View.GONE);
            holder.borderCrossingDateTime.setVisibility(View.GONE);
        }

		// Separator line handling
        if (position + 1 == getCount()) {
            // In case of last element at all.
			holder.headerDivider.setVisibility(View.GONE);
            holder.segmentDivider.setVisibility(View.GONE);
            holder.itineraryDivider.setVisibility(View.GONE);
        }

        if (position + 1 < getCount() && getItem(position + 1) instanceof CompactItinerary) {
            // Case last element in an itinerary.
			holder.headerDivider.setVisibility(View.GONE);
            holder.segmentDivider.setVisibility(View.GONE);
            holder.itineraryDivider.setVisibility(View.VISIBLE);
        } else if (position + 1 < getCount()) {
            // Case not last element in an itinerary.
			holder.headerDivider.setVisibility(View.GONE);
            holder.segmentDivider.setVisibility(View.VISIBLE);
            holder.itineraryDivider.setVisibility(View.GONE);
			if (position - 1 > -1 && getItemViewType(position - 1) == HEADER_ROW) {
				// Case: First segment in the itinerary
				holder.headerDivider.setVisibility(View.VISIBLE);
			}
        }

        return view;
    }

	/**
	 * {@inheritDoc}
	 *
	 */
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

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public int getItemViewType(int position) {
		if (getItem(position) instanceof CompactItinerary) {
			return HEADER_ROW;
		} else {
			return ENTRY_ROW;
		}
	}


    private String formatDate(Date date, boolean withYear) {
		if (date == null) {
			return StringUtilities.EMPTY_STRING;
		}
        if (withYear) {
            return DateUtils.formatDateTime(ctx, date.getTime(), DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR| DateUtils.FORMAT_SHOW_TIME);
        } else {
            return DateUtils.formatDateTime(ctx, date.getTime(), DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR);
        }

    }
}
