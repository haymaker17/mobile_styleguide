package com.concur.mobile.core.expense.travelallowance.ui.model;

import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by D049515 on 22.10.2015.
 */
public class SelectedTravelAllowancesList extends ArrayList<FixedTravelAllowance> implements Serializable {

    private static final long serialVersionUID = -4569469434863394584L;

    private boolean isBreakfastMultiSelected;
    private boolean isLunchMultiSelected;
    private boolean isDinnerMultiSelected;
    private boolean isLodgingTypeMultiSelected;
    private boolean isOvernightMultiSelected;
    private boolean hasOnlyLastDays;

    public SelectedTravelAllowancesList() {
        super();
    }

    public SelectedTravelAllowancesList(Collection<FixedTravelAllowance> allowances) {
        super(allowances);
        checkMultiSelection((List<FixedTravelAllowance>) allowances);
    }

    private void checkMultiSelection(Collection<FixedTravelAllowance> allowances) {

        ArrayList<FixedTravelAllowance> list = new ArrayList<FixedTravelAllowance>(allowances);
        FixedTravelAllowance template = getTemplate();
        if (template == null) {
            return;
        }

        for (FixedTravelAllowance ta:  list) {
            if (ta == null) {
                continue;
            }
            if (!iCodeEqual(template.getBreakfastProvision(), ta.getBreakfastProvision())) {
                isBreakfastMultiSelected = true;
            }

            if (!iCodeEqual(template.getLunchProvision(), ta.getLunchProvision())) {
                isLunchMultiSelected = true;
            }

            if (!iCodeEqual(template.getDinnerProvision(), ta.getDinnerProvision())) {
                isDinnerMultiSelected = true;
            }

            if (!iCodeEqual(template.getLodgingType(), ta.getLodgingType())) {
                isLodgingTypeMultiSelected = true;
            }

            if (!ta.isLastDay() && ta.getOvernightIndicator() != template.getOvernightIndicator()) {
                isOvernightMultiSelected = true;
            }
        }
    }

    @Override
    public boolean add(FixedTravelAllowance object) {
        if (object  == null) {
            return false;
        }
        boolean result = super.add(object);
        checkMultiSelection(this);
        return result;
    }

    @Override
    public void add(int index, FixedTravelAllowance object) {
        if (object  == null) {
            return;
        }
        super.add(index, object);
        checkMultiSelection(this);
    }

    @Override
    public boolean addAll(Collection<? extends FixedTravelAllowance> collection) {
        if (collection  == null) {
            return false;
        }
        boolean result = super.addAll(collection);
        checkMultiSelection(this);
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends FixedTravelAllowance> collection) {
        if (collection  == null) {
            return false;
        }
        boolean result = super.addAll(index, collection);
        checkMultiSelection(this);
        return result;
    }


    private boolean iCodeEqual(ICode a, ICode b) {

        if ((a != null && b == null) || (a == null && b != null)) {
            return false;
        }

        if (a == null && b == null) {
            return true;
        }

        return a.equals(b);
    }

    public FixedTravelAllowance getTemplate() {
        List<FixedTravelAllowance> list = this;
        if (list == null || list.size() == 0) {
            return null;
        }
        FixedTravelAllowance result = null;
        for (FixedTravelAllowance ta: list) {
            if (ta != null && !ta.isLastDay()) {
                result = ta;
                break;
            }
        }

        if (result == null) {
            hasOnlyLastDays = true;
            return list.get(0);
        } else {
            return result;
        }
    }

    public boolean isBreakfastMultiSelected() {
        return isBreakfastMultiSelected;
    }

    public boolean isLunchMultiSelected() {
        return isLunchMultiSelected;
    }

    public boolean isDinnerMultiSelected() {
        return isDinnerMultiSelected;
    }

    public boolean isLodgingTypeMultiSelected() {
        return isLodgingTypeMultiSelected;
    }

    public boolean isOvernightMultiSelected() {
        return isOvernightMultiSelected;
    }

    public boolean hasOnlyLastDays() {
        return hasOnlyLastDays;
    }
}
