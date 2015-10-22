package com.concur.mobile.core.expense.travelallowance.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.service.GetTAFixedAllowancesRequest2;
import com.concur.mobile.core.expense.travelallowance.service.IRequestListener;
import com.concur.mobile.core.expense.travelallowance.service.UpdateFixedAllowances;
import com.concur.mobile.core.expense.travelallowance.ui.model.SelectedTravelAllowancesList;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael Becherer on 23-Jun-15.
 */
public class FixedTravelAllowanceController extends BaseController {

    private static final String CLASS_TAG = FixedTravelAllowanceController.class.getSimpleName();

    private final class MealProvisionWithIndicator extends MealProvision {

        private boolean indicator;

        public MealProvisionWithIndicator(ICode mealProvision, boolean indicator) {
            super(mealProvision.getCode(), mealProvision.getDescription());
            this.indicator = indicator;
        }

        public boolean getIndicator() {
            return indicator;
        }
    }

    private List<BaseAsyncResultReceiver> receiverList;

    /**
     * The list of registered listeners being notified from this controller as soon as data
     * requests have been answered.
     */
//    private List<IServiceRequestListener> listeners;

    private GetTAFixedAllowancesRequest2 getFixedAllowancesRequest2;

    /**
     * The context needed in order to access resources
     */
    private Context context;

    /**
     * The list of travel allowances this controller is dealing with
     */
    private List<FixedTravelAllowance> fixedTravelAllowances;

    /**
     * This map contains the same FixedTravelAllowance objects. Map is used for efficient access to specific FixedTravelAllowance
     * objects.
     */
    private Map<String, FixedTravelAllowance> fixedTAIdMap;

    private FixedTravelAllowanceControlData controlData;


    /**
     * Creates an instance and initializes the object
     */
    public FixedTravelAllowanceController(Context context) {
        this.fixedTravelAllowances = new ArrayList<FixedTravelAllowance>();
        this.fixedTAIdMap = new HashMap<String, FixedTravelAllowance>();
        this.context = context;
        this.receiverList = new ArrayList<BaseAsyncResultReceiver>();
//        this.listeners = new ArrayList<IServiceRequestListener>();
    }

    protected void setFixedTAList(List<FixedTravelAllowance> list) {
        this.fixedTravelAllowances = list;
        Collections.sort(fixedTravelAllowances);
        this.fixedTAIdMap = new HashMap<String, FixedTravelAllowance>();
        fillTAMap();
    }

    /**
     * Reads the allowances associated with the expene report from the backend
     * @param expenseReportKey
     * @return true, if request has been sent, otherwise false
     */
    public boolean refreshFixedTravelAllowances(String expenseReportKey, final IRequestListener requestor) {

        if (StringUtilities.isNullOrEmpty(expenseReportKey)) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshFixedTravelAllowances", "Report Key is null! Refused." ));
            return false;
        }
        Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshFixedTravelAllowances", "Report Key " + expenseReportKey));
        this.fixedTravelAllowances = new ArrayList<FixedTravelAllowance>();
        this.controlData = new FixedTravelAllowanceControlData();

        if (getFixedAllowancesRequest2 != null && getFixedAllowancesRequest2.getStatus() != AsyncTask.Status.FINISHED) {
            // There is already an async task which is not finished yet. Return silently and let the task finish his work first.
            return false;
        }

        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, requestor) {

            @Override
            public void onRequestSuccess(Bundle resultData) {
                if (resultData != null && resultData.containsKey(BundleId.FIXED_TRAVEL_ALLOWANCE_CONTROL_DATA)) {
                    controlData = (FixedTravelAllowanceControlData) resultData.getSerializable(BundleId.FIXED_TRAVEL_ALLOWANCE_CONTROL_DATA);
                }
                fixedTravelAllowances = getFixedAllowancesRequest2.getFixedTravelAllowances();
                //mealsProvisionLabels = getFixedAllowancesRequest2.getMealsProvisionLabelMap();
                fillTAMap();

                int size = 0;
                if (fixedTravelAllowances != null) {
                    Collections.sort(fixedTravelAllowances);
                    size = fixedTravelAllowances.size();
                }
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG,
                        "refreshFixedTravelAllowances->onRequestSuccess", "Reading fixed TAs, Size = " + size));
                notifyListener(ControllerAction.REFRESH, true, resultData);
                super.onRequestSuccess(resultData);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG,
                        "refreshFixedTravelAllowances->onRequestFail", "Reading fixed TAs failed"));
                notifyListener(ControllerAction.REFRESH, false, resultData);
                super.onRequestFail(resultData);
            }
        });

        getFixedAllowancesRequest2 = new GetTAFixedAllowancesRequest2(context, receiver, expenseReportKey);
        getFixedAllowancesRequest2.execute();
        return true;
    }

    private void fillTAMap() {
        for (FixedTravelAllowance ta : fixedTravelAllowances) {
            fixedTAIdMap.put(ta.getFixedTravelAllowanceId(), ta);
        }
    }

    public FixedTravelAllowance getFixedTA(String fixedTAId) {
        return fixedTAIdMap.get(fixedTAId);
    }

    /**
     * Get the list of fixed travel allowances
     * @return Fixed Travel Allowance list.
     */
    public List<FixedTravelAllowance> getFixedTravelAllowances() {
        return this.fixedTravelAllowances;
    }

    /**
     * Groups fixed travel allowances according to their natural sorting order from where a
     * list is derived, which contains two kind of objects: locations and allowances.
     * The list will be lead by the first location found in the sorted list followed by its
     * allowances pointing to the same location until the next different location will lead
     * the next group of allowances within the result list.
     *
     * @return The sorted and grouped list containing locations and allowances. If there is no
     * data the method will return an empty list.
     */
    public List<Object> getLocationsAndAllowances() {

        List<String> sortedLocations = new ArrayList<String>();
        List<FixedTravelAllowance> fixedTAList = new ArrayList<FixedTravelAllowance>(this.fixedTravelAllowances);
        Collections.sort(fixedTAList);
        Map<String, List<FixedTravelAllowance>> fixedTAGroups = new HashMap<String, List<FixedTravelAllowance>>();
        List<Object> locationAndTAList = new ArrayList<Object>();

        for (FixedTravelAllowance allowance : fixedTAList) {
            List<FixedTravelAllowance> taList;
            if (fixedTAGroups.containsKey(allowance.getLocationName())) {
                taList = fixedTAGroups.get(allowance.getLocationName());
                taList.add(allowance);
            } else {
                taList = new ArrayList<FixedTravelAllowance>();
                taList.add(allowance);
                fixedTAGroups.put(allowance.getLocationName(), taList);
                sortedLocations.add(allowance.getLocationName());
            }
        }

        if (fixedTAGroups.keySet().size() > 1) {
            for (String key : sortedLocations) {
                locationAndTAList.add(key);
                for (FixedTravelAllowance value : fixedTAGroups.get(key)) {
                    locationAndTAList.add(value);
                }
            }
        } else {
            locationAndTAList.addAll(fixedTAList);
        }

        return locationAndTAList;
    }

    /**
     * Checks, whether the fixed travel allowances are associated with several different
     * Locations
     *
     * @return true, if the fixed travel allowances are associated with several different
     * locations
     */
    public boolean hasMultipleGroups() {

        boolean multipleGroups = false;
        Iterator<FixedTravelAllowance> it = fixedTravelAllowances.iterator();
        while (multipleGroups == false && it.hasNext()) {
            FixedTravelAllowance allowance = it.next();
            if (allowance.getLocationName() != null
                    && !allowance.getLocationName().equals(fixedTravelAllowances.get(0).getLocationName())) {
                multipleGroups = true;
            }
        }
        return multipleGroups;
    }

    /**
     * Sums up relevant travel allowances
     * @return The sum
     */
    public double getSum() {
        double sum = 0.0;
        for (FixedTravelAllowance allowance: fixedTravelAllowances) {
            if (!allowance.getExcludedIndicator()) {
                sum += allowance.getAmount();
            }
        }
        return sum;
    }

    /**
     * Returns a string representing the allowance period
     * @param dateFormatter To format the dates
     * @return
     */
    public String getPeriod(IDateFormat dateFormatter) {

        List<FixedTravelAllowance> sortedList = new ArrayList<FixedTravelAllowance>(this.fixedTravelAllowances);
        Date startDate;
        Date endDate;

        Collections.sort(sortedList, Collections.reverseOrder());
        startDate = sortedList.get(0).getDate();
        endDate = sortedList.get(sortedList.size() - 1).getDate();

        return DateUtils.startEndDateToString(startDate, endDate, dateFormatter, false, false, true);
    }

    /**
     * Builds a string based on the meals provisions (breakfast, lunch and dinner), such
     * as "Provided: Breakfast, Lunch; Business Meal: Dinner" for the given fixed travel allowance.
     * Therefore the provision characteristics of breakfast, lunch and dinner (e.g. "Provided")
     * are used for grouping. The order within the result is given by the rule:
     * breakfast before lunch before dinner.
     * With parameter maxGroups the maximum number of groups can be specified.
     *
     * @param allowance The allowance holding the data
     * @param maxGroups The maximum level of string concatenation. Supports values greater 0.
     * @return The textual representation of the meal provisions. Empty String, if
     * there is nothing.
     */
    public String mealsProvisionToText(FixedTravelAllowance allowance, int maxGroups) {

        if (this.context == null) {
            return StringUtilities.EMPTY_STRING;
        }

        if (maxGroups < 1) {
            return StringUtilities.EMPTY_STRING;
        }

        if (allowance == null) {
            return StringUtilities.EMPTY_STRING;
        }

        String resultString = StringUtilities.EMPTY_STRING;
        List<String> mealsList;
       
        List<MealProvisionWithIndicator> sortedProvisions = new ArrayList<MealProvisionWithIndicator>();
        //List<MealProvision> checkBoxProvisions = new ArrayList<MealProvision>();
        Map<ICode, List<String>> provisionMap = new HashMap<ICode, List<String>>();

        if (allowance.getBreakfastProvision() != null && !StringUtilities.isNullOrEmpty(allowance.getBreakfastProvision().getCode())
                && !allowance.getBreakfastProvision().getCode().equals(MealProvision.NOT_PROVIDED_CODE)) {

            mealsList = new ArrayList<String>();
            //mealsList.add(context.getString(R.string.itin_breakfast));
            mealsList.add(this.controlData.getLabel(FixedTravelAllowanceControlData.BREAKFAST_PROVIDED_LABEL));
            provisionMap.put(allowance.getBreakfastProvision(), mealsList);
            MealProvisionWithIndicator sortedProvision = new MealProvisionWithIndicator(allowance.getBreakfastProvision(),
                    controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX));
            sortedProvisions.add(sortedProvision);
        }

        if (allowance.getLunchProvision() != null && !StringUtilities.isNullOrEmpty(allowance.getLunchProvision().getCode())
                && !allowance.getLunchProvision().getCode().equals(MealProvision.NOT_PROVIDED_CODE)) {
            if (provisionMap.containsKey(allowance.getLunchProvision())) {
                mealsList = provisionMap.get(allowance.getLunchProvision());
                //mealsList.add(context.getString(R.string.itin_lunch));
                mealsList.add(this.controlData.getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL));
            } else {
                mealsList = new ArrayList<String>();
                //mealsList.add(context.getString(R.string.itin_lunch));
                mealsList.add(this.controlData.getLabel(FixedTravelAllowanceControlData.LUNCH_PROVIDED_LABEL));
                provisionMap.put(allowance.getLunchProvision(), mealsList);
                MealProvisionWithIndicator sortedProvision = new MealProvisionWithIndicator(allowance.getLunchProvision(),
                        controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX));
                sortedProvisions.add(sortedProvision);
            }
        }

        if (allowance.getDinnerProvision() != null && !StringUtilities.isNullOrEmpty(allowance.getDinnerProvision().getCode())
                && !allowance.getDinnerProvision().getCode().equals(MealProvision.NOT_PROVIDED_CODE)) {
            if (provisionMap.containsKey(allowance.getDinnerProvision())) {
                mealsList = provisionMap.get(allowance.getDinnerProvision());
                //mealsList.add(context.getString(R.string.itin_dinner));
                mealsList.add(this.controlData.getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL));
            } else {
                mealsList = new ArrayList<String>();
                //mealsList.add(context.getString(R.string.itin_dinner));
                mealsList.add(this.controlData.getLabel(FixedTravelAllowanceControlData.DINNER_PROVIDED_LABEL));
                provisionMap.put(allowance.getDinnerProvision(), mealsList);
                MealProvisionWithIndicator sortedProvision = new MealProvisionWithIndicator(allowance.getDinnerProvision(),
                        controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX));
                sortedProvisions.add(sortedProvision);
            }
        }

        int groupCount = 0;
        for (MealProvisionWithIndicator key : sortedProvisions) {

            groupCount++;
            if (!key.getIndicator()) {
                resultString = resultString + key + ": ";
            }
            int memberCount = 0;
            for (String value : provisionMap.get(key)) {
                memberCount++;
                resultString = resultString + value;
                if (memberCount < provisionMap.get(key).size()) {
                    resultString = resultString + ", ";
                }
            }
            if (groupCount + 1 > maxGroups) {
                break;
            }

            if (groupCount + 1 <= sortedProvisions.size()) {
                resultString = resultString + "; ";
            }

        }
        return resultString;
    }


    public FixedTravelAllowanceControlData getControlData() {
        if (controlData == null) {
            controlData = new FixedTravelAllowanceControlData();
        }
        return controlData;
    }

    public boolean showBreakfastProvision() {
        boolean showCheckBox = controlData
                .getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX);
        boolean showPickList = controlData
                .getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST);

        if (!showCheckBox && !showPickList) {
            return false;
        } else {
            return true;
        }
    }

    public boolean showLunchProvision() {
        boolean showCheckBox = controlData
                .getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX);
        boolean showPickList = controlData
                .getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST);

        if (!showCheckBox && !showPickList) {
            return false;
        } else {
            return true;
        }
    }

    public boolean showDinnerProvision() {
        boolean showCheckBox = controlData
                .getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX);
        boolean showPickList = controlData
                .getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST);

        if (!showCheckBox && !showPickList) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Updates the given allowances
     * @param allowances The list of allowances to be updated
     * @param expenseReportKey The expense report the allowances are associated with
     * @return true, if the request has been sent
     */
    public boolean executeUpdate(List<FixedTravelAllowance> allowances, String expenseReportKey, final IRequestListener listener) {
        if (StringUtilities.isNullOrEmpty(expenseReportKey)) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdate", "Report Key is null! Refused." ));
            return false;
        }
        if (allowances == null || allowances.size() == 0) {
            Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdate", "Allowance list is null or empty! Refused." ));
            return false;
        }
        BaseAsyncResultReceiver receiver = new BaseAsyncResultReceiver(new Handler());
        receiverList.add(receiver);
        receiver.setListener(new AsyncReplyListenerImpl(receiverList, receiver, listener) {
            @Override
            public void onRequestSuccess(Bundle resultData) {
                boolean isSuccess = resultData.getBoolean(BundleId.IS_SUCCESS);
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdate->onRequestSuccess", "isSuccess = " + isSuccess));
                notifyListener(ControllerAction.UPDATE, isSuccess, resultData);
                super.onRequestSuccess(resultData);
            }

            @Override
            public void onRequestFail(Bundle resultData) {
                Log.d(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "executeUpdate->onRequestFail", "Failed!"));
                notifyListener(ControllerAction.UPDATE, false, resultData);
                super.onRequestFail(resultData);
            }
        });

        UpdateFixedAllowances request = new UpdateFixedAllowances(context, receiver, expenseReportKey, allowances);
        request.execute();
        return true;
    }

    public FixedTravelAllowance getAllowanceByDate(Date date){
        if (date == null){
            return null;
        }
        for (FixedTravelAllowance allowance : fixedTravelAllowances){
            if (date.equals(allowance.getDate())){
                return  allowance;
            }
        }
        return null;
    }

    public SelectedTravelAllowancesList getSelectedTravelAllowances() {
        SelectedTravelAllowancesList result = new SelectedTravelAllowancesList();
        for (FixedTravelAllowance ta: getFixedTravelAllowances()) {
            if (ta.isSelected()) {
                result.add(ta);
            }
        }

        return result;
    }

    public void unselectAll() {
        for (FixedTravelAllowance ta: getSelectedTravelAllowances()) {
            ta.setIsSelected(false);
        }
    }

    public void selectAll() {
        for (FixedTravelAllowance ta: getFixedTravelAllowances()) {
            if (!ta.isLocked()) {
                ta.setIsSelected(true);
            }
        }
    }

    public List<FixedTravelAllowance> applyTaValues (FixedTravelAllowance fromTa, SelectedTravelAllowancesList toTaList) {
        for (FixedTravelAllowance ta: toTaList) {
            if (!toTaList.isBreakfastMultiSelected()) {
                ta.setBreakfastProvision((MealProvision) fromTa.getBreakfastProvision());
            }

            if (!toTaList.isLunchMultiSelected()) {
                ta.setLunchProvision((MealProvision) fromTa.getLunchProvision());
            }

            if (!toTaList.isDinnerMultiSelected()) {
                ta.setDinnerProvision((MealProvision) fromTa.getDinnerProvision());
            }

            if (!toTaList.isLodgingTypeMultiSelected()) {
                ta.setLodgingType((LodgingType) fromTa.getLodgingType());
            }


            if (!ta.isLastDay() && !toTaList.hasOnlyLastDays()) {
                ta.setOvernightIndicator(fromTa.getOvernightIndicator());
            }

        }

        return toTaList;
    }


}
