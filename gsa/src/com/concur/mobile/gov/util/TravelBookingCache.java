package com.concur.mobile.gov.util;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.gov.travel.activity.OpenOrExistingAuthListItem;
import com.concur.mobile.gov.travel.activity.TDYPerDiemLocationItem;
import com.concur.mobile.gov.travel.data.GenerateAuthNumRow;
import com.concur.mobile.gov.travel.data.PerDiemListRow;
import com.concur.mobile.gov.travel.data.TANumberListRow;
import com.concur.mobile.gov.travel.service.AuthNumsReply;
import com.concur.mobile.gov.travel.service.DocInfoFromTripLocatorReply;
import com.concur.mobile.gov.travel.service.PerDiemLocationListReply;
import com.concur.mobile.gov.travel.service.PerDiemRateReply;

/**
 * This cache class required to store various reply which required to book travel.
 * 
 * @author sunill
 * 
 */
public class TravelBookingCache {

    public static enum BookingSelection {
        AIR ("AIR"),
        HOTEL ("HOTEL"),
        CAR ("CAR"),
        RAIL ("RAIL");

        private String name;

        /**
         * Constructor an instance of <code>BookingSelection</code>.
         * 
         * @param name
         *            the booking name.
         */
        BookingSelection(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return
         *         the name of the enum value.
         */
        String getName() {
            return name;
        }
    };

    public static final String EXISTING_AUTH_FILTER = "Auth Without Reservations";
    public static final String OPEN_AUTH_FILTER = "Open Auth";
    public static final String GROUP_AUTH_FILTER = "Group Auth";

    private AuthNumsReply authNumsReply;
    private PerDiemRateReply perDiemRateReply;
    private PerDiemLocationListReply perDiemLocationListReply;

    private BookingSelection selectedBookingType;
    private LocationChoice selectedLocation;

    private GenerateAuthNumRow generateAuthNum;

    private List<TANumberListRow> openGroupAutList;
    private List<TANumberListRow> existingAuthList;
    private List<PerDiemListRow> perdiemList;

    private OpenOrExistingAuthListItem selectedAuthItem;
    private TDYPerDiemLocationItem selectedPerDiemItem;

    private boolean isGenerateAuthUsed;
    private boolean isGroupAuthUsed;
    private boolean isExistingAuthUsed;

    private DocInfoFromTripLocatorReply docInfoFromTripLocatorReply;

    public TravelBookingCache() {
    }

    public AuthNumsReply getAuthNumsReply() {
        return authNumsReply;
    }

    public void setAuthNumsReply(AuthNumsReply authNumsReply) {
        this.authNumsReply = authNumsReply;
    }

    public PerDiemRateReply getPerDiemRateReply() {
        return perDiemRateReply;
    }

    public void setPerDiemRateReply(PerDiemRateReply perDiemRateReply) {
        this.perDiemRateReply = perDiemRateReply;
    }

    public PerDiemLocationListReply getPerDiemLocationListReply() {
        return perDiemLocationListReply;
    }

    public void setPerDiemLocationListReply(PerDiemLocationListReply perDiemLocationListReply) {
        this.perDiemLocationListReply = perDiemLocationListReply;
    }

    /**
     * Get open or group auth list
     * 
     * @param list
     *            : TANumberListRow list
     * @return list of open/group auth
     */
    public List<TANumberListRow> getOpenGroupAuth(List<TANumberListRow> list) {
        List<TANumberListRow> result = new ArrayList<TANumberListRow>();
        if (list == null) {
            return null;
        }
        for (TANumberListRow taNumberListRow : list) {
            if (taNumberListRow.taType.equalsIgnoreCase(OPEN_AUTH_FILTER)
                || taNumberListRow.taType.equalsIgnoreCase(GROUP_AUTH_FILTER)) {
                result.add(taNumberListRow);
            }
        }
        setOpenGroupAutList(result);
        return result;
    }

    /**
     * Get open or group auth list
     * 
     * @param list
     *            : TANumberListRow list
     * @return list of existing auth
     */
    public List<TANumberListRow> getExistingAuth(List<TANumberListRow> list) {
        List<TANumberListRow> result = new ArrayList<TANumberListRow>();
        if (list == null) {
            return null;
        }
        for (TANumberListRow taNumberListRow : list) {
            if (taNumberListRow.taType.equalsIgnoreCase(EXISTING_AUTH_FILTER)) {
                result.add(taNumberListRow);
            }
        }
        setExistingAuthList(result);
        return result;
    }

    public BookingSelection getSelectedBookingType() {
        return selectedBookingType;
    }

    public void setSelectedBookingType(BookingSelection selectedBookingType) {
        this.selectedBookingType = selectedBookingType;
    }

    public GenerateAuthNumRow getGenerateAuthNum() {
        return generateAuthNum;
    }

    public void setGenerateAuthNum(GenerateAuthNumRow generateAuthNum) {
        this.generateAuthNum = generateAuthNum;
    }

    public List<TANumberListRow> getOpenGroupAutList() {
        return openGroupAutList;
    }

    private void setOpenGroupAutList(List<TANumberListRow> openGroupAutList) {
        this.openGroupAutList = openGroupAutList;
    }

    public List<TANumberListRow> getExistingAuthList() {
        return existingAuthList;
    }

    private void setExistingAuthList(List<TANumberListRow> existingAuthList) {
        this.existingAuthList = existingAuthList;
    }

    public LocationChoice getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(LocationChoice selectedLocation) {
        this.selectedLocation = selectedLocation;
    }

    public List<PerDiemListRow> getPerDiemList() {
        return perdiemList;
    }

    public void setPerDiemList(List<PerDiemListRow> list) {
        this.perdiemList = list;
    }

    public OpenOrExistingAuthListItem getSelectedAuthItem() {
        return selectedAuthItem;
    }

    public void setSelectedAuthItem(OpenOrExistingAuthListItem selectedAuthItem) {
        this.selectedAuthItem = selectedAuthItem;
    }

    public TDYPerDiemLocationItem getSelectedPerDiemItem() {
        return selectedPerDiemItem;
    }

    public void setSelectedPerDiemItem(TDYPerDiemLocationItem selectedPerDiemItem) {
        this.selectedPerDiemItem = selectedPerDiemItem;
    }

    public boolean isGenerateAuthUsed() {
        return isGenerateAuthUsed;
    }

    public void setGenerateAuthUsed(boolean isGenerateAuthUsed) {
        this.isGenerateAuthUsed = isGenerateAuthUsed;
    }

    public boolean isGroupAuthUsed() {
        return isGroupAuthUsed;
    }

    public void setGroupAuthUsed(boolean isGroupAuthUsed) {
        this.isGroupAuthUsed = isGroupAuthUsed;
    }

    public boolean isExistingAuthUsed() {
        return isExistingAuthUsed;
    }

    public void setExistingAuthUsed(boolean isExistingAuthUsed) {
        this.isExistingAuthUsed = isExistingAuthUsed;
    }

    public DocInfoFromTripLocatorReply getDocumentListReply() {
        return docInfoFromTripLocatorReply;
    }

    public void setDocumentListReply(DocInfoFromTripLocatorReply documentListReply) {
        this.docInfoFromTripLocatorReply = documentListReply;
    }

}
