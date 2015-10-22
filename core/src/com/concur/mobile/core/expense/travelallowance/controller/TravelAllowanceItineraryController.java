package com.concur.mobile.core.expense.travelallowance.controller;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.datamodel.AssignableItinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.Itinerary;
import com.concur.mobile.core.expense.travelallowance.datamodel.ItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.service.AbstractItineraryDeleteRequest;
import com.concur.mobile.core.expense.travelallowance.service.AssignItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.service.DeleteItineraryRowRequest;
import com.concur.mobile.core.expense.travelallowance.service.GetAssignableItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.service.IRequestListener;
import com.concur.mobile.core.expense.travelallowance.service.SaveItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.service.UnassignItineraryRequest;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerary;
import com.concur.mobile.core.expense.travelallowance.ui.model.CompactItinerarySegment;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    private GetTAItinerariesRequest getItinerariesRequest;

    private Context context;

    private List<Itinerary> itineraryList;

    private Map<String, List<AssignableItinerary>> assignableItineraryList;

    private List<BaseAsyncResultReceiver> receiverList;

    public TravelAllowanceItineraryController(Context context) {
        this.context = context;
        this.receiverList = new ArrayList<BaseAsyncResultReceiver>();
        this.assignableItineraryList = new HashMap<String, List<AssignableItinerary>>();
    }

    public void refreshItineraries(String expenseReportKey, boolean isManager, final IRequestListener requestor) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshItineraries", "expenseReportKey = " + expenseReportKey + ", isManager = " + isManager));
        if (getItinerariesRequest != null && getItinerariesRequest.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return;
        }
        this.itineraryList = new ArrayList<Itinerary>();

        final BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiverList.add(receiver);

        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, requestor) {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                itineraryList = getItinerariesRequest.getItineraryList();
                notifyListener(ControllerAction.REFRESH, true, resultData);
                super.onRequestSuccess(resultData);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                notifyListener(ControllerAction.REFRESH, false, resultData);
                super.onRequestFail(resultData);
            }
        });

        getItinerariesRequest = new GetTAItinerariesRequest(context, receiver,
                expenseReportKey, isManager);

        getItinerariesRequest.execute();
    }

    public List<Itinerary> getItineraryList() {
        if (itineraryList == null) {
            itineraryList = new ArrayList<Itinerary>();
        }
        return itineraryList;
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

                if (nextSegment != null && segment.getArrivalLocation() != null) {
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

    /**
     * Executes the backend update for the given itinerary
     * @param updItinerary The itinerary to be changed or created
     * @return true, if the request has been sent
     */
    public boolean executeUpdate(final Itinerary updItinerary) {
        if (updItinerary == null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdate", "Itinerary is null! Refused."));
            return false;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdate", "Itinerary = " + updItinerary.toString()));
        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        this.receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, null) {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                Itinerary resultItinerary = (Itinerary) resultData.getSerializable(BundleId.ITINERARY);
                boolean isSuccess = handleAfterUpdateResponse(updItinerary, resultItinerary);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdateItinerary->onRequestSuccess",
                        "isSuccess = " + isSuccess + ", Resulting Itinerary = " + resultItinerary.toString()));
                notifyListener(ControllerAction.UPDATE, isSuccess, resultData);
                super.onRequestSuccess(resultData);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdateItinerary->onRequestFail", "Failed!"));
                notifyListener(ControllerAction.UPDATE, false, resultData);
                super.onRequestFail(resultData);
            }

        });

        SaveItineraryRequest request = new SaveItineraryRequest(context, receiver, updItinerary);
        request.execute();
        return true;
    }

    public boolean areDatesOverlapping(Itinerary itinerary, boolean setErrors) {
        if (itinerary == null || itinerary.getSegmentList() == null || itinerary.getSegmentList().size() == 0) {
            return false;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "areDatesOverlapping",
                itinerary.toString()));
        ItinerarySegment segment = null;
        ItinerarySegment successorSegment = null;
        Comparator<Date> comparator = DateUtils.getDateComparator(false);
        Message msg = null;
        int pos = 0;
        int result = 0;
        Iterator<ItinerarySegment> itItin = itinerary.getSegmentList().iterator();

        while (itItin.hasNext()) {
            segment = itItin.next();
            result = comparator.compare(segment.getStartDateUTC(), segment.getEndDateUTC());
            if (result > -1) {//equal or greater
                if (setErrors) {
                    msg = new Message(Message.Severity.ERROR, Message.MSG_UI_START_BEFORE_END,
                            context.getString(R.string.ta_msg_start_end),
                            ItinerarySegment.Field.DEPARTURE_DATE_TIME.getName(),
                            ItinerarySegment.Field.ARRIVAL_DATE_TIME.getName());
                    segment.setMessage(msg);
                }
                return true;
            }
            if (itItin.hasNext()) {
                successorSegment =  itinerary.getSegmentList().get(pos + 1);
                result = comparator.compare(segment.getEndDateUTC(), successorSegment.getStartDateUTC());
                if (result > -1) {//equal or greater
                    if (setErrors) {
                        msg = new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_SUCCESSOR,
                                context.getString(R.string.ta_overlap_following),
                                ItinerarySegment.Field.ARRIVAL_DATE_TIME.getName());
                        segment.setMessage(msg);
                        msg = new Message(Message.Severity.ERROR, Message.MSG_UI_OVERLAPPING_PREDECESSOR,
                                context.getString(R.string.ta_overlap_preceding),
                                ItinerarySegment.Field.DEPARTURE_DATE_TIME.getName());
                        successorSegment.setMessage(msg);
                    }
                    return true;
                }
            }
            pos++;
        }
        return false;
    }

    /**
     * Checks, whether all mandatory fields are filled and sets the error message object
     * accordingly.
     * @param itinerary the itinerary to be checked
     * @param setErrors if true, the errors are added to the itinerary
     * @return false, if not all mandatory fields are filled, otherwise true.
     */
    public boolean areAllMandatoryFieldsFilled(Itinerary itinerary, boolean setErrors) {
        if (itinerary == null) {
            return true;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "areAllMandatoryFieldsFilled",
                itinerary.toString()));
        boolean result = true;
        if (StringUtilities.isNullOrEmpty(itinerary.getName())) {
            if (setErrors) {
                Message msg = new Message(Message.Severity.ERROR,
                        Message.MSG_UI_MISSING_DATES, context.getString(R.string.general_fill_required_fields),
                        Itinerary.Field.NAME.getName());
                itinerary.setMessage(msg);
            }
            result = false;
        }
        for (ItinerarySegment segment : itinerary.getSegmentList()) {
            List<String> fields = new ArrayList<String>();
            if (segment.getArrivalDateTime() == null) {
                fields.add(ItinerarySegment.Field.ARRIVAL_DATE_TIME.getName());
            }
            if (segment.getDepartureDateTime() ==  null) {
                fields.add(ItinerarySegment.Field.DEPARTURE_DATE_TIME.getName());
            }
            if (segment.getArrivalLocation() == null) {
                fields.add(ItinerarySegment.Field.ARRIVAL_LOCATION.getName());
            }
            if (segment.getDepartureLocation() == null) {
                fields.add(ItinerarySegment.Field.DEPARTURE_LOCATION.getName());
            }
            if (fields.size() > 0) {
                if (setErrors) {
                    Message msg = new Message(Message.Severity.ERROR, Message.MSG_UI_MISSING_DATES,
                            context.getString(R.string.general_fill_required_fields), fields);
                    segment.setMessage(msg);
                }
                result = false;
            }
        }
        return result;
    }

    public boolean executeDeleteItinerary(final Itinerary itinerary, final IRequestListener listener) {
        if (itinerary == null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteItinerary", "Itinerary is null! Refused."));
            return false;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteItinerary", "Itinerary = " + itinerary.toString()));
        final BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiverList.add(receiver);

        receiver.setListener(new BaseAsyncRequestTask.AsyncReplyListener() {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                receiverList.remove(receiver);

                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteItinerary->onRequestSuccess", "isSuccess = " + isSuccess));
                if (isSuccess) {
                    itineraryList.remove(itinerary);
                    notifyListener(ControllerAction.DELETE, true, null);
                    if (listener != null) {
                        listener.onRequestSuccess(resultData);
                    }
                } else {
                    Message msg = (Message) resultData
                            .getSerializable(AbstractItineraryDeleteRequest.RESULT_BUNDLE_ID_MESSAGE);
                    if (msg != null) {
                        resultData.putSerializable(BundleId.ITINERARY, itinerary);
                    }
                    notifyListener(ControllerAction.DELETE, false, resultData);
                    if (listener != null) {
                        listener.onRequestFailed();
                    }
                }
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                receiverList.remove(receiver);
                if (listener != null) {
                    listener.onRequestFailed();
                }
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteItinerary->onRequestFail", "Failed!"));
                Itinerary itin = getItinerary(itinerary.getItineraryID());
                if (itin != null) {
                    resultData.putSerializable(BundleId.ITINERARY, itinerary);
                }
                notifyListener(ControllerAction.DELETE, false, resultData);
            }

            @Override
            public void onRequestCancel(Bundle resultData) {
                receiverList.remove(receiver);
            }

            @Override
            public void cleanup() {

            }
        });

        DeleteItineraryRequest deleteRequest = new DeleteItineraryRequest(context, receiver, itinerary.getItineraryID());
        deleteRequest.execute();
        return true;
    }

    public boolean executeDeleteSegment(final String itineraryId, final ItinerarySegment segment) {
        if (StringUtilities.isNullOrEmpty(itineraryId) || segment == null) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteSegment", "Refused."));
            return false;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG,
                "executeDeleteSegment", "Itinerary Id = " + itineraryId + ", Segment = " + segment.toString()));
        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        this.receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, null) {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(AbstractItineraryDeleteRequest.IS_SUCCESS, false);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteSegment->onRequestSuccess", "isSuccess = " + isSuccess));
                if (isSuccess) {
                    int pos = getSegmentPositionById(getItinerary(itineraryId), segment.getId());
                    if (pos > -1) {
                        getItinerary(itineraryId).getSegmentList().remove(pos);
                    }
                    resultData.putSerializable(BundleId.SEGMENT, segment);
                    notifyListener(ControllerAction.DELETE, true, resultData);
                } else {
                    resultData.putSerializable(BundleId.SEGMENT, segment);
                    notifyListener(ControllerAction.DELETE, false, resultData);
                }
                super.onRequestSuccess(resultData);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeDeleteSegment->onRequestFail", "Failed!"));
                notifyListener(ControllerAction.DELETE, false, resultData);
                super.onRequestFail(resultData);
            }
        });

        DeleteItineraryRowRequest deleteRequest = new DeleteItineraryRowRequest(context, receiver, itineraryId, segment.getId());
        deleteRequest.execute();
        return true;
    }

    /**
     * Checks, whether there are any errors associated with the given Itinerary
     * @param itinerary The itinerary to be checked
     * @return true, if there is at least one error, otherwise false.
     */
    public boolean hasErrors(Itinerary itinerary) {
        if (itinerary == null) {
            return false;
        }
        Message msg = itinerary.getMessage();
        if (msg != null && msg.getSeverity() == Message.Severity.ERROR) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "hasErrors", "Error on Itinerary level = " + msg.toString()));
            return true;
        }
        if (itinerary.getSegmentList() == null || itinerary.getSegmentList().size() == 0) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "hasErrors", "None. Itinerary = " + itinerary.toString()));
            return false;
        }
        for (ItinerarySegment segment : itinerary.getSegmentList()) {
            msg = segment.getMessage();
            if (msg != null && msg.getSeverity() == Message.Severity.ERROR) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "hasErrors", "Error on Segment level = " + msg.toString()));
                return true;
            }
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "hasErrors", "None. Itinerary = " + itinerary.toString()));
        return false;
    }

    /**
     * Clears all messages of the given Itinerary.
     * @param itinerary The itinerary for which the messages should be cleared
     */
    public void resetMessages(Itinerary itinerary) {
        if (itinerary == null) {
            return;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetMessages",
                "For " + itinerary.toString()));
        itinerary.setMessage(null);
        if (itinerary.getSegmentList() == null || itinerary.getSegmentList().size() == 0) {
            return;
        }
        for (ItinerarySegment segment : itinerary.getSegmentList()) {
            segment.setMessage(null);
        }
    }

    /**
     * Clears the segment message at the given position. In case the segment contains an UI message
     * referring to overlapping with predecessor segment or successor segment the method will clear
     * the corresponding mirror message from the predecessor respectively from the successor in
     * addition.
     * @param itinerary The itinerary holding the segments
     */
    public void resetSegmentMessage(Itinerary itinerary, int position) {
        if (itinerary == null || itinerary.getSegmentList() == null || itinerary.getSegmentList().size() == 0) {
            return;
        }
        if (position < 0 || position + 1 > itinerary.getSegmentList().size()
                || itinerary.getSegmentList().get(position) == null) {
            return;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "resetSegmentMessage",
                "For " + itinerary.getSegmentList().get(position).toString()));
        Message msg = itinerary.getSegmentList().get(position).getMessage();
        if (msg != null) {
            if (Message.MSG_UI_OVERLAPPING_PREDECESSOR.equals(msg.getCode()) && position > 0) {
                Message msgPredecessor = itinerary.getSegmentList().get(position - 1).getMessage();
                if (msgPredecessor != null && Message.MSG_UI_OVERLAPPING_SUCCESSOR.equals(msgPredecessor.getCode())) {
                    itinerary.getSegmentList().get(position - 1).setMessage(null);
                }
            }
            if (Message.MSG_UI_OVERLAPPING_SUCCESSOR.equals(msg.getCode())
                    && position + 1 < itinerary.getSegmentList().size()) {
                Message msgSuccessor = itinerary.getSegmentList().get(position + 1).getMessage();
                if (msgSuccessor != null && Message.MSG_UI_OVERLAPPING_PREDECESSOR.equals(msgSuccessor.getCode())) {
                    itinerary.getSegmentList().get(position + 1).setMessage(null);
                }
            }
            itinerary.getSegmentList().get(position).setMessage(null);
        }
    }

    private boolean handleAfterUpdateResponse(Itinerary updItin, Itinerary resultItin) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleAfterUpdateResponse",
                "Received Itinerary Id = " + resultItin.getItineraryID()));
        boolean isSuccess = true;
        String itinId = resultItin.getItineraryID();
        if (itinId != null && getItinerary(itinId) == null) {
            // Create entire itinerary case
            return handleItineraryCreate(updItin, resultItin);
        } else if (itinId != null && getItinerary(itinId) != null) {
            // Create segment or Update segment case
            for (ItinerarySegment segment : resultItin.getSegmentList()) {
                if (getItinerary(itinId).getSegment(segment.getId()) == null) {
                    isSuccess = isSuccess && handleSegmentCreate(updItin, segment);
                } else if (getItinerary(itinId).getSegment(segment.getId()) != null) {
                    // Update segment case
                    isSuccess = isSuccess && handleSegmentUpdate(updItin, segment);
                }
            }
            getItinerary(itinId).setName(resultItin.getName()); //Should not change in updItin
        }
        return isSuccess;
    }

    /**
     * Check the itinerary. If there is a row which failed during creation don't add the itinerary
     * to the list and delete it from backend.
     *
     * @param updItinerary The origin itinerary sent to the backend
     * @param createdItinerary The resulting itinerary created in the backend
     *
     */
    private boolean handleItineraryCreate(Itinerary updItinerary, Itinerary createdItinerary) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleItineraryCreate",
                "Recently created Itinerary Id = " + createdItinerary.getItineraryID()));
        boolean isSuccess = true;
        for(ItinerarySegment segment : createdItinerary.getSegmentList()) {
            Message msg = segment.getMessage();
            if (msg != null) {
                if (msg.getSeverity() == Message.Severity.ERROR) {
                    isSuccess = false;
                }
                ItinerarySegment updSegment = getSegmentWithSameHandle(updItinerary, segment);
                if (updSegment != null) {
                    updSegment.setMessage(msg);
                }
            }
        }
        if (isSuccess) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleItineraryCreate",
                    "Adding itinerary to DB buffer"));
            itineraryList.add(createdItinerary);
        } else {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleItineraryCreate",
                    "Triggering auto deletion of itinerary. Segments having errors"));
            executeDeleteItinerary(createdItinerary, null);
        }
        return isSuccess;
    }

    /**
     * An existing itinerary has been updated. This method handles the creation of a new segment
     * for the given itinerary.
     *
     * @param updItinerary The original itinerary the application tried to update in the backend
     * @param resultSegment The resulting segment passed from the backend
     * @return true, if success
     */
    private boolean handleSegmentCreate(Itinerary updItinerary, ItinerarySegment resultSegment) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentCreate",
                "Existing Itinerary Id = " + updItinerary.getItineraryID()));
        boolean isSuccess = true;
        Message msg = resultSegment.getMessage();
        ItinerarySegment updSegment = getSegmentWithSameHandle(updItinerary, resultSegment);
        if (msg != null) {
            if (msg.getSeverity() == Message.Severity.ERROR) {
                isSuccess = false;
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentCreate",
                        "Adding error message to segment of Itinerary to be updated " + updItinerary.toString()));
                if (updSegment != null) {
                    updSegment.setMessage(msg);
                }
            }
        }
        if (isSuccess) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentCreate",
                    "Adding new segment to DB Buffer " + resultSegment.toString()));
            Itinerary itin = getItinerary(updItinerary.getItineraryID());
            itin.getSegmentList().add(resultSegment); //Including positive messages
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentCreate",
                    "Exchange segment of Itinerary to be updated with new result " + resultSegment.toString()));
            if (updSegment != null) {
                int position = updItinerary.getSegmentList().indexOf(updSegment);
                if (position > -1) {
                    updItinerary.getSegmentList().remove(position);
                    updItinerary.getSegmentList().add(position, resultSegment);
                }
            }
        }
        return isSuccess;
    }

    private boolean handleSegmentUpdate(Itinerary updItinerary, ItinerarySegment resultSegment) {
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentUpdate",
                "Existing Itinerary Id = " + updItinerary.getItineraryID() + ", existing Segment Id = " + resultSegment.getId()));
        boolean isSuccess = true;
        Itinerary itin = getItinerary(updItinerary.getItineraryID());
        ItinerarySegment itinSegment = itin.getSegment(resultSegment.getId());
        ItinerarySegment updSegment = updItinerary.getSegment(resultSegment.getId());
        Message msg = resultSegment.getMessage();
        if (msg != null) {
            if (msg.getSeverity() == Message.Severity.ERROR) {
                isSuccess = false;
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentUpdate",
                        "Adding error message to segment of the itinerary to be updated " + updItinerary.toString()));
                if (updSegment != null) {
                    updSegment.setMessage(msg);
                }
            }
        }
        if (isSuccess) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "handleSegmentUpdate",
                    "Exchange segment of the itinerary to be updated as well as the segment of the buffered DB itinerary with new result " + resultSegment.toString()));
            int position = itin.getSegmentList().indexOf(itinSegment);
            if (position > -1) {
                itin.getSegmentList().remove(position);
                itin.getSegmentList().add(position, resultSegment); //Including positive messages
            }
            position = updItinerary.getSegmentList().indexOf(updSegment);
            if (position > -1) {
                updItinerary.getSegmentList().remove(position);
                updItinerary.getSegmentList().add(position, resultSegment);
            }
        }
        return isSuccess;
    }

    /**
     * Searches for a similar segment in the given itinerary. Similarity is given, if the
     * segment has the same virtual handle, by means departure date and arrival date are
     * equal or if the segments are of the same valid segment Id respectively have the same
     * object reference.
     * @param itinerary The itinerary where to search the segment for
     * @param segment The segment for which the sibling needs to be found
     * @return The segment found within the list of segments of the given itinerary
     */
    private ItinerarySegment getSegmentWithSameHandle(Itinerary itinerary, ItinerarySegment segment) {
        if (itinerary == null || itinerary.getSegmentList() == null || segment == null ) {
            return null;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getSegmentWithSameHandle", "Search in itinerary " + itinerary.toString()));
        Date departure = segment.getDepartureDateTime();
        Date arrival = segment.getArrivalDateTime();
        for (ItinerarySegment cmpSegment : itinerary.getSegmentList()) {
            if (segment == cmpSegment || segment.getId() != null && segment.getId().equals(cmpSegment.getId())) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getSegmentWithSameHandle",
                        "Found same segment = " + cmpSegment.toString()));
                return cmpSegment;
            }
            int departureCompare = DateUtils.getDateComparator(false).compare(departure, cmpSegment.getDepartureDateTime());
            int arrivalCompare = DateUtils.getDateComparator(false).compare(arrival, cmpSegment.getArrivalDateTime());
            if (departureCompare == 0 && arrivalCompare == 0) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getSegmentWithSameHandle",
                        "Found segment with same virtual handle = " + cmpSegment.toString()));
                return cmpSegment;
            }
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "getSegmentWithSameHandle", "No match"));
        return null;
    }

    /**
     * Retrieves the first position of the segment with the given ID.
     * @param itinerary The Itinerary to search in.
     * @param segmentId The segment ID to search for.
     * @return The position if found. Otherwise -1.
     */
    public int getSegmentPositionById(Itinerary itinerary, String segmentId) {
        int position = -1;
        if (itinerary != null && itinerary.getSegmentList() != null && itinerary.getSegmentList().size() > 0
                && !StringUtilities.isNullOrEmpty(segmentId)) {
            for (ItinerarySegment segment : itinerary.getSegmentList()) {
                position++;
                if (segmentId.equals(segment.getId())) {
                    return position;
                }
            }
        }
        return position;
    }

    public void refreshAssignableItineraries(final String expenseReportKey, final IRequestListener requestor) {
        final BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        this.receiverList.add(receiver);

        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, requestor) {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                assignableItineraryList.put(expenseReportKey, (List<AssignableItinerary>) resultData
                        .getSerializable(BundleId.ASSIGNABLE_ITINERARIES));
                super.onRequestSuccess(resultData);
            }
        });

        GetAssignableItinerariesRequest request = new GetAssignableItinerariesRequest(context, receiver ,expenseReportKey);
        request.execute();
    }

    public void assignItinerary(String rptKey, String itinKey, final IRequestListener requestor) {
        final BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        this.receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, requestor));

        AssignItineraryRequest request = new AssignItineraryRequest(context, receiver, rptKey, itinKey);
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "assignItinerary", "Start Task."));
        request.execute();
    }

    public void unassignItinerary(String rptKey, String itinKey, final IRequestListener requestor) {
        final BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        this.receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, requestor));

        UnassignItineraryRequest request = new UnassignItineraryRequest(context, receiver, rptKey, itinKey);
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "unassignItinerary", "Start Task."));
        request.execute();
    }

    public List<AssignableItinerary> getAssignableItineraryList(String expenseReportKey) {
        List<AssignableItinerary> list = assignableItineraryList.get(expenseReportKey);
        if (list == null) {
            list = new ArrayList<AssignableItinerary>();
        }

        return list;
    }

}
