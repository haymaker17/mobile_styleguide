package com.concur.mobile.core.expense.travelallowance.controller;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItineraryLocation;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.service.AbstractItineraryDeleteRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRowRequest;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.service.SaveItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.ItineraryUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This controller is the glue between the backend service layer and the travel allowance itinerary UI.
 * 
 * This controller is instantiated in {@code ConcurCore}. The instance can be referenced via getTaItineraryController().
 * 
 * The #refreshItineraries method start the backend service request task to refresh the itinerary list which is also managed by
 * this controller.
 * 
 * Consumers can register an {@code IServiceRequestListener} to this controller. As soon as the backend service request has done
 * his job all registered listener will be notified. The consumer can afterwards get the refreshed itinerary list via
 * #getItineraryList.
 *
 * The itinerary list UI needs an own UI model. The method #getCompactItineraryList transforms the data model into the UI model.
 * 
 * @author Patricius Komarnicki
 */
public class TravelAllowanceItineraryController extends BaseController {

    public static final String CONTROLLER_TAG = TravelAllowanceItineraryController.class.getName();

    private static final String CLASS_TAG = TravelAllowanceItineraryController.class.getSimpleName();

    private BaseAsyncResultReceiver receiver;

    //private List<IServiceRequestListener> listeners;

    private GetTAItinerariesRequest getItinerariesRequest;

    private Context context;

    private List<Itinerary> itineraryList;

    private Itinerary itineraryStage;

    private List<Message> messageCache;

    public TravelAllowanceItineraryController(Context context) {
       // this.listeners = new ArrayList<IServiceRequestListener>();
        this.context = context;
        this.messageCache = new ArrayList<Message>();
    }

    public void refreshItineraries(String expenseReportKey, boolean isManager) {

        this.itineraryList = new ArrayList<Itinerary>();

        if (getItinerariesRequest != null && getItinerariesRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return;
        }

        receiver = new BaseAsyncResultReceiver(new Handler());

        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                itineraryList = getItinerariesRequest.getItineraryList();
                notifyListener(ControllerAction.REFRESH, true, resultData);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestSuccess", "Request success"));
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(ControllerAction.REFRESH, false, resultData);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onRequestFail", "Request failed"));
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                // Not needed yet.
                return;
            }

            @Override
            public void cleanup() {
                // Not needed yet.
                return;
            }
        });

        getItinerariesRequest = new GetTAItinerariesRequest(context, receiver,
                expenseReportKey, isManager);

        getItinerariesRequest.execute();
    }

    public List<Itinerary> getItineraryList() {
        if (itineraryList == null) {
            return new ArrayList<Itinerary>();
        }
        return itineraryList;
    }

    public Itinerary getItineraryStage () {
        return this.itineraryStage;
    }

    public CompactItinerary getCompactItinerary(String compactItineraryId) {
        if (StringUtilities.isNullOrEmpty(compactItineraryId)) {
            return null;
        }
        for (CompactItinerary compItinerary : getCompactItineraryList()) {
            if (compactItineraryId.equals(compItinerary.getItineraryID())) {
                return compItinerary;
            }
        }
        return null;
    }

    public Itinerary getItinerary(String itineraryId) {
        if (StringUtilities.isNullOrEmpty(itineraryId)) {
            return null;
        }
        for (Itinerary itinerary : itineraryList) {
            if (itineraryId.equals(itinerary.getItineraryID())) {
                return itinerary;
            }
        }
        return null;
    }

    public List<CompactItinerary> getCompactItineraryList() {
        List<CompactItinerary> result = new ArrayList<CompactItinerary>();

        for (Itinerary itinerary : getItineraryList()) {
            CompactItinerary compactItinerary = new CompactItinerary();
            compactItinerary.setName(itinerary.getName());
            compactItinerary.setItineraryID(itinerary.getItineraryID());
            compactItinerary.setExpenseReportID(itinerary.getExpenseReportID());

            int position = 0;
            for (ItinerarySegment segment : itinerary.getSegmentList()) {

                if (position == 0) {
                    // Treat the very first segment separately because this will be always the open start segment
                    CompactItinerarySegment firstCompactSegment = new CompactItinerarySegment();
                    firstCompactSegment.setLocation(segment.getDepartureLocation());
                    firstCompactSegment.setDepartureDateTime(segment.getDepartureDateTime());
                    firstCompactSegment.setIsSegmentOpen(true);
                    compactItinerary.getSegmentList().add(firstCompactSegment);
                }

                // Get the next itinerary segment if there is one.
                ItinerarySegment nextSegment = null;
                if (position + 1 < itinerary.getSegmentList().size()) {
                    nextSegment = itinerary.getSegmentList().get(position + 1);
                }

                if (nextSegment != null) {
                    // Check the to location of the current segment and the from location of the next segment
                    if (segment.getArrivalLocation().equals(nextSegment.getDepartureLocation())) {
                        // Create a closed compact segment
                        CompactItinerarySegment compactSegment = new CompactItinerarySegment();
                        compactSegment.setLocation(segment.getArrivalLocation());
                        compactSegment.setDepartureDateTime(nextSegment.getDepartureDateTime());
                        compactSegment.setArrivalDateTime(segment.getArrivalDateTime());
                        compactSegment.setBorderCrossingDateTime(segment.getBorderCrossDateTime());
                        compactSegment.setIsSegmentOpen(false);
                        compactItinerary.getSegmentList().add(compactSegment);
                    } else {
                        // Create two open compact segments
                        CompactItinerarySegment compactSegmentA = new CompactItinerarySegment();
                        compactSegmentA.setLocation(segment.getArrivalLocation());
                        compactSegmentA.setArrivalDateTime(segment.getArrivalDateTime());
                        compactSegmentA.setIsSegmentOpen(true);
                        compactItinerary.getSegmentList().add(compactSegmentA);

                        CompactItinerarySegment compactSegmentB = new CompactItinerarySegment();
                        compactSegmentB.setLocation(nextSegment.getDepartureLocation());
                        compactSegmentB.setDepartureDateTime(nextSegment.getDepartureDateTime());
                        compactSegmentB.setBorderCrossingDateTime(nextSegment.getBorderCrossDateTime());
                        compactSegmentB.setIsSegmentOpen(true);
                        compactItinerary.getSegmentList().add(compactSegmentB);
                    }
                }

                if (nextSegment == null) {
                    // Last segment
                    CompactItinerarySegment lastCompactSegment = new CompactItinerarySegment();
                    lastCompactSegment.setArrivalDateTime(segment.getArrivalDateTime());
                    lastCompactSegment.setLocation(segment.getArrivalLocation());
                    lastCompactSegment.setIsSegmentOpen(true);
                    compactItinerary.getSegmentList().add(lastCompactSegment);
                }

                position++;
            }

            result.add(compactItinerary);
        }

        return result;
    }

    public void executeUpdate(Itinerary itinerary) {
        if (itinerary == null) {
            return;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                Itinerary resultItinerary = (Itinerary) resultData.getSerializable(BundleId.ITINERARY);
                boolean isSuccess = handleAfterUpdateResponse(resultItinerary);
                notifyListener(ControllerAction.UPDATE, isSuccess, resultData);

            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Message msg = new Message(Message.Severity.ERROR, "500", "Backend Dump!");
                messageCache.add(msg);
                notifyListener(ControllerAction.UPDATE, false, resultData);
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        SaveItineraryRequest request = new SaveItineraryRequest(context, receiver, itinerary);
        request.execute();

    }

    public synchronized void setItineraryStage(Itinerary itinerary) {
        this.itineraryStage = itinerary;
    }

    /**
     * Checks the date denoted by datePosition within the itinerary segment located at the given
     * position within the given itinerary w.r.t. date overlaps.
     * The method rushes through the given order of the list elements and expects the dates in
     * ascending order.
     * @param itinerary the itinerary containing the segment
     * @param segmentPosition the position of the segment within the itinerary
     * @param datePosition -1 = departure, 0 = border crossing, 1 = arrival
     * @return -1 = Overlap with predecessor, 0 = no Overlap, 1 = Overlap with successor
     */
    public int checkOverlapping(Itinerary itinerary, int segmentPosition, int datePosition) {
        if (itinerary == null || itinerary.getSegmentList() == null || itinerary.getSegmentList().size() == 0) {
            return 0;
        }
        if (datePosition < - 1 || datePosition > 1) {
            return 0;
        }
        if (segmentPosition < 0 || segmentPosition + 1 > itinerary.getSegmentList().size()) {
            return 0;
        }
        ItinerarySegment segment = itinerary.getSegmentList().get(segmentPosition);
        Comparator<Date> comparator = DateUtils.getDateComparator(false);
        Date checkDate = null;
        Message msg = null;
        int result;
        if (datePosition == -1) {//Departure
            checkDate = segment.getStartDateUTC();
            result = comparator.compare(checkDate, segment.getEndDateUTC());
            if (result != -1) {
                resetMessages(segment);
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_START_BEFORE_END,
                        context.getString(R.string.ta_msg_start_end));
                msg.setSourceObject(segment);
                messageCache.add(msg);
                return result;
            }
        } else if (datePosition == 1) {//Arrival
            checkDate = segment.getEndDateUTC();
            result = comparator.compare(checkDate, segment.getStartDateUTC());
            if (result != 1) {
                resetMessages(segment);
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_START_BEFORE_END, context.getString(R.string.ta_msg_start_end));
                msg.setSourceObject(segment);
                messageCache.add(msg);
                return result;
            }
        } else {//TODO: BorderCrossing
            return 0;
        }
        ListIterator<ItinerarySegment> itUp = itinerary.getSegmentList().listIterator(segmentPosition);
        while (itUp.hasPrevious()) {
            ItinerarySegment cmpSegment = itUp.previous();
            result = comparator.compare(checkDate, cmpSegment.getEndDateUTC());
            if (result != 1) {
                resetMessages(segment);
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_PREDECESSOR,
                        context.getString(R.string.ta_overlap_preceding));
                msg.setSourceObject(segment);
                messageCache.add(msg);
                return result;
            }
            result = comparator.compare(checkDate, cmpSegment.getStartDateUTC());
            if (result != 1) {
                resetMessages(segment);
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_PREDECESSOR,
                        context.getString(R.string.ta_overlap_preceding));
                msg.setSourceObject(segment);
                messageCache.add(msg);
                return result;
            }
            resetMessages(cmpSegment, Message.MSG_UI_OVERLAPPING_SUCCESSOR);
        }
        ListIterator<ItinerarySegment> itDown = itinerary.getSegmentList().listIterator(segmentPosition + 1);
        while (itDown.hasNext()) {
            ItinerarySegment cmpSegment = itDown.next();
            result = comparator.compare(checkDate, cmpSegment.getStartDateUTC());
            if (result != -1) {
                resetMessages(segment);
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_SUCCESSOR,
                        context.getString(R.string.ta_overlap_following));
                msg.setSourceObject(segment);
                messageCache.add(msg);
                return result;
            }
            result = comparator.compare(checkDate, cmpSegment.getEndDateUTC());
            if (result != -1) {
                resetMessages(segment);
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_SUCCESSOR,
                        context.getString(R.string.ta_overlap_following));
                msg.setSourceObject(segment);
                messageCache.add(msg);
                return result;
            }
            resetMessages(cmpSegment, Message.MSG_UI_OVERLAPPING_PREDECESSOR);
        }
        return 0;
    }

    /**
     * Checks, whether all mandatory fields are filled
     * @param itinerary the itinerary to be checked
     * @return false, if not all mandatory fields are filled, otherwise true.
     */
    public boolean areAllMandatoryFieldsFilled(Itinerary itinerary) {
        if (itinerary == null) {
            return true;
        }
        resetMessages(itinerary);
        Message msg;
        if (StringUtilities.isNullOrEmpty(itinerary.getName())) {
            msg = new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES,
                    context.getString(R.string.general_fill_required_fields));
            msg.setSourceObject(itinerary);
            messageCache.add(msg);
            return false;
        }
        for (ItinerarySegment segment : itinerary.getSegmentList()) {
            if (segment.getArrivalDateTime() == null || segment.getDepartureDateTime() == null
                    || segment.getArrivalLocation() == null
                    || segment.getDepartureLocation() == null) {
                msg = new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES,
                        context.getString(R.string.general_fill_required_fields));
                msg.setSourceObject(itinerary);
                messageCache.add(msg);
                return false;
            }
        }
        return true;
    }

    public void executeDeleteItinerary(final Itinerary itinerary) {
        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                if (isSuccess) {
                    itineraryList.remove(itinerary);
                    notifyListener(ControllerAction.DELETE, true, null);
                } else {
                    Message msg = (Message) resultData
                            .getSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE);
                    if (msg != null) {
                        resultData.putSerializable(BundleId.ITINERARY, itinerary);
                    }
                    notifyListener(ControllerAction.DELETE, false, resultData);
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Itinerary itin = getItinerary(itinerary.getItineraryID());
                if (itin != null) {
                    resultData.putSerializable(BundleId.ITINERARY, itinerary);
                }
                notifyListener(ControllerAction.DELETE, false, resultData);
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        DeleteItineraryRequest deleteRequest = new DeleteItineraryRequest(context, receiver, itinerary.getItineraryID());
        deleteRequest.execute();
    }

    public void executeDeleteSegment(final String itineraryId, final ItinerarySegment segment) {
        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                if (isSuccess) {
                    getItinerary(itineraryId).getSegmentList().remove(segment);
                    resultData.putSerializable(BundleId.SEGMENT, segment);
                    notifyListener(ControllerAction.DELETE, true, resultData);
                } else {
                    Message msg = (Message) resultData
                            .getSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE);
                    msg.setSourceObject(segment);
                    messageCache.add(msg);
                    resultData.putSerializable(BundleId.SEGMENT, segment);
                    notifyListener(ControllerAction.DELETE, false, resultData);
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
//                Message msg = new Message(Message.Severity.ERROR, "@ Delete Failed @");
//                msg.setSourceObject(segment);
//                resultData.putSerializable(BundleId.SEGMENT, segment);
//                resultData.putSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE, msg);
                notifyListener(ControllerAction.DELETE, false, resultData);
            }

            @Override
            public void onRequestCancel(Bundle resultData) {

            }

            @Override
            public void cleanup() {

            }
        });

        DeleteItineraryRowRequest deleteRequest = new DeleteItineraryRowRequest(context, receiver, itineraryId, segment.getId());
        deleteRequest.execute();
    }

    public List<Message> getMessages() {
        if (messageCache == null) {
            messageCache = new ArrayList<Message>();
        }
        return messageCache;
    }

    public List<Message> getMessages(final Object sourceObject) {
        List<Message> resultList = new ArrayList<Message>();
        if (sourceObject == null || messageCache == null) {
            return resultList;
        }
        for (Message msg : messageCache ) {
            if (msg.getSourceObject() == sourceObject) {
                resultList.add(msg);
            }
        }
        return resultList;
    }

    public void resetMessages() {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                "Clear messageCache completely, actual size = " + messageCache.size()));
        messageCache = new ArrayList<Message>();
    }

    public void resetMessages(Itinerary itinerary) {
        if (itinerary == null || messageCache == null || messageCache.size() == 0) {
            return;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                itinerary.toString()) + ", actual messageCache size = " + messageCache.size());
        Iterator<Message> it = messageCache.iterator();
        Itinerary msgItinerary;
        while (it.hasNext()) {
            Message msg = it.next();
            if (msg.getSourceObject() != null && msg.getSourceObject() instanceof Itinerary) {
                msgItinerary = (Itinerary) msg.getSourceObject();
                if ((msgItinerary == itinerary)
                        || (msgItinerary.getItineraryID() != null
                            && msgItinerary.getItineraryID().equals(itinerary.getItineraryID()))) {
                    it.remove();
                }
            }
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                "new messageCache size = " + messageCache.size()));
    }

    public void resetBackendMessages (ItinerarySegment segment) {
        if (segment == null || messageCache == null || messageCache.size() == 0) {
            return;
        }
        Iterator<Message> it = messageCache.iterator();
        ItinerarySegment msgSegment;
        while (it.hasNext()) {
            Message msg = it.next();
            if (msg.getCode() != null && !msg.getCode().startsWith("UI.")) {
                if (msg.getSourceObject() != null && msg.getSourceObject() instanceof ItinerarySegment) {
                    msgSegment = (ItinerarySegment) msg.getSourceObject();
                    if ((msgSegment == segment)
                            || msgSegment.getId() != null && msgSegment.getId().equals(segment.getId())) {
                        it.remove();
                    } else {
                        Date departure = segment.getDepartureDateTime();
                        Date arrival = segment.getArrivalDateTime();
                        int departureCompare = DateUtils.getDateComparator(false).compare(departure, msgSegment.getDepartureDateTime());
                        int arrivalCompare = DateUtils.getDateComparator(false).compare(arrival, msgSegment.getArrivalDateTime());
                        if (departureCompare == 0 && arrivalCompare == 0) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    public void resetMessages(ItinerarySegment segment) {
        if (segment == null || messageCache == null || messageCache.size() == 0) {
            return;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                segment.toString()) + ", actual messageCache size = " + messageCache.size());
        Iterator<Message> it = messageCache.iterator();
        ItinerarySegment msgSegment;
        while (it.hasNext()) {
            Message msg = it.next();
            if (msg.getSourceObject() != null && msg.getSourceObject() instanceof ItinerarySegment) {
                msgSegment = (ItinerarySegment) msg.getSourceObject();
                if ((msgSegment == segment)
                        || msgSegment.getId() != null && msgSegment.getId().equals(segment.getId())) {
                    it.remove();
                }
            }
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                "new messageCache size = " + messageCache.size()));
    }

    public void resetMessages(ItinerarySegment segment, String code) {
        if (segment == null || messageCache == null || messageCache.size() == 0 || StringUtilities.isNullOrEmpty(code)) {
            return;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                segment.toString()) + ", code = " + code + ", actual messageCache size = " + messageCache.size());
        Iterator<Message> it = messageCache.iterator();
        ItinerarySegment msgSegment;
        while (it.hasNext()) {
            Message msg = it.next();
            if (code.equals(msg.getCode())) {
                if (msg.getSourceObject() != null && msg.getSourceObject() instanceof ItinerarySegment) {
                    msgSegment = (ItinerarySegment) msg.getSourceObject();
                    if ((msgSegment == segment)
                            || msgSegment.getId() != null && msgSegment.getId().equals(segment.getId())) {
                        it.remove();
                    }
                }
            }
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                "new messageCache size = " + messageCache.size()));
    }

    private boolean handleAfterUpdateResponse(Itinerary resultItin) {
        boolean isSuccess = true;
            String itinId = resultItin.getItineraryID();
            if (itinId != null && getItinerary(itinId) == null) {
                // Create entire itinerary case
                return handleItineraryCreate(resultItin);
            } else if (itinId != null && getItinerary(itinId) != null) {
                // Create segment or Update segment case
                for (ItinerarySegment segment : resultItin.getSegmentList()) {
                    if (getItinerary(itinId).getSegment(segment.getId()) == null) {
                        // Create segment case
                        isSuccess = handleSegmentCreate(itinId, segment);
                    } else if (getItinerary(itinId).getSegment(segment.getId()) != null) {
                        // Update segment case
                       isSuccess = handleSegmentUpdate(itinId, segment);
                    }
                }

                getItinerary(itinId).setName(resultItin.getName());
            }

        return isSuccess;

    }

    /**
     * Check the itinerary. If there is a row which failed during creation don't add the itinerary
     * to the list and delete it from backend.
     * 
     * @param createdItinerary
     *
     */
    private boolean handleItineraryCreate(Itinerary createdItinerary) {
        boolean isSuccess = true;
        for(ItinerarySegment segment : createdItinerary.getSegmentList()) {
            Message msg = segment.getMessage();
            if (msg != null) {
                msg.setSourceObject(segment);
                messageCache.add(msg);

                if (msg.getSeverity() == Message.Severity.ERROR) {
                    isSuccess = false;
                }
            }
        }

        if (!isSuccess) {
            executeDeleteItinerary(createdItinerary);
        } else {
            itineraryList.add(createdItinerary);
        }

        return isSuccess;
    }

    private boolean handleSegmentCreate(String itinId, ItinerarySegment segment) {
        boolean isSuccess = true;
        Itinerary itin = getItinerary(itinId);

        Message msg = segment.getMessage();
        if (msg != null) {
            msg.setSourceObject(segment);
            messageCache.add(msg);

            if (msg.getSeverity() == Message.Severity.ERROR) {
                isSuccess = false;
            }
        }

        if (isSuccess) {
            itin.getSegmentList().add(segment);
        }

        return isSuccess;
    }

//    private boolean handleSegmentCreate(String itinId, ItinerarySegment segment) {
//        boolean isSuccess = true;
//        Itinerary itin = getItinerary(itinId);
//
//        Message msg = segment.getMessage();
//        if (msg != null && msg.getSeverity() == Message.Severity.ERROR) {
//            ItinerarySegment stageSegment = getSegmentWithSameHandle(itineraryStage, segment);
//            msg.setSourceObject(stageSegment);
//            messageCache.add(msg);
//            isSuccess = false;
//        }
//
//        if (isSuccess) {
//            itin.getSegmentList().add(segment);
//        }
//
//        return isSuccess;
//    }

//    private ItinerarySegment getSegmentWithSameHandle(Itinerary itinerary, ItinerarySegment segment) {
//        if (itinerary == null || itinerary.getSegmentList() == null || segment == null ) {
//            return null;
//        }
//        Date departure = segment.getDepartureDateTime();
//        Date arrival = segment.getArrivalDateTime();
//        for (ItinerarySegment cmpSegment : itinerary.getSegmentList()) {
//            int departureCompare = DateUtils.getDateComparator(false).compare(departure, cmpSegment.getDepartureDateTime());
//            int arrivalCompare = DateUtils.getDateComparator(false).compare(arrival, cmpSegment.getArrivalDateTime());
//            if (departureCompare == 0 && arrivalCompare == 0) {
//                return cmpSegment;
//            }
//        }
//        return null;
//    }

    private boolean handleSegmentUpdate(String itinId, ItinerarySegment segment) {
        boolean isSuccess = true;
        Itinerary itin = getItinerary(itinId);
        ItinerarySegment itinSegment = itin.getSegment(segment.getId());
        Message msg = segment.getMessage();
        if (msg != null) {
            msg.setSourceObject(segment);
            itinSegment.setMessage(msg);
            messageCache.add(msg);
            if (msg.getSeverity() == Message.Severity.ERROR) {
                isSuccess = false;
            }
        }

        if (isSuccess) {
            int position = itin.getSegmentList().indexOf(itinSegment);
            itin.getSegmentList().remove(position);
            itin.getSegmentList().add(position, segment);
        }

        return isSuccess;
    }

}
