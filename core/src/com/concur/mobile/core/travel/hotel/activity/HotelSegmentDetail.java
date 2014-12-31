package com.concur.mobile.core.travel.hotel.activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ProgressBar;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.travel.activity.AsyncImageAdapter;
import com.concur.mobile.core.travel.activity.ImageActivity;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.travel.data.Segment.SegmentType;
import com.concur.mobile.core.travel.hotel.data.HotelSegment;
import com.concur.mobile.core.travel.hotel.service.HotelImagesReply;
import com.concur.mobile.core.travel.service.CancelSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.platform.util.Format;

public class HotelSegmentDetail extends SegmentDetail {

    private final static String CLS_TAG = HotelSegmentDetail.class.getSimpleName();
    private static final String EXTRA_HOTEL_CANCEL_RECEIVER_KEY = "hotel.cancel.receiver";
    private static final String EXTRA_HOTEL_CANCEL_SEGMENT_KEY = "hotel.cancel.segment";
    HotelSegment seg;
    private BaseAsyncResultReceiver hotelCancelReceiver;
    protected final IntentFilter filter = new IntentFilter(Const.ACTION_HOTEL_IMAGES_RESULTS);
    private CancelSegment cancelSegment;

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {

        /**
         * Receive notification that the list of hotel images has been retrieved.
         */
        public void onReceive(Context context, Intent intent) {

            // Turn off the progress bar.
            ProgressBar imageProgress = (ProgressBar) findViewById(R.id.hotelImageProgress);
            if (imageProgress != null) {
                // If imageProgress is null then something is whacked so we're just ignoring the entire
                // response

                imageProgress.setVisibility(View.GONE);

                // Handle the message
                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    updateHotelImage(HotelImagesReply.getImagesFromBundle(intent
                                            .getBundleExtra(Const.EXTRA_TRAVEL_HOTEL_IMAGES)));
                                } else {
                                    String actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + actionStatusErrorMessage + ".");
                                }
                            } else {
                                String lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage
                                        + ".");
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
            }
        }
    };

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Notify the Application that this activity is recreated so attach this new activity reference to all Async Tasks that
        // are running (started) by this activity
        ConcurCore concurCoreApp = (ConcurCore) getApplication();
        concurCoreApp.attach(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.segment_hotel);

        if (!segmentInitDelayed) {
            buildView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
        if (retainer.contains(EXTRA_HOTEL_CANCEL_RECEIVER_KEY)) {
            hotelCancelReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_HOTEL_CANCEL_RECEIVER_KEY);
            // Reset the activity reference.
            HotelCancelSegmentListerner hotelCancelSegmentListerner = new HotelCancelSegmentListerner();
            hotelCancelReceiver.setListener(hotelCancelSegmentListerner);
            hotelCancelSegmentListerner.setFm(getSupportFragmentManager());
        }
        if (retainer.contains(EXTRA_HOTEL_CANCEL_SEGMENT_KEY)) {
            cancelSegment = (CancelSegment) retainer.get(EXTRA_HOTEL_CANCEL_SEGMENT_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        if (hotelCancelReceiver != null) {
            hotelCancelReceiver.setListener(null);
            retainer.put(EXTRA_HOTEL_CANCEL_RECEIVER_KEY, hotelCancelReceiver);
        }

        if (cancelSegment != null && cancelSegment.progressDialog != null) {
            if (cancelSegment.progressDialog.isShowing()) {
                cancelSegment.progressDialog.dismiss();
            }
            retainer.put(EXTRA_HOTEL_CANCEL_SEGMENT_KEY, cancelSegment);
        }
    }

    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();
        if (segmentInitDelayed) {
            buildView();
        }
    }

    protected void buildView() {
        seg = (HotelSegment) super.seg;

        if (seg == null) {
            // Something is wrong here. Get out of this activity and back to wherever to hopefully reload the trips.
            // MOB-10690
            finish();
            return;
        }

        // If we have no images yet, go get them
        if (seg.imagePairs == null) {
            if (seg.propertyImageCount > 0) {
                ProgressBar imageProgress = (ProgressBar) findViewById(R.id.hotelImageProgress);
                imageProgress.setVisibility(View.VISIBLE);

                ConcurCore app = (ConcurCore) getApplication();
                ConcurService service = app.getService();
                service.getHotelImages(seg.gdsId, seg.propertyId.trim());
            }
        } else {
            // Otherwise, just display them.
            updateHotelImage(seg.imagePairs);
        }

        setText(R.id.hotelVendor, seg.segmentName);

        StringBuilder sb = new StringBuilder(getText(R.string.segment_confirm)).append(' ').append(seg.confirmNumber);
        setText(R.id.hotelConfirm, sb.toString());

        sb.setLength(0);
        sb.append(getText(R.string.segment_hotel_checkin)).append(' ')
                .append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getStartDateLocal()));
        setText(R.id.hotelCheckin, sb.toString());

        sb.setLength(0);
        sb.append(getText(R.string.segment_hotel_checkout)).append(' ')
                .append(Format.safeFormatCalendar(FormatUtil.SHORT_DAY_TIME_DISPLAY, seg.getEndDateLocal()));
        setText(R.id.hotelCheckout, sb.toString());

        if (seg.phoneNumber != null && seg.phoneNumber.trim().length() > 0) {
            String formattedNumber = PhoneNumberUtils.formatNumber(PhoneNumberUtils.stripSeparators(seg.phoneNumber));
            populateField(R.id.hotelPhone, R.string.segment_phone, formattedNumber, Linkify.PHONE_NUMBERS);
        } else {
            hideField(R.id.hotelPhone);
        }

        sb.setLength(0);
        sb.append(seg.startAddress)
                .append(", ")
                .append(com.concur.mobile.base.util.Format.localizeText(this, R.string.general_address2, seg.startCity,
                        seg.startState, seg.startPostCode));
        populateField(R.id.hotelLocation, R.string.segment_location, sb.toString());
        linkMap(R.id.hotelLocation, sb.toString());

        // MOB-14659
        if (isBlank(seg.roomTypeLocalized)) {
            hideField(R.id.hotelRoomType);
            hideField(R.id.hotelRoomTypeSeparator);
        } else {
            populateField(R.id.hotelRoomType, R.string.segment_hotel_room, textOrNA(seg.roomTypeLocalized));
        }

        populateField(R.id.hotelStatus, R.string.segment_status, seg.statusLocalized);

        final Locale locale = getResources().getConfiguration().locale;
        CharSequence dailyRate;
        if (seg.dailyRate != null) {
            dailyRate = FormatUtil.formatAmount(seg.dailyRate, locale, seg.currency, true);
        } else {
            dailyRate = Const.NA;
        }
        populateField(R.id.hotelDailyRate, R.string.segment_hotel_rate_daily, dailyRate);

        CharSequence totalRate;
        if (seg.totalRate != null) {
            totalRate = FormatUtil.formatAmount(seg.totalRate, locale, seg.currency, true);
        } else {
            totalRate = Const.NA;
        }
        populateField(R.id.hotelTotalRate, R.string.segment_hotel_rate_total, totalRate);

        if (!isBlank(seg.cancellationPolicy)) {
            populateField(R.id.hotelCancel, R.string.segment_hotel_cancelpolicy, seg.cancellationPolicy, true);
        } else {
            hideField(R.id.hotelCancel);
        }

        // show travel points
        populateTravelPoints();

        if (!trip.allowCancel) {
            hideField(R.id.cancel);
        }
    }

    @Override
    protected String getHeaderTitle() {
        return getText(R.string.hotel_search_detail_title).toString();
    }

    protected void updateHotelImage(ArrayList<ImagePair> imagePairs) {

        if (imagePairs != null && imagePairs.size() > 0) {
            seg.imagePairs = imagePairs;

            // Set the image to the first one
            AsyncImageView aiv = (AsyncImageView) findViewById(R.id.hotelImage);
            aiv.setAsyncUri(URI.create(imagePairs.get(0).thumbnail));

            aiv.setOnClickListener(new OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.view.View.OnClickListener#onClick(android.view.View)
                 */
                public void onClick(View view) {
                    // Display the vendor image gallery dialog.
                    showDialog(Const.DIALOG_TRAVEL_HOTEL_VIEW_IMAGES);
                }

            });

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case Const.DIALOG_TRAVEL_HOTEL_VIEW_IMAGES: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.hotel_search_property_images));
            builder.setCancelable(true);
            LayoutInflater inflater = LayoutInflater.from(this);
            Gallery hotelImageGallery = (Gallery) inflater.inflate(R.layout.travel_hotel_room_gallery, null);
            hotelImageGallery.setAdapter(new AsyncImageAdapter(this, seg.imagePairs));
            hotelImageGallery.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(HotelSegmentDetail.this, ImageActivity.class);
                    intent.putExtra(Const.EXTRA_IMAGE_TITLE, getText(R.string.hotel_search_picture));
                    ImagePair imgPair = (ImagePair) parent.getAdapter().getItem(position);
                    intent.putExtra(Const.EXTRA_IMAGE_URL, imgPair.image);
                    startActivity(intent);
                }
            });
            builder.setView(hotelImageGallery);
            dialog = builder.create();
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelConfirmDialogMessage()
     */
    @Override
    protected String getSegmentCancelConfirmDialogMessage() {
        return com.concur.mobile.base.util.Format.localizeText(this, R.string.dlg_hotel_confirm_cancel_message,
                seg.startCity, Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, seg.getStartDayLocal()),
                Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, seg.getEndDayLocal()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelConfirmDialogTitle()
     */
    @Override
    protected String getSegmentCancelConfirmDialogTitle() {
        return getText(R.string.dlg_hotel_confirm_cancel_title).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.travel.SegmentDetail#getSegmentCancelFilter()
     */
    @Override
    protected IntentFilter getSegmentCancelFilter() {
        return new IntentFilter(Const.ACTION_HOTEL_CANCEL_RESULT);
    }

    protected void doSegmentCancelV2() {
        // Make the call

        hotelCancelReceiver = new BaseAsyncResultReceiver(new Handler());
        HotelCancelSegmentListerner hotelCancelSegmentListerner = new HotelCancelSegmentListerner();
        hotelCancelReceiver.setListener(hotelCancelSegmentListerner);
        hotelCancelSegmentListerner.setFm(getSupportFragmentManager());

        String bookingSource = seg.bookingSource;
        String reason = null;
        String recordLocator = seg.locator;
        String segmentKey = seg.segmentKey;
        String tripId = trip.cliqbookTripId;

        if (ConcurCore.isConnected()) {
            cancelSegment = new CancelSegment(HotelSegmentDetail.this, getApplicationContext(), 1, hotelCancelReceiver,
                    SegmentType.HOTEL, bookingSource, reason, recordLocator, segmentKey, tripId);
            // Make the call
            cancelSegment.execute();
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }

    }
}
